package com.gemwallet.android.features.settings.settings.views

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.R
import com.gemwallet.android.features.settings.currency.components.emojiFlags
import com.gemwallet.android.features.settings.settings.components.LinkItem
import com.gemwallet.android.features.settings.settings.viewmodels.SettingsViewModel
import com.gemwallet.android.services.isNotificationsAvailable
import com.gemwallet.android.ui.components.ReviewManager
import com.gemwallet.android.ui.components.Scene
import com.gemwallet.android.ui.components.SubheaderItem
import com.gemwallet.android.ui.components.open
import com.gemwallet.android.ui.theme.Spacer16
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import uniffi.Gemstone.Config
import uniffi.Gemstone.SocialUrl
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
    scrollState: ScrollState = rememberScrollState()
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val reviewManager = remember { ReviewManager(context) }
    val version = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        context.packageManager.getPackageInfo(context.packageName, 0)
    }.versionName

    val uriHandler = LocalUriHandler.current
    var requestPushGrant by remember {
        mutableStateOf(false)
    }
    Scene(
        title = stringResource(id = R.string.settings_title),
        mainActionPadding = PaddingValues(0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            LinkItem(
                title = stringResource(id = R.string.wallets_title),
                icon = R.drawable.settings_wallets,
                onClick = onWallets
            )
            LinkItem(
                title = stringResource(id = R.string.settings_security),
                icon = R.drawable.settings_security,
                onClick = onSecurity
            )
            HorizontalDivider(modifier = Modifier, thickness = 0.4.dp)
            if (isNotificationsAvailable()) {
                LinkItem(
                    title = stringResource(id = R.string.settings_notifications_title),
                    icon = R.drawable.settings_notifications,
                    trailingContent = @Composable {
                        Switch(
                            checked = uiState.pushEnabled,
                            onCheckedChange = {
                                if (it) requestPushGrant = true else viewModel.notificationEnable()
                            }
                        )
                    },
                    onClick = {}
                )
            }
            LinkItem(
                title = stringResource(R.string.settings_currency),
                icon = R.drawable.settings_currency,
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
                        val language =
                            context.resources.configuration.getLocales().get(0).displayLanguage.replaceFirstChar {
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
            LinkItem(title = stringResource(id = R.string.settings_networks_title), icon = R.drawable.settings_networks) {
                onNetworks()
            }
            LinkItem(title = stringResource(id = R.string.wallet_connect_title), icon = R.drawable.settings_wc) {
                onBridges()
            }
            HorizontalDivider(modifier = Modifier, thickness = 0.4.dp)
            
            SubheaderItem(title = stringResource(id = R.string.settings_community))
            LinkItem(title = stringResource(id = R.string.social_x), icon = R.drawable.twitter) {
                uriHandler.open(Config().getSocialUrl(SocialUrl.X) ?: "")
            }
            LinkItem(title = stringResource(id = R.string.social_discord), icon = R.drawable.discord) {
                uriHandler.open(Config().getSocialUrl(SocialUrl.DISCORD) ?: "")
            }
            LinkItem(title = stringResource(id = R.string.social_telegram), icon = R.drawable.telegram) {
                uriHandler.open(Config().getSocialUrl(SocialUrl.TELEGRAM) ?: "")
            }
            LinkItem(title = stringResource(id = R.string.social_github), icon = R.drawable.github) {
                uriHandler.open(Config().getSocialUrl(SocialUrl.GIT_HUB) ?: "")
            }
            LinkItem(title = stringResource(id = R.string.social_youtube), icon = R.drawable.youtube) {
                uriHandler.open(Config().getSocialUrl(SocialUrl.YOU_TUBE) ?: "")
            }
            HorizontalDivider(modifier = Modifier, thickness = 0.4.dp)

            LinkItem(
                title = stringResource(id = R.string.settings_aboutus),
                icon = R.drawable.settings_about_us,
                onClick = onAboutUs
            )
//            LinkItem(
//                title = stringResource(id = R.string.settings_rate_app),
//                icon = R.drawable.settings_rate,
//                onClick = {
//                    reviewManager.open()
//                }
//            )
            if (uiState.developEnabled) {
                LinkItem(title = stringResource(id = R.string.settings_developer), icon = R.drawable.settings_developer) {
                    onDevelop()
                }
            }
            LinkItem(
                title = stringResource(id = R.string.settings_version),
                icon = R.drawable.settings_version,
                trailingContent = {
                    Text(
                        modifier = Modifier.combinedClickable(
                            onClick = {},
                            onLongClick = viewModel::developEnable
                        ),
                        text = "${stringResource(id = R.string.settings_version)}: $version",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            ) {}
            Spacer16()
        }
    }

    if (requestPushGrant) {
        PushRequest(viewModel::notificationEnable) { requestPushGrant = false }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PushRequest(
    onNotificationEnable: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        onNotificationEnable()
        return
    }
    val permissionState =
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    if (permissionState.status.isGranted) {
        onNotificationEnable()
        onDismiss()
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            text = {
                Text(text = stringResource(id = R.string.notifications_permission_request_notification))
            },
            confirmButton = {
                Button(onClick = { permissionState.launchPermissionRequest() }) {
                    Text(text = stringResource(id = R.string.common_grant_permission))
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text(text = stringResource(id = R.string.common_no_thanks))
                }
            }
        )
    }
}