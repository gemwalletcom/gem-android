package com.gemwallet.android.ui.components.buttons

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun FieldBottomAction(
    imageVector: ImageVector,
    contentDescription: String,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    ElevatedButton(
        modifier = modifier,
        colors = ButtonDefaults.buttonColors().copy(containerColor = MaterialTheme.colorScheme.scrim),
        onClick = onClick,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = text,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}