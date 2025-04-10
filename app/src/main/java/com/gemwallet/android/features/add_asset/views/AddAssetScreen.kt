package com.gemwallet.android.features.add_asset.views

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ext.asset
import com.gemwallet.android.features.add_asset.models.AddAssetUIState
import com.gemwallet.android.features.add_asset.viewmodels.AddAssetViewModel
import com.gemwallet.android.ui.components.QrCodeRequest

@Composable
fun AddAssetScree(
    onFinish: () -> Unit,
    onCancel: () -> Unit,
    viewModel: AddAssetViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val chains by viewModel.chains.collectAsStateWithLifecycle()
    val network by viewModel.chain.collectAsStateWithLifecycle()
    val token by viewModel.token.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()

    BackHandler(uiState.scene != AddAssetUIState.Scene.Form) {
        viewModel.cancelSelectChain()
        viewModel.cancelScan()
    }

    AnimatedContent(
        targetState = uiState.scene,
        transitionSpec = {
            if (uiState.scene != AddAssetUIState.Scene.Form) {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(350)
                ) togetherWith slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(350)
                )
            } else {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                ) togetherWith slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                )
            }
        },
        label = "phrase"
    ) {
        when (it) {
            AddAssetUIState.Scene.Form -> {
                AddAssetScene(
                    searchState = searchState,
                    addressState = viewModel.addressState,
                    network = network.asset(),
                    token = token,
                    onCancel = onCancel,
                    onScan = viewModel::onQrScan,
                    onAddAsset = { viewModel.addAsset(onFinish) },
                    onChainSelect = uiState.onSelectChain,
                )
            }
            AddAssetUIState.Scene.QrScanner -> {
                QrCodeRequest(
                    onResult = viewModel::setQrData,
                    onCancel = viewModel::cancelScan
                )
            }
            AddAssetUIState.Scene.SelectChain -> SelectChain(
                chains = chains,
                chainFilter = viewModel.chainFilter,
                onSelect = viewModel::setChain,
                onCancel = viewModel::cancelSelectChain,
            )
        }
    }
}