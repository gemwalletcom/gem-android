package com.gemwallet.android.ui.components.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import com.gemwallet.android.ui.components.designsystem.Spacer16

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalBottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
    dragHandle: @Composable () -> Unit = { Box { Spacer16() } },
    content: @Composable ColumnScope.() -> Unit,
) {
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f),
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = dragHandle,
        content = content
    )
}