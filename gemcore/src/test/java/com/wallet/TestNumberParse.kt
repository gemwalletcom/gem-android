package com.wallet

import com.gemwallet.android.math.numberParse
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class TestNumberParse {

    @Test
    fun testUSInput() {
        assertEquals("0.1", "0.1".numberParse().toString())
        assertEquals(BigDecimal("0.1"), "0.1".numberParse())

        assertEquals("0.2", "0.2".numberParse().toString())
        assertEquals(BigDecimal("0.2"), "0.2".numberParse())


        assertEquals("1", "1".numberParse().toString())
        assertEquals(BigDecimal("1"), "1".numberParse())

        assertEquals("1.2", "1.2".numberParse().toString())
        assertEquals(BigDecimal("1.2"), "1.2".numberParse())
        assertEquals(1.2f, "1.2".numberParse().toFloat())

        assertEquals("1.13", "1.13".numberParse().toString())
        assertEquals(BigDecimal("1.13"), "1.13".numberParse())

        assertEquals("0.1234567", "0.1234567".numberParse().toString())
        assertEquals("0.1234567", "0,1234567".numberParse().toString())
        assertEquals("730.1234567", "730.1234567".numberParse().toString())
        assertEquals("730.1234567", "730,1234567".numberParse().toString())
        assertEquals("122726.1234567", "122,726.1234567".numberParse().toString())
    }

    @Test
    fun testRU_UAInput() {
        assertEquals(BigDecimal("0.1234567"), "0.1234567".numberParse())
        assertEquals(BigDecimal("0.1234567"), "0,1234567".numberParse())
        assertEquals(BigDecimal("730.1234567"), "730.1234567".numberParse())
        assertEquals(BigDecimal("730.1234567"), "730,1234567".numberParse())
        assertEquals(BigDecimal("122726.1234567"), "122 726.1234567".numberParse())
    }
}