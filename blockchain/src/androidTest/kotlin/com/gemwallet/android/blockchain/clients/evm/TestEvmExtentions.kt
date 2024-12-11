package com.gemwallet.android.blockchain.clients.evm

import com.gemwallet.android.blockchain.clients.ethereum.encodeTransactionData
import com.gemwallet.android.blockchain.clients.ethereum.getDestinationAddress
import com.gemwallet.android.blockchain.includeLibs
import com.gemwallet.android.math.toHexString
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.EVMChain
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.math.BigInteger

class TestEvmExtentions {
    companion object {
        init {
            includeLibs()
        }
    }

    @Test
    fun testEncodeTransactionData() {
        val result = EVMChain.encodeTransactionData(
            AssetId(Chain.SmartChain, "0xbb4CdB9CBd36B01bD1cBaEBF2De08d9173bc095c"),
            null,
            BigInteger.TEN,
            "0x9b1DB81180c31B1b428572Be105E209b5A6222b7",
        )
        assertEquals(
            "0xa9059cbb0000000000000000000000009b1db81180c31b1b428572be105e209b5a6222b7000000000" +
                    "000000000000000000000000000000000000000000000000000000a",
            result.toHexString()
        )
    }

    @Test
    fun testEncodeTransactionData_with_memo() {
        val result = EVMChain.encodeTransactionData(
            AssetId(Chain.SmartChain, "0xbb4CdB9CBd36B01bD1cBaEBF2De08d9173bc095c"),
            "0x00000000000000000000000000000000000000000000000000000002eedef652",
            BigInteger.TEN,
            "0x9b1DB81180c31B1b428572Be105E209b5A6222b7",
        )
        assertEquals("0x00000000000000000000000000000000000000000000000000000002eedef652", result.toHexString())
    }

    @Test
    fun testGetDestinationAddress_token() {
        val result = EVMChain.getDestinationAddress(
            AssetId(Chain.SmartChain, "0xbb4CdB9CBd36B01bD1cBaEBF2De08d9173bc095c"),
            "0x9b1DB81180c31B1b428572Be105E209b5A6222b7"
        )
        assertEquals(result, "0xbb4CdB9CBd36B01bD1cBaEBF2De08d9173bc095c")
    }

    @Test
    fun testGetDestinationAddress_native() {
        val result = EVMChain.getDestinationAddress(
            AssetId(Chain.SmartChain),
            "0x9b1DB81180c31B1b428572Be105E209b5A6222b7"
        )
        assertEquals(result, "0x9b1DB81180c31B1b428572Be105E209b5A6222b7")
    }
}