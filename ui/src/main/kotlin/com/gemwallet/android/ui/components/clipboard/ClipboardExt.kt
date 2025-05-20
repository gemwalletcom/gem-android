package com.gemwallet.android.ui.components.clipboard

import android.content.ClipData
import androidx.compose.ui.platform.NativeClipboard

fun NativeClipboard.setPlainText(data: String) {
    setPrimaryClip(ClipData.newPlainText("", data))
}

fun NativeClipboard.getPlainText(): String? {
    return primaryClip?.getItemAt(0)?.text?.toString()
}