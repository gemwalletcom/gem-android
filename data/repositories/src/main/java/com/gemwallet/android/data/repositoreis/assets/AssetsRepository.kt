package com.gemwallet.android.data.repositoreis.assets

import com.gemwallet.android.blockchain.operators.GetAsset
import com.gemwallet.android.cases.device.GetDeviceIdCase
import com.gemwallet.android.cases.tokens.SearchTokensCase
import com.gemwallet.android.cases.transactions.GetTransactions
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.service.store.database.AssetsDao
import com.gemwallet.android.data.service.store.database.AssetsPriorityDao
import com.gemwallet.android.data.service.store.database.BalancesDao
import com.gemwallet.android.data.service.store.database.PricesDao
import com.gemwallet.android.data.service.store.database.entities.DbAsset
import com.gemwallet.android.data.service.store.database.entities.DbAssetConfig
import com.gemwallet.android.data.service.store.database.entities.DbAssetMarket
import com.gemwallet.android.data.service.store.database.entities.DbAssetWallet
import com.gemwallet.android.data.service.store.database.entities.DbBalance
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
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class AssetsRepository @Inject constructor(
    private val assetsDao: AssetsDao,
    private val assetsPriorityDao: AssetsPriorityDao,
    private val balancesDao: BalancesDao,
    private val pricesDao: PricesDao,
    private val gemApi: GemApiClient,
    private val sessionRepository: SessionRepository,
    private val balancesRemoteSource: BalancesRemoteSource,
    getTransactions: GetTransactions,
    private val searchTokensCase: SearchTokensCase,
    private val getDeviceIdCase: GetDeviceIdCase,
    private val priceClient: PriceWebSocketClient,
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
                changeCurrency(it?.currency ?: return@collectLatest)
            }
        }

        scope.launch(Dispatchers.IO) {
            syncSwapSupportChains()
        }
    }

    suspend fun sync() = withContext(Dispatchers.IO) {
        getAssetsInfo().firstOrNull()?.updateBalances()?.awaitAll()
    }

    suspend fun syncAssetInfo(assetId: AssetId, account: Account) = withContext(Dispatchers.IO) {
        val updateBalancesJob = async { updateBalances(assetId) }
        val getAssetFull = async { syncMarketInfo(assetId, account) }
        updateBalancesJob.await()
        getAssetFull.await()
    }

    suspend fun syncMarketInfo(assetId: AssetId, owner: Account?) = withContext(Dispatchers.IO) {
        val assetInfo = if (owner == null) {
            getTokensInfo(listOf(assetId.toIdentifier())).map { it.firstOrNull() }
        } else {
            getAssetInfo(assetId)
        }.firstOrNull() ?: return@withContext
        val currency = assetInfo.price?.currency ?: Currency.USD
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
            isSellEnabled = assetFull.properties.isSellable == true,
            isStakeEnabled = assetFull.properties.isStakeable == true,
            isSwapEnabled = assetInfo.id().chain.isSwapSupport(),
            stakingApr = assetFull.properties.stakingApr,
            rank = assetFull.score.rank,
            updatedAt = System.currentTimeMillis(),
        )
        val linkRecords = assetFull.links.toAssetLinkRecord(assetId)
        val marketRecord = marketInfo?.market?.toRecord(assetId) ?: DbAssetMarket(assetId.toIdentifier())
        assetsDao.update(record)
        runCatching { assetsDao.insert(linkRecords, marketRecord) }
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

    fun getTokenInfo(assetId: AssetId): Flow<AssetInfo?> {
        return assetsDao.getAssetInfo(assetId.toIdentifier(), assetId.chain).flatMapLatest {
            if (it == null) {
                assetsDao.getTokenInfo(assetId.toIdentifier(), assetId.chain).map { it?.toModel() }
            } else {
                flow { emit(it.toModel()) }
            }
        }
    }

    fun getAssetInfo(assetId: AssetId): Flow<AssetInfo?> {
        return assetsDao.getAssetInfo(assetId.toIdentifier(), assetId.chain).map { it?.toModel() }
    }

    fun getTokensInfo(assetsId: List<String>): Flow<List<AssetInfo>> {
        return assetsDao.getAssetsInfoByAllWallets(assetsId).toAssetInfoModel()
    }

    fun search(query: String, byAllWallets: Boolean): Flow<List<AssetInfo>> {
        val query = query.trim()
        return if (byAllWallets) {
            assetsPriorityDao.hasPriorities(query).map { it > 0 }.flatMapLatest {
                if (it) {
                    assetsDao.searchByAllWalletsWithPriority(query)
                } else {
                    assetsDao.searchByAllWallets(query)
                }
            }

        } else {
            assetsPriorityDao.hasPriorities(query).map { it > 0 }.flatMapLatest {
                if (it) {
                    assetsDao.searchWithPriority(query)
                } else {
                    assetsDao.search(query)
                }
            }
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

    suspend fun resolve(wallet: Wallet, assetsId: List<AssetId>) = withContext(Dispatchers.IO) {
        if (assetsId.isEmpty()) return@withContext
        val assetsFull = gemApi.getAssets(assetsId.map { it.toIdentifier() }).getOrNull() ?: return@withContext
        assetsFull.forEach {
            val asset = it.asset
            add(wallet.id, wallet.getAccount(asset.chain())?.address ?: return@forEach, asset, true)
            runCatching { assetsDao.addLinks(it.links.toAssetLinkRecord(asset.id)) }
        }

        val balancesJob = async(Dispatchers.IO) {
            getAssetsInfo(assetsId).firstOrNull()?.updateBalances()
        }
        balancesJob.await()
    }

    fun importAssets(wallet: Wallet) = scope.launch(Dispatchers.IO) {
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
        }
        wallet.accounts.filter { !Chain.exclude().contains(it.chain) }.map {
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
    fun invalidateDefault(wallet: Wallet) = scope.launch(Dispatchers.IO) {
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
    }

    suspend fun switchVisibility(
        walletId: String,
        owner: Account,
        assetId: AssetId,
        visibility: Boolean,
    ) = withContext(Dispatchers.IO) {
        runCatching {
            assetsDao.linkAssetToWallet(
                DbAssetWallet(
                    walletId = walletId,
                    assetId = assetId.toIdentifier(),
                    accountAddress = owner.address
                )
            )
        }
        setVisibility(walletId, assetId, visibility)
        if (visibility) {
            updateBalances(assetId)
        }
    }

    suspend fun togglePin(walletId: String, assetId: AssetId) {
        val config = assetsDao.getConfig(walletId, assetId.toIdentifier()) ?: DbAssetConfig(
            walletId = walletId,
            assetId = assetId.toIdentifier(),
        )
        runCatching { assetsDao.setConfig(config.copy(isVisible = true, isPinned = !config.isPinned)) }
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
        val defaultScore = uniffi.gemstone.assetDefaultRank(asset.chain().string)
        runCatching { assetsDao.insert(asset.toRecord(defaultScore), link, config) }

        if (visible) {
            priceClient.addAssetId(asset.id)
        }
    }

    suspend fun updateBuyAvailable(assets: List<String>) {
        assetsDao.resetBuyAvailable()
        assetsDao.updateBuyAvailable(assets)
    }

    suspend fun updateSellAvailable(assets: List<String>) {
        assetsDao.resetSellAvailable()
        assetsDao.updateSellAvailable(assets)
    }

    private fun onTransactions(txs: List<TransactionExtended>) = scope.launch {
        txs.map { txEx ->
            async {
                getAssetsInfo(txEx.transaction.getAssociatedAssetIds()).firstOrNull()?.updateBalances()
            }
        }.awaitAll()
    }

    fun getAssetLinks(id: AssetId): Flow<List<AssetLink>> {
        return assetsDao.getAssetLinks(id.toIdentifier()).toAssetLinksModel()
    }

    fun getAssetMarket(id: AssetId): Flow<AssetMarket?> {
        return assetsDao.getAssetMarket(id.toIdentifier()).map { it?.toModel() }
    }

    private fun Account.isVisibleByDefault(type: WalletType): Boolean {
        return visibleByDefault.contains(chain) || type != WalletType.multicoin
    }

    private suspend fun setVisibility(walletId: String, assetId: AssetId, visibility: Boolean) = withContext(Dispatchers.IO) {
        val config = assetsDao.getConfig(walletId = walletId, assetId = assetId.toIdentifier())
            ?: DbAssetConfig(assetId = assetId.toIdentifier(), walletId = walletId)
        runCatching { assetsDao.setConfig(config.copy(isVisible = visibility)) }

        if (visibility) {
            priceClient.addAssetId(assetId)
        }
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
            dbNativeBalance?.let { runCatching { balancesDao.insert(it) } }

            val delegationBalances = balancesRemoteSource.getDelegationBalances(account)
            val dbFullBalance = DbBalance.mergeDelegation(dbNativeBalance, delegationBalances
                ?.toRecord(walletId, account.address, updatedAt))
            dbFullBalance?.let { runCatching { balancesDao.insert(it) } }
            dbFullBalance?.toModel()
        }

        val getTokens = async {
            val balances = balancesRemoteSource.getTokensBalances(account, tokens)
            runCatching {
                balancesDao.insert(
                    balances.map {
                        it.toRecord(
                            walletId,
                            account.address,
                            updatedAt
                        )
                    }
                )
            }
            balances
        }
        listOfNotNull(getNative.await()) + getTokens.await()
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

    private suspend fun changeCurrency(currency: Currency) {
        val rate = pricesDao.getRates(currency)?.toModel() ?: return
        pricesDao.getAll().firstOrNull()?.map {
            it.copy(value = (it.usdValue ?: 0.0) * rate.rate, currency = currency.string)
        }?.let { pricesDao.insert(it) }
    }
}