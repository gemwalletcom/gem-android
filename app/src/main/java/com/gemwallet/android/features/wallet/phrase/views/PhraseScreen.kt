package com.gemwallet.android.features.wallet.phrase.views

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.R
import com.gemwallet.android.features.wallet.WalletUIState
import com.gemwallet.android.features.wallet.WalletViewModel
import com.gemwallet.android.ui.components.LoadingScene
import com.gemwallet.android.ui.components.PhraseLayout
import com.gemwallet.android.ui.components.Scene
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.padding16

@Composable
fun PhraseScreen(
    walletId: String,
    onCancel: () -> Unit,
) {
    val viewModel: WalletViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(walletId) {
        viewModel.init(walletId, isPhrase = true)
        onDispose {  }
    }
    val clipboardManager = LocalClipboardManager.current
    val words = (state as? WalletUIState.Phrase)?.words

    if (words == null) {
        LoadingScene(title = stringResource(id = R.string.common_secret_phrase), onCancel)
        return
    }

    Scene(
        title = stringResource(id = R.string.common_secret_phrase),
        padding = PaddingValues(padding16),
        backHandle = true,
        onClose = onCancel,
    ) {
        Spacer16()
        PhraseLayout(words = words)
        Spacer16()
        TextButton(
            onClick = { clipboardManager.setText(AnnotatedString(words.joinToString(" "))) }
        ) {
            Text(text = stringResource(id = R.string.common_copy))
        }
    }
}