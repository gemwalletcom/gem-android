package com.gemwallet.android.features.activities.presents.details

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.domains.asset.chain
import com.gemwallet.android.domains.transaction.values.TransactionDetailsValue
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.dialog.DialogBar
import com.gemwallet.android.ui.components.list_item.property.PropertyNetworkFee
import com.gemwallet.android.ui.components.screen.ModalBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FeeDetailsDialog(
    model: TransactionDetailsValue.Fee?,
    onCancel: () -> Unit,
) {
    model ?: return
    ModalBottomSheet(
        onDismissRequest = onCancel,
        dragHandle = {
            DialogBar(stringResource(R.string.common_done), onCancel)
        }
    ) {
        model.asset.chain.asset().let {
            PropertyNetworkFee(
                networkTitle = it.name,
                networkSymbol = it.symbol,
                feeCrypto = model.value,
                feeFiat = model.equivalent,
                showedCryptoAmount = true
            )
        }
    }
}