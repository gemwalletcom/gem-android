package com.gemwallet.android.features.activities.viewmodels

import com.gemwallet.android.features.activities.models.TxDetailsProperty
import com.gemwallet.android.model.AssetInfo
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.NFTAsset
import com.wallet.core.primitives.TransactionDirection
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.TransactionType

class TxDetailsScreenModel(
    val asset: Asset,
    val cryptoAmount: String,
    val fiatAmount: String,
    val direction: TransactionDirection,
    val state: TransactionState,
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
    val nftAsset: NFTAsset? = null,
    val properties: List<TxDetailsProperty>,
)