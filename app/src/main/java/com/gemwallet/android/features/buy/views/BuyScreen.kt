package com.gemwallet.android.features.buy.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.R
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.type
import com.gemwallet.android.features.buy.viewmodels.BuyError
import com.gemwallet.android.features.buy.viewmodels.BuyUIState
import com.gemwallet.android.features.buy.viewmodels.BuyViewModel
import com.gemwallet.android.interactors.getIcon
import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.ui.components.AmountField
import com.gemwallet.android.ui.components.AssetListItem
import com.gemwallet.android.ui.components.AsyncImage
import com.gemwallet.android.ui.components.CellEntity
import com.gemwallet.android.ui.components.Container
import com.gemwallet.android.ui.components.FatalStateScene
import com.gemwallet.android.ui.components.MainActionButton
import com.gemwallet.android.ui.components.Scene
import com.gemwallet.android.ui.components.Table
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.WalletTheme
import com.gemwallet.android.ui.theme.padding8
import com.gemwallet.android.ui.theme.trailingIcon20
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.FiatProvider

@Composable
fun BuyScreen(
    assetId: AssetId,
    onCancel: () -> Unit,
) {
    val viewModel: BuyViewModel = hiltViewModel()
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
        is BuyUIState.Success -> {
            val success = state as BuyUIState.Success
            UI(
                isLoading = success.isQuoteLoading,
                error = success.error,
                asset = success.asset,
                title = success.title,
                cryptoAmount = success.cryptoAmount,
                fiatAmount = viewModel.amount,
                provider = success.selectProvider,
                providers = success.providers,
                redirectUri = success.redirectUrl,
                onCancel = onCancel,
                onAmount = viewModel::updateAmount,
                onLotSelect = {
                    viewModel.updateAmount(it.toInt().toString())
                },
                onProviderSelect = viewModel::setProvider
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UI(
    isLoading: Boolean,
    error: BuyError?,
    asset: Asset?,
    title: String,
    cryptoAmount: String,
    fiatAmount: String,
    provider: BuyUIState.Provider?,
    providers: List<BuyUIState.Provider>,
    redirectUri: String?,
    onCancel: () -> Unit,
    onLotSelect: (Double) -> Unit,
    onAmount: (String) -> Unit,
    onProviderSelect: (FiatProvider) -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    var isShowProviders by remember {
        mutableStateOf(false)
    }
    Scene(
        title = stringResource(id = R.string.buy_title, title),
        onClose = onCancel,
        mainAction = {
            if (!redirectUri.isNullOrEmpty()) {
                MainActionButton(
                    title = stringResource(id = R.string.common_continue),
                    onClick = { uriHandler.openUri(redirectUri) }
                )
            }
        }
    ) {
        AmountField(
            amount = fiatAmount,
            assetSymbol = "$",
            equivalent = cryptoAmount,
            error = "",
            onValueChange = onAmount,
            onNext = { }
        )
        Spacer16()
        if (asset != null) {
            Container {
                AssetListItem(
                    modifier = Modifier.height(74.dp),
                    chain = asset.id.chain,
                    title = asset.name,
                    support = if (asset.id.type() == AssetSubtype.NATIVE) {
                        null
                    } else {
                        asset.id.chain.asset().name
                    },
                    assetType = asset.type,
                    iconUrl = asset.getIconUrl(),
                    badge = asset.symbol,
                    dividerShowed = false,
                    trailing = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LotButton(lot = BuyUIState.BuyLot("$250", 250.0), onLotSelect)
                            Spacer16()
                            LotButton(lot = BuyUIState.BuyLot("$500", 500.0), onLotSelect)
                        }
                    },
                )
            }
        }
        Spacer16()
        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth()) {
                CircularProgressIndicator(modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.Center))
            }
        } else if (error != null) {
            Container {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    text = when (error) {
                        BuyError.MinimumAmount -> stringResource(
                            id = R.string.transfer_minimum_amount,
                            "20$"
                        )
                        BuyError.QuoteNotAvailable -> stringResource(id = R.string.buy_no_results)
                        BuyError.ValueIncorrect -> stringResource(id = R.string.amount_error_invalid_amount)
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
            }

        } else {
            if (provider != null) {
                Table(
                    items = listOf(
                        CellEntity(
                            label = stringResource(id = R.string.common_provider),
                            data = provider.provider.name,
                            action = { isShowProviders = true },
                            trailing = {
                                AsyncImage(
                                    modifier = Modifier.size(trailingIcon20),
                                    model = provider.provider.getIcon(),
                                    contentDescription = ""
                                )
                            }
                        ),
                        CellEntity(
                            label = stringResource(id = R.string.buy_rate),
                            data = provider.rate,
                        )
                    )
                )
            }
        }
    }

    if (isShowProviders) {
        ModalBottomSheet(
            onDismissRequest = { isShowProviders = false },
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            Table(
                items = providers.map {
                    CellEntity(
                        icon = "file:///android_asset/fiat/${it.provider.name.lowercase()}.png",
                        label = it.provider.name,
                        data = it.cryptoAmount
                    ) {
                        onProviderSelect(it.provider)
                        isShowProviders = false
                    }
                }
            )
        }
    }
}

@Composable
fun RowScope.LotButton(lot: BuyUIState.BuyLot, onLotClick: (Double) -> Unit) {
    Button(
        modifier = Modifier,
        colors = ButtonDefaults.buttonColors().copy(containerColor = MaterialTheme.colorScheme.scrim),
        contentPadding = PaddingValues(padding8),
        onClick = { onLotClick(lot.value) }
    ) {
        Text(
            text = lot.title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W400),
        )
    }
}

@Composable
@Preview
fun PreviewBuyUI() {
    WalletTheme {
        UI(
            isLoading = false,
            error = null,
            title = "BNB",
            asset = null,
            cryptoAmount = "~1.5054",
            fiatAmount = "1500",
            provider = BuyUIState.Provider(
                FiatProvider("FooProvider", ""),
                cryptoAmount = "0,888ETH",
                rate = "1BNB ~ $332.15"
            ),
            providers = emptyList(),
            redirectUri = null,
            onCancel = {},
            onLotSelect = {},
            onProviderSelect = {},
            onAmount = {},
        )
    }
}
