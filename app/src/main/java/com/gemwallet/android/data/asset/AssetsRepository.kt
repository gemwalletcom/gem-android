package com.gemwallet.android.data.asset

import android.util.Log
import com.gemwallet.android.blockchain.operators.GetAsset
import com.gemwallet.android.cases.transactions.GetTransactionsCase
import com.gemwallet.android.data.chains.ChainInfoLocalSource
import com.gemwallet.android.data.config.ConfigRepository
import com.gemwallet.android.data.repositories.session.SessionRepository
import com.gemwallet.android.data.tokens.TokensRepository
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ext.type
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Balances
import com.gemwallet.android.model.SyncState
import com.gemwallet.android.services.GemApiClient
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
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

@Singleton
class AssetsRepository @Inject constructor(
    private val gemApi: GemApiClient,
    private val sessionRepository: SessionRepository,
    private val tokensRepository: TokensRepository,
    private val assetsLocalSource: AssetsLocalSource,
    private val balancesRemoteSource: BalancesRemoteSource,
    private val configRepository: ConfigRepository,
    getTransactionsCase: GetTransactionsCase,
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
                assetsLocalSource.getAssetsInfo().firstOrNull()?.updateBalances()?.awaitAll()
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
        val assetInfo = assetsLocalSource.getAssetInfo(assetId).firstOrNull() ?: return@withContext
        val currency = assetInfo.price?.currency ?: return@withContext

        val updatePriceJob = async { updatePrices(currency, assetId) }
        val updateBalancesJob = async { updateBalances(assetId) }
        val getAssetFull = async {
            val assetFull = gemApi.getAsset(assetId.toIdentifier(), currency.string)
                .getOrNull() ?: return@async

            assetsLocalSource.setAssetDetails(
                assetId = assetId,
                buyable = assetFull.details?.isBuyable ?: false,
                swapable = assetFull.details?.isSwapable ?: false,
                stakeable = assetFull.details?.isStakeable ?: false,
                links = assetFull.details?.links,
                market = assetFull.market,
                rank = assetFull.score.rank,
                stakingApr = assetFull.details?.stakingApr,
            )
        }
        updatePriceJob.await()
        updateBalancesJob.await()
        getAssetFull.await()
    }

    suspend fun getNativeAssets(wallet: Wallet): Result<List<Asset>> = withContext(Dispatchers.IO) {
        assetsLocalSource.getNativeAssets(wallet.accounts)
    }

    override suspend fun getAsset(assetId: AssetId): Asset? {
        return assetsLocalSource.getById(assetId = assetId)
    }

    suspend fun getStakeApr(assetId: AssetId): Double? = withContext(Dispatchers.IO) {
        assetsLocalSource.getStakingApr(assetId)
    }

    @Deprecated("Use the getAssetInfo() method")
    suspend fun getById(wallet: Wallet, assetId: AssetId): Result<List<AssetInfo>> {
        val assetInfos = assetsLocalSource.getById(wallet.accounts, assetId).getOrNull()
        val result = if (assetInfos.isNullOrEmpty()) {
            val tokens = tokensRepository.getByIds(listOf(assetId))
            tokens.mapNotNull { token ->
                AssetInfo(
                    owner = wallet.getAccount(token.id.chain) ?: return@mapNotNull null,
                    asset = token,
                )
            }
        } else {
            assetInfos
        }
        return Result.success(result)
    }

    fun getAssetsInfo(): Flow<List<AssetInfo>> = assetsLocalSource.getAssetsInfo().map { assets ->
        assets.filter { !ChainInfoLocalSource.exclude.contains(it.asset.id.chain) }
    }

    fun getAssetsInfo(assetsId: List<AssetId>): Flow<List<AssetInfo>> = assetsLocalSource.getAssetsInfo(assetsId).map { assets ->
        assetsId.map { id ->
            assets.firstOrNull { it.asset.id.toIdentifier() == id.toIdentifier() }
                ?: tokensRepository.assembleAssetInfo(id)
        }.filterNotNull()
    }

    suspend fun getAssetInfo(assetId: AssetId): Flow<AssetInfo> = withContext(Dispatchers.IO) {
        assetsLocalSource.getAssetInfo(assetId).mapNotNull {
            it ?: tokensRepository.assembleAssetInfo(assetId)
        }
    }

    suspend fun search(wallet: Wallet, query: String): Flow<List<AssetInfo>> {
        val assetsFlow = assetsLocalSource.search(query)
        val tokensFlow = tokensRepository.getByChains(wallet.accounts.map { it.chain }, query)
        return combine(assetsFlow, tokensFlow) { assets, tokens ->
            assets + tokens.mapNotNull { asset ->
                AssetInfo(
                    asset = asset,
                    owner = wallet.getAccount(asset.id.chain) ?: return@mapNotNull null,
                )
            }
            .filter { !ChainInfoLocalSource.exclude.contains(it.asset.id.chain) }
            .distinctBy { it.asset.id.toIdentifier() }
        }
    }

    suspend fun getAssetByTokenId(chain: Chain, address: String): Asset? = withContext(Dispatchers.IO) {
        val assetId = AssetId(chain, address)
        tokensRepository.search(assetId)
        tokensRepository.getByIds(listOf(assetId)).firstOrNull()
    }

    suspend fun invalidateDefault(wallet: Wallet, currency: Currency) = scope.launch {
        val assets = assetsLocalSource.getAssetsInfo(wallet.accounts)
            .associateBy( { it.asset.id.toIdentifier() }, { it } )
        wallet.accounts.filter { !ChainInfoLocalSource.exclude.contains(it.chain) }
            .map { account ->
                Pair(account, account.chain.asset())
            }.map {
                val isNew = assets[it.first.chain.string] == null
                val isVisible = assets[it.second.id.toIdentifier()]?.metadata?.isEnabled
                    ?: visibleByDefault.contains(it.first.chain) || wallet.type != WalletType.multicoin
                assetsLocalSource.add(wallet.id, it.first.address, it.second, isVisible)
                async {
                    if (isNew) {
                        val balances = updateBalances(it.first, emptyList()).firstOrNull()
                        if ((balances?.calcTotal()?.atomicValue?.compareTo(BigInteger.ZERO) ?: 0) > 0) {
                            assetsLocalSource.setVisibility(wallet.id, it.first, it.second.id, true)
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
                tokensRepository.search(assetId.tokenId!!)
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
        val assetResult = assetsLocalSource.getById(listOf(owner), assetId)
        if (assetResult.isFailure || assetResult.getOrNull()?.isEmpty() != false) {
            val asset = tokensRepository.getByIds(listOf(assetId))
            assetsLocalSource.add(owner.address, asset)
        }
        assetsLocalSource.setVisibility(walletId, owner, assetId, visibility)
        launch { updateBalances(assetId) }
        launch { updatePrices(currency, assetId) }
    }

    suspend fun togglePin(walletId: String, assetId: AssetId) {
        assetsLocalSource.togglePinned(walletId, assetId)
    }

    suspend fun clearPrices() = withContext(Dispatchers.IO) {
        assetsLocalSource.clearPrices()
    }

    suspend fun updatePrices(currency: Currency, vararg assetIds: AssetId) = withContext(Dispatchers.IO) {
        val ids = assetIds.toList().ifEmpty { assetsLocalSource.getAllAssetsIds() }
            .map { it.toIdentifier() }
        val prices = gemApi.prices(AssetPricesRequest(currency.string, ids))
            .getOrNull()?.prices ?: emptyList()
        assetsLocalSource.setPrices(prices)
    }

    suspend fun updateBalances(vararg tokens: AssetId) {
        assetsLocalSource.getAssetsInfo(tokens.toList()).firstOrNull()
            ?.updateBalances()
            ?.awaitAll()
    }

    private suspend fun updateBalances(account: Account, tokens: List<AssetId>): List<Balances> {
        val balances = balancesRemoteSource.getBalances(account, tokens)
        assetsLocalSource.setBalances(account, balances)
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

    suspend fun saveOrder(walletId: String, order: List<AssetId>) {
        assetsLocalSource.saveOrder(walletId, order)
    }
}