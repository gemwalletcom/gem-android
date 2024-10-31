package com.gemwallet.android.di

import android.content.Context
import com.gemwallet.android.blockchain.RpcClientAdapter
import com.gemwallet.android.blockchain.clients.BroadcastProxy
import com.gemwallet.android.blockchain.clients.NodeStatusClientsProxy
import com.gemwallet.android.blockchain.clients.SignPreloaderProxy
import com.gemwallet.android.blockchain.clients.SignTransferProxy
import com.gemwallet.android.blockchain.clients.SignerPreload
import com.gemwallet.android.blockchain.clients.aptos.AptosBalanceClient
import com.gemwallet.android.blockchain.clients.aptos.AptosBroadcastClient
import com.gemwallet.android.blockchain.clients.aptos.AptosNodeStatusClient
import com.gemwallet.android.blockchain.clients.aptos.AptosSignClient
import com.gemwallet.android.blockchain.clients.aptos.AptosSignerPreloader
import com.gemwallet.android.blockchain.clients.aptos.AptosTransactionStatusClient
import com.gemwallet.android.blockchain.clients.bitcoin.BitcoinBalanceClient
import com.gemwallet.android.blockchain.clients.bitcoin.BitcoinBroadcastClient
import com.gemwallet.android.blockchain.clients.bitcoin.BitcoinNodeStatusClient
import com.gemwallet.android.blockchain.clients.bitcoin.BitcoinSignClient
import com.gemwallet.android.blockchain.clients.bitcoin.BitcoinSignerPreloader
import com.gemwallet.android.blockchain.clients.bitcoin.BitcoinTransactionStatusClient
import com.gemwallet.android.blockchain.clients.cosmos.CosmosBalanceClient
import com.gemwallet.android.blockchain.clients.cosmos.CosmosBroadcastClient
import com.gemwallet.android.blockchain.clients.cosmos.CosmosNodeStatusClient
import com.gemwallet.android.blockchain.clients.cosmos.CosmosSignClient
import com.gemwallet.android.blockchain.clients.cosmos.CosmosSignerPreloader
import com.gemwallet.android.blockchain.clients.cosmos.CosmosStakeClient
import com.gemwallet.android.blockchain.clients.cosmos.CosmosTransactionStatusClient
import com.gemwallet.android.blockchain.clients.ethereum.EvmBalanceClient
import com.gemwallet.android.blockchain.clients.ethereum.EvmBroadcastClient
import com.gemwallet.android.blockchain.clients.ethereum.EvmGetTokenClient
import com.gemwallet.android.blockchain.clients.ethereum.EvmNodeStatusClient
import com.gemwallet.android.blockchain.clients.ethereum.EvmSignClient
import com.gemwallet.android.blockchain.clients.ethereum.EvmSignerPreloader
import com.gemwallet.android.blockchain.clients.ethereum.EvmSwapClient
import com.gemwallet.android.blockchain.clients.ethereum.EvmTransactionStatusClient
import com.gemwallet.android.blockchain.clients.ethereum.SmartchainStakeClient
import com.gemwallet.android.blockchain.clients.near.NearBalanceClient
import com.gemwallet.android.blockchain.clients.near.NearBroadcastClient
import com.gemwallet.android.blockchain.clients.near.NearNodeStatusClient
import com.gemwallet.android.blockchain.clients.near.NearSignClient
import com.gemwallet.android.blockchain.clients.near.NearSignerPreloader
import com.gemwallet.android.blockchain.clients.near.NearTransactionStatusClient
import com.gemwallet.android.blockchain.clients.solana.SolanaBalanceClient
import com.gemwallet.android.blockchain.clients.solana.SolanaBroadcastClient
import com.gemwallet.android.blockchain.clients.solana.SolanaNodeStatusClient
import com.gemwallet.android.blockchain.clients.solana.SolanaSignClient
import com.gemwallet.android.blockchain.clients.solana.SolanaSignerPreloader
import com.gemwallet.android.blockchain.clients.solana.SolanaStakeClient
import com.gemwallet.android.blockchain.clients.solana.SolanaTokenClient
import com.gemwallet.android.blockchain.clients.solana.SolanaTransactionStatusClient
import com.gemwallet.android.blockchain.clients.sui.SuiBalanceClient
import com.gemwallet.android.blockchain.clients.sui.SuiBroadcastClient
import com.gemwallet.android.blockchain.clients.sui.SuiGetTokenClient
import com.gemwallet.android.blockchain.clients.sui.SuiNodeStatusClient
import com.gemwallet.android.blockchain.clients.sui.SuiSignClient
import com.gemwallet.android.blockchain.clients.sui.SuiSignerPreloader
import com.gemwallet.android.blockchain.clients.sui.SuiStakeClient
import com.gemwallet.android.blockchain.clients.sui.SuiTransactionStatusClient
import com.gemwallet.android.blockchain.clients.ton.TonBalanceClient
import com.gemwallet.android.blockchain.clients.ton.TonBroadcastClient
import com.gemwallet.android.blockchain.clients.ton.TonGetTokenClient
import com.gemwallet.android.blockchain.clients.ton.TonNodeStatusClient
import com.gemwallet.android.blockchain.clients.ton.TonSignClient
import com.gemwallet.android.blockchain.clients.ton.TonSignerPreloader
import com.gemwallet.android.blockchain.clients.ton.TonTransactionStatusClient
import com.gemwallet.android.blockchain.clients.tron.TronBalanceClient
import com.gemwallet.android.blockchain.clients.tron.TronBroadcastClient
import com.gemwallet.android.blockchain.clients.tron.TronGetTokenClient
import com.gemwallet.android.blockchain.clients.tron.TronNodeStatusClient
import com.gemwallet.android.blockchain.clients.tron.TronSignClient
import com.gemwallet.android.blockchain.clients.tron.TronSignerPreloader
import com.gemwallet.android.blockchain.clients.tron.TronTransactionStatusClient
import com.gemwallet.android.blockchain.clients.xrp.XrpBalanceClient
import com.gemwallet.android.blockchain.clients.xrp.XrpBroadcastClient
import com.gemwallet.android.blockchain.clients.xrp.XrpNodeStatusClient
import com.gemwallet.android.blockchain.clients.xrp.XrpSignClient
import com.gemwallet.android.blockchain.clients.xrp.XrpSignerPreloader
import com.gemwallet.android.blockchain.clients.xrp.XrpTransactionStatusClient
import com.gemwallet.android.blockchain.operators.SignTransfer
import com.gemwallet.android.cases.banners.CancelBannerCase
import com.gemwallet.android.cases.banners.GetBannersCase
import com.gemwallet.android.cases.tokens.GetTokensCase
import com.gemwallet.android.cases.tokens.SearchTokensCase
import com.gemwallet.android.cases.transactions.CreateTransactionCase
import com.gemwallet.android.cases.transactions.GetTransactionCase
import com.gemwallet.android.cases.transactions.GetTransactionsCase
import com.gemwallet.android.cases.transactions.PutTransactionsCase
import com.gemwallet.android.data.database.AssetsDao
import com.gemwallet.android.data.database.BalancesDao
import com.gemwallet.android.data.database.BannersDao
import com.gemwallet.android.data.database.ConnectionsDao
import com.gemwallet.android.data.database.NodesDao
import com.gemwallet.android.data.database.PricesDao
import com.gemwallet.android.data.database.SessionDao
import com.gemwallet.android.data.database.StakeDao
import com.gemwallet.android.data.database.TokensDao
import com.gemwallet.android.data.database.TransactionsDao
import com.gemwallet.android.data.repositories.asset.AssetsRepository
import com.gemwallet.android.data.repositories.asset.BalancesRemoteSource
import com.gemwallet.android.data.repositories.banners.BannersRepository
import com.gemwallet.android.data.repositories.bridge.BridgesRepository
import com.gemwallet.android.data.repositories.buy.BuyRepository
import com.gemwallet.android.data.repositories.chains.ChainInfoRepository
import com.gemwallet.android.data.repositories.config.ConfigRepository
import com.gemwallet.android.data.repositories.config.ConfigStore
import com.gemwallet.android.data.repositories.config.OfflineFirstConfigRepository
import com.gemwallet.android.data.repositories.nodes.NodesRepository
import com.gemwallet.android.data.repositories.session.SessionRepository
import com.gemwallet.android.data.repositories.session.SessionRepositoryImpl
import com.gemwallet.android.data.repositories.stake.StakeRepository
import com.gemwallet.android.data.repositories.swap.SwapRepository
import com.gemwallet.android.data.repositories.tokens.TokensRepository
import com.gemwallet.android.data.repositories.transaction.TransactionsRepository
import com.gemwallet.android.data.repositories.wallet.WalletsRepository
import com.gemwallet.android.interactors.sync.SyncTransactions
import com.gemwallet.android.services.GemApiClient
import com.gemwallet.android.services.GemApiStaticClient
import com.gemwallet.android.services.GemNameResolveService
import com.gemwallet.android.services.NameResolveService
import com.gemwallet.android.services.SyncService
import com.google.gson.Gson
import com.wallet.core.primitives.Chain
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

