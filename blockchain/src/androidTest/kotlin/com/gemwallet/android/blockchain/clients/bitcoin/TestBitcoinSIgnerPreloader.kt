package com.gemwallet.android.blockchain.clients.bitcoin

import com.gemwallet.android.blockchain.clients.bitcoin.services.BitcoinFeeService
import com.gemwallet.android.blockchain.clients.bitcoin.services.BitcoinUTXOService
import com.gemwallet.android.blockchain.includeLibs
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.DestinationAddress
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.blockchain.bitcoin.models.BitcoinFeeResult
import com.wallet.core.blockchain.bitcoin.models.BitcoinUTXO
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.math.BigInteger

class TestBitcoinSignerPreloader {

    companion object {
        init {
            includeLibs()
        }
    }

    @Test
    fun testBitcoinMinimumByteFee() {
        assertEquals(BitcoinFeeCalculator.getMinimumByteFee(Chain.Bitcoin), BigInteger.ONE)
        assertEquals(BitcoinFeeCalculator.getMinimumByteFee(Chain.Litecoin), BigInteger.valueOf(5))
        assertEquals(BitcoinFeeCalculator.getMinimumByteFee(Chain.Doge), BigInteger.valueOf(1000))
    }

    @Test
    fun testBitcoinPriority() {
        assertEquals(BitcoinFeeCalculator.getFeePriority(Chain.Bitcoin, TxSpeed.Slow), "6")
        assertEquals(BitcoinFeeCalculator.getFeePriority(Chain.Bitcoin, TxSpeed.Normal), "3")
        assertEquals(BitcoinFeeCalculator.getFeePriority(Chain.Bitcoin, TxSpeed.Fast), "1")

        assertEquals(BitcoinFeeCalculator.getFeePriority(Chain.Litecoin, TxSpeed.Slow), "6")
        assertEquals(BitcoinFeeCalculator.getFeePriority(Chain.Litecoin, TxSpeed.Normal), "3")
        assertEquals(BitcoinFeeCalculator.getFeePriority(Chain.Litecoin, TxSpeed.Fast), "1")

        assertEquals(BitcoinFeeCalculator.getFeePriority(Chain.Doge, TxSpeed.Slow), "8")
        assertEquals(BitcoinFeeCalculator.getFeePriority(Chain.Doge, TxSpeed.Normal), "4")
        assertEquals(BitcoinFeeCalculator.getFeePriority(Chain.Doge, TxSpeed.Fast), "2")
    }

    @Test
    fun testBitcoinPreloader() {
        var requestPubKey = ""
        val preloader = BitcoinSignerPreloader(
            Chain.Bitcoin,
            object : BitcoinUTXOService {
                override suspend fun getUTXO(address: String): Result<List<BitcoinUTXO>> {
                    requestPubKey = address

                    return Result.success(
                        listOf(
                            BitcoinUTXO(
                                txid = "9f47e5d5ae3dac0662766f95d0cdda9c242c08d6baac4ce5d82464cf948abd53",
                                vout = 1,
                                value = "86055170",
                            )
                        )
                    )
                }

            },
            object : BitcoinFeeService {
                override suspend fun estimateFee(priority: String): Result<BitcoinFeeResult> {
                    val value = when (priority) {
                        "8" -> "0.17457567"
                        "4" -> "0.17457567"
                        "2" -> "0.60356369"
                        else -> return Result.failure(Exception("Incorrect priority"))
                    }
                    return Result.success(BitcoinFeeResult(value))
                }
            }
        )
        val result = runBlocking {
            preloader.preloadNativeTransfer(
                params = ConfirmParams.TransferParams.Native(
                    assetId = AssetId(Chain.Bitcoin),
                    from = Account(
                        Chain.Doge,
                        "DDyZeg24eU3csLa7LMWrZEoqnHXccz6c94",
                        "",
                        "dgub8rNuTi8ofZu1jVDKpBxW9VFo62kjjx3b6CcameEZnrNNHJ3sKCnWBxQSv6qAP6jrwZEpfT1ZdKsrcBFKGTMV8zgBtjZmvQt29VPnLzbHjjD"
                    ),
                    amount = BigInteger.valueOf(10_000_000_000),
                    destination = DestinationAddress("D8UBj4EfNfNWNCdnCSgpY48yZDqPdTZXWW"),
                    isMaxAmount = false
                )
            )
        }
        assertEquals("dgub8rNuTi8ofZu1jVDKpBxW9VFo62kjjx3b6CcameEZnrNNHJ3sKCnWBxQSv6qAP6jrwZEpfT1ZdKsrcBFKGTMV8zgBtjZmvQt29VPnLzbHjjD", requestPubKey)
        assertEquals(BigInteger.valueOf(10_000_000_000), result.input.amount)
        assertEquals("DDyZeg24eU3csLa7LMWrZEoqnHXccz6c94", result.input.from.address)
        assertEquals(AssetId(Chain.Bitcoin).toIdentifier(), result.input.assetId.toIdentifier())
        assertEquals(false, result.input.isMax())
        assertEquals("D8UBj4EfNfNWNCdnCSgpY48yZDqPdTZXWW", result.input.destination()?.address)
        assertEquals(null, result.input.memo())
        assertEquals(BigInteger.valueOf(3351936), result.chainData.fee().amount)
        assertEquals(BigInteger.valueOf(17458), result.chainData.gasGee().maxGasPrice)
        assertEquals(BigInteger.valueOf(192), result.chainData.gasGee().limit)
        assertEquals(AssetId(Chain.Doge).toIdentifier(), result.chainData.fee().feeAssetId.toIdentifier())
        assertEquals(TxSpeed.Normal, result.chainData.fee().speed)
        assertEquals("9f47e5d5ae3dac0662766f95d0cdda9c242c08d6baac4ce5d82464cf948abd53", (result.chainData as BitcoinSignerPreloader.BitcoinChainData).utxo[0].txid)
        assertEquals(1, (result.chainData as BitcoinSignerPreloader.BitcoinChainData).utxo[0].vout)
        assertEquals("86055170", (result.chainData as BitcoinSignerPreloader.BitcoinChainData).utxo[0].value)
        assertEquals(3, result.chainData.allFee().size)
        assertEquals(BigInteger.valueOf(192), result.chainData.gasGee(TxSpeed.Fast).limit)
        assertEquals(BigInteger.valueOf(60357), result.chainData.gasGee(TxSpeed.Fast).maxGasPrice)
        assertEquals(BigInteger.valueOf(11588544), result.chainData.gasGee(TxSpeed.Fast).amount)
        assertEquals(BigInteger.valueOf(192), result.chainData.gasGee(TxSpeed.Slow).limit)
        assertEquals(BigInteger.valueOf(17458), result.chainData.gasGee(TxSpeed.Slow).maxGasPrice)
        assertEquals(BigInteger.valueOf(3351936), result.chainData.gasGee(TxSpeed.Slow).amount)
    }
}