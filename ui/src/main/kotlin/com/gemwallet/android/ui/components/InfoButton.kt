package com.gemwallet.android.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.gemwallet.android.ui.theme.Spacer4
import com.gemwallet.android.ui.theme.trailingIconMedium

@Composable
fun InfoButton(entity: InfoSheetEntity) {
    var showBottomSheet by remember { mutableStateOf(false) }
    Spacer4()
    Icon(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .size(trailingIconMedium)
            .clickable(onClick = { showBottomSheet = true }),
        imageVector = Icons.Outlined.Info,
        contentDescription = "",
        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
    )
    if (showBottomSheet) {
        InfoBottomSheet(entity) {
            showBottomSheet = false
        }
    }
}