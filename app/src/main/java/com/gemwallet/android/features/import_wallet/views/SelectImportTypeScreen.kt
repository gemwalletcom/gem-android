package com.gemwallet.android.features.import_wallet.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.R
import com.gemwallet.android.features.asset_select.components.SearchBar
import com.gemwallet.android.features.import_wallet.viewmodels.ChainUIState
import com.gemwallet.android.features.import_wallet.viewmodels.ImportType
import com.gemwallet.android.features.import_wallet.viewmodels.SelectImportTypeViewModel
import com.gemwallet.android.ui.components.ChainItem
import com.gemwallet.android.ui.components.Container
import com.gemwallet.android.ui.components.Scene
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.padding16
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.WalletType

@Composable
fun SelectImportTypeScreen(
    onClose: () -> Unit,
    onSelect: (ImportType) -> Unit,
) {
    val viewModel: SelectImportTypeViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SelectImportTypeScene(
        chains = uiState.chains,
        chainFilter = viewModel.chainFilter,
        onSelect = onSelect,
        onClose = onClose,
    )
}

@Composable
private fun SelectImportTypeScene(
    chains: List<ChainUIState>,
    chainFilter: TextFieldState,
    onSelect: (ImportType) -> Unit,
    onClose: () -> Unit,
) {

    Scene(
        title = stringResource(id = R.string.wallet_import_title),
        onClose = onClose,
    ) {
        LazyColumn(modifier = Modifier) {
            item {
                SearchBar(query = chainFilter, modifier = Modifier.padding(padding16))
            }
            item {
                Container {
                    ChainItem(
                        title = stringResource(id = R.string.wallet_multicoin),
                        icon = "android.resource://com.gemwallet.android/drawable/ic_splash",
                        dividerShowed = false,
                    ) {
                        onSelect(ImportType(WalletType.multicoin))
                    }
                }
                Spacer16()
            }
            items(chains.size) {
                ChainItem(
                    chain = chains[it].chain,
                    title = chains[it].title,
                    icon = chains[it].icon,
                ) {
                    onSelect(ImportType(WalletType.single, chains[it].chain))
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewChainSelectScreen() {
    MaterialTheme {
        Column {
            SelectImportTypeScene(
                chains = listOf(
                    ChainUIState(title = "foo Chain #1", icon = "", chain = Chain.Bitcoin),
                    ChainUIState(title = "foo Chain #2", icon = "", chain = Chain.Bitcoin),
                    ChainUIState(title = "foo Chain #3", icon = "", chain = Chain.Bitcoin),
                    ChainUIState(title = "foo Chain #4", icon = "", chain = Chain.Bitcoin),
                    ChainUIState(title = "foo Chain #5", icon = "", chain = Chain.Bitcoin),
                    ChainUIState(title = "foo Chain #6", icon = "", chain = Chain.Bitcoin),
                ),
                chainFilter = rememberTextFieldState(),
                onClose = {},
                onSelect = {}
            )
        }
    }
}