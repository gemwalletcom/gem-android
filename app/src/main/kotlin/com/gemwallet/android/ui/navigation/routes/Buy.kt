package com.gemwallet.android.ui.navigation.routes

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import androidx.navigation.navigation
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ui.models.actions.CancelAction
import com.gemwallet.features.asset_select.presents.views.SelectBuyScreen
import com.gemwallet.features.buy.views.FiatScreen
import com.wallet.core.primitives.AssetId
import kotlinx.serialization.Serializable

@Serializable
data class FiatInput(val assetId: String)

@Serializable
object FiatSelect

@Serializable
object Fiat

fun NavController.navigateToBuyScreen(assetId: AssetId? = null, navOptions: NavOptions? = null) {
    if (assetId == null) {
        navigate(FiatSelect, navOptions ?: navOptions { launchSingleTop = true })
    } else {
        navigate(FiatInput(assetId.toIdentifier()), navOptions ?: navOptions { launchSingleTop = true })
    }
}

fun NavGraphBuilder.fiatScreen(
    cancelAction: CancelAction,
    onBuy: (AssetId) -> Unit,
) {
    navigation<Fiat>(startDestination = FiatSelect) {
        composable<FiatInput> {
            FiatScreen(
                cancelAction = cancelAction
            )
        }

        composable<FiatSelect> {
            SelectBuyScreen(
                cancelAction = cancelAction,
                onSelect = {
                    onBuy(it)
                }
            )
        }
    }
}