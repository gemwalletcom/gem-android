package com.gemwallet.android.features.confirm.views

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.R
import com.gemwallet.android.features.confirm.models.ConfirmError
import com.gemwallet.android.features.confirm.models.ConfirmSceneState
import com.gemwallet.android.features.confirm.viewmodels.ConfirmViewModel
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.ui.components.AmountListHead
import com.gemwallet.android.ui.components.FatalStateScene
import com.gemwallet.android.ui.components.LoadingScene
import com.gemwallet.android.ui.components.MainActionButton
import com.gemwallet.android.ui.components.Scene
import com.gemwallet.android.ui.components.SwapListHead
import com.gemwallet.android.ui.components.Table
import com.gemwallet.android.ui.components.titles.getTitle
import com.gemwallet.android.ui.theme.Spacer16
import com.wallet.core.primitives.TransactionType

@Composable
fun ConfirmScreen(
    params: ConfirmParams? = null,
    onFinish: (String) -> Unit,
    onCancel: () -> Unit,
    viewModel: ConfirmViewModel = hiltViewModel(),
) {
//    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val amountModel by viewModel.amountUIModel.collectAsStateWithLifecycle()
    val txInfoUIModel by viewModel.txInfoUIModel.collectAsStateWithLifecycle()
    val feeModel by viewModel.feeUIModel.collectAsStateWithLifecycle()

    DisposableEffect(params.hashCode()) {
        if (params != null) {
            viewModel.init(params)
        }

        onDispose { }
    }

    BackHandler(true) {
        onCancel()
    }

//    val state = uiState as? ConfirmSceneState.Loaded ?: return

    Scene(
        title = stringResource(amountModel?.txType?.getTitle() ?: R.string.transfer_confirm),
        onClose = onCancel,
        mainAction = {
            MainActionButton(
                title = "",//state.error.stringResource(),
                enabled = true, //state.error == ConfirmError.None,
                loading = false,//state.sending,
                onClick = viewModel::send,
            )
        }
    ) {
        if (amountModel?.txType == TransactionType.Swap) {
            SwapListHead(
                fromAsset = amountModel?.fromAsset,
                fromValue = amountModel?.fromAmount!!,
                toAsset = amountModel?.toAsset!!,
                toValue = amountModel?.toAmount!!,
                currency = amountModel?.currency,
            )
        } else {
            AmountListHead(
                amount = amountModel?.amount ?: "",
                equivalent = amountModel?.amountEquivalent,
            )
        }
        Table(txInfoUIModel)
        Spacer16()
        Table(feeModel)
//        if (!state.txHash.isNullOrEmpty()) {
//            onFinish(state.txHash)
//        }
    }

//    when (uiState) {
//        is ConfirmSceneState.Fatal -> FatalStateScene(
//            title = stringResource(params.getTxType().getTitle()),
//            message = (uiState as ConfirmSceneState.Fatal).error.stringResource(),
//            onCancel = onCancel,
//            onTryAgain = { viewModel.init(params) }
//        )
//        ConfirmSceneState.Loading -> LoadingScene(
//            title = stringResource(params.getTxType().getTitle()),
//            onCancel = onCancel,
//        )
//        is ConfirmSceneState.Loaded -> ConfirmScene(
//            state = uiState as ConfirmSceneState.Loaded,
//            onSend = viewModel::send,
//            onFinish = onFinish,
//            onCancel = onCancel,
//        )
//    }
}