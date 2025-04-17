package com.gemwallet.android.blockchain.clients.Stellar

import com.gemwallet.android.blockchain.clients.stellar.StellarSignClient
import com.gemwallet.android.blockchain.clients.stellar.StellarSignPreloadClient
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

class TestStellarSigner {
    companion object {
        init {
            includeLibs()
        }
    }

    @Test
    fun testStellarNativeSign() {
        val hdWallet = HDWallet(testPhrase, "")
        val privateKey = hdWallet.getKeyForCoin(CoinType.STELLAR)
        val from = hdWallet.getAddressForCoin(CoinType.STELLAR)
        val signer = StellarSignClient(Chain.Stellar)

        val sign = runBlocking {
            signer.signNativeTransfer(
                params = ConfirmParams.TransferParams.Native(
                    Chain.Stellar.asset(),
                    Account(Chain.Stellar, from, ""),
                    BigInteger.valueOf(10_000),
                    DestinationAddress(from),
                ),
                chainData = StellarSignPreloadClient.StellarChainData(
                    sequence = 1,
                    fees = listOf(
                        Fee(
                            priority = FeePriority.Normal,
                            feeAssetId = AssetId(Chain.Stellar),
                            amount = BigInteger.TEN
                        ),
                    ),
                ),
                finalAmount = BigInteger.valueOf(10_000),
                FeePriority.Normal,
                privateKey.data(),
            )
        }

        assertEquals("0x41414141414845774d46575a4d462b466b504d56665170332f702b71704e694f4f6851694" +
                "4535466476756364774496f414141414367414141414141414141424141414141414141414141414" +
                "14141424141414141414141414145414141414163544177565a6b775834575138785639436e662b6" +
                "e36716b324934364643494e4a4e386142586f61306967414141414141414141414141414a7841414" +
                "14141414141414141586f6130696741414142414b366e522b67544956464c7347454831776f42496" +
                "252514f4f56334863745975742f4b7972316e4c62424d34527952674370327576664f7954354c326" +
                "94d45507554797861726533515a446b544d62734756417142413d3d", sign.first().toHexString())
    }
}