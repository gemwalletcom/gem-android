package com.gemwallet.android.features.wallets.viewmodels

sealed interface WalletsEvent {
    data class SelectClick(val walletId: String) : WalletsEvent
    data class DeleteClick(val walletId: String, val onBoard: () -> Unit) : WalletsEvent
}
