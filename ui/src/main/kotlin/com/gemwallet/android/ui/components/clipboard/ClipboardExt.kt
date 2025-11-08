package com.gemwallet.android.ui.components.clipboard

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import android.widget.Toast
import androidx.compose.ui.platform.NativeClipboard
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

fun NativeClipboard.setPlainText(context: Context, data: String, isSensitive: Boolean = false) {
    val clip = ClipData.newPlainText("", data).apply {
        if (isSensitive) {
            description.extras = PersistableBundle().apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
                } else {
                    putBoolean("android.content.extra.IS_SENSITIVE", true)
                }
            }
        }
    }
    setPrimaryClip(clip)
}

fun NativeClipboard.getPlainText(): String? {
    return primaryClip?.getItemAt(0)?.text?.toString()
}