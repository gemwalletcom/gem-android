package com.gemwallet.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gemwallet.android.R
import com.gemwallet.android.features.asset.navigation.navigateToAssetScreen
import com.gemwallet.android.features.assets.navigation.assetsRoute
import com.gemwallet.android.features.assets.navigation.navigateToAssetsScreen
import com.gemwallet.android.features.assets.views.AssetsScreen
import com.gemwallet.android.features.bridge.navigation.navigateToBridgesScreen
import com.gemwallet.android.features.buy.navigation.navigateToBuyScreen
import com.gemwallet.android.features.create_wallet.navigation.navigateToAssetsManageScreen
import com.gemwallet.android.features.receive.navigation.navigateToReceiveScreen
import com.gemwallet.android.features.recipient.navigation.navigateToSendScreen
import com.gemwallet.android.features.settings.navigation.navigateToAboutUsScreen
import com.gemwallet.android.features.settings.navigation.navigateToCurrenciesScreen
import com.gemwallet.android.features.settings.navigation.navigateToDevelopScreen
import com.gemwallet.android.features.settings.navigation.navigateToNetworksScreen
import com.gemwallet.android.features.settings.navigation.navigateToSecurityScreen
import com.gemwallet.android.features.settings.navigation.navigateToSettingsScreen
import com.gemwallet.android.features.settings.navigation.settingsRoute
import com.gemwallet.android.features.settings.settings.views.SettingsScene
import com.gemwallet.android.features.swap.navigation.navigateToSwap
import com.gemwallet.android.features.transactions.list.views.TransactionsScreen
import com.gemwallet.android.features.transactions.navigation.activitiesRoute
import com.gemwallet.android.features.transactions.navigation.navigateToActivitiesScreen
import com.gemwallet.android.features.transactions.navigation.navigateToTransactionScreen
import com.gemwallet.android.features.wallets.navigation.navigateToWalletsScreen
import com.gemwallet.android.model.BottomNavItem

@Composable
fun MainScreen(
    navController: NavController,
    currentTab: MutableState<String>,
) {
    BackHandler(currentTab.value == activitiesRoute || currentTab.value == settingsRoute) {
        currentTab.value = assetsRoute
    }
    val context = LocalContext.current
    val assetsListState = rememberLazyListState()
    val activitiesListState = rememberLazyListState()
    val settingsScrollState = rememberScrollState()

    val navItems = remember {
        listOf(
            BottomNavItem(
                label = context.getString(R.string.common_wallet),
                icon = Icons.Default.Wallet,
                route = assetsRoute,
                navigate = { navigateToAssetsScreen(it) }
            ),
            BottomNavItem(
                label = context.getString(R.string.activity_title),
                icon = Icons.Default.ElectricBolt,
                route = activitiesRoute,
                navigate = { navigateToActivitiesScreen(navOptions = it) }
            ),
            BottomNavItem(
                label = context.getString(R.string.settings_title),
                icon = Icons.Default.Settings,
                route = settingsRoute,
                navigate = { navigateToSettingsScreen(it) }
            ),
        )
    }
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
            ) {
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = item.route == currentTab.value,
                        onClick = {
                            currentTab.value = item.route
                        },
                        icon = {
                            val modifier = Modifier.size(24.dp)
                            if (item.route == assetsRoute) {
                                Icon(
                                    modifier = modifier,
                                    painter = painterResource(R.drawable.wallets),
                                    contentDescription = item.label,
                                )
                            } else {
                                Icon(
                                    modifier = modifier,
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                )
                            }
                        },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors().copy(
                            selectedIconColor = MaterialTheme.colorScheme.onSurface,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            unselectedIconColor = MaterialTheme.colorScheme.secondary,
                            unselectedTextColor = MaterialTheme.colorScheme.secondary,
                            selectedIndicatorColor = Color.Transparent,
                        )
                    )
                }
            }
        }
    ) {
        Box(modifier = Modifier.padding(bottom = it.calculateBottomPadding())) {
            when (currentTab.value) {
                assetsRoute -> AssetsScreen(
                    onShowWallets = navController::navigateToWalletsScreen,
                    onShowAssetManage = navController::navigateToAssetsManageScreen,
                    onSendClick = navController::navigateToSendScreen,
                    onReceiveClick = navController::navigateToReceiveScreen,
                    onBuyClick = navController::navigateToBuyScreen,
                    onSwapClick = navController::navigateToSwap,
                    onTransactionClick = navController::navigateToTransactionScreen,
                    onAssetClick = navController::navigateToAssetScreen,
                    listState = assetsListState,
                )
                activitiesRoute -> TransactionsScreen(
                    listState = activitiesListState,
                    onTransaction = navController::navigateToTransactionScreen,
                )
                else -> SettingsScene(
                    scrollState = settingsScrollState,
                    onSecurity = navController::navigateToSecurityScreen,
                    onBridges = navController::navigateToBridgesScreen,
                    onDevelop = navController::navigateToDevelopScreen,
                    onCurrencies = navController::navigateToCurrenciesScreen,
                    onWallets = navController::navigateToWalletsScreen,
                    onNetworks = navController::navigateToNetworksScreen,
                    onAboutUs = navController::navigateToAboutUsScreen,
                )
            }
        }
    }
}