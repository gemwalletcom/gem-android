package com.gemwallet.android.features.recipient.navigation

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ext.urlDecode
import com.gemwallet.android.ext.urlEncode
import com.gemwallet.android.features.amount.navigation.OnAmount
import com.gemwallet.android.features.asset_select.views.SelectSendScreen
import com.gemwallet.android.features.recipient.views.RecipientForm
import com.wallet.core.primitives.AssetId

internal const val assetIdArg = "assetId"
internal const val destinationAddressArg = "destination_address"
internal const val memoArg = "memo"
internal const val addressDomainArg = "address_domain"

const val sendRoute = "send"
const val sendRecipientRoute = "send_recipient"
const val sendAssetSelectRoute = "sendAssetSelect"

fun NavController.navigateToSendScreen(assetId: AssetId? = null, navOptions: NavOptions? = null) {
    if (assetId == null) {
        navigate(sendAssetSelectRoute, navOptions)
    } else {
        navigate("$sendRecipientRoute/${assetId.toIdentifier().urlEncode()}", navOptions)
    }
}

@OptIn(ExperimentalGetImage::class)
fun NavGraphBuilder.recipientScreen(
    onCancel: () -> Unit,
    onSend: (AssetId) -> Unit,
    onAmount: OnAmount,
) {
    navigation("$sendRecipientRoute/{$assetIdArg}", sendRoute) {
        composable(
            route = sendAssetSelectRoute,
        ) {
            SelectSendScreen(
                onCancel = onCancel,
                onSelect = {
                    onSend(it)
                }
            )
        }

        composable(
            route = "$sendRecipientRoute/{$assetIdArg}",
            arguments = listOf(
                navArgument(assetIdArg) {
                    type = NavType.StringType
                    nullable = true
                }
            ),
        ) {
            val assetId = it.arguments?.getString(assetIdArg)?.urlDecode()?.toAssetId()
            val destinationAddress = it.arguments?.getString(destinationAddressArg)?.urlDecode() ?: ""
            val addressDomain = it.arguments?.getString(addressDomainArg)?.urlDecode() ?: ""
            val memo = it.arguments?.getString(memoArg)?.urlDecode() ?: ""
            if (assetId == null) {
                onCancel()
            } else {
                RecipientForm(
                    assetId = assetId,
                    destinationAddress = destinationAddress,
                    addressDomain = addressDomain,
                    memo = memo,
                    onNext = onAmount,
                    onCancel = onCancel,
                )
            }
        }
    }
}