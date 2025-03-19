package com.gemwallet.android.features.transactions.details.model

import com.gemwallet.android.model.AssetInfo
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.TransactionDirection
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.TransactionType

class TxDetailsScreenModel(
    val assetId: AssetId,
    val assetSymbol: String,
    val assetIcon: String,
    val assetType: AssetType,
    val cryptoAmount: String,
    val fiatAmount: String,
    val createdAt: String,
    val direction: TransactionDirection,
    val from: String,
    val to: String,
    val memo: String?,
    val state: TransactionState,
    val networkTitle: String,
    val feeCrypto: String,
    val feeFiat: String,
    val type: TransactionType,
    val explorerUrl: String,
    val explorerName: String = "",
    val fromAsset: AssetInfo? = null,
    val toAsset: AssetInfo? = null,
    val fromValue: String? = null,
    val toValue: String? = null,
    val currency: Currency? = null,
)