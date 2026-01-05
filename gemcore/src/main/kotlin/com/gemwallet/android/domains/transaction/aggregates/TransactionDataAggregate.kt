package com.gemwallet.android.domains.transaction.aggregates

import com.gemwallet.android.ext.getAddressEllipsisText
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.TransactionDirection
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.TransactionType

interface TransactionDataAggregate {
    val id: String
    val asset: Asset
    val address: String
    val value: String
    val equivalentValue: String?
    val type: TransactionType
    val direction: TransactionDirection
    val state: TransactionState

    val isPending: Boolean
        get() = state == TransactionState.Pending

    val createdAt: Long
}