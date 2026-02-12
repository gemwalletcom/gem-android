package com.gemwallet.android.features.wallet.presents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.features.wallet.viewmodels.WalletViewModel

@Composable
fun WalletNavScreen(
    onPhraseShow: (String) -> Unit,
    onBoard: () -> Unit,
    onCancel: () -> Unit,
) {
    val viewModel: WalletViewModel = hiltViewModel()
    val wallet by viewModel.wallet.collectAsStateWithLifecycle()

    WalletScene(
        wallet = wallet,
        onWalletName = viewModel::setWalletName,
        onPhraseShow = onPhraseShow,
        onDelete = { viewModel.delete(onBoard, onCancel) },
        onCancel = onCancel,
    )
}