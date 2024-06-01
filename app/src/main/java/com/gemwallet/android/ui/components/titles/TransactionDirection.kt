package com.gemwallet.android.ui.components.titles

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.wallet.core.primitives.TransactionDirection

@Composable
fun TransactionDirection.getValueColor(): Color {
    return when (this) {
        TransactionDirection.SelfTransfer,
        TransactionDirection.Outgoing -> MaterialTheme.colorScheme.onSurface
        TransactionDirection.Incoming -> MaterialTheme.colorScheme.tertiary
    }
}