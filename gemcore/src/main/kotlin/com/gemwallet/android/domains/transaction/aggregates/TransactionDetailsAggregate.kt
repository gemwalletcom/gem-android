package com.gemwallet.android.domains.transaction.aggregates

import com.gemwallet.android.domains.transaction.values.ValueGroup
import com.gemwallet.android.domains.transaction.values.TransactionDetailsValue
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.TransactionDirection
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.TransactionType

interface TransactionDetailsAggregate {
    val id: String
    val asset: Asset
    val type: TransactionType
    val direction: TransactionDirection
    val state: TransactionState

    val currency: Currency

    val amount: TransactionDetailsValue.Amount
    val fee: TransactionDetailsValue.Fee
    val date: TransactionDetailsValue.Date
    val status: TransactionDetailsValue.Status
    val memo: TransactionDetailsValue.Memo?
    val network: TransactionDetailsValue.Network
    val destination: TransactionDetailsValue.Destination?

    val explorer: TransactionDetailsValue.Explorer

    val valueGroups: List<ValueGroup<TransactionDetailsValue>>
}