internal fun availableChains() = (Chain.entries.toSet() - ChainInfoRepository.exclude.toSet())

@InstallIn(SingletonComponent::class)
@Module
object DataModule {

    @Provides
    @Singleton
    fun provideGSON(): Gson = Gson()

    @Provides
    @Singleton
    fun provideBuyRepository(
        configRepository: ConfigRepository,
        gemFiatQuoteClient: GemApiClient,
    ): BuyRepository = BuyRepository(
        configRepository = configRepository,
        remoteSource = gemFiatQuoteClient
    )

    @Provides
    @Singleton
    fun provideTokensRepository(
        tokensDao: TokensDao,
        gemApiClient: GemApiClient,
        rpcClients: RpcClientAdapter,
    ): TokensRepository = TokensRepository(
        tokensDao = tokensDao,
        gemApiClient = gemApiClient,
        getTokenClients = availableChains().mapNotNull {
            when (it) {
                Chain.AvalancheC,
                Chain.Base,
                Chain.SmartChain,
                Chain.Arbitrum,
                Chain.Polygon,
                Chain.OpBNB,
                Chain.Fantom,
                Chain.Gnosis,
                Chain.Optimism,
                Chain.Manta,
                Chain.Blast,
                Chain.ZkSync,
                Chain.Linea,
                Chain.Mantle,
                Chain.Celo,
                Chain.Ethereum -> EvmGetTokenClient(it, rpcClients.getClient(it))
                Chain.Tron -> TronGetTokenClient(it, rpcClients.getClient(Chain.Tron))
                Chain.Solana -> SolanaTokenClient(it, rpcClients.getClient(Chain.Solana))
                Chain.Sui -> SuiGetTokenClient(it, rpcClients.getClient(it))
                Chain.Ton -> TonGetTokenClient(it, rpcClients.getClient(it))
                Chain.Doge,
                Chain.Litecoin,
                Chain.Bitcoin,
                Chain.Thorchain,
                Chain.Osmosis,
                Chain.Celestia,
                Chain.Injective,
                Chain.Sei,
                Chain.Noble,
                Chain.Cosmos,
                Chain.Aptos,
                Chain.Xrp,
                Chain.Near -> null
            }
        }
    )

