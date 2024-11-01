package com.gemwallet.android.data.repositoreis.di

import com.gemwallet.android.blockchain.RpcClientAdapter
import com.gemwallet.android.blockchain.clients.aptos.AptosTransactionStatusClient
import com.gemwallet.android.blockchain.clients.bitcoin.BitcoinTransactionStatusClient
import com.gemwallet.android.blockchain.clients.cosmos.CosmosTransactionStatusClient
import com.gemwallet.android.blockchain.clients.ethereum.EvmTransactionStatusClient
import com.gemwallet.android.blockchain.clients.near.NearTransactionStatusClient
import com.gemwallet.android.blockchain.clients.solana.SolanaTransactionStatusClient
import com.gemwallet.android.blockchain.clients.sui.SuiTransactionStatusClient
import com.gemwallet.android.blockchain.clients.ton.TonTransactionStatusClient
import com.gemwallet.android.blockchain.clients.tron.TronTransactionStatusClient
import com.gemwallet.android.blockchain.clients.xrp.XrpTransactionStatusClient
import com.gemwallet.android.cases.transactions.CreateTransactionCase
import com.gemwallet.android.cases.transactions.GetTransactionCase
import com.gemwallet.android.cases.transactions.GetTransactionsCase
import com.gemwallet.android.cases.transactions.PutTransactionsCase
import com.gemwallet.android.data.repositoreis.transactions.TransactionsRepository
import com.gemwallet.android.data.service.store.database.AssetsDao
import com.gemwallet.android.data.service.store.database.TransactionsDao
import com.gemwallet.android.data.services.gemapi.di.GemJson
import com.gemwallet.android.ext.available
import com.google.gson.Gson
import com.wallet.core.primitives.Chain
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object TransactionsModule {

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
        stateClients = Chain.available().map {
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
    fun provideGetTransactionsCase(transactionsRepository: com.gemwallet.android.data.repositoreis.transactions.TransactionsRepository): GetTransactionsCase {
        return transactionsRepository
    }

    @Singleton
    @Provides
    fun provideGetTransactionCase(transactionsRepository: com.gemwallet.android.data.repositoreis.transactions.TransactionsRepository): GetTransactionCase {
        return transactionsRepository
    }

    @Singleton
    @Provides
    fun providePutTransactionsCase(transactionsRepository: com.gemwallet.android.data.repositoreis.transactions.TransactionsRepository): PutTransactionsCase {
        return transactionsRepository
    }

    @Singleton
    @Provides
    fun provideCreateTransactionsCase(transactionsRepository: com.gemwallet.android.data.repositoreis.transactions.TransactionsRepository): CreateTransactionCase {
        return transactionsRepository
    }

}

