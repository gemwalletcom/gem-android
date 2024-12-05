package com.gemwallet.android.features.settings.aboutus.views

import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
    val viewModel: AboutUsViewModel = hiltViewModel()
    val context = LocalContext.current

    AboutUsScene(
        onCancel = onCancel,
        onDevelopEnable = {
            val developer = context.getString(R.string.settings_developer)
            val enable = context.getString(R.string.settings_enable_value, developer)
            Toast.makeText(context, enable, Toast.LENGTH_SHORT).show()
            viewModel.developEnable()
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AboutUsScene(
    onCancel: () -> Unit,
    onDevelopEnable: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val version = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.PackageInfoFlags.of(0)
        )
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
        HorizontalDivider(modifier = Modifier, thickness = 0.4.dp)
        LinkItem(
            title = stringResource(id = R.string.settings_version),
            icon = R.drawable.settings_version,
            trailingContent = {
                Text(
                    modifier = Modifier.combinedClickable(
                        onClick = {},
                        onLongClick = onDevelopEnable
                    ),
                    text = "${stringResource(id = R.string.settings_version)}: $version",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        ) {}
    }
}