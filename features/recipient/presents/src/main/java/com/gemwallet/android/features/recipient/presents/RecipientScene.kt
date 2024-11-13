package com.gemwallet.android.features.recipient.presents

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.features.recipient.presents.views.destinationView
import com.gemwallet.android.features.recipient.viewmodel.RecipientViewModel
import com.gemwallet.android.features.recipient.viewmodel.models.QrScanField
import com.gemwallet.android.features.recipient.viewmodel.models.RecipientError
import com.gemwallet.android.localize.R
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.ui.components.buttons.MainActionButton
import com.gemwallet.android.ui.components.keyboardAsState
import com.gemwallet.android.ui.components.qrCodeRequest
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.actions.AmountTransactionAction
import com.gemwallet.android.ui.models.actions.CancelAction
import com.gemwallet.android.ui.models.actions.ConfirmTransactionAction
import com.wallet.core.primitives.NameRecord

@Composable
fun RecipientScene(
    cancelAction: CancelAction,
    amountAction: AmountTransactionAction,
    confirmAction: ConfirmTransactionAction,
) {
    val viewModel: RecipientViewModel = hiltViewModel()

    val assetInfo by viewModel.asset.collectAsStateWithLifecycle()
    val addressError by viewModel.addressError.collectAsStateWithLifecycle()
    val memoError by viewModel.memoErrorState.collectAsStateWithLifecycle()

    var scan by remember { mutableStateOf(QrScanField.None) }

    if (scan != QrScanField.None) {
        qrCodeRequest(
            { scan = QrScanField.None },
            {
                viewModel.setQrData(scan, it, confirmAction)
                scan = QrScanField.None
            }
        )
        return
    }

    RecipientScene(
        assetInfo = assetInfo ?: return, // TODO: Improve it.
        hasMemo = viewModel.hasMemo(),
        addressState = viewModel.addressState, // TODO: Change it to textfieldstate
        memoState = viewModel.memoState,
        nameRecordState = viewModel.nameRecordState,
        addressError = addressError,
        memoError = memoError,
        onQrScan = { scan = it },
        onNext = { viewModel.onNext(amountAction) },
        onCancel = cancelAction,
    )
}

@Composable
fun RecipientScene(
    assetInfo: AssetInfo,
    hasMemo: Boolean,
    addressState: MutableState<String>,
    memoState: MutableState<String>,
    nameRecordState: MutableState<NameRecord?>,
    addressError: RecipientError,
    memoError: RecipientError,
    onQrScan: (QrScanField) -> Unit,
    onNext: () -> Unit,
    onCancel: CancelAction,
) {
    val isKeyBoardOpen by keyboardAsState()
    val isSmallScreen = LocalConfiguration.current.screenHeightDp.dp < 680.dp

    Scene(
        title = stringResource(id = R.string.transfer_send_title),
        onClose = { onCancel() },
        mainAction = {
            if (!isKeyBoardOpen || !isSmallScreen) {
                MainActionButton(
                    title = stringResource(id = R.string.common_continue),
                    onClick = onNext,
                )
            }
        },
        actions = {
            TextButton(onClick = onNext,
                colors = ButtonDefaults.textButtonColors()
                    .copy(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.common_continue).uppercase())
            }
        }
    ) {
        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
            destinationView(
                asset = assetInfo,
                hasMemo = hasMemo,
                addressState = addressState,
                addressError = addressError,
                memoState = memoState,
                memoError = memoError,
                nameRecordState = nameRecordState,
                onQrScan = onQrScan,
            )
        }
    }
}