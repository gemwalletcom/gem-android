package wallet.android.app.signers

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gemwallet.android.blockchain.clients.aptos.AptosSignClient
import com.gemwallet.android.blockchain.clients.aptos.AptosSignerPreloader
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
class TestAptosSign {

    @Test
    fun testNativeSign() {
        val signClient = AptosSignClient(Chain.Aptos)
        val privateKey = HDWallet(testPhrase, "")
            .getKeyForCoin(CoinType.APTOS)

        val sign = runBlocking {
            signClient.signTransfer(
                params = SignerParams(
                    input = ConfirmParams.TransferParams.Native(
                        assetId = Chain.Aptos.asset().id,
                        amount = BigInteger.TEN.pow(Chain.Aptos.asset().decimals),
                        destination = DestinationAddress("0x82111f2975a0f6080d178236369b7479f6aed1203ef4a23f8205e4b91716b783"),
                        from = Account(chain = Chain.Aptos, "0x9b1DB81180c31B1b428572Be105E209b5A6222b7", "", ""),
                    ),
                    finalAmount = BigInteger.TEN.pow(Chain.Aptos.asset().decimals),
                    chainData = AptosSignerPreloader.AptosChainData(
                        sequence = 1L,
                        fee = GasFee(
                            speed = TxSpeed.Normal,
                            maxGasPrice = BigInteger.TEN,
                            limit = BigInteger("21000"),
                            minerFee = BigInteger.TEN,
                            relay = BigInteger.TEN,
                            feeAssetId = Chain.Aptos.asset().id,
                        )
                    )
                ),
                txSpeed = TxSpeed.Normal,
                privateKey.data(),
            )
        }
        Assert.assertEquals(
            "0x7b2265787069726174696f6e5f74696d657374616d705f73656373223a2233363634333930303832222c226761735f756e69745f7072696365223a223130222c226d61785f6761735f616d6f756e74223a223231303030222c227061796c6f6164223a7b22617267756d656e7473223a5b22307838323131316632393735613066363038306431373832333633363962373437396636616564313230336566346132336638323035653462393137313662373833222c22313030303030303030225d2c2266756e6374696f6e223a223078313a3a6170746f735f6163636f756e743a3a7472616e73666572222c2274797065223a22656e7472795f66756e6374696f6e5f7061796c6f6164222c22747970655f617267756d656e7473223a5b5d7d2c2273656e646572223a22307839623164623831313830633331623162343238353732626531303565323039623561363232326237222c2273657175656e63655f6e756d626572223a2231222c227369676e6174757265223a7b227075626c69635f6b6579223a22307863316334336435616464666531633233376164616436393732643566333866376239363135366163666336663765666438666234643533303765306365383861222c227369676e6174757265223a2230786637343939653239656430303739313161616166356130636335386333613963333365663333333666353337646663343333636264393030306630633030336634653964666339396231336531663235656133396130376466303164393763656639636334353930313364323163626165626563303032633938343537653030222c2274797065223a22656432353531395f7369676e6174757265227d7d",
            sign.toHexString()
        )
    }
}