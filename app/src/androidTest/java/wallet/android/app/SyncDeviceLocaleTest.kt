package wallet.android.app

import com.gemwallet.android.interactors.sync.SyncDevice
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class SyncDeviceLocaleTest {

    @Test
    fun testZH() {
        assertEquals("zh-Hans", SyncDevice.getLocale(Locale("zh")))
        assertEquals("zh-Hans", SyncDevice.getLocale(Locale("zh", "CN")))
        assertEquals("zh-Hans", SyncDevice.getLocale(Locale("zh", "TW")))
    }

    @Test
    fun testPT() {
        assertEquals("pt", SyncDevice.getLocale(Locale("pt")))
        assertEquals("pt-BR", SyncDevice.getLocale(Locale("pt", "BR")))
    }

    @Test
    fun testEN() {
        assertEquals("en", SyncDevice.getLocale(Locale("en")))
        assertEquals("en", SyncDevice.getLocale(Locale("en", "US")))
    }
}