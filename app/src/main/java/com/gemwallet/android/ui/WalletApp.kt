package com.gemwallet.android.ui

import android.content.Context
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.gemwallet.android.BuildConfig
import com.gemwallet.android.R
import com.gemwallet.android.features.create_wallet.navigation.navigateToCreateWalletScreen
import com.gemwallet.android.features.import_wallet.navigation.navigateToImportWalletScreen
import com.gemwallet.android.features.onboarding.OnboardScreen
import com.gemwallet.android.ui.components.ReviewManager
import com.gemwallet.android.ui.components.open
import com.gemwallet.android.ui.navigation.WalletNavGraph
import com.gemwallet.android.ui.theme.Spacer16

@Composable
fun WalletApp() {
    val viewModel: AppViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val startDestination = viewModel.getStartDestination()
    var currentRoute by remember {
        mutableStateOf<String?>(null)
    }
    navController.addOnDestinationChangedListener { _, dest, _ ->
        currentRoute = dest.route
    }
    WalletNavGraph(
        navController = navController,
        startDestination = startDestination,
        onboard = {
            OnboardScreen(
                onCreateWallet = navController::navigateToCreateWalletScreen,
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
        ReviewManager(LocalContext.current).open()
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
    val uriHandler = LocalUriHandler.current
    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = {
            TextButton(onClick = {
                onCancel()
                uriHandler.open(BuildConfig.UPDATE_URL)
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

    val installer = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R){
        context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
    } else{
        context.packageManager.getInstallerPackageName(context.packageName)
    }
    return installer != null && validInstallers.contains(installer)
}