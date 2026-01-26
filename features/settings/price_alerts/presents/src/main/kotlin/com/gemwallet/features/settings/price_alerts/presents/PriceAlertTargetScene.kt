package com.gemwallet.features.settings.price_alerts.presents

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.TabsBar
import com.gemwallet.android.ui.components.buttons.MainActionButton
import com.gemwallet.android.ui.components.clickable
import com.gemwallet.android.ui.components.parseMarkdownToAnnotatedString
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.theme.WalletTheme
import com.gemwallet.android.ui.theme.paddingHalfSmall
import com.gemwallet.android.ui.theme.paddingLarge
import com.gemwallet.android.ui.theme.paddingSmall
import com.gemwallet.features.settings.price_alerts.viewmodels.models.PriceAlertTargetError
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.PriceAlertDirection
import com.wallet.core.primitives.PriceAlertNotificationType

private val tabs = listOf(
    PriceAlertNotificationType.Price,
    PriceAlertNotificationType.PricePercentChange,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PriceAlertTargetScene(
    value: TextFieldState = rememberTextFieldState(),
    type: PriceAlertNotificationType,
    direction: PriceAlertDirection,
    currency: Currency,
    currentPriceValue: Double,
    currentPriceFormatted: String,
    error: PriceAlertTargetError?,
    onType: (PriceAlertNotificationType) -> Unit,
    onDirection: (PriceAlertDirection) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Throwable) {}
    }

    Scene(
        titleContent = {
            TabsBar(
                tabs = tabs,
                selected = type,
                onSelect = {
                    onType(it)
                    value.clearText()
                }
            ) { item ->
                Text(
                    stringResource(
                        when (item) {
                            PriceAlertNotificationType.Price ->  R.string.asset_price
                            PriceAlertNotificationType.PricePercentChange -> R.string.common_percentage
                            PriceAlertNotificationType.Auto -> R.string.common_no
                        }
                    ),
                )
            }
        },
        mainAction = {
            MainActionButton(
                title = stringResource(R.string.transfer_confirm),
                enabled = error == null && value.text.isNotEmpty(),
                onClick = onConfirm,
            )
        },
        onClose = onCancel,
    ) {
        Spacer(modifier = Modifier.size(paddingLarge * 2))
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(paddingSmall),
        ) {
            item {
                Text(
                    text = when (type) {
                        PriceAlertNotificationType.Auto -> ""
                        PriceAlertNotificationType.Price -> {
                            val inputPrice = try {
                                value.text.toString().toDouble()
                            } catch (_: Throwable) { 0.0 }
                            when {
                                inputPrice == 0.0 -> stringResource(R.string.price_alerts_set_alert_set_target_price)
                                inputPrice < currentPriceValue -> stringResource(R.string.price_alerts_set_alert_price_under)
                                inputPrice > currentPriceValue -> stringResource(R.string.price_alerts_set_alert_price_over)
                                else -> stringResource(R.string.price_alerts_set_alert_set_target_price)
                            }
                        }

                        PriceAlertNotificationType.PricePercentChange -> when (direction) {
                            PriceAlertDirection.Up -> stringResource(R.string.price_alerts_set_alert_price_increases_by)
                            PriceAlertDirection.Down -> stringResource(R.string.price_alerts_set_alert_price_decreases_by)
                        }
                    },
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(paddingHalfSmall),
                ) {
                    Box(Modifier.weight(1f)) {
                        if (type == PriceAlertNotificationType.PricePercentChange) {
                            Icon(
                                modifier = Modifier.align(Alignment.CenterEnd).clickable {
                                    val direction = when (direction) {
                                        PriceAlertDirection.Up -> PriceAlertDirection.Down
                                        PriceAlertDirection.Down -> PriceAlertDirection.Up
                                    }
                                    onDirection(direction)
                                },
                                imageVector = when (direction) {
                                    PriceAlertDirection.Up -> Icons.Default.ArrowCircleUp
                                    PriceAlertDirection.Down -> Icons.Default.ArrowCircleDown
                                },
                                contentDescription = "",
                                tint = when (direction) {
                                    PriceAlertDirection.Up -> MaterialTheme.colorScheme.tertiary
                                    PriceAlertDirection.Down -> MaterialTheme.colorScheme.error
                                },
                            )
                        }
                    }
                    BasicTextField(
                        modifier = Modifier.width(IntrinsicSize.Min).focusRequester(focusRequester),
                        state = value,
                        lineLimits = TextFieldLineLimits.SingleLine,
                        textStyle = MaterialTheme.typography.displaySmall.copy(
                            textAlign = TextAlign.Center,
                            color = if (value.text.isEmpty()) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        interactionSource = interactionSource,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        outputTransformation = OutputTransformation {
                            if (this.length == 0) {
                                this.append("0")
                            }
                        }
                    )
                    Box(Modifier.weight(1f)) {
                        Text(
                            text = when (type) {
                                PriceAlertNotificationType.Auto -> ""
                                PriceAlertNotificationType.Price -> java.util.Currency.getInstance(currency.string).symbol
                                PriceAlertNotificationType.PricePercentChange -> "%"
                            },
                            style = MaterialTheme.typography.displaySmall,
                        )
                    }
                }
            }
            item {
                Text(
                    text = parseMarkdownToAnnotatedString("${stringResource(R.string.price_alerts_set_alert_current_price)} **$currentPriceFormatted**"),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            if (type == PriceAlertNotificationType.PricePercentChange) {
                val percentage = listOf("5", "10", "15")
                item {
                    TabsBar(tabs = percentage, selected = value.text, onSelect = { select -> value.edit { this.replace(0, this.length, select) } }) {
                        Text("$it%")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PriceAlertTargetScenePricePreview() {
    WalletTheme {
        PriceAlertTargetScene(
            value = rememberTextFieldState(""),
            direction = PriceAlertDirection.Up,
            type = PriceAlertNotificationType.Price,
            currency = Currency.USD,
            currentPriceFormatted = "901.8$",
            currentPriceValue = 901.8,
            error = null,
            onType = {},
            onDirection = {},
            onConfirm = {},
            onCancel = {},
        )
    }
}

@Preview
@Composable
fun PriceAlertTargetScenePercentagePreview() {
    WalletTheme {
        PriceAlertTargetScene(
            value = rememberTextFieldState(""),
            direction = PriceAlertDirection.Up,
            type = PriceAlertNotificationType.PricePercentChange,
            currency = Currency.USD,
            currentPriceFormatted = "901.8$",
            currentPriceValue = 901.8,
            error = null,
            onType = {},
            onDirection = {},
            onConfirm = {},
            onCancel = {},
        )
    }
}