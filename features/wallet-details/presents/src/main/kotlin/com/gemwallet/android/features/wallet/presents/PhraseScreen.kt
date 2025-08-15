package com.gemwallet.android.features.wallet.presents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.features.wallet.viewmodels.WalletViewModel
import com.gemwallet.android.ui.DisableScreenShooting
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.clipboard.setPlainText
import com.gemwallet.android.ui.components.screen.LoadingScene
import com.gemwallet.android.ui.components.screen.PhraseLayout
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.Spacer8
import com.gemwallet.android.ui.theme.paddingDefault
import com.wallet.core.primitives.WalletType

@Composable
fun PhraseScreen(
    onCancel: () -> Unit,
) {
    DisableScreenShooting()

    val viewModel: WalletViewModel = hiltViewModel()
    val wallet by viewModel.wallet.collectAsStateWithLifecycle()
    val phrase by viewModel.phrase.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val clipboardManager = LocalClipboard.current.nativeClipboard
    val walletType = wallet?.type
    if (phrase == null) {
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
                .background(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                )
                .padding(16.dp)
            ,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.secret_phrase_do_not_share_title),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Spacer8()
            Text(
                text = stringResource(id = R.string.secret_phrase_do_not_share_description),
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }
        Spacer16()
        Spacer16()
        when (walletType) {
            WalletType.multicoin,
            WalletType.single -> PhraseLayout(words = phrase?.split(" ") ?: emptyList())
            WalletType.private_key -> Text(
                text = phrase ?: "",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            else -> {}
        }

        Spacer16()
        TextButton(
            onClick = { clipboardManager.setPlainText(context, phrase ?: "") }
        ) {
            Text(text = stringResource(id = R.string.common_copy))
        }
    }
}