    @Provides
    @Singleton
    fun provideGetTokensCase(tokensRepository: TokensRepository): GetTokensCase = tokensRepository

    @Provides
    @Singleton
    fun provideSearchTokensCase(tokensRepository: TokensRepository): SearchTokensCase = tokensRepository

    @Provides
    @Singleton
    fun provideBalanceRemoteSource(
        rpcClients: RpcClientAdapter,
    ): BalancesRemoteSource = BalancesRemoteSource(
        availableChains().map {
            when (it) {
                Chain.Doge,
                Chain.Litecoin,
                Chain.Bitcoin -> BitcoinBalanceClient(it, rpcClients.getClient(it))
                Chain.AvalancheC,
                Chain.Base,
                Chain.SmartChain,
                Chain.Arbitrum,
                Chain.Polygon,
                Chain.OpBNB,
                Chain.Fantom,
                Chain.Gnosis,
                Chain.Optimism,
                Chain.Manta,
                Chain.Blast,
                Chain.ZkSync,
                Chain.Linea,
                Chain.Mantle,
                Chain.Celo,
                Chain.Ethereum -> EvmBalanceClient(it, rpcClients.getClient(it))
                Chain.Solana -> SolanaBalanceClient(it, rpcClients.getClient(Chain.Solana))
                Chain.Thorchain,
                Chain.Osmosis,
                Chain.Celestia,
                Chain.Injective,
                Chain.Sei,
                Chain.Noble,
                Chain.Cosmos -> CosmosBalanceClient(it, rpcClients.getClient(it))
                Chain.Ton -> TonBalanceClient(it, rpcClients.getClient(Chain.Ton))
                Chain.Tron -> TronBalanceClient(it, rpcClients.getClient(Chain.Tron))
                Chain.Aptos -> AptosBalanceClient(it, rpcClients.getClient(it))
                Chain.Sui -> SuiBalanceClient(it, rpcClients.getClient(it))
                Chain.Xrp -> XrpBalanceClient(it, rpcClients.getClient(it))
                Chain.Near -> NearBalanceClient(it, rpcClients.getClient(it))
            }
        }
    )

