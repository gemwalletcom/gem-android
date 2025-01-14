package com.gemwallet.android.features.settings.aboutus.views

import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.gemwallet.android.R
import com.gemwallet.android.features.settings.settings.components.LinkItem
import com.gemwallet.android.ui.components.open
import com.gemwallet.android.ui.components.screen.Scene
import uniffi.gemstone.Config
import uniffi.gemstone.PublicUrl

@Composable
fun AboutUsScreen(
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val version = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        context.packageManager.getPackageInfo(context.packageName, 0)
    }.versionName
    Scene(title = stringResource(id = R.string.settings_aboutus), onClose = onCancel) {
        LinkItem(title = stringResource(id = R.string.settings_privacy_policy)) {
            uriHandler.open(Config().getPublicUrl(PublicUrl.PRIVACY_POLICY))
        }
        LinkItem(title = stringResource(id = R.string.settings_terms_of_services)) {
            uriHandler.open(Config().getPublicUrl(PublicUrl.TERMS_OF_SERVICE))
        }
        LinkItem(
            title = stringResource(id = R.string.settings_version),
            icon = R.drawable.settings_version,
            trailingContent = {
                Text(
                    text = "${stringResource(id = R.string.settings_version)}: $version",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        ) {}
    }
}