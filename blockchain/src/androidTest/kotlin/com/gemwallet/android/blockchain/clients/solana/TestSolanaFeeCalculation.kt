package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.includeLibs
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.DestinationAddress
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.math.BigInteger

class TestSolanaFeeCalculation {
    companion object {
        init {
            includeLibs()
        }
    }

    @Test
    fun testSolana_calculate_native_fee() {
        val feeCalculator = SolanaFeeCalculator(TestSolanaFeeService())

        val result = runBlocking {
            feeCalculator.calculate(
                ConfirmParams.Builder(
                    assetId = AssetId(Chain.Solana),
                    from = Account(Chain.Solana, "AGkXQZ9qm99xukisDUHvspWHESrcjs8Y4AmQQgef3BRh", ""),
                    amount = BigInteger.valueOf(10_000_000)
                ).transfer(destination = DestinationAddress("AGkXQZ9qm99xukisDUHvspWHESrcjs8Y4AmQQgef3BRh"))
            )
        }
        assertEquals(BigInteger("105005000"), result.amount)
        assertEquals(BigInteger("1050000000"), result.minerFee)
        assertEquals(BigInteger("5000"), result.maxGasPrice)
        assertEquals(BigInteger("100000"), result.limit)
        assertEquals(BigInteger("30"), result.options["tokenAccountCreation"])
    }

    @Test
    fun testSolana_calculate_native_fee_without_priority_fee() {
        val feeCalculator = SolanaFeeCalculator(TestSolanaFeeService(fees = emptyList()))

        val result = runBlocking {
            feeCalculator.calculate(
                ConfirmParams.Builder(
                    assetId = AssetId(Chain.Solana),
                    from = Account(Chain.Solana, "AGkXQZ9qm99xukisDUHvspWHESrcjs8Y4AmQQgef3BRh", ""),
                    amount = BigInteger.valueOf(10_000_000)
                ).transfer(destination = DestinationAddress("AGkXQZ9qm99xukisDUHvspWHESrcjs8Y4AmQQgef3BRh"))
            )
        }
        assertEquals(BigInteger("6000"), result.amount)
        assertEquals(BigInteger("10000"), result.minerFee)
        assertEquals(BigInteger("5000"), result.maxGasPrice)
        assertEquals(BigInteger("100000"), result.limit)
        assertEquals(BigInteger("30"), result.options["tokenAccountCreation"])
    }

    @Test
    fun testSolana_calculate_native_fee_without_create() {
        val feeCalculator = SolanaFeeCalculator(TestSolanaFeeService(fees = emptyList(), rentExemption = 0))

        val result = runBlocking {
            feeCalculator.calculate(
                ConfirmParams.Builder(
                    assetId = AssetId(Chain.Solana),
                    from = Account(Chain.Solana, "AGkXQZ9qm99xukisDUHvspWHESrcjs8Y4AmQQgef3BRh", ""),
                    amount = BigInteger.valueOf(10_000_000)
                ).transfer(destination = DestinationAddress("AGkXQZ9qm99xukisDUHvspWHESrcjs8Y4AmQQgef3BRh"))
            )
        }
        assertEquals(BigInteger("6000"), result.amount)
        assertEquals(BigInteger("10000"), result.minerFee)
        assertEquals(BigInteger("5000"), result.maxGasPrice)
        assertEquals(BigInteger("100000"), result.limit)
        assertEquals(BigInteger("0"), result.options["tokenAccountCreation"])
    }

    @Test
    fun testSolana_calculate_token_fee_without_create() {
        val feeCalculator = SolanaFeeCalculator(TestSolanaFeeService(listOf(100, 200)))

        val result = runBlocking {
            feeCalculator.calculate(
                ConfirmParams.Builder(
                    assetId = AssetId(Chain.Solana, "Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB"),
                    from = Account(Chain.Solana, "AGkXQZ9qm99xukisDUHvspWHESrcjs8Y4AmQQgef3BRh", ""),
                    amount = BigInteger.valueOf(10_000_000)
                ).transfer(destination = DestinationAddress("AGkXQZ9qm99xukisDUHvspWHESrcjs8Y4AmQQgef3BRh"))
            )
        }
        assertEquals(BigInteger("15000"), result.amount)
        assertEquals(BigInteger("100000"), result.minerFee)
        assertEquals(BigInteger("5000"), result.maxGasPrice)
        assertEquals(BigInteger("100000"), result.limit)
        assertEquals(BigInteger("30"), result.options["tokenAccountCreation"])
    }
}