package com.wallet

import com.gemwallet.android.math.parseNumber
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class TestNumberParse {

    @Test
    fun testUSInput() {
        assertEquals("0.1", "0.1".parseNumber().toString())
        assertEquals(BigDecimal("0.1"), "0.1".parseNumber())

        assertEquals("0.2", "0.2".parseNumber().toString())
        assertEquals(BigDecimal("0.2"), "0.2".parseNumber())


        assertEquals("1", "1".parseNumber().toString())
        assertEquals(BigDecimal("1"), "1".parseNumber())

        assertEquals("1.2", "1.2".parseNumber().toString())
        assertEquals(BigDecimal("1.2"), "1.2".parseNumber())
        assertEquals(1.2f, "1.2".parseNumber().toFloat())

        assertEquals("1.13", "1.13".parseNumber().toString())
        assertEquals(BigDecimal("1.13"), "1.13".parseNumber())

        assertEquals("0.1234567", "0.1234567".parseNumber().toString())
        assertEquals("0.1234567", "0,1234567".parseNumber().toString())
        assertEquals("730.1234567", "730.1234567".parseNumber().toString())
        assertEquals("730.1234567", "730,1234567".parseNumber().toString())
        assertEquals("122726.1234567", "122,726.1234567".parseNumber().toString())
    }

    @Test
    fun testRU_UAInput() {
        assertEquals(BigDecimal("0.1234567"), "0.1234567".parseNumber())
        assertEquals(BigDecimal("0.1234567"), "0,1234567".parseNumber())
        assertEquals(BigDecimal("730.1234567"), "730.1234567".parseNumber())
        assertEquals(BigDecimal("730.1234567"), "730,1234567".parseNumber())
        assertEquals(BigDecimal("122726.1234567"), "122 726.1234567".parseNumber())
    }
}