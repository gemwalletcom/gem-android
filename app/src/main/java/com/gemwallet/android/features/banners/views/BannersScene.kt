package com.gemwallet.android.features.banners.views

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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.ui.components.AssetIcon
import com.gemwallet.android.ui.theme.padding12
import com.gemwallet.android.ui.theme.padding16
import com.gemwallet.android.ui.theme.padding8
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.BannerEvent

@Composable
fun BannersScene(
    asset: Asset?,
    isGlobal: Boolean = false,
    viewModel: BannersViewModel = hiltViewModel(),
) {
    LaunchedEffect(asset?.id?.toIdentifier(), isGlobal) {
        viewModel.init(asset, isGlobal)
    }
    val banners by viewModel.banners.collectAsStateWithLifecycle()
    val pageState = rememberPagerState { banners.size }
    HorizontalPager(pageState, pageSpacing = padding16) { page ->
        val banner = banners[page]
        ElevatedCard(
            modifier = Modifier.padding(padding16),
            onClick = {},
            colors = CardDefaults.elevatedCardColors().copy(containerColor = MaterialTheme.colorScheme.background)
        ) {
            val (title, description) = when (banner.event) {
                BannerEvent.stake -> Pair(
                    stringResource(R.string.banner_stake_title, asset?.name ?: ""),
                    stringResource(R.string.banner_stake_description, asset?.name ?: "")
                )
                BannerEvent.account_activation -> Pair(
                    stringResource(R.string.banner_account_activation_title, asset?.name ?: ""),
                    stringResource(R.string.banner_account_activation_description, asset?.name ?: "", "10 XRP")
                )
                BannerEvent.enable_notifications -> Pair(
                    stringResource(R.string.banner_enable_notifications_title, asset?.name ?: ""),
                    stringResource(R.string.banner_enable_notifications_description, )
                )
            }
            BannerText(
                title = title,
                subtitle = description,
                iconUrl = asset?.getIconUrl() ?: "android.resource://com.gemwallet.android/${R.drawable.brandmark}",

            ) { viewModel.onCancel(banner) }
        }
    }
}

@Composable
private fun BannerText(
    title: String,
    subtitle: String,
    iconUrl: String,
    onCancel: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.size(padding16))
        AssetIcon(modifier = Modifier.size(36.dp), iconUrl = iconUrl, placeholder = iconUrl, supportIcon = "")
        Spacer(modifier = Modifier.size(padding8))
        Box(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = padding12, end = 0.dp, bottom = padding12)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        if (title.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    modifier = Modifier.weight(1f, false),
                                    text = title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.W500),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            modifier = Modifier.padding(top = 0.dp, bottom = 2.dp),
                            text = subtitle,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                Spacer(modifier = Modifier.size(padding8))
                IconButton(onClick = onCancel) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "cancel_banner")
                }
            }
        }
    }
}