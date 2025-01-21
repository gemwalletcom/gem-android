package com.gemwallet.android.features.stake.validators.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.features.stake.validators.model.ValidatorsUIState
import com.gemwallet.android.features.stake.validators.viewmodels.ValidatorsViewModel
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.FatalStateScene
import com.gemwallet.android.ui.components.LoadingScene
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
        ValidatorsUIState.Fatal -> FatalStateScene(
            title = stringResource(id = R.string.stake_validators),
            message = "Error load validators",
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