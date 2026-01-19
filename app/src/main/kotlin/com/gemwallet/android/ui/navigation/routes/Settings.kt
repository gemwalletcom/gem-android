package com.gemwallet.android.ui.navigation.routes

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navOptions
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ui.components.animation.enterTabScreenTransition
import com.gemwallet.android.ui.components.animation.exitTabScreenTransition
import com.gemwallet.features.settings.aboutus.presents.AboutUsScreen
import com.gemwallet.features.settings.currency.presents.CurrenciesScene
import com.gemwallet.features.settings.develop.presents.DevelopScene
import com.gemwallet.features.settings.networks.presents.NetworksScreen
import com.gemwallet.features.settings.price_alerts.presents.PriceAlertTargetNavScreen
import com.gemwallet.features.settings.price_alerts.presents.PriceAlertsNavScreen
import com.gemwallet.features.settings.security.presents.SecurityScene
import com.gemwallet.features.settings.settings.presents.views.PreferencesScene
import com.gemwallet.features.settings.settings.presents.views.SettingsScene
import com.gemwallet.features.settings.settings.presents.views.SupportChatScreen
import com.wallet.core.primitives.AssetId
import kotlinx.serialization.Serializable

const val settingsRoute = "settings"

const val supportUri = "gem://support"

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
data class PriceAlertsRoute(val assetId: String? = null)

@Serializable
data class AddPriceAlertTargetRoute(val assetId: String)

@Serializable
object SupportRoute

@Serializable
object PreferencesRoute

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

fun NavController.navigateToPriceAlertsScreen(assetId: AssetId? = null, navOptions: NavOptions? = null) {
    navigate(PriceAlertsRoute(assetId?.toIdentifier()), navOptions ?: navOptions { launchSingleTop = true })
}

fun NavController.navigateToAddPriceAlertTargetScreen(assetId: AssetId, navOptions: NavOptions? = null) {
    navigate(AddPriceAlertTargetRoute(assetId.toIdentifier()), navOptions ?: navOptions { launchSingleTop = true })
}

fun NavController.navigateToSupport(navOptions: NavOptions? = null) {
    navigate(SupportRoute, navOptions ?: navOptions { launchSingleTop = true })
}

fun NavController.navigateToPreferences(navOptions: NavOptions? = null) {
    navigate(PreferencesRoute, navOptions ?: navOptions { launchSingleTop = true })
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
    onAddPriceAlertTarget: (AssetId) -> Unit,
    onChart: (AssetId) -> Unit,
    onSupport: () -> Unit,
    onPerpetual: () -> Unit,
    onReferral: () -> Unit,
    onPreferences: () -> Unit,
    onCancel: () -> Unit,
) {
    composable<SettingsRoute>(
        enterTransition = enterTabScreenTransition,
        exitTransition = exitTabScreenTransition,
    ) {
        SettingsScene(
            onSecurity = onSecurity,
            onBridges = onBridges,
            onDevelop = onDevelop,
            onAboutUs = onAboutUs,
            onWallets = onWallets,
            onSupport = onSupport,
            onPerpetual = onPerpetual,
            onPriceAlerts = onPriceAlerts,
            onReferral = onReferral,
            onPreferences = onPreferences
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
        PriceAlertsNavScreen(onChart = onChart, onAddPriceAlertTarget = onAddPriceAlertTarget, onCancel = onCancel)
    }

    composable<AddPriceAlertTargetRoute> {
        PriceAlertTargetNavScreen(onCancel = onCancel)
    }

    composable<PreferencesRoute> {
        PreferencesScene(
            onNetworks = onNetworks,
            onCurrencies = onCurrencies,
            onPerpetual = onPerpetual,
            onCancel = onCancel,
        )
    }

    composable<SupportRoute>(
        deepLinks = listOf(
            navDeepLink<SupportRoute>(basePath = supportUri)
        )
    ) {
        SupportChatScreen(onCancel = onCancel)
    }
}