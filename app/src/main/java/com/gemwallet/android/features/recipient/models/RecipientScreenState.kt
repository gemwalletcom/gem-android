package com.gemwallet.android.features.recipient.models

sealed interface RecipientScreenState {
    object Idle : RecipientScreenState
    object ScanAddress : RecipientScreenState
    object ScanMemo : RecipientScreenState
}