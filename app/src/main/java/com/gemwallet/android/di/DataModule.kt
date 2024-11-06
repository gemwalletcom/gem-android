package com.gemwallet.android.di

import com.gemwallet.android.blockchain.RpcClientAdapter
import com.gemwallet.android.blockchain.clients.BroadcastProxy
import com.gemwallet.android.blockchain.clients.NodeStatusClientsProxy
import com.gemwallet.android.blockchain.clients.SignPreloaderProxy
import com.gemwallet.android.blockchain.clients.SignTransferProxy
import com.gemwallet.android.blockchain.clients.SignerPreload
import com.gemwallet.android.blockchain.clients.aptos.AptosBroadcastClient
import com.gemwallet.android.blockchain.clients.aptos.AptosNodeStatusClient
import com.gemwallet.android.blockchain.clients.aptos.AptosSignClient
import com.gemwallet.android.blockchain.clients.aptos.AptosSignerPreloader
import com.gemwallet.android.blockchain.clients.bitcoin.BitcoinBroadcastClient
import com.gemwallet.android.blockchain.clients.bitcoin.BitcoinNodeStatusClient
import com.gemwallet.android.blockchain.clients.bitcoin.BitcoinSignClient
import com.gemwallet.android.blockchain.clients.bitcoin.BitcoinSignerPreloader
import com.gemwallet.android.blockchain.clients.cosmos.CosmosBroadcastClient
import com.gemwallet.android.blockchain.clients.cosmos.CosmosNodeStatusClient
import com.gemwallet.android.blockchain.clients.cosmos.CosmosSignClient
import com.gemwallet.android.blockchain.clients.cosmos.CosmosSignerPreloader
import com.gemwallet.android.blockchain.clients.ethereum.EvmBroadcastClient
import com.gemwallet.android.blockchain.clients.ethereum.EvmNodeStatusClient
import com.gemwallet.android.blockchain.clients.ethereum.EvmSignClient
import com.gemwallet.android.blockchain.clients.ethereum.EvmSignerPreloader
import com.gemwallet.android.blockchain.clients.near.NearBroadcastClient
import com.gemwallet.android.blockchain.clients.near.NearNodeStatusClient
import com.gemwallet.android.blockchain.clients.near.NearSignClient
import com.gemwallet.android.blockchain.clients.near.NearSignerPreloader
import com.gemwallet.android.blockchain.clients.solana.SolanaBroadcastClient
import com.gemwallet.android.blockchain.clients.solana.SolanaNodeStatusClient
import com.gemwallet.android.blockchain.clients.solana.SolanaSignClient
import com.gemwallet.android.blockchain.clients.solana.SolanaSignerPreloader
import com.gemwallet.android.blockchain.clients.sui.SuiBroadcastClient
import com.gemwallet.android.blockchain.clients.sui.SuiNodeStatusClient
import com.gemwallet.android.blockchain.clients.sui.SuiSignClient
import com.gemwallet.android.blockchain.clients.sui.SuiSignerPreloader
import com.gemwallet.android.blockchain.clients.ton.TonBroadcastClient
import com.gemwallet.android.blockchain.clients.ton.TonNodeStatusClient
import com.gemwallet.android.blockchain.clients.ton.TonSignClient
import com.gemwallet.android.blockchain.clients.ton.TonSignerPreloader
import com.gemwallet.android.blockchain.clients.tron.TronBroadcastClient
import com.gemwallet.android.blockchain.clients.tron.TronNodeStatusClient
import com.gemwallet.android.blockchain.clients.tron.TronSignClient
import com.gemwallet.android.blockchain.clients.tron.TronSignerPreloader
import com.gemwallet.android.blockchain.clients.xrp.XrpBroadcastClient
import com.gemwallet.android.blockchain.clients.xrp.XrpNodeStatusClient
import com.gemwallet.android.blockchain.clients.xrp.XrpSignClient
import com.gemwallet.android.blockchain.clients.xrp.XrpSignerPreloader
import com.gemwallet.android.blockchain.operators.SignTransfer
import com.gemwallet.android.cases.pricealerts.EnablePriceAlertCase
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.buy.BuyRepository
import com.gemwallet.android.data.repositoreis.config.ConfigRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.ext.available
import com.gemwallet.android.interactors.sync.SyncTransactions
import com.gemwallet.android.services.SyncService
import com.wallet.core.primitives.Chain
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DataModule {

    @Provides
    @Singleton
    fun providesBroadcastProxy(
        rpcClients: RpcClientAdapter,
    ): BroadcastProxy = BroadcastProxy(
        Chain.available().mapNotNull {
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
                Chain.World,
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
        Chain.available().mapNotNull {
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
                Chain.World,
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
    fun provideNodeStatusClient(
        rpcClients: RpcClientAdapter,
    ): NodeStatusClientsProxy {
        return NodeStatusClientsProxy(
            Chain.available().mapNotNull {
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
                    Chain.World,
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
    fun provideSyncService(
        gemApiClient: GemApiClient,
        configRepository: ConfigRepository,
        sessionRepository: SessionRepository,
        walletsRepository: WalletsRepository,
        buyRepository: BuyRepository,
        syncTransactions: SyncTransactions,
        enablePriceAlertCase: EnablePriceAlertCase,
    ): SyncService {
        return SyncService(
            gemApiClient = gemApiClient,
            configRepository = configRepository,
            sessionRepository = sessionRepository,
            walletsRepository = walletsRepository,
            syncTransactions = syncTransactions,
            enablePriceAlertCase = enablePriceAlertCase,
            buyRepository = buyRepository,
        )
    }
}