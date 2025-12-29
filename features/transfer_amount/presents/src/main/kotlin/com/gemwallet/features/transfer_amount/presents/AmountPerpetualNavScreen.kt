package com.gemwallet.features.transfer_amount.presents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.clickable
import com.gemwallet.android.ui.components.list_item.property.DataBadgeChevron
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.features.transfer_amount.presents.dialogs.SelectLeverageDialog
import com.gemwallet.features.transfer_amount.viewmodels.PerpetualAmountViewModel
import com.wallet.core.primitives.PerpetualDirection

@Composable
fun AmountPerpetualNavScreen(
    onConfirm: (ConfirmParams) -> Unit,
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
    val availableLeverages by viewModel.availableLeverages.collectAsStateWithLifecycle()
    val leverage by viewModel.leverage.collectAsStateWithLifecycle()

    var showLeverageSelect by remember { mutableStateOf(false) }

    AmountScene(
        title = when (params?.perpetualDirection) {
            PerpetualDirection.Short -> stringResource(R.string.perpetual_short)
            else -> stringResource(R.string.perpetual_long)
        },
        amount = viewModel.amount,
        amountInputType = amountInputType,
        txType = params?.txType ?: return,
        asset = assetInfo?.asset ?: return,
        currency = assetInfo?.price?.currency ?: return,
        error = error,
        equivalent = equivalent,
        availableBalance = availableBalance,
        reserveForFee = reserveForFee?.toString(),
        onNext = { viewModel.onNext(onConfirm) },
        onInputAmount = viewModel::updateAmount,
        onInputTypeClick = viewModel::switchInputType,
        onMaxAmount = viewModel::onMaxAmount,
        onCancel = onClose,
    ) {
        PropertyItem(
            modifier = Modifier.clickable { showLeverageSelect = true },
            title = { PropertyTitleText(R.string.perpetual_leverage) },
            data = { PropertyDataText("${leverage}x", badge = { DataBadgeChevron() }) },
            listPosition = ListPosition.Single,
        )
    }

    if (showLeverageSelect) {
        SelectLeverageDialog(
            leverages = availableLeverages,
            onDismiss = { showLeverageSelect = false },
            onSelect = viewModel::setLeverage
        )
    }
}