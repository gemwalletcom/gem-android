package com.gemwallet.android.features.wallet

sealed interface WalletEvent {

    data class SetWalletId(val walletId: String) : WalletEvent
}