package com.gemwallet.android.data.repositoreis.di

import com.gemwallet.android.blockchain.RpcClientAdapter
import com.gemwallet.android.blockchain.clients.algorand.AlgorandTransactionStatusClient
import com.gemwallet.android.blockchain.clients.aptos.AptosTransactionStatusClient
import com.gemwallet.android.blockchain.clients.bitcoin.BitcoinTransactionStatusClient
import com.gemwallet.android.blockchain.clients.cardano.CardanoTransactionStatusClient
import com.gemwallet.android.blockchain.clients.cosmos.CosmosTransactionStatusClient
import com.gemwallet.android.blockchain.clients.ethereum.EvmTransactionStatusClient
import com.gemwallet.android.blockchain.clients.near.NearTransactionStatusClient
import com.gemwallet.android.blockchain.clients.polkadot.PolkadotTransactionStatusClient
import com.gemwallet.android.blockchain.clients.solana.SolanaTransactionStatusClient
import com.gemwallet.android.blockchain.clients.stellar.StellarTransactionStatusClient
import com.gemwallet.android.blockchain.clients.sui.SuiTransactionStatusClient
import com.gemwallet.android.blockchain.clients.ton.TonTransactionStatusClient
import com.gemwallet.android.blockchain.clients.tron.TronTransactionStatusClient
import com.gemwallet.android.blockchain.clients.xrp.XrpTransactionStatusClient
import com.gemwallet.android.cases.transactions.CreateTransaction
import com.gemwallet.android.cases.transactions.GetTransaction
import com.gemwallet.android.cases.transactions.GetTransactionUpdateTime
import com.gemwallet.android.cases.transactions.GetTransactions
import com.gemwallet.android.cases.transactions.PutTransactions
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
                ChainType.Solana -> SolanaTransactionStatusClient(it, rpcClients.getClient(Chain.Solana))
                ChainType.Cosmos -> CosmosTransactionStatusClient(it, rpcClients.getClient(it))
                ChainType.Ton -> TonTransactionStatusClient(it, rpcClients.getClient(it))
                ChainType.Tron -> TronTransactionStatusClient(it, rpcClients.getClient(Chain.Tron))
                ChainType.Aptos -> AptosTransactionStatusClient(it, rpcClients.getClient(it))
                ChainType.Sui -> SuiTransactionStatusClient(it, rpcClients.getClient(it))
                ChainType.Xrp -> XrpTransactionStatusClient(it, rpcClients.getClient(it))
                ChainType.Near -> NearTransactionStatusClient(it, rpcClients.getClient(it))
                ChainType.Algorand -> AlgorandTransactionStatusClient(it, rpcClients.getClient(it))
                ChainType.Stellar -> StellarTransactionStatusClient(it, rpcClients.getClient(it))
                ChainType.Polkadot -> PolkadotTransactionStatusClient(it, rpcClients.getClient(it))
                ChainType.Cardano -> CardanoTransactionStatusClient(it, rpcClients.getClient(it))
            }
        },
    )

    @Singleton
    @Provides
    fun provideGetTransactionsCase(transactionsRepository: TransactionsRepository): GetTransactions {
        return transactionsRepository
    }

    @Singleton
    @Provides
    fun provideGetTransactionCase(transactionsRepository: TransactionsRepository): GetTransaction {
        return transactionsRepository
    }

    @Singleton
    @Provides
    fun providePutTransactionsCase(transactionsRepository: TransactionsRepository): PutTransactions {
        return transactionsRepository
    }

    @Singleton
    @Provides
    fun provideCreateTransactionsCase(transactionsRepository: TransactionsRepository): CreateTransaction {
        return transactionsRepository
    }


    @Singleton
    @Provides
    fun provideUpdateTime(transactionsRepository: TransactionsRepository): GetTransactionUpdateTime {
        return transactionsRepository
    }
}

