package com.gemwallet.android.features.confirm.views

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.features.confirm.models.ConfirmSceneState
import com.gemwallet.android.features.confirm.viewmodels.ConfirmViewModel
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.ui.components.FatalStateScene
import com.gemwallet.android.ui.components.LoadingScene
import com.gemwallet.android.ui.components.titles.getTitle

@Composable
fun ConfirmScreen(
    params: ConfirmParams,
    onFinish: (String) -> Unit,
    onCancel: () -> Unit,
    viewModel: ConfirmViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(params.hashCode()) {
        viewModel.init(params)

        onDispose { }
    }

    BackHandler(true) {
        onCancel()
    }

    when (uiState) {
        is ConfirmSceneState.Fatal -> FatalStateScene(
            title = stringResource(params.getTxType().getTitle()),
            message = (uiState as ConfirmSceneState.Fatal).error.stringResource(),
            onCancel = onCancel,
            onTryAgain = { viewModel.init(params) }
        )
        ConfirmSceneState.Loading -> LoadingScene(
            title = stringResource(params.getTxType().getTitle()),
            onCancel = onCancel,
        )
        is ConfirmSceneState.Loaded -> ConfirmScene(
            state = uiState as ConfirmSceneState.Loaded,
            onSend = viewModel::send,
            onFinish = onFinish,
            onCancel = onCancel,
        )
    }
}