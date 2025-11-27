package com.gemwallet.features.buy.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.clickable
import com.gemwallet.android.ui.models.actions.CancelAction
import com.gemwallet.android.ui.theme.paddingSmall
import com.gemwallet.features.buy.viewmodels.FiatViewModel
import com.gemwallet.features.buy.viewmodels.models.BuyError
import com.gemwallet.features.buy.viewmodels.models.FiatSuggestion
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.FiatQuoteType

@Composable
fun FiatScreen(
    cancelAction: CancelAction,
) {
    val viewModel: FiatViewModel = hiltViewModel()

    val urlLoading = remember { mutableStateOf(false) }

    val type by viewModel.type.collectAsStateWithLifecycle()
    val suggestedAmounts by viewModel.suggestedAmounts.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val asset by viewModel.assetInfoUIModel.collectAsStateWithLifecycle()
    val amount by viewModel.amount.collectAsStateWithLifecycle()
    val providers by viewModel.providers.collectAsStateWithLifecycle()
    val selectedProvider by viewModel.selectedProvider.collectAsStateWithLifecycle()

    val uriHandler = LocalUriHandler.current

    BuyScene(
        asset = asset,
        state = state,
        type = type,
        providers = providers,
        selectedProvider = selectedProvider,
        cancelAction = cancelAction,
        fiatAmount = amount,
        suggestedAmounts = suggestedAmounts,
        urlLoading = urlLoading,
        onAmount = viewModel::updateAmount,
        onLotSelect = viewModel::updateAmount,
        onProviderSelect = viewModel::setProvider,
        onTypeClick = viewModel::setType,
        onBuy = {
            urlLoading.value = true
            viewModel.getUrl {
                it?.let { uriHandler.openUri(it) }
                urlLoading.value = false
            }
        }
    )
}

@Composable
fun LotButton(fiatSuggestion: FiatSuggestion, onLotClick: (FiatSuggestion) -> Unit) {
        Text(
            modifier = Modifier
                .clip(RoundedCornerShape(paddingSmall))
                .clickable { onLotClick(fiatSuggestion) }
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingSmall)
            ,
            text = fiatSuggestion.text,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.W500),
        )
}

@Composable
fun BuyError.mapError(type: FiatQuoteType, asset: Asset) = when (this) {
    BuyError.MinimumAmount -> stringResource(id = R.string.transfer_minimum_amount, "${FiatViewModel.MIN_FIAT_AMOUNT}$")
    BuyError.QuoteNotAvailable -> stringResource(id = R.string.buy_no_results)
    BuyError.ValueIncorrect -> stringResource(id = R.string.errors_invalid_amount)
    BuyError.EmptyAmount -> stringResource(
        R.string.input_enter_amount_to, when (type) {
            FiatQuoteType.Buy -> stringResource(R.string.buy_title, "")
            FiatQuoteType.Sell -> stringResource(R.string.sell_title, "")
        }
    )

    BuyError.InsufficientBalance -> stringResource(R.string.transfer_insufficient_balance, "${asset.name} (${asset.symbol})")
}