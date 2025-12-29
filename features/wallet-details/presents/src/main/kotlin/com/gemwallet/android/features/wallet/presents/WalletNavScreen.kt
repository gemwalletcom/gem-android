package com.gemwallet.android.features.wallet.presents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.features.wallet.viewmodels.WalletUIState
import com.gemwallet.android.features.wallet.viewmodels.WalletViewModel
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.screen.FatalStateScene

@Composable
fun WalletNavScreen(
    onAuthRequest: (() -> Unit) -> Unit,
    onPhraseShow: (String) -> Unit,
    onBoard: () -> Unit,
    onCancel: () -> Unit,
) {
    val viewModel: WalletViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val wallet by viewModel.wallet.collectAsStateWithLifecycle()

    when (state) {
        is WalletUIState.Success -> {
            WalletScene(
                wallet = wallet,
                onAuthRequest = onAuthRequest,
                onWalletName = viewModel::setWalletName,
                onPhraseShow = { wallet?.id?.let { onPhraseShow(it) } },
                onDelete = { viewModel.delete(onBoard, onCancel) },
                onCancel = onCancel,
            )
        }
        is WalletUIState.Fatal -> {
            FatalStateScene(
                title = stringResource(id = R.string.common_wallet),
                message = (state as WalletUIState.Fatal).message,
                onCancel = onCancel,
            )
        }
    }
}