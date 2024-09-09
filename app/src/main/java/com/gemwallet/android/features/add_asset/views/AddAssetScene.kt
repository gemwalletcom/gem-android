package com.gemwallet.android.features.add_asset.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gemwallet.android.R
import com.gemwallet.android.features.add_asset.models.AddAssetError
import com.gemwallet.android.features.add_asset.models.AddAssetUIState
import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.ui.components.AddressChainField
import com.gemwallet.android.ui.components.AsyncImage
import com.gemwallet.android.ui.components.CellEntity
import com.gemwallet.android.ui.components.CircularProgressIndicator16
import com.gemwallet.android.ui.components.MainActionButton
import com.gemwallet.android.ui.components.Scene
import com.gemwallet.android.ui.components.Table
import com.gemwallet.android.ui.theme.padding16

@Composable
fun AddAssetScene(
    uiState: AddAssetUIState,
    onQuery: (String) -> Unit,
    onScan: () -> Unit,
    onAddAsset: () -> Unit,
    onChainSelect: () -> Unit,
    onCancel: () -> Unit,
) {
    var inputState by remember(uiState.address) {
        mutableStateOf(uiState.address)
    }
    var inputStateError by remember(uiState.address) {
        mutableStateOf(uiState.error)
    }

    Scene(
        title = stringResource(id = R.string.assets_add_custom_token),
        mainAction = {
            MainActionButton(
                title = stringResource(id = R.string.assets_add_custom_token),
                enabled = uiState.error == AddAssetError.None && uiState.asset != null,
                onClick = onAddAsset,
            )
        },
        onClose = onCancel,
    ) {
        Table(
            items = listOf(
                CellEntity(
                    label = stringResource(id = R.string.transfer_network),
                    data = uiState.networkTitle,
                    trailing = {
                        AsyncImage(
                            modifier = Modifier.size(20.dp),
                            model = uiState.networkIcon,
                            contentDescription = "asset_icon"
                        )
                    },
                    action = onChainSelect
                )
            )
        )
        Column(
            modifier = Modifier.padding(padding16)
        ) {
            AddressChainField(
                chain = uiState.chain,
                label = "Contract Address",
                value = inputState,
                searchName = false,
                onValueChange = { input, _ ->
                    inputState = input
                    inputStateError = AddAssetError.None
                    onQuery(input)
                },
                onQrScanner = onScan,
            )
        }
        if (uiState.isLoading) {
            Box {
                CircularProgressIndicator16(modifier = Modifier.align(Alignment.Center))
            }
        }
        if (uiState.error != AddAssetError.None) {
            Box {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(padding16),
                    text = stringResource(id = R.string.errors_token_unable_fetch_token_information, uiState.address),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            }
        }
        if (uiState.asset != null) {
            Table(
                items = listOf(
                    CellEntity(
                        label = stringResource(id = R.string.asset_name),
                        data = uiState.asset.name,
                        trailing = {
                            AsyncImage(
                                modifier = Modifier.size(32.dp),
                                model = uiState.asset.getIconUrl(),
                                placeholderText = uiState.asset.symbol,
                                contentDescription = "asset_icon"
                            )
                        },
                    ),
                    CellEntity(
                        label = stringResource(id = R.string.asset_symbol),
                        data = uiState.asset.symbol,
                    ),
                    CellEntity(
                        label = stringResource(id = R.string.asset_decimals),
                        data = uiState.asset.decimals.toString(),
                    ),
                )
            )
        }
    }
}