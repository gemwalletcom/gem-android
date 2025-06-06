package com.gemwallet.android.ui.components.clipboard

import android.content.ClipData
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.ui.platform.NativeClipboard

fun NativeClipboard.setPlainText(context: Context, data: String) {
    setPrimaryClip(ClipData.newPlainText("", data))
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
}

fun NativeClipboard.getPlainText(): String? {
    return primaryClip?.getItemAt(0)?.text?.toString()
}