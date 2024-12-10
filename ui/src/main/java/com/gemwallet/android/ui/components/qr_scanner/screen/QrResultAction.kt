package com.gemwallet.android.ui.components.qr_scanner.screen

fun interface QrResultAction {
    operator fun invoke(result: String)
}