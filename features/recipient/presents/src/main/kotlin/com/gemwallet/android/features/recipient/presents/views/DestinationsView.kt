package com.gemwallet.android.features.recipient.presents.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.domains.asset.chain
import com.gemwallet.android.features.recipient.viewmodel.models.QrScanField
import com.gemwallet.android.features.recipient.viewmodel.models.RecipientError
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.theme.Spacer4
import com.wallet.core.primitives.NameRecord

fun LazyListScope.destinationView(
    asset: AssetInfo,
    hasMemo: Boolean,
    addressState: MutableState<String>,
    addressError: RecipientError,
    memoState: MutableState<String>,
    memoError: RecipientError,
    nameRecordState: MutableState<NameRecord?>,
    onQrScan: (QrScanField) -> Unit,
) {
    item {
        Column {
            AddressChainField(
                chain = asset.asset.chain,
                value = addressState.value,
                label = stringResource(id = R.string.transfer_recipient_address_field),
                error = recipientErrorString(error = addressError),
                onValueChange = { input, nameRecord ->
                    addressState.value = input
                    nameRecordState.value = nameRecord
                },
                onQrScanner = { onQrScan(QrScanField.Address) }
            )
            if (hasMemo) {
                Spacer4()
                MemoTextField(
                    value = memoState.value,
                    label = stringResource(id = R.string.transfer_memo),
                    onValueChange = { memoState.value = it },
                    error = memoError,
                    onQrScanner = { onQrScan(QrScanField.Address) },
                )
            }
        }
    }
}