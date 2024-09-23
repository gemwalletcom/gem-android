package com.gemwallet.android.features.banners.views

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.R
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.features.banners.viewmodels.BannersViewModel
import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.ui.components.ListItem
import com.gemwallet.android.ui.components.ListItemTitle
import com.gemwallet.android.ui.theme.padding16
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
        Card(modifier = Modifier) {
            ListItem(
                iconUrl = asset?.getIconUrl()
                    ?: "android.resource://com.gemwallet.android/${R.drawable.brandmark}"
            ) {
                when (banner.event) {
                    BannerEvent.stake -> ListItemTitle(
                        title = stringResource(R.string.banner_stake_title, asset?.name ?: ""),
                        subtitle = stringResource(R.string.banner_stake_description, asset?.name ?: "")
                    )
                    BannerEvent.account_activation -> ListItemTitle(
                        title = stringResource(R.string.banner_account_activation_title, asset?.name ?: ""),
                        subtitle = stringResource(R.string.banner_account_activation_description, "", "")
                    )
                    BannerEvent.enable_notifications -> ListItemTitle(
                        title = stringResource(R.string.banner_enable_notifications_title, asset?.name ?: ""),
                        subtitle = stringResource(R.string.banner_enable_notifications_description, "", "")
                    )
                }
            }
        }
    }
}