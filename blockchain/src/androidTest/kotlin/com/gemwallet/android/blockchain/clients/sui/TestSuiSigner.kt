package com.gemwallet.android.blockchain.clients.sui

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

class TestSuiSigner {
    companion object {
        init {
            includeLibs()
        }
    }

    @Test
    fun testSuiNativeSign() {
        val hdWallet = HDWallet(testPhrase, "")
        val privateKey = hdWallet.getKeyForCoin(CoinType.SUI)
        val from = hdWallet.getAddressForCoin(CoinType.SUI)
        val signer = SuiSignClient(Chain.Sui)

        val sign = runBlocking {
            signer.sign(
                params = ConfirmParams.TransferParams.Native(
                    AssetId(Chain.Sui),
                    Account(Chain.Sui, from, ""),
                    BigInteger.valueOf(10_000),
                    DestinationAddress(from),
                ),
                chainData = SuiSignerPreloader.SuiChainData(
                    messageBytes = "AAACAAgAypo7AAAAAAAgLuHnHoVlKe7YHnpDy6mnmWueg/fpbWoPf2pUAO0b" +
                            "wWgCAgABAQAAAQEDAAAAAAEBAC7h5x6FZSnu2B56Q8upp5lrnoP36W1qD39qVADtG8F" +
                            "oAS2bHegcizOpgucdlh7PdMz4cCyV89Xv8+pSQHYdUVM07UIbGgAAAAAgbiAG3TMqRi" +
                            "5eRLenwRl5FY6EUk6/RG4a4cjnsc8KNU0u4ecehWUp7tgeekPLqaeZa56D9+ltag9/a" +
                            "lQA7RvBaO4CAAAAAAAAQHh9AQAAAAAA_0xc6af5cd37f2bab89411aff2a642522f88" +
                            "6e3a8bdfea1d0549729e99f3a241bd5",
                    fee = Fee(
                        speed = TxSpeed.Normal,
                        feeAssetId = AssetId(Chain.Sui),
                        amount = BigInteger.TEN
                    ),
                ),
                finalAmount = BigInteger.valueOf(10_000),
                TxSpeed.Normal,
                privateKey.data(),
            )
        }

        assertEquals("0x414141434141674179706f3741414141414141674c75486e486f566c4b653759486e70447" +
                "9366d6e6d577565672f667062576f5066327055414f3062775767434167414241514141415145444" +
                "14141414141454241433768357836465a536e75324235365138757070356c726e6f5033365731714" +
                "4333971564144744738466f4153326248656763697a4f70677563646c683750644d7a34634379563" +
                "8395876382b70535148596455564d303755496247674141414141676269414733544d71526935655" +
                "24c656e77526c3546593645556b362f5247346134636a6e7363384b4e55307534656365685755703" +
                "7746765656b504c7161655a61353644392b6c746167392f616c514137527642614f3443414141414" +
                "14141415148683941514141414141415f4149696a4d44515a786859764c4564762f5439317349553" +
                "2653544545471546954522b5835314b48354531626372594b376131544d652b746178336c792b657" +
                "5716257363048734c4657623944672f767732542f475153664243524d424a44476f30546d5734543" +
                "1366b393252684d4d4a69305577782b6b57664e36386f743770513d3d", sign.first().toHexString())
    }
}