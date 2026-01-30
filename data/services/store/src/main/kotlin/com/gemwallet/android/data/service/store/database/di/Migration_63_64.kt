package com.gemwallet.android.data.service.store.database.di

import android.content.Context
import android.util.Log
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gemwallet.android.application.PasswordStore
import com.gemwallet.android.application.wallet.coordinators.WalletIdGenerator
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.WalletType
import java.io.File

class Migration_63_64(context: Context, private val passwordStore: PasswordStore) : Migration(63, 64) {

    val keysDir = context.dataDir

    val walletIdGenerator = object : WalletIdGenerator {}

    override fun migrate(db: SupportSQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(false)

        val walletIds = mutableMapOf<String, WalletType>()
        val newWalletIds = mutableMapOf<String, String>()
        val accounts = mutableMapOf<String, MutableList<Account>>()
        val walletIdsToDelete = mutableListOf<String>()

        val walletsCursor = db.query("SELECT id, type FROM wallets")

        while (walletsCursor.moveToNext()) {
            val typeEnumName = walletsCursor.getString(1)
            val type = WalletType.entries.firstOrNull { it.name == typeEnumName } ?: continue
            walletIds[walletsCursor.getString(0)] = type
        }
        walletsCursor.close()

        val accountsCursor = db.query("SELECT wallet_id, address, chain FROM accounts",)
        while (accountsCursor.moveToNext()) {
            val walletId = accountsCursor.getString(0)
            val address = accountsCursor.getString(1)
            val chainEnumName = accountsCursor.getString(2)
            val chain = Chain.entries.firstOrNull { it.name == chainEnumName } ?: continue
            val account = Account(
                chain = chain,
                address = address,
                derivationPath = ""
            )
            accounts[walletId]?.add(account) ?: accounts.put(walletId, mutableListOf(account))
        }
        accountsCursor.close()

        for (walletEntry in accounts) {
            val oldWalletId = walletEntry.key
            val accounts = walletEntry.value
            val walletType = walletIds[oldWalletId] ?: continue
            val account = walletIdGenerator.getPriorityAccount(accounts) ?: continue
            val newWalletId = walletIdGenerator.generateWalletId(walletType, account.chain, account.address)
            if (newWalletIds.contains(newWalletId)) {
                walletIdsToDelete.add(oldWalletId)
                if (walletType != WalletType.View) {
                    try {
                        File(keysDir, oldWalletId).delete()
                        passwordStore.removePassword(oldWalletId)
                    } catch (err: Throwable) {
                        Log.d("MIGRATE_WALLET_ID", "Error on delete duplicates", err)
                    }
                }
            } else {
                newWalletIds[newWalletId] = oldWalletId
                try {
                    passwordStore.putPassword(newWalletId, passwordStore.getPassword(oldWalletId))
                    File(keysDir, oldWalletId)
                        .copyTo(File(keysDir, newWalletId))
                } catch (err: Throwable) {
                    Log.d("MIGRATE_WALLET_ID", "Error on copy keys", err)
                }
            }
        }

        for (newWalletId in newWalletIds) {
            db.execSQL("UPDATE wallets SET id = ? WHERE id = ?", arrayOf(newWalletId.key, newWalletId.value))
            db.execSQL("UPDATE accounts SET wallet_id = ? WHERE wallet_id = ?", arrayOf(newWalletId.key, newWalletId.value))
            db.execSQL("UPDATE asset_wallet SET wallet_id = ? WHERE wallet_id = ?", arrayOf(newWalletId.key, newWalletId.value))
            db.execSQL("UPDATE balances SET wallet_id = ? WHERE wallet_id = ?", arrayOf(newWalletId.key, newWalletId.value))
            db.execSQL("UPDATE recent_assets SET wallet_id = ? WHERE wallet_id = ?", arrayOf(newWalletId.key, newWalletId.value))
            db.execSQL("UPDATE asset_config SET wallet_id = ? WHERE wallet_id = ?", arrayOf(newWalletId.key, newWalletId.value))
            db.execSQL("UPDATE banners SET wallet_id = ? WHERE wallet_id = ?", arrayOf(newWalletId.key, newWalletId.value))
            db.execSQL("UPDATE room_connection SET wallet_id = ? WHERE wallet_id = ?", arrayOf(newWalletId.key, newWalletId.value))
            db.execSQL("UPDATE nft_association SET wallet_id = ? WHERE wallet_id = ?", arrayOf(newWalletId.key, newWalletId.value))
            db.execSQL("UPDATE transactions SET walletId = ? WHERE walletId = ?", arrayOf(newWalletId.key, newWalletId.value))
        }

        val sessionCursor = db.query("SELECT wallet_id FROM session WHERE id = 1")
        val sessionWalletId: String? = if (sessionCursor.moveToNext()) {
            val walletId = sessionCursor.getString(0)
            newWalletIds.firstNotNullOfOrNull {  entry -> entry.takeIf { it.value == walletId }?.key } ?: newWalletIds.keys.firstOrNull()
        } else {
            newWalletIds.keys.firstOrNull()
        }

        if (sessionWalletId == null) {
            db.execSQL("UPDATE session SET wallet_id = ? WHERE id = 1", arrayOf(sessionWalletId))
        }

        for (walletId in walletIdsToDelete) {
            db.execSQL("DELETE FROM wallets WHERE id = ?", arrayOf(walletId))
            db.execSQL("DELETE FROM accounts WHERE wallet_id = ?", arrayOf(walletId))
            db.execSQL("DELETE FROM asset_wallet WHERE wallet_id = ?", arrayOf(walletId))
            db.execSQL("DELETE FROM recent_assets WHERE wallet_id = ?", arrayOf(walletId))
            db.execSQL("DELETE FROM asset_config WHERE wallet_id = ?", arrayOf(walletId))
            db.execSQL("DELETE FROM banners WHERE wallet_id = ?", arrayOf(walletId))
            db.execSQL("DELETE FROM room_connection WHERE wallet_id = ?", arrayOf(walletId))
            db.execSQL("DELETE FROM nft_association WHERE wallet_id = ?", arrayOf(walletId))
            db.execSQL("DELETE FROM transactions WHERE walletId = ?", arrayOf(walletId))
        }
    }
}