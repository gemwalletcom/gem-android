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
import com.gemwallet.android.ext.toChainType
import com.google.gson.Gson
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.ChainType
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
            when (it.toChainType()) {
                ChainType.Bitcoin -> BitcoinTransactionStatusClient(it, rpcClients.getClient(it))
                ChainType.Ethereum -> EvmTransactionStatusClient(it, rpcClients.getClient(it))
                ChainType.Solana -> SolanaTransactionStatusClient(rpcClients.getClient(Chain.Solana))
                ChainType.Cosmos -> CosmosTransactionStatusClient(it, rpcClients.getClient(it))
                ChainType.Ton -> TonTransactionStatusClient(rpcClients.getClient(it))
                ChainType.Tron -> TronTransactionStatusClient(rpcClients.getClient(Chain.Tron))
                ChainType.Aptos -> AptosTransactionStatusClient(it, rpcClients.getClient(it))
                ChainType.Sui -> SuiTransactionStatusClient(it, rpcClients.getClient(it))
                ChainType.Xrp -> XrpTransactionStatusClient(it, rpcClients.getClient(it))
                ChainType.Near -> NearTransactionStatusClient(it, rpcClients.getClient(it))
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

}

