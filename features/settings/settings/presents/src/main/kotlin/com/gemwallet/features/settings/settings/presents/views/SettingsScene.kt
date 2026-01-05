@file:OptIn(ExperimentalMaterial3Api::class)

package com.gemwallet.features.settings.settings.presents.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ui.BuildConfig
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.PushRequest
import com.gemwallet.android.ui.components.list_item.LinkItem
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.open
import com.gemwallet.features.settings.settings.viewmodels.SettingsViewModel
import uniffi.gemstone.Config
import uniffi.gemstone.SocialUrl

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsScene(
    onSecurity: () -> Unit,
    onBridges: () -> Unit,
    onDevelop: () -> Unit,
    onWallets: () -> Unit,
    onAboutUs: () -> Unit,
    onPriceAlerts: () -> Unit,
    onSupport: () -> Unit,
    onPreferences: () -> Unit,
    onPerpetual: () -> Unit,
    onReferral: () -> Unit,
    scrollState: ScrollState = rememberScrollState()
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRewardsAvailable by viewModel.isRewardsAvailable.collectAsStateWithLifecycle()
    val pushEnabled by viewModel.pushEnabled.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val supportState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isShowDevelopEnable by remember { mutableStateOf(false) }

    val uriHandler = LocalUriHandler.current
    var requestPushGrant by remember { mutableStateOf<(() -> Unit)?>(null) }
    Scene(
        title = stringResource(id = R.string.settings_title),
        mainActionPadding = PaddingValues(0.dp),
        navigationBarPadding = false,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            LinkItem(
                title = stringResource(id = R.string.wallets_title),
                icon = R.drawable.settings_wallets,
                listPosition = ListPosition.First,
                onClick = onWallets
            )
            LinkItem(
                title = stringResource(id = R.string.settings_security),
                icon = R.drawable.settings_security,
                listPosition = ListPosition.Last,
                onClick = onSecurity
            )
            if (BuildConfig.DEBUG) {
                LinkItem(
                    title = stringResource(id = R.string.perpetuals_title),
                    icon = R.drawable.settings_security,
                    listPosition = ListPosition.Last,
                    onClick = onPerpetual
                )
            }
            LinkItem(title = stringResource(id = R.string.settings_preferences_title), icon = R.drawable.settings_preferences, listPosition = ListPosition.Single) {
                onPreferences()
            }
            if (viewModel.isNotificationsAvailable()) {
                LinkItem(
                    title = stringResource(id = R.string.settings_notifications_title),
                    icon = R.drawable.settings_notifications,
                    listPosition = ListPosition.First,
                    trailingContent = @Composable {
                        Switch(
                            checked = pushEnabled,
                            onCheckedChange = {
                                if (it) requestPushGrant = viewModel::notificationEnable else viewModel.notificationEnable()
                            }
                        )
                    },
                    onClick = {}
                )
                LinkItem(
                    title = stringResource(id = R.string.settings_price_alerts_title),
                    icon = R.drawable.settings_pricealert,
                    listPosition = ListPosition.Last,
                    onClick = onPriceAlerts
                )
            }

            if (isRewardsAvailable) {
                LinkItem(
                    title = stringResource(id = R.string.rewards_title),
                    icon = R.drawable.settings_wallets,
                    listPosition = ListPosition.Single
                ) {
                    onReferral()
                }
            }
            LinkItem(title = stringResource(id = R.string.wallet_connect_title), icon = R.drawable.settings_wc, listPosition = ListPosition.Single) {
                onBridges()
            }

            SubheaderItem(R.string.settings_community)
            LinkItem(title = stringResource(id = R.string.social_x), icon = R.drawable.twitter, listPosition = ListPosition.First) {
                uriHandler.open(context, Config().getSocialUrl(SocialUrl.X) ?: "")
            }
            LinkItem(title = stringResource(id = R.string.social_discord), icon = R.drawable.discord) {
                uriHandler.open(context, Config().getSocialUrl(SocialUrl.DISCORD) ?: "")
            }
            LinkItem(title = stringResource(id = R.string.social_telegram), icon = R.drawable.telegram) {
                uriHandler.open(context, Config().getSocialUrl(SocialUrl.TELEGRAM) ?: "")
            }
            LinkItem(title = stringResource(id = R.string.social_github), icon = R.drawable.github) {
                uriHandler.open(context, Config().getSocialUrl(SocialUrl.GIT_HUB) ?: "")
            }
            LinkItem(title = stringResource(id = R.string.social_youtube), icon = R.drawable.youtube, listPosition = ListPosition.Last,) {
                uriHandler.open(context, Config().getSocialUrl(SocialUrl.YOU_TUBE) ?: "")
            }

            LinkItem(
                title = stringResource(id = R.string.settings_support),
                icon = R.drawable.settings_support,
                listPosition = ListPosition.First,
            ) {
                if (!pushEnabled) {
                    requestPushGrant = {
                        viewModel.notificationEnable()
                        onSupport()
                    }
                } else {
                    onSupport()
                }
            }
            Box(modifier = Modifier.fillMaxWidth()) {
                LinkItem(
                    title = stringResource(id = R.string.settings_aboutus),
                    icon = R.drawable.settings_about_us,
                    listPosition = ListPosition.Last,
                    onClick = onAboutUs,
                    onLongClick = { isShowDevelopEnable = true }
                )
                DropdownMenu(
                    isShowDevelopEnable, { isShowDevelopEnable = false },
                    containerColor = MaterialTheme.colorScheme.background,
                ) {
                    DropdownMenuItem(
                        text = { Text("Enable develop") },
                        onClick = {
                            isShowDevelopEnable =  false
                            viewModel.developEnable()
                        }
                    )
                }
            }
            if (uiState.developEnabled) {
                LinkItem(title = stringResource(id = R.string.settings_developer), icon = R.drawable.settings_developer, listPosition = ListPosition.Single,) {
                    onDevelop()
                }
            }
            Spacer(modifier = Modifier.size(it.calculateBottomPadding()))
        }
    }

    requestPushGrant?.let {
        PushRequest(it) { requestPushGrant = null }
    }
}