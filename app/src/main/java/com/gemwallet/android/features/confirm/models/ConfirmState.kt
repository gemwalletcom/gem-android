package com.gemwallet.android.features.confirm.models

sealed interface ConfirmState {
    data object Prepare : ConfirmState

    data object Ready : ConfirmState

    data object Sending : ConfirmState

    class Result(val txHash: String, val error: ConfirmError? = null) : ConfirmState

    class Error(val message: ConfirmError) : ConfirmState

    data object FatalError : ConfirmState
}