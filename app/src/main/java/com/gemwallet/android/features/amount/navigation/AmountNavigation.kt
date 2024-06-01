package com.gemwallet.android.features.amount.navigation

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ext.urlDecode
import com.gemwallet.android.ext.urlEncode
import com.gemwallet.android.features.amount.views.AmountScreen
import com.gemwallet.android.model.ConfirmParams
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.TransactionType

internal const val txTypeArg = "tx_type"
internal const val destinationAddressArg = "destination_address"
internal const val memoArg = "memo"
internal const val addressDomainArg = "address_domain"
internal const val delegationIdArg = "delegation_id"
internal const val validatorIdArg = "validator_id"

const val sendAmountRoute = "send_amount"

fun interface OnAmount {
    operator fun invoke(
        assetId: AssetId,
        destinationAddress: String,
        addressDomain: String,
        memo: String,
        validatorId: String?,
        delegationId: String?,
        txType: TransactionType,
    )
}

fun NavController.navigateToAmountScreen(
    assetId: AssetId,
    destinationAddress: String,
    addressDomain: String,
    memo: String,
    validatorId: String? = null,
    delegationId: String? = null,
    txType: TransactionType = TransactionType.Transfer,
) {
    navigate(route = "$sendAmountRoute/${assetId.toIdentifier().urlEncode()}" +
            "?$destinationAddressArg=${destinationAddress.urlEncode()}" +
            "&$addressDomainArg=${addressDomain.urlEncode()}" +
            "&$memoArg=${memo.urlEncode()}" +
            "&$txTypeArg=${txType.string.urlEncode()}" +
            "&$delegationIdArg=${delegationId?.urlEncode() ?: ""}" +
            "&$validatorIdArg=${validatorId?.urlEncode() ?: ""}",
    )
}

@OptIn(ExperimentalGetImage::class)
fun NavGraphBuilder.amount(
    onCancel: () -> Unit,
    onConfirm: (ConfirmParams) -> Unit,
) {
    navigation(
        "$sendAmountRoute/{${com.gemwallet.android.features.recipient.navigation.assetIdArg}}",
        sendAmountRoute
    ) {
        composable(
            route = "$sendAmountRoute/{${com.gemwallet.android.features.recipient.navigation.assetIdArg}}" +
                    "?$destinationAddressArg={$destinationAddressArg}" +
                    "&$addressDomainArg={$addressDomainArg}" +
                    "&$memoArg={$memoArg}" +
                    "&$delegationIdArg={$delegationIdArg}" +
                    "&$validatorIdArg={$validatorIdArg}" +
                    "&$txTypeArg={$txTypeArg}",
            arguments = listOf(
                navArgument(com.gemwallet.android.features.recipient.navigation.assetIdArg) {
                    type = NavType.StringType
                },
                navArgument(destinationAddressArg) {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument(addressDomainArg) {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument(memoArg) {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument(txTypeArg) {
                    type = NavType.StringType
                    nullable = false
                },
                navArgument(delegationIdArg) {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument(validatorIdArg) {
                    type = NavType.StringType
                    nullable = true
                },
            )
        ) { entry ->
            val assetId = entry.arguments?.getString(com.gemwallet.android.features.recipient.navigation.assetIdArg)?.urlDecode()?.toAssetId()
            val destinationAddress =
                entry.arguments?.getString(destinationAddressArg)?.urlDecode() ?: ""
            val addressDomain = entry.arguments?.getString(addressDomainArg)?.urlDecode() ?: ""
            val meta = entry.arguments?.getString(memoArg)?.urlDecode() ?: ""
            val delegationId = entry.arguments?.getString(delegationIdArg)?.urlDecode() ?: ""
            val validatorId = entry.arguments?.getString(validatorIdArg)?.urlDecode() ?: ""
            val txTypeString = entry.arguments?.getString(txTypeArg)?.urlDecode()
            val txType = TransactionType.entries
                .firstOrNull { it.string == txTypeString } ?: TransactionType.Transfer

            if (assetId == null) {
                onCancel()
            } else {
                AmountScreen(
                    assetId = assetId,
                    destinationAddress = destinationAddress,
                    addressDomain = addressDomain,
                    memo = meta,
                    txType = txType,
                    delegationId = delegationId,
                    validatorId = validatorId,
                    onCancel = onCancel,
                    onConfirm = onConfirm,
                )
            }
        }
    }
}