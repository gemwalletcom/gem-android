package com.gemwallet.android.data.service.store.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletType

@Entity(tableName = "wallets")
data class DbWallet(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "domain_name") val domainName: String?,
    val type: WalletType,
    val position: Int,
    val pinned: Boolean,
    val index: Int,
)

fun DbWallet.toModel(accounts: List<DbAccount>): Wallet {
    return Wallet(
        id = id,
        name = name,
        type = type,
        accounts = accounts.toModel(),
        index = index,
        order = 0,
        isPinned = pinned,
    )
}

fun Wallet.toRecord(): DbWallet {
    return DbWallet(
        id = id,
        name = name,
        type = type,
        domainName = null,
        position = 0,
        pinned = isPinned,
        index = index,
    )
}