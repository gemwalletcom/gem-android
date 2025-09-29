package com.gemwallet.android.blockchain.clients.tron

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
import com.wallet.core.primitives.DelegationValidator
import com.wallet.core.primitives.FeePriority
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test
import wallet.core.jni.CoinType
import wallet.core.jni.HDWallet
import java.math.BigInteger

class TestTronSigner {
    companion object {
        init {
            includeLibs()
        }
    }

    @Test
    fun testTronNativeSign() {
        val hdWallet = HDWallet(testPhrase, "")
        val privateKey = hdWallet.getKeyForCoin(CoinType.TRON)
        val from = hdWallet.getAddressForCoin(CoinType.TRON)
        val signer = TronSignClient(Chain.Tron)

        val sign = runBlocking {
            signer.signNativeTransfer(
                params = ConfirmParams.TransferParams.Native(
                    Chain.Tron.asset(),
                    Account(Chain.Tron, from, ""),
                    BigInteger.valueOf(10_000),
                    DestinationAddress(from),
                ),
                chainData = TronChainData(
                    blockNumber = 69501435UL,
                    blockVersion = 31UL,
                    txTrieRoot = "fe35cfe51299075978398390271d3f09bdc34e6f43fe8b76eb469314b32ceaff",
                    witnessAddress = "41456798cb4ab28109d8cc643cd7da7bd6069ceae9",
                    parentHash = "00000000042481fa52ef745a31aa66fc2acf8d156b6116bc5b3ede2aebbb894c",
                    blockTimestamp = 1739156280000UL,
                    votes = emptyMap(),
                ),
                finalAmount = BigInteger.valueOf(10_000),
                fee = Fee(
                    priority = FeePriority.Normal,
                    feeAssetId = AssetId(Chain.Tron),
                    amount = BigInteger.TEN
                ),
                privateKey.data(),
            )
        }

        assertEquals("0x7b227261775f64617461223a7b22636f6e7472616374223a5b7b22706172616d657465722" +
                "23a7b22747970655f75726c223a22747970652e676f6f676c65617069732e636f6d2f70726f746f6" +
                "36f6c2e5472616e73666572436f6e7472616374222c2276616c7565223a7b22616d6f756e74223a3" +
                "1303030302c226f776e65725f61646472657373223a2234313837623539656337626235383235303" +
                "5333366623832333566396133663865656339633162666538222c22746f5f61646472657373223a2" +
                "23431383762353965633762623538323530353333666238323335663961336638656563396331626" +
                "66538227d7d2c2274797065223a225472616e73666572436f6e7472616374227d5d2c22657870697" +
                "26174696f6e223a313733393139323238303030302c226665655f6c696d6974223a31302c2272656" +
                "65f626c6f636b5f6279746573223a2238316662222c227265665f626c6f636b5f68617368223a226" +
                "5333365396435303365623232653761222c2274696d657374616d70223a313733393135363238303" +
                "030307d2c227369676e6174757265223a5b223834616231646130663934633361303434323335376" +
                "13364363461616539376137393336633635626536303663316339373765616663623636643838633" +
                "56666373365663338623333666363303564646662316635383836316339633235383037363030646" +
                "238643837323137356161653533396431396430633037393663383031225d2c2274784944223a226" +
                "53433363432633930366435656362636535656561326436653337343261393730373163383539653" +
                "43438356535306630353166653965636337363066633933227d", sign.first().toHexString()
        )
    }

    @Test
    fun testTronTokenSign() {
        val hdWallet = HDWallet(testPhrase, "")
        val privateKey = hdWallet.getKeyForCoin(CoinType.TRON)
        val from = hdWallet.getAddressForCoin(CoinType.TRON)
        val signer = TronSignClient(Chain.Tron)

        val sign = runBlocking {
            signer.signTokenTransfer(
                params = ConfirmParams.TransferParams.Token(
                    Asset(AssetId(Chain.Tron, "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t"), "", "", 8, AssetType.TOKEN),
                    Account(Chain.Tron, from, ""),
                    BigInteger.valueOf(10_000),
                    DestinationAddress(from),
                ),
                chainData = TronChainData(
                    blockNumber = 69501435UL,
                    blockVersion = 31UL,
                    txTrieRoot = "fe35cfe51299075978398390271d3f09bdc34e6f43fe8b76eb469314b32ceaff",
                    witnessAddress = "41456798cb4ab28109d8cc643cd7da7bd6069ceae9",
                    parentHash = "00000000042481fa52ef745a31aa66fc2acf8d156b6116bc5b3ede2aebbb894c",
                    blockTimestamp = 1739156280000UL,
                    votes = emptyMap(),
                ),
                finalAmount = BigInteger.valueOf(10_000),
                fee = Fee(
                    priority = FeePriority.Normal,
                    feeAssetId = AssetId(Chain.Tron),
                    amount = BigInteger.TEN
                ),
                privateKey.data(),
            )
        }

        assertEquals("0x7b227261775f64617461223a7b22636f6e7472616374223a5b7b22706172616d657465722" +
                "23a7b22747970655f75726c223a22747970652e676f6f676c65617069732e636f6d2f70726f746f6" +
                "36f6c2e54726967676572536d617274436f6e7472616374222c2276616c7565223a7b22636f6e747" +
                "26163745f61646472657373223a22343161363134663830336236666437383039383661343263373" +
                "865633963376637376536646564313363222c2264617461223a22613930353963626230303030303" +
                "03030303030303030303030303030303034313837623539656337626235383235303533336662383" +
                "23335663961336638656563396331626665383030303030303030303030303030303030303030303" +
                "03030303030303030303030303030303030303030303030303030303030303030303030303030323" +
                "73130222c226f776e65725f61646472657373223a223431383762353965633762623538323530353" +
                "33366623832333566396133663865656339633162666538227d7d2c2274797065223a22547269676" +
                "76572536d617274436f6e7472616374227d5d2c2265787069726174696f6e223a313733393139323" +
                "238303030302c226665655f6c696d6974223a31302c227265665f626c6f636b5f6279746573223a2" +
                "238316662222c227265665f626c6f636b5f68617368223a226533336539643530336562323265376" +
                "1222c2274696d657374616d70223a313733393135363238303030307d2c227369676e61747572652" +
                "23a5b223031366238326662353533323861636331336330333866336335326631646432633866663" +
                "73939333661343262366432303262633434646264626332623866663032373866666463346366396" +
                "36436346566323431343666643563613233373930336262313732326565623630303233623333336" +
                "266313733333862336130363030225d2c2274784944223a226661323932636636303631626636656" +
                "26561643263343139393836643562323336353366306635303436323766623234613036653066343" +
                "03562346264626566227d", sign.first().toHexString()
        )
    }

    @Test
    fun testDelegateSign() {
        val hdWallet = HDWallet(testPhrase, "")
        val privateKey = hdWallet.getKeyForCoin(CoinType.TRON)
        val from = hdWallet.getAddressForCoin(CoinType.TRON)
        val signer = TronSignClient(Chain.Tron)

        val sign = runBlocking {
            signer.signDelegate(
                params = ConfirmParams.Stake.DelegateParams(
                    Chain.Tron.asset(),
                    Account(Chain.Tron, from, ""),
                    BigInteger.valueOf(10_000),
                    validator = DelegationValidator(
                        chain = Chain.Tron,
                        id = "TCEo1hMAdaJrQmvnGTCcGT2LqrGU4N7Jqf",
                        name = "",
                        isActive = true,
                        commision = 0.0,
                        apr = 0.9,
                    ),
                ),
                chainData = TronChainData(
                    blockNumber = 69501435UL,
                    blockVersion = 31UL,
                    txTrieRoot = "fe35cfe51299075978398390271d3f09bdc34e6f43fe8b76eb469314b32ceaff",
                    witnessAddress = "41456798cb4ab28109d8cc643cd7da7bd6069ceae9",
                    parentHash = "00000000042481fa52ef745a31aa66fc2acf8d156b6116bc5b3ede2aebbb894c",
                    blockTimestamp = 1739156280000UL,
                    votes = mapOf(
                        "TLyqzVGLV1srkB7dToTAEqgDSfPtXRJZYH" to 1UL,
                        "TCEo1hMAdaJrQmvnGTCcGT2LqrGU4N7Jqf" to 1UL,
                    ),
                ),
                finalAmount = BigInteger.valueOf(10_000),
                fee = Fee(
                    priority = FeePriority.Normal,
                    feeAssetId = AssetId(Chain.Tron),
                    amount = BigInteger.TEN
                ),
                privateKey.data(),
            )
        }

        assertEquals("0x7b227261775f64617461223a7b22636f6e7472616374223a5b7b22706172616d657465722" +
                "23a7b22747970655f75726c223a22747970652e676f6f676c65617069732e636f6d2f70726f746f6" +
                "36f6c2e467265657a6542616c616e63655632436f6e7472616374222c2276616c7565223a7b22667" +
                "26f7a656e5f62616c616e6365223a31303030302c226f776e65725f61646472657373223a2234313" +
                "83762353965633762623538323530353333666238323335663961336638656563396331626665382" +
                "22c227265736f75726365223a2242414e445749445448227d7d2c2274797065223a22467265657a6" +
                "542616c616e63655632436f6e7472616374227d5d2c2265787069726174696f6e223a31373339313" +
                "9323238303030302c226665655f6c696d6974223a31302c227265665f626c6f636b5f62797465732" +
                "23a2238316662222c227265665f626c6f636b5f68617368223a22653333653964353033656232326" +
                "53761222c2274696d657374616d70223a313733393135363238303030307d2c227369676e6174757" +
                "265223a5b22666434333865303236306335663636383636616536363639356439396539616637383" +
                "26462613033316434633265613638326535356635306262343539363764323434343236613130393" +
                "73639393233376232613639613430353063303762623538653035616163383134353035623935386" +
                "6636263343665613436303730613031225d2c2274784944223a22353366353066386539353665613" +
                "73631633766323538303237373935666633646630343537623336393262633735363564323338663" +
                "339313735666130366161227d", sign.first().toHexString()
        )
    }
}