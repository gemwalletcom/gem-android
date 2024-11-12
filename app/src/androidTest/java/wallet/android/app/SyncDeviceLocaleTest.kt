package wallet.android.app

import com.gemwallet.android.data.repositoreis.device.DeviceRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class SyncDeviceLocaleTest {

    @Test
    fun testZH() {
        assertEquals("zh-Hans", DeviceRepository.getLocale(Locale("zh")))
        assertEquals("zh-Hans", DeviceRepository.getLocale(Locale("zh", "CN")))
        assertEquals("zh-Hans", DeviceRepository.getLocale(Locale("zh", "TW")))
    }

    @Test
    fun testPT() {
        assertEquals("pt", DeviceRepository.getLocale(Locale("pt")))
        assertEquals("pt-BR", DeviceRepository.getLocale(Locale("pt", "BR")))
    }

    @Test
    fun testEN() {
        assertEquals("en", DeviceRepository.getLocale(Locale("en")))
        assertEquals("en", DeviceRepository.getLocale(Locale("en", "US")))
    }
}