    @Provides
    @Singleton
    fun provideAssetsRepository(
        gemApiClient: GemApiClient,
        assetsDao: AssetsDao,
        balancesDao: BalancesDao,
        pricesDao: PricesDao,
        sessionRepository: SessionRepository,
        balancesRemoteSource: BalancesRemoteSource,
        configRepository: ConfigRepository,
        getTransactionsCase: GetTransactionsCase,
        getTokensCase: GetTokensCase,
        searchTokensCase: SearchTokensCase,
    ): AssetsRepository = AssetsRepository(
        gemApi = gemApiClient,
        assetsDao = assetsDao,
        balancesDao = balancesDao,
        pricesDao = pricesDao,
        sessionRepository = sessionRepository,
        getTransactionsCase = getTransactionsCase,
        balancesRemoteSource = balancesRemoteSource,
        configRepository = configRepository,
        getTokensCase = getTokensCase,
        searchTokensCase = searchTokensCase,
    )

    @Provides
    @Singleton
    fun providesBroadcastRepository(
        rpcClients: RpcClientAdapter,
    ): BroadcastProxy = BroadcastProxy(
        availableChains().map {
            when (it) {
                Chain.Doge,
                Chain.Litecoin,
                Chain.Bitcoin -> BitcoinBroadcastClient(it, rpcClients.getClient(it))
                Chain.AvalancheC,
                Chain.Base,
                Chain.SmartChain,
                Chain.Arbitrum,
                Chain.Polygon,
                Chain.OpBNB,
                Chain.Fantom,
                Chain.Gnosis,
                Chain.Optimism,
                Chain.Manta,
                Chain.Blast,
                Chain.ZkSync,
                Chain.Linea,
                Chain.Mantle,
                Chain.Celo,
                Chain.Ethereum -> EvmBroadcastClient(it, rpcClients.getClient(it))
                Chain.Solana -> SolanaBroadcastClient(rpcClients.getClient(Chain.Solana))
                Chain.Thorchain,
                Chain.Osmosis,
                Chain.Celestia,
                Chain.Injective,
                Chain.Sei,
                Chain.Noble,
                Chain.Cosmos -> CosmosBroadcastClient(it, rpcClients.getClient(it))
                Chain.Ton -> TonBroadcastClient(rpcClients.getClient(it))
                Chain.Tron -> TronBroadcastClient(rpcClients.getClient(Chain.Tron))
                Chain.Aptos -> AptosBroadcastClient(it, rpcClients.getClient(it))
                Chain.Sui -> SuiBroadcastClient(it, rpcClients.getClient(it))
                Chain.Xrp -> XrpBroadcastClient(it, rpcClients.getClient(it))
                Chain.Near -> NearBroadcastClient(it, rpcClients.getClient(it))
            }
        },
    )

