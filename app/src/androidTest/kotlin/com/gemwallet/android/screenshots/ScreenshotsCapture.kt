package com.gemwallet.android.screenshots

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.takeScreenshot
import androidx.test.core.graphics.writeToTestStorage
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.gemwallet.android.App
import com.gemwallet.android.BuildConfig
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.core.IsNull
import org.junit.Before
import org.junit.runner.RunWith
import java.util.Locale


private const val BASIC_PACKAGE = "com.gemwallet.android"
private const val LAUNCH_TIMEOUT = 5000L
private const val SCREEN_TIMEOUT = 1000L

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 18)
class ScreenshotsCapture {
    private lateinit var device: UiDevice

    @Before
    fun startMainActivityFromHomeScreen() {
        // Initialize UiDevice instance
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Start from the home screen
        device.pressHome()

        // Wait for launcher
        val launcherPackage: String = device.launcherPackageName
        assertThat(launcherPackage, IsNull.notNullValue())
        device.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), LAUNCH_TIMEOUT)

        val context = ApplicationProvider.getApplicationContext<App>()

        prepareWallet(context)
        device.pressBack()
    }

//    @Test
//    fun takeScreenShots() {
//        assertThat(device, notNullValue())
//        val locales = arrayOf(
//            Pair("en", ""),
//            Pair("ar", ""),
//            Pair("de", ""),
//            Pair("es", ""),
//            Pair("fr", ""),
//            Pair("ja", ""),
//            Pair("ko", ""),
//            Pair("pl", ""),
//            Pair("pt", "BR"),
//            Pair("ru", ""),
//            Pair("tr", ""),
//            Pair("uk", ""),
//            Pair("vi", ""),
//            Pair("zh", "CN"),
//            Pair("zh", "TW"),
//        )
//
//        for (locale in locales) {
//            runScenario(language = locale.first, country = locale.second)
//        }
//    }

    private fun runScenario(language: String, country: String) {
        val context = ApplicationProvider.getApplicationContext<App>()
        setLanguage(language, country, context)
        val path = "$language${if (country.isEmpty()) "" else "-$country"}"

        device.wait(Until.hasObject(By.res("assetsManageAction")), 5_000)
        takeScreenshot().writeToTestStorage("$path/0_assets_list")

        device.findObject(By.res("assets_list"))
            .scrollUntil(Direction.DOWN, Until.findObject(By.res(AssetId(Chain.Solana).toIdentifier())))
        device.findObject(By.res(AssetId(Chain.Solana).toIdentifier())).click()
        runBlocking { delay(SCREEN_TIMEOUT * 2) }
        takeScreenshot().writeToTestStorage("$path/1_asset_details")

        device.wait(Until.hasObject(By.res("assetChart")), 5_000)
        device.findObject(By.res("assetChart")).click()
        runBlocking { delay(SCREEN_TIMEOUT * 3) }
        takeScreenshot().writeToTestStorage("$path/2_asset_chart")
        runBlocking { delay(SCREEN_TIMEOUT) }

        device.pressBack()
        device.wait(Until.hasObject(By.res("assetStake")), 5_000)
        device.findObject(By.res("assetStake")).click()
        runBlocking { delay(SCREEN_TIMEOUT * 5) }
        takeScreenshot().writeToTestStorage("$path/3_asset_stake")

        device.pressBack()
        device.wait(Until.hasObject(By.res("assetBuy")), 5_000)
        device.findObject(By.res("assetBuy")).click()
        runBlocking { delay(SCREEN_TIMEOUT * 5) }
        takeScreenshot().writeToTestStorage("$path/4_asset_buy")

        device.pressBack()
        device.pressBack()
        device.wait(Until.hasObject(By.res("assetsManageAction")), 5_000)
        device.findObject(By.res("assetsManageAction")).click()
        runBlocking { delay(SCREEN_TIMEOUT) }
        takeScreenshot().writeToTestStorage("$path/5_asset_manage")
        runBlocking { delay(SCREEN_TIMEOUT) }
        device.pressBack()

        device.wait(Until.hasObject(By.res("activitiesTab")), 5_000)
        device.findObject(By.res("activitiesTab")).click()
        runBlocking { delay(SCREEN_TIMEOUT) }
        takeScreenshot().writeToTestStorage("$path/6_activities")
        device.pressBack()

        device.wait(Until.hasObject(By.res("settingsTab")), 5_000)
        device.findObject(By.res("settingsTab")).click()
        runBlocking { delay(SCREEN_TIMEOUT) }
        takeScreenshot().writeToTestStorage("$path/7_settings")
        device.pressBack()
        device.pressBack()
        runBlocking { delay(SCREEN_TIMEOUT) }
    }

    private fun setLanguage(language: String, country: String, context: Context) {
        val monitor = InstrumentationRegistry.getInstrumentation()
            .addMonitor("com.gemwallet.android.MainActivity", null, false)
        val intent = context.packageManager.getLaunchIntentForPackage(BASIC_PACKAGE)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)
        val activity = monitor.waitForActivityWithTimeout(2000)
        val baseContext = activity.baseContext
        val locale = Locale(language, country)
        Locale.setDefault(locale)
        val config: Configuration = baseContext.resources.configuration
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }

    private fun prepareWallet(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(BASIC_PACKAGE)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)
        // Wait for the app to appear
        device.wait(Until.hasObject(By.pkg(BASIC_PACKAGE).depth(0)), LAUNCH_TIMEOUT)

        device.wait(Until.hasObject(By.res("import")), LAUNCH_TIMEOUT * 5)
        device.findObject(By.res("import")).click()
        runBlocking { delay(SCREEN_TIMEOUT) }

        device.findObject(By.res("multicoin_item")).click()
        runBlocking { delay(SCREEN_TIMEOUT) }

        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.setPrimaryClip(ClipData.newPlainText("phrase", BuildConfig.TEST_PHRASE))
        runBlocking { delay(SCREEN_TIMEOUT) }
        device.findObject(By.res("paste")).click()
        runBlocking { delay(SCREEN_TIMEOUT / 2) }
        device.findObject(By.res("main_action")).click()
        runBlocking { delay(SCREEN_TIMEOUT * 15) }
    }
}