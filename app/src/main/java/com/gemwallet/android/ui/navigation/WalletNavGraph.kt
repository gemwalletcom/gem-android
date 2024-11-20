package com.gemwallet.android.ui.navigation

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
import com.gemwallet.android.features.amount.navigation.amount
import com.gemwallet.android.features.amount.navigation.navigateToAmountScreen
import com.gemwallet.android.features.amount.navigation.navigateToSendScreen
import com.gemwallet.android.features.asset.navigation.assetChartScreen
import com.gemwallet.android.features.asset.navigation.assetRoute
import com.gemwallet.android.features.asset.navigation.assetScreen
import com.gemwallet.android.features.asset.navigation.navigateToAssetChartScreen
import com.gemwallet.android.features.asset.navigation.navigateToAssetScreen
import com.gemwallet.android.features.assets.navigation.assetsRoute
import com.gemwallet.android.features.assets.navigation.assetsScreen
import com.gemwallet.android.features.bridge.navigation.bridgesScreen
import com.gemwallet.android.features.bridge.navigation.navigateToBridgeScreen
import com.gemwallet.android.features.bridge.navigation.navigateToBridgesScreen
import com.gemwallet.android.features.buy.navigation.buyScreen
import com.gemwallet.android.features.buy.navigation.navigateToBuyScreen
import com.gemwallet.android.features.confirm.navigation.confirm
import com.gemwallet.android.features.confirm.navigation.navigateToConfirmScreen
import com.gemwallet.android.features.create_wallet.navigation.assetsManageScreen
import com.gemwallet.android.features.create_wallet.navigation.createWalletScreen
import com.gemwallet.android.features.create_wallet.navigation.navigateToAssetsManageScreen
import com.gemwallet.android.features.create_wallet.navigation.navigateToCreateWalletScreen
import com.gemwallet.android.features.import_wallet.navigation.importWalletScreen
import com.gemwallet.android.features.import_wallet.navigation.navigateToImportWalletScreen
import com.gemwallet.android.features.main.views.MainScreen
import com.gemwallet.android.features.onboarding.OnboardingDest
import com.gemwallet.android.features.receive.navigation.navigateToReceiveScreen
import com.gemwallet.android.features.receive.navigation.receiveScreen
import com.gemwallet.android.features.settings.navigation.navigateToAboutUsScreen
import com.gemwallet.android.features.settings.navigation.navigateToCurrenciesScreen
import com.gemwallet.android.features.settings.navigation.navigateToDevelopScreen
import com.gemwallet.android.features.settings.navigation.navigateToNetworksScreen
import com.gemwallet.android.features.settings.navigation.navigateToPriceAlertsScreen
import com.gemwallet.android.features.settings.navigation.navigateToSecurityScreen
import com.gemwallet.android.features.settings.navigation.settingsRoute
import com.gemwallet.android.features.settings.navigation.settingsScreen
import com.gemwallet.android.features.stake.navigation.navigateToDelegation
import com.gemwallet.android.features.stake.navigation.navigateToStake
import com.gemwallet.android.features.stake.navigation.stake
import com.gemwallet.android.features.stake.navigation.stakeRoute
import com.gemwallet.android.features.swap.navigation.navigateToSwap
import com.gemwallet.android.features.swap.navigation.swap
import com.gemwallet.android.features.swap.navigation.swapRoute
import com.gemwallet.android.features.transactions.navigation.activitiesRoute
import com.gemwallet.android.features.transactions.navigation.activitiesScreen
import com.gemwallet.android.features.transactions.navigation.navigateToTransactionScreen
import com.gemwallet.android.features.transactions.navigation.transactionScreen
import com.gemwallet.android.features.wallet.navigation.navigateToPhraseScreen
import com.gemwallet.android.features.wallet.navigation.navigateToWalletScreen
import com.gemwallet.android.features.wallet.navigation.walletScreen
import com.gemwallet.android.features.wallets.navigation.navigateToWalletsScreen
import com.gemwallet.android.features.wallets.navigation.walletsScreen
import com.gemwallet.android.ui.navigation.routes.SendSelect
import com.gemwallet.android.ui.navigation.routes.Transfer
import com.gemwallet.android.ui.navigation.routes.navigateToRecipientInput
import com.gemwallet.android.ui.navigation.routes.recipientInput
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
    val currentTab = remember {
        mutableStateOf(assetsRoute)
    }
    NavHost(
        modifier = modifier.semantics { testTagsAsResourceId = true },
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
            val reset = it.arguments?.getBoolean("reset") ?: false
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
                onSwapClick = navController::navigateToSwap,
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
                onStake = navController::navigateToStake,
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
                            assetRoute -> NavigateAfterConfirm.Transfer(assetId).navigate(navController)
                            stakeRoute -> NavigateAfterConfirm.Stake().navigate(navController)
                            swapRoute -> NavigateAfterConfirm.Swap(assetId).navigate(navController)
                        }
                    },
                    cancelAction = onCancel
                )
            }

            buyScreen(
                onCancel = onCancel,
                onBuy = navController::navigateToBuyScreen
            )

            receiveScreen(
                onCancel = onCancel,
                onReceive = navController::navigateToReceiveScreen,
            )

            walletsScreen(
                onCreateWallet = navController::navigateToCreateWalletScreen,
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

        navigation(
            startDestination = activitiesRoute,
            route = "activities-group",
        ) {
            activitiesScreen(
                onTransaction = navController::navigateToTransactionScreen
            )

            transactionScreen(
                onCancel = onCancel
            )
        }

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
            onCancel = onCancel,
            onCreated = {
                navController.navigateToRoot()
                currentTab.value = assetsRoute
            }
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

    class Stake() : NavigateAfterConfirm {

        override fun navigate(navController: NavController) {
            navController.popBackStack(stakeRoute, true)
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