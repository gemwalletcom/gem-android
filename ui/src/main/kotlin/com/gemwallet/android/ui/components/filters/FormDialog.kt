package com.gemwallet.android.ui.components.filters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.screen.ModalBottomSheet
import com.gemwallet.android.ui.theme.normalPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormDialog(
    title: String,
    fullScreen: Boolean = false,
    onDismiss: () -> Unit,
    onClear: (() -> Unit)? = null,
    doneAction: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = fullScreen)

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        dragHandle = {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .normalPadding(),
            ) {
                Box(modifier = Modifier.weight(0.5f)) {
                    (doneAction)?.let {
                        TextButton(
                            modifier = Modifier.align(Alignment.CenterStart),
                            onClick = onDismiss,
                        ) {
                            Text(stringResource(R.string.common_cancel))
                        }
                    }
                        ?:
                    (onClear)?.let {
                        TextButton(
                            modifier = Modifier.align(Alignment.CenterStart),
                            onClick = it,
                        ) {
                            Text(stringResource(R.string.filter_clear))
                        }
                    }

                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                ) {
                    Surface(
                        modifier = Modifier.padding(vertical = 0.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Box(Modifier.size(width = 32.dp, height = 4.dp))
                    }
                    Text(
                        text = title,
                        textAlign = TextAlign.Center,
                        modifier = Modifier,
                    )
                }
                Box(modifier = Modifier.weight(0.5f)) {
                    if (doneAction != null) {
                        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                            doneAction()
                        }
                    } else {
                        TextButton(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            onClick = onDismiss,
                        ) {
                            Text(stringResource(R.string.common_done))
                        }
                    }
                }
            }
        },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}