    @Provides
    @Singleton
    fun provideSignerPreloader(
        rpcClients: RpcClientAdapter,
    ): SignerPreload = SignPreloaderProxy(
        availableChains().map {
            when (it) {
                Chain.Doge,
                Chain.Litecoin,
                Chain.Bitcoin -> BitcoinSignerPreloader(it, rpcClients.getClient(it))
                Chain.AvalancheC,
                Chain.Base,
                Chain.SmartChain,
                Chain.Arbitrum,
                Chain.Polygon,
                Chain.OpBNB,
                Chain.Fantom,
                Chain.Gnosis,
                Chain.Optimism,
                Chain.Manta,
                Chain.Blast,
                Chain.ZkSync,
                Chain.Linea,
                Chain.Mantle,
                Chain.Celo,
                Chain.Ethereum -> EvmSignerPreloader(it, rpcClients.getClient(it))
                Chain.Solana -> SolanaSignerPreloader(rpcClients.getClient(Chain.Solana))
                Chain.Thorchain,
                Chain.Osmosis,
                Chain.Celestia,
                Chain.Injective,
                Chain.Sei,
                Chain.Noble,
                Chain.Cosmos -> CosmosSignerPreloader(it, rpcClients.getClient(it))
                Chain.Ton -> TonSignerPreloader(rpcClients.getClient(it))
                Chain.Tron -> TronSignerPreloader(rpcClients.getClient(Chain.Tron))
                Chain.Aptos -> AptosSignerPreloader(it, rpcClients.getClient(it))
                Chain.Sui -> SuiSignerPreloader(it, rpcClients.getClient(it))
                Chain.Xrp -> XrpSignerPreloader(it, rpcClients.getClient(it))
                Chain.Near -> NearSignerPreloader(it, rpcClients.getClient(it))
            }
        },
    )

    @Provides
    @Singleton
    fun provideSignService(
        assetsRepository: AssetsRepository,
    ): SignTransfer = SignTransferProxy(
        clients = listOf(
            SolanaSignClient(assetsRepository),
            TronSignClient(),
            BitcoinSignClient(Chain.Bitcoin),
            BitcoinSignClient(Chain.Doge),
            BitcoinSignClient(Chain.Litecoin),
            TonSignClient(),
            EvmSignClient(Chain.Ethereum),
            EvmSignClient(Chain.Fantom),
            EvmSignClient(Chain.Gnosis),
            EvmSignClient(Chain.AvalancheC),
            EvmSignClient(Chain.Base),
            EvmSignClient(Chain.SmartChain),
            EvmSignClient(Chain.Arbitrum),
            EvmSignClient(Chain.Polygon),
            EvmSignClient(Chain.OpBNB),
            EvmSignClient(Chain.Manta),
            EvmSignClient(Chain.Blast),
            EvmSignClient(Chain.ZkSync),
            EvmSignClient(Chain.Linea),
            EvmSignClient(Chain.Mantle),
            EvmSignClient(Chain.Celo),
            CosmosSignClient(Chain.Cosmos),
            CosmosSignClient(Chain.Osmosis),
            CosmosSignClient(Chain.Thorchain),
            CosmosSignClient(Chain.Celestia),
            CosmosSignClient(Chain.Injective),
            CosmosSignClient(Chain.Sei),
            CosmosSignClient(Chain.Noble),
            AptosSignClient(Chain.Aptos),
            SuiSignClient(Chain.Sui),
            XrpSignClient(Chain.Xrp),
            NearSignClient(Chain.Near),
        ),
    )

