package com.gemwallet.android.data.services.gemapi.di

import android.content.Context
import com.gemwallet.android.blockchain.Mime
import com.gemwallet.android.blockchain.RpcClientAdapter
import com.gemwallet.android.blockchain.clients.algorand.services.AlgorandService
import com.gemwallet.android.blockchain.clients.aptos.services.AptosServices
import com.gemwallet.android.blockchain.clients.bitcoin.services.BitcoinRpcClient
import com.gemwallet.android.blockchain.clients.cardano.services.CardanoServices
import com.gemwallet.android.blockchain.clients.cosmos.services.CosmosRpcClient
import com.gemwallet.android.blockchain.clients.ethereum.services.EvmRpcClient
import com.gemwallet.android.blockchain.clients.near.NearRpcClient
import com.gemwallet.android.blockchain.clients.polkadot.services.PolkadotServices
import com.gemwallet.android.blockchain.clients.solana.services.SolanaRpcClient
import com.gemwallet.android.blockchain.clients.stellar.services.StellarService
import com.gemwallet.android.blockchain.clients.sui.SuiRpcClient
import com.gemwallet.android.blockchain.clients.ton.TonRpcClient
import com.gemwallet.android.blockchain.clients.tron.TronRpcClient
import com.gemwallet.android.blockchain.clients.xrp.XrpRpcClient
import com.gemwallet.android.cases.nodes.GetCurrentNodeCase
import com.gemwallet.android.cases.nodes.GetNodesCase
import com.gemwallet.android.cases.nodes.SetCurrentNodeCase
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.data.services.gemapi.GemApiStaticClient
import com.gemwallet.android.data.services.gemapi.http.NodeSelectorInterceptor
import com.gemwallet.android.data.services.gemapi.http.ResultCallAdapterFactory
import com.gemwallet.android.ext.available
import com.gemwallet.android.ext.toChainType
import com.gemwallet.android.serializer.jsonEncoder
import com.google.gson.Gson
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.ChainType
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter.Factory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
annotation class NodeHttpClient

@Qualifier
annotation class GemHttpClient

@InstallIn(SingletonComponent::class)
@Module
object ClientsModule {
    @GemHttpClient
    @Provides
    @Singleton
    fun provideGemHttpClient(): OkHttpClient = OkHttpClient
        .Builder()
        .connectionPool(ConnectionPool(8, 5, TimeUnit.MINUTES))
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        })
        .addNetworkInterceptor { chain ->
            chain.proceed(
                chain.request()
                    .newBuilder()
                    .header("User-Agent", "Gem/Android")
                    .build()
            )
        }
        .build()

    @NodeHttpClient
    @Provides
    @Singleton
    fun provideHttpClient(
        @ApplicationContext context: Context,
        getNodesCase: GetNodesCase,
        getCurrentNodeCase: GetCurrentNodeCase,
        setCurrentNodeCase: SetCurrentNodeCase,
    ): OkHttpClient = OkHttpClient
        .Builder()
        .connectionPool(ConnectionPool(32, 5, TimeUnit.MINUTES))
        .cache(Cache(context.cacheDir, 10 * 1024 * 1024))
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .addInterceptor(NodeSelectorInterceptor(getNodesCase, getCurrentNodeCase, setCurrentNodeCase))
        .addInterceptor(HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        })
        .build()

    @Provides
    @Singleton
    fun provideGemApiClient(@GemHttpClient httpClient: OkHttpClient): GemApiClient =
        Retrofit.Builder()
            .baseUrl("https://api.gemwallet.com")
            .client(httpClient)
            .addConverterFactory(jsonEncoder.asConverterFactory(Mime.Json.value))
            .addCallAdapterFactory(ResultCallAdapterFactory())
            .build()
            .create(GemApiClient::class.java)

    @Provides
    @Singleton
    fun provideGemApiStaticClient(@GemHttpClient httpClient: OkHttpClient): GemApiStaticClient {
        return Retrofit.Builder()
            .baseUrl("https://assets.gemwallet.com")
            .client(httpClient)
            .addConverterFactory(jsonEncoder.asConverterFactory(Mime.Json.value))
            .addCallAdapterFactory(ResultCallAdapterFactory())
            .build()
            .create(GemApiStaticClient::class.java)
    }

    @Provides
    @Singleton
    fun provideRpcClientsAdapter(
        @NodeHttpClient httpClient: OkHttpClient,
    ): RpcClientAdapter {
        val gson = Gson()
        val converter = jsonEncoder.asConverterFactory(Mime.Json.value)
        val ethConverter = GsonConverterFactory.create(
            gson.newBuilder()
                .registerTypeAdapter(EvmRpcClient.EvmNumber::class.java, EvmRpcClient.BalanceDeserializer())
                .registerTypeAdapter(EvmRpcClient.TokenBalance::class.java, EvmRpcClient.TokenBalanceDeserializer())
                .create()
        )
        val tonConverter = GsonConverterFactory.create(
            gson.newBuilder()
                .registerTypeAdapter(TonRpcClient.JetonAddress::class.java, TonRpcClient.JetonAddressSerializer())
                .create()
        )
        val gsonConverter = GsonConverterFactory.create(gson)
        val adapter = RpcClientAdapter()
        Chain.available().map {
            val url = "https://${it.string}"
            val rpc: Any = when (it.toChainType()) {
                ChainType.Bitcoin -> buildClient(url, BitcoinRpcClient::class.java, converter, httpClient)
                ChainType.Ethereum -> buildClient(url, EvmRpcClient::class.java, ethConverter, httpClient)
                ChainType.Solana -> buildClient(url, SolanaRpcClient::class.java, gsonConverter, httpClient)
                ChainType.Cosmos -> buildClient(url, CosmosRpcClient::class.java, converter, httpClient)
                ChainType.Ton -> buildClient(url, TonRpcClient::class.java, tonConverter, httpClient)
                ChainType.Tron -> buildClient(url, TronRpcClient::class.java, gsonConverter, httpClient)
                ChainType.Aptos -> buildClient(url, AptosServices::class.java, converter, httpClient)
                ChainType.Sui -> buildClient(url, SuiRpcClient::class.java, gsonConverter, httpClient)
                ChainType.Xrp -> buildClient(url, XrpRpcClient::class.java, converter, httpClient)
                ChainType.Near -> buildClient(url, NearRpcClient::class.java, gsonConverter, httpClient)
                ChainType.Algorand -> buildClient(url, AlgorandService::class.java, converter, httpClient)
                ChainType.Stellar -> buildClient(url, StellarService::class.java, converter, httpClient)
                ChainType.Polkadot -> buildClient(url, PolkadotServices::class.java, converter, httpClient)
                ChainType.Cardano -> buildClient(url, CardanoServices::class.java, converter, httpClient)
            }
            adapter.add(it, rpc)
        }
        return adapter
    }

    private fun <T>buildClient(
        baseUrl: String,
        clazz: Class<T>,
        converterFactory: Factory,
        httpClient: OkHttpClient
    ): T {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(httpClient)
            .addConverterFactory(converterFactory)
            .addCallAdapterFactory(ResultCallAdapterFactory())
            .build()
            .create(clazz)
    }
}