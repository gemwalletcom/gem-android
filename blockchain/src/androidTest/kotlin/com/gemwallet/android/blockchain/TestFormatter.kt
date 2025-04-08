package com.gemwallet.android.blockchain

import com.gemwallet.android.model.compactFormatter
import com.wallet.core.primitives.Currency
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.util.Locale

class TestFormatter {
    @Test
    fun testCompactFormat_Italy() {
//        assertEquals("0,00 €", Currency.EUR.compactFormatter(0.0, Locale.ITALY))
//        assertEquals("12,00 €", Currency.EUR.compactFormatter(12.0, Locale.ITALY))
//        assertEquals("1234,00 €", Currency.EUR.compactFormatter(1234.0, Locale.ITALY))
        assertEquals("5,00 Mio €", Currency.EUR.compactFormatter(5_000_000.0, Locale.ITALY))
        assertEquals("7,89 Mrd €", Currency.EUR.compactFormatter(7_890_000_000.0, Locale.ITALY))
        assertEquals("1,20 Bln €", Currency.EUR.compactFormatter(1_200_000_000_000.0, Locale.ITALY))
    }

    @Test
    fun testCompactFormat_Usd() {
//        assertEquals("\$0.00", Currency.USD.compactFormatter(0.0, Locale.US))
//        assertEquals("\$12.00", Currency.USD.compactFormatter(12.0, Locale.US))
//        assertEquals("\$1.23K", Currency.USD.compactFormatter(1234.0, Locale.US))
        assertEquals("\$5.00M", Currency.USD.compactFormatter(5_000_000.0, Locale.US))
        assertEquals("\$7.89B", Currency.USD.compactFormatter(7_890_000_000.0, Locale.US))
        assertEquals("\$1.20T", Currency.USD.compactFormatter(1_200_000_000_000.0, Locale.US))
    }
}