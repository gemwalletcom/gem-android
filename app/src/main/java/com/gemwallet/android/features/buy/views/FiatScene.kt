package com.gemwallet.android.features.buy.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gemwallet.android.features.buy.models.BuyFiatProviderUIModel
import com.gemwallet.android.features.buy.models.FiatSceneState
import com.gemwallet.android.features.buy.models.FiatSuggestion
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
fun BuyScene(
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
    onTypeClick: (FiatQuoteType) -> Unit,
) {
    asset ?: return
    val uriHandler = LocalUriHandler.current
    val isShowProviders = remember { mutableStateOf(false) }

    Scene(
        title = {
            if (asset.assetInfo.metadata?.isSellEnabled == true) {
                SingleChoiceSegmentedButtonRow {
                    FiatQuoteType.entries.forEachIndexed { index, entry ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = FiatQuoteType.entries.size
                            ),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = MaterialTheme.colorScheme.primary,
                                activeContentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            icon = {},
                            onClick = { onTypeClick(entry) },
                            selected = entry == type,
                            label = {
                                Text(
                                    stringResource(
                                        when (entry) {
                                            FiatQuoteType.Buy -> R.string.buy_title
                                            FiatQuoteType.Sell -> R.string.sell_title
                                        }, ""
                                    )
                                )
                            }
                        )
                    }
                }
            } else {
                Text(
                    modifier = Modifier,
                    text = stringResource(id = R.string.buy_title, asset.asset.name),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        onClose = { cancelAction() },
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
            equivalent = if (state == null && type == FiatQuoteType.Buy) selectedProvider?.cryptoFormatted ?: " " else " ",
            error = "",
            onValueChange = onAmount,
            textStyle = MaterialTheme.typography.displayMedium,
            onNext = { }
        )
        Container {
            AssetListItem(
                modifier = Modifier.Companion.height(74.dp),
                uiModel = asset,
                support = { ListItemSupportText(asset.cryptoFormatted) },
                dividerShowed = false,
                trailing = {
                    Row(verticalAlignment = Alignment.Companion.CenterVertically) {
                        suggestedAmounts.forEach { suggestion ->
                            when (suggestion) {
                                FiatSuggestion.RandomAmount -> RandomGradientButton(
                                    onClick = { onLotSelect(FiatSuggestion.RandomAmount) }
                                )
                                is FiatSuggestion.SuggestionAmount,
                                is FiatSuggestion.SuggestionPercent -> {
                                    LotButton(suggestion, onLotSelect)
                                    Spacer8()
                                }
                                FiatSuggestion.MaxAmount -> {
                                    LotButton(suggestion, onLotSelect)
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
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .padding(20.dp),
                    textAlign = TextAlign.Companion.Center,
                    text = state.error?.mapError(type) ?: "",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            FiatSceneState.Loading -> {
                Box(
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.Companion
                            .size(30.dp)
                            .align(Alignment.Companion.Center),
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