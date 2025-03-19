package com.gemwallet.android.blockchain.clients.xrp

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
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test
import wallet.core.jni.CoinType
import wallet.core.jni.HDWallet
import java.math.BigInteger

class TestXrpSigner {
    companion object {
        init {
            includeLibs()
        }
    }

    @Test
    fun testXrpNativeSign() {
        val hdWallet = HDWallet(testPhrase, "")
        val privateKey = hdWallet.getKeyForCoin(CoinType.XRP)
        val from = hdWallet.getAddressForCoin(CoinType.XRP)
        val signer = XrpSignClient(Chain.Xrp)

        val sign = runBlocking {
            signer.signNativeTransfer(
                params = ConfirmParams.TransferParams.Native(
                    AssetId(Chain.Xrp),
                    Account(Chain.Xrp, from, ""),
                    BigInteger.valueOf(10_000),
                    DestinationAddress(from),
                ),
                chainData = XrpSignerPreloader.XrpChainData(
                    sequence = 1,
                    blockNumber = 1,
                    fee = Fee(
                        speed = TxSpeed.Normal,
                        feeAssetId = AssetId(Chain.Xrp),
                        amount = BigInteger.TEN,
                    ),
                ),
                finalAmount = BigInteger.valueOf(10_000),
                TxSpeed.Normal,
                privateKey.data(),
            )
        }

        assertEquals(
            "0x12000022000000002400000001201b0000000b61400000000000271068400000000000000" +
                "a7321020927cc15435c44dd3b74189e950d941939425f2864876e06747f093dc68b83e9744630440" +
                "2205db7de344e8aabb5887f1e612aa32faa16ada58c177ee25150758bb8772a320402201140778e8" +
                "b58fa7884c0fffe632cbca99f3a7396ed8ff57213232f18ef65fcb38114dd10693e412bff789ebf6" +
                "baa9714036c9ae214bb8314dd10693e412bff789ebf6baa9714036c9ae214bb",
            sign.first().toHexString()
        )
    }
}