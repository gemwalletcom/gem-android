package com.gemwallet.android.data.repositoreis.assets

import android.os.Build
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.service.store.database.AssetsDao
import com.gemwallet.android.data.service.store.database.PriceAlertsDao
import com.gemwallet.android.data.service.store.database.PricesDao
import com.gemwallet.android.data.service.store.database.entities.toDTO
import com.gemwallet.android.data.service.store.database.entities.toRecord
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.model.BuildInfo
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetPrice
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.FiatRate
import com.wallet.core.primitives.WebSocketPriceAction
import com.wallet.core.primitives.WebSocketPriceActionType
import com.wallet.core.primitives.WebSocketPricePayload
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.wss
import io.ktor.client.request.request
import io.ktor.http.HttpMethod
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicBoolean

class PriceWebSocketClient(
    private val sessionRepository: SessionRepository,
    private val buildInfo: BuildInfo,
    private val assetsDao: AssetsDao,
    private val pricesDao: PricesDao,
    private val priceAlertsDao: PriceAlertsDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val priceActionFlow = MutableSharedFlow<WebSocketPriceAction>()
    private val cancelCommandFlow = MutableSharedFlow<Boolean>()
    private var started = AtomicBoolean(false)

    val client = HttpClient(CIO) {
        install(WebSockets) {
            pingIntervalMillis = 15_000
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
        install(UserAgent) {
            agent = "Gem/${buildInfo.versionCode}  Android/${Build.VERSION.RELEASE} Version/${buildInfo.versionName}"
        }
    }

    init {
        scope.launch {
            sessionRepository.session().collectLatest {
                if (!started.get()) {
                    start()
                }
            }
        }
    }

    fun start() = scope.launch(Dispatchers.IO) {
        try {
            started.set(sessionRepository.session().firstOrNull()?.wallet != null)
            if (!started.get()) {
                return@launch
            }
            client.wss(
                method = HttpMethod.Get,
                host = "api.gemwallet.com",
                port = 443,
                path = "/v1/ws/prices",
            ) {
                webSocketBlock()
            }
        } catch (_: Throwable) {
        }
    }

    fun stop() = scope.launch(Dispatchers.Default) {
        started.set(false)
        cancelCommandFlow.emit(true)
    }

    private suspend fun updateRates(newRates: List<FiatRate>, currency: Currency) {
        pricesDao.setRates(newRates.toRecord())
        newRates.firstOrNull { it.symbol == currency.string }?.let { rate ->
            pricesDao.getAll().firstOrNull()?.map {
                it.copy(value = (it.usdValue ?: 0.0) * rate.rate)
            }?.let { pricesDao.insert(it) }
        }
    }

    private suspend fun updatePrices(prices: List<AssetPrice>, rate: FiatRate) {
        val newPrices = prices.toRecord(rate)
        runCatching { pricesDao.insert(newPrices) }
    }

    private suspend fun handlePriceUpdate(prices: List<AssetPrice>, newRates: List<FiatRate>) {
        val currency = sessionRepository.getCurrentCurrency()
        updateRates(newRates, currency)
        val rate = pricesDao.getRates(currency).firstOrNull()?.toDTO() ?: return
        updatePrices(prices, rate)
    }

    private fun reinit() = scope.launch(Dispatchers.Default) {
        val ids = assetsDao.getAssetsPriceUpdate().mapNotNull { it.toAssetId() }
        val priceAlerts = priceAlertsDao.getAlerts().firstOrNull()
            ?.mapNotNull { it.assetId.toAssetId() } ?: emptyList()
        priceActionFlow.emit(
            WebSocketPriceAction(
                action = WebSocketPriceActionType.Subscribe,
                assets = (ids + priceAlerts).takeIf { it.isNotEmpty() } ?: listOf(
                    AssetId(Chain.Bitcoin),
                    AssetId(Chain.Ethereum),
                    AssetId(Chain.Solana),
                    AssetId(Chain.SmartChain),
                )
            )
        )

    }

    fun addAssetId(id: AssetId) = scope.launch(Dispatchers.Default) {
        priceActionFlow.emit(
            WebSocketPriceAction(
                action = WebSocketPriceActionType.Add,
                assets = listOf(id)
            )
        )
    }

    suspend fun DefaultClientWebSocketSession.webSocketBlock() {
        launch(Dispatchers.IO) {
            priceActionFlow.collectLatest {
                try {
                    sendSerialized(it)
                } catch (_: Throwable) { }
            }
        }
        val wss = this
        launch(Dispatchers.Default) {
            cancelCommandFlow.collectLatest {
                try {
                    if (it) {
                        started.set(false)
                        wss.cancel()
                    }
                } catch (_: Throwable) { }
            }
        }
        runCatching { reinit() }
        while (started.get()) {
            val pricePayload = try {
                receiveDeserialized<WebSocketPricePayload>()
            } catch (_: Throwable) {
                delay(15_000)
                continue
            }
            handlePriceUpdate(pricePayload.prices, pricePayload.rates)
        }
        cancel()
    }
}