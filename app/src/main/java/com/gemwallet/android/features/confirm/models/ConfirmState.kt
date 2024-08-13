package com.gemwallet.android.features.confirm.models

sealed interface ConfirmState {
    data object Prepare : ConfirmState

    data object Ready : ConfirmState

    data object Sending : ConfirmState

    class Result(txHash: String) : ConfirmState

    class Error(message: ConfirmError) : ConfirmState
}