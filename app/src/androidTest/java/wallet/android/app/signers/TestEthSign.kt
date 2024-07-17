package wallet.android.app.signers

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gemwallet.android.blockchain.clients.ethereum.EvmSignClient
import com.gemwallet.android.blockchain.clients.ethereum.EvmSignerPreloader
import com.gemwallet.android.ext.asset
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.DestinationAddress
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.SignerParams
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import wallet.android.app.testPhrase
import wallet.core.jni.CoinType
import wallet.core.jni.HDWallet
import java.math.BigInteger

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class TestEthSign {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.gemwallet.android", appContext.packageName)
    }

    @Test
    fun testEthSign() {
        val signClient = EvmSignClient(Chain.Ethereum)
        val privateKey = HDWallet(testPhrase, "")
            .getKeyForCoin(CoinType.ETHEREUM)

        val sign = runBlocking {
            signClient.signTransfer(
                params = SignerParams(
                    input = ConfirmParams.TransferParams(
                        assetId = com.wallet.core.primitives.Chain.Ethereum.asset().id,
                        amount = BigInteger.TEN.pow(com.wallet.core.primitives.Chain.Ethereum.asset().decimals),
                        destination = DestinationAddress("0x9b1DB81180c31B1b428572Be105E209b5A6222b7"),
                    ),
                    owner = "0x9b1DB81180c31B1b428572Be105E209b5A6222b7",
                    finalAmount = BigInteger.TEN.pow(com.wallet.core.primitives.Chain.Ethereum.asset().decimals),
                    info = EvmSignerPreloader.Info(
                        chainId = BigInteger.ONE,
                        nonce = BigInteger.ONE,
                        fee = GasFee(
                            maxGasPrice = BigInteger.TEN,
                            limit = BigInteger("21000"),
                            minerFee = BigInteger.TEN,
                            relay = BigInteger.TEN,
                            feeAssetId = com.wallet.core.primitives.Chain.Ethereum.asset().id,
                        )
                    )
                ),
                privateKey.data(),
            )
        }
        assertEquals(
            "0x02f86a01010a0a825208949b1db81180c31b1b428572be105e209b5a6222b7880de0b6b3a764000080c001a04936670cff2d450a1375fb2c42cf9f97130f9f9365197e4e8461a8c43fe24786a041833ce7835a78604c8518ef4dcef4e6dcd2e2d031dce759cd89790b6054fa07",
            sign.toHexString()
        )
    }

    @Test
    fun testEthTokenSign() {
        val signClient = EvmSignClient(Chain.Ethereum)
        val privateKey = HDWallet("seminar cruel gown pause law tortoise step stairs size amused pond weapon", "")
            .getKeyForCoin(CoinType.ETHEREUM)

        val sign = runBlocking {
            signClient.signTransfer(
                params = SignerParams(
                    input = ConfirmParams.TransferParams(
                        assetId = AssetId(Chain.Ethereum, "0xdAC17F958D2ee523a2206206994597C13D831ec7"),
                        amount = BigInteger.TEN.pow(com.wallet.core.primitives.Chain.Ethereum.asset().decimals),
                        destination = DestinationAddress("0x9b1DB81180c31B1b428572Be105E209b5A6222b7"),
                    ),
                    owner = "0x9b1DB81180c31B1b428572Be105E209b5A6222b7",
                    finalAmount = BigInteger.TEN.pow(com.wallet.core.primitives.Chain.Ethereum.asset().decimals),
                    info = EvmSignerPreloader.Info(
                        chainId = BigInteger.ONE,
                        nonce = BigInteger.ONE,
                        fee = GasFee(
                            maxGasPrice = BigInteger.TEN,
                            limit = BigInteger("91000"),
                            minerFee = BigInteger.TEN,
                            relay = BigInteger.TEN,
                            feeAssetId = com.wallet.core.primitives.Chain.Ethereum.asset().id,
                        )
                    )
                ),
                privateKey.data(),
            )
        }
        assertEquals(
            "0x02f8a801010a0a8301637894dac17f958d2ee523a2206206994597c13d831ec780b844a9059cbb0000000000000000000000009b1db81180c31b1b428572be105e209b5a6222b70000000000000000000000000000000000000000000000000de0b6b3a7640000c001a02a975d0be8ce97d4518cd22407a21697f0177b8ca7c057b868eaba32aefd6887a00b20f74083ac147b7733c0b72d2f87cc96fc75be610da14b4f6265703421d273",
            sign.toHexString()
        )
    }
}