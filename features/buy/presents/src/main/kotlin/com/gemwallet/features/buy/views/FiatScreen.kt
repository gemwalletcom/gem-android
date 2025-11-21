package com.gemwallet.features.buy.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.clickable
import com.gemwallet.android.ui.components.progress.CircularProgressIndicator20
import com.gemwallet.android.ui.models.actions.CancelAction
import com.gemwallet.android.ui.theme.paddingSmall
import com.gemwallet.features.buy.viewmodels.FiatViewModel
import com.gemwallet.features.buy.viewmodels.models.BuyError
import com.gemwallet.features.buy.viewmodels.models.FiatSuggestion
import com.wallet.core.primitives.FiatQuoteType

@Composable
fun FiatScreen(
    cancelAction: CancelAction,
) {
    val viewModel: FiatViewModel = hiltViewModel()

    var isShowProgress by remember { mutableStateOf(false) }

    val type by viewModel.type.collectAsStateWithLifecycle()
    val suggestedAmounts by viewModel.suggestedAmounts.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val asset by viewModel.asset.collectAsStateWithLifecycle()
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
        onAmount = viewModel::updateAmount,
        onLotSelect = viewModel::updateAmount,
        onProviderSelect = viewModel::setProvider,
        onTypeClick = viewModel::setType,
        onBuy = {
            isShowProgress = true
            viewModel.getUrl {
                it?.let { uriHandler.openUri(it) }
                isShowProgress = false
            }
        }
    )

    if (isShowProgress) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(paddingSmall))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                CircularProgressIndicator20()
            }
        }
    }
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