package com.gemwallet.android.blockchain.clients.near

import com.gemwallet.android.blockchain.clients.near.NearSignerPreloader.NearChainData
import com.gemwallet.android.blockchain.includeLibs
import com.gemwallet.android.blockchain.testPhrase
import com.gemwallet.android.ext.asset
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

class TestNearSigner {
    companion object {
        init {
            includeLibs()
        }
    }

    @Test
    fun testNearNativeSign() {
        val hdWallet = HDWallet(testPhrase, "")
        val privateKey = hdWallet.getKeyForCoin(CoinType.NEAR)
        val from = hdWallet.getAddressForCoin(CoinType.NEAR)
        val signer = NearSignClient(Chain.Near)

        val sign = runBlocking {
            signer.signNativeTransfer(
                params = ConfirmParams.TransferParams.Native(
                    Chain.Near.asset(),
                    Account(Chain.Near, from, ""),
                    BigInteger.valueOf(10_000),
                    DestinationAddress(from),
                ),
                chainData = NearChainData(
                    block = "2ADR7pgpkd2uFFkQcAyCxL5YB4d9SewALTLEuFbUUJLe",
                    sequence = 134180900000002,
                    fee = Fee(
                        priority = FeePriority.Normal,
                        feeAssetId = AssetId(Chain.Near),
                        amount = BigInteger.TEN
                    ),
                ),
                finalAmount = BigInteger.valueOf(10_000),
                FeePriority.Normal,
                privateKey.data(),
            )
        }

        assertEquals("0x514141414144517a5a5752684d475132597a6b774d6a4d304d446b354f44686c4e6a6b79" +
                "4f4749345a546b784e7a46685a57457a5a4751795a4441304e5749324e7a426a5a4467314f47513" +
                "44d6a4977596d46685a546379596d5141512b326731736b434e416d596a6d6b6f754f6b5847756f" +
                "3930744246746e444e68593243494c7175637230435565467343586f4141454141414141304d325" +
                "66b5954426b4e6d4d354d44497a4e4441354f5467345a5459354d6a68694f4755354d5463785957" +
                "56684d32526b4d6d51774e4456694e6a6377593251344e54686b4f4449794d474a68595755334d6" +
                "d4a6b4554667a37422b42314d4249344757695468372f697170475a587a7259324639326d6b3754" +
                "656749536e3842414141414178416e4141414141414141414141414141414141414141785855714" +
                "76e7459566e574b703550475347674d585461616a466e75326869536c4363475757654953655551" +
                "505355765247464933742b7a57565a4f37796e626d78456945586552434c46584d4262464d736b4" +
                "443513d3d", sign.first().toHexString())
    }
}