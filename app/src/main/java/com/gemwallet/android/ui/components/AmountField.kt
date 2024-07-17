package com.gemwallet.android.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ColumnScope.AmountField(
    amount: String,
    assetSymbol: String,
    equivalent: String,
    error: String,
    onValueChange: (String) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.displaySmall,
    transformation: AmountTransformation = CryptoAmountTransformation(assetSymbol, MaterialTheme.colorScheme.secondary),
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
    )
    Spacer(modifier = Modifier.height(4.dp))
    if (equivalent.isNotEmpty()) {
        Text(
            text = equivalent,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
    Text(
        text = error,
        color = MaterialTheme.colorScheme.error,
    )
}

class CryptoAmountTransformation(symbol: String, color: Color) : AmountTransformation(symbol, color) {

    override fun transformText(text: AnnotatedString): AnnotatedString {
        return text + AnnotatedString(
            "${if (text.isEmpty()) "0" else ""} $symbol", SpanStyle(color = color)
        )
    }

    override fun convertToOriginal(text: AnnotatedString, offset: Int): Int =
        if (offset > text.text.length) text.text.length else offset
}

abstract class AmountTransformation(
    val symbol: String,
    val color: Color,
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val result = transformText(text)
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = offset

            override fun transformedToOriginal(offset: Int): Int = convertToOriginal(text, offset)
        }
        // Add formatting
        return TransformedText(result, offsetMapping)
    }

    abstract fun transformText(text: AnnotatedString): AnnotatedString

    abstract fun convertToOriginal(text: AnnotatedString, offset: Int): Int
}