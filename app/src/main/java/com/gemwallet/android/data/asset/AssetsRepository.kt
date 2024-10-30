package com.gemwallet.android.data.asset

import android.util.Log
import com.gemwallet.android.blockchain.operators.GetAsset
import com.gemwallet.android.cases.tokens.GetTokensCase
import com.gemwallet.android.cases.tokens.SearchTokensCase
import com.gemwallet.android.cases.transactions.GetTransactionsCase
import com.gemwallet.android.data.config.ConfigRepository
import com.gemwallet.android.data.database.AssetsDao
import com.gemwallet.android.data.database.BalancesDao
import com.gemwallet.android.data.database.PricesDao
import com.gemwallet.android.data.database.entities.DbAsset
import com.gemwallet.android.data.database.entities.DbAssetConfig
import com.gemwallet.android.data.database.entities.DbBalance
import com.gemwallet.android.data.database.entities.DbPrice
import com.gemwallet.android.data.database.mappers.AssetInfoMapper
import com.gemwallet.android.data.repositories.chains.ChainInfoRepository
import com.gemwallet.android.data.repositories.session.SessionRepository
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ext.type
import com.gemwallet.android.model.AssetBalance
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.AssetPriceInfo
import com.gemwallet.android.model.Balance
import com.gemwallet.android.model.Balances
import com.gemwallet.android.model.SyncState
import com.gemwallet.android.services.GemApiClient
import com.google.gson.Gson
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetLinks
import com.wallet.core.primitives.AssetMarket
import com.wallet.core.primitives.AssetMetaData
import com.wallet.core.primitives.AssetPrice
import com.wallet.core.primitives.AssetPricesRequest
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.TransactionExtended
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.map
import kotlin.collections.mapNotNull

