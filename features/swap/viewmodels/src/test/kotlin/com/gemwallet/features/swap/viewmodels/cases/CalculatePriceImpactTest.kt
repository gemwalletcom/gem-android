package com.gemwallet.features.swap.viewmodels.cases

import com.gemwallet.features.swap.viewmodels.models.PriceImpactType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import kotlin.math.abs
import kotlin.math.absoluteValue

class CalculatePriceImpactTest {

    @Test
    fun `calculatePriceImpactCore should return null when pay amount is zero`() {
        val result = calculatePriceImpactCore(BigDecimal.ZERO, BigDecimal("100"))
        assertNull(result)
    }

    @Test
    fun `calculatePriceImpactCore should return null when receive amount is zero`() {
        val result = calculatePriceImpactCore(BigDecimal("100"), BigDecimal.ZERO)
        assertNull(result)
    }

    @Test
    fun `calculatePriceImpactCore should return null when both amounts are zero`() {
        val result = calculatePriceImpactCore(BigDecimal.ZERO, BigDecimal.ZERO)
        assertNull(result)
    }

    @Test
    fun `calculatePriceImpactCore should return Positive type for favorable swap`() {
        val pay = BigDecimal("100")
        val receive = BigDecimal("110")
        
        val result = calculatePriceImpactCore(pay, receive)
        
        assertNotNull(result)
        assertEquals(PriceImpactType.Positive, result!!.type)
        assertEquals(false, result.isHigh)
        assertTrue("Expected ~10% but got ${result.percentage}", abs(result.percentage!! - 10.0) < 0.001)
    }

    @Test
    fun `calculatePriceImpactCore should return null for very small negative impact`() {
        val pay = BigDecimal("100")
        val receive = BigDecimal("99.5")
        
        val result = calculatePriceImpactCore(pay, receive)
        
        assertNull("Impact less than 1 percent should return null", result)
    }

    @Test
    fun `calculatePriceImpactCore should return Medium type for 1-5 percent negative impact`() {
        val pay = BigDecimal("100")
        val receive = BigDecimal("97")
        
        val result = calculatePriceImpactCore(pay, receive)
        
        assertNotNull(result)
        assertEquals(PriceImpactType.Medium, result!!.type)
        assertEquals(false, result.isHigh)
        assertTrue("Expected ~-3% but got ${result.percentage}", abs(result.percentage!! - (-3.0)) < 0.001)
    }

    @Test
    fun `calculatePriceImpactCore should return High type for greater than 5 percent negative impact`() {
        val pay = BigDecimal("100")
        val receive = BigDecimal("90")
        
        val result = calculatePriceImpactCore(pay, receive)
        
        assertNotNull(result)
        assertEquals(PriceImpactType.High, result!!.type)
        assertEquals(false, result.isHigh)
        assertTrue("Expected ~-10% but got ${result.percentage}", abs(result.percentage!! - (-10.0)) < 0.001)
    }

    @Test
    fun `calculatePriceImpactCore should handle equal pay and receive amounts`() {
        val pay = BigDecimal("100")
        val receive = BigDecimal("100")
        
        val result = calculatePriceImpactCore(pay, receive)
        
        assertNull("Equal amounts should have 0 percent impact and return null", result)
    }

    @Test
    fun `calculatePriceImpactCore should calculate correct impact for different decimals`() {
        val pay = BigDecimal("1.5")
        val receive = BigDecimal("3.0")
        
        val result = calculatePriceImpactCore(pay, receive)
        
        assertNotNull(result)
        assertEquals(PriceImpactType.Positive, result!!.type)
        assertEquals(false, result.isHigh)
        assertTrue("Expected ~100% but got ${result.percentage}", abs(result.percentage!! - 100.0) < 0.001)
    }

    @Test
    fun `calculatePriceImpactCore should handle very large numbers`() {
        val pay = BigDecimal("1000000000")
        val receive = BigDecimal("950000000")
        
        val result = calculatePriceImpactCore(pay, receive)
        
        assertNotNull(result)
        assertEquals(PriceImpactType.High, result!!.type)
        assertEquals(false, result.isHigh)
        assertTrue("Expected ~-5% but got ${result.percentage}", abs(result.percentage!! - (-5.0)) < 0.01)
    }

    @Test
    fun `calculatePriceImpactCore should handle very small numbers`() {
        val pay = BigDecimal("0.00001")
        val receive = BigDecimal("0.000008")
        
        val result = calculatePriceImpactCore(pay, receive)
        
        assertNotNull(result)
        assertEquals(PriceImpactType.High, result!!.type)
        assertEquals(false, result.isHigh)
        assertTrue("Expected ~-20% but got ${result.percentage}", abs(result.percentage!! - (-20.0)) < 0.01)
    }

    @Test
    fun `calculatePriceImpactCore should use custom isHighProvider when provided`() {
        val pay = BigDecimal("100")
        val receive = BigDecimal("97")
        val customHighProvider = { impact: Double -> impact.absoluteValue > 2.0 }
        
        val result = calculatePriceImpactCore(pay, receive, customHighProvider)
        
        assertNotNull(result)
        assertEquals(PriceImpactType.Medium, result!!.type)
        assertEquals(true, result.isHigh)
        assertTrue("Expected ~-3% but got ${result.percentage}", abs(result.percentage!! - (-3.0)) < 0.001)
    }

    @Test
    fun `calculatePriceImpactCore should handle boundary case at exactly 1 percent negative impact`() {
        val pay = BigDecimal("100")
        val receive = BigDecimal("99")
        
        val result = calculatePriceImpactCore(pay, receive)
        
        assertNotNull(result)
        assertEquals(PriceImpactType.Medium, result!!.type)
        assertEquals(false, result.isHigh)
        assertTrue("Expected ~-1% but got ${result.percentage}", abs(result.percentage!! - (-1.0)) < 0.01)
    }

    @Test
    fun `calculatePriceImpactCore should handle boundary case at exactly 5 percent negative impact`() {
        val pay = BigDecimal("100")
        val receive = BigDecimal("95")
        
        val result = calculatePriceImpactCore(pay, receive)
        
        assertNotNull(result)
        assertTrue("Expected ~-5% but got ${result!!.percentage}", abs(result.percentage!! - (-5.0)) < 0.01)
    }

    @Test
    fun `calculatePriceImpactCore should handle precision edge cases`() {
        val pay = BigDecimal("100.123456789")
        val receive = BigDecimal("98.765432109")
        
        val result = calculatePriceImpactCore(pay, receive)
        
        assertNotNull(result)
        assertEquals(PriceImpactType.Medium, result!!.type)
        assertEquals(false, result.isHigh)
    }

    @Test
    fun `calculatePriceImpactCore should handle negative amounts gracefully`() {
        val pay = BigDecimal("-100")
        val receive = BigDecimal("97")
        
        val result = calculatePriceImpactCore(pay, receive)
        
        assertNotNull(result)
        assertEquals(false, result!!.isHigh)
    }

    @Test
    fun `calculatePriceImpactCore should handle fractional amounts`() {
        val pay = BigDecimal("0.5")
        val receive = BigDecimal("0.48")
        
        val result = calculatePriceImpactCore(pay, receive)
        
        assertNotNull(result)
        assertEquals(PriceImpactType.Medium, result!!.type)
        assertEquals(false, result.isHigh)
        assertTrue("Expected ~-4% but got ${result.percentage}", abs(result.percentage!! - (-4.0)) < 0.01)
    }
}