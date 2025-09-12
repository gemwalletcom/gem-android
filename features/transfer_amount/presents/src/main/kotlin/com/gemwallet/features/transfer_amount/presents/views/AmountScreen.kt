package com.gemwallet.features.transfer_amount.presents.views

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.screen.LoadingScene
import com.gemwallet.features.transfer_amount.viewmodels.AmountViewModel

@Composable
fun AmountScreen(
    onCancel: () -> Unit,
    onConfirm: (ConfirmParams) -> Unit,
    viewModel: AmountViewModel = hiltViewModel(),
) {
    val params by viewModel.params.collectAsStateWithLifecycle()
    val assetInfo by viewModel.assetInfo.collectAsStateWithLifecycle()
    val validatorState by viewModel.validatorState.collectAsStateWithLifecycle()
    val error by viewModel.errorUIState.collectAsStateWithLifecycle()
    val equivalent by viewModel.equivalentState.collectAsStateWithLifecycle()
    val availableBalance by viewModel.availableBalance.collectAsStateWithLifecycle()
    val amountPrefill by viewModel.prefillAmount.collectAsStateWithLifecycle()
    val amountInputType by viewModel.amountInputType.collectAsStateWithLifecycle()

    var isSelectValidator by remember {
        mutableStateOf(false)
    }

    BackHandler(isSelectValidator) {
        isSelectValidator = false
    }

    if (assetInfo == null) {
        LoadingScene(stringResource(id = R.string.transfer_amount_title), onCancel)
        return
    }

    AnimatedContent(
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
                chain = assetInfo?.asset?.id?.chain ?: return@AnimatedContent,
                selectedValidatorId = validatorState?.id!!,
                onCancel = { isSelectValidator = false },
                onSelect = {
                    isSelectValidator = false
                    viewModel.setDelegatorValidator(it)
                }
            )
            false -> AmountScene(
                amount = viewModel.amount,
                amountPrefill = amountPrefill,
                asset = assetInfo?.asset ?: return@AnimatedContent,
                currency = assetInfo?.price?.currency ?: com.wallet.core.primitives.Currency.USD,
                amountInputType = amountInputType,
                txType = params?.txType ?: return@AnimatedContent,
                validatorState = validatorState,
                error = error,
                equivalent = equivalent,
                availableBalance = availableBalance,
                onNext = { viewModel.onNext(onConfirm) },
                onInputAmount = viewModel::updateAmount,
                onMaxAmount = viewModel::onMaxAmount,
                onInputTypeClick = viewModel::switchInputType,
                onCancel = onCancel,
            ) {
                isSelectValidator = !isSelectValidator
            }
        }
    }
}