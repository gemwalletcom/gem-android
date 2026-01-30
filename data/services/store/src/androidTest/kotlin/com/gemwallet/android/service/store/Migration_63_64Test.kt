package com.gemwallet.android.service.store

import android.content.Context
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gemwallet.android.application.PasswordStore
import com.gemwallet.android.data.service.store.database.GemDatabase
import com.gemwallet.android.data.service.store.database.di.Migration_63_64
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class Migration_63_64Test {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        GemDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    private lateinit var passwordStore: TestPasswordStore
    private lateinit var context: Context
    private lateinit var keysDir: File

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        keysDir = context.dataDir
        passwordStore = TestPasswordStore()
    }

    @After
    fun tearDown() {
        keysDir.listFiles()?.forEach { file ->
            if (file.name.startsWith("wallet_") || file.name.startsWith("multicoin_") ||
                file.name.startsWith("single_") || file.name.startsWith("privatekey_") ||
                file.name.startsWith("view_")
            ) {
                file.delete()
            }
        }
    }

    @Test
    fun migrate63To64_multicoinWallet_generatesCorrectId() {
        val oldWalletId = "wallet_old_multicoin"
        val ethAddress = "0x1234567890123456789012345678901234567890"
        val btcAddress = "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa"

        helper.createDatabase(TEST_DB, 63).apply {
            execSQL("INSERT INTO wallets (id, name, type, position, pinned, `index`, source) VALUES ('$oldWalletId', 'Test Wallet', 'Multicoin', 0, 0, 0, 'Import')")
            execSQL("INSERT INTO accounts (wallet_id, address, chain, derivation_path) VALUES ('$oldWalletId', '$ethAddress', 'Ethereum', 'm/44''/60''/0''/0/0')")
            execSQL("INSERT INTO accounts (wallet_id, address, chain, derivation_path) VALUES ('$oldWalletId', '$btcAddress', 'Bitcoin', 'm/44''/0''/0''/0/0')")
            execSQL("INSERT INTO session (id, wallet_id, currency) VALUES (1, '$oldWalletId', 'USD')")
            close()
        }

        passwordStore.putPassword(oldWalletId, "test_password")
        File(keysDir, oldWalletId).writeText("test_key_data")

        val db = helper.runMigrationsAndValidate(TEST_DB, 64, true, Migration_63_64(context, passwordStore))

        val walletCursor = db.query("SELECT id, type FROM wallets WHERE name = 'Test Wallet'")
        assertTrue(walletCursor.moveToNext())
        val newWalletId = walletCursor.getString(0)
        assertEquals("multicoin_$ethAddress", newWalletId)
        walletCursor.close()

        val accountCursor = db.query("SELECT wallet_id FROM accounts WHERE address = '$ethAddress'")
        assertTrue(accountCursor.moveToNext())
        assertEquals(newWalletId, accountCursor.getString(0))
        accountCursor.close()

        val sessionCursor = db.query("SELECT wallet_id FROM session WHERE id = 1")
        assertTrue(sessionCursor.moveToNext())
        assertEquals(newWalletId, sessionCursor.getString(0))
        sessionCursor.close()

        assertEquals("test_password", passwordStore.getPassword(newWalletId))
        assertTrue(File(keysDir, newWalletId).exists())
        assertTrue(File(keysDir, newWalletId).readText() == "test_key_data")

        db.close()
    }

    @Test
    fun migrate63To64_singleWallet_generatesCorrectId() {
        val oldWalletId = "wallet_old_single"
        val ethAddress = "0xabcdef1234567890abcdef1234567890abcdef12"

        helper.createDatabase(TEST_DB, 63).apply {
            execSQL("INSERT INTO wallets (id, name, type, position, pinned, `index`, source) VALUES ('$oldWalletId', 'Ethereum Wallet', 'Single', 0, 0, 0, 'Import')")
            execSQL("INSERT INTO accounts (wallet_id, address, chain, derivation_path) VALUES ('$oldWalletId', '$ethAddress', 'Ethereum', 'm/44''/60''/0''/0/0')")
            execSQL("INSERT INTO session (id, wallet_id, currency) VALUES (1, '$oldWalletId', 'USD')")
            close()
        }

        passwordStore.putPassword(oldWalletId, "test_password_single")
        File(keysDir, oldWalletId).writeText("test_key_single")

        val db = helper.runMigrationsAndValidate(TEST_DB, 64, true, Migration_63_64(context, passwordStore))

        val walletCursor = db.query("SELECT id FROM wallets WHERE name = 'Ethereum Wallet'")
        assertTrue(walletCursor.moveToNext())
        val newWalletId = walletCursor.getString(0)
        assertEquals("single_ethereum_$ethAddress", newWalletId)
        walletCursor.close()

        assertEquals("test_password_single", passwordStore.getPassword(newWalletId))
        assertTrue(File(keysDir, newWalletId).exists())

        db.close()
    }

    @Test
    fun migrate63To64_privateKeyWallet_generatesCorrectId() {
        val oldWalletId = "wallet_old_privatekey"
        val solAddress = "SoLAddress123456789012345678901234567890"

        helper.createDatabase(TEST_DB, 63).apply {
            execSQL("INSERT INTO wallets (id, name, type, position, pinned, `index`, source) VALUES ('$oldWalletId', 'Solana Wallet', 'PrivateKey', 0, 0, 0, 'Import')")
            execSQL("INSERT INTO accounts (wallet_id, address, chain, derivation_path) VALUES ('$oldWalletId', '$solAddress', 'Solana', '')")
            execSQL("INSERT INTO session (id, wallet_id, currency) VALUES (1, '$oldWalletId', 'USD')")
            close()
        }

        passwordStore.putPassword(oldWalletId, "test_password_pk")
        File(keysDir, oldWalletId).writeText("test_key_pk")

        val db = helper.runMigrationsAndValidate(TEST_DB, 64, true, Migration_63_64(context, passwordStore))

        val walletCursor = db.query("SELECT id FROM wallets WHERE name = 'Solana Wallet'")
        assertTrue(walletCursor.moveToNext())
        val newWalletId = walletCursor.getString(0)
        assertEquals("privateKey_solana_$solAddress", newWalletId)
        walletCursor.close()

        assertEquals("test_password_pk", passwordStore.getPassword(newWalletId))
        assertTrue(File(keysDir, newWalletId).exists())

        db.close()
    }

    @Test
    fun migrate63To64_viewWallet_generatesCorrectId() {
        val oldWalletId = "wallet_old_view"
        val btcAddress = "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh"

        helper.createDatabase(TEST_DB, 63).apply {
            execSQL("INSERT INTO wallets (id, name, type, position, pinned, `index`, source) VALUES ('$oldWalletId', 'Watch Only', 'View', 0, 0, 0, 'Import')")
            execSQL("INSERT INTO accounts (wallet_id, address, chain, derivation_path) VALUES ('$oldWalletId', '$btcAddress', 'Bitcoin', '')")
            execSQL("INSERT INTO session (id, wallet_id, currency) VALUES (1, '$oldWalletId', 'USD')")
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 64, true, Migration_63_64(context, passwordStore))

        val walletCursor = db.query("SELECT id FROM wallets WHERE name = 'Watch Only'")
        assertTrue(walletCursor.moveToNext())
        val newWalletId = walletCursor.getString(0)
        assertEquals("view_bitcoin_$btcAddress", newWalletId)
        walletCursor.close()

        db.close()
    }

    @Test
    fun migrate63To64_duplicateWallets_handlesCorrectly() {
        val oldWalletId1 = "wallet_dup_1"
        val oldWalletId2 = "wallet_dup_2"
        val ethAddress = "0x9999999999999999999999999999999999999999"

        helper.createDatabase(TEST_DB, 63).apply {
            execSQL("INSERT INTO wallets (id, name, type, position, pinned, `index`, source) VALUES ('$oldWalletId1', 'First Wallet', 'Multicoin', 0, 0, 0, 'Import')")
            execSQL("INSERT INTO accounts (wallet_id, address, chain, derivation_path) VALUES ('$oldWalletId1', '$ethAddress', 'Ethereum', 'm/44''/60''/0''/0/0')")

            execSQL("INSERT INTO wallets (id, name, type, position, pinned, `index`, source) VALUES ('$oldWalletId2', 'Duplicate Wallet', 'Multicoin', 1, 0, 1, 'Import')")
            execSQL("INSERT INTO accounts (wallet_id, address, chain, derivation_path) VALUES ('$oldWalletId2', '$ethAddress', 'Ethereum', 'm/44''/60''/0''/0/0')")

            execSQL("INSERT INTO session (id, wallet_id, currency) VALUES (1, '$oldWalletId1', 'USD')")
            close()
        }

        passwordStore.putPassword(oldWalletId1, "password1")
        passwordStore.putPassword(oldWalletId2, "password2")
        File(keysDir, oldWalletId1).writeText("key1")
        File(keysDir, oldWalletId2).writeText("key2")

        val db = helper.runMigrationsAndValidate(TEST_DB, 64, true, Migration_63_64(context, passwordStore))

        val walletCursor = db.query("SELECT COUNT(*) FROM wallets")
        assertTrue(walletCursor.moveToNext())
        assertEquals(1, walletCursor.getInt(0))
        walletCursor.close()

        val remainingWalletCursor = db.query("SELECT id FROM wallets LIMIT 1")
        assertTrue(remainingWalletCursor.moveToNext())
        val remainingId = remainingWalletCursor.getString(0)
        assertEquals("multicoin_$ethAddress", remainingId)
        remainingWalletCursor.close()

        assertNotNull(passwordStore.getPassword(remainingId))

        db.close()
    }

    @Test
    fun migrate63To64_updatesAllRelatedTables() {
        val oldWalletId = "wallet_relations_test"
        val ethAddress = "0xaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"

        helper.createDatabase(TEST_DB, 63).apply {
            execSQL("INSERT INTO wallets (id, name, type, position, pinned, `index`, source) VALUES ('$oldWalletId', 'Relations Test', 'Multicoin', 0, 0, 0, 'Import')")
            execSQL("INSERT INTO accounts (wallet_id, address, chain, derivation_path) VALUES ('$oldWalletId', '$ethAddress', 'Ethereum', 'm/44''/60''/0''/0/0')")
            execSQL("INSERT INTO asset_wallet (wallet_id, asset_id, account_address) VALUES ('$oldWalletId', 'ethereum', '$ethAddress')")
            execSQL("INSERT INTO recent_assets (wallet_id, asset_id, type, addedAt) VALUES ('$oldWalletId', 'ethereum', 'swap', 0)")
            execSQL("INSERT INTO asset_config (wallet_id, asset_id, is_pinned, is_visible, list_position) VALUES ('$oldWalletId', 'ethereum', 0, 1, 0)")
            execSQL("INSERT INTO session (id, wallet_id, currency) VALUES (1, '$oldWalletId', 'USD')")
            close()
        }

        passwordStore.putPassword(oldWalletId, "test_relations")
        File(keysDir, oldWalletId).writeText("test_relations_key")

        val db = helper.runMigrationsAndValidate(TEST_DB, 64, true, Migration_63_64(context, passwordStore))

        val expectedWalletId = "multicoin_$ethAddress"

        val assetWalletCursor = db.query("SELECT wallet_id FROM asset_wallet WHERE asset_id = 'ethereum'")
        assertTrue(assetWalletCursor.moveToNext())
        assertEquals(expectedWalletId, assetWalletCursor.getString(0))
        assetWalletCursor.close()

        val recentCursor = db.query("SELECT wallet_id FROM recent_assets WHERE asset_id = 'ethereum'")
        assertTrue(recentCursor.moveToNext())
        assertEquals(expectedWalletId, recentCursor.getString(0))
        recentCursor.close()

        val configCursor = db.query("SELECT wallet_id FROM asset_config WHERE asset_id = 'ethereum'")
        assertTrue(configCursor.moveToNext())
        assertEquals(expectedWalletId, configCursor.getString(0))
        configCursor.close()

        db.close()
    }

    @Test
    fun migrate63To64_multipleWallets_prioritizesEthereumAccount() {
        val oldWalletId = "wallet_priority_test"
        val btcAddress = "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2"
        val ethAddress = "0xbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
        val solAddress = "SoLPriorityTest1234567890123456789012345"

        helper.createDatabase(TEST_DB, 63).apply {
            execSQL("INSERT INTO wallets (id, name, type, position, pinned, `index`, source) VALUES ('$oldWalletId', 'Priority Test', 'Multicoin', 0, 0, 0, 'Import')")
            execSQL("INSERT INTO accounts (wallet_id, address, chain, derivation_path) VALUES ('$oldWalletId', '$btcAddress', 'Bitcoin', 'm/44''/0''/0''/0/0')")
            execSQL("INSERT INTO accounts (wallet_id, address, chain, derivation_path) VALUES ('$oldWalletId', '$solAddress', 'Solana', 'm/44''/501''/0''/0')")
            execSQL("INSERT INTO accounts (wallet_id, address, chain, derivation_path) VALUES ('$oldWalletId', '$ethAddress', 'Ethereum', 'm/44''/60''/0''/0/0')")
            execSQL("INSERT INTO session (id, wallet_id, currency) VALUES (1, '$oldWalletId', 'USD')")
            close()
        }

        passwordStore.putPassword(oldWalletId, "test_priority")
        File(keysDir, oldWalletId).writeText("test_priority_key")

        val db = helper.runMigrationsAndValidate(TEST_DB, 64, true, Migration_63_64(context, passwordStore))

        val walletCursor = db.query("SELECT id FROM wallets WHERE name = 'Priority Test'")
        assertTrue(walletCursor.moveToNext())
        val newWalletId = walletCursor.getString(0)
        assertEquals("multicoin_$ethAddress", newWalletId)
        walletCursor.close()

        db.close()
    }

    @Test
    fun migrate63To64_sessionWithOldWalletId_updatesCorrectly() {
        val oldWalletId = "wallet_session_update"
        val ethAddress = "0xcccccccccccccccccccccccccccccccccccccccc"

        helper.createDatabase(TEST_DB, 63).apply {
            execSQL("INSERT INTO wallets (id, name, type, position, pinned, `index`, source) VALUES ('$oldWalletId', 'Session Update Wallet', 'Multicoin', 0, 0, 0, 'Import')")
            execSQL("INSERT INTO accounts (wallet_id, address, chain, derivation_path) VALUES ('$oldWalletId', '$ethAddress', 'Ethereum', 'm/44''/60''/0''/0/0')")
            execSQL("INSERT INTO session (id, wallet_id, currency) VALUES (1, '$oldWalletId', 'USD')")
            close()
        }

        passwordStore.putPassword(oldWalletId, "test_session_update")
        File(keysDir, oldWalletId).writeText("test_session_key")

        val db = helper.runMigrationsAndValidate(TEST_DB, 64, true, Migration_63_64(context, passwordStore))

        val walletCursor = db.query("SELECT COUNT(*) FROM wallets")
        assertTrue(walletCursor.moveToNext())
        assertEquals(1, walletCursor.getInt(0))
        walletCursor.close()

        val newWalletId = "multicoin_$ethAddress"
        val sessionCursor = db.query("SELECT wallet_id FROM session WHERE id = 1")
        assertTrue(sessionCursor.moveToNext())
        assertEquals(newWalletId, sessionCursor.getString(0))
        sessionCursor.close()

        assertEquals("test_session_update", passwordStore.getPassword(newWalletId))

        db.close()
    }

    @Test
    fun migrate63To64_updatesTransactionsTable() {
        val oldWalletId = "wallet_transactions_test"
        val ethAddress = "0xdddddddddddddddddddddddddddddddddddddddd"
        val txId = "0xtransaction123"
        val txHash = "0xhash123"

        helper.createDatabase(TEST_DB, 63).apply {
            execSQL("INSERT INTO wallets (id, name, type, position, pinned, `index`, source) VALUES ('$oldWalletId', 'Transaction Test', 'Multicoin', 0, 0, 0, 'Import')")
            execSQL("INSERT INTO accounts (wallet_id, address, chain, derivation_path) VALUES ('$oldWalletId', '$ethAddress', 'Ethereum', 'm/44''/60''/0''/0/0')")
            execSQL("""
                INSERT INTO transactions (
                    id, walletId, hash, assetId, feeAssetId, owner, recipient,
                    state, type, blockNumber, sequence, fee, value, direction,
                    createdAt, updatedAt
                ) VALUES (
                    '$txId', '$oldWalletId', '$txHash', 'ethereum', 'ethereum', '$ethAddress', '0xrecipient',
                    'confirmed', 'transfer', '12345', '1', '0.001', '1.5', 'self_transfer',
                    1234567890, 1234567890
                )
            """.trimIndent())
            execSQL("INSERT INTO session (id, wallet_id, currency) VALUES (1, '$oldWalletId', 'USD')")
            close()
        }

        passwordStore.putPassword(oldWalletId, "test_tx")
        File(keysDir, oldWalletId).writeText("test_tx_key")

        val db = helper.runMigrationsAndValidate(TEST_DB, 64, true, Migration_63_64(context, passwordStore))

        val newWalletId = "multicoin_$ethAddress"

        val walletCursor = db.query("SELECT id FROM wallets WHERE name = 'Transaction Test'")
        assertTrue(walletCursor.moveToNext())
        assertEquals(newWalletId, walletCursor.getString(0))
        walletCursor.close()

        val txCursor = db.query("SELECT walletId FROM transactions WHERE id = '$txId'")
        assertTrue(txCursor.moveToNext())
        assertEquals(newWalletId, txCursor.getString(0))
        txCursor.close()

        val txHashCursor = db.query("SELECT walletId FROM transactions WHERE hash = '$txHash'")
        assertTrue(txHashCursor.moveToNext())
        assertEquals(newWalletId, txHashCursor.getString(0))
        txHashCursor.close()

        db.close()
    }

    private class TestPasswordStore : PasswordStore {
        private val passwords = mutableMapOf<String, String>()

        override fun createPassword(key: String): String {
            val password = "generated_password_$key"
            passwords[key] = password
            return password
        }

        override fun getPassword(key: String): String {
            return passwords[key] ?: ""
        }

        override fun putPassword(key: String, password: String) {
            passwords[key] = password
        }

        override fun removePassword(key: String): Boolean {
            return passwords.remove(key) != null
        }
    }
}
