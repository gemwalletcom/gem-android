package com.gemwallet.android.data.repositoreis.assets

import com.gemwallet.android.application.transactions.coordinators.GetChangedTransactions
import com.gemwallet.android.blockchain.operators.GetAsset
import com.gemwallet.android.blockchain.services.BalancesService
import com.gemwallet.android.cases.assets.AddRecentActivity
import com.gemwallet.android.cases.assets.GetRecent
import com.gemwallet.android.cases.tokens.SearchTokensCase
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.tokens.toPriorityQuery
import com.gemwallet.android.data.service.store.database.AssetsDao
import com.gemwallet.android.data.service.store.database.AssetsPriorityDao
import com.gemwallet.android.data.service.store.database.BalancesDao
import com.gemwallet.android.data.service.store.database.PricesDao
import com.gemwallet.android.data.service.store.database.entities.DbAsset
import com.gemwallet.android.data.service.store.database.entities.DbAssetConfig
import com.gemwallet.android.data.service.store.database.entities.DbAssetMarket
import com.gemwallet.android.data.service.store.database.entities.DbAssetWallet
import com.gemwallet.android.data.service.store.database.entities.DbRecentActivity
import com.gemwallet.android.data.service.store.database.entities.toAssetInfoModel
import com.gemwallet.android.data.service.store.database.entities.toAssetLinkRecord
import com.gemwallet.android.data.service.store.database.entities.toAssetLinksModel
import com.gemwallet.android.data.service.store.database.entities.toDTO
import com.gemwallet.android.data.service.store.database.entities.toRecord
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.data.services.gemapi.GemDeviceApiClient
import com.gemwallet.android.domains.asset.chain
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.exclude
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.getAssociatedAssetIds
import com.gemwallet.android.ext.isSwapSupport
import com.gemwallet.android.ext.swapSupport
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ext.type
import com.gemwallet.android.model.AssetBalance
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.RecentType
import com.gemwallet.android.model.TransactionExtended
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetLink
import com.wallet.core.primitives.AssetMarket
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.AssetTag
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.FiatRate
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private typealias WalletId = String

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class AssetsRepository @Inject constructor(
    private val assetsDao: AssetsDao,
    private val assetsPriorityDao: AssetsPriorityDao,
    private val balancesDao: BalancesDao,
    private val pricesDao: PricesDao,
    private val gemApi: GemApiClient,
    private val gemDeviceApiClient: GemDeviceApiClient,
    private val sessionRepository: SessionRepository,
    private val balancesService: BalancesService,
    getChangedTransactions: GetChangedTransactions,
    private val searchTokensCase: SearchTokensCase,
    private val priceClient: PriceWebSocketClient,
    private val updateBalances: UpdateBalances = UpdateBalances(balancesDao, balancesService),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : GetAsset, AddRecentActivity, GetRecent {

    private val visibleByDefault = listOf(
        Chain.Ethereum,
        Chain.Bitcoin,
        Chain.SmartChain,
        Chain.Solana,
        Chain.Tron,
    )

    private val importStatus = MutableStateFlow<Map<WalletId, Boolean>>(emptyMap())

    init {
        scope.launch(Dispatchers.IO) {
            getChangedTransactions.getChangedTransactions().collect {
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

    suspend fun syncAssetInfo(assetId: AssetId, wallet: Wallet) = withContext(Dispatchers.IO) {
        val account = wallet.getAccount(assetId) ?: return@withContext
        val asset = getTokenInfo(assetId).firstOrNull()?.asset ?: return@withContext
        add(walletId = wallet.id, accountAddress = account.address, asset = asset, false)
        val updateBalancesJob = async { updateBalances(assetId) }
        val getAssetFull = async { syncMarketInfo(assetId, account) }
        priceClient.addAssetId(assetId)
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
        val assetFullJob = async {
            try {
                gemApi.getAsset(assetIdIdentifier)
            } catch (_: Throwable) {
                null
            }
        }
        val marketInfoJob = async {
            try {
                gemApi.getMarket(assetIdIdentifier, currency.string)
            } catch (_: Throwable) {
                null
            }

        }

        val assetFull = assetFullJob.await() ?: return@withContext
        val marketInfo = marketInfoJob.await()
        val record = DbAsset(
            id = assetInfo.id().toIdentifier(),
            name = assetFull.asset.name,
            symbol = assetInfo.asset.symbol,
            decimals = assetInfo.asset.decimals,
            type = assetInfo.asset.type,
            chain = assetInfo.asset.chain,
            isBuyEnabled = assetFull.properties.isBuyable,
            isSellEnabled = assetFull.properties.isSellable,
            isStakeEnabled = assetFull.properties.isStakeable,
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
            ?.toDTO()
            ?: emptyList()
    }

    override suspend fun getAsset(assetId: AssetId): Asset? = withContext(Dispatchers.IO) {
        getAssetInfo(assetId).firstOrNull()?.asset
    }

    fun getAssetsInfo(): Flow<List<AssetInfo>> = assetsDao.getAssetsInfo()
        .toAssetInfoModel()

    fun getAssetsInfo(assetsId: List<AssetId>): Flow<List<AssetInfo>> = assetsDao
        .getAssetsInfo(assetsId.map { it.toIdentifier() })
        .toAssetInfoModel()
        .flowOn(Dispatchers.IO)


    suspend fun searchToken(assetId: AssetId, currency: Currency): Boolean {
        return searchTokensCase.search(assetId, currency)
    }

    suspend fun getToken(assetId: AssetId): Flow<Asset?> = withContext(Dispatchers.IO) {
        assetsDao.getTokenInfo(assetId.toIdentifier(), assetId.chain).map { it?.toDTO()?.asset }
    }

    fun getTokenInfo(assetId: AssetId): Flow<AssetInfo?> {
        return assetsDao.getAssetInfo(assetId.toIdentifier(), assetId.chain)
            .flatMapLatest { assetInfo ->
            if (assetInfo == null) {
                assetsDao.getTokenInfo(assetId.toIdentifier(), assetId.chain).map { it?.toDTO() }
            } else {
                flow { emit(assetInfo.toDTO()) }
            }
        }
        .flowOn(Dispatchers.IO)
    }

    fun getAssetInfo(assetId: AssetId): Flow<AssetInfo?> {
        return assetsDao.getAssetInfo(assetId.toIdentifier(), assetId.chain)
            .map { it?.toDTO() }
            .flowOn(Dispatchers.IO)
    }

    fun getTokensInfo(assetsId: List<String>): Flow<List<AssetInfo>> {
        return assetsDao.getAssetsInfoByAllWallets(assetsId).toAssetInfoModel()
            .map { items -> items.distinctBy { it.id() } }
    }

    fun search(query: String, tags: List<AssetTag>, byAllWallets: Boolean): Flow<List<AssetInfo>> {
        val query = tags.toPriorityQuery(query)
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
        .map { assets ->
            assets.filter { !Chain.exclude().contains(it.asset.id.chain) }
                .distinctBy { it.asset.id.toIdentifier() }
        }
    }

    fun swapSearch(wallet: Wallet, query: String, byChains: List<Chain>, byAssets: List<AssetId>, tags: List<AssetTag>): Flow<List<AssetInfo>> {
        val query = tags.toPriorityQuery(query)
        val walletChains = wallet.accounts.map { it.chain }
        val includeChains = byChains.filter { walletChains.contains(it) }
        val includeAssetIds = byAssets.filter { walletChains.contains(it.chain) }
        return assetsPriorityDao.hasPriorities(query).map { it > 0 }.flatMapLatest { hasPriority ->
                if (hasPriority) {
                    assetsDao.swapSearchWithPriority(query, includeChains, includeAssetIds.map { it.toIdentifier() })
                } else {
                    assetsDao.swapSearch(query, includeChains, includeAssetIds.map { it.toIdentifier() })
                }
            }
            .toAssetInfoModel()
            .map { assets ->
                assets.filter { !Chain.exclude().contains(it.asset.id.chain) }
                    .distinctBy { it.asset.id.toIdentifier() }
            }
    }

    suspend fun resolve(wallet: Wallet, assetsId: List<AssetId>) = withContext(Dispatchers.IO) {
        if (assetsId.isEmpty()) return@withContext
        try {
            gemApi.getAssets(assetsId).forEach {
                val asset = it.asset
                add(wallet.id, wallet.getAccount(asset.chain)?.address ?: return@forEach, asset, true)
            }
        } catch (_: Throwable) {
            return@withContext
        }

        val balancesJob = async(Dispatchers.IO) {
            getAssetsInfo(assetsId).firstOrNull()?.updateBalances()
        }
        balancesJob.await()
    }

    // TODO: Move out to application/import/coordinators/ImportAssets
    fun importAssets(wallet: Wallet, currency: Currency) = scope.launch(Dispatchers.IO) {
//        TODO: Replace to job retention:
//        importStatus[wallet.id] = scope.launch(Dispatchers.IO) {
//            try {
//
//            } finally {
//                importStatus.update { it.toMutableMap().remove(wallet.id) };
//            }
//        }
        importStatus.update { items ->
            items.toMutableMap().apply { put(wallet.id, true) }
        }

        try {
            val availableAssetsId = gemDeviceApiClient.getAssets(walletId = wallet.id, 0)
            val assetIds = availableAssetsId.mapNotNull { it.toAssetId() }
            val tokenIds = assetIds.filter { it.type() != AssetSubtype.NATIVE }

            searchTokensCase.search(tokenIds, currency)

            assetIds.map { assetId ->
                async {
                    val asset = assetsDao.getAsset(assetId.toIdentifier())?.toDTO() ?: return@async null
                    add(
                        walletId = wallet.id,
                        accountAddress = wallet.getAccount(assetId.chain)?.address ?: return@async null,
                        asset = asset,
                        visible = true
                    )
                    asset
                }
            }.awaitAll()
            sync()
        } catch (_: Throwable) {
            return@launch
        } finally {
            importStatus.update { items ->
                items.toMutableMap().apply { remove(wallet.id) }
            }
        }
    }

    fun importInProgress(walletId: String): Flow<Boolean> {
        return importStatus.mapLatest { it[walletId] == true }
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
                        val balances = updateBalances.updateBalances(wallet.id, account, emptyList()).firstOrNull()
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
        val assetInfo = getAssetInfo(assetId).firstOrNull()
        if (assetInfo?.walletId != walletId) {
            runCatching {
                assetsDao.linkAssetToWallet(
                    DbAssetWallet(
                        walletId = walletId,
                        assetId = assetId.toIdentifier(),
                        accountAddress = owner.address
                    )
                )
            }
        }
        if (assetInfo?.metadata?.isEnabled != visibility) {
            setVisibility(walletId, assetId, visibility)
        }
        if (visibility) {
            updateBalances(assetId)
            priceClient.addAssetId(assetId)
        }
    }

    suspend fun togglePin(walletId: String, assetId: AssetId) = withContext(Dispatchers.IO) {
        val config = assetsDao.getConfig(walletId, assetId.toIdentifier()) ?: DbAssetConfig(
            walletId = walletId,
            assetId = assetId.toIdentifier(),
        )
        runCatching { assetsDao.setConfig(config.copy(isVisible = true, isPinned = !config.isPinned)) }
    }

    suspend fun updateBalances(vararg tokens: AssetId) {
        getAssetsInfo(tokens.toList()).firstOrNull()?.updateBalances()?.awaitAll()
    }

    suspend fun add(walletId: String, accountAddress: String, asset: Asset, visible: Boolean) {
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
        val defaultScore = uniffi.gemstone.assetDefaultRank(asset.chain.string)
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
                getAssetsInfo(txEx.transaction.getAssociatedAssetIds())
                    .firstOrNull()
                    ?.updateBalances()
            }
        }.awaitAll()
    }

    fun getAssetLinks(id: AssetId): Flow<List<AssetLink>> {
        return assetsDao.getAssetLinks(id.toIdentifier())
            .toAssetLinksModel()
            .flowOn(Dispatchers.IO)
    }

    fun getAssetMarket(id: AssetId): Flow<AssetMarket?> {
        return assetsDao.getAssetMarket(id.toIdentifier())
            .map { it?.toDTO() }
            .flowOn(Dispatchers.IO)
    }

    private fun Account.isVisibleByDefault(type: WalletType): Boolean {
        return visibleByDefault.contains(chain) || type != WalletType.Multicoin
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

    private suspend fun List<AssetInfo>.updateBalances(): List<Deferred<List<AssetBalance>>> = withContext(Dispatchers.IO) {
        groupBy { it.walletId }
            .mapValues { wallet ->
                val walletId = wallet.key ?: return@mapValues null
                wallet.value.groupBy { it.asset.chain }
                    .mapKeys { it.value.firstOrNull()?.owner }
                    .mapValues { entry -> entry.value.map { it.asset } }
                    .mapNotNull { entry ->
                        val account: Account = entry.key ?: return@mapNotNull null
                        if (entry.value.isEmpty()) {
                            return@mapNotNull null
                        }
                        async {
                            updateBalances.updateBalances(walletId, account, entry.value)
                        }
                    }
            }
            .mapNotNull { it.value }
            .flatten()
    }

    private suspend fun changeCurrency(currency: Currency) {
        val rate = pricesDao.getRates(currency).map { it?.toDTO() }.firstOrNull() ?: return
        pricesDao.getAll().firstOrNull()?.map {
            it.copy(value = (it.usdValue ?: 0.0) * rate.rate, currency = currency.string)
        }?.let { pricesDao.insert(it) }
    }

    suspend fun getCurrencyRate(currency: Currency): Flow<FiatRate?> {
        return pricesDao.getRates(currency).map { it?.toDTO() }
    }

    override suspend fun addRecentActivity(
        assetId: AssetId,
        walletId: String,
        type: RecentType,
        toAssetId: AssetId?,
    ) {
        return assetsDao.addRecentActivity(
            DbRecentActivity(
                assetId = assetId.toIdentifier(),
                walletId = walletId,
                toAssetId = toAssetId?.toIdentifier(),
                type = type,
                addedAt = System.currentTimeMillis(),
            )
        )
    }

    override fun getRecentActivities(
        type: List<RecentType>
    ): Flow<List<AssetInfo>> {
        return assetsDao.getRecentByType(type).toAssetInfoModel()
    }
}