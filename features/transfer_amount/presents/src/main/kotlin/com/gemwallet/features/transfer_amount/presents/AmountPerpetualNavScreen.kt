package com.gemwallet.features.transfer_amount.presents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.features.transfer_amount.viewmodels.PerpetualAmountViewModel

@Composable
fun AmountPerpetualNavScreen(
    onClose: () -> Unit,
    viewModel: PerpetualAmountViewModel = hiltViewModel()
) {
    val params by viewModel.params.collectAsStateWithLifecycle()
    val assetInfo by viewModel.assetInfo.collectAsStateWithLifecycle()
    val error by viewModel.amountError.collectAsStateWithLifecycle()
    val equivalent by viewModel.amountEquivalent.collectAsStateWithLifecycle()
    val availableBalance by viewModel.availableBalanceFormatted.collectAsStateWithLifecycle()
    val reserveForFee by viewModel.reserveForFee.collectAsStateWithLifecycle()
    val amountInputType by viewModel.amountInputType.collectAsStateWithLifecycle()

    AmountScene(
        amount = viewModel.amount,
        amountInputType = amountInputType,
        txType = params?.txType ?: return,
        asset = assetInfo?.asset ?: return,
        currency = assetInfo?.price?.currency ?: return,
        error = error,
        equivalent = equivalent,
        availableBalance = availableBalance,
        reserveForFee = reserveForFee?.toString(),
        onNext = {},
        onInputAmount = viewModel::updateAmount,
        onInputTypeClick = viewModel::switchInputType,
        onMaxAmount = viewModel::onMaxAmount,
        onCancel = onClose,
        onResourceSelect = {},
        onValidator = {}
    )
}