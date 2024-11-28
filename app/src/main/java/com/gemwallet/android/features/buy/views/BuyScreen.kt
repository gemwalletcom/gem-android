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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.R
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.type
import com.gemwallet.android.features.buy.models.BuyError
import com.gemwallet.android.features.buy.models.BuyUIState
import com.gemwallet.android.features.buy.viewmodels.BuyViewModel
import com.gemwallet.android.ui.components.image.getIcon
import com.gemwallet.android.ui.components.AmountField
import com.gemwallet.android.ui.components.AssetListItem
import com.gemwallet.android.ui.components.CellEntity
import com.gemwallet.android.ui.components.Container
import com.gemwallet.android.ui.components.FatalStateScene
import com.gemwallet.android.ui.components.ListItemSupportText
import com.gemwallet.android.ui.components.Table
import com.gemwallet.android.ui.components.buttons.MainActionButton
import com.gemwallet.android.ui.components.designsystem.Spacer16
import com.gemwallet.android.ui.components.designsystem.padding8
import com.gemwallet.android.ui.components.designsystem.trailingIcon20
import com.gemwallet.android.ui.components.image.AsyncImage
import com.gemwallet.android.ui.components.open
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.theme.WalletTheme
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.FiatProvider

@Composable
fun BuyScreen(
    assetId: AssetId,
    onCancel: () -> Unit,
    viewModel: BuyViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(assetId) {
        viewModel.init(assetId)
    }
    when (state) {
        is BuyUIState.Fatal -> FatalStateScene(
            title = stringResource(id = R.string.buy_title),
            message = (state as BuyUIState.Fatal).message,
            onCancel = onCancel,
        )
        is BuyUIState.Idle -> {
            val success = state as BuyUIState.Idle
            Idle(
                state = success,
                onCancel = onCancel,
                fiatAmount = viewModel.amount,
                onAmount = viewModel::updateAmount,
                onLotSelect = {
                    viewModel.updateAmount(it.toInt().toString())
                },
                onProviderSelect = viewModel::setProvider
            )
        }
    }
}

@Composable
private fun Idle(
    state: BuyUIState.Idle,
    fiatAmount: String,
    onCancel: () -> Unit,
    onLotSelect: (Double) -> Unit,
    onAmount: (String) -> Unit,
    onProviderSelect: (FiatProvider) -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val isShowProviders = remember { mutableStateOf(false) }
    Scene(
        title = stringResource(id = R.string.buy_title, state.asset?.name ?: ""),
        onClose = onCancel,
        mainAction = {
            MainActionButton(
                title = stringResource(id = R.string.common_continue),
                enabled = state.isAvailable(),
                onClick = { uriHandler.open(state.redirectUrl ?: "") }
            )
        }
    ) {
        Spacer16()
        AmountField(
            amount = fiatAmount,
            assetSymbol = "$",
            equivalent = if (state.isAvailable()) state.cryptoAmount else " ",
            error = "",
            onValueChange = onAmount,
            textStyle = MaterialTheme.typography.displayMedium,
            onNext = { }
        )
        if (state.asset != null) {
            Container {
                AssetListItem(
                    modifier = Modifier.height(74.dp),
                    uiModel = state.asset,
                    support = if (state.asset.asset.id.type() == AssetSubtype.NATIVE) {
                        null
                    } else {
                        { ListItemSupportText(state.asset.asset.id.chain.asset().name) }
                    },
                    badge = if (state.asset.symbol == state.asset.name) null else state.asset.symbol,
                    dividerShowed = false,
                    trailing = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LotButton("$100", 100.0, onLotSelect)
                            Spacer16()
                            LotButton("$150", 150.0, onLotSelect)
                        }
                    },
                )
            }
        }

        if (state.isAvailable()) {
            Table(
                items = listOf(
                    CellEntity(
                        label = stringResource(id = R.string.common_provider),
                        data = state.currentProvider?.provider?.name ?: "",
                        action = { isShowProviders.value = true },
                        trailing = {
                            AsyncImage(
                                model = state.currentProvider?.provider?.getIcon(),
                                size = trailingIcon20,
                            )
                        }
                    ),
                    CellEntity(
                        label = stringResource(id = R.string.buy_rate),
                        data = state.currentProvider?.rate ?: "",
                    )
                )
            )
        } else {
            if (state.isQuoteLoading) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.Center),
                        strokeWidth = 1.dp,
                    )
                }
            } else if (state.error != null) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    textAlign = TextAlign.Center,
                    text = state.error.mapError(),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }

    ProviderList(isShow = isShowProviders, providers = state.providers, onProviderSelect)
}

@Composable
fun LotButton(title: String, value: Double, onLotClick: (Double) -> Unit) {
    Button(
        modifier = Modifier,
        colors = ButtonDefaults.buttonColors().copy(containerColor = MaterialTheme.colorScheme.scrim),
        contentPadding = PaddingValues(padding8),
        onClick = { onLotClick(value) }
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W400),
        )
    }
}

@Composable
fun BuyError.mapError() = when (this) {
    BuyError.MinimumAmount -> stringResource(
        id = R.string.transfer_minimum_amount,
        "20$"
    )
    BuyError.QuoteNotAvailable -> stringResource(id = R.string.buy_no_results)
    BuyError.ValueIncorrect -> stringResource(id = R.string.errors_invalid_amount)
}

@Composable
@Preview
fun PreviewBuyUI() {
    WalletTheme {
        Idle(
            state = BuyUIState.Idle(),
            fiatAmount = "50",
            onCancel = {},
            onLotSelect = {},
            onProviderSelect = {},
            onAmount = {},
        )
    }
}
