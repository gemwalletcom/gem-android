package com.gemwallet.android.blockchain.clients.polkadot

import com.gemwallet.android.blockchain.clients.polkadot.models.PolkadotSigningData
import com.gemwallet.android.blockchain.includeLibs
import com.gemwallet.android.blockchain.testPhrase
import com.gemwallet.android.math.toHexString
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
                    AssetId(Chain.Polkadot),
                    Account(Chain.Polkadot, from, ""),
                    BigInteger.valueOf(10_000),
                    DestinationAddress(from),
                ),
                chainData = PolkadotSignerPreloaderClient.PolkadotChainData(
                    sequence = 0,
                    data = PolkadotSigningData(
                        genesisHash = "0x91b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c3",
                        blockHash = "0x6e3ffeaa3be9d19bd110e5b6e7cbbc92cceed0d2ec557276c296bf7970ace2e5",
                        blockNumber = BigInteger.valueOf(24666537),
                        specVersion = BigInteger.valueOf(1003004),
                        transactionVersion = BigInteger.valueOf(26),
                        period = 64,
                    ),
                    blockNumber = 24666537,
                    fee = Fee(
                        priority = FeePriority.Normal,
                        feeAssetId = AssetId(Chain.Polkadot),
                        amount = BigInteger.TEN
                    ),
                ),
                finalAmount = BigInteger.valueOf(10_000),
                FeePriority.Normal,
                privateKey.data(),
            )
        }

        assertEquals("0x35028400cd3cfbbaa8f217c2a29ceae4b4063b597b629861916bad98f9826e03d1ab120e0" +
                "0c094fad2608116257a997619a48d172340c69bca386c9f6b352903ee09a349b4ac90bff77872773" +
                "1dac78a56b6c5d901af843ca9c9a69b9e46cca042c4f70c099502000000050000cd3cfbbaa8f217c" +
                "2a29ceae4b4063b597b629861916bad98f9826e03d1ab120e419c", sign.first().toHexString()
        )
    }
}