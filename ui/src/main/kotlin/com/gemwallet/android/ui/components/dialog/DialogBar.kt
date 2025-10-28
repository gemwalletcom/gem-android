package com.gemwallet.android.ui.components.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.theme.Spacer8
import com.gemwallet.android.ui.theme.normalPadding

@Composable
fun DialogBar(
    title: String,
    onDismissRequest: () -> Unit,
) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .normalPadding(),
    ) {
        Box(modifier = Modifier.weight(0.5f)) {
            TextButton(
                modifier = Modifier.align(Alignment.CenterStart),
                onClick = onDismissRequest,
            ) {
                Text(stringResource(R.string.common_done))
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(
                modifier = Modifier.padding(vertical = 0.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Box(Modifier.size(width = 32.dp, height = 4.dp))
            }
            Spacer8()
            Text(
                text = title,
                textAlign = TextAlign.Center,
                modifier = Modifier,
            )
        }
        Box(modifier = Modifier.weight(0.5f))
    }
}