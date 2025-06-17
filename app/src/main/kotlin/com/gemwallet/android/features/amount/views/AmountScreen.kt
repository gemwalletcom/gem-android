package com.gemwallet.android.features.amount.views

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.features.amount.viewmodels.AmountViewModel
import com.gemwallet.android.features.stake.validators.views.ValidatorsScreen
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.screen.LoadingScene

@Composable
fun AmountScreen(
    onCancel: () -> Unit,
    onConfirm: (ConfirmParams) -> Unit,
    viewModel: AmountViewModel = hiltViewModel(),
) {
    val uiModel by viewModel.uiModel.collectAsStateWithLifecycle()
    val validatorState by viewModel.validatorState.collectAsStateWithLifecycle()
    val inputError by viewModel.inputErrorState.collectAsStateWithLifecycle()
    val amountError by viewModel.nextErrorState.collectAsStateWithLifecycle()
    val equivalent by viewModel.equivalentState.collectAsStateWithLifecycle()
    val availableBalance by viewModel.availableBalance.collectAsStateWithLifecycle()
    val amountPrefill by viewModel.prefillAmount.collectAsStateWithLifecycle()

    var isSelectValidator by remember {
        mutableStateOf(false)
    }

    BackHandler(isSelectValidator) {
        isSelectValidator = false
    }

    if (uiModel == null) {
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
                chain = uiModel?.asset?.id?.chain ?: return@AnimatedContent,
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
                uiModel = uiModel ?: return@AnimatedContent,
                validatorState = validatorState,
                inputError = inputError,
                amountError = amountError,
                equivalent = equivalent,
                availableBalance = availableBalance,
                onNext = { viewModel.onNext(onConfirm) },
                onAmount = viewModel::updateAmount,
                onMaxAmount = viewModel::onMaxAmount,
                onCancel = onCancel,
            ) {
                isSelectValidator = !isSelectValidator
            }
        }
    }
}