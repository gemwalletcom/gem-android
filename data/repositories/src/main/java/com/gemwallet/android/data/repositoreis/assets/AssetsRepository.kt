package com.gemwallet.android.data.repositoreis.assets

import com.gemwallet.android.blockchain.operators.GetAsset
import com.gemwallet.android.cases.device.GetDeviceIdCase
import com.gemwallet.android.cases.tokens.SearchTokensCase
import com.gemwallet.android.cases.transactions.GetTransactions
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.service.store.database.AssetsDao
import com.gemwallet.android.data.service.store.database.BalancesDao
import com.gemwallet.android.data.service.store.database.PricesDao
import com.gemwallet.android.data.service.store.database.entities.DbAsset
import com.gemwallet.android.data.service.store.database.entities.DbAssetConfig
import com.gemwallet.android.data.service.store.database.entities.DbAssetMarket
import com.gemwallet.android.data.service.store.database.entities.DbAssetWallet
import com.gemwallet.android.data.service.store.database.entities.DbBalance
import com.gemwallet.android.data.service.store.database.entities.DbPrice
import com.gemwallet.android.data.service.store.database.entities.mergeDelegation
import com.gemwallet.android.data.service.store.database.entities.mergeNative
import com.gemwallet.android.data.service.store.database.entities.toAssetInfoModel
import com.gemwallet.android.data.service.store.database.entities.toAssetLinkRecord
import com.gemwallet.android.data.service.store.database.entities.toAssetLinksModel
import com.gemwallet.android.data.service.store.database.entities.toModel
import com.gemwallet.android.data.service.store.database.entities.toRecord
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.chain
import com.gemwallet.android.ext.exclude
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.getAssociatedAssetIds
import com.gemwallet.android.ext.isSwapSupport
import com.gemwallet.android.ext.swapSupport
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.AssetBalance
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.TransactionExtended
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetLink
import com.wallet.core.primitives.AssetMarket
import com.wallet.core.primitives.AssetPricesRequest
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssetsRepository @Inject constructor(
    private val assetsDao: AssetsDao,
    private val balancesDao: BalancesDao,
    private val pricesDao: PricesDao,
    private val gemApi: GemApiClient,
    private val sessionRepository: SessionRepository,
    private val balancesRemoteSource: BalancesRemoteSource,
    getTransactions: GetTransactions,
    private val searchTokensCase: SearchTokensCase,
    private val getDeviceIdCase: GetDeviceIdCase,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : GetAsset {
    private val visibleByDefault = listOf(Chain.Ethereum, Chain.Bitcoin, Chain.SmartChain, Chain.Solana)

    init {
        scope.launch(Dispatchers.IO) {
            getTransactions.getChangedTransactions().collect {
                onTransactions(it)
            }
        }
        scope.launch(Dispatchers.IO) {
            sessionRepository.session().collectLatest {
                sync(it?.currency ?: return@collectLatest)
            }
        }

        scope.launch(Dispatchers.IO) {
            syncSwapSupportChains()
        }
    }

    suspend fun sync(currency: Currency) = withContext(Dispatchers.IO) {
        val balancesJob = getAssetsInfo().firstOrNull()?.updateBalances()
        val pricesJob = async(Dispatchers.IO) { updatePrices(currency) }
        balancesJob?.awaitAll()
        pricesJob.await()
    }

    suspend fun syncAssetInfo(assetId: AssetId, account: Account) = withContext(Dispatchers.IO) {
        val currency = getAssetInfo(assetId).firstOrNull()?.price?.currency ?: return@withContext
        val updatePriceJob = async { updatePrices(currency, assetId) }
        val updateBalancesJob = async { updateBalances(assetId) }
        val getAssetFull = async { syncMarketInfo(assetId, account) }
        updatePriceJob.await()
        updateBalancesJob.await()
        getAssetFull.await()
    }

    suspend fun syncMarketInfo(assetId: AssetId, owner: Account?) = withContext(Dispatchers.IO) {
        val assetInfo = if (owner == null) {
            getAssetsInfoByAllWallets(listOf(assetId.toIdentifier())).map { it.firstOrNull() }
        } else {
            getAssetInfo(assetId)
        }.firstOrNull() ?: return@withContext
        val currency = assetInfo.price?.currency ?: return@withContext
        val assetIdIdentifier = assetId.toIdentifier()
        val assetFullJob = async { gemApi.getAsset(assetIdIdentifier, currency.string).getOrNull() }
        val marketInfoJob = async { gemApi.getMarket(assetIdIdentifier, currency.string).getOrNull() }

        val assetFull = assetFullJob.await() ?: return@withContext
        val marketInfo = marketInfoJob.await()
        val record = DbAsset(
            id = assetInfo.id().toIdentifier(),
            name = assetFull.asset.name,
            symbol = assetInfo.asset.symbol,
            decimals = assetInfo.asset.decimals,
            type = assetInfo.asset.type,
            chain = assetInfo.asset.chain(),
            isBuyEnabled = assetFull.properties.isBuyable == true,
            isStakeEnabled = assetFull.properties.isStakeable == true,
            isSwapEnabled = assetInfo.id().chain.isSwapSupport(),
            stakingApr = assetFull.properties.stakingApr,
            rank = assetFull.score.rank,
            updatedAt = System.currentTimeMillis(),
        )
        val linkRecords = assetFull.links.toAssetLinkRecord(assetId)
        val marketRecord = marketInfo?.market?.toRecord(assetId) ?: DbAssetMarket(assetId.toIdentifier())
        assetsDao.update(record)
        assetsDao.insert(linkRecords, marketRecord)
    }

    suspend fun getNativeAssets(wallet: Wallet): List<Asset> = withContext(Dispatchers.IO) {
        assetsDao.getNativeWalletAssets(wallet.id)
            .firstOrNull()
            ?.toModel()
            ?: emptyList()
    }

    override suspend fun getAsset(assetId: AssetId): Asset? = withContext(Dispatchers.IO) {
        getAssetInfo(assetId).firstOrNull()?.asset
    }

    fun getAssetsInfo(): Flow<List<AssetInfo>> = assetsDao.getAssetsInfo().toAssetInfoModel()

    fun getAssetsInfo(assetsId: List<AssetId>): Flow<List<AssetInfo>> = assetsDao
        .getAssetsInfo(assetsId.map { it.toIdentifier() })
        .toAssetInfoModel()


    suspend fun searchToken(assetId: AssetId): Boolean {
        return searchTokensCase.search(assetId)
    }

    suspend fun getToken(assetId: AssetId): Flow<Asset?> = withContext(Dispatchers.IO) {
        assetsDao.getTokenInfo(assetId.toIdentifier(), assetId.chain).map { it?.toModel()?.asset }
    }

    fun getAssetInfo(assetId: AssetId): Flow<AssetInfo?> =
        assetsDao.getAssetInfo(assetId.toIdentifier(), assetId.chain).map { it?.toModel() }

    fun getAssetsInfoByAllWallets(assetsId: List<String>): Flow<List<AssetInfo>> {
        return assetsDao.getAssetsInfoByAllWallets(assetsId).toAssetInfoModel()
    }

    fun search(query: String, byAllWallets: Boolean): Flow<List<AssetInfo>> {
        val query = query.trim()
        return if (byAllWallets) {
            assetsDao.searchByAllWallets(query)
        } else {
            assetsDao.search(query)
        }
        .toAssetInfoModel()
        .map {
            it.filter { !Chain.exclude().contains(it.asset.id.chain) }
            .distinctBy { it.asset.id.toIdentifier() }
        }
    }

    fun swapSearch(wallet: Wallet, query: String, byChains: List<Chain>, byAssets: List<AssetId>): Flow<List<AssetInfo>> {
        val query = query.trim()
        val walletChains = wallet.accounts.map { it.chain }
        val includeChains = byChains.filter { walletChains.contains(it) }
        val includeAssetIds = byAssets.filter { walletChains.contains(it.chain) }

        return assetsDao.swapSearch(query, includeChains, includeAssetIds.map { it.toIdentifier() })
            .toAssetInfoModel()
            .map {
                it.filter { !Chain.exclude().contains(it.asset.id.chain) }
                    .distinctBy { it.asset.id.toIdentifier() }
            }
    }

    suspend fun resolve(currency: Currency, wallet: Wallet, assetsId: List<AssetId>) = withContext(Dispatchers.IO) {
        if (assetsId.isEmpty()) return@withContext
        val assetsFull = gemApi.getAssets(assetsId.map { it.toIdentifier() }).getOrNull() ?: return@withContext
        assetsFull.forEach {
            val asset = it.asset
            add(wallet.id, wallet.getAccount(asset.chain())?.address ?: return@forEach, asset, true)
            assetsDao.addLinks(it.links.toAssetLinkRecord(asset.id))
        }

        val balancesJob = async(Dispatchers.IO) {
            getAssetsInfo(assetsId).firstOrNull()?.updateBalances()
        }
        val pricesJob = async(Dispatchers.IO) {
            updatePrices(currency)
        }
        balancesJob.await()
        pricesJob.await()
    }

    /**
     *  Create assets for new wallet(import or create wallet)
     *  */
    suspend fun createAssets(wallet: Wallet) {
        wallet.accounts.filter { !Chain.exclude().contains(it.chain) }
            .map { account ->
                val asset = account.chain.asset()
                val isVisible = account.isVisibleByDefault(wallet.type)
                add(wallet.id, account.address, asset, isVisible)
            }
    }

    fun importAssets(wallet: Wallet, currency: Currency) = scope.launch(Dispatchers.IO) {
        launch(Dispatchers.IO) {
            delay(2000) // Wait subscription - token processing
            val availableAssetsId = gemApi.getAssets(getDeviceIdCase.getDeviceId(), wallet.index).getOrNull()
                ?: return@launch
            availableAssetsId.mapNotNull { it.toAssetId() }.filter { it.tokenId != null }
                .map { assetId ->
                    async {
                        searchTokensCase.search(assetId.tokenId!!)
                        val asset = assetsDao.getAsset(assetId.toIdentifier())?.toModel() ?: return@async null
                        add(
                            walletId = wallet.id,
                            accountAddress = wallet.getAccount(assetId.chain)?.address ?: return@async null,
                            asset = asset,
                            visible = true
                        )
                        asset
                    }
                }
                .awaitAll()
                .filterNotNull()
                .groupBy { it.id.chain }
                .map {
                    async {
                        updateBalances(
                            wallet.id,
                            wallet.getAccount(it.key) ?: return@async,
                            it.value
                        )
                    }
                }
                .awaitAll()

            launch { updatePrices(currency) }

        }
        wallet.accounts.map {
            async {
                val balances = updateBalances(wallet.id, it, emptyList()).firstOrNull()
                if ((balances?.totalAmount ?: 0.0) > 0.0) {
                    setVisibility(wallet.id, it.chain.asset().id, true)
                }
            }
        }.awaitAll()
    }

    /**
     * Check and add new coins and active tokens
     * */
    fun invalidateDefault(wallet: Wallet, currency: Currency) = scope.launch(Dispatchers.IO) {
        val assets = getNativeAssets(wallet).associateBy( { it.id.toIdentifier() }, { it })

        wallet.accounts.filter { !Chain.exclude().contains(it.chain) }
            .map { account ->
                val asset = account.chain.asset()
                async {
                    if (assets[account.chain.string] == null) {
                        add(wallet.id, account.address, asset, false)
                        val balances = updateBalances(wallet.id, account, emptyList()).firstOrNull()
                        if ((balances?.totalAmount ?: 0.0) > 0.0) {
                            setVisibility(wallet.id, asset.id, true)
                        }
                    }
                }
            }.awaitAll()
        scope.launch { updatePrices(currency) }
    }

    private fun Account.isVisibleByDefault(type: WalletType): Boolean {
        return visibleByDefault.contains(chain) || type != WalletType.multicoin
    }

    suspend fun switchVisibility(
        walletId: String,
        owner: Account,
        assetId: AssetId,
        visibility: Boolean,
        currency: Currency,
    ) = withContext(Dispatchers.IO) {
        assetsDao.linkAssetToWallet(
            DbAssetWallet(
            walletId = walletId,
            assetId = assetId.toIdentifier(),
            accountAddress = owner.address
            )
        )
        setVisibility(walletId, assetId, visibility)
        if (visibility) {
            launch { updateBalances(assetId) }
            launch { updatePrices(currency) }
        }
    }

    suspend fun togglePin(walletId: String, assetId: AssetId) {
        val config = assetsDao.getConfig(walletId, assetId.toIdentifier()) ?: DbAssetConfig(
            walletId = walletId,
            assetId = assetId.toIdentifier(),
        )
        assetsDao.setConfig(config.copy(isVisible = true, isPinned = !config.isPinned))
    }

    suspend fun clearPrices() = withContext(Dispatchers.IO) {
        pricesDao.deleteAll()
    }

    suspend fun updatePrices(currency: Currency, vararg assetIds: AssetId) = withContext(Dispatchers.IO) {
        val ids = assetIds.toList().ifEmpty {
            assetsDao.getAll().map { it.id }.toSet().mapNotNull { it.toAssetId() }.toList()
        }
        .map { it.toIdentifier() }
        // TODO: java.lang.ClassCastException:
        //  at com.gemwallet.android.data.repositoreis.assets.AssetsRepository$updatePrices$2.invokeSuspend (AssetsRepository.kt:388)
        val prices = try {
            gemApi.prices(AssetPricesRequest(currency.string, ids)).getOrNull()?.prices ?: emptyList()
        } catch (_: Throwable) {
            emptyList()
        }
        pricesDao.insert(
            prices.map {
                price -> DbPrice(price.assetId, price.price, price.priceChangePercentage24h, currency.string)
            }
        )
    }

    suspend fun updateBalances(vararg tokens: AssetId) {
        getAssetsInfo(tokens.toList()).firstOrNull()?.updateBalances()?.awaitAll()
    }

    suspend fun add(walletId: String, accountAddress: String, asset: Asset, visible: Boolean) = withContext(Dispatchers.IO) {
        val link = DbAssetWallet(
            assetId = asset.id.toIdentifier(),
            walletId = walletId,
            accountAddress = accountAddress
        )
        val config = DbAssetConfig(
            assetId = asset.id.toIdentifier(),
            walletId = walletId,
            isVisible = visible,
        )
        assetsDao.insert(asset.toRecord(), link, config)
    }


    private suspend fun setVisibility(walletId: String, assetId: AssetId, visibility: Boolean) = withContext(Dispatchers.IO) {
        val config = assetsDao.getConfig(walletId = walletId, assetId = assetId.toIdentifier())
            ?: DbAssetConfig(assetId = assetId.toIdentifier(), walletId = walletId)
        assetsDao.setConfig(config.copy(isVisible = visibility))
    }

    private suspend fun syncSwapSupportChains() {
        assetsDao.resetSwapable()
        assetsDao.setSwapable(Chain.swapSupport())
    }

    private suspend fun updateBalances(walletId: String, account: Account, tokens: List<Asset>): List<AssetBalance>  = withContext(Dispatchers.IO) {
        val updatedAt = System.currentTimeMillis()

        val getNative = async {
            val prevBalance = balancesDao.getByAccount(walletId, account.address, account.chain.string)
            val nativeBalance = balancesRemoteSource.getNativeBalances(account)
            val dbNativeBalance = DbBalance.mergeNative(
                prevBalance,
                nativeBalance?.toRecord(walletId, account.address, updatedAt),
            )
            dbNativeBalance?.let { balancesDao.insert(it) }

            val delegationBalances = balancesRemoteSource.getDelegationBalances(account)
            val dbFullBalance = DbBalance.mergeDelegation(dbNativeBalance, delegationBalances
                ?.toRecord(walletId, account.address, updatedAt))
            dbFullBalance?.let { balancesDao.insert(it) }
            dbFullBalance?.toModel()
        }

        val getTokens = async {
            val balances = balancesRemoteSource.getTokensBalances(account, tokens)
            balancesDao.insert(balances.map { it.toRecord(walletId, account.address, updatedAt) })
            balances
        }
        listOfNotNull(getNative.await()) + getTokens.await()
    }

    suspend fun updateBayAvailable(assets: List<String>) {
        assetsDao.resetBuyAvailable()
        assetsDao.updateBuyAvailable(assets)
    }

    private fun onTransactions(txs: List<TransactionExtended>) = scope.launch {
        txs.map { txEx ->
            async {
                getAssetsInfo(txEx.transaction.getAssociatedAssetIds()).firstOrNull()?.updateBalances()
            }
        }.awaitAll()
    }

    private suspend fun List<AssetInfo>.updateBalances(): List<Deferred<List<AssetBalance>>> = withContext(Dispatchers.IO) {
        println()
        groupBy { it.walletId }
            .mapValues { wallet ->
                val walletId = wallet.key ?: return@mapValues null
                wallet.value.groupBy { it.asset.chain() }
                    .mapKeys { it.value.firstOrNull()?.owner }
                    .mapValues { entry -> entry.value.filter { it.metadata?.isEnabled == true }.map { it.asset } }
                    .mapNotNull { entry ->
                        val account: Account = entry.key ?: return@mapNotNull null
                        if (entry.value.isEmpty()) {
                            return@mapNotNull null
                        }
                        async {
                            updateBalances(walletId, account, entry.value)
                        }
                    }
            }
            .mapNotNull { it.value }
            .flatten()
    }

    fun getAssetLinks(id: AssetId): Flow<List<AssetLink>> {
        return assetsDao.getAssetLinks(id.toIdentifier()).toAssetLinksModel()
    }

    fun getAssetMarket(id: AssetId): Flow<AssetMarket?> {
        return assetsDao.getAssetMarket(id.toIdentifier()).map { it?.toModel() }
    }
}