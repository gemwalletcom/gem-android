package com.gemwallet.android.features.create_wallet.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gemwallet.android.R
import com.gemwallet.android.features.create_wallet.components.WordChip
import com.gemwallet.android.ui.components.MainActionButton
import com.gemwallet.android.ui.components.PhraseLayout
import com.gemwallet.android.ui.components.Scene
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.padding16
import kotlin.math.min

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CheckPhrase(
    words: List<String>,
    onDone: (String) -> Unit,
    onCancel: () -> Unit,
) {
    val random = remember {
        val shuffled = mutableListOf<String>()
        for (i in 0..words.size / 4) {
            val part = words.subList(
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
    Scene(
        title = stringResource(id = R.string.transfer_confirm),
        onClose = onCancel,
        padding = PaddingValues(padding16),
        mainAction = {
            MainActionButton(
                title = stringResource(id = R.string.common_continue),
                enabled = isDone,
            ) {
                onDone(result.joinToString(" "))
            }
        }
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
        FlowRow(
            modifier = Modifier.padding(16.dp),
            maxItemsInEachRow = 4,
            horizontalArrangement = Arrangement.Center,
        ) {
            (random).forEach { word ->
                WordChip(word = word, isEnable = !result.contains(word)) {
                    val index = result.size
                    if (words[index] == word) {
                        result.add(word)
                        render[result.size - 1] = word
                        true
                    } else {
                        false
                    }
                }
            }
        }
    }
}