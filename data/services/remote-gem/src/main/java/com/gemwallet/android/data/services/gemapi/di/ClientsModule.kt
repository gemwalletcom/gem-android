package com.gemwallet.android.data.services.gemapi.di

import android.content.Context
import com.gemwallet.android.blockchain.RpcClientAdapter
import com.gemwallet.android.blockchain.clients.aptos.AptosRpcClient
import com.gemwallet.android.blockchain.clients.bitcoin.BitcoinRpcClient
import com.gemwallet.android.blockchain.clients.cosmos.CosmosRpcClient
import com.gemwallet.android.blockchain.clients.ethereum.EvmRpcClient
import com.gemwallet.android.blockchain.clients.near.NearRpcClient
import com.gemwallet.android.blockchain.clients.solana.SolanaRpcClient
import com.gemwallet.android.blockchain.clients.sui.SuiRpcClient
import com.gemwallet.android.blockchain.clients.ton.TonRpcClient
import com.gemwallet.android.blockchain.clients.tron.TronRpcClient
import com.gemwallet.android.blockchain.clients.xrp.XrpRpcClient
import com.gemwallet.android.cases.nodes.GetCurrentNodeCase
import com.gemwallet.android.cases.nodes.GetNodesCase
import com.gemwallet.android.cases.nodes.SetCurrentNodeCase
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.data.services.gemapi.Transactions
import com.gemwallet.android.data.services.gemapi.http.GemApiStaticClient
import com.gemwallet.android.data.services.gemapi.http.NodeSelectorInterceptor
import com.gemwallet.android.data.services.gemapi.http.ResultCallAdapterFactory
import com.gemwallet.android.data.services.gemapi.models.DeviceSerializer
import com.gemwallet.android.data.services.gemapi.models.NameRecordDeserialize
import com.gemwallet.android.data.services.gemapi.models.NodeSerializer
import com.gemwallet.android.data.services.gemapi.models.ReleaseDeserialize
import com.gemwallet.android.data.services.gemapi.models.SubscriptionSerializer
import com.gemwallet.android.data.services.gemapi.models.SwapQuoteDeserializer
import com.gemwallet.android.data.services.gemapi.models.TransactionsSerializer
import com.gemwallet.android.ext.available
import com.gemwallet.android.serializer.AssetIdSerializer
import com.google.gson.Gson
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Device
import com.wallet.core.primitives.NameRecord
import com.wallet.core.primitives.Node
import com.wallet.core.primitives.Release
import com.wallet.core.primitives.Subscription
import com.wallet.core.primitives.SwapQuote
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
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton


@Qualifier
annotation class NodeHttpClient

@Qualifier
annotation class GemHttpClient

@Qualifier
annotation class GemJson

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

    @GemJson
    @Provides
    @Singleton
    fun provideGemApiJson(gson: Gson): Gson {
        return gson.newBuilder()
            .registerTypeAdapter(Node::class.java, NodeSerializer())
            .registerTypeAdapter(Device::class.java, DeviceSerializer())
            .registerTypeAdapter(Subscription::class.java, SubscriptionSerializer())
            .registerTypeAdapter(Transactions::class.java, TransactionsSerializer())
            .registerTypeAdapter(NameRecord::class.java, NameRecordDeserialize())
            .registerTypeAdapter(SwapQuote::class.java, SwapQuoteDeserializer())
            .registerTypeAdapter(Release::class.java, ReleaseDeserialize())
            .registerTypeAdapter(AssetId::class.java, AssetIdSerializer())
            .create()
    }

    @Provides
    @Singleton
    fun provideJSONConverterFactory(@GemJson gson: Gson): Factory = GsonConverterFactory.create(gson)

    @Provides
    @Singleton
    fun provideGemApiClient(@GemHttpClient httpClient: OkHttpClient, converterFactory: Factory): GemApiClient =
        Retrofit.Builder()
            .baseUrl("https://api.gemwallet.com")
            .client(httpClient)
            .addConverterFactory(converterFactory)
            .addCallAdapterFactory(ResultCallAdapterFactory())
            .build()
            .create(GemApiClient::class.java)

    @Provides
    @Singleton
    fun provideGemApiStaticClient(@GemHttpClient httpClient: OkHttpClient, converterFactory: Factory): GemApiStaticClient {
        return Retrofit.Builder()
            .baseUrl("https://assets.gemwallet.com")
            .client(httpClient)
            .addConverterFactory(converterFactory)
            .addCallAdapterFactory(ResultCallAdapterFactory())
            .build()
            .create(GemApiStaticClient::class.java)
    }

    @Provides
    @Singleton
    fun provideRpcClientsAdapter(
        @NodeHttpClient httpClient: OkHttpClient,
        gson: Gson,
        converter: Factory,
    ): RpcClientAdapter {
        val ethConverter = GsonConverterFactory.create(
            gson.newBuilder()
                .registerTypeAdapter(
                    EvmRpcClient.EvmNumber::class.java,
                    EvmRpcClient.BalanceDeserializer()
                )
                .registerTypeAdapter(
                    EvmRpcClient.TokenBalance::class.java,
                    EvmRpcClient.TokenBalanceDeserializer()
                )
                .create()
        )
        val tonConverter = GsonConverterFactory.create(
            gson.newBuilder().registerTypeAdapter(
                TonRpcClient.JetonAddress::class.java,
                TonRpcClient.JetonAddressSerializer()
            ).create()
        )
        val adapter = RpcClientAdapter()
        Chain.available().mapNotNull {
            val url = "https://${it.string}"
            val rpc: Any = when (it) {
                Chain.Doge,
                Chain.Litecoin,
                Chain.Bitcoin -> buildClient(url, BitcoinRpcClient::class.java, converter, httpClient)
                Chain.AvalancheC,
                Chain.Polygon,
                Chain.Arbitrum,
                Chain.Base,
                Chain.OpBNB,
                Chain.SmartChain,
                Chain.Fantom,
                Chain.Gnosis,
                Chain.Optimism,
                Chain.Manta,
                Chain.Blast,
                Chain.ZkSync,
                Chain.Linea,
                Chain.Mantle,
                Chain.Celo,
                Chain.Ethereum -> buildClient(url, EvmRpcClient::class.java, ethConverter, httpClient)
                Chain.Solana -> buildClient(url, SolanaRpcClient::class.java, converter, httpClient)
                Chain.Osmosis,
                Chain.Thorchain,
                Chain.Celestia,
                Chain.Injective,
                Chain.Sei,
                Chain.Noble,
                Chain.Cosmos -> buildClient(url, CosmosRpcClient::class.java, converter, httpClient)
                Chain.Ton -> buildClient(url, TonRpcClient::class.java, tonConverter, httpClient)
                Chain.Tron -> buildClient(url, TronRpcClient::class.java, converter, httpClient)
                Chain.Aptos -> buildClient(url, AptosRpcClient::class.java, converter, httpClient)
                Chain.Sui -> buildClient(url, SuiRpcClient::class.java, converter, httpClient)
                Chain.Xrp -> buildClient(url, XrpRpcClient::class.java, converter, httpClient)
                Chain.Near -> buildClient(url, NearRpcClient::class.java, converter, httpClient)
                Chain.World -> return@mapNotNull null
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
    ): T = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        .addConverterFactory(converterFactory)
        .addCallAdapterFactory(ResultCallAdapterFactory())
        .build()
        .create(clazz)
}