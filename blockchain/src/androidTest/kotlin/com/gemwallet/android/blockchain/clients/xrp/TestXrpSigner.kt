package com.gemwallet.android.blockchain.clients.xrp

import com.gemwallet.android.blockchain.includeLibs
import com.gemwallet.android.blockchain.testPhrase
import com.gemwallet.android.ext.asset
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.DestinationAddress
import com.gemwallet.android.model.Fee
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FeePriority
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
                    Chain.Xrp.asset(),
                    Account(Chain.Xrp, from, ""),
                    BigInteger.valueOf(10_000),
                    DestinationAddress(from),
                ),
                chainData = XrpChainData(
                    sequence = 1,
                    blockNumber = 1,
                ),
                finalAmount = BigInteger.valueOf(10_000),
                fee = Fee(
                    priority = FeePriority.Normal,
                    feeAssetId = AssetId(Chain.Xrp),
                    amount = BigInteger.TEN,
                ),
                privateKey.data(),
            )
        }

        assertEquals(
            "0x12000022000000002400000001201b0000000d61400000000000271068400000000000000a73210209" +
                    "27cc15435c44dd3b74189e950d941939425f2864876e06747f093dc68b83e974473045022100" +
                    "d5b18dd75818a07d4b4ed5846b07604e48607789d393691334f59d3761f6874c02205ac35c8b" +
                    "cadb9fdbd81022f3629bfa0aac0b80afa655628c8ddbfee030f1bb178114dd10693e412bff78" +
                    "9ebf6baa9714036c9ae214bb8314dd10693e412bff789ebf6baa9714036c9ae214bb",
            String(sign.first())
        )
    }
}