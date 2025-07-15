package com.gemwallet.android

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.svg.SvgDecoder
import com.gemwallet.android.data.repositoreis.assets.PriceWebSocketClient
import com.reown.android.Core
import com.reown.android.CoreClient
import com.reown.android.relay.ConnectionType
import com.reown.walletkit.client.Wallet
import com.reown.walletkit.client.WalletKit
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject


@HiltAndroidApp
class App : Application(), SingletonImageLoader.Factory,  Application.ActivityLifecycleCallbacks {

    @Inject
    lateinit var priceClient: PriceWebSocketClient

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        walletConnectConfig()
    }

    private fun walletConnectConfig() {
        val projectId = "3bc07cd7179d11ea65335fb9377702b6"
        val connectionType = ConnectionType.AUTOMATIC
        val metaData = Core.Model.AppMetaData(
            name = "Gem Wallet",
            description = "Gem Web3 Wallet",
            url = "https://gemwallet.com",
            icons = listOf("https://gemwallet.com/images/gem-logo-256x256.png"),
            redirect = "gem://wc/"
        )
        CoreClient.initialize(
            application = this,
            projectId = projectId,
            metaData = metaData,
            connectionType = connectionType,
            telemetryEnabled = false,
        ) {
            Log.d("WalletConnect", "Err", it.throwable)
        }
        val initParams = Wallet.Params.Init(core = CoreClient)
        WalletKit.initialize(initParams) { _ -> }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(SvgDecoder.Factory())
            }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(this, 0.25)
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

    override fun onActivityResumed(activity: Activity) {
        priceClient.start()
    }

    override fun onActivityStopped(activity: Activity) {
        priceClient.stop()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) { }

    override fun onActivityStarted(activity: Activity) { }

    override fun onActivityPaused(activity: Activity) { }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) { }

    override fun onActivityDestroyed(activity: Activity) { }

    companion object {
        init {
            System.loadLibrary("TrustWalletCore")
            System.loadLibrary("gemstone")
        }
    }
}