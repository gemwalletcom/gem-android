package com.gemwallet.android.features.receive.views

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.R
import com.gemwallet.android.features.receive.model.ReceiveScreenModel
import com.gemwallet.android.features.receive.viewmodels.ReceiveViewModel
import com.gemwallet.android.ui.components.FieldBottomAction
import com.gemwallet.android.ui.components.LoadingScene
import com.gemwallet.android.ui.components.Scene
import com.gemwallet.android.ui.rememberQRCodePainter
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.WalletTheme
import com.wallet.core.primitives.Chain

@Composable
fun ReceiveScreen(onCancel: () -> Unit) {
    val viewModel: ReceiveViewModel = hiltViewModel()
    val uiState by viewModel.screenModel.collectAsStateWithLifecycle()
    UI(uiState, viewModel::setVisible, onCancel)
}

@Composable
private fun UI(
    state: ReceiveScreenModel?,
    onCopy: () -> Unit,
    onCancel: () -> Unit,
) {
    if (state == null) {
        LoadingScene(title = stringResource(R.string.wallet_receive), onCancel)
        return
    }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val shareTitle = stringResource(id = R.string.common_share)

    val onShare = fun () {
        val type = "text/plain"
        val subject = "${state.chain}\n${state.assetSymbol}"

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = type
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, state.address)

        ContextCompat.startActivity(
            context,
            Intent.createChooser(intent, shareTitle),
            null
        )
    }

    val onCopyClick = fun () {
        onCopy()
        clipboardManager.setText(AnnotatedString(state.address))
    }

    Scene(
        title = stringResource(id = R.string.receive_title, state.assetSymbol),
        onClose = onCancel,
        actions = {
            IconButton(onCopyClick) {
                Icon(Icons.Default.ContentCopy, "")
            }
            IconButton(onShare) {
                Icon(Icons.Default.Share, "")
            }
        }
    ) {
        if (state.address.isEmpty() || state.chain == null) {
            return@Scene
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ElevatedCard(
                        modifier = Modifier,
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White,
                            contentColor = Color.White,
                        )
                    ) {
                        Image(
                            modifier = Modifier
                                .widthIn(100.dp, 400.dp)
                                .heightIn(100.dp, 400.dp)
                                .padding(12.dp),
                            painter = rememberQRCodePainter(
                                content = state.address,
                                cacheName = "${state.chain.string}_${state.address}",
                                size = 300.dp
                            ),
                            contentDescription = "Receive QR",
                            contentScale = ContentScale.FillWidth
                        )
                    }
                    Spacer16()
                    Text(
                        text = state.walletName,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.address,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row {
                        FieldBottomAction(
                            modifier = Modifier.weight(1f),
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "paste",
                            text = stringResource(id = R.string.common_copy),
                            onClick = onCopyClick,
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        FieldBottomAction(
                            modifier = Modifier.weight(1f),
                            imageVector = Icons.Default.Share,
                            contentDescription = "share",
                            text = stringResource(id = R.string.common_share),
                            onClick = onShare
                        )
                    }
                    Spacer16()
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewReceiveScreen() {
    WalletTheme {
        UI(
            ReceiveScreenModel(
                address = "0xverylong foo foo address very long foo address",
                walletName = "Foo wallet",
                assetSymbol = "FOO",
                chain = Chain.Bitcoin,
            ),
            onCancel = {},
            onCopy = {},
        )
    }
}