package com.gemwallet.android.features.banners.views

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ext.chain
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.features.banners.viewmodels.BannersViewModel
import com.gemwallet.android.model.DownloadStatus
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.designsystem.Spacer16
import com.gemwallet.android.ui.components.designsystem.Spacer8
import com.gemwallet.android.ui.components.designsystem.padding12
import com.gemwallet.android.ui.components.designsystem.padding16
import com.gemwallet.android.ui.components.image.IconWithBadge
import com.gemwallet.android.ui.components.image.getIconUrl
import com.gemwallet.android.ui.components.progress.CircularProgressIndicator16
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Banner
import com.wallet.core.primitives.BannerEvent
import com.wallet.core.primitives.BannerState
import java.io.File

@Composable
fun BannersScene(
    asset: Asset?,
    onClick: (Banner) -> Unit,
    isGlobal: Boolean = false,
    viewModel: BannersViewModel = hiltViewModel(),
) {
    LaunchedEffect(asset?.id?.toIdentifier(), isGlobal) { viewModel.init(asset, isGlobal) }

    val banners by viewModel.banners.collectAsStateWithLifecycle()
    val downloadStatus by viewModel.downloadApkState.collectAsStateWithLifecycle()
    val pageState = rememberPagerState { banners.size }
    val context = LocalContext.current

    if(downloadStatus != null) {
        DownloadBanner(downloadStatus!!) { status ->
            when (status) {
                is DownloadStatus.Completed -> {
                        if (!context.packageManager.canRequestPackageInstalls()) {
                            requestInstallFromUnknownSources(context)
                        } else {
                            installApk(context, status.file)
                        }
                }

                is DownloadStatus.Error -> {
                    viewModel.onRetryDownload()
                }

                else -> Unit
            }
        }
    } else {
        HorizontalPager(pageState, pageSpacing = padding16) { page ->
            val banner = banners[page]
            Box(modifier = Modifier.clickable { onClick(banner) }) {
                val (title, description) = when (banner.event) {
                    BannerEvent.Stake -> Pair(
                        stringResource(R.string.banner_stake_title, asset?.name ?: ""),
                        stringResource(R.string.banner_stake_description, asset?.name ?: "")
                    )
                    BannerEvent.AccountActivation -> Pair(
                        stringResource(R.string.banner_account_activation_title, asset?.name ?: ""),
                        stringResource(R.string.banner_account_activation_description, asset?.name ?: "",
                            viewModel.getActivationFee(asset)
                        ))
                    BannerEvent.EnableNotifications -> Pair(
                        stringResource(R.string.banner_enable_notifications_title, asset?.name ?: ""),
                        stringResource(R.string.banner_enable_notifications_description)
                    )
                    BannerEvent.AccountBlockedMultiSignature -> Pair(
                        stringResource(R.string.common_warning),
                        stringResource(R.string.warnings_multi_signature_blocked, asset?.chain() ?: "")
                    )
                    BannerEvent.ActivateAsset -> TODO()
                }
                BannerText(
                    title = title,
                    subtitle = description,
                    iconUrl = asset?.getIconUrl()
                        ?: "android.resource://com.gemwallet.android/${R.drawable.brandmark}",
                    state = banner.state,
                ) { viewModel.onCancel(banner) }
            }
        }
    }
}

@Composable
private fun DownloadBanner(
    downloadStatus: DownloadStatus,
    onClick: (DownloadStatus) -> Unit,
) {
    //todo localize strings
    val (title, subtitle) =  when (downloadStatus) {
        is DownloadStatus.Started -> {
            "Downloading Apk" to "Starting download"
        }
        is DownloadStatus.Downloading -> {
            "Downloading Apk" to "${downloadStatus.progress}%"
        }
        is DownloadStatus.Completed -> {
             "Install now" to "New version download completed"
        }
        is DownloadStatus.Error -> {
             stringResource(R.string.common_try_again) to "Download error"
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick(downloadStatus) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer16()
        if(downloadStatus is DownloadStatus.Started || downloadStatus is DownloadStatus.Downloading) {
            CircularProgressIndicator16()
        } else {
            IconWithBadge(icon = "android.resource://com.gemwallet.android/${R.drawable.brandmark}", size = 36.dp)
        }
        Spacer16()
        Column(
            modifier = Modifier.weight(1f).padding(top = 14.dp, end = 0.dp, bottom = padding12),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.W500),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                modifier = Modifier.padding(top = 0.dp, bottom = 2.dp),
                text = subtitle,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun BannerText(
    title: String,
    subtitle: String,
    iconUrl: String,
    state: BannerState,
    onCancel: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer16()
        IconWithBadge(icon = iconUrl, placeholder = iconUrl, size = 36.dp)
        Spacer16()
        Column(
            modifier = Modifier.weight(1f).padding(top = 14.dp, end = 0.dp, bottom = padding12),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.W500),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                modifier = Modifier.padding(top = 0.dp, bottom = 2.dp),
                text = subtitle,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (state != BannerState.AlwaysActive) {
            Spacer8()
            IconButton(onClick = onCancel) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "cancel_banner")
            }
        } else {
            Spacer16()
        }
    }
}

private fun requestInstallFromUnknownSources(context: Context) {
    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
        data = Uri.parse("package:${context.packageName}")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

private fun installApk(context: Context, file: File) {
    val apkUri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val installIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(apkUri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(installIntent)
}

@Preview
@Composable
private fun BannerTextPreview() {
        Surface {
            BannerText(
                title = "Stake XRP",
                subtitle = "Earn rewards by staking your XRP",
                iconUrl = "android.resource://com.gemwallet.android/${R.drawable.brandmark}",
                state = BannerState.Cancelled,
                onCancel = {}
            )
        }

}

@Preview
@Composable
private fun BannerTextAlwaysActivePreview() {
        Surface {
            BannerText(
                title = "Account Blocked",
                subtitle = "Your account is blocked by multi-signature",
                iconUrl = "android.resource://com.gemwallet.android/${R.drawable.brandmark}",
                state = BannerState.AlwaysActive,
                onCancel = {}
            )
        }
}