    @Singleton
    @Provides
    fun provideTransactionsRepository(
        transactionsDao: TransactionsDao,
        assetsDao: AssetsDao,
        rpcClients: RpcClientAdapter,
        @GemJson gson: Gson,
    ): TransactionsRepository = TransactionsRepository(
        transactionsDao = transactionsDao,
        assetsDao = assetsDao,
        gson = gson,
        stateClients = availableChains().map {
            when (it) {
                Chain.Doge,
                Chain.Litecoin,
                Chain.Bitcoin -> BitcoinTransactionStatusClient(it, rpcClients.getClient(it))
                Chain.AvalancheC,
                Chain.Base,
                Chain.SmartChain,
                Chain.Arbitrum,
                Chain.Polygon,
                Chain.OpBNB,
                Chain.Fantom,
                Chain.Gnosis,
                Chain.Optimism,
                Chain.Manta,
                Chain.Blast,
                Chain.ZkSync,
                Chain.Linea,
                Chain.Mantle,
                Chain.Celo,
                Chain.Ethereum -> EvmTransactionStatusClient(it, rpcClients.getClient(it))
                Chain.Solana -> SolanaTransactionStatusClient(rpcClients.getClient(Chain.Solana))
                Chain.Thorchain,
                Chain.Osmosis,
                Chain.Celestia,
                Chain.Injective,
                Chain.Sei,
                Chain.Noble,
                Chain.Cosmos -> CosmosTransactionStatusClient(it, rpcClients.getClient(it))
                Chain.Ton -> TonTransactionStatusClient(rpcClients.getClient(it))
                Chain.Tron -> TronTransactionStatusClient(rpcClients.getClient(Chain.Tron))
                Chain.Aptos -> AptosTransactionStatusClient(it, rpcClients.getClient(it))
                Chain.Sui -> SuiTransactionStatusClient(it, rpcClients.getClient(it))
                Chain.Xrp -> XrpTransactionStatusClient(it, rpcClients.getClient(it))
                Chain.Near -> NearTransactionStatusClient(it, rpcClients.getClient(it))
            }
        },
    )

    @Singleton
    @Provides
    fun provideGetTransactionsCase(transactionsRepository: TransactionsRepository): GetTransactionsCase {
        return transactionsRepository
    }

    @Singleton
    @Provides
    fun provideGetTransactionCase(transactionsRepository: TransactionsRepository): GetTransactionCase {
        return transactionsRepository
    }

    @Singleton
    @Provides
    fun providePutTransactionsCase(transactionsRepository: TransactionsRepository): PutTransactionsCase {
        return transactionsRepository
    }

    @Singleton
    @Provides
    fun provideCreateTransactionsCase(transactionsRepository: TransactionsRepository): CreateTransactionCase {
        return transactionsRepository
    }

    @Singleton
    @Provides
    fun provideSwapRepository(
        gemApiClient: GemApiClient,
        rpcClients: RpcClientAdapter,
    ): SwapRepository = SwapRepository(
        gemApiClient = gemApiClient,
        swapClients = availableChains().mapNotNull {
            when (it) {
                Chain.AvalancheC,
                Chain.Base,
                Chain.SmartChain,
                Chain.Arbitrum,
                Chain.Polygon,
                Chain.OpBNB,
                Chain.Fantom,
                Chain.Gnosis,
                Chain.Optimism,
                Chain.Manta,
                Chain.Blast,
                Chain.ZkSync,
                Chain.Linea,
                Chain.Mantle,
                Chain.Celo,
                Chain.Ethereum -> EvmSwapClient(it, rpcClients.getClient(it))
                else -> null
            }
        }
    )
    
    @Singleton
    @Provides
    fun provideStakeRepository(
        stakeDao: StakeDao,
        rpcClients: RpcClientAdapter,
        gemApiStaticClient: GemApiStaticClient,
    ): StakeRepository {
        return StakeRepository(
            stakeDao = stakeDao,
            gemApiStaticClient = gemApiStaticClient,
            stakeClients = availableChains().mapNotNull { 
                when (it) {
                    Chain.Bitcoin,
                    Chain.Litecoin,
                    Chain.Ethereum,
                    Chain.Blast,
                    Chain.Polygon,
                    Chain.Arbitrum,
                    Chain.Ton,
                    Chain.Tron,
                    Chain.Doge,
                    Chain.Optimism,
                    Chain.Aptos,
                    Chain.Base,
                    Chain.AvalancheC,
                    Chain.Xrp,
                    Chain.OpBNB,
                    Chain.Fantom,
                    Chain.Manta,
                    Chain.Thorchain,
                    Chain.ZkSync,
                    Chain.Linea,
                    Chain.Mantle,
                    Chain.Celo,
                    Chain.Near,
                    Chain.Gnosis -> null
                    Chain.Osmosis,
                    Chain.Celestia,
                    Chain.Injective,
                    Chain.Sei,
                    Chain.Noble,
                    Chain.Cosmos -> CosmosStakeClient(it, rpcClients.getClient(it))
                    Chain.Sui -> SuiStakeClient(rpcClients.getClient(it))
                    Chain.Solana -> SolanaStakeClient(rpcClients.getClient(it))
                    Chain.SmartChain -> SmartchainStakeClient(rpcClients.getClient(it))
                }
            }
        )
    }

