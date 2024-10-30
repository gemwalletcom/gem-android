package com.gemwallet.android

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet
import dagger.hilt.android.HiltAndroidApp
import java.lang.System


@HiltAndroidApp
class App : Application(), ImageLoaderFactory {
    private val RELAY_URL = "relay.walletconnect.com"

    override fun onCreate() {
        super.onCreate()
        walletConnectConfig(this)
    }

    private fun walletConnectConfig(application: Application) {
        val projectId = "0d9db544461f12ed6dd8450a6c717753"
        val serverUrl = "wss://$RELAY_URL?projectId=${projectId}"
        val connectionType = ConnectionType.AUTOMATIC
        CoreClient.initialize(
            metaData = Core.Model.AppMetaData(
                name = "Gem Wallet",
                description = "Gem Web3 Wallet",
                url = "https://gemwallet.com",
                icons = listOf("https://gemwallet.com/images/gem-logo-256x256.png"),
                redirect = "gem://wc/"
            ),
            connectionType = connectionType,
            application = application,
            relayServerUrl = serverUrl,
            onError = {}
        )
        val initParams = Wallet.Params.Init(core = CoreClient)
        Web3Wallet.initialize(initParams) { _ -> }
    }

    companion object {
        init {
            System.loadLibrary("TrustWalletCore")
            System.loadLibrary("gemstone")
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(512L * 1024 * 1024) // 512Mb
                    .build()
            }
            .build()
    }
}