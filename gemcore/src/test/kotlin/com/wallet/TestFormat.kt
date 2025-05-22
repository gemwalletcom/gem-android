package com.wallet

import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.format
import com.wallet.core.primitives.Currency
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.math.BigInteger

class TestFormat {

    @Test
    fun testFormat_shor() {
        assertEquals(Crypto(BigInteger.valueOf(123)).format(0, "", 2), "123.00")
        assertEquals(Crypto(BigInteger.valueOf(12344)).format(6, "", 2), "0.01")
        assertEquals(Crypto(BigInteger.valueOf(1_000_000)).format(0, "", 2), "1,000,000.00")
        assertEquals(Crypto(BigInteger.valueOf(1_000)).format(0, "", 2), "1,000.00")
        assertEquals(Crypto(BigInteger.valueOf(1)).format(1, "", 2), "0.10")
        assertEquals(Crypto(BigInteger.valueOf(1)).format(2, "", 2), "0.01")
        assertEquals(Crypto(BigInteger.valueOf(1)).format(3, "", 2, dynamicPlace = true), "0.001")
        assertEquals(Crypto(BigInteger.valueOf(1)).format(4, "", 2, dynamicPlace = true), "0.0001")
        assertEquals(Crypto(BigInteger.valueOf(12345678910)).format(6, "", 2), "12,345.67")
        assertEquals(Crypto(BigInteger.valueOf(12345678910)).format(10, "", 2), "1.23")
        assertEquals(Crypto(BigInteger.valueOf(12345678910)).format(18, "", 2, dynamicPlace = true), "0.00000001")
    }

    @Test
    fun testCurrency_Format() {
        assertEquals(Currency.USD.format(2.0), "$2.00")
        assertEquals(Currency.USD.format(2.04E-6), "$0.00")
        assertEquals(Currency.USD.format(2.04E-6, dynamicPlace = true), "$0.00000204")
    }
}