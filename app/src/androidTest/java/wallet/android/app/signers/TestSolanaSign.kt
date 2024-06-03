package wallet.android.app.signers

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gemwallet.android.blockchain.clients.solana.SolanaSignClient
import com.gemwallet.android.blockchain.clients.solana.SolanaSignerPreloader
import com.gemwallet.android.blockchain.operators.GetAsset
import com.gemwallet.android.ext.asset
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.SignerParams
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import wallet.android.app.testPhrase
import wallet.core.jni.CoinType
import wallet.core.jni.HDWallet
import java.math.BigInteger

@RunWith(AndroidJUnit4::class)
class TestSolanaSign {

    @Test
    fun testNativeSign() {

        // FIXME
        return

        val signClient = SolanaSignClient(object : GetAsset {
            override suspend fun getAsset(assetId: AssetId): Asset? {
                return null
            }
        })
        val privateKey = HDWallet(testPhrase, "")
            .getKeyForCoin(CoinType.SOLANA)

        val sign = runBlocking {
            signClient.signTransfer(
                params = SignerParams(
                    input = ConfirmParams.TransferParams(
                        assetId = com.wallet.core.primitives.Chain.Solana.asset().id,
                        amount = BigInteger.TEN.pow(com.wallet.core.primitives.Chain.Solana.asset().decimals),
                        to = "4Yu2e1Wz5T1Ci2hAPswDqvMgSnJ1Ftw7ZZh8x7xKLx7S",
                    ),
                    owner = "4Yu2e1Wz5T1Ci2hAPswDqvMgSnJ1Ftw7ZZh8x7xKLx7S",
                    finalAmount = BigInteger.TEN.pow(com.wallet.core.primitives.Chain.Solana.asset().decimals),
                    info = SolanaSignerPreloader.Info(
                        blockhash = "",
                        senderTokenAddress = "",
                        recipientTokenAddress = null,
                        fee = GasFee(
                            maxGasPrice = BigInteger.TEN,
                            limit = BigInteger("21000"),
                            minerFee = BigInteger.TEN,
                            relay = BigInteger.TEN,
                            feeAssetId = com.wallet.core.primitives.Chain.Solana.asset().id,
                        )
                    )
                ),
                privateKey.data(),
            )
        }
        Assert.assertEquals(
            "0x41513265386f632b323032796e7564666a6e73434c676a532b3334616175676c364665314655344d3936597a6a542f6246666465343637784e7869787863356f6e6570724268616e733062534d4644793243665541516342414145445365626b44466a2b415242396b4b486b394f4167745057456e614370426a475a3869707a61372f4e43577330767554324f647746546758414767305a6175317474703157354f7153437a77434c53384e5738357a7151414141414141414141414141414141414141414141414141414141414141414141414141414141414141415149434141454d416741414141444b6d6a734141414141",
            sign.toHexString()
        )
    }
}