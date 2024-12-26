package com.gemwallet.android.blockchain.clients.aptos

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gemwallet.android.blockchain.includeLibs
import com.gemwallet.android.blockchain.testPhrase
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.DestinationAddress
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
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
class TestAptosSigner {

    companion object {
        init {
            includeLibs()
        }
    }

    @Test
    fun testAptosNativeSign() {
        val privateKey = HDWallet(testPhrase, "").getKeyForCoin(CoinType.APTOS)
        val signer = AptosSignClient(Chain.Aptos)

        val sign = runBlocking {
            signer.signTransfer(
                SignerParams(
                    input = ConfirmParams.TransferParams.Native(
                        AssetId(Chain.Aptos),
                        Account(Chain.Aptos, "0x9b1DB81180c31B1b428572Be105E209b5A6222b7", ""),
                        BigInteger.valueOf(10_000_000_000),
                        DestinationAddress("0x82111f2975a0f6080d178236369b7479f6aed1203ef4a23f8205e4b91716b783"),
                    ),
                    chainData = AptosSignerPreloader.AptosChainData(
                        8L,
                        GasFee(
                            AssetId(Chain.Aptos),
                            speed = TxSpeed.Normal,
                            maxGasPrice = BigInteger.valueOf(150L),
                            limit = BigInteger.valueOf(18L)
                        )
                    ),
                    finalAmount = BigInteger.valueOf(10_000_000_000)
                ),
                TxSpeed.Normal,
                privateKey.data()
            )
        }

        assertEquals("0x7b2265787069726174696f6e5f74696d657374616d705f73656373223a22333636343339" +
                "30303832222c226761735f756e69745f7072696365223a22313530222c226d61785f6761735f616" +
                "d6f756e74223a223138222c227061796c6f6164223a7b22617267756d656e7473223a5b22307838" +
                "3231313166323937356130663630383064313738323336333639623734373966366165643132303" +
                "36566346132336638323035653462393137313662373833222c223130303030303030303030225d" +
                "2c2266756e6374696f6e223a223078313a3a6170746f735f6163636f756e743a3a7472616e73666" +
                "572222c2274797065223a22656e7472795f66756e6374696f6e5f7061796c6f6164222c22747970" +
                "655f617267756d656e7473223a5b5d7d2c2273656e646572223a223078396231646238313138306" +
                "33331623162343238353732626531303565323039623561363232326237222c2273657175656e63" +
                "655f6e756d626572223a2238222c227369676e6174757265223a7b227075626c69635f6b6579223" +
                "a223078633163343364356164646665316332333761646164363937326435663338663762393631" +
                "35366163666336663765666438666234643533303765306365383861222c227369676e617475726" +
                "5223a22307866306366643937363039626365663963666164623930626438656636356531343264" +
                "3862393039666537306137383539393434323330656234306566353061623236306130623931663" +
                "9356336323263646365376366373138353233653565656235323664336661356438393635646431" +
                "333966333637303930653562303031222c2274797065223a22656432353531395f7369676e61747" +
                "57265227d7d", sign.toHexString())
    }

    @Test
    fun testAptos_token_sign() {
        val privateKey = HDWallet(testPhrase, "").getKeyForCoin(CoinType.APTOS)
        val signer = AptosSignClient(Chain.Aptos)

        val sign = runBlocking {
            signer.signTransfer(
                SignerParams(
                    input = ConfirmParams.TransferParams.Token(
                        AssetId(Chain.Aptos, "0x53a30a6e5936c0a4c5140daed34de39d17ca7fcae08f947c02e979cef98a3719::coin::LSD"),
                        Account(Chain.Aptos, "0x9b1DB81180c31B1b428572Be105E209b5A6222b7", ""),
                        BigInteger.valueOf(10_000_000_000),
                        DestinationAddress("0x82111f2975a0f6080d178236369b7479f6aed1203ef4a23f8205e4b91716b783"),
                    ),
                    chainData = AptosSignerPreloader.AptosChainData(
                        8L,
                        GasFee(
                            AssetId(Chain.Aptos),
                            speed = TxSpeed.Normal,
                            maxGasPrice = BigInteger.valueOf(150L),
                            limit = BigInteger.valueOf(18L)
                        )
                    ),
                    finalAmount = BigInteger.valueOf(10_000_000_000)
                ),
                TxSpeed.Normal,
                privateKey.data()
            )
        }

        assertEquals("0x7b2265787069726174696f6e5f74696d657374616d705f73656373223a22333636343339" +
                "30303832222c226761735f756e69745f7072696365223a22313530222c226d61785f6761735f616" +
                "d6f756e74223a223138222c227061796c6f6164223a7b22617267756d656e7473223a5b22307838" +
                "3231313166323937356130663630383064313738323336333639623734373966366165643132303" +
                "36566346132336638323035653462393137313662373833222c223130303030303030303030225d" +
                "2c2266756e6374696f6e223a223078313a3a6170746f735f6163636f756e743a3a7472616e73666" +
                "5725f636f696e73222c2274797065223a22656e7472795f66756e6374696f6e5f7061796c6f6164" +
                "222c22747970655f617267756d656e7473223a5b223078353361333061366535393336633061346" +
                "3353134306461656433346465333964313763613766636165303866393437633032653937396365" +
                "66393861333731393a3a636f696e3a3a4c5344225d7d2c2273656e646572223a223078396231646" +
                "23831313830633331623162343238353732626531303565323039623561363232326237222c2273" +
                "657175656e63655f6e756d626572223a2238222c227369676e6174757265223a7b227075626c696" +
                "35f6b6579223a223078633163343364356164646665316332333761646164363937326435663338" +
                "66376239363135366163666336663765666438666234643533303765306365383861222c2273696" +
                "76e6174757265223a22307836613530643739633131626566363138356263363836613936666539" +
                "6565643537613263353832613131363834303331313065383030346636623436666236343039663" +
                "0393233386339663164363330643936623965663731653532663362643037326466396432353131" +
                "343430313461356366666437643137643164303062222c2274797065223a22656432353531395f7" +
                "369676e6174757265227d7d", sign.toHexString())
    }
}