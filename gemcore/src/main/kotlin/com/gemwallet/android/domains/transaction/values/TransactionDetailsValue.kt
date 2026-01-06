package com.gemwallet.android.domains.transaction.values

import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.NFTAsset
import com.wallet.core.primitives.NFTAssetId
import com.wallet.core.primitives.SwapProvider
import com.wallet.core.primitives.TransactionState

sealed interface TransactionDetailsValue {

    sealed interface Amount : TransactionDetailsValue {
        class Swap(
            val fromAsset: Asset,
            val fromValue: String,
            val toAsset: Asset,
            val toValue: String,
            val currency: Currency,
        ) : Amount

        class NFT(val asset: NFTAsset) : Amount

        class Plain(
            val asset: Asset,
            val value: String,
            val equivalent: String,
        ) : Amount

        object None : Amount
    }

    class Fee(
        val asset: Asset,
        val value: String,
        val equivalent: String,
    ) : TransactionDetailsValue

    class Date(val data: String) : TransactionDetailsValue

    sealed interface Destination : TransactionDetailsValue {
        class Sender(val data: String) : Destination
        class Recipient(val data: String) : Destination
        class Provider(val data: SwapProvider) : Destination
    }

    class Status(val data: TransactionState) : TransactionDetailsValue

    class Memo(val data: String) : TransactionDetailsValue

    class Network(val data: Asset) : TransactionDetailsValue

    class Explorer(url: String, val name: String) : TransactionDetailsValue
}