@Singleton
class AssetsRepository @Inject constructor(
    private val assetsDao: AssetsDao,
    private val balancesDao: BalancesDao,
    private val pricesDao: PricesDao,
    private val gemApi: GemApiClient,
    private val sessionRepository: SessionRepository,
    private val balancesRemoteSource: BalancesRemoteSource,
    private val configRepository: ConfigRepository,
    getTransactionsCase: GetTransactionsCase,
    private val getTokensCase: GetTokensCase,
    private val searchTokensCase: SearchTokensCase,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : GetAsset {
    private val visibleByDefault = listOf(Chain.Ethereum, Chain.Bitcoin, Chain.SmartChain, Chain.Solana)

    private var syncJob: Deferred<Unit>? = null

    private val _syncState = MutableStateFlow(SyncState.Idle)
    val syncState: Flow<SyncState> = _syncState

    init {
        scope.launch(Dispatchers.IO) {
            getTransactionsCase.getChangedTransactions().collect {
                onTransactions(it)
            }
        }
        scope.launch(Dispatchers.IO) {
            sessionRepository.session().collectLatest {
                sync(it?.currency ?: return@collectLatest)
            }
        }
    }

    suspend fun sync(currency: Currency) = withContext(Dispatchers.IO) {
        val syncJob = syncJob
        if (syncJob?.isActive == true) {
            try {
                syncJob.cancel()
            } catch (err: Throwable) {
                Log.d("ASSETS_REPOSITORY", "Error on cancel job", err)
            }
            finally {
                _syncState.tryEmit(SyncState.Idle)
            }
        }
        this@AssetsRepository.syncJob = async {
            _syncState.tryEmit(SyncState.InSync)
            val balancesJob = async(Dispatchers.IO) {
                getAssetsInfo().firstOrNull()?.updateBalances()?.awaitAll()
            }
            val pricesJob = async(Dispatchers.IO) {
                updatePrices(currency)
            }
            balancesJob.await()
            pricesJob.await()
            _syncState.tryEmit(SyncState.Idle)
        }
    }

    suspend fun syncAssetInfo(assetId: AssetId) = withContext(Dispatchers.IO) {
        val assetInfo = getAssetInfo(assetId).firstOrNull() ?: return@withContext
        val currency = assetInfo.price?.currency ?: return@withContext

        val updatePriceJob = async { updatePrices(currency, assetId) }
        val updateBalancesJob = async { updateBalances(assetId) }
        val getAssetFull = async {
            val assetFull = gemApi.getAsset(assetId.toIdentifier(), currency.string)
                .getOrNull() ?: return@async

            val gson = Gson()
            assetsDao.getById(assetId.toIdentifier()).map {
                it.copy(
                    isBuyEnabled = assetFull.details?.isBuyable == true,
                    isSwapEnabled = assetFull.details?.isBuyable == true,
                    isStakeEnabled = assetFull.details?.isSwapable == true,
                    stakingApr = assetFull.details?.stakingApr,
                    links =  assetFull.details?.links?.let { gson.toJson(it) },
                    market = assetFull.market?.let { gson.toJson(it) },
                    rank = assetFull.score.rank,
                )
            }.forEach {
                assetsDao.update(it)
            }
        }
        updatePriceJob.await()
        updateBalancesJob.await()
        getAssetFull.await()
    }

    suspend fun getNativeAssets(wallet: Wallet): List<Asset> = withContext(Dispatchers.IO) {
        assetsDao.getAssetsByType(wallet.accounts.map { it.address }).mapNotNull {
            Asset( // TODO: Mapper
                id = it.id.toAssetId() ?: return@mapNotNull null,
                name = it.name,
                symbol = it.symbol,
                decimals = it.decimals,
                type = it.type,
            )
        }
    }

    override suspend fun getAsset(assetId: AssetId): Asset? = withContext(Dispatchers.IO) {
        val room = assetsDao.getById(assetId.toIdentifier()).firstOrNull() ?: return@withContext null
        Asset( // TODO: Mapper
            id = assetId,
            name = room.name,
            symbol = room.symbol,
            decimals = room.decimals,
            type = room.type,
        )
    }

    suspend fun getStakeApr(assetId: AssetId): Double? = withContext(Dispatchers.IO) {
        assetsDao.getById(assetId.toIdentifier()).firstOrNull()?.stakingApr
    }

    @Deprecated("Use the getAssetInfo() method")
    suspend fun getById(wallet: Wallet, assetId: AssetId): List<AssetInfo> {
        val assetInfos = getById(wallet.accounts, assetId)
        val result = if (assetInfos.isEmpty()) {
            val tokens = getTokensCase.getByIds(listOf(assetId))
            tokens.mapNotNull { token ->
                AssetInfo(
                    owner = wallet.getAccount(token.id.chain) ?: return@mapNotNull null,
                    asset = token,
                )
            }
        } else {
            assetInfos
        }
        return result
    }

    fun getAssetsInfo(): Flow<List<AssetInfo>> = assetsDao.getAssetsInfo()
        .map { AssetInfoMapper().asDomain(it) }
        .map { assets -> assets.filter { !ChainInfoRepository.exclude.contains(it.asset.id.chain) } }

    fun getAssetsInfo(assetsId: List<AssetId>): Flow<List<AssetInfo>> = assetsDao
        .getAssetsInfo(assetsId.map { it.toIdentifier() })
        .map { AssetInfoMapper().asDomain(it) }
        .map { assets ->
            assetsId.map { id ->
                assets.firstOrNull { it.asset.id.toIdentifier() == id.toIdentifier() }
                    ?: getTokensCase.assembleAssetInfo(id)
            }.filterNotNull()
        }

    suspend fun getAssetInfo(assetId: AssetId): Flow<AssetInfo> = withContext(Dispatchers.IO) {
        assetsDao.getAssetInfo(assetId.toIdentifier(), assetId.chain)
            .map { AssetInfoMapper().asDomain(it).firstOrNull() }
            .mapNotNull { it ?: getTokensCase.assembleAssetInfo(assetId) }
    }

    fun search(wallet: Wallet, query: String): Flow<List<AssetInfo>> {
        val assetsFlow = assetsDao.searchAssetInfo(query).map { AssetInfoMapper().asDomain(it) }
        val tokensFlow = getTokensCase.getByChains(wallet.accounts.map { it.chain }, query)
        return combine(assetsFlow, tokensFlow) { assets, tokens ->
            assets + tokens.mapNotNull { asset ->
                AssetInfo(
                    asset = asset,
                    owner = wallet.getAccount(asset.id.chain) ?: return@mapNotNull null,
                )
            }
            .filter { !ChainInfoRepository.exclude.contains(it.asset.id.chain) }
            .distinctBy { it.asset.id.toIdentifier() }
        }
    }

    suspend fun getAssetByTokenId(chain: Chain, address: String): Asset? = withContext(Dispatchers.IO) {
        val assetId = AssetId(chain, address)
        searchTokensCase.search(assetId)
        getTokensCase.getByIds(listOf(assetId)).firstOrNull()
    }

    fun invalidateDefault(wallet: Wallet, currency: Currency) = scope.launch(Dispatchers.IO) {
        val assets = assetsDao.getAssetsInfoByAccounts(wallet.accounts.map { it.address })
            .map { AssetInfoMapper().asDomain(it) }
            .firstOrNull()
            ?.associateBy( { it.asset.id.toIdentifier() }, { it } )?: emptyMap()

        wallet.accounts.filter { !ChainInfoRepository.exclude.contains(it.chain) }
            .map { account ->
                Pair(account, account.chain.asset())
            }.map {
                val isNew = assets[it.first.chain.string] == null
                val isVisible = assets[it.second.id.toIdentifier()]?.metadata?.isEnabled
                    ?: visibleByDefault.contains(it.first.chain) || wallet.type != WalletType.multicoin
                add(wallet.id, it.first.address, it.second, isVisible)
                async {
                    if (isNew) {
                        val balances = updateBalances(it.first, emptyList()).firstOrNull()
                        if ((balances?.calcTotal()?.atomicValue?.compareTo(BigInteger.ZERO) ?: 0) > 0) {
                            setVisibility(wallet.id, it.second.id, true)
                        }
                    }
                }
            }.awaitAll()
        scope.launch { updatePrices(currency) }
        delay(2000) // Wait subscription
        val availableAssets = gemApi.getAssets(configRepository.getDeviceId(), wallet.index).getOrNull() ?: return@launch
        availableAssets.mapNotNull {
            it.toAssetId()
        }.filter {
            it.tokenId != null
        }.map { assetId ->
            async {
                val account =
                    wallet.getAccount(assetId.chain) ?: return@async
                searchTokensCase.search(assetId.tokenId!!)
                switchVisibility(wallet.id, account, assetId, true, currency)
            }
        }.awaitAll()
    }

    suspend fun switchVisibility(
        walletId: String,
        owner: Account,
        assetId: AssetId,
        visibility: Boolean,
        currency: Currency,
    ) = withContext(Dispatchers.IO) {
        val assetResult = getById(listOf(owner), assetId)
        if (assetResult.isEmpty()) {
            val asset = getTokensCase.getByIds(listOf(assetId))
            assetsDao.insert(asset.map { modelToRoom(owner.address, it)})
        }
        setVisibility(walletId, assetId, visibility)
        launch { updateBalances(assetId) }
        launch { updatePrices(currency, assetId) }
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
        val prices = gemApi.prices(AssetPricesRequest(currency.string, ids))
            .getOrNull()?.prices ?: emptyList()
        pricesDao.insert(
            prices.map {
                    price -> DbPrice(price.assetId, price.price, price.priceChangePercentage24h)
            }
        )
    }

    suspend fun updateBalances(vararg tokens: AssetId) {
        getAssetsInfo(tokens.toList()).firstOrNull()?.updateBalances()?.awaitAll()
    }

    suspend fun saveOrder(walletId: String, order: List<AssetId>) {
        val records = order.mapIndexed { index, assetId ->
            val assetConfig = assetsDao.getConfig(walletId, assetId.toIdentifier()) ?: DbAssetConfig(
                assetId = assetId.toIdentifier(),
                walletId = walletId,
            )
            assetConfig.copy(listPosition = index)
        }
        assetsDao.setConfig(records)
    }

    suspend fun add(walletId: String, address: String, asset: Asset, visible: Boolean) = withContext(Dispatchers.IO) {
        assetsDao.insert(modelToRoom(address, asset))

        assetsDao.setConfig(
            DbAssetConfig(
                assetId = asset.id.toIdentifier(),
                walletId = walletId,
                isVisible = visible,
            )
        )
    }

    private suspend fun setVisibility(walletId: String, assetId: AssetId, visibility: Boolean) = withContext(Dispatchers.IO) {
        val config = assetsDao.getConfig(walletId, assetId.toIdentifier())
            ?: DbAssetConfig(walletId, assetId.toIdentifier())
        assetsDao.setConfig(config.copy(isVisible = visibility, isPinned = false))
    }

    private suspend fun updateBalances(account: Account, tokens: List<AssetId>): List<Balances> {
        val balances = balancesRemoteSource.getBalances(account, tokens)
        val updatedAt = System.currentTimeMillis()
        balancesDao.insert(
            balances.map { it.items }.flatten().map {
                DbBalance(
                    assetId = it.assetId.toIdentifier(),
                    address = account.address,
                    type = it.balance.type,
                    amount = it.balance.value,
                    updatedAt = updatedAt,
                )
            }
        )
        return balances
    }

    private fun onTransactions(txs: List<TransactionExtended>) = scope.launch {
        txs.map { tx ->
            async {
                val tokens = mutableListOf<AssetId>().apply {
                    if (tx.asset.id.type() == AssetSubtype.TOKEN) {
                        add(tx.asset.id)
                    }
                    if (tx.feeAsset.id.type() == AssetSubtype.TOKEN) {
                        add(tx.asset.id)
                    }
                }
                updateBalances(Account(tx.transaction.assetId.chain, tx.transaction.from, ""), tokens)
            }
        }.awaitAll()
    }

    private suspend fun List<AssetInfo>.updateBalances(): List<Deferred<List<Balances>>> = withContext(Dispatchers.IO) {
        groupBy { it.owner.chain }
            .mapKeys { it.value.firstOrNull()?.owner }
            .mapValues { entry -> entry.value.filter { it.metadata?.isEnabled == true }.map { it.asset.id } }
            .mapNotNull { entry ->
                val account = entry.key ?: return@mapNotNull null
                if (entry.value.isEmpty()) {
                    return@mapNotNull null
                }
                async {
                    updateBalances(account, entry.value)
                }
            }
    }

    private suspend fun getById(accounts: List<Account>, assetId: AssetId): List<AssetInfo> = withContext(Dispatchers.IO) {
        val dbAssetId = listOf(assetId.toIdentifier())
        val addresses = accounts.map { it.address }.toSet().toList()
        val assets = assetsDao.getById(addresses, dbAssetId)
        if (assets.isEmpty()) {
            return@withContext emptyList()
        }
        val balances = balancesDao.getByAssetId(addresses, dbAssetId)
        val prices = pricesDao.getByAssets(assets.map { it.id }).map {
            AssetPriceInfo(price = AssetPrice(it.assetId, it.value, it.dayChanged), currency = Currency.USD)
        }
        assets.mapNotNull { asset ->
            val assetId = asset.id.toAssetId() ?: return@mapNotNull null
            val account = accounts.firstOrNull { it.address == asset.address && it.chain == assetId.chain }
                ?: return@mapNotNull null
            roomToModel(Gson(), assetId, account, asset, balances, prices)
        }
    }

    private fun roomToModel(
        gson: Gson,
        assetId: AssetId,
        account: Account,
        room: DbAsset,
        balances: List<DbBalance>,
        prices: List<AssetPriceInfo>
    ) = AssetInfo(
        owner = account,
        asset = Asset(
            id = assetId,
            name = room.name,
            symbol = room.symbol,
            decimals = room.decimals,
            type = room.type,
        ),
        balances = Balances(
            balances
                .filter { it.assetId == room.id && it.address == room.address }
                .map { AssetBalance(assetId, Balance(value = it.amount, type = it.type)) }
        ),
        price = prices.firstOrNull { it.price.assetId ==  room.id},
        metadata = AssetMetaData(
            isEnabled = true, // TODO: Deprecated
            isBuyEnabled = room.isBuyEnabled,
            isSwapEnabled = room.isSwapEnabled,
            isStakeEnabled = room.isStakeEnabled,
            isPinned = false,
        ),
        links = if (room.links != null) gson.fromJson(room.links, AssetLinks::class.java) else null,
        market = if (room.market != null) gson.fromJson(room.market, AssetMarket::class.java) else null,
        rank = room.rank,
    )

    private fun modelToRoom(address: String, asset: Asset) = DbAsset(
        id = asset.id.toIdentifier(),
        address = address,
        name = asset.name,
        symbol = asset.symbol,
        decimals = asset.decimals,
        type = asset.type,
        createdAt = System.currentTimeMillis(),
    )
}