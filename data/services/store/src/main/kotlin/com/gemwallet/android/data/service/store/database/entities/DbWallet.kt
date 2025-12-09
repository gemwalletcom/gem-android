package com.gemwallet.android.data.service.store.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletSource
import com.wallet.core.primitives.WalletType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Entity(tableName = "wallets")
data class DbWallet(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "domain_name") val domainName: String?,
    val type: WalletType,
    val position: Int,
    val pinned: Boolean,
    val index: Int,
    @ColumnInfo(defaultValue = "Import") val source: WalletSource,
)

fun DbWallet.toDTO(accounts: List<DbAccount>): Wallet {
    return Wallet(
        id = id,
        name = name,
        type = type,
        accounts = accounts.toDTO(),
        index = index,
        order = 0,
        isPinned = pinned,
        source = source,
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
        source = source,
    )
}

fun Map<DbWallet, List<DbAccount>>.toDTO() = map { entry -> entry.key.toDTO(entry.value) }

fun Flow<Map<DbWallet, List<DbAccount>>>.toDTO() = map { entry -> entry.toDTO() }