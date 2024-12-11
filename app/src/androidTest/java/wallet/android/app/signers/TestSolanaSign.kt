package wallet.android.app.signers

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gemwallet.android.blockchain.clients.solana.SolanaSignClient
import com.gemwallet.android.blockchain.clients.solana.SolanaSignerPreloader
import com.gemwallet.android.blockchain.operators.GetAsset
import com.gemwallet.android.ext.asset
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.DestinationAddress
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.SolanaTokenProgramId
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
        val signClient = SolanaSignClient(Chain.Solana, object : GetAsset {
            override suspend fun getAsset(assetId: AssetId): Asset? {
                return null
            }
        })
        val privateKey = HDWallet(testPhrase, "")
            .getKeyForCoin(CoinType.SOLANA)

        val sign = runBlocking {
            signClient.signTransfer(
                params = SignerParams(
                    input = ConfirmParams.TransferParams.Token(
                        assetId = Chain.Solana.asset().id,
                        amount = BigInteger.TEN.pow(Chain.Solana.asset().decimals),
                        from = Account(chain = Chain.Solana, address = "4Yu2e1Wz5T1Ci2hAPswDqvMgSnJ1Ftw7ZZh8x7xKLx7S", "", null),
                        destination = DestinationAddress("4Yu2e1Wz5T1Ci2hAPswDqvMgSnJ1Ftw7ZZh8x7xKLx7S"),
                    ),
                    finalAmount = BigInteger.TEN.pow(Chain.Solana.asset().decimals),
                    chainData = SolanaSignerPreloader.SolanaChainData(
                        blockhash = "DzfXchZJoLMG3cNftcf2sw7qatkkuwQf4xH15N5wkKAb",
                        senderTokenAddress = "",
                        recipientTokenAddress = null,
                        tokenProgram = SolanaTokenProgramId.Token,
                        fee = GasFee(
                            maxGasPrice = BigInteger.TEN,
                            limit = BigInteger("21000"),
                            minerFee = BigInteger.TEN,
                            relay = BigInteger.TEN,
                            speed = TxSpeed.Normal,
                            feeAssetId = Chain.Solana.asset().id,
                        )
                    )
                ),
                txSpeed = TxSpeed.Normal,
                privateKey.data(),
            )
        }
        Assert.assertEquals(
            "0x41634c77313244776a6937474d4565476a386c64457669664e555350394e445947744251476c36557150726362583569444e546f483330514b6f6a316756376755496e4e575031442f555a6571516d6a424b7a5161513842414149455365626b44466a2b415242396b4b486b394f4167745057456e614370426a475a3869707a61372f4e43577330767554324f647746546758414767305a6175317474703157354f7153437a77434c53384e5738357a71514d47526d2f6c495263792f2b7974756e4c446d2b65386a4f573778666353617978446d7a704141414141414141414141414141414141414141414141414141414141414141414141414141414141414141414141444245656d755657654d366a353761766a757378556b6637775a444c634d4e45742b47575a63384457736a414d4341416b44436741414141414141414143414155434346494141414d434141454d416741414141444b6d6a734141414141",
            sign.toHexString()
        )
    }
}