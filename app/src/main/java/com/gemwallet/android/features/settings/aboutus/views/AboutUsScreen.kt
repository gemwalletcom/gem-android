package com.gemwallet.android.features.settings.aboutus.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
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
    val uriHandler = LocalUriHandler.current
    Scene(title = stringResource(id = R.string.settings_aboutus), onClose = onCancel) {
        LinkItem(title = stringResource(id = R.string.settings_privacy_policy)) {
            uriHandler.open(Config().getPublicUrl(PublicUrl.PRIVACY_POLICY))
        }
        LinkItem(title = stringResource(id = R.string.settings_terms_of_services)) {
            uriHandler.open(Config().getPublicUrl(PublicUrl.TERMS_OF_SERVICE))
        }
    }
}