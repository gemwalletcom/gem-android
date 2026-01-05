package com.gemwallet.features.buy.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
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
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.paddingDefault
import com.gemwallet.android.ui.theme.paddingHalfSmall
import com.gemwallet.android.ui.theme.paddingSmall
import com.gemwallet.features.buy.viewmodels.models.BuyFiatProviderUIModel
import com.gemwallet.features.buy.viewmodels.models.FiatSceneState
import com.gemwallet.features.buy.viewmodels.models.FiatSuggestion
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.FiatProvider
import com.wallet.core.primitives.FiatQuoteType

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BuyScene(
    asset: AssetInfoUIModel?,
    state: FiatSceneState?,
    type: FiatQuoteType,
    providers: List<BuyFiatProviderUIModel>,
    selectedProvider: BuyFiatProviderUIModel?,
    fiatAmount: String,
    suggestedAmounts: List<FiatSuggestion>,
    urlLoading: State<Boolean>,
    cancelAction: CancelAction,
    onLotSelect: (FiatSuggestion) -> Unit,
    onAmount: (String) -> Unit,
    onProviderSelect: (FiatProvider) -> Unit,
    onTypeClick: (FiatQuoteType) -> Unit,
    onBuy: () -> Unit
) {
    asset ?: return
    val isShowProviders = remember { mutableStateOf(false) }

    Scene(
        titleContent = {
            if (asset.assetInfo.metadata?.isSellEnabled == true && asset.assetInfo.balance.balance.hasAvailable()) {
                Row(
                    modifier = Modifier.padding(horizontal = paddingDefault),
                    horizontalArrangement = Arrangement.spacedBy(paddingHalfSmall),
                ) {
                    FiatQuoteType.entries.forEachIndexed { index, item ->
                        ToggleButton(
                            modifier = Modifier.semantics { role = Role.RadioButton },
                            checked = item == type,
                            onCheckedChange = { onTypeClick(item) },
                            colors = ToggleButtonDefaults.toggleButtonColors()
                                .copy(containerColor = MaterialTheme.colorScheme.background),
                            shapes = when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                    .copy(checkedShape = ButtonGroupDefaults.connectedLeadingButtonShape)

                                FiatQuoteType.entries.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                    .copy(checkedShape = ButtonGroupDefaults.connectedTrailingButtonShape)

                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                    .copy(checkedShape = ShapeDefaults.Small)
                            },
                        ) {
                            Text(
                                stringResource(
                                    when (item) {
                                        FiatQuoteType.Buy -> R.string.buy_title
                                        FiatQuoteType.Sell -> R.string.sell_title
                                    }, ""
                                ),
                            )
                        }
                    }
                }
            } else {
                Text(
                    modifier = Modifier,
                    text = stringResource(R.string.buy_title, asset.asset.name),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        onClose = { cancelAction() },
        mainAction = {
            MainActionButton(
                title = stringResource(R.string.common_continue),
                enabled = state == null,
                loading = urlLoading.value,
                onClick = onBuy,
            )
        }
    ) {
        Spacer16()
        AmountField(
            amount = fiatAmount,
            assetSymbol = "$",
            currency = Currency.USD,
            equivalent = selectedProvider?.cryptoFormatted ?: " ",
            error = "",
            onValueChange = onAmount,
            textStyle = MaterialTheme.typography.displayMedium,
            onNext = { },
        )
        Spacer16()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(paddingSmall)
        ) {
            suggestedAmounts.forEach { suggestion ->
                when (suggestion) {
                    FiatSuggestion.RandomAmount -> RandomGradientButton(
                        onClick = { onLotSelect(FiatSuggestion.RandomAmount) }
                    )
                    is FiatSuggestion.SuggestionAmount -> {
                        LotButton(suggestion, onLotSelect)
                    }
                }
            }
        }
        AssetListItem(
            asset = asset,
            listPosition = ListPosition.Single,
            support = { ListItemSupportText(asset.cryptoFormatted) },
            trailing = {},
        )

        when (state) {
            is FiatSceneState.Error -> {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                    text = state.error?.mapError(type, asset.asset) ?: "",
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