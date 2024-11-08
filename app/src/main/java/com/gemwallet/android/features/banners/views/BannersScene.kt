package com.gemwallet.android.features.banners.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.R
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.features.banners.viewmodels.BannersViewModel
import com.gemwallet.android.interactors.chain
import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.ui.components.AssetIcon
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.Spacer8
import com.gemwallet.android.ui.theme.padding12
import com.gemwallet.android.ui.theme.padding16
import com.gemwallet.android.ui.theme.padding8
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Banner
import com.wallet.core.primitives.BannerEvent
import com.wallet.core.primitives.BannerState

@Composable
fun BannersScene(
    asset: Asset?,
    onClick: (Banner) -> Unit,
    isGlobal: Boolean = false,
    viewModel: BannersViewModel = hiltViewModel(),
) {
    LaunchedEffect(asset?.id?.toIdentifier(), isGlobal) { viewModel.init(asset, isGlobal) }
    val banners by viewModel.banners.collectAsStateWithLifecycle()
    val pageState = rememberPagerState { banners.size }
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
                    stringResource(R.string.banner_account_activation_description, asset?.name ?: "", "10 XRP")
                )
                BannerEvent.EnableNotifications -> Pair(
                    stringResource(R.string.banner_enable_notifications_title, asset?.name ?: ""),
                    stringResource(R.string.banner_enable_notifications_description)
                )
                BannerEvent.AccountBlockedMultiSignature -> Pair(
                    stringResource(R.string.common_warning),
                    stringResource(R.string.warnings_multi_signature_blocked, asset?.chain() ?: "")
                )
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
        AssetIcon(
            modifier = Modifier.size(36.dp),
            iconUrl = iconUrl,
            placeholder = iconUrl,
            supportIcon = ""
        )
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