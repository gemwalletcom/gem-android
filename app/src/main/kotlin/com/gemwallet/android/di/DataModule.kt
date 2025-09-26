package com.gemwallet.android.di

import com.gemwallet.android.blockchain.clients.algorand.AlgorandSignClient
import com.gemwallet.android.blockchain.clients.aptos.AptosSignClient
import com.gemwallet.android.blockchain.clients.bitcoin.BitcoinSignClient
import com.gemwallet.android.blockchain.clients.cardano.CardanoSignClient
import com.gemwallet.android.blockchain.clients.cosmos.CosmosSignClient
import com.gemwallet.android.blockchain.clients.ethereum.EvmSignClient
import com.gemwallet.android.blockchain.clients.hyper.HyperCoreSignClient
import com.gemwallet.android.blockchain.clients.near.NearSignClient
import com.gemwallet.android.blockchain.clients.polkadot.PolkadotSignClient
import com.gemwallet.android.blockchain.clients.solana.SolanaSignClient
import com.gemwallet.android.blockchain.clients.stellar.StellarSignClient
import com.gemwallet.android.blockchain.clients.sui.SuiSignClient
import com.gemwallet.android.blockchain.clients.ton.TonSignClient
import com.gemwallet.android.blockchain.clients.tron.TronSignClient
import com.gemwallet.android.blockchain.clients.xrp.XrpSignClient
import com.gemwallet.android.blockchain.services.BroadcastService
import com.gemwallet.android.blockchain.services.NodeStatusService
import com.gemwallet.android.blockchain.services.SignClientProxy
import com.gemwallet.android.blockchain.services.SignerPreloaderProxy
import com.gemwallet.android.cases.device.SyncSubscription
import com.gemwallet.android.cases.transactions.SyncTransactions
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.buy.BuyRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.ext.available
import com.gemwallet.android.ext.toChainType
import com.gemwallet.android.services.SyncService
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.ChainType
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uniffi.gemstone.GemGateway
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DataModule {

    @Provides
    @Singleton
    fun providesBroadcastProxy(
        gateway: GemGateway,
    ): BroadcastService = BroadcastService(
        gateway = gateway,
    )

    @Provides
    @Singleton
    fun provideSignerPreloader(
        gateway: GemGateway,
    ): SignerPreloaderProxy {
        return SignerPreloaderProxy(
            gateway = gateway,
        )
    }

    @Provides
    @Singleton
    fun provideSignService(
        assetsRepository: AssetsRepository,
    ): SignClientProxy = SignClientProxy(
        clients = Chain.available().map {
            when (it.toChainType()) {
                ChainType.Ethereum -> EvmSignClient(it)
                ChainType.Bitcoin -> BitcoinSignClient(it)
                ChainType.Solana -> SolanaSignClient(it, assetsRepository)
                ChainType.Cosmos -> CosmosSignClient(it)
                ChainType.Ton -> TonSignClient(it)
                ChainType.Tron -> TronSignClient(it)
                ChainType.Aptos -> AptosSignClient(it)
                ChainType.Sui -> SuiSignClient(it)
                ChainType.Xrp -> XrpSignClient(it)
                ChainType.Near -> NearSignClient(it)
                ChainType.Algorand -> AlgorandSignClient(it)
                ChainType.Stellar -> StellarSignClient(it)
                ChainType.Polkadot -> PolkadotSignClient(it)
                ChainType.Cardano -> CardanoSignClient(it)
                ChainType.HyperCore -> HyperCoreSignClient(it)
            }
        },
    )

    @Singleton
    @Provides
    fun provideNodeStatusClient(
        gateway: GemGateway,
    ): NodeStatusService {
        return NodeStatusService(gateway)
    }

    @Singleton
    @Provides
    fun provideSyncService(
        sessionRepository: SessionRepository,
        walletsRepository: WalletsRepository,
        buyRepository: BuyRepository,
        syncTransactions: SyncTransactions,
        syncSubscription: SyncSubscription,
    ): SyncService {
        return SyncService(
            sessionRepository = sessionRepository,
            walletsRepository = walletsRepository,
            syncTransactions = syncTransactions,
            buyRepository = buyRepository,
            syncSubscription = syncSubscription,
        )
    }
}