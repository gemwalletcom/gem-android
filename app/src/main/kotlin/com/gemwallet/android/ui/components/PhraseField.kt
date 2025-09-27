package com.gemwallet.android.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.buttons.FieldBottomAction
import com.gemwallet.android.ui.components.clipboard.getPlainText
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.WalletTheme

@Composable
private fun PhraseTextField(
    value: String,
    onValueChange: (String) -> Unit,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit,
) {
    val clipboardManager = LocalClipboard.current.nativeClipboard
    val textStyle = LocalTextStyle.current
    val mergedTextStyle = textStyle.merge(TextStyle(color = MaterialTheme.colorScheme.onSurface))
    decorationBox {
        Column(modifier = modifier) {
            BasicTextField(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                onValueChange = onValueChange,
                minLines = minLines,
                interactionSource = interactionSource,
                textStyle = mergedTextStyle,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            )
            Spacer16()
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                FieldBottomAction(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .align(Alignment.CenterEnd),
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = "paste",
                    text = stringResource(id = R.string.common_paste),
                ) {
                    onValueChange(clipboardManager.getPlainText() ?: "")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhraseField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "",
    minLines: Int = 1,
) {
    val interactionSource = remember { MutableInteractionSource() }
    PhraseTextField(
        modifier = if (label.isNotEmpty()) {
            modifier
                .semantics(mergeDescendants = true) {}
                .padding(top = 8.dp)
        } else {
            modifier
        },
        value = value,
        onValueChange = onValueChange,
        interactionSource = interactionSource,
        minLines = minLines,
    ) { innerTextField ->
        OutlinedTextFieldDefaults.DecorationBox(
            value = value,
            innerTextField = innerTextField,
            enabled = true,
            singleLine = false,
            visualTransformation = VisualTransformation.None,
            interactionSource = interactionSource,
            label = {
                Text(text = label)
            },
            placeholder = {
                Text(text = placeholder)
            },
            colors = OutlinedTextFieldDefaults.colors(),
            container = {
                OutlinedTextFieldDefaults.Container(
                    enabled = true,
                    isError = false,
                    interactionSource = interactionSource,
                    shape = OutlinedTextFieldDefaults.shape,
                    colors = OutlinedTextFieldDefaults.colors()
                )
            }
        )
    }
}
//
//@Composable
//@Preview
//fun PreviewPhraseTextField() {
//    WalletTheme {
//        PhraseField(value = "", onValueChange = {})
//    }
//}