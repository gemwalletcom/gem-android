package com.gemwallet.features.settings.settings.presents.views

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.PushRequest
import com.gemwallet.android.ui.components.list_item.LinkItem
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.open
import com.gemwallet.features.settings.currency.presents.components.emojiFlags
import com.gemwallet.features.settings.settings.viewmodels.SettingsViewModel
import uniffi.gemstone.Config
import uniffi.gemstone.DocsUrl
import uniffi.gemstone.PublicUrl
import uniffi.gemstone.SocialUrl
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsScene(
    onSecurity: () -> Unit,
    onBridges: () -> Unit,
    onDevelop: () -> Unit,
    onCurrencies: () -> Unit,
    onWallets: () -> Unit,
    onAboutUs: () -> Unit,
    onNetworks: () -> Unit,
    onPriceAlerts: () -> Unit,
    scrollState: ScrollState = rememberScrollState()
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pushEnabled by viewModel.pushEnabled.collectAsStateWithLifecycle()
    val context = LocalContext.current
//    val reviewManager = remember { ReviewManager() }
//    val version = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//        context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
//    } else {
//        context.packageManager.getPackageInfo(context.packageName, 0)
//    }.versionName
    var isShowDevelopEnable by remember { mutableStateOf(false) }

    val uriHandler = LocalUriHandler.current
    var requestPushGrant by remember {
        mutableStateOf(false)
    }
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
            if (viewModel.isNotificationsAvailable()) {
                LinkItem(
                    title = stringResource(id = R.string.settings_notifications_title),
                    icon = R.drawable.settings_notifications,
                    listPosition = ListPosition.First,
                    trailingContent = @Composable {
                        Switch(
                            checked = pushEnabled,
                            onCheckedChange = {
                                if (it) requestPushGrant = true else viewModel.notificationEnable()
                            }
                        )
                    },
                    onClick = {}
                )
                LinkItem(
                    title = stringResource(id = R.string.settings_price_alerts_title),
                    icon = R.drawable.settings_pricealert,
                    listPosition = ListPosition.Last,
//                    trailingContent = @Composable {
//                        Switch(
//                            checked = uiState.pushEnabled,
//                            onCheckedChange = {
//                                if (it) requestPushGrant = true else viewModel.notificationEnable()
//                            }
//                        )
//                    },
                    onClick = onPriceAlerts
                )
            }

            LinkItem(
                title = stringResource(R.string.settings_currency),
                icon = R.drawable.settings_currency,
                listPosition = ListPosition.First,
                supportingContent = {
                    Text(text = "${emojiFlags[uiState.currency.string]}  ${uiState.currency.string}")
                },
                onClick = onCurrencies,
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                LinkItem(
                    title = stringResource(id = R.string.settings_language),
                    icon = R.drawable.settings_language,
                    supportingContent = {
                        val language = context.resources.configuration.getLocales().get(0).displayLanguage.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                        }
                        Text(text = language)
                    },
                    onClick = {
                        val intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS)
                        intent.data = Uri.fromParts("package", context.packageName, null)
                        context.startActivity(intent)
                    }
                )
            }
            LinkItem(title = stringResource(id = R.string.settings_networks_title), icon = R.drawable.settings_networks, listPosition = ListPosition.Last) {
                onNetworks()
            }
            LinkItem(title = stringResource(id = R.string.wallet_connect_title), icon = R.drawable.settings_wc, listPosition = ListPosition.Single) {
                onBridges()
            }

            SubheaderItem(title = stringResource(id = R.string.settings_community))
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
            LinkItem(title = stringResource(id = R.string.social_youtube), icon = R.drawable.youtube) {
                uriHandler.open(context, Config().getSocialUrl(SocialUrl.YOU_TUBE) ?: "")
            }

            LinkItem(
                title = stringResource(id = R.string.settings_help_center),
                icon = R.drawable.settings_help_center,
                listPosition = ListPosition.Last,
            ) {
                uriHandler.open(
                    context,
                    Config().getDocsUrl(DocsUrl.Start).toUri()
                        .buildUpon()
                        .appendQueryParameter("utm_source", "gemwallet_android")
                        .build()
                        .toString()
                )
            }
            LinkItem(
                title = stringResource(id = R.string.settings_support),
                icon = R.drawable.settings_support,
                listPosition = ListPosition.First,
            ) {
                uriHandler.open(
                    context,
                    Config().getPublicUrl(PublicUrl.SUPPORT).toUri()
                        .buildUpon()
                        .appendQueryParameter("utm_source", "gemwallet_android")
                        .build()
                        .toString()
                )
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
//            LinkItem(
//                title = stringResource(id = R.string.settings_rate_app),
//                icon = R.drawable.settings_rate,
//                onClick = {
//                    reviewManager.open()
//                }
//            )
            if (uiState.developEnabled) {
                LinkItem(title = stringResource(id = R.string.settings_developer), icon = R.drawable.settings_developer, listPosition = ListPosition.Single,) {
                    onDevelop()
                }
            }
        }
    }

    if (requestPushGrant) {
        PushRequest(viewModel::notificationEnable) { requestPushGrant = false }
    }
}