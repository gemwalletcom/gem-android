package com.gemwallet.android.features.activities.models

import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.SwapProvider
import com.wallet.core.primitives.TransactionState

sealed interface TxDetailsProperty {

    class Date(val data: String) : TxDetailsProperty

    sealed class Destination(val data: String,) : TxDetailsProperty {
        class Sender(data: String) : Destination(data)
        class Recipient(data: String) : Destination(data)
    }

    class Status(val asset: Asset, val data: TransactionState) : TxDetailsProperty

    class Memo(val data: String) : TxDetailsProperty

    class Network(val data: Asset) : TxDetailsProperty

    class Provider(val data: SwapProvider) : TxDetailsProperty
}