package com.gemwallet.android.blockchain.clients.polkadot

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

class TestPolkadotSigner {
    companion object {
        init {
            includeLibs()
        }
    }

    @Test
    fun testPolkadotNativeSign() {
        val hdWallet = HDWallet(testPhrase, "")
        val privateKey = hdWallet.getKeyForCoin(CoinType.POLKADOT)
        val from = hdWallet.getAddressForCoin(CoinType.POLKADOT)
        val signer = PolkadotSignClient(Chain.Polkadot)

        val sign = runBlocking {
            signer.signNativeTransfer(
                params = ConfirmParams.TransferParams.Native(
                    Chain.Polkadot.asset(),
                    Account(Chain.Polkadot, from, ""),
                    BigInteger.valueOf(10_000),
                    DestinationAddress(from),
                ),
                chainData = PolkadotChainData(
                    sequence = 0UL,
                    genesisHash = "0x91b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c3",
                    blockHash = "0x6e3ffeaa3be9d19bd110e5b6e7cbbc92cceed0d2ec557276c296bf7970ace2e5",
                    blockNumber = 24666537UL,
                    specVersion = 1003004UL,
                    transactionVersion = 26UL,
                    period = 64,
                ),
                finalAmount = BigInteger.valueOf(10_000),
                fee = Fee(
                    priority = FeePriority.Normal,
                    feeAssetId = AssetId(Chain.Polkadot),
                    amount = BigInteger.TEN
                ),
                privateKey.data(),
            )
        }

        assertEquals(
            "0x39028400cd3cfbbaa8f217c2a29ceae4b4063b597b629861916bad98f9826e03d1ab120" +
                    "e00b2276e04c8adcd667512ec0440dd208f8ada56a4aec7572e4742ca2c0f8e5752d4d4f29d7" +
                    "2a17c5d7e6bbfe2dfc9f081e567fdb9111be12ca04dec40cd2be0079502000000000a0000cd3" +
                    "cfbbaa8f217c2a29ceae4b4063b597b629861916bad98f9826e03d1ab120e419c",
            String(sign.first())
        )
    }
}