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
import com.gemwallet.android.blockchain.clients.aptos.AptosSignClient
import com.gemwallet.android.blockchain.clients.aptos.AptosSignerPreloader
import com.gemwallet.android.blockchain.clients.aptos.AptosTransactionStatusClient
import com.gemwallet.android.blockchain.clients.bitcoin.BitcoinBalanceClient
import com.gemwallet.android.blockchain.clients.bitcoin.BitcoinBroadcastClient
import com.gemwallet.android.blockchain.clients.bitcoin.BitcoinSignClient
import com.gemwallet.android.blockchain.clients.bitcoin.BitcoinSignerPreloader
import com.gemwallet.android.blockchain.clients.bitcoin.BitcoinTransactionStatusClient
import com.gemwallet.android.blockchain.clients.cosmos.CosmosBalanceClient
import com.gemwallet.android.blockchain.clients.cosmos.CosmosBroadcastClient
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
import com.gemwallet.android.blockchain.clients.near.NearSignClient
import com.gemwallet.android.blockchain.clients.near.NearSignerPreloader
import com.gemwallet.android.blockchain.clients.near.NearTransactionStatusClient
import com.gemwallet.android.blockchain.clients.solana.SolanaBalanceClient
import com.gemwallet.android.blockchain.clients.solana.SolanaBroadcastClient
import com.gemwallet.android.blockchain.clients.solana.SolanaSignClient
import com.gemwallet.android.blockchain.clients.solana.SolanaSignerPreloader
import com.gemwallet.android.blockchain.clients.solana.SolanaStakeClient
import com.gemwallet.android.blockchain.clients.solana.SolanaTokenClient
import com.gemwallet.android.blockchain.clients.solana.SolanaTransactionStatusClient
import com.gemwallet.android.blockchain.clients.sui.SuiBalanceClient
import com.gemwallet.android.blockchain.clients.sui.SuiBroadcastClient
import com.gemwallet.android.blockchain.clients.sui.SuiGetTokenClient
import com.gemwallet.android.blockchain.clients.sui.SuiSignClient
import com.gemwallet.android.blockchain.clients.sui.SuiSignerPreloader
import com.gemwallet.android.blockchain.clients.sui.SuiStakeClient
import com.gemwallet.android.blockchain.clients.sui.SuiTransactionStatusClient
import com.gemwallet.android.blockchain.clients.ton.TonBalanceClient
import com.gemwallet.android.blockchain.clients.ton.TonBroadcastClient
import com.gemwallet.android.blockchain.clients.ton.TonGetTokenClient
import com.gemwallet.android.blockchain.clients.ton.TonSignClient
import com.gemwallet.android.blockchain.clients.ton.TonSignerPreloader
import com.gemwallet.android.blockchain.clients.ton.TonTransactionStatusClient
import com.gemwallet.android.blockchain.clients.tron.TronBalanceClient
import com.gemwallet.android.blockchain.clients.tron.TronBroadcastClient
import com.gemwallet.android.blockchain.clients.tron.TronGetTokenClient
import com.gemwallet.android.blockchain.clients.tron.TronSignClient
import com.gemwallet.android.blockchain.clients.tron.TronSignerPreloader
import com.gemwallet.android.blockchain.clients.tron.TronTransactionStatusClient
import com.gemwallet.android.blockchain.clients.xrp.XrpBalanceClient
import com.gemwallet.android.blockchain.clients.xrp.XrpBroadcastClient
import com.gemwallet.android.blockchain.clients.xrp.XrpSignClient
import com.gemwallet.android.blockchain.clients.xrp.XrpSignerPreloader
import com.gemwallet.android.blockchain.clients.xrp.XrpTransactionStatusClient
import com.gemwallet.android.blockchain.operators.SignTransfer
import com.gemwallet.android.data.asset.AssetsDao
import com.gemwallet.android.data.asset.AssetsLocalSource
import com.gemwallet.android.data.asset.AssetsRepository
import com.gemwallet.android.data.asset.AssetsRoomSource
import com.gemwallet.android.data.asset.BalancesDao
import com.gemwallet.android.data.asset.BalancesRemoteSource
import com.gemwallet.android.data.asset.BalancesRetrofitRemoteSource
import com.gemwallet.android.data.asset.PricesDao
import com.gemwallet.android.data.asset.PricesRemoteSource
import com.gemwallet.android.data.asset.PricesRetrofitSource
import com.gemwallet.android.data.bridge.BridgesRepository
import com.gemwallet.android.data.bridge.ConnectionsDao
import com.gemwallet.android.data.bridge.ConnectionsLocalSource
import com.gemwallet.android.data.bridge.ConnectionsRoomSource
import com.gemwallet.android.data.buy.BuyRepository
import com.gemwallet.android.data.chains.ChainInfoLocalSource
import com.gemwallet.android.data.chains.ChainInfoStaticSource
import com.gemwallet.android.data.config.ConfigRepository
import com.gemwallet.android.data.config.NodeDao
import com.gemwallet.android.data.config.NodeLocalSource
import com.gemwallet.android.data.config.NodesRepository
import com.gemwallet.android.data.config.OfflineFirstConfigRepository
import com.gemwallet.android.data.session.SessionLocalSource
import com.gemwallet.android.data.session.SessionRepository
import com.gemwallet.android.data.session.SessionSharedPreferenceSource
import com.gemwallet.android.data.stake.StakeDao
import com.gemwallet.android.data.stake.StakeLocalSource
import com.gemwallet.android.data.stake.StakeRepository
import com.gemwallet.android.data.stake.StakeRoomSource
import com.gemwallet.android.data.swap.SwapRepository
import com.gemwallet.android.data.tokens.OfflineFirstTokensRepository
import com.gemwallet.android.data.tokens.TokensDao
import com.gemwallet.android.data.tokens.TokensLocalSource
import com.gemwallet.android.data.tokens.TokensRepository
import com.gemwallet.android.data.tokens.TokensRoomSource
import com.gemwallet.android.data.transaction.TransactionsDao
import com.gemwallet.android.data.transaction.TransactionsLocalSource
import com.gemwallet.android.data.transaction.TransactionsRepository
import com.gemwallet.android.data.transaction.TransactionsRoomSource
import com.gemwallet.android.data.wallet.AccountsDao
import com.gemwallet.android.data.wallet.WalletsDao
import com.gemwallet.android.data.wallet.WalletsLocalSource
import com.gemwallet.android.data.wallet.WalletsRepository
import com.gemwallet.android.data.wallet.WalletsRoomSource
import com.gemwallet.android.services.GemApiClient
import com.gemwallet.android.services.GemApiStaticClient
import com.gemwallet.android.services.GemIpAddressService
import com.gemwallet.android.services.GemNameResolveService
import com.gemwallet.android.services.IpAddressService
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

