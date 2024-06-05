package com.gemwallet.android.data.asset

import com.gemwallet.android.blockchain.operators.GetAsset
import com.gemwallet.android.data.chains.ChainInfoLocalSource
import com.gemwallet.android.data.config.ConfigRepository
import com.gemwallet.android.data.tokens.TokensRepository
import com.gemwallet.android.data.transaction.TransactionsRepository
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ext.type
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Balances
import com.gemwallet.android.services.GemApiClient
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.TransactionExtended
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssetsRepository @Inject constructor(
    private val gemApiClient: GemApiClient,
    private val tokensRepository: TokensRepository,
    private val assetsLocalSource: AssetsLocalSource,
    private val balancesRemoteSource: BalancesRemoteSource,
    private val pricesRemoteSource: PricesRemoteSource,
    transactionsRepository: TransactionsRepository,
    private val configRepository: ConfigRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : GetAsset {
    private val visibleByDefault = listOf(Chain.Ethereum, Chain.Bitcoin, Chain.SmartChain, Chain.Solana)
    private val onRefreshAssets = mutableListOf<() -> Unit>() // TODO: Thread safe; Replace to flow

    init {
        transactionsRepository.subscribe(this::onTransactions)
    }

    suspend fun syncTokens(wallet: Wallet, currency: Currency) = withContext(Dispatchers.IO) {
        val balancesJob = async(Dispatchers.IO) {
            updateBalances(wallet)
        }
        val pricesJob = async(Dispatchers.IO) {
            updatePrices(currency)
        }
        balancesJob.await()
        pricesJob.await()
    }

    suspend fun syncAssetInfo(account: Account, assetId: AssetId, currency: Currency) = withContext(Dispatchers.IO) {
        launch { updatePrices(currency, assetId) }
        launch { updateBalances(account, assetId) }
        val assetFull = gemApiClient.getAsset(
            assetId = assetId.toIdentifier(),
            currency = currency.string
        ).getOrNull() ?: return@withContext

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

    suspend fun getNativeAssets(wallet: Wallet): Result<List<Asset>> = withContext(Dispatchers.IO) {
        assetsLocalSource.getNativeAssets(wallet.accounts)
    }

    suspend fun getAllByWallet(wallet: Wallet): Result<List<AssetInfo>> = withContext(Dispatchers.IO) {
        assetsLocalSource.getAllByAccounts(wallet.accounts)
    }

    suspend fun getAllByWalletFlow(wallet: Wallet): Flow<List<AssetInfo>> {
        return assetsLocalSource.getAllByAccountsFlow(wallet.accounts)
            .map { it.filter { !ChainInfoLocalSource.exclude.contains(it.asset.id.chain) } }
    }

    suspend fun search(wallet: Wallet, query: String): Flow<List<AssetInfo>> {
        val assetsFlow = assetsLocalSource.search(wallet.accounts, query)
        val tokensFlow = tokensRepository.search(wallet.accounts.map { it.chain }, query)
        return combine(
            assetsFlow,
            tokensFlow,
        ) { assets, tokens ->
            val result = assets + tokens.mapNotNull { asset ->
                AssetInfo(
                    asset = asset,
                    owner = wallet.getAccount(asset.id.chain) ?: return@mapNotNull null,
                )
            }.filter { !ChainInfoLocalSource.exclude.contains(it.asset.id.chain) }
            result.distinctBy {
                it.asset.id.toIdentifier()
            }
        }
    }

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


    suspend fun invalidateDefault(walletType: WalletType, wallet: Wallet, currency: Currency) = scope.launch {
        val assets = assetsLocalSource.getAllByAccounts(wallet.accounts)
            .getOrNull()?.associateBy( { it.asset.id.toIdentifier() }, { it } ) ?: emptyMap()
        wallet.accounts.filter { !ChainInfoLocalSource.exclude.contains(it.chain) }
            .map { account ->
                Pair(account, account.chain.asset())
            }.map {
                val isNew = assets[it.first.chain.string] == null
                val isVisible = assets[it.second.id.toIdentifier()]?.metadata?.isEnabled
                    ?: visibleByDefault.contains(it.first.chain) || walletType != WalletType.multicoin
                assetsLocalSource.add(it.first.address, it.second, isVisible)
                async {
                    if (isNew) {
                        val balances = updateBalances(it.first, emptyList()).firstOrNull()
                        if ((balances?.calcTotal()?.atomicValue?.compareTo(BigInteger.ZERO) ?: 0) > 0) {
                            assetsLocalSource.setVisibility(it.first, it.second.id, true)
                        }
                    }
                }
            }.awaitAll()
        delay(2000) // Wait subscription
        val availableAssets = gemApiClient.getAssets(configRepository.getDeviceId(), wallet.index).getOrNull() ?: return@launch
        availableAssets.mapNotNull {
            it.toAssetId()
        }.filter {
            it.tokenId != null
        }.map { assetId ->
            async {
                val account =
                    wallet.getAccount(assetId.chain) ?: return@async
                tokensRepository.search(assetId.tokenId!!)
                switchVisibility(account, assetId, true, currency)
            }
        }.awaitAll()
    }

    suspend fun switchVisibility(
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
        assetsLocalSource.setVisibility(owner, assetId, visibility)
        updateBalances(owner, assetId)
        updatePrices(currency)
        onRefreshAssets.forEach { it() }
    }

    suspend fun clearPrices() = withContext(Dispatchers.IO) {
        assetsLocalSource.clearPrices()
    }

    suspend fun updatePrices(currency: Currency, vararg assetIds: AssetId) = withContext(Dispatchers.IO) {
        val ids = if (assetIds.isEmpty()) {
            assetsLocalSource.getAllAssetsIds()
        } else {
            assetIds.toList()
        }
        val remoteResult = pricesRemoteSource.loadPrices(currency.string, ids)
        val prices = remoteResult.getOrNull() ?: return@withContext
        assetsLocalSource.setPrices(prices)
        onRefreshAssets.forEach { it() }
    }

    fun subscribe(onRefreshAssets: () -> Unit) {
        this.onRefreshAssets.add(onRefreshAssets)
    }

    override suspend fun getAsset(assetId: AssetId): Asset? {
        return assetsLocalSource.getById(assetId = assetId)
    }

    suspend fun getStakeApr(assetId: AssetId): Double? = withContext(Dispatchers.IO) {
        assetsLocalSource.getStakingApr(assetId)
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
        onRefreshAssets.forEach { it() }
    }

    private suspend fun updateBalances(wallet: Wallet) {
        scope.launch {
            wallet.accounts.map {  account ->
                async {
                    val assets = assetsLocalSource
                        .getAllByAccount(account)
                        .getOrNull()
                        ?.filter { it.metadata?.isEnabled == true }
                        ?.map { it.asset.id } ?: return@async
                    if (assets.isEmpty()) {
                        return@async
                    }
                    updateBalances(account, assets)
                    onRefreshAssets.forEach { it() }
                }
            }.awaitAll()
        }
    }

    suspend fun updateBalances(account: Account, vararg tokens: AssetId) {
        updateBalances(account, tokens.toList())
    }

    suspend fun getAssetByTokenId(chain: Chain, address: String): Asset? = withContext(Dispatchers.IO) {
        val assetId = AssetId(chain, address)
        tokensRepository.search(assetId)
        tokensRepository.getByIds(listOf(assetId)).firstOrNull()
    }

    private suspend fun updateBalances(account: Account, tokens: List<AssetId>): List<Balances> {
        val balances = balancesRemoteSource.getBalances(account, tokens).getOrNull() ?: return emptyList()
        assetsLocalSource.setBalances(account, balances)
        return balances
    }
}