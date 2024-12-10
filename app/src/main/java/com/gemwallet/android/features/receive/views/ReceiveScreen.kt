package com.gemwallet.android.features.receive.views

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.R
import com.gemwallet.android.features.receive.viewmodels.ReceiveViewModel
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.ui.components.AssetListItem
import com.gemwallet.android.ui.components.CenterEllipsisText
import com.gemwallet.android.ui.components.LoadingScene
import com.gemwallet.android.ui.components.buttons.MainActionButton
import com.gemwallet.android.ui.components.designsystem.padding8
import com.gemwallet.android.ui.components.qr_code.rememberQRCodePainter
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.AssetInfoUIModel
import com.gemwallet.android.ui.theme.WalletTheme

@Composable
fun ReceiveScreen(onCancel: () -> Unit) {
    val viewModel: ReceiveViewModel = hiltViewModel()
    val assetInfo by viewModel.asset.collectAsStateWithLifecycle()

    if (assetInfo != null) {
        ReceiveScene(assetInfo, viewModel::setVisible, onCancel)
    } else {
        LoadingScene(title = stringResource(R.string.wallet_receive), onCancel)
    }
}

@Composable
private fun ReceiveScene(
    assetInfo: AssetInfo?,
    onCopy: () -> Unit,
    onCancel: () -> Unit,
) {
    assetInfo ?: return
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val shareTitle = stringResource(id = R.string.common_share)

    val onShare = fun () {
        val type = "text/plain"
        val subject = "${assetInfo.owner.chain}\n${assetInfo.asset.symbol}"

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = type
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, assetInfo.owner.address)

        ContextCompat.startActivity(
            context,
            Intent.createChooser(intent, shareTitle),
            null
        )
    }

    val onCopyClick = fun () {
        onCopy()
        clipboardManager.setText(AnnotatedString(assetInfo.owner.address))
    }

    Scene(
        title = stringResource(id = R.string.receive_title, assetInfo.asset.symbol),
        onClose = onCancel,
        actions = {
            IconButton(onShare) {
                Icon(Icons.Default.Share, "")
            }
        }
    ) {
        if (assetInfo.owner.address.isEmpty()) {
            return@Scene
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 36.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.weight(1F))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(16.dp),
                        clip = false,
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        MaterialTheme.colorScheme.scrim
                            .copy(alpha = 0.3f)
                            .compositeOver(MaterialTheme.colorScheme.background)
                    ),
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                ) {
                    Image(
                        modifier = Modifier
                            .widthIn(100.dp, 400.dp)
                            .heightIn(100.dp, 400.dp)
                            .padding(12.dp),
                        painter = rememberQRCodePainter(
                            content = assetInfo.owner.address,
                            cacheName = "${assetInfo.owner.chain.string}_${assetInfo.owner.address}",
                            size = 300.dp
                        ),
                        contentDescription = "Receive QR",
                        contentScale = ContentScale.FillWidth
                    )
                }

                AssetListItem(
                    modifier = Modifier.height(74.dp),
                    uiModel = AssetInfoUIModel(assetInfo),
                    support = {
                        CenterEllipsisText(
                            text = assetInfo.owner.address,
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    badge = if (assetInfo.asset.symbol == assetInfo.asset.name) null else assetInfo.asset.symbol,
                    dividerShowed = false,
                    trailing = {
                        Button(
                            modifier = Modifier,
                            colors = ButtonDefaults.buttonColors()
                                .copy(containerColor = MaterialTheme.colorScheme.scrim),
                            contentPadding = PaddingValues(padding8),
                            onClick = onCopyClick
                        ) {
                            Text(
                                text = stringResource(id = R.string.common_copy),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W400),
                            )
                        }
                    },
                )
            }
            Spacer(modifier = Modifier.weight(1F))
            MainActionButton(
                title = stringResource(id = R.string.common_share),
                onClick = onShare
            )
        }
    }
}

@Preview
@Composable
fun PreviewReceiveScreen() {
    WalletTheme {
        ReceiveScene(
            null,
            onCancel = {},
            onCopy = {},
        )
    }
}