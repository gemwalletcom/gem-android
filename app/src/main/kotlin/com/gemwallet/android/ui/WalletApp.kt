@file:OptIn(ExperimentalPermissionsApi::class)

package com.gemwallet.android.ui

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.gemwallet.android.BuildConfig
import com.gemwallet.android.features.create_wallet.navigation.navigateToCreateWalletRulesScreen
import com.gemwallet.android.features.import_wallet.navigation.navigateToImportWalletScreen
import com.gemwallet.android.features.onboarding.OnboardScreen
import com.gemwallet.android.flavors.ReviewManager
import com.gemwallet.android.ui.navigation.WalletNavGraph
import com.gemwallet.android.ui.theme.Spacer16
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun WalletApp(
    navController: NavHostController = rememberNavController(),
    viewModel: AppViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var startDestination by remember { mutableStateOf<String?>(null) }

    val permissionState = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        null
    } else {
        rememberPermissionState(
            permission = Manifest.permission.POST_NOTIFICATIONS,
            onPermissionResult = {
                if (it) {
                    viewModel.onNotificationsEnable()
                } else {
                    viewModel.laterAskNotifications()
                }
            }
        )
    }

    var requestNotificationPermissions by remember { mutableStateOf(false) }
    val askNotifications by viewModel.askNotifications.collectAsStateWithLifecycle()

    LaunchedEffect(requestNotificationPermissions) {
        if (requestNotificationPermissions) {
            permissionState?.launchPermissionRequest()
        }
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            startDestination = viewModel.getStartDestination()
        }
    }

    WalletNavGraph(
        navController = navController,
        startDestination = startDestination ?: return,
        onboard = {
            OnboardScreen(
                onCreateWallet = navController::navigateToCreateWalletRulesScreen,
                onImportWallet = navController::navigateToImportWalletScreen,
            )
        },
    )
    if (state.intent == AppIntent.ShowUpdate) {
        ShowUpdateDialog(
            version = state.version,
            onSkip = viewModel::onSkip,
            onCancel = viewModel::onCancelUpdate
        )
    }

    if (state.intent == AppIntent.ShowReview) {
        viewModel.onReviewOpen()
        ReviewManager().open(LocalActivity.current ?: return)
    }

    if (askNotifications) {
        if (permissionState == null) {
            AlertDialog(
                onDismissRequest = viewModel::laterAskNotifications,
                text = {
                    Text(text = stringResource(id = R.string.notifications_permission_request_notification))
                },
                confirmButton = {
                    Button(onClick = viewModel::onNotificationsEnable) {
                        Text(text = stringResource(id = R.string.common_grant_permission))
                    }
                },
                dismissButton = {
                    Button(onClick = viewModel::laterAskNotifications) {
                        Text(text = stringResource(id = R.string.common_no_thanks))
                    }
                }
            )
        } else {
            if (permissionState.status.isGranted) {
                viewModel.onNotificationsEnable()
            } else {
                requestNotificationPermissions = askNotifications
            }
        }
    }
}

@Composable
private fun ShowUpdateDialog(
    version: String,
    onSkip: (String) -> Unit,
    onCancel: () -> Unit,
) {
    if (fromGooglePlay(LocalContext.current)) {
        return
    }
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = {
            TextButton(onClick = {
                onCancel()
                uriHandler.open(context, BuildConfig.UPDATE_URL)
            }) {
                Text(text = stringResource(id = R.string.update_app_action))
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onCancel) {
                    Text(text = stringResource(id = R.string.common_cancel))
                }
                Spacer16()
                TextButton(onClick = { onSkip(version) }) {
                    Text(text = stringResource(R.string.common_skip))
                }
            }
        },
        title = {
            Text(text = stringResource(id = R.string.update_app_title))
        },
        text = {
            Text(text = stringResource(id = R.string.update_app_description, version))
        }
    )
}

@Suppress("DEPRECATION")
private fun fromGooglePlay(context: Context): Boolean {
    // A list with valid installers package name
    val validInstallers = listOf("com.android.vending", "com.google.android.feedback")

    val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
        context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
    } else{
        context.packageManager.getInstallerPackageName(context.packageName)
    }
    return installer != null && validInstallers.contains(installer)
}