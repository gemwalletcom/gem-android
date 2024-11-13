package com.gemwallet.android.features.create_wallet.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gemwallet.android.R
import com.gemwallet.android.features.create_wallet.components.WordChip
import com.gemwallet.android.ui.components.PhraseLayout
import com.gemwallet.android.ui.components.buttons.MainActionButton
import com.gemwallet.android.ui.components.designsystem.Spacer16
import com.gemwallet.android.ui.components.designsystem.padding16
import com.gemwallet.android.ui.components.screen.Scene
import kotlin.math.min

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CheckPhrase(
    words: List<String>,
    onDone: (String) -> Unit,
    onCancel: () -> Unit,
) {
    val random = remember {
        val shuffled = mutableListOf<Pair<Int, String>>()
        for (i in 0..words.size / 4) {
            val part = words.mapIndexed { index, word -> Pair(index, word) }.subList(
                fromIndex = i * 4,
                toIndex = min(i * 4 + 4, words.size)
            ).shuffled()
            shuffled.addAll(part)
        }
        shuffled.toList()
    }
    val render = remember {
        val state = mutableStateListOf<String>()
        state.addAll(words.map { "" })
        state
    }
    val result = remember {
        mutableStateListOf<String>()
    }
    val isDone by remember {
        derivedStateOf {
            result.joinToString() == words.joinToString()
        }
    }
    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp.dp < 680.dp

    val onWordClick: (String) -> Boolean = { word ->
        val index = result.size
        if (words[index] == word) {
            result.add(word)
            render[result.size - 1] = word
            true
        } else {
            false
        }
    }

    Scene(
        title = stringResource(id = R.string.transfer_confirm),
        onClose = onCancel,
        padding = PaddingValues(horizontal = padding16),
        mainAction = {
            AnimatedVisibility(
                visible = isDone || !isSmallScreen,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                MainActionButton(
                    title = stringResource(id = R.string.common_continue),
                    enabled = isDone,
                ) {
                    onDone(result.joinToString(" "))
                }
            }
        }
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(id = R.string.secret_phrase_confirm_quick_test_title),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer16()
            PhraseLayout(
                words = render,
            )
            AnimatedVisibility(visible = !isDone || !isSmallScreen) {
                FlowRow(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth(),
                    maxItemsInEachRow = 4,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    if (isSmallScreen) {
                        val slice = result.size / 4
                        if (slice < 3) {
                            (random.slice(slice * 4..<slice * 4 + 4)).forEach {word ->
                                WordChip(word.second, result.getOrNull(word.first) != word.second, onWordClick)
                            }
                        }
                    } else {
                        (random).forEach { word ->
                            WordChip(word.second, result.getOrNull(word.first) != word.second, onWordClick)
                        }
                    }
                }
            }
        }
    }
}