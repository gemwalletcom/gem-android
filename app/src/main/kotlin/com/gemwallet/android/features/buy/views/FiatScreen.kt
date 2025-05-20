package com.gemwallet.android.features.buy.views

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.features.buy.models.BuyError
import com.gemwallet.android.features.buy.models.FiatSuggestion
import com.gemwallet.android.features.buy.viewmodels.FiatViewModel
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.designsystem.padding8
import com.gemwallet.android.ui.models.actions.CancelAction
import com.wallet.core.primitives.FiatQuoteType

@Composable
fun FiatScreen(
    cancelAction: CancelAction,
) {
    val viewModel: FiatViewModel = hiltViewModel()

    val type by viewModel.type.collectAsStateWithLifecycle()
    val suggestedAmounts by viewModel.suggestedAmounts.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val asset by viewModel.asset.collectAsStateWithLifecycle()
    val amount by viewModel.amount.collectAsStateWithLifecycle()
    val providers by viewModel.providers.collectAsStateWithLifecycle()
    val selectedProvider by viewModel.selectedProvider.collectAsStateWithLifecycle()

    BuyScene(
        asset = asset,
        state = state,
        type = type,
        providers = providers,
        selectedProvider = selectedProvider,
        cancelAction = cancelAction,
        fiatAmount = amount,
        suggestedAmounts = suggestedAmounts,
        onAmount = viewModel::updateAmount,
        onLotSelect = viewModel::updateAmount,
        onProviderSelect = viewModel::setProvider,
        onTypeClick = viewModel::setType,
    )
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
fun BuyError.mapError(type: FiatQuoteType) = when (this) {
    BuyError.MinimumAmount -> stringResource(id = R.string.transfer_minimum_amount, "${FiatViewModel.MIN_FIAT_AMOUNT}$")
    BuyError.QuoteNotAvailable -> stringResource(id = R.string.buy_no_results)
    BuyError.ValueIncorrect -> stringResource(id = R.string.errors_invalid_amount)
    BuyError.EmptyAmount -> stringResource(
        R.string.input_enter_amount_to, when (type) {
            FiatQuoteType.Buy -> stringResource(R.string.buy_title, "")
            FiatQuoteType.Sell -> stringResource(R.string.sell_title, "")
        }
    )
}