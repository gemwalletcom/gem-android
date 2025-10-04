package com.gemwallet.features.settings.aboutus.presents

import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.LinkItem
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.open
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
        LazyColumn {
            item {
                LinkItem(
                    title = stringResource(id = R.string.settings_privacy_policy),
                    listPosition = ListPosition.First
                ) {
                    uriHandler.open(context, Config().getPublicUrl(PublicUrl.PRIVACY_POLICY))
                }
                LinkItem(
                    title = stringResource(id = R.string.settings_terms_of_services),
                    listPosition = ListPosition.Last
                ) {
                    uriHandler.open(context, Config().getPublicUrl(PublicUrl.TERMS_OF_SERVICE))
                }
                LinkItem(
                    title = stringResource(id = R.string.settings_version),
                    painter = painterResource(id = R.drawable.settings_version),
                    listPosition = ListPosition.Single,
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
    }
}