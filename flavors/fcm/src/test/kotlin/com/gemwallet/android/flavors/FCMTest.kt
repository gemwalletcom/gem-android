package com.gemwallet.android.flavors

import org.junit.Assert.*
import org.junit.Test

class FCMTest {

    @Test
    fun parseData_withNullType_returnsNull() {
        val result = FCM.parseData(null, "data")
        assertNull(result)
    }

    @Test
    fun parseData_withEmptyType_returnsNull() {
        val result = FCM.parseData("", "data")
        assertNull(result)
    }

    @Test
    fun parseData_withNullData_returnsNull() {
        val result = FCM.parseData("transaction", null)
        assertNull(result)
    }

    @Test
    fun parseData_withEmptyData_returnsNull() {
        val result = FCM.parseData("transaction", "")
        assertNull(result)
    }

    @Test
    fun parseData_withInvalidType_returnsNull() {
        val result = FCM.parseData("invalidType", """{"walletIndex":1,"assetId":"bitcoin","transactionId":"abc123"}""")
        assertNull(result)
    }

    @Test
    fun parseData_withValidTransactionData_returnsCorrectPair() {
        val jsonData = """{"walletIndex":1,"assetId":"bitcoin","transactionId":"abc123"}"""
        val result = FCM.parseData("transaction", jsonData)
        
        assertNotNull(result)
        assertEquals(1, result?.first)
        assertEquals("bitcoin", result?.second)
    }

    @Test
    fun parseData_withValidAssetData_returnsCorrectPair() {
        val jsonData = """{"assetId":"ethereum"}"""
        val result = FCM.parseData("asset", jsonData)
        
        assertNotNull(result)
        assertNull(result?.first)
        assertEquals("ethereum", result?.second)
    }

    @Test
    fun parseData_withTestType_returnsNull() {
        val result = FCM.parseData("test", """{"someData":"value"}""")
        assertNull(result)
    }

    @Test
    fun parseData_withPriceAlertType_returnsNull() {
        val result = FCM.parseData("priceAlert", """{"someData":"value"}""")
        assertNull(result)
    }

    @Test
    fun parseData_withBuyAssetType_returnsNull() {
        val result = FCM.parseData("buyAsset", """{"someData":"value"}""")
        assertNull(result)
    }

    @Test
    fun parseData_withSwapAssetType_returnsNull() {
        val result = FCM.parseData("swapAsset", """{"someData":"value"}""")
        assertNull(result)
    }

    @Test
    fun parseData_withMalformedTransactionJson_returnsNull() {
        val invalidJson = """{"walletIndex":"notAnInt","assetId":"bitcoin"}"""
        val result = FCM.parseData("transaction", invalidJson)
        assertNull(result)
    }

    @Test
    fun parseData_withMalformedAssetJson_returnsNull() {
        val invalidJson = """{"invalidField":"value"}"""
        val result = FCM.parseData("asset", invalidJson)
        assertNull(result)
    }

    @Test
    fun parseData_withCompletelyInvalidJson_returnsNull() {
        val invalidJson = """not valid json at all"""
        val result = FCM.parseData("transaction", invalidJson)
        assertNull(result)
    }

    @Test
    fun parseData_withTransactionMissingFields_returnsNull() {
        val incompleteJson = """{"walletIndex":1}"""
        val result = FCM.parseData("transaction", incompleteJson)
        assertNull(result)
    }

    @Test
    fun parseData_withAssetMissingFields_returnsNull() {
        val incompleteJson = """{"someOtherField":"value"}"""
        val result = FCM.parseData("asset", incompleteJson)
        assertNull(result)
    }
}