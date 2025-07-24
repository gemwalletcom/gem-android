package wallet.android.app.import_wallet

import com.gemwallet.android.data.repositoreis.wallets.PhraseAddressImportWalletService
import com.wallet.core.primitives.Chain
import org.junit.Assert.assertEquals
import org.junit.Test
import wallet.core.jni.Base58
import wallet.core.jni.CoinType
import wallet.core.jni.PrivateKey

class TestDecodePrivateKey {

    private val testBase58Key = "4ha2npeRkDXipjgGJ3L5LhZ9TK9dRjP2yktydkFBhAzXj3N8ytpYyTS24kxcYGEefy4WKWRcog2zSPvpPZoGmxCC"

    companion object {
        init {
            System.loadLibrary("TrustWalletCore")
            System.loadLibrary("gemstone")
        }
    }

    @Test
    fun testBase58Decode() {
        val base58 = "DTJi5pMtSKZHdkLX4wxwvjGjf2xwXx1LSuuUZhugYWDV"
        val key = PhraseAddressImportWalletService.decodePrivateKey(chain = Chain.Solana, testBase58Key)
        val address = CoinType.SOLANA.deriveAddress(PrivateKey(key))
        assertEquals("JSTURBrew3zGaJjtk7qcvd7gapeExX3GC7DiQBaCKzU", address)
        assertEquals(base58, Base58.encodeNoCheck(key))

        val hex = "0x30df0ffc2b43717f4653c2a1e827e9dfb3d9364e019cc60092496cd4997d5d6e"
        val key2 = PhraseAddressImportWalletService.decodePrivateKey(chain = Chain.Ethereum, hex)
        val address2 = CoinType.ETHEREUM.deriveAddress(PrivateKey(key2))
        assertEquals("0x4ce31c0b2114abe61Ac123E1E6254E961C18D10B", address2)
    }

    @Test
    fun testImportStellarKey() {
        val base32Key = "SA6XNHUKMW4QAKSHB2NOZ4SYP34ERYVAWSBTEDREYSJ2LEJ5LFHLTIRJ"
        val key = PhraseAddressImportWalletService.decodePrivateKey(Chain.Stellar, base32Key)
        val address = CoinType.STELLAR.deriveAddress(PrivateKey(key))
        assertEquals("GADB4BDKTOE36L6QN2JLIPNNJ7EZPSY5BIVKWXLWYZLIPXNQWIRQQZKT", address)
    }
}