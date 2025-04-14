package com.gemwallet.android.blockchain.clients.sui

import com.gemwallet.android.blockchain.includeLibs
import com.gemwallet.android.blockchain.testPhrase
import com.gemwallet.android.ext.asset
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.DestinationAddress
import com.gemwallet.android.model.Fee
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FeePriority
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
            signer.signNativeTransfer(
                params = ConfirmParams.TransferParams.Native(
                    Chain.Sui.asset(),
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
                        priority = FeePriority.Normal,
                        feeAssetId = AssetId(Chain.Sui),
                        amount = BigInteger.TEN
                    ),
                ),
                finalAmount = BigInteger.valueOf(10_000),
                FeePriority.Normal,
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

    @Test
    fun testSuiTokenSign() {
        val hdWallet = HDWallet(testPhrase, "")
        val privateKey = hdWallet.getKeyForCoin(CoinType.SUI)
        val from = hdWallet.getAddressForCoin(CoinType.SUI)
        val signer = SuiSignClient(Chain.Sui)

        val sign = runBlocking {
            signer.signTokenTransfer(
                params = ConfirmParams.TransferParams.Token(
                    Asset(AssetId(Chain.Sui, "0xe4239cd951f6c53d9c41e25270d80d31f925ad1655e5ba5b543843d4a66975ee::SUIP::SUIP"), "", "", 8, AssetType.TOKEN),
                    Account(Chain.Sui, from, ""),
                    BigInteger.valueOf(10_000),
                    DestinationAddress(from),
                ),
                chainData = SuiSignerPreloader.SuiChainData(
                    messageBytes = "AAAEAQA+cu/kqxz/pp3Qmo6eoLJz+so76TaSloB1SmEUVhWCaUYoGhwAAAAAI" +
                            "BtZ+Y3WbB+PQtHrS7YgMDZGLzVxrT20trS/6hpbAEmBAQC01EdV46fEpnMdgod7BT2jJ" +
                            "F0uO3bB4vxKQwUM5D5LgUYoGhwAAAAAIG7IzS1+nt9AlH/Ky7M7uvu/hnOkXUjbo13FT" +
                            "zBuGsExAAgAypo7AAAAAAAgLuHnHoVlKe7YHnpDy6mnmWueg/fpbWoPf2pUAO0bwWgDA" +
                            "wEAAAEBAQACAQAAAQECAAEBAwEAAAABAwAu4ecehWUp7tgeekPLqaeZa56D9+ltag9/a" +
                            "lQA7RvBaAEtmx3oHIszqYLnHZYez3TM+HAslfPV7/PqUkB2HVFTNEYoGhwAAAAAIMLTz" +
                            "+S2gTgAQzNMurZ1aIQOw24OKmyO5LC3mt0mBR+cLuHnHoVlKe7YHnpDy6mnmWueg/fpb" +
                            "WoPf2pUAO0bwWjuAgAAAAAAAEB4fQEAAAAAAA==_0xd3877ebdd9f50a6d7d919d6e28" +
                            "a26dd62ec43db0986466a35fa78c84394d3046",
                    fee = Fee(
                        priority = FeePriority.Normal,
                        feeAssetId = AssetId(Chain.Sui),
                        amount = BigInteger.TEN
                    ),
                ),
                finalAmount = BigInteger.valueOf(10_000),
                FeePriority.Normal,
                privateKey.data(),
            )
        }

        assertEquals("0x414141454151412b63752f6b71787a2f707033516d6f36656f4c4a7a2b736f37365461536" +
                "c6f4231536d4555566857436155596f47687741414141414942745a2b59335762422b50517448725" +
                "33759674d445a474c7a5678725432307472532f3668706241456d424151433031456456343666457" +
                "06e4d64676f64374254326a4a4630754f3362423476784b5177554d3544354c6755596f476877414" +
                "1414141494737497a53312b6e7439416c482f4b79374d377576752f686e4f6b58556a626f3133465" +
                "47a4275477345784141674179706f3741414141414141674c75486e486f566c4b653759486e70447" +
                "9366d6e6d577565672f667062576f5066327055414f3062775767444177454141414542415141434" +
                "15141414151454341414542417745414141414241774175346563656857557037746765656b504c7" +
                "161655a61353644392b6c746167392f616c514137527642614145746d78336f4849737a71594c6e4" +
                "85a59657a33544d2b4841736c665056372f5071556b4232485646544e45596f47687741414141414" +
                "94d4c547a2b533267546741517a4e4d75725a316149514f7732344f4b6d794f354c43336d74306d4" +
                "2522b634c75486e486f566c4b653759486e704479366d6e6d577565672f667062576f50663270554" +
                "14f306277576a75416741414141414141454234665145414141414141413d3d5f4143616e5766545" +
                "263766530354c3030576f2b757a2f48586365656b622f6a6b303676386d4f41352f797872692f534" +
                "f466675653430354d573968307a61556c7131676f754e6634624d4f694c6a594e726c39315267716" +
                "64243524d424a44476f30546d57345431366b393252684d4d4a69305577782b6b57664e36386f743" +
                "770513d3d", sign.first().toHexString())
    }
}