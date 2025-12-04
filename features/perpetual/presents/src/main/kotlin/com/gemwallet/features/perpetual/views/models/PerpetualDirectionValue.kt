package com.gemwallet.features.perpetual.views.models

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.ui.R
import com.wallet.core.primitives.PerpetualDirection

@Composable
fun PerpetualDirection.text(leverage: Int): String = when (this) {
    PerpetualDirection.Short -> "${stringResource(R.string.perpetual_short)} ${leverage}x"
    PerpetualDirection.Long -> "${stringResource(R.string.perpetual_long)} ${leverage}x"
}

@Composable
fun PerpetualDirection.color(): Color = when (this) {
    PerpetualDirection.Short -> MaterialTheme.colorScheme.error
    PerpetualDirection.Long -> MaterialTheme.colorScheme.tertiary
}

