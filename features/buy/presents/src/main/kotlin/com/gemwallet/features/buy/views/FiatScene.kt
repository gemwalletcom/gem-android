package com.gemwallet.features.buy.views

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gemwallet.android.domains.asset.getFiatProviderIcon
import com.gemwallet.android.model.hasAvailable
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.buttons.MainActionButton
import com.gemwallet.android.ui.components.buttons.RandomGradientButton
import com.gemwallet.android.ui.components.fields.AmountField
import com.gemwallet.android.ui.components.list_item.AssetInfoUIModel
import com.gemwallet.android.ui.components.list_item.AssetListItem
import com.gemwallet.android.ui.components.list_item.ListItemSupportText
import com.gemwallet.android.ui.components.list_item.property.DataBadgeChevron
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.models.actions.CancelAction
import com.gemwallet.android.ui.open
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.Spacer8
import com.gemwallet.features.buy.viewmodels.models.BuyFiatProviderUIModel
import com.gemwallet.features.buy.viewmodels.models.FiatSceneState
import com.gemwallet.features.buy.viewmodels.models.FiatSuggestion
import com.wallet.core.primitives.Currency
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
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val isShowProviders = remember { mutableStateOf(false) }

    Scene(
        titleContent = {
            if (asset.assetInfo.metadata?.isSellEnabled == true && asset.assetInfo.balance.balance.hasAvailable()) {
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
                onClick = { uriHandler.open(context, selectedProvider?.redirectUrl ?: "") }
            )
        }
    ) {
        Spacer16()
        AmountField(
            amount = fiatAmount,
            assetSymbol = if (type == FiatQuoteType.Buy) "$" else asset.symbol,
            currency = Currency.USD,
            equivalent = if (state == null && type == FiatQuoteType.Buy) selectedProvider?.cryptoFormatted ?: " " else " ",
            error = "",
            onValueChange = onAmount,
            textStyle = MaterialTheme.typography.displayMedium,
            onNext = { },
        )
        AssetListItem(
            modifier = Modifier.Companion.height(74.dp),
            asset = asset,
            listPosition = ListPosition.Single,
            support = { ListItemSupportText(asset.cryptoFormatted) },
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
                PropertyItem(
                    modifier = Modifier.clickable(enabled = providers.size > 1) { isShowProviders.value = true },
                    title = { PropertyTitleText(R.string.common_provider) },
                    data = {
                        PropertyDataText(
                            selectedProvider.provider.name,
                            badge = {
                                DataBadgeChevron(
                                    icon = selectedProvider.provider.getFiatProviderIcon(),
                                    isShowChevron = providers.size > 1
                                )
                            }
                        )
                    },
                    listPosition = ListPosition.First,
                )
                PropertyItem(
                    title = R.string.buy_rate,
                    data = selectedProvider.rate,
                    listPosition = ListPosition.Last,
                )
            }
        }
    }

    ProviderList(isShow = isShowProviders, providers = providers, onProviderSelect)
}