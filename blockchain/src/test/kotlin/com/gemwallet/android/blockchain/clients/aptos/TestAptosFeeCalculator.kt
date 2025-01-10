package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.blockchain.clients.aptos.models.AptosAccount
import com.gemwallet.android.blockchain.clients.aptos.services.AptosAccountsService
import com.gemwallet.android.blockchain.clients.aptos.services.AptosFeeService
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.DestinationAddress
import com.wallet.core.blockchain.aptos.models.AptosGasFee
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.math.BigInteger

class TestAptosFeeCalculator {
    @Test
    fun testAptosFeeAccountError() {
        val preloader = AptosSignerPreloader(
            chain = Chain.Aptos,
            accountsService = object : AptosAccountsService {
                override suspend fun accounts(address: String): Result<AptosAccount> {
                    return Result.success(aptosAccountResponse.copy(sequence_number = null, error_code = null, message = "Error message"))
                }

            },
            feeService = object : AptosFeeService {
                override suspend fun feePrice(): Result<AptosGasFee> {
                    return Result.success(aptosFeeResponse)
                }
            }
        )
        try {
            runBlocking {
                preloader.preloadNativeTransfer(
                    params = ConfirmParams.TransferParams.Native(
                        assetId = AssetId(Chain.Aptos),
                        from = Account(Chain.Aptos, "0x80c3cca35602e4568a7ac88d4d91110f8efa6c45c659439c2b4ed04033059c6f", ""),
                        amount = BigInteger.valueOf(10_000_000_000),
                        destination = DestinationAddress("0xd7257c62806cea85fc8eaf947377b672fe062b81e6c0b19b6d8a3f408e59cf8c"),
                        isMaxAmount = false
                    )
                )
            }
            assertTrue(false)
        } catch (err: Throwable) {
            assertEquals("Error message", err.message)
        }
    }
}