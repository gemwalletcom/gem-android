package wallet.android.app.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.gemwallet.android.data.asset.AssetsRoomSource
import com.gemwallet.android.data.database.GemDatabase
import com.gemwallet.android.data.database.entities.DbAsset
import com.gemwallet.android.data.database.entities.DbBalance
import com.gemwallet.android.data.database.entities.DbPrice
import com.gemwallet.android.data.database.entities.DbSession
import com.gemwallet.android.data.wallet.AccountRoom
import com.gemwallet.android.data.wallet.WalletRoom
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.BalanceType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.WalletType
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestGetAssetInfo {

    private lateinit var source: AssetsRoomSource
    private lateinit var db: GemDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, GemDatabase::class.java).build()
        source = AssetsRoomSource(db.assetsDao(), db.balancesDao(), db.pricesDao())
        createData()
    }

    @Test
    fun testGetAssetInfo() {
        val assetInfo = runBlocking { source.getAssetInfo(AssetId(Chain.Ethereum)).firstOrNull() }
        assertEquals(Chain.Ethereum, assetInfo?.owner?.chain)
        assertEquals("some-address-1", assetInfo?.owner?.address)
        assertEquals("Ethereum-1", assetInfo?.asset?.name)
        assertEquals("test-wallet-1", assetInfo?.walletName)
        assertEquals(WalletType.multicoin, assetInfo?.walletType)
        assertEquals(1.0, assetInfo?.price?.price?.price)
        assertEquals(10.0, assetInfo?.price?.price?.priceChangePercentage24h)
        assertEquals(Currency.AUD, assetInfo?.price?.currency)
        assertEquals(BalanceType.available, assetInfo?.balances?.items?.firstOrNull()?.balance?.type)
        assertEquals("10000", assetInfo?.balances?.items?.firstOrNull()?.balance?.value)
    }

    @Test
    fun testGetAssetInfoThanChangeSession() {
        val assetInfo = runBlocking { source.getAssetInfo(AssetId(Chain.Ethereum)).firstOrNull() }
        assertEquals(Chain.Ethereum, assetInfo?.owner?.chain)
        assertEquals("some-address-1", assetInfo?.owner?.address)
        assertEquals("Ethereum-1", assetInfo?.asset?.name)
        assertEquals("test-wallet-1", assetInfo?.walletName)
        assertEquals(WalletType.multicoin, assetInfo?.walletType)
        assertEquals(1.0, assetInfo?.price?.price?.price)
        assertEquals(10.0, assetInfo?.price?.price?.priceChangePercentage24h)
        assertEquals(Currency.AUD, assetInfo?.price?.currency)
        assertEquals("10000", assetInfo?.balances?.available()?.atomicValue?.toString())
        assertEquals("7000", assetInfo?.balances?.rewards()?.atomicValue?.toString())

        runBlocking {
            db.sessionDao().update(
                DbSession(
                    walletId = "test-wallet-2",
                    currency = Currency.AED.string
                )
            )
        }

        val assetInfo1 = runBlocking { source.getAssetInfo(AssetId(Chain.Ethereum)).firstOrNull() }
        assertEquals(Chain.Ethereum, assetInfo1?.owner?.chain)
        assertEquals("some-address-2", assetInfo1?.owner?.address)
        assertEquals("Ethereum-2", assetInfo1?.asset?.name)
        assertEquals("test-wallet-2", assetInfo1?.walletName)
        assertEquals(WalletType.single, assetInfo1?.walletType)
        assertEquals(1.0, assetInfo1?.price?.price?.price)
        assertEquals(10.0, assetInfo1?.price?.price?.priceChangePercentage24h)
        assertEquals(Currency.AED, assetInfo1?.price?.currency)
        assertEquals("12000", assetInfo1?.balances?.available()?.atomicValue?.toString())
        assertEquals("11000", assetInfo1?.balances?.rewards()?.atomicValue?.toString())
    }

    @Test
    fun testGetAssetInfoFlow() = runTest {
        source.getAssetInfo(AssetId(Chain.Ethereum)).test {
            db.sessionDao().update(
                DbSession(
                    walletId = "test-wallet-2",
                    currency = Currency.AED.string
                )
            )
            awaitItem()
            val item = awaitItem()
            assertEquals(Currency.AED, item?.price?.currency)
            assertEquals("some-address-2", item?.owner?.address)
            assertEquals("12000", item?.balances?.available()?.atomicValue?.toString())
            db.balancesDao().insert(
                DbBalance(
                    assetId = AssetId(Chain.Ethereum).toIdentifier(),
                    address =  "some-address-2",
                    type = BalanceType.available,
                    amount = "100000",
                    updatedAt = 0L,
                )
            )
            assertEquals("100000", awaitItem()?.balances?.available()?.atomicValue?.toString())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun testGetAssetsInfo() = runTest {
        val roomSource = AssetsRoomSource(db.assetsDao(), db.balancesDao(), db.pricesDao())
        val assets = roomSource.getAssetsInfo().firstOrNull()?.sortedBy { it.owner.address }
        assertEquals(4, assets?.size)
        assertEquals("Ethereum-1", assets?.firstOrNull()?.asset?.name)
        assertEquals("17000", assets?.firstOrNull()?.balances?.calcTotal()?.atomicValue?.toString())
    }

    @Test
    fun testSearch() = runTest {
        val roomSource = AssetsRoomSource(db.assetsDao(), db.balancesDao(), db.pricesDao())
        val assets = roomSource.search("bnb").firstOrNull()?.sortedBy { it.asset.name }
        assertEquals(2, assets?.size)
        assertEquals("BNB-4", assets?.firstOrNull()?.asset?.name)
        assertEquals("14000", assets?.firstOrNull()?.balances?.calcTotal()?.atomicValue?.toString())
    }

    @Test
    fun testGetByAccount() = runTest {
        val roomSource = AssetsRoomSource(db.assetsDao(), db.balancesDao(), db.pricesDao())
        val assets = roomSource.getAssetsInfo(
            listOf(
                Account(
                    chain = Chain.Ethereum,
                    address = "some-address-1",
                    derivationPath = "",
                ),
                Account(
                    chain = Chain.Ethereum,
                    address = "some-address-2",
                    derivationPath = "",
                ),
                Account(
                    chain = Chain.Bitcoin,
                    address = "some-address-3",
                    derivationPath = "",
                ),
                Account(
                    chain = Chain.SmartChain,
                    address = "some-address-4",
                    derivationPath = "",
                ),
            )
        ).sortedBy { it.owner.address }
        assertEquals(5, assets.size)
        assertEquals("Ethereum-1", assets.firstOrNull()?.asset?.name)
        assertEquals("17000", assets.firstOrNull()?.balances?.calcTotal()?.atomicValue?.toString())
        assertEquals(1.0, assets.first().price?.price?.price)
        assertEquals(Currency.AUD, assets.first().price?.currency)
        assertEquals("Ethereum-2", assets[1].asset.name)
        assertEquals("23000", assets[1].balances.calcTotal().atomicValue.toString())
        assertEquals("BNB-TOKEN-1", assets.last().asset.name)
        assertEquals("15000", assets.last().balances.calcTotal().atomicValue.toString())
    }

    private fun createData() {
        db.walletsDao().apply {
            insert(
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
            insert(
                WalletRoom(
                    id = "test-wallet-2",
                    name = "test-wallet-2",
                    domainName = null,
                    type = WalletType.single,
                    position = 0,
                    pinned = false,
                    index = 0,
                ),
            )
        }
        db.accountsDao().insert(
            listOf(
                AccountRoom(
                    walletId = "test-wallet-1",
                    derivationPath = "",
                    address = "some-address-1",
                    chain = Chain.Ethereum,
                    extendedPublicKey = "",
                ),
                AccountRoom(
                    walletId = "test-wallet-2",
                    derivationPath = "",
                    address = "some-address-2",
                    chain = Chain.Ethereum,
                    extendedPublicKey = "",
                ),
                AccountRoom(
                    walletId = "test-wallet-1",
                    derivationPath = "",
                    address = "some-address-3",
                    chain = Chain.Bitcoin,
                    extendedPublicKey = "",
                ),
                AccountRoom(
                    walletId = "test-wallet-1",
                    derivationPath = "",
                    address = "some-address-4",
                    chain = Chain.SmartChain,
                    extendedPublicKey = "",
                ),
            )
        )
        db.assetsDao().insert(
            listOf(
                DbAsset(
                    address = "some-address-1",
                    id = AssetId(Chain.Ethereum).toIdentifier(),
                    name = "Ethereum-1",
                    symbol = "Eth",
                    decimals = 18,
                    type = AssetType.NATIVE,
                    stakingApr = 12.2,
                ),
                DbAsset(
                    address = "some-address-3",
                    id = AssetId(Chain.Bitcoin).toIdentifier(),
                    name = "Bitcoin-3",
                    symbol = "Btc",
                    decimals = 18,
                    type = AssetType.NATIVE,
                    stakingApr = 0.0,
                ),
                DbAsset(
                    address = "some-address-4",
                    id = AssetId(Chain.SmartChain).toIdentifier(),
                    name = "BNB-4",
                    symbol = "BNB",
                    decimals = 18,
                    type = AssetType.NATIVE,
                    stakingApr = 0.0,
                ),
                DbAsset(
                    address = "some-address-4",
                    id = AssetId(Chain.SmartChain, "some-token-id-1").toIdentifier(),
                    name = "BNB-TOKEN-1",
                    symbol = "BNBT-`1",
                    decimals = 18,
                    type = AssetType.TOKEN,
                    stakingApr = 0.0,
                ),
                DbAsset(
                    address = "some-address-2",
                    id = AssetId(Chain.Ethereum).toIdentifier(),
                    name = "Ethereum-2",
                    symbol = "Eth",
                    decimals = 18,
                    type = AssetType.NATIVE,
                )
            )
        )
        db.pricesDao().insert(
            listOf(
                DbPrice(
                    assetId = AssetId(Chain.Ethereum).toIdentifier(),
                    value = 1.0,
                    dayChanged = 10.0
                ),
                DbPrice(
                    assetId = AssetId(Chain.Bitcoin).toIdentifier(),
                    value = 2.0,
                    dayChanged = 10.0
                ),
                DbPrice(
                    assetId = AssetId(Chain.SmartChain).toIdentifier(),
                    value = 3.0,
                    dayChanged = 10.0
                ),
            )
        )
        db.balancesDao().insert(
            listOf(
                DbBalance(
                    assetId = AssetId(Chain.Ethereum).toIdentifier(),
                    address =  "some-address-1",
                    type = BalanceType.available,
                    amount = "10000",
                    updatedAt = 0L,
                ),
                DbBalance(
                    assetId = AssetId(Chain.Ethereum).toIdentifier(),
                    address =  "some-address-1",
                    type = BalanceType.rewards,
                    amount = "7000",
                    updatedAt = 0L,
                ),
                DbBalance(
                    assetId = AssetId(Chain.Ethereum).toIdentifier(),
                    address =  "some-address-2",
                    type = BalanceType.available,
                    amount = "12000",
                    updatedAt = 0L,
                ),
                DbBalance(
                    assetId = AssetId(Chain.Ethereum).toIdentifier(),
                    address =  "some-address-2",
                    type = BalanceType.rewards,
                    amount = "11000",
                    updatedAt = 0L,
                ),
                DbBalance(
                    assetId = AssetId(Chain.Bitcoin).toIdentifier(),
                    address =  "some-address-3",
                    type = BalanceType.available,
                    amount = "13000",
                    updatedAt = 0L,
                ),
                DbBalance(
                    assetId = AssetId(Chain.SmartChain).toIdentifier(),
                    address =  "some-address-4",
                    type = BalanceType.available,
                    amount = "14000",
                    updatedAt = 0L,
                ),
                DbBalance(
                    assetId = AssetId(Chain.SmartChain, "some-token-id-1").toIdentifier(),
                    address =  "some-address-4",
                    type = BalanceType.available,
                    amount = "15000",
                    updatedAt = 0L,
                )
            )
        )
        runBlocking {
            db.sessionDao().update(
                DbSession(
                    walletId = "test-wallet-1",
                    currency = Currency.AUD.string
                )
            )
        }
    }
}