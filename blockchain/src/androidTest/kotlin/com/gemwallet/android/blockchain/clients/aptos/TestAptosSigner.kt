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

        assertEquals("0x7b2265787069726174696f6e5f74696d657374616d705f73656373223a2233363634333930303832222c226761735f756e69745f7072696365223a22313530222c226d61785f6761735f616d6f756e74223a223138222c227061796c6f6164223a7b22617267756d656e7473223a5b22307838323131316632393735613066363038306431373832333633363962373437396636616564313230336566346132336638323035653462393137313662373833222c223130303030303030303030225d2c2266756e6374696f6e223a223078313a3a6170746f735f6163636f756e743a3a7472616e73666572222c2274797065223a22656e7472795f66756e6374696f6e5f7061796c6f6164222c22747970655f617267756d656e7473223a5b5d7d2c2273656e646572223a22307839623164623831313830633331623162343238353732626531303565323039623561363232326237222c2273657175656e63655f6e756d626572223a2238222c227369676e6174757265223a7b227075626c69635f6b6579223a22307863316334336435616464666531633233376164616436393732643566333866376239363135366163666336663765666438666234643533303765306365383861222c227369676e6174757265223a2230786630636664393736303962636566396366616462393062643865663635653134326438623930396665373061373835393934343233306562343065663530616232363061306239316639356336323263646365376366373138353233653565656235323664336661356438393635646431333966333637303930653562303031222c2274797065223a22656432353531395f7369676e6174757265227d7d", sign.toHexString())
    }
}