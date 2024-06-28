package wallet.android.app.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gemwallet.android.data.database.entities.DbAsset
import com.gemwallet.android.data.asset.AssetsDao
import com.gemwallet.android.data.database.GemDatabase
import com.gemwallet.android.data.database.entities.DbSession
import com.gemwallet.android.data.tokens.TokenRoom
import com.gemwallet.android.data.tokens.TokensDao
import com.gemwallet.android.data.wallet.AccountRoom
import com.gemwallet.android.data.wallet.WalletRoom
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.WalletType
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestEntityAsset {
    private lateinit var assetsDao: AssetsDao
    private lateinit var tokenDao: TokensDao
    private lateinit var db: GemDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, GemDatabase::class.java).build()
        assetsDao = db.assetsDao()
        tokenDao = db.tokensDao()
    }

    @Test
    fun testGetAssetInfo() {
        db.walletsDao().insert(
            WalletRoom(
                id = "test-wallet-1",
                name = "test-wallet-1",
                domainName = null,
                type = WalletType.multicoin,
                position = 0,
                pinned = false,
                index = 0,
            ),
        )
        db.walletsDao().insert(
            WalletRoom(
                id = "test-wallet-2",
                name = "test-wallet-2",
                domainName = null,
                type = WalletType.multicoin,
                position = 0,
                pinned = false,
                index = 0,
            ),
        )
        db.accountsDao().insert(
            AccountRoom(
                walletId = "test-wallet-1",
                derivationPath = "",
                address = "some-address-1",
                chain = Chain.Ethereum,
                extendedPublicKey = "",
            )
        )
        db.accountsDao().insert(
            AccountRoom(
                walletId = "test-wallet-2",
                derivationPath = "",
                address = "some-address-2",
                chain = Chain.Ethereum,
                extendedPublicKey = "",
            )
        )
        assetsDao.insert(
            DbAsset(
                address = "some-address-1",
                id = "ethereum",
                name = "Ethereum-1",
                symbol = "Eth",
                decimals = 18,
                type = AssetType.NATIVE,
            )
        )
        assetsDao.insert(
            DbAsset(
                address = "some-address-2",
                id = "ethereum",
                name = "Ethereum-2",
                symbol = "Eth",
                decimals = 18,
                type = AssetType.NATIVE,
            )
        )
        runBlocking {
            db.sessionDao().update(DbSession(
                walletId = "test-wallet-1",
                currency = Currency.USD.string
            ))
        }

        assertEquals("test-wallet-1", db.sessionDao().getSession()?.walletId)
        val assetId = AssetId(Chain.Ethereum)
        val asset = runBlocking {
            assetsDao.getAssetById(assetId.toIdentifier(), Chain.Ethereum)
                .firstOrNull()
        }?.firstOrNull()
        assertEquals(assetId.toIdentifier(), asset?.id)
        assertEquals("Ethereum-1", asset?.name)
        assertEquals(18, asset?.decimals)
        assertEquals("Eth", asset?.symbol)
        assertEquals(AssetType.NATIVE, asset?.type)
        assertEquals("some-address-1", asset?.address)
        assertEquals(Chain.Ethereum, asset?.chain)
        assertEquals("test-wallet-1", asset?.walletId)
        assertEquals("test-wallet-1", asset?.walletName)
        assertEquals(WalletType.multicoin, asset?.walletType)
    }


    @Test
    fun testAssembleTokenAssetInfo() {
        db.walletsDao().insert(
            WalletRoom(
                id = "test-wallet-1",
                name = "test-wallet-1",
                domainName = null,
                type = WalletType.multicoin,
                position = 0,
                pinned = false,
                index = 0,
            ),
        )
        db.walletsDao().insert(
            WalletRoom(
                id = "test-wallet-2",
                name = "test-wallet-2",
                domainName = null,
                type = WalletType.multicoin,
                position = 0,
                pinned = false,
                index = 0,
            ),
        )
        db.accountsDao().insert(
            AccountRoom(
                walletId = "test-wallet-1",
                derivationPath = "",
                address = "some-address-1",
                chain = Chain.Ethereum,
                extendedPublicKey = "",
            )
        )
        db.accountsDao().insert(
            AccountRoom(
                walletId = "test-wallet-2",
                derivationPath = "",
                address = "some-address-2",
                chain = Chain.Ethereum,
                extendedPublicKey = "",
            )
        )
        tokenDao.insert(
            TokenRoom(
                id = "ethereum",
                name = "Ethereum-1",
                symbol = "Eth",
                decimals = 18,
                type = AssetType.NATIVE,
                rank = 0,
            )
        )
        tokenDao.insert(
            TokenRoom(
                id = "ethereum_0xabcdef12345567890",
                name = "Ethereum-2",
                symbol = "Eth",
                decimals = 18,
                type = AssetType.NATIVE,
                rank = 0,
            )
        )
        runBlocking {
            db.sessionDao().update(DbSession(
                walletId = "test-wallet-1",
                currency = Currency.USD.string
            ))
        }

        assertEquals("test-wallet-1", db.sessionDao().getSession()?.walletId)
        val assetId = AssetId(Chain.Ethereum)
        val asset = runBlocking {
            tokenDao.assembleAssetInfo(Chain.Ethereum, assetId.toIdentifier())
        }.firstOrNull()
        assertEquals(assetId.toIdentifier(), asset?.id)
        assertEquals("Ethereum-1", asset?.name)
        assertEquals(18, asset?.decimals)
        assertEquals("Eth", asset?.symbol)
        assertEquals(AssetType.NATIVE, asset?.type)
        assertEquals("some-address-1", asset?.address)
        assertEquals(Chain.Ethereum, asset?.chain)
        assertEquals("test-wallet-1", asset?.walletId)
        assertEquals("test-wallet-1", asset?.walletName)
        assertEquals(WalletType.multicoin, asset?.walletType)
    }

}