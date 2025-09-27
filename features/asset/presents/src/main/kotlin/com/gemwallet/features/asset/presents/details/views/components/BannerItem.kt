package com.gemwallet.features.asset.presents.details.views.components

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.gemwallet.android.domains.asset.chain
import com.gemwallet.android.ext.getReserveBalanceUrl
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.ui.open
import com.gemwallet.features.banner.views.BannersScene
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.BannerEvent
import uniffi.gemstone.Config
import uniffi.gemstone.DocsUrl

@Composable
internal fun BannerItem(
    assetInfo: AssetInfo,
    onStake: (AssetId) -> Unit,
    onConfirm: (ConfirmParams) -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    BannersScene(
        asset = assetInfo.asset,
        onClick = {
            when (it.event) {
                BannerEvent.Stake -> onStake(assetInfo.asset.id)
                BannerEvent.AccountBlockedMultiSignature ->
                    uriHandler.open(context, Config().getDocsUrl(DocsUrl.TRON_MULTI_SIGNATURE))

                BannerEvent.ActivateAsset -> {
                    val params = ConfirmParams.Builder(
                        asset = assetInfo.asset,
                        from = assetInfo.owner ?: return@BannersScene
                    ).activate()
                    onConfirm(params)
                }

                BannerEvent.AccountActivation -> assetInfo.asset.chain
                    .getReserveBalanceUrl()?.let { uri -> uriHandler.open(context, uri) }

                else -> {}
            }
        },
        false
    )
    HorizontalDivider(thickness = 0.dp)
//    item {
//    }
}