package com.gemwallet.android.ui.components.buttons

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gemwallet.android.ui.components.designsystem.mainActionHeight
import com.gemwallet.android.ui.components.progress.CircularProgressIndicator20

val disableButtonColor = Color(0xFF1742C5)

@Composable
fun MainActionButton(
    title: String,
    enabled: Boolean = true,
    loading: Boolean = false,
    colors: ButtonColors = ButtonDefaults.buttonColors().copy(
        disabledContainerColor = disableButtonColor,
        disabledContentColor = MaterialTheme.colorScheme.onPrimary,
    ),
    onClick: () -> Unit,
) {
    MainActionButton(enabled && !loading, colors, onClick) {
        if (loading) {
            CircularProgressIndicator20(color = MaterialTheme.colorScheme.onPrimary)
        } else {
            Text(
                modifier = Modifier.padding(4.dp),
                text = title,
                fontSize = 18.sp,
            )
        }
    }
}

@Composable
fun MainActionButton(
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = mainActionHeight)
            .testTag("main_action"),
        onClick = onClick,
        enabled = enabled,
        colors = colors,
    ) {
        content()
    }
}