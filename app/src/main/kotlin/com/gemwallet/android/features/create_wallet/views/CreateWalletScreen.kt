package com.gemwallet.android.features.create_wallet.views

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.features.create_wallet.viewmodels.CreateWalletViewModel
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.screen.PhraseLayout
import com.gemwallet.android.ui.components.buttons.MainActionButton
import com.gemwallet.android.ui.components.clipboard.setPlainText
import com.gemwallet.android.ui.components.designsystem.Spacer16
import com.gemwallet.android.ui.components.designsystem.padding16
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.theme.WalletTheme

@Composable
fun CreateWalletScreen(
    onCancel: () -> Unit,
    onCreated: () -> Unit,
) {
    val viewModel: CreateWalletViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(uiState.isShowSafeMessage) {
        viewModel.handleCreateDismiss()
    }

    AnimatedContent(
        targetState = uiState.isShowSafeMessage,
        transitionSpec = {
            if (uiState.isShowSafeMessage) {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(350)
                ) togetherWith slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(350)
                )
            } else {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                ) togetherWith slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                )
            }
        },
        label = "phrase"
    ) { state ->
        when (state) {
            true -> CheckPhrase(
                words = uiState.data,
                onDone = { viewModel.handleCreate(onCreated) },
                onCancel = viewModel::handleCreateDismiss,
            )
            false -> UI(
                generatedNameIndex = uiState.generatedNameIndex,
                data = uiState.data,
                dataError = uiState.dataError,
                onCreate = viewModel::handleReadyToCreate,
                onCancel = onCancel,
            )
        }
    }
    if (uiState.loading) {
        Dialog(
            onDismissRequest = {  },
            DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Box(
                contentAlignment= Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun UI(
    generatedNameIndex: Int,
    data: List<String>,
    dataError: String,
    onCreate: (String) -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboard.current.nativeClipboard
    val name = stringResource(id = R.string.wallet_default_name, generatedNameIndex)
    Scene(
        title = stringResource(id = R.string.wallet_new_title),
        onClose = onCancel,
        padding = PaddingValues(padding16),
        mainAction = {
            MainActionButton(
                title = stringResource(id = R.string.common_continue),
                onClick = { onCreate(name) }
            )
        }
    ) {
        if (dataError.isNotEmpty()) {
            Text(text = dataError)
        } else {
            Text(
                text = stringResource(id = R.string.secret_phrase_save_phrase_safely),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer16()
            PhraseLayout(words = data)
        }
        Spacer16()
        TextButton(onClick = { clipboardManager.setPlainText(context, data.joinToString(" ")) }) {
            Text(text = stringResource(id = R.string.common_copy))
        }
    }
}


@Composable
@Preview
@Preview(name = "Pixel 2", device = Devices.PIXEL_2)
@Preview(name = "Pixel 3", device = Devices.PIXEL_3)
@Preview(name = "Pixel 4", device = Devices.PIXEL_4)
@Preview(name = "Nexus 5", device = Devices.NEXUS_5)
@Preview(name = "Nexus 7", device = Devices.NEXUS_7)
fun PreviewCreateUI() {
    WalletTheme {
        Column {
            UI(
                generatedNameIndex = 2,
                data = listOf(
                    "cinnamon", "two", "three", "cinnamon", "five", "six",
                    "seven", "eight", "cinnamon", "ten", "eleven", "twelve"
                ),
                dataError = "",
                onCreate = {},
                onCancel = {},
            )
        }
    }
}