package com.gemwallet.android.features.amount.views

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.gemwallet.android.R
import com.gemwallet.android.features.amount.model.AmountScreenState
import com.gemwallet.android.features.amount.viewmodels.AmountViewModel
import com.gemwallet.android.features.stake.validators.views.ValidatorsScreen
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.ui.components.FatalStateScene
import com.gemwallet.android.ui.components.LoadingScene
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.TransactionType

@Composable
fun AmountScreen(
    assetId: AssetId,
    txType: TransactionType,
    destinationAddress: String,
    addressDomain: String,
    memo: String,
    delegationId: String,
    validatorId: String,
    onCancel: () -> Unit,
    onConfirm: (ConfirmParams) -> Unit,
    viewModel: AmountViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    DisposableEffect(assetId) {
        viewModel.init(
            assetId = assetId,
            destinationAddress = destinationAddress,
            addressDomain = addressDomain,
            memo = memo,
            delegationId = delegationId,
            validatorId = validatorId,
            txType = txType,
        )

        onDispose {  }
    }

    var isSelectValidator by remember {
        mutableStateOf(false)
    }

    when (uiState) {
        AmountScreenState.Fatal -> FatalStateScene(
            title = stringResource(id = R.string.transfer_amount_title),
            message = "Stake error",
            onCancel = onCancel,
        )
        is AmountScreenState.Loaded -> AnimatedContent(
            isSelectValidator,
            transitionSpec = {
                if (isSelectValidator) {
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
            label = "stake"
        ) { state ->
            when (state) {
                true -> ValidatorsScreen(
                    chain = (uiState as AmountScreenState.Loaded).assetId.chain,
                    selectedValidatorId = (uiState as AmountScreenState.Loaded).validator?.id ?: "",
                    onCancel = { isSelectValidator = false },
                    onSelect = {
                        isSelectValidator = false
                        viewModel.updateValidator(it)
                    }
                )
                false -> AmountScene(
                    amount = viewModel.amount,
                    uiState = uiState as AmountScreenState.Loaded,
                    onNext = { viewModel.onNext(onConfirm) },
                    onAmount = viewModel::updateAmount,
                    onMaxAmount = viewModel::onMaxAmount,
                    onCancel = onCancel,
                ) {
                    isSelectValidator = !isSelectValidator
                }
            }
        }
        AmountScreenState.Loading -> LoadingScene(
            title = stringResource(id = R.string.transfer_amount_title),
            onCancel = onCancel,
        )
    }

    BackHandler(isSelectValidator) {
        isSelectValidator = false
    }
}