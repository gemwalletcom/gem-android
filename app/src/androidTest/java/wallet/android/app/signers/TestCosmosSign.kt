package wallet.android.app.signers

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gemwallet.android.blockchain.clients.cosmos.CosmosSignClient
import com.gemwallet.android.blockchain.clients.cosmos.CosmosSignerPreloader
import com.gemwallet.android.ext.asset
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.DestinationAddress
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import wallet.android.app.testPhrase
import wallet.core.jni.CoinType
import wallet.core.jni.HDWallet
import java.math.BigInteger

@RunWith(AndroidJUnit4::class)
class TestCosmosSign {

    @Test
    fun testNativeSign() {
        val signClient = CosmosSignClient(Chain.Cosmos)
        val privateKey = HDWallet(testPhrase, "")
            .getKeyForCoin(CoinType.COSMOS)

        val sign = runBlocking {
            signClient.signTransfer(
                params = SignerParams(
                    input = ConfirmParams.TransferParams.Native(
                        assetId = Chain.Cosmos.asset().id,
                        amount = BigInteger.TEN.pow(com.wallet.core.primitives.Chain.Cosmos.asset().decimals),
                        destination = DestinationAddress("cosmos1kglemumu8mn658j6g4z9jzn3zef2qdyyydv7tr"),
                        from = Account(chain = Chain.Cosmos, address = "cosmos1kglemumu8mn658j6g4z9jzn3zef2qdyyydv7tr", "", "")
                    ),
                    finalAmount = BigInteger.TEN.pow(com.wallet.core.primitives.Chain.Cosmos.asset().decimals),
                    chainData = CosmosSignerPreloader.CosmosChainData(
                        chainId = "",
                        accountNumber = 1L,
                        sequence = 1L,
                        fee = GasFee(
                            maxGasPrice = BigInteger.TEN,
                            limit = BigInteger("21000"),
                            minerFee = BigInteger.TEN,
                            relay = BigInteger.TEN,
                            speed = TxSpeed.Normal,
                            feeAssetId = com.wallet.core.primitives.Chain.Cosmos.asset().id,
                        )
                    )
                ),
                txSpeed = TxSpeed.Normal,
                privateKey.data(),
            )
        }
        Assert.assertEquals(
            "0x7b226d6f6465223a2242524f4144434153545f4d4f44455f53594e43222c2274785f6279746573223a2243704d4243704142436877765932397a6257397a4c6d4a68626d7375646a46695a5852684d53354e633264545a57356b456e414b4c574e7663323176637a46725a32786c6258567464546874626a59314f476f325a7a52364f577036626a4e365a5759796357523565586c6b646a6430636849745932397a6257397a4d57746e62475674645731314f4731754e6a5534616a5a6e4e486f35616e70754d33706c5a6a4a785a486c35655752324e3352794768414b4258566864473974456763784d4441774d444177456d6b4b55417047436838765932397a6257397a4c6d4e79655842306279357a5a574e774d6a5532617a4575554856695332563545694d4b49514d736c63596e374468506535622f386c4d33466e50586847426a3553644331352b584931685a316759624242494543674949415267424568554b44776f466457463062323053426a49784d4441784d4243497041456151483345334861534f68354c726a796b5043384d6d3854332f35324b5676496562493335635a6c4231626a664e344a414b46314977366c53472f37755672704b2f383462307a364c714b557a2f377241486735614b2b633d227d",
            sign.toHexString()
        )
    }
}