internal fun availableChains() = (Chain.entries.toSet() - ChainInfoLocalSource.exclude.toSet())

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
        localSource: TokensLocalSource,
        gemApiClient: GemApiClient,
        rpcClients: RpcClientAdapter,
    ): TokensRepository = OfflineFirstTokensRepository(
        localSource = localSource,
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
    fun provideBalanceRemoteSource(
        rpcClients: RpcClientAdapter,
    ): BalancesRemoteSource = BalancesRetrofitRemoteSource(
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
        tokensRepository: TokensRepository,
        transactionsRepository: TransactionsRepository,
        assetsLocalSource: AssetsLocalSource,
        balancesRemoteSource: BalancesRemoteSource,
        pricesRemoteSource: PricesRemoteSource,
        configRepository: ConfigRepository,
    ): AssetsRepository = AssetsRepository(
        gemApiClient = gemApiClient,
        tokensRepository = tokensRepository,
        transactionsRepository = transactionsRepository,
        assetsLocalSource = assetsLocalSource,
        balancesRemoteSource = balancesRemoteSource,
        pricesRemoteSource = pricesRemoteSource,
        configRepository = configRepository,
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
        localSource: TransactionsLocalSource,
        assetsLocalSource: AssetsLocalSource,
        rpcClients: RpcClientAdapter,
    ): TransactionsRepository = TransactionsRepository(
        localSource = localSource,
        assetsLocalSource = assetsLocalSource,
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
        stakeLocalSource: StakeLocalSource,
        rpcClients: RpcClientAdapter,
        gemApiStaticClient: GemApiStaticClient,
    ): StakeRepository {
        return StakeRepository(
            localSource = stakeLocalSource,
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
            availableChains().mapNotNull {
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
                    Chain.Solana,
                    Chain.Thorchain,
                    Chain.Osmosis,
                    Chain.Celestia,
                    Chain.Injective,
                    Chain.Sei,
                    Chain.Noble,
                    Chain.Cosmos,
                    Chain.Ton,
                    Chain.Tron,
                    Chain.Aptos,
                    Chain.Sui,
                    Chain.Xrp,
                    Chain.Doge,
                    Chain.Litecoin,
                    Chain.Bitcoin,
                    Chain.Near -> null
                }
            }
        )
    }

    @Singleton
    @Provides
    fun provideNodesRepository(
        nodeLocalSource: NodeLocalSource,
    ): NodesRepository = NodesRepository(nodeLocalSource)

    @Singleton
    @Provides
    fun provideBridgeRepository(
        walletsRepository: WalletsRepository,
        localSource: ConnectionsLocalSource,
    ): BridgesRepository {
        return BridgesRepository(walletsRepository, localSource)
    }

    @Singleton
    @Provides
    fun provideChainInfoLocalSource(): ChainInfoLocalSource = ChainInfoStaticSource()

    @Singleton
    @Provides
    fun provideWalletsLocalSource(
        walletsDao: WalletsDao,
        accountsDao: AccountsDao,
    ): WalletsLocalSource = WalletsRoomSource(walletsDao = walletsDao, accountsDao = accountsDao)

    @Singleton
    @Provides
    fun provideAssetsLocalSource(
        assetsDao: AssetsDao,
        balancesDao: BalancesDao,
        pricesDao: PricesDao,
    ): AssetsLocalSource = AssetsRoomSource(
        assetsDao = assetsDao,
        balancesDao = balancesDao,
        pricesDao = pricesDao,
    )

    @Singleton
    @Provides
    fun provideTokensLocalSource(
        tokensDao: TokensDao,
    ): TokensLocalSource = TokensRoomSource(
        tokensDao = tokensDao,
    )

    @Singleton
    @Provides
    fun provideSessionLocalSource(
        @ApplicationContext context: Context,
    ): SessionLocalSource = SessionSharedPreferenceSource(context = context)

    @Singleton
    @Provides
    fun providePricesRemoteSource(
        rpcClient: GemApiClient,
    ): PricesRemoteSource = PricesRetrofitSource(
        rpcClient = rpcClient,
    )

    @Singleton
    @Provides
    fun provideIpAddressService(
        client: GemApiClient,
    ): IpAddressService = GemIpAddressService(client)

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
    fun provideTransactionsLocalSource(
        transactionsDao: TransactionsDao,
        @GemJson gson: Gson,
    ): TransactionsLocalSource = TransactionsRoomSource(
        transactionsDao = transactionsDao,
        gson = gson,
    )

    @Singleton
    @Provides
    fun provideConnectionsLocalSource(
        connectionsDao: ConnectionsDao
    ): ConnectionsLocalSource = ConnectionsRoomSource(connectionsDao)

    @Singleton
    @Provides
    fun provideSyncService(
        gemApiClient: GemApiClient,
        configRepository: ConfigRepository,
        nodesRepository: NodesRepository,
        sessionRepository: SessionRepository,
        walletsRepository: WalletsRepository,
    ): SyncService {
        return SyncService(
            gemApiClient = gemApiClient,
            configRepository = configRepository,
            nodesRepository = nodesRepository,
            sessionRepository = sessionRepository,
            walletsRepository = walletsRepository,
        )
    }

    @Singleton
    @Provides
    fun provideStakeLocalSource(
        stakeDao: StakeDao,
    ): StakeLocalSource {
        return StakeRoomSource(stakeDao)
    }

    @Singleton
    @Provides
    fun provideNodesLocalSource(
        nodeDao: NodeDao,
    ): NodeLocalSource {
        return NodeLocalSource(nodeDao)
    }
}