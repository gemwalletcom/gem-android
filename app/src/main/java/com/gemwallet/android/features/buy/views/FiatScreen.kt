package com.gemwallet.android.features.buy.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.features.buy.models.BuyError
import com.gemwallet.android.features.buy.models.BuyFiatProviderUIModel
import com.gemwallet.android.features.buy.viewmodels.FiatSceneState
import com.gemwallet.android.features.buy.viewmodels.FiatSuggestion
import com.gemwallet.android.features.buy.viewmodels.FiatViewModel
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.AmountField
import com.gemwallet.android.ui.components.AssetListItem
import com.gemwallet.android.ui.components.CellEntity
import com.gemwallet.android.ui.components.Container
import com.gemwallet.android.ui.components.Table
import com.gemwallet.android.ui.components.buttons.MainActionButton
import com.gemwallet.android.ui.components.buttons.RandomGradientButton
import com.gemwallet.android.ui.components.designsystem.Spacer16
import com.gemwallet.android.ui.components.designsystem.Spacer8
import com.gemwallet.android.ui.components.designsystem.padding8
import com.gemwallet.android.ui.components.designsystem.trailingIconMedium
import com.gemwallet.android.ui.components.image.AsyncImage
import com.gemwallet.android.ui.components.image.getFiatProviderIcon
import com.gemwallet.android.ui.components.list_item.ListItemSupportText
import com.gemwallet.android.ui.components.open
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.AssetInfoUIModel
import com.gemwallet.android.ui.models.actions.CancelAction
import com.wallet.core.primitives.FiatProvider
import com.wallet.core.primitives.FiatQuoteType

@Composable
fun FiatScreen(
    cancelAction: CancelAction,
) {
    val viewModel: FiatViewModel = hiltViewModel()

    val state by viewModel.state.collectAsStateWithLifecycle()
    val asset by viewModel.asset.collectAsStateWithLifecycle()
    val amount by viewModel.amount.collectAsStateWithLifecycle()
    val providers by viewModel.providers.collectAsStateWithLifecycle()
    val selectedProvider by viewModel.selectedProvider.collectAsStateWithLifecycle()

    BuyScene(
        asset = asset,
        state = state,
        type = viewModel.type,
        providers = providers,
        selectedProvider = selectedProvider,
        cancelAction = cancelAction,
        fiatAmount = amount,
        suggestedAmounts = viewModel.suggestedAmounts,
        onAmount = viewModel::updateAmount,
        onLotSelect = viewModel::updateAmount,
        onProviderSelect = viewModel::setProvider,
    )
}

@Composable
private fun BuyScene(
    asset: AssetInfoUIModel?,
    state: FiatSceneState?,
    type: FiatQuoteType,
    providers: List<BuyFiatProviderUIModel>,
    selectedProvider: BuyFiatProviderUIModel?,
    fiatAmount: String,
    suggestedAmounts: List<FiatSuggestion>,
    cancelAction: CancelAction,
    onLotSelect: (FiatSuggestion) -> Unit,
    onAmount: (String) -> Unit,
    onProviderSelect: (FiatProvider) -> Unit,
) {
    asset ?: return
    val uriHandler = LocalUriHandler.current
    val isShowProviders = remember { mutableStateOf(false) }
    Scene(
        title = stringResource(id = R.string.buy_title, asset.asset.name),
        onClose = {
            cancelAction.invoke()
        },
        mainAction = {
            MainActionButton(
                title = stringResource(id = R.string.common_continue),
                enabled = state == null,
                onClick = { uriHandler.open(selectedProvider?.redirectUrl ?: "") }
            )
        }
    ) {
        Spacer16()
        AmountField(
            amount = fiatAmount,
            assetSymbol = if (type == FiatQuoteType.Buy) "$" else asset.symbol,
            equivalent = if (state == null) selectedProvider?.cryptoFormatted ?: " " else " ",
            error = "",
            onValueChange = onAmount,
            textStyle = MaterialTheme.typography.displayMedium,
            onNext = { }
        )
        Container {
            AssetListItem(
                modifier = Modifier.height(74.dp),
                uiModel = asset,
                support = {
                    ListItemSupportText(asset.cryptoFormatted)
                },
                dividerShowed = false,
                trailing = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        suggestedAmounts.forEach { suggestion ->
                            when (suggestion) {
                                FiatSuggestion.RandomAmount -> RandomGradientButton(
                                    onClick = { onLotSelect(FiatSuggestion.RandomAmount) }
                                )
                                is FiatSuggestion.SuggestionAmount,
                                is FiatSuggestion.SuggestionPercent -> {
                                    LotButton(
                                        suggestion, onLotSelect
                                    )
                                    Spacer8()
                                }

                                FiatSuggestion.MaxAmount -> {
                                    LotButton(
                                        suggestion, onLotSelect
                                    )
                                }
                            }
                        }
                    }
                },
            )
        }

        when (state) {
            is FiatSceneState.Error -> {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    textAlign = TextAlign.Center,
                    text = state.error?.mapError() ?: "",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            FiatSceneState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.Center),
                        strokeWidth = 1.dp,
                    )
                }
            }

            null -> if (selectedProvider != null) {
                Table(
                    items = listOf(
                        CellEntity(
                            label = stringResource(id = R.string.common_provider),
                            data = selectedProvider.provider.name,
                            action = { isShowProviders.value = true },
                            trailing = {
                                AsyncImage(
                                    model = selectedProvider.provider.getFiatProviderIcon(),
                                    size = trailingIconMedium,
                                )
                            }
                        ),
                        CellEntity(
                            label = stringResource(id = R.string.buy_rate),
                            data = selectedProvider.rate,
                        )
                    )
                )
            }
        }
    }

    ProviderList(isShow = isShowProviders, providers = providers, onProviderSelect)
}

@Composable
fun LotButton(fiatSuggestion: FiatSuggestion, onLotClick: (FiatSuggestion) -> Unit) {
    Button(
        modifier = Modifier,
        colors = ButtonDefaults.buttonColors()
            .copy(containerColor = MaterialTheme.colorScheme.scrim),
        contentPadding = PaddingValues(padding8),
        onClick = { onLotClick(fiatSuggestion) }
    ) {
        Text(
            text = fiatSuggestion.text,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W400),
        )
    }
}

@Composable
fun BuyError.mapError() = when (this) {
    BuyError.MinimumAmount -> stringResource(
        id = R.string.transfer_minimum_amount,
        "${FiatViewModel.MIN_FIAT_AMOUNT}$"
    )

    BuyError.QuoteNotAvailable -> stringResource(id = R.string.buy_no_results)
    BuyError.ValueIncorrect -> stringResource(id = R.string.errors_invalid_amount)
}