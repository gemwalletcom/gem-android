package com.gemwallet.android.ui.navigation.routes

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.gemwallet.android.features.settings.aboutus.views.AboutUsScreen
import com.gemwallet.android.features.settings.currency.views.CurrenciesScene
import com.gemwallet.android.features.settings.develop.views.DevelopScene
import com.gemwallet.android.features.settings.networks.views.NetworksScreen
import com.gemwallet.android.features.settings.price_alerts.views.PriceAlertsScreen
import com.gemwallet.android.features.settings.security.views.SecurityScene
import com.gemwallet.android.features.settings.settings.views.SettingsScene
import com.gemwallet.android.ui.components.animation.enterTabScreenTransition
import com.gemwallet.android.ui.components.animation.exitTabScreenTransition
import com.wallet.core.primitives.AssetId
import kotlinx.serialization.Serializable

const val settingsRoute = "settings"

@Serializable
object SettingsRoute

@Serializable
object CurrenciesRoute

@Serializable
object SecurityRoute

@Serializable
object DevelopRoute

@Serializable
object AboutusRoute

@Serializable
object NetworksRoute

@Serializable
object PriceAlertsRoute

fun NavController.navigateToSettingsScreen(navOptions: NavOptions? = null) {
    navigate(SettingsRoute, navOptions ?: navOptions { launchSingleTop = true })
}

fun NavController.navigateToCurrenciesScreen(navOptions: NavOptions? = null) {
    navigate(CurrenciesRoute, navOptions ?: navOptions { launchSingleTop = true })
}

fun NavController.navigateToSecurityScreen(navOptions: NavOptions? = null) {
    navigate(SecurityRoute, navOptions ?: navOptions { launchSingleTop = true })
}

fun NavController.navigateToDevelopScreen(navOptions: NavOptions? = null) {
    navigate(DevelopRoute, navOptions ?: navOptions { launchSingleTop = true })
}

fun NavController.navigateToAboutUsScreen(navOptions: NavOptions? = null) {
    navigate(AboutusRoute, navOptions ?: navOptions { launchSingleTop = true })
}

fun NavController.navigateToNetworksScreen(navOptions: NavOptions? = null) {
    navigate(NetworksRoute, navOptions ?: navOptions { launchSingleTop = true })
}

fun NavController.navigateToPriceAlertsScreen(navOptions: NavOptions? = null) {
    navigate(PriceAlertsRoute, navOptions ?: navOptions { launchSingleTop = true })
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
    composable<SettingsRoute>(
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

    composable<CurrenciesRoute> {
        CurrenciesScene(onCancel = onCancel)
    }

    composable<SecurityRoute> {
        SecurityScene(onCancel = onCancel)
    }

    composable<DevelopRoute> {
        DevelopScene(onCancel = onCancel)
    }

    composable<AboutusRoute> {
        AboutUsScreen(onCancel = onCancel)
    }

    composable<NetworksRoute> {
        NetworksScreen(onCancel = onCancel)
    }

    composable<PriceAlertsRoute> {
        PriceAlertsScreen(onChart = onChart, onCancel = onCancel)
    }
}