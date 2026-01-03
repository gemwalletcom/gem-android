package com.gemwallet.android.features.main.views

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.gemwallet.android.features.activities.presents.list.TransactionsNavScreen
import com.gemwallet.android.features.create_wallet.navigation.navigateToAssetsManageScreen
import com.gemwallet.android.features.create_wallet.navigation.navigateToAssetsSearchScreen
import com.gemwallet.android.features.main.models.BottomNavItem
import com.gemwallet.android.features.main.viewmodels.MainScreenViewModel
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.models.navigation.assetsRoute
import com.gemwallet.android.ui.navigation.routes.navigateToAboutUsScreen
import com.gemwallet.android.ui.navigation.routes.navigateToActivitiesScreen
import com.gemwallet.android.ui.navigation.routes.navigateToAssetScreen
import com.gemwallet.android.ui.navigation.routes.navigateToAssetsScreen
import com.gemwallet.android.ui.navigation.routes.navigateToBridgesScreen
import com.gemwallet.android.ui.navigation.routes.navigateToBuyScreen
import com.gemwallet.android.ui.navigation.routes.navigateToDevelopScreen
import com.gemwallet.android.ui.navigation.routes.navigateToNftAsset
import com.gemwallet.android.ui.navigation.routes.navigateToNftCollection
import com.gemwallet.android.ui.navigation.routes.navigateToPerpetualsScreen
import com.gemwallet.android.ui.navigation.routes.navigateToPreferences
import com.gemwallet.android.ui.navigation.routes.navigateToPriceAlertsScreen
import com.gemwallet.android.ui.navigation.routes.navigateToReceiveScreen
import com.gemwallet.android.ui.navigation.routes.navigateToRecipientInput
import com.gemwallet.android.ui.navigation.routes.navigateToReferralScreen
import com.gemwallet.android.ui.navigation.routes.navigateToSecurityScreen
import com.gemwallet.android.ui.navigation.routes.navigateToSettingsScreen
import com.gemwallet.android.ui.navigation.routes.navigateToSupport
import com.gemwallet.android.ui.navigation.routes.navigateToSwap
import com.gemwallet.android.ui.navigation.routes.navigateToTransactionScreen
import com.gemwallet.android.ui.navigation.routes.navigateToWalletsScreen
import com.gemwallet.android.ui.navigation.routes.nftRoute
import com.gemwallet.android.ui.navigation.routes.settingsRoute
import com.gemwallet.android.ui.navigation.routes.transactionsRoute
import com.gemwallet.features.assets.views.AssetsScreen
import com.gemwallet.features.nft.presents.NftListScene
import com.gemwallet.features.settings.settings.presents.views.SettingsScene
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    navController: NavController,
    currentTab: MutableState<String>,
    viewModel: MainScreenViewModel = hiltViewModel()
) {
    val pendingCount by viewModel.pendingTxCount.collectAsStateWithLifecycle()
    val collectionsAvailable by viewModel.collectionsAvailable.collectAsStateWithLifecycle()

    BackHandler(currentTab.value == transactionsRoute || currentTab.value == settingsRoute) {
        currentTab.value = assetsRoute
    }
    val assetsListState = rememberLazyListState()
    val activitiesListState = rememberLazyListState()
    val nftListState = rememberLazyGridState()
    val settingsScrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val walletLabel = stringResource(R.string.common_wallet)
    val collectionsLabel = stringResource(R.string.nft_collections)
    val activitiesLabel = stringResource(R.string.activity_title)
    val settingsLabel = stringResource(R.string.settings_title)

    val navItems = remember(pendingCount) {
        listOfNotNull(
            BottomNavItem(
                label = walletLabel,
                icon = Icons.Default.Wallet,
                route = assetsRoute,
                testTag = "mainTab",
                navigate = { navigateToAssetsScreen(it) }
            ),
            if (collectionsAvailable) BottomNavItem(
                label = collectionsLabel,
                icon = Icons.Default.EmojiEvents,
                route = nftRoute,
                testTag = "nftTab",
                navigate = { navigateToSettingsScreen(it) }
            ) else null,
            BottomNavItem(
                label = activitiesLabel,
                icon = Icons.Default.ElectricBolt,
                route = transactionsRoute,
                badge = pendingCount,
                testTag = "activitiesTab",
                navigate = { navigateToActivitiesScreen(navOptions = it) }
            ),
            BottomNavItem(
                label = settingsLabel,
                icon = Icons.Default.Settings,
                route = settingsRoute,
                testTag = "settingsTab",
                navigate = { navigateToSettingsScreen(it) }
            ),
        )
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        bottomBar = {
            Column {
                HorizontalDivider(thickness = 0.5.dp)
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                ) {
                    navItems.forEach { item ->
                        NavigationBarItem(
                            modifier = Modifier.testTag(item.testTag),
                            selected = item.route == currentTab.value,
                            onClick = {
                                val prevRoute = currentTab.value
                                currentTab.value = item.route
                                coroutineScope.launch {
                                    when (prevRoute) {
                                        assetsRoute -> assetsListState.animateScrollToItem(0)
                                        transactionsRoute -> activitiesListState.animateScrollToItem(0)
                                        nftRoute -> nftListState.animateScrollToItem(0)
                                        settingsRoute -> settingsScrollState.animateScrollTo(0)
                                        else -> null
                                    }
                                }
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
                                    BadgedBox(
                                        badge = {
                                            if (!item.badge.isNullOrEmpty()) {
                                                Badge(
                                                    modifier = Modifier.offset(x = 6.dp, y = 0.dp)
                                                ) {
                                                    Text(text = item.badge)
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            modifier = modifier,
                                            imageVector = item.icon,
                                            contentDescription = item.label,
                                        )

                                    }
                                }
                            },
                            label = { Text(item.label, maxLines = 1, overflow = TextOverflow.MiddleEllipsis) },
                            colors = NavigationBarItemDefaults.colors().copy(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                unselectedIconColor = MaterialTheme.colorScheme.secondary,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                                selectedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            )
                        )
                    }
                }
            }
        }
    ) {
        Box(modifier = Modifier.padding(bottom = it.calculateBottomPadding())) {
            when (currentTab.value) {
                assetsRoute -> AssetsScreen(
                    onShowWallets = navController::navigateToWalletsScreen,
                    onManage = navController::navigateToAssetsManageScreen,
                    onSearch = navController::navigateToAssetsSearchScreen,
                    onSendClick = navController::navigateToRecipientInput,
                    onReceiveClick = navController::navigateToReceiveScreen,
                    onBuyClick = navController::navigateToBuyScreen,
                    onSwapClick = navController::navigateToSwap,
                    onAssetClick = navController::navigateToAssetScreen,
                    listState = assetsListState,
                )
                transactionsRoute -> TransactionsNavScreen(
                    listState = activitiesListState,
                    onTransaction = navController::navigateToTransactionScreen,
                )
                nftRoute -> NftListScene(
                    listState = nftListState,
                    cancelAction = null,
                    collectionAction = navController::navigateToNftCollection,
                    assetAction = navController::navigateToNftAsset,
                )
                else -> SettingsScene(
                    scrollState = settingsScrollState,
                    onSecurity = navController::navigateToSecurityScreen,
                    onBridges = navController::navigateToBridgesScreen,
                    onDevelop = navController::navigateToDevelopScreen,
                    onWallets = navController::navigateToWalletsScreen,
                    onPriceAlerts = navController::navigateToPriceAlertsScreen,
                    onSupport = navController::navigateToSupport,
                    onPerpetual = navController::navigateToPerpetualsScreen,
                    onAboutUs = navController::navigateToAboutUsScreen,
                    onReferral = navController::navigateToReferralScreen,
                    onPreferences = navController::navigateToPreferences,
                )
            }
        }
    }
}