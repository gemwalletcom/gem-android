package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.blockchain.clients.aptos.models.AptosAccount
import com.gemwallet.android.blockchain.clients.aptos.services.AptosAccountsService
import com.gemwallet.android.blockchain.clients.aptos.services.AptosFeeService
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.DestinationAddress
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.blockchain.aptos.models.AptosGasFee
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.math.BigInteger

class TestAptosSignerPreloader {

    @Test
    fun testAptosPreload() {
        var serviceAddressParam = mutableListOf<String>()
        val preloader = AptosSignerPreloader(
            chain = Chain.Aptos,
            accountsService = object : AptosAccountsService {
                override suspend fun accounts(address: String): Result<AptosAccount> {
                    serviceAddressParam.add(address)
                    return Result.success(aptosAccountResponse)
                }

            },
            feeService = object : AptosFeeService {
                override suspend fun feePrice(): Result<AptosGasFee> {
                    return Result.success(aptosFeeResponse)
                }
            }
        )
        val result = runBlocking {
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
        assertEquals("0x80c3cca35602e4568a7ac88d4d91110f8efa6c45c659439c2b4ed04033059c6f", serviceAddressParam[0])
        assertEquals("0xd7257c62806cea85fc8eaf947377b672fe062b81e6c0b19b6d8a3f408e59cf8c", serviceAddressParam[1])
        assertEquals(BigInteger.valueOf(10_000_000_000), result.input.amount)
        assertEquals("0x80c3cca35602e4568a7ac88d4d91110f8efa6c45c659439c2b4ed04033059c6f", result.input.from.address)
        assertEquals(AssetId(Chain.Aptos).toIdentifier(), result.input.assetId.toIdentifier())
        assertEquals(false, result.input.isMax())
        assertEquals("0xd7257c62806cea85fc8eaf947377b672fe062b81e6c0b19b6d8a3f408e59cf8c", result.input.destination()?.address)
        assertEquals(null, result.input.memo())
        assertEquals(BigInteger.valueOf(202800L), result.chainData.fee().amount)
        assertEquals(BigInteger.valueOf(150L), (result.chainData.fee() as GasFee).maxGasPrice)
        assertEquals(BigInteger.valueOf(1352), (result.chainData.fee() as GasFee).limit)
        assertEquals(AssetId(Chain.Aptos).toIdentifier(), result.chainData.fee().feeAssetId.toIdentifier())
        assertEquals(TxSpeed.Normal, result.chainData.fee().speed)
        assertEquals(8L, (result.chainData as AptosSignerPreloader.AptosChainData).sequence)
    }

    @Test
    fun testAptosEmptyAccountPreload() {
        val preloader = AptosSignerPreloader(
            chain = Chain.Aptos,
            accountsService = object : AptosAccountsService {
                override suspend fun accounts(address: String): Result<AptosAccount> {
                    return Result.success(aptosAccountResponse.copy(sequence_number = null, error_code = "account_not_found"))
                }

            },
            feeService = object : AptosFeeService {
                override suspend fun feePrice(): Result<AptosGasFee> {
                    return Result.success(aptosFeeResponse)
                }
            }
        )
        val result = runBlocking {
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
        assertEquals(BigInteger.valueOf(202800L), result.chainData.fee().amount)
        assertEquals(BigInteger.valueOf(150L), (result.chainData.fee() as GasFee).maxGasPrice)
        assertEquals(BigInteger.valueOf(1352L), (result.chainData.fee() as GasFee).limit)
        assertEquals(AssetId(Chain.Aptos).toIdentifier(), result.chainData.fee().feeAssetId.toIdentifier())
        assertEquals(TxSpeed.Normal, result.chainData.fee().speed)
        assertEquals(0L, (result.chainData as AptosSignerPreloader.AptosChainData).sequence)
    }

    @Test
    fun testAptosFeeFail() {
        val preloader = AptosSignerPreloader(
            chain = Chain.Aptos,
            accountsService = object : AptosAccountsService {
                override suspend fun accounts(address: String): Result<AptosAccount> {
                    return Result.success(aptosAccountResponse.copy(sequence_number = null, error_code = null, message = "Message"))
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
            assertEquals(null, err.message)
        }
    }
}