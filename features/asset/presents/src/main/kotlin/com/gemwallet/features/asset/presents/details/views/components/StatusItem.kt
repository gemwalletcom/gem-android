package com.gemwallet.features.asset.presents.details.views.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.ext.type
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.InfoSheetEntity
import com.gemwallet.android.ui.components.list_item.property.DataBadgeChevron
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.open
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.pendingColor
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetSubtype
import uniffi.gemstone.Config
import uniffi.gemstone.DocsUrl

internal fun LazyListScope.status(asset: Asset, rank: Int) {
    val status = rank.getVerificationStatus()
    if (asset.id.type() == AssetSubtype.NATIVE || status == null) {
        return
    }
    item {
        val context = LocalContext.current
        val uriHandler = LocalUriHandler.current
        PropertyItem(
            modifier = Modifier.Companion.clickable {
                uriHandler.open(
                    context,
                    Config().getDocsUrl(DocsUrl.TOKEN_VERIFICATION)
                )
            },
            title = {
                PropertyTitleText(
                    text = R.string.transaction_status,
                    info = when (status) {
                        AssetVerification.Suspicious -> InfoSheetEntity.AssetStatusSuspiciousInfo
                        AssetVerification.Unverified -> InfoSheetEntity.AssetStatusUnverifiedInfo
                    }
                )
            },
            data = {
                PropertyDataText(
                    stringResource(
                        when (status) {
                            AssetVerification.Suspicious -> R.string.asset_verification_suspicious
                            AssetVerification.Unverified -> R.string.asset_verification_unverified
                        }
                    ),
                    color = when (status) {
                        AssetVerification.Suspicious -> MaterialTheme.colorScheme.error
                        AssetVerification.Unverified -> pendingColor
                    },
                    badge = {
                        DataBadgeChevron(
                            when (status) {
                                AssetVerification.Suspicious -> R.drawable.suspicious
                                AssetVerification.Unverified -> R.drawable.unverified
                            }
                        )
                    }
                )
            },
        )
        Spacer16()
    }
}

private enum class AssetVerification(val min: Int) {
    Suspicious(5),
    Unverified(15),
}

private fun Int.getVerificationStatus(): AssetVerification? {
    return if (this < AssetVerification.Suspicious.min) {
        AssetVerification.Suspicious
    } else if (this < AssetVerification.Unverified.min) {
        AssetVerification.Unverified
    } else {
        null
    }
}