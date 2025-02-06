package com.gemwallet.android.blockchain.clients.cardano

import com.gemwallet.android.blockchain.clients.cardano.CardanoSignerPreloaderClient.CardanoChainData
import com.gemwallet.android.blockchain.includeLibs
import com.gemwallet.android.blockchain.testPhrase
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.DestinationAddress
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.UTXO
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test
import wallet.core.jni.CoinType
import wallet.core.jni.HDWallet
import java.math.BigInteger

class TestCardanoSigner {
    companion object {
        init {
            includeLibs()
        }
    }

    @Test
    fun testCardanoNativeSign() {
        val privateKey = HDWallet(testPhrase, "").getKeyForCoin(CoinType.CARDANO)
        val signer = CardanoSignClient(Chain.Cardano)

        val sign = runBlocking {
            signer.sign(
                params = ConfirmParams.TransferParams.Native(
                    AssetId(Chain.Cardano),
                    Account(Chain.Cardano, "addr1q9d2dxen8ywvs9yzxxn2w4mvffn797fquauvugt2ug7mfsuqj3lzdq9h0rsketzszrnfm930658swmpe7kpq53c2tmwql4rvtq", ""),
                    BigInteger.valueOf(10_000),
                    DestinationAddress("addr1q9d2dxen8ywvs9yzxxn2w4mvffn797fquauvugt2ug7mfsuqj3lzdq9h0rsketzszrnfm930658swmpe7kpq53c2tmwql4rvtq"),
                ),
                chainData = CardanoChainData(
                    fee = Fee(
                        speed = TxSpeed.Normal,
                        feeAssetId = AssetId(Chain.Cardano),
                        amount = BigInteger.TEN
                    ),
                    utxos = listOf(
                        UTXO(
                            address = "addr1q9d2dxen8ywvs9yzxxn2w4mvffn797fquauvugt2ug7mfsuqj3lzdq9h0rsketzszrnfm930658swmpe7kpq53c2tmwql4rvtq",
                            transaction_id = "412c5a964cf4515210bf4b82f45df6521c38e1e5381f27638fc509bef6679378",
                            value = "7945975",
                            vout = 1,
                        )
                    )
                ),
                finalAmount = BigInteger.valueOf(10_000),
                TxSpeed.Normal,
                privateKey.data(),
            )
        }

        assertEquals("0x83a40081825820412c5a964cf4515210bf4b82f45df6521c38e1e5381f27638fc509bef66" +
                "79378010182825839015aa69b33391cc8148231a6a7576c4a67e2f920e778ce216ae23db4c380947" +
                "e2680b778e16cac5010e69d962fd50f076c39f5820a470a5edc192710825839015aa69b33391cc81" +
                "48231a6a7576c4a67e2f920e778ce216ae23db4c380947e2680b778e16cac5010e69d962fd50f076" +
                "c39f5820a470a5edc1a0076859c021a0002924b031a0b532b80a10081825820878150b7cb71f9406" +
                "e36dcdd250e22dc943ec4525233581536497acb4f13e670584004bf4935e4dcc7f54947c4eb0b9cd" +
                "d24880b168138af31b394ee0e7fe935900383822f432a1c2b533ea54f8ef12f1758b30a045f3918e" +
                "f25a51dad762d071b0af6", sign.first().toHexString())
    }
}