package com.gemwallet.features.confirm.presents.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.model.Fee
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.clickable
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyNetworkFee
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.components.list_item.property.itemsPositioned
import com.gemwallet.android.ui.components.screen.ModalBottomSheet
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.theme.trailingIconSmall
import com.gemwallet.features.confirm.models.FeeRateUIModel
import com.gemwallet.features.confirm.models.FeeUIModel
import com.wallet.core.primitives.FeePriority

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeeDetails(
    currentFee: FeeUIModel.FeeInfo?,
    fee: List<Fee>,
    onSelect: (FeePriority) -> Unit,
    onCancel: () -> Unit,
) {
    currentFee ?: return
    ModalBottomSheet(onCancel) {
        LazyColumn {
            item {
                PropertyNetworkFee(
                    currentFee.feeAsset.name,
                    currentFee.feeAsset.symbol,
                    currentFee.cryptoAmount,
                    currentFee.fiatAmount,
                    showedCryptoAmount = true,
                )
            }

            if (fee.size > 1) {
                itemsPositioned(fee) { position, item ->
                    FeePriorityView(
                        FeeRateUIModel(item),
                        item.priority == currentFee.priority,
                        position,
                    ) { onSelect(item.priority) }
                }
            }
        }
    }
}

@Composable
private fun FeePriorityView(fee: FeeRateUIModel, isSelected: Boolean, position: ListPosition, onClick: () -> Unit) {
    PropertyItem(
        modifier = Modifier.clickable(onClick = onClick),
        title = {
            PropertyTitleText(
                text = when (fee.priority) {
                    FeePriority.Fast -> "\uD83D\uDE80  ${stringResource(R.string.fee_rates_fast)}"
                    FeePriority.Normal -> "\uD83D\uDC8E  ${stringResource(R.string.fee_rates_normal)}"
                    FeePriority.Slow -> "\uD83D\uDC22  ${stringResource(R.string.fee_rates_slow)}"
                },
                trailing = {
                    if (isSelected) {
                        Icon(
                            modifier = Modifier.size(trailingIconSmall),
                            imageVector = Icons.Outlined.Done,
                            contentDescription = ""
                        )
                    } else {
                        Box(modifier = Modifier.size(trailingIconSmall))
                    }
                }
            )
        },
        data = {
            PropertyDataText(
                fee.price
            )
        },
        listPosition = position,
    )
}