    @Singleton
    @Provides
    fun provideNodeStatusClient(
        rpcClients: RpcClientAdapter,
    ): NodeStatusClientsProxy {
        return NodeStatusClientsProxy(
            availableChains().map {
                when (it) {
                    Chain.AvalancheC,
                    Chain.Base,
                    Chain.SmartChain,
                    Chain.Arbitrum,
                    Chain.Polygon,
                    Chain.OpBNB,
                    Chain.Fantom,
                    Chain.Gnosis,
                    Chain.Optimism,
                    Chain.Manta,
                    Chain.Blast,
                    Chain.ZkSync,
                    Chain.Linea,
                    Chain.Mantle,
                    Chain.Celo,
                    Chain.Ethereum -> EvmNodeStatusClient(it, rpcClients.getClient(it))
                    Chain.Solana -> SolanaNodeStatusClient(it, rpcClients.getClient(it))
                    Chain.Thorchain,
                    Chain.Osmosis,
                    Chain.Celestia,
                    Chain.Injective,
                    Chain.Sei,
                    Chain.Noble,
                    Chain.Cosmos -> CosmosNodeStatusClient(it, rpcClients.getClient(it))
                    Chain.Ton -> TonNodeStatusClient(it, rpcClients.getClient(it))
                    Chain.Tron -> TronNodeStatusClient(it, rpcClients.getClient(it))
                    Chain.Xrp -> XrpNodeStatusClient(it, rpcClients.getClient(it))
                    Chain.Doge,
                    Chain.Litecoin,
                    Chain.Bitcoin -> BitcoinNodeStatusClient(it, rpcClients.getClient(it))
                    Chain.Near -> NearNodeStatusClient(it, rpcClients.getClient(it))
                    Chain.Aptos -> AptosNodeStatusClient(it, rpcClients.getClient(it))
                    Chain.Sui -> SuiNodeStatusClient(it, rpcClients.getClient(it))
                }
            }
        )
    }

    @Singleton
    @Provides
    fun provideBridgeRepository(
        walletsRepository: WalletsRepository,
        connectionsDao: ConnectionsDao,
    ): BridgesRepository {
        return BridgesRepository(walletsRepository, connectionsDao)
    }

    @Singleton
    @Provides
    fun provideSessionRepository(
        sessionDao: SessionDao,
        walletsRepository: WalletsRepository,
    ): SessionRepository = SessionRepositoryImpl(
        sessionDao = sessionDao,
        walletsRepository = walletsRepository
    )

    @Singleton
    @Provides
    fun provideNameResolveService(
        client: GemApiClient,
    ): NameResolveService = GemNameResolveService(client)

    @Singleton
    @Provides
    fun provideConfigRepository(
        @ApplicationContext context: Context,
    ): ConfigRepository = OfflineFirstConfigRepository(
        context = context,
    )

    @Singleton
    @Provides
    fun provideSyncService(
        gemApiClient: GemApiClient,
        configRepository: ConfigRepository,
        nodesRepository: NodesRepository,
        sessionRepository: SessionRepository,
        walletsRepository: WalletsRepository,
        syncTransactions: SyncTransactions,
    ): SyncService {
        return SyncService(
            gemApiClient = gemApiClient,
            configRepository = configRepository,
            sessionRepository = sessionRepository,
            walletsRepository = walletsRepository,
            syncTransactions = syncTransactions,
        )
    }

    @Singleton
    @Provides
    fun provideBannersRepository(
        bannersDao: BannersDao,
        configRepository: ConfigRepository,
    ): BannersRepository {
        return BannersRepository(bannersDao, configRepository)
    }

    @Singleton
    @Provides
    fun provideGetBannersCase(bannersRepository: BannersRepository): GetBannersCase = bannersRepository

    @Singleton
    @Provides
    fun provideCancelBannerCase(bannersRepository: BannersRepository): CancelBannerCase = bannersRepository
}