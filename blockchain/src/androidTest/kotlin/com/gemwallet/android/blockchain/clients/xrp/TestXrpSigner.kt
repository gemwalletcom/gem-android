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
import kotlin.Int

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
                    bockNumber = 1,
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

        assertEquals("0x1200002200000000240000000161400000000000271068400000000000000a7321020927c" +
                "c15435c44dd3b74189e950d941939425f2864876e06747f093dc68b83e97446304402207f219106a" +
                "8220669fb310c43974629e83925779f990e516dca87987624ee88480220557d0efc012c22948cd28" +
                "25aa64010e99ebe4255571472982aaa50ab8452d1ed8114dd10693e412bff789ebf6baa9714036c9" +
                "ae214bb8314dd10693e412bff789ebf6baa9714036c9ae214bb", sign.first().toHexString())
    }
}