package com.gemwallet.android.ui.navigation.routes

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.features.asset_select.views.SelectSendScreen
import com.gemwallet.android.features.recipient.presents.RecipientScene
import com.gemwallet.android.ui.models.actions.AmountTransactionAction
import com.gemwallet.android.ui.models.actions.AssetIdAction
import com.gemwallet.android.ui.models.actions.CancelAction
import com.gemwallet.android.ui.models.actions.ConfirmTransactionAction
import com.wallet.core.primitives.AssetId
import kotlinx.serialization.Serializable

@Serializable
data class RecipientInput(val assetId: String)

@Serializable
object SendSelect

fun NavController.navigateToRecipientInput(assetId: AssetId? = null) {
    if (assetId == null) {
        navigate(SendSelect, navOptions { launchSingleTop = true })
    } else {
        navigate(RecipientInput(assetId.toIdentifier()), navOptions { launchSingleTop = true })
    }
}

fun NavGraphBuilder.recipientInput(
    cancelAction: CancelAction,
    recipientAction: AssetIdAction,
    amountAction: AmountTransactionAction,
    confirmAction: ConfirmTransactionAction,
) {
    composable<SendSelect> {
        SelectSendScreen(
            onCancel = { cancelAction() },
            onSelect = {
                recipientAction(it)
            }
        )
    }

    composable<RecipientInput> {
        RecipientScene(
            cancelAction = cancelAction,
            amountAction = amountAction,
            confirmAction = confirmAction,
        )
    }
}