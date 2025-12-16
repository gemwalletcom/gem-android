package com.gemwallet.android.domains.price.values

import com.gemwallet.android.domains.price.PriceState
import com.wallet.core.primitives.Currency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PriceableValueTest {

    @Test
    fun testPriceableValue_stateUp() {
        val price = createTestPriceableValue(dayChangePercentage = 2.5)

        assertEquals(PriceState.Up, price.state)
    }

    @Test
    fun testPriceableValue_stateDown() {
        val price = createTestPriceableValue(dayChangePercentage = -2.5)

        assertEquals(PriceState.Down, price.state)
    }

    @Test
    fun testPriceableValue_stateNone() {
        val price = createTestPriceableValue(dayChangePercentage = 0.0)

        assertEquals(PriceState.None, price.state)
    }

    @Test
    fun testPriceableValue_stateNullPercentage() {
        val price = createTestPriceableValue(dayChangePercentage = null)

        assertEquals(PriceState.None, price.state)
    }

    @Test
    fun testPriceableValue_dayChangePercentageFormattedPositive() {
        val price = createTestPriceableValue(dayChangePercentage = 2.5)
        val formatted = price.dayChangePercentageFormatted

        assertEquals("+2.50%", formatted)
    }

    @Test
    fun testPriceableValue_dayChangePercentageFormattedNegative() {
        val price = createTestPriceableValue(dayChangePercentage = -5.2)
        val formatted = price.dayChangePercentageFormatted

        assertEquals("-5.20%", formatted)
    }

    @Test
    fun testPriceableValue_dayChangePercentageFormattedZero() {
        val price = createTestPriceableValue(dayChangePercentage = 0.0)
        val formatted = price.dayChangePercentageFormatted

        assertEquals("0.00%", formatted)
    }

    @Test
    fun testPriceableValue_dayChangePercentageFormattedNull() {
        val price = createTestPriceableValue(dayChangePercentage = null)
        val formatted = price.dayChangePercentageFormatted

        assertEquals("0.00%", formatted)
    }

    @Test
    fun testPriceableValue_priceValueFormattedWithNull() {
        val price = createTestPriceableValue(priceValue = null)
        val formatted = price.priceValueFormated

        assertEquals("", formatted)
    }

    @Test
    fun testPriceableValue_priceValueFormattedWithNaN() {
        val price = createTestPriceableValue(priceValue = Double.NaN)
        val formatted = price.priceValueFormated

        assertEquals("", formatted)
    }

    @Test
    fun testPriceableValue_priceValueFormattedWithZero() {
        val price = createTestPriceableValue(priceValue = 0.0)
        val formatted = price.priceValueFormated

        assertTrue("Formatted zero price should not be empty", formatted.isNotEmpty())
    }

    @Test
    fun testPriceableValue_priceValueFormattedWithLowValue() {
        val price = createTestPriceableValue(priceValue = 0.0025)
        val formatted = price.priceValueFormated

        assertTrue("Formatted low price should not be empty", formatted.isNotEmpty())
    }

    @Test
    fun testPriceableValue_priceValueFormattedWithNegative() {
        val price = createTestPriceableValue(priceValue = -100.0)
        val formatted = price.priceValueFormated

        assertTrue("Formatted negative price should not be empty", formatted.isNotEmpty())
    }

    @Test
    fun testPriceableValue_multiplePropertiesConsistency() {
        val price = createTestPriceableValue(
            priceValue = 50000.0,
            dayChangePercentage = 3.5,
            currency = Currency.USD
        )

        assertEquals(50000.0, price.priceValue!!, 0.01)
        assertEquals(3.5, price.dayChangePercentage!!, 0.01)
        assertEquals(Currency.USD, price.currency)
        assertEquals(PriceState.Up, price.state)
        assertEquals("+3.50%", price.dayChangePercentageFormatted)
    }

    @Test
    fun testPriceableValue_stateTransitionFromPositiveToNegative() {
        val priceUp = createTestPriceableValue(dayChangePercentage = 1.5)
        assertEquals(PriceState.Up, priceUp.state)

        val priceDown = createTestPriceableValue(dayChangePercentage = -1.5)
        assertEquals(PriceState.Down, priceDown.state)
    }

    @Test
    fun testPriceableValue_stateWithVerySmallChange() {
        val priceSmallUp = createTestPriceableValue(dayChangePercentage = 0.001)
        assertEquals(PriceState.None, priceSmallUp.state)

        val priceSmallDown = createTestPriceableValue(dayChangePercentage = -0.001)
        assertEquals(PriceState.None, priceSmallDown.state)
    }

    @Test
    fun testPriceableValue_stateWithMinimumDetectableChange() {
        val priceMinUp = createTestPriceableValue(dayChangePercentage = 0.01)
        assertEquals(PriceState.Up, priceMinUp.state)

        val priceMinDown = createTestPriceableValue(dayChangePercentage = -0.01)
        assertEquals(PriceState.Down, priceMinDown.state)
    }

    private fun createTestPriceableValue(
        currency: Currency = Currency.USD,
        priceValue: Double? = 1000.0,
        dayChangePercentage: Double? = 0.0
    ): PriceableValue {
        return object : PriceableValue {
            override val currency: Currency = currency
            override val priceValue: Double? = priceValue
            override val dayChangePercentage: Double? = dayChangePercentage
        }
    }
}
