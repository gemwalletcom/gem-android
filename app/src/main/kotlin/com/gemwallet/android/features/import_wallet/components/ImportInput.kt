package com.gemwallet.android.features.import_wallet.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PlatformImeOptions
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.blockchain.operators.InvalidWords
import com.gemwallet.android.blockchain.operators.walletcore.WCValidatePhraseOperator
import com.gemwallet.android.model.ImportType
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.clickable
import com.gemwallet.android.ui.components.clipboard.getPlainText
import com.gemwallet.android.ui.components.progress.CircularProgressIndicator16
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.features.add_asset.viewmodels.AddressChainViewModel
import com.wallet.core.primitives.NameRecord
import com.wallet.core.primitives.WalletType

@Composable
internal fun ImportInput(
    inputState: TextFieldValue,
    importType: ImportType,
    onValueChange: (TextFieldValue) -> Unit,
    onResolved: (NameRecord?) -> Unit,
) {
    val viewModel: AddressChainViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        viewModel.onResolved(onResolved)

        onDispose { }
    }

    val errorColor =  MaterialTheme.colorScheme.error
    val clipboardManager = LocalClipboard.current.nativeClipboard
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            BasicTextField(
                modifier = Modifier.fillMaxWidth(),
                onValueChange = {
                    onValueChange(it)
                    if (importType.walletType == WalletType.view || importType.walletType == WalletType.private_key) {
                        viewModel.onInput(it.text, importType.chain)
                    }
                },
                value = inputState,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                minLines = 2,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                visualTransformation = {
                    if (importType.walletType == WalletType.view  || importType.walletType == WalletType.private_key) {
                        return@BasicTextField TransformedText(it, OffsetMapping.Identity)
                    }
                    TransformedText(
                        highlightErrors(
                            it.text,
                            errorColor = errorColor
                        ),
                        OffsetMapping.Identity
                    )
                },
                decorationBox = { innerTextField ->
                    if (inputState.text.isEmpty()) {
                        Text(
                            text = when (importType.walletType) {
                                WalletType.view -> stringResource(R.string.wallet_import_address_field)
                                WalletType.private_key -> stringResource(R.string.common_private_key)
                                else -> stringResource(R.string.wallet_import_secret_phrase)
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                    innerTextField()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    platformImeOptions = PlatformImeOptions("flagNoPersonalizedLearning"),
                    autoCorrectEnabled = false,
                ),
                interactionSource = interactionSource,
            )
            Row(
                modifier = Modifier.align(Alignment.TopEnd),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator16()
                    Spacer(modifier = Modifier.size(8.dp))
                }
                if (uiState.isResolve) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Name is resolved",
                        tint = MaterialTheme.colorScheme.tertiary,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }
                if (uiState.isFail) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Default.Error,
                        contentDescription = "Name is resolved",
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }
            }
        }
        Spacer16()
        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clickable {
                        val newValue = clipboardManager.getPlainText() ?: ""
                        onValueChange(
                            TextFieldValue("$newValue ", TextRange(newValue.length + 1))
                        )
                    }
                    .testTag("paste"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = "paste",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(id = R.string.common_paste),
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

private fun highlightErrors(text: String, errorColor: Color): AnnotatedString {
    val validateResult = WCValidatePhraseOperator().invoke(text)
    val error = validateResult.exceptionOrNull()
    val inputWords = text.split(" ")
    val spans = if (error is InvalidWords) {
        error.words.filter {
            text.indexOf("$it ") != -1
        }.map { word ->
            val ranges = mutableListOf<AnnotatedString.Range<SpanStyle>>()
            var offset = 0
            for (i in inputWords.indices) {
                val inputWord = inputWords[i]
                val end = offset + inputWord.length + 1
                if (inputWord == word && end <= text.length && end != offset) {
                    ranges.add(
                        AnnotatedString.Range(
                            item = SpanStyle(color = errorColor),
                            start = offset,
                            end = end,
                        )
                    )
                }
                offset = end
            }
            ranges
        }.flatten()
    } else {
        emptyList()
    }
    return AnnotatedString(text, spans)
}