package com.gemwallet.features.swap.viewmodels.cases

import com.gemwallet.features.swap.viewmodels.models.PriceImpactType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal
import kotlin.math.absoluteValue

/**
 * Tests for the calculatePriceImpact function logic using calculatePriceImpactCore.
 */
class CalculatePriceImpactTest {

    @Test
    fun `calculatePriceImpactCore should return null when pay is zero`() {
        val result = calculatePriceImpactCore(
            pay = BigDecimal.ZERO,
            receive = BigDecimal("1000.0")
        )

        assertNull("Should return null when pay is zero", result)
    }

    @Test
    fun `calculatePriceImpactCore should return positive impact for negative price change`() {
        val result = calculatePriceImpactCore(
            pay = BigDecimal("1000.0"),
            receive = BigDecimal("900.0")
        )

        assertEquals(-10.0, result?.percentage ?: 0.0, 0.001)
        assertEquals(PriceImpactType.Positive, result?.type)
        assertEquals(false, result?.isHigh)
    }

    @Test
    fun `calculatePriceImpactCore should return positive impact for large negative change`() {
        val result = calculatePriceImpactCore(
            pay = BigDecimal("1000.0"),
            receive = BigDecimal("850.0"),
            isHighProvider = { impact -> impact.absoluteValue > 10.0 }
        )

        assertEquals(-15.0, result?.percentage ?: 0.0, 0.001)
        assertEquals(PriceImpactType.Positive, result?.type)
        assertEquals(true, result?.isHigh)
    }

    @Test
    fun `calculatePriceImpactCore should return null for very small positive impact`() {
        val result = calculatePriceImpactCore(
            pay = BigDecimal("1000.0"),
            receive = BigDecimal("1005.0")
        )

        assertNull("Should return null for impact less than 1%", result)
    }

    @Test
    fun `calculatePriceImpactCore should return medium impact for exactly 1 percent`() {
        val result = calculatePriceImpactCore(
            pay = BigDecimal("1000.0"),
            receive = BigDecimal("1010.0")
        )

        assertEquals(1.0, result?.percentage ?: 0.0, 0.001)
        assertEquals(PriceImpactType.Medium, result?.type)
        assertEquals(false, result?.isHigh)
    }

    @Test
    fun `calculatePriceImpactCore should return medium impact for 1 to 5 percent range`() {
        val result = calculatePriceImpactCore(
            pay = BigDecimal("1000.0"),
            receive = BigDecimal("1030.0")
        )

        assertEquals(3.0, result?.percentage ?: 0.0, 0.001)
        assertEquals(PriceImpactType.Medium, result?.type)
        assertEquals(false, result?.isHigh)
    }

    @Test
    fun `calculatePriceImpactCore should return high impact for exactly 5 percent`() {
        val result = calculatePriceImpactCore(
            pay = BigDecimal("1000.0"),
            receive = BigDecimal("1050.0")
        )

        assertEquals(5.0, result?.percentage ?: 0.0, 0.001)
        assertEquals(PriceImpactType.High, result?.type)
        assertEquals(false, result?.isHigh)
    }

    @Test
    fun `calculatePriceImpactCore should return high impact for greater than 5 percent`() {
        val result = calculatePriceImpactCore(
            pay = BigDecimal("1000.0"),
            receive = BigDecimal("1080.0"),
            isHighProvider = { impact -> impact.absoluteValue > 5.0 }
        )

        assertEquals(8.0, result?.percentage ?: 0.0, 0.001)
        assertEquals(PriceImpactType.High, result?.type)
        assertEquals(true, result?.isHigh)
    }

    @Test
    fun `calculatePriceImpactCore should handle very large amounts correctly`() {
        val result = calculatePriceImpactCore(
            pay = BigDecimal("1000000.0"),
            receive = BigDecimal("1030000.0")
        )

        assertEquals(3.0, result?.percentage ?: 0.0, 0.001)
        assertEquals(PriceImpactType.Medium, result?.type)
        assertEquals(false, result?.isHigh)
    }

    @Test
    fun `calculatePriceImpactCore should handle very small amounts correctly`() {
        val result = calculatePriceImpactCore(
            pay = BigDecimal("0.001"),
            receive = BigDecimal("0.00103")
        )

        assertEquals(3.0, result?.percentage ?: 0.0, 0.001)
        assertEquals(PriceImpactType.Medium, result?.type)
        assertEquals(false, result?.isHigh)
    }

    @Test
    fun `calculatePriceImpactCore should handle decimal precision correctly`() {
        val result = calculatePriceImpactCore(
            pay = BigDecimal("100.123456"),
            receive = BigDecimal("102.12595344")
        )

        assertEquals(1.999999968012799, result?.percentage ?: 0.0, 0.001)
        assertEquals(PriceImpactType.Medium, result?.type)
        assertEquals(false, result?.isHigh)
    }

    @Test
    fun `calculatePriceImpactCore should handle zero receive amount`() {
        val result = calculatePriceImpactCore(
            pay = BigDecimal("1000.0"),
            receive = BigDecimal.ZERO
        )

        assertEquals(-100.0, result?.percentage ?: 0.0, 0.001)
        assertEquals(PriceImpactType.Positive, result?.type)
        assertEquals(false, result?.isHigh)
    }

    @Test
    fun `calculatePriceImpactCore should handle equal amounts`() {
        val result = calculatePriceImpactCore(
            pay = BigDecimal("1000.0"),
            receive = BigDecimal("1000.0")
        )

        assertNull("Should return null for 0% impact", result)
    }
}