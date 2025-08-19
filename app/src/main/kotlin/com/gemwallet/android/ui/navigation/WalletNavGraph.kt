package com.gemwallet.android.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import androidx.navigation.navigation
import com.gemwallet.android.features.add_asset.navigation.addAssetScreen
import com.gemwallet.android.features.add_asset.navigation.navigateToAddAssetScreen
import com.gemwallet.android.features.assets.navigation.assetsRoute
import com.gemwallet.android.features.assets.navigation.assetsScreen
import com.gemwallet.android.features.bridge.navigation.bridgesScreen
import com.gemwallet.android.features.bridge.navigation.navigateToBridgeScreen
import com.gemwallet.android.features.bridge.navigation.navigateToBridgesScreen
import com.gemwallet.android.features.buy.navigation.fiatScreen
import com.gemwallet.android.features.buy.navigation.navigateToBuyScreen
import com.gemwallet.android.features.confirm.navigation.confirm
import com.gemwallet.android.features.confirm.navigation.navigateToConfirmScreen
import com.gemwallet.android.features.create_wallet.navigation.assetsManageScreen
import com.gemwallet.android.features.create_wallet.navigation.createWalletScreen
import com.gemwallet.android.features.create_wallet.navigation.navigateToAssetsManageScreen
import com.gemwallet.android.features.create_wallet.navigation.navigateToCreateWalletRulesScreen
import com.gemwallet.android.features.create_wallet.navigation.navigateToCreateWalletScreen
import com.gemwallet.android.features.import_wallet.navigation.importWalletScreen
import com.gemwallet.android.features.import_wallet.navigation.navigateToImportWalletScreen
import com.gemwallet.android.features.main.views.MainScreen
import com.gemwallet.android.features.onboarding.OnboardingDest
import com.gemwallet.android.ui.navigation.routes.navigateToReceiveScreen
import com.gemwallet.android.ui.navigation.routes.receiveScreen
import com.gemwallet.android.features.settings.navigation.navigateToAboutUsScreen
import com.gemwallet.android.features.settings.navigation.navigateToCurrenciesScreen
import com.gemwallet.android.features.settings.navigation.navigateToDevelopScreen
import com.gemwallet.android.features.settings.navigation.navigateToNetworksScreen
import com.gemwallet.android.features.settings.navigation.navigateToPriceAlertsScreen
import com.gemwallet.android.features.settings.navigation.navigateToSecurityScreen
import com.gemwallet.android.features.settings.navigation.settingsRoute
import com.gemwallet.android.features.settings.navigation.settingsScreen
import com.gemwallet.android.features.wallets.navigation.navigateToWalletsScreen
import com.gemwallet.android.features.wallets.navigation.walletsScreen
import com.gemwallet.android.ui.components.animation.enterTransition
import com.gemwallet.android.ui.components.animation.exitTransition
import com.gemwallet.android.ui.components.animation.popEnterTransition
import com.gemwallet.android.ui.components.animation.popExitTransition
import com.gemwallet.android.ui.navigation.routes.SendSelect
import com.gemwallet.android.ui.navigation.routes.Transfer
import com.gemwallet.android.ui.navigation.routes.activitiesScreen
import com.gemwallet.android.ui.navigation.routes.amount
import com.gemwallet.android.ui.navigation.routes.assetChartScreen
import com.gemwallet.android.ui.navigation.routes.assetRoutePath
import com.gemwallet.android.ui.navigation.routes.assetScreen
import com.gemwallet.android.ui.navigation.routes.navigateToAmountScreen
import com.gemwallet.android.ui.navigation.routes.navigateToAssetChartScreen
import com.gemwallet.android.ui.navigation.routes.navigateToAssetScreen
import com.gemwallet.android.ui.navigation.routes.navigateToDelegation
import com.gemwallet.android.ui.navigation.routes.navigateToNftAsset
import com.gemwallet.android.ui.navigation.routes.navigateToNftCollection
import com.gemwallet.android.ui.navigation.routes.navigateToPhraseScreen
import com.gemwallet.android.ui.navigation.routes.navigateToRecipientInput
import com.gemwallet.android.ui.navigation.routes.navigateToSendScreen
import com.gemwallet.android.ui.navigation.routes.navigateToStake
import com.gemwallet.android.ui.navigation.routes.navigateToSwap
import com.gemwallet.android.ui.navigation.routes.navigateToTransactionScreen
import com.gemwallet.android.ui.navigation.routes.navigateToWalletScreen
import com.gemwallet.android.ui.navigation.routes.nftCollection
import com.gemwallet.android.ui.navigation.routes.recipientInput
import com.gemwallet.android.ui.navigation.routes.stake
import com.gemwallet.android.ui.navigation.routes.stakeRoute
import com.gemwallet.android.ui.navigation.routes.swap
import com.gemwallet.android.ui.navigation.routes.swapRoute
import com.gemwallet.android.ui.navigation.routes.transactionDetailsScreen
import com.gemwallet.android.ui.navigation.routes.walletScreen
import com.wallet.core.primitives.AssetId

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WalletNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String,
    onboard: @Composable () -> Unit,
) {
    val onCancel: () -> Unit = { navController.navigateUp() }
    val currentTab = remember { mutableStateOf(assetsRoute) }

    NavHost(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .semantics { testTagsAsResourceId = true },
        navController = navController,
        startDestination = startDestination,
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
    ) {
        composable(
            route = "/",
            arguments = listOf(
                navArgument("reset") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) {
            val reset = it.arguments?.getBoolean("reset") == true
            if (reset) {
                currentTab.value = assetsRoute
            }
            MainScreen(navController = navController, currentTab = currentTab)
        }

        navigation(
            startDestination = assetsRoute,
            route = "wallet-group",
        ) {
            assetsScreen(
                onShowWallets = navController::navigateToWalletsScreen,
                onShowAssetManage = navController::navigateToAssetsManageScreen,
                onSendClick = navController::navigateToRecipientInput,
                onReceiveClick = navController::navigateToReceiveScreen,
                onBuyClick = navController::navigateToBuyScreen,
                onAssetClick = navController::navigateToAssetScreen,
            )

            assetScreen(
                onCancel = onCancel,
                onTransfer = navController::navigateToRecipientInput,
                onReceive = navController::navigateToReceiveScreen,
                onBuy = navController::navigateToBuyScreen,
                onSwap = navController::navigateToSwap,
                onTransaction = navController::navigateToTransactionScreen,
                onChart = navController::navigateToAssetChartScreen,
                openNetwork = navController::navigateToAssetScreen,
                onStake = navController::navigateToStake,
                onConfirm = navController::navigateToConfirmScreen
            )

            assetChartScreen(
                onCancel = onCancel
            )

            assetsManageScreen(
                onAddAsset = navController::navigateToAddAssetScreen,
                onCancel = onCancel,
            )

            navigation<Transfer>(startDestination = SendSelect) {
                swap(
                    onConfirm = navController::navigateToConfirmScreen,
                    onCancel = onCancel
                )

                recipientInput(
                    cancelAction = onCancel,
                    recipientAction = navController::navigateToRecipientInput,
                    amountAction = navController::navigateToAmountScreen,
                    confirmAction = navController::navigateToConfirmScreen,
                )

                amount(
                    onCancel = onCancel,
                    onSend = navController::navigateToSendScreen,
                    onConfirm = navController::navigateToConfirmScreen,
                )

                confirm(
                    finishAction = { assetId, hash, route ->
                        when (route) {
                            assetRoutePath -> NavigateAfterConfirm.Transfer(assetId).navigate(navController)
                            stakeRoute -> NavigateAfterConfirm.Stake(assetId).navigate(navController)
                            swapRoute -> NavigateAfterConfirm.Swap(assetId).navigate(navController)
                        }
                    },
                    onBuy = navController::navigateToBuyScreen,
                    cancelAction = onCancel
                )
            }

            nftCollection(
                cancelAction = onCancel,
                collectionIdAction = navController::navigateToNftCollection,
                assetIdAction = navController::navigateToNftAsset,
                onRecipient = navController::navigateToRecipientInput
            )

            fiatScreen(
                cancelAction = onCancel,
                onBuy = navController::navigateToBuyScreen
            )

            receiveScreen(
                onCancel = onCancel,
                onReceive = navController::navigateToReceiveScreen,
            )

            walletsScreen(
                onCreateWallet = navController::navigateToCreateWalletRulesScreen,
                onImportWallet = navController::navigateToImportWalletScreen,
                onEditWallet = navController::navigateToWalletScreen,
                onSelectWallet = {
                    navController.navigateToRoot()
                    currentTab.value = assetsRoute
                },
                onBoard = {
                    navController.navigate(OnboardingDest.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                },
                onCancel = onCancel
            )

            walletScreen(
                onCancel = onCancel,
                onBoard = {
                    navController.navigate(OnboardingDest.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                },
                onPhraseShow = navController::navigateToPhraseScreen
            )

            stake(
                onAmount = navController::navigateToAmountScreen,
                onConfirm = navController::navigateToConfirmScreen,
                onDelegation = navController::navigateToDelegation,
                onCancel = onCancel,
            )

            addAssetScreen(
                onCancel = onCancel,
                onFinish = {
                    navController.navigateToRoot()
                    currentTab.value = assetsRoute
                }
            )
        }

        activitiesScreen(onTransaction = navController::navigateToTransactionScreen)

        transactionDetailsScreen(onCancel = onCancel)

        navigation(
            startDestination = settingsRoute,
            route = "settings-group"
        ) {
            bridgesScreen(
                onConnection = navController::navigateToBridgeScreen,
                onCancel = onCancel,
            )

            settingsScreen(
                onSecurity = navController::navigateToSecurityScreen,
                onCurrencies = navController::navigateToCurrenciesScreen,
                onBridges = navController::navigateToBridgesScreen,
                onDevelop = navController::navigateToDevelopScreen,
                onWallets = navController::navigateToWalletsScreen,
                onAboutUs = navController::navigateToAboutUsScreen,
                onNetworks = navController::navigateToNetworksScreen,
                onChart = navController::navigateToAssetChartScreen,
                onPriceAlerts = navController::navigateToPriceAlertsScreen,
                onCancel = onCancel,
            )
        }

        composable(OnboardingDest.route) {
            onboard()
        }

        createWalletScreen(
            onAcceptRules = navController::navigateToCreateWalletRulesScreen,
            onCreateWallet = navController::navigateToCreateWalletScreen,
            onCancel = onCancel,
            onCreated = {
                navController.navigateToRoot()
                currentTab.value = assetsRoute
            },
        )

        importWalletScreen(
            onCancel = onCancel,
            onImported = {
                navController.navigateToRoot()
                currentTab.value = assetsRoute
            },
            onSelectType = navController::navigateToImportWalletScreen,
        )
    }
}

fun NavController.navigateToRoot() {
    navigate(
        route = "/?reset=true",
        navOptions = navOptions {
            popUpTo(0) {
                inclusive = true
            }
        }
    )
}

sealed interface NavigateAfterConfirm {
    fun navigate(navController: NavController)

    class Transfer(private val assetId: AssetId) : NavigateAfterConfirm {

        override fun navigate(navController: NavController) {
            navController.navigateToAssetScreen(
                assetId,
                navOptions {
                    launchSingleTop = true
                    popUpTo(Transfer) {
                        inclusive = true
                    }
                }
            )
        }
    }

    class Stake(private val assetId: AssetId) : NavigateAfterConfirm {

        override fun navigate(navController: NavController) {
            navController.navigateToAssetScreen(
                assetId,
                navOptions {
                    launchSingleTop = true
                    popUpTo(Transfer) {
                        inclusive = true
                    }
                }
            )
        }
    }

    class Swap(private val assetId: AssetId) : NavigateAfterConfirm {

        override fun navigate(navController: NavController) {
            navController.navigateToAssetScreen(
                assetId,
                navOptions {
                    launchSingleTop = true
                    popUpTo(Transfer) {
                        inclusive = true
                    }
                }
            )
        }
    }
}