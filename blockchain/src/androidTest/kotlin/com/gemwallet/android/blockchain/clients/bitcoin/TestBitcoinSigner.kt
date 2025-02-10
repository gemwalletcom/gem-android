package com.gemwallet.android.blockchain.clients.bitcoin

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gemwallet.android.blockchain.includeLibs
import com.gemwallet.android.blockchain.testPhrase
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.DestinationAddress
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.blockchain.bitcoin.models.BitcoinUTXO
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
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
        const val SIGN_RESULT = "0x010000000153bd8a94cf6424d8e54cacbad6082c249cdacdd0956f766206a" +
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
                    AssetId(Chain.Doge),
                    Account(Chain.Doge, "D8UBj4EfNfNWNCdnCSgpY48yZDqPdTZXWW", "", "dgub8rNuTi8ofZu1jVDKpBxW9VFo62kjjx3b6CcameEZnrNNHJ3sKCnWBxQSv6qAP6jrwZEpfT1ZdKsrcBFKGTMV8zgBtjZmvQt29VPnLzbHjjD"),
                    BigInteger.valueOf(10_000_000_000),
                    DestinationAddress("D8UBj4EfNfNWNCdnCSgpY48yZDqPdTZXWW"),
                ),
                chainData = BitcoinSignerPreloader.BitcoinChainData(
                    listOf(
                        BitcoinUTXO(
                            txid = "9f47e5d5ae3dac0662766f95d0cdda9c242c08d6baac4ce5d82464cf948abd53",
                            vout = 1,
                            value = "86055170",
                        )
                    ),
                    listOf(
                        GasFee(
                            AssetId(Chain.Doge),
                            speed = TxSpeed.Normal,
                            maxGasPrice = BigInteger.valueOf(150L),
                            limit = BigInteger.valueOf(18L)
                        )
                    )
                ),
                finalAmount = BigInteger.valueOf(10_000_000_000),
                TxSpeed.Normal,
                privateKey.data()
            )
        }

        assertEquals(SIGN_RESULT, sign.first().toHexString())
    }
}