package com.gemwallet.android.ui.components.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.theme.Spacer8
import com.gemwallet.android.ui.theme.normalPadding

@Composable
fun PhraseLayout(
    words: List<String>,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val halfSize = words.size / 2
        for (i in 0 until halfSize) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                PhraseWordItem(
                    index = i,
                    word = words[i],
                    isNextToEnter = (i > 0 && words[i].isEmpty() && words[i - 1].isNotEmpty()) ||  (words[i].isEmpty() && i == 0),
                )
                Spacer(modifier = Modifier.width(20.dp))
                PhraseWordItem(
                    index = i + halfSize,
                    word = words[i + halfSize],
                    isNextToEnter = words[i + halfSize].isEmpty() && words[(i + halfSize) - 1].isNotEmpty(),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun RowScope.PhraseWordItem(
    index: Int,
    word: String,
    isNextToEnter: Boolean,
) {
    Surface(
        modifier = Modifier.weight(0.5f),
        shadowElevation = 2.dp,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.scrim,
        border = if (isNextToEnter) BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier.normalPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer8()
                Text(
                    text = "${index + 1}.",
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier,
                )
            }
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = word,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}