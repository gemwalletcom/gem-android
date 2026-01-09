package com.gemwallet.features.receive.presents

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.domains.asset.chain
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.type
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.buttons.MainActionButton
import com.gemwallet.android.ui.components.clickable
import com.gemwallet.android.ui.components.clipboard.setPlainText
import com.gemwallet.android.ui.components.list_head.HeaderIcon
import com.gemwallet.android.ui.components.parseMarkdownToAnnotatedString
import com.gemwallet.android.ui.components.screen.LoadingScene
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.theme.isSmallScreen
import com.gemwallet.android.ui.theme.paddingDefault
import com.gemwallet.android.ui.theme.paddingHalfSmall
import com.gemwallet.android.ui.theme.paddingSmall
import com.gemwallet.features.receive.presents.components.rememberQRCodePainter
import com.gemwallet.features.receive.viewmodels.ReceiveViewModel
import com.wallet.core.primitives.AssetSubtype

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
    val context = LocalContext.current
    val clipboardManager = LocalClipboard.current.nativeClipboard
    val shareTitle = stringResource(id = R.string.common_share)
    val imageSize = if (isSmallScreen()) 220.dp else 300.dp
    val imagePadding = if (isSmallScreen()) paddingSmall else paddingDefault

    val onShare = fun () {
        val type = "text/plain"
        val subject = "${assetInfo.owner?.chain}\n${assetInfo.asset.symbol}"

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = type
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, assetInfo.owner?.address)

        context.startActivity(Intent.createChooser(intent, shareTitle))
    }

    val onCopyClick = fun () {
        onCopy()
        clipboardManager.setPlainText(context, assetInfo.owner?.address ?: "")
    }

    Scene(
        title = stringResource(id = R.string.receive_title, ""),
        onClose = onCancel,
        actions = {
            IconButton(onShare) {
                Icon(Icons.Default.Share, "")
            }
        },
        mainAction = {
            MainActionButton(onClick = onCopyClick) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(paddingHalfSmall)
                ) {
                    Icon(Icons.Default.ContentCopy, "copy")
                    Text(stringResource(id = R.string.common_copy))
                }
            }
        }
    ) {
        if (assetInfo.owner?.address.isNullOrEmpty()) {
            return@Scene
        }
        LazyColumn (
            modifier = Modifier
                .fillMaxSize()
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(imagePadding)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(paddingSmall)
                        ) {
                            HeaderIcon(assetInfo.asset)
                            Row(horizontalArrangement = Arrangement.spacedBy(paddingSmall)) {
                                Text(
                                    text = assetInfo.asset.name,
                                    overflow = TextOverflow.MiddleEllipsis,
                                    maxLines = 1,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = assetInfo.asset.symbol,
                                    overflow = TextOverflow.MiddleEllipsis,
                                    maxLines = 1,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal),
                                    color = MaterialTheme.colorScheme.secondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        ElevatedCard(
                            modifier = Modifier.width(imageSize),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White,
                                contentColor = Color.White,
                            )
                        ) {
                            Image(
                                modifier = Modifier
                                    .widthIn(100.dp, imageSize)
                                    .heightIn(100.dp, imageSize)
                                    .padding(imagePadding)
                                    .clickable(onCopyClick),
                                painter = rememberQRCodePainter(
                                    content = assetInfo.owner?.address ?: "",
                                    cacheName = "${assetInfo.owner?.chain?.string}_${assetInfo.owner?.address}",
                                    size = 300.dp
                                ),
                                contentDescription = "Receive QR",
                                contentScale = ContentScale.FillWidth
                            )
                            Text(
                                modifier = Modifier
                                    .width(imageSize)
                                    .padding(horizontal = imagePadding)
                                    .clickable(onCopyClick),
                                text = assetInfo.owner?.address ?: "",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Spacer(modifier = Modifier.size(imagePadding))
                        }
                        Text(
                            modifier = Modifier.width(imageSize),
                            text = parseMarkdownToAnnotatedString(
                                stringResource(
                                    R.string.receive_warning,
                                    "**${assetInfo.asset.symbol}**",
                                    "**${ assetInfo.asset.chain.asset().name + if (assetInfo.asset.id.type() == AssetSubtype.TOKEN) " (${assetInfo.asset.type})" else "" }**"
                                )
                            ),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.size(it.calculateBottomPadding()))
            }
        }
    }
}

