package com.gemwallet.android.features.settings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import androidx.navigation.navigation
import com.gemwallet.android.features.settings.aboutus.views.AboutUsScreen
import com.gemwallet.android.features.settings.currency.views.CurrenciesScene
import com.gemwallet.android.features.settings.develop.views.DevelopScene
import com.gemwallet.android.features.settings.networks.views.NetworksScreen
import com.gemwallet.android.features.settings.price_alerts.views.PriceAlertsScreen
import com.gemwallet.android.features.settings.security.views.SecurityScene
import com.gemwallet.android.features.settings.settings.views.SettingsScene
import com.gemwallet.android.ui.navigation.enterTabScreenTransition
import com.gemwallet.android.ui.navigation.exitTabScreenTransition
import com.wallet.core.primitives.AssetId

const val settingsRoute = "settings"
const val currenciesRoute = "currencies"
const val securityRoute = "security"
const val developRoute = "develop"
const val aboutusRoute = "aboutus"
const val networksRoute = "networks"
const val priceAlertsRoute = "price_alerts"

fun NavController.navigateToSettingsScreen(navOptions: NavOptions? = null) {
    navigate(settingsRoute, navOptions ?: navOptions { launchSingleTop = true })
}

fun NavController.navigateToCurrenciesScreen(navOptions: NavOptions? = null) {
    navigate(currenciesRoute, navOptions ?: navOptions { launchSingleTop = true })
}

fun NavController.navigateToSecurityScreen(navOptions: NavOptions? = null) {
    navigate(securityRoute, navOptions ?: navOptions { launchSingleTop = true })
}

fun NavController.navigateToDevelopScreen(navOptions: NavOptions? = null) {
    navigate(developRoute, navOptions ?: navOptions { launchSingleTop = true })
}

fun NavController.navigateToAboutUsScreen(navOptions: NavOptions? = null) {
    navigate(aboutusRoute, navOptions ?: navOptions { launchSingleTop = true })
}

fun NavController.navigateToNetworksScreen(navOptions: NavOptions? = null) {
    navigate(networksRoute, navOptions ?: navOptions { launchSingleTop = true })
}

fun NavController.navigateToPriceAlertsScreen(navOptions: NavOptions? = null) {
    navigate(priceAlertsRoute, navOptions ?: navOptions { launchSingleTop = true })
}

fun NavGraphBuilder.settingsScreen(
    onSecurity: () -> Unit,
    onCurrencies: () -> Unit,
    onWallets: () -> Unit,
    onBridges: () -> Unit,
    onDevelop: () -> Unit,
    onAboutUs: () -> Unit,
    onNetworks: () -> Unit,
    onPriceAlerts: () -> Unit,
    onChart: (AssetId) -> Unit,
    onCancel: () -> Unit,
) {
    navigation(settingsRoute, settingsRoute + "group") {
        composable(
            settingsRoute,
            enterTransition = enterTabScreenTransition,
            exitTransition = exitTabScreenTransition,
        ) {
            SettingsScene(
                onSecurity = onSecurity,
                onCurrencies = onCurrencies,
                onBridges = onBridges,
                onDevelop = onDevelop,
                onAboutUs = onAboutUs,
                onWallets = onWallets,
                onNetworks = onNetworks,
                onPriceAlerts = onPriceAlerts,
            )
        }

        composable(currenciesRoute) {
            CurrenciesScene(onCancel = onCancel)
        }

        composable(securityRoute) {
            SecurityScene(onCancel = onCancel)
        }

        composable(developRoute) {
            DevelopScene(onCancel = onCancel)
        }

        composable(aboutusRoute) {
            AboutUsScreen(onCancel = onCancel)
        }

        composable(networksRoute) {
            NetworksScreen(onCancel = onCancel)
        }

        composable(priceAlertsRoute) {
            PriceAlertsScreen(onChart = onChart, onCancel = onCancel)
        }
    }
}