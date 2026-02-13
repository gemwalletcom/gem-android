package com.gemwallet.android.features.wallet.presents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.features.wallet.viewmodels.WalletSecretDataViewModel
import com.gemwallet.android.ui.DisableScreenShooting
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.clipboard.setPlainText
import com.gemwallet.android.ui.components.screen.LoadingScene
import com.gemwallet.android.ui.components.screen.PhraseLayout
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.theme.paddingDefault

@Composable
fun WalletSecretDataNavScreen(
    onCancel: () -> Unit,
    viewModel: WalletSecretDataViewModel = hiltViewModel()
) {
    DisableScreenShooting()

    val value by viewModel.data.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val clipboardManager = LocalClipboard.current.nativeClipboard


    if (value == null) {
        LoadingScene(title = stringResource(id = R.string.common_secret_phrase), onCancel)
        return
    }

    Scene(
        title = stringResource(id = R.string.common_secret_phrase),
        padding = PaddingValues(paddingDefault),
        backHandle = true,
        onClose = onCancel,
    ) {
        Column(
            modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(paddingDefault)
        ) {
            Column(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(16.dp)
                ,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.secret_phrase_do_not_share_title),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(id = R.string.secret_phrase_do_not_share_description),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            }

            value?.privateKey()?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
            } ?: PhraseLayout(words = value?.phrase() ?: emptyList())

            TextButton(
                onClick = { clipboardManager.setPlainText(context, value.toString(), true) }
            ) {
                Text(text = stringResource(id = R.string.common_copy))
            }
        }
    }
}