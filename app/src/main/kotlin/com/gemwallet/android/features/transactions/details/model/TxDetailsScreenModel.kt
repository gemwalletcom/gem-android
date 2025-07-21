package com.gemwallet.android.features.transactions.details.model

import com.gemwallet.android.model.AssetInfo
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.NFTAsset
import com.wallet.core.primitives.SwapProvider
import com.wallet.core.primitives.TransactionDirection
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.TransactionType

class TxDetailsScreenModel(
    val asset: Asset,
    val cryptoAmount: String,
    val fiatAmount: String,
    val createdAt: String,
    val direction: TransactionDirection,
    val from: String,
    val to: String,
    val memo: String?,
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
    val provider: SwapProvider? = null,
    val currency: Currency? = null,
    val nftAsset: NFTAsset? = null,
)