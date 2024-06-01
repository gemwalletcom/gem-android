package com.gemwallet.android.features.create_wallet.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.gemwallet.android.ui.theme.padding8

private enum class WordState {
    Error,
    Done,
    Idle,
}

@Composable
internal fun WordChip(
    word: String,
    isEnable: Boolean,
    onClick: (String) -> Boolean,
) {
    val shakeController = rememberShakeController()
    var wordState by remember { mutableStateOf(WordState.Idle) }
    val color: Color by animateColorAsState(
        when (wordState) {
            WordState.Error -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.primary
        }, label = "Button color"
    )
    SuggestionChip(
        modifier = Modifier
            .padding(end = padding8)
            .shake(shakeController, onComplete = { wordState = WordState.Idle }),
        onClick = {
            if (!isEnable) {
                return@SuggestionChip
            }
            if (!onClick(word)) {
                shakeController.shake(
                    ShakeConfig(
                        iterations = 4,
                        intensity = 2_000f,
                        rotateY = 15f,
                        translateX = 20f,
                    )
                )
                wordState = WordState.Error
            }
        },
        enabled = wordState != WordState.Idle || isEnable,
        colors = SuggestionChipDefaults.elevatedSuggestionChipColors().copy(
            containerColor = color,
        ),
        label = {
            Text(
                text = word,
                color = Color.White,
            )
        },
        border = null,
    )
}