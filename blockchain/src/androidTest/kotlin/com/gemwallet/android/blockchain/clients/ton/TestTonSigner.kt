package com.gemwallet.android.blockchain.clients.ton

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

class TestTonSigner {
    companion object {
        init {
            includeLibs()
        }
    }

    @Test
    fun testTonNativeSign() {
        val hdWallet = HDWallet(testPhrase, "")
        val privateKey = hdWallet.getKeyForCoin(CoinType.TON)
        val from = hdWallet.getAddressForCoin(CoinType.TON)
        val signer = TonSignClient(Chain.Ton)

        val sign = runBlocking {
            signer.signNativeTransfer(
                params = ConfirmParams.TransferParams.Native(
                    AssetId(Chain.Ton),
                    Account(Chain.Ton, from, ""),
                    BigInteger.valueOf(10_000),
                    DestinationAddress(from),
                ),
                chainData = TonSignerPreloader.TonChainData(
                    sequence = 1,
                    fee = Fee(
                        speed = TxSpeed.Normal,
                        feeAssetId = AssetId(Chain.Ton),
                        amount = BigInteger.TEN
                    ),
                    expireAt = 1000000000,
                ),
                finalAmount = BigInteger.valueOf(10_000),
                TxSpeed.Normal,
                privateKey.data(),
            )
        }

        assertEquals("0x74653663636b45424241454172674142525967426b463177363763424c4730653044376a3" +
                "0793253687a666c4365324a726c416a53347043385548673835414d415147634f5a35572f6a6b437" +
                "14e536a39777250336973524e386b3250734a764153315263374b2b41426b2f5667737644344d536" +
                "c634546705335365347686b6d4337705359774a4d314f6364376949565543593144654641696d706" +
                "f7863376d736f4141414141415141444167466b5167426b463177363763424c4730653044376a307" +
                "93253687a666c4365324a726c416a533470433855486738354245346741414141414141414141414" +
                "141414141414544414142764e784b4a", sign.first().toHexString())
    }

    @Test
    fun testTonTokenSign() {
        val hdWallet = HDWallet(testPhrase, "")
        val privateKey = hdWallet.getKeyForCoin(CoinType.TON)
        val from = hdWallet.getAddressForCoin(CoinType.TON)
        val signer = TonSignClient(Chain.Ton)

        val sign = runBlocking {
            signer.signTokenTransfer(
                params = ConfirmParams.TransferParams.Token(
                    AssetId(Chain.Ton, "EQCxE6mUtQJKFnGfaROTKOt1lZbDiiX1kCixRv7Nw2Id_sDs"),
                    Account(Chain.Ton, from, ""),
                    BigInteger.valueOf(10_000),
                    DestinationAddress(from),
                ),
                chainData = TonSignerPreloader.TonChainData(
                    sequence = 1,
                    jettonAddress = "EQAlgB03OjJKdXrlwZiGJD5snSzPKF2VL5bErJn_cqJANGH9",
                    expireAt = 1000000000,
                    fee = Fee(
                        speed = TxSpeed.Normal,
                        feeAssetId = AssetId(Chain.Ton),
                        amount = BigInteger.TEN
                    ),
                ),
                finalAmount = BigInteger.valueOf(10_000),
                TxSpeed.Normal,
                privateKey.data(),
            )
        }

        assertEquals("0x74653663636b4542424145412f774142525967426b463177363763424c4730653044376a3" +
                "0793253687a666c4365324a726c416a53347043385548673835414d4151476362614f36626a524c6" +
                "b6265776255726a386359556f634a4937764a4465584834756f5a7174545a7a66354352564252773" +
                "8726a4d4b4d4e67344d456166547779776536776f322b42686566586b684f746445616b43796d706" +
                "f7863376d736f4141414141415141444167466759674153774136626e526b6c4f723179344d78444" +
                "568383254705a6e6c43374b6c387469566b7a2f75564567476741414141414141414141414141414" +
                "14141424177436d4434702b705141414141414141414141496e4549415a4264634f7533415378744" +
                "874412b34394d746b6f633335516e74696135514930754b5176464234504f524144494c726831323" +
                "443574e6f396f483348706c736c446d2f4b45397354584b42476c785346346f5042357941674b4c4" +
                "437344f", sign.first().toHexString())
    }
}