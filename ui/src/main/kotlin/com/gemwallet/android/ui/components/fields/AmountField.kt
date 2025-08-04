package com.gemwallet.android.ui.components.fields

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.components.designsystem.headerSupportIconSize
import com.gemwallet.android.ui.models.AmountInputType
import com.wallet.core.primitives.Currency

@Composable
fun ColumnScope.AmountField(
    amount: String,
    assetSymbol: String,
    currency: Currency,
    equivalent: String,
    onValueChange: (String) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    inputType: AmountInputType = AmountInputType.Crypto,
    onInputTypeClick: (() -> Unit)? = null,
    readOnly: Boolean = false,
    error: String,
    textStyle: TextStyle = MaterialTheme.typography.displaySmall,
    transformation: AmountTransformation = CryptoAmountTransformation(
        when (inputType) {
            AmountInputType.Crypto -> assetSymbol
            AmountInputType.Fiat -> android.icu.util.Currency.getInstance(currency.string).symbol
        },
        inputType,
        MaterialTheme.colorScheme.secondary
    ),
) {
    val interactionSource = remember { MutableInteractionSource() }

    BasicTextField(
        modifier = modifier,
        value = TextFieldValue(
            text = amount,
            selection = TextRange(if (amount.isNotEmpty()) amount.length else 0)
        ),
        onValueChange = { onValueChange(it.text) },
        visualTransformation = transformation,
        maxLines = 1,
        textStyle = textStyle.copy(
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(
            onNext = { onNext() }
        ),
        interactionSource = interactionSource,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        readOnly = readOnly,
    )
    Spacer(modifier = Modifier.height(4.dp))
    if (equivalent.isNotEmpty()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = equivalent,
                color = MaterialTheme.colorScheme.secondary,
            )
            if (onInputTypeClick != null) {
                Icon(
                    modifier = Modifier.size(headerSupportIconSize).clickable { onInputTypeClick() },
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = ""
                )
            }
        }
    }
    Text(
        text = error,
        color = MaterialTheme.colorScheme.error,
    )
}

class CryptoAmountTransformation(symbol: String, inputType: AmountInputType, color: Color) : AmountTransformation(inputType, symbol, color) {

    override fun transformText(text: AnnotatedString): AnnotatedString {
        val zeroValue = if (text.isEmpty()) "0" else ""
        val info = buildAnnotatedString {
            when (inputType) {
                AmountInputType.Crypto -> {
                    append(zeroValue)
                    append(" ")
                    append(symbol)
                    addStyle(
                        SpanStyle(color = color),
                        start = 0,
                        end = zeroValue.length,
                    )
                }
                AmountInputType.Fiat -> {
                    append(symbol)
                    append(" ")
                    append(zeroValue)
                    addStyle(
                        SpanStyle(color = color),
                        start = symbol.length,
                        end = symbol.length + zeroValue.length + 1,
                    )
                }
            }
        }
        return when (inputType) {
            AmountInputType.Crypto -> text + info
            AmountInputType.Fiat -> info + text
        }
    }

    override fun convertToOriginal(text: AnnotatedString, offset: Int): Int = when (inputType) {
        AmountInputType.Crypto -> if (offset > text.text.length) text.text.length else offset
        AmountInputType.Fiat -> if (offset > text.text.length) 0 else text.text.length
    }
}

abstract class AmountTransformation(
    val inputType: AmountInputType,
    val symbol: String,
    val color: Color,
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val result = transformText(text)
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return offset + when (inputType) {
                    AmountInputType.Crypto -> 0
                    AmountInputType.Fiat -> symbol.length + 1 + if (text.isEmpty()) 1 else 0
                }
            }

            override fun transformedToOriginal(offset: Int): Int = convertToOriginal(text, offset)
        }
        // Add formatting
        return TransformedText(result, offsetMapping)
    }

    abstract fun transformText(text: AnnotatedString): AnnotatedString

    abstract fun convertToOriginal(text: AnnotatedString, offset: Int): Int
}