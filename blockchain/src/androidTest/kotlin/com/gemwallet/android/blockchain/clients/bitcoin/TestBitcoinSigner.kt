package com.gemwallet.android.blockchain.clients.bitcoin

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gemwallet.android.blockchain.includeLibs
import com.gemwallet.android.blockchain.testPhrase
import com.gemwallet.android.ext.asset
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.DestinationAddress
import com.gemwallet.android.model.GasFee
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FeePriority
import com.wallet.core.primitives.UTXO
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import wallet.core.jni.CoinType
import wallet.core.jni.HDWallet
import java.math.BigInteger

@RunWith(AndroidJUnit4::class)
class TestBitcoinSigner {

    companion object {
        const val SIGN_RESULT = "010000000153bd8a94cf6424d8e54cacbad6082c249cdacdd0956f766206a" +
                "c3daed5e5479f010000006b483045022100ead2b7637532b167e66ddf76eafd032ada898c8719a" +
                "680de8183b71fb3086a3e022055a073be9148cb8b9dc71de04755dbf11f140fee167ed0d4b44d6" +
                "a54a829e75e012102fd6585adc0e86019abf00e83552d054cb5f4359ad4db8ca338099381f43e2" +
                "5a5000000000182a82005000000001976a91424849c1d94eb9e6e002dd75fdcbce0a9673daba78" +
                "8ac00000000"
        init {
            includeLibs()
        }
    }

    @Test
    fun testBitcoinNativeSign() {
        val privateKey = HDWallet(testPhrase, "").getKeyForCoin(CoinType.DOGECOIN)
        val signer = BitcoinSignClient(Chain.Doge)

        val sign = runBlocking {
            signer.signNativeTransfer(
                params = ConfirmParams.TransferParams.Native(
                    Chain.Doge.asset(),
                    Account(Chain.Doge, "D8UBj4EfNfNWNCdnCSgpY48yZDqPdTZXWW", "", "dgub8rNuTi8ofZu1jVDKpBxW9VFo62kjjx3b6CcameEZnrNNHJ3sKCnWBxQSv6qAP6jrwZEpfT1ZdKsrcBFKGTMV8zgBtjZmvQt29VPnLzbHjjD"),
                    BigInteger.valueOf(10_000_000_000),
                    DestinationAddress("D8UBj4EfNfNWNCdnCSgpY48yZDqPdTZXWW"),
                ),
                chainData = BitcoinChainData(
                    listOf(
                        UTXO(
                            transaction_id = "9f47e5d5ae3dac0662766f95d0cdda9c242c08d6baac4ce5d82464cf948abd53",
                            vout = 1,
                            value = "86055170",
                            address = ""
                        )
                    ),
                ),
                finalAmount = BigInteger.valueOf(10_000_000_000),
                fee = GasFee(
                    AssetId(Chain.Doge),
                    priority = FeePriority.Normal,
                    maxGasPrice = BigInteger.valueOf(150L),
                    limit = BigInteger.valueOf(18L)
                ),
                privateKey.data()
            )
        }

        assertEquals(SIGN_RESULT, String(sign.first()))
    }

    @Test
    fun testBitcoinSwapSign() {
        val privateKey = HDWallet(testPhrase, "").getKeyForCoin(CoinType.DOGECOIN)
        val signer = BitcoinSignClient(Chain.Doge)

        val params = ConfirmParams.TransferParams.Native(
            Chain.Doge.asset(),
            Account(Chain.Doge, "D8UBj4EfNfNWNCdnCSgpY48yZDqPdTZXWW", "", "dgub8rNuTi8ofZu1jVDKpBxW9VFo62kjjx3b6CcameEZnrNNHJ3sKCnWBxQSv6qAP6jrwZEpfT1ZdKsrcBFKGTMV8zgBtjZmvQt29VPnLzbHjjD"),
            BigInteger.valueOf(10_000_000_000),
            DestinationAddress("D8UBj4EfNfNWNCdnCSgpY48yZDqPdTZXWW"),
            memo = "=:s:0xEe7E9CcFb529f2c1Cc02C0Aea8aCed7Ec7e98B5e:0/1/0:g1:50"
        )
        val chainData = BitcoinChainData(
            listOf(
                UTXO(
                    transaction_id = "9f47e5d5ae3dac0662766f95d0cdda9c242c08d6baac4ce5d82464cf948abd53",
                    vout = 1,
                    value = "86055170",
                    address = ""
                )
            ),
        )
        val finalAmount = BigInteger.valueOf(10_000_000_000)
        val fee = GasFee(
            AssetId(Chain.Doge),
            priority = FeePriority.Normal,
            maxGasPrice = BigInteger.valueOf(150L),
            limit = BigInteger.valueOf(18L)
        )
        val input = signer.getSigningInput(
            params = params,
            chainData = chainData,
            finalAmount = finalAmount,
            fee = fee,
            privateKey = privateKey.data(),
        )

        assertEquals(
            "0x3d3a733a3078456537453943634662353239663263314363303243304165613861436564374563376539384235653a302f312f303a67313a3530",
            input.outputOpReturn.toByteArray().toHexString()
        )
    }
}