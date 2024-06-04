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
                        blockhash = "DzfXchZJoLMG3cNftcf2sw7qatkkuwQf4xH15N5wkKAb",
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
            "0x416349776a555a6b6f65466f7569306d6a38506a6861672f32333372787346687570304b2b4b3862575145774c77704b7678736d6e2f3761754c635059652f6b7378446c332b63346970574b38334e6d6e6c4d4a77774942414145445365626b44466a2b415242396b4b486b394f4167745057456e614370426a475a3869707a61372f4e43577330767554324f647746546758414767305a6175317474703157354f7153437a77434c53384e5738357a715141414141414141414141414141414141414141414141414141414141414141414141414141414141414177524870726c566e6a4f6f2b6532723437724d564a482b38475179334444524c66686c6d5850413172497742416749414151774341414141414d71614f7741414141413d",
            sign.toHexString()
        )
    }
}