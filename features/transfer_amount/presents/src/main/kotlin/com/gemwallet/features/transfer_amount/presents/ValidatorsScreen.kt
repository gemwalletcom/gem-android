package com.gemwallet.features.transfer_amount.presents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.screen.FatalStateScene
import com.gemwallet.android.ui.components.screen.LoadingScene
import com.gemwallet.features.transfer_amount.models.ValidatorsUIState
import com.gemwallet.features.transfer_amount.viewmodels.ValidatorsViewModel
import com.wallet.core.primitives.Chain

@Composable
fun ValidatorsScreen(
    chain: Chain,
    selectedValidatorId: String,
    viewModel: ValidatorsViewModel = hiltViewModel(),
    onCancel: () -> Unit,
    onSelect: (String) -> Unit
) {
    DisposableEffect(chain) {

        viewModel.init(chain)

        onDispose {  }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (uiState) {
        ValidatorsUIState.Empty -> FatalStateScene(
            title = stringResource(id = R.string.stake_validators),
            message = "Validators not found",
            onCancel = onCancel
        )
        is ValidatorsUIState.Loaded -> ValidatorsScene(
            uiState = uiState as ValidatorsUIState.Loaded,
            selectedValidatorId = selectedValidatorId,
            onCancel = onCancel,
            onSelect = onSelect,
        )
        ValidatorsUIState.Loading -> LoadingScene(stringResource(id = R.string.stake_validators), onCancel)
    }
}