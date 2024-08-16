package com.gemwallet.android.features.import_wallet.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gemwallet.android.R
import com.wallet.core.primitives.WalletType

@Composable
internal fun WalletTypeTab(
    type: WalletType,
    selectedType: WalletType,
    onTypeChange: (WalletType) -> Unit,
) {
    val isSelected = type == selectedType
    Tab(
        modifier = Modifier
            .padding(horizontal = if (isSelected) 2.dp else 0.dp, vertical = 2.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (isSelected) {
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.09f)
                } else {
                    Color.Transparent
                }
            ),
        selected = true,
        onClick = { onTypeChange(type) },
        text = {
            Text(
                text = when (type) {
                    WalletType.view -> stringResource(id = R.string.common_address)
                    WalletType.single, WalletType.multicoin -> stringResource(id = R.string.common_phrase)
                    WalletType.private_key -> stringResource(id = R.string.common_private_key)
                },
                maxLines = 1,
                color =  MaterialTheme.colorScheme.onSurface,
            )
        },
        selectedContentColor =  MaterialTheme.colorScheme.secondary.copy(alpha = 0.09f),
        unselectedContentColor = Color.Transparent,
    )
}