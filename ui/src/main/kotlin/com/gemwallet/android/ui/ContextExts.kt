package com.gemwallet.android.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }

    return null
}

@Composable
fun DisableScreenShooting() {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        context.findActivity()?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE,
        )

        onDispose {
            context.findActivity()?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE,)
        }
    }
}