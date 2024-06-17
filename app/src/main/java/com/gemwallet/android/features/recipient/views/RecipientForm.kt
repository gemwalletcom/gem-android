package com.gemwallet.android.features.recipient.views

import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.R
import com.gemwallet.android.blockchain.clients.ethereum.StakeHub.Companion.address
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.features.amount.navigation.OnAmount
import com.gemwallet.android.features.recipient.viewmodels.RecipientFormError
import com.gemwallet.android.features.recipient.viewmodels.RecipientFormUIState
import com.gemwallet.android.features.recipient.viewmodels.RecipientFormViewModel
import com.gemwallet.android.ui.components.AddressChainField
import com.gemwallet.android.ui.components.FatalStateScene
import com.gemwallet.android.ui.components.LoadingScene
import com.gemwallet.android.ui.components.MainActionButton
import com.gemwallet.android.ui.components.Scene
import com.gemwallet.android.ui.components.TransferTextFieldActions
import com.gemwallet.android.ui.components.qrcodescanner.qrCodeRequest
import com.gemwallet.android.ui.theme.WalletTheme
import com.gemwallet.android.ui.theme.padding16
import com.gemwallet.android.ui.theme.space4
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.NameRecord

@ExperimentalGetImage
@Composable
fun RecipientForm(
    assetId: AssetId,
    destinationAddress: String,
    addressDomain: String,
    memo: String,
    onCancel: () -> Unit,
    onNext: OnAmount,
    viewModel: RecipientFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(key1 = assetId.toIdentifier()) {

        viewModel.init(assetId, destinationAddress, addressDomain, memo)

        onDispose {
            viewModel.reset()
        }
    }

    when (uiState) {
        is RecipientFormUIState.Fatal -> FatalStateScene(
            title = stringResource(R.string.transaction_recipient),
            message = (uiState as RecipientFormUIState.Fatal).error,
            onCancel = onCancel,
        )
        is RecipientFormUIState.Idle -> Idle(
            state = (uiState as RecipientFormUIState.Idle),
            addressState = viewModel.addressState,
            memoState = viewModel.memoState,
            onScanAddress = viewModel::scanAddress,
            onScanMemo = viewModel::scanMemo,
            onNext = { input, nameRecord, memoInput -> viewModel.onNext(input, nameRecord, memoInput, onNext)  },
            onCancel = onCancel,
        )
        RecipientFormUIState.Loading -> LoadingScene(
            title = stringResource(R.string.transaction_recipient),
            onCancel = onCancel
        )
        RecipientFormUIState.ScanQr -> qrCodeRequest(
            onResult = viewModel::setQrData,
            onCancel = viewModel::scanCancel
        )
    }
}

@Composable
private fun Idle(
    state: RecipientFormUIState.Idle,
    addressState: MutableState<String>,
    memoState: MutableState<String>,
    onScanAddress: () -> Unit,
    onScanMemo: () -> Unit,
    onNext: (input: String, nameRecord: NameRecord?, memoInput: String) -> Unit,
    onCancel: () -> Unit,
) {
    var inputStateError by remember(address, state.addressError) {
        mutableStateOf(state.addressError)
    }
    var nameRecordState by remember(state.addressDomain) {
        mutableStateOf<NameRecord?>(null)
    }
    Scene(
        title = stringResource(id = R.string.transaction_recipient),
        padding = PaddingValues(padding16),
        onClose = onCancel,
        mainAction = {
            MainActionButton(
                title = stringResource(id = R.string.common_continue),
                enabled = inputStateError == RecipientFormError.None,
                onClick = {
                    onNext(addressState.value, nameRecordState, memoState.value)
                },
            )
        }
    ) {
        val assetInfo = state.assetInfo
        if (assetInfo != null) {
            AddressChainField(
                chain = assetInfo.asset.id.chain,
                value = addressState.value,
                label = stringResource(id = R.string.transfer_recipient_address_field),
                error = recipientErrorString(error = inputStateError),
                onValueChange = { input, nameRecord ->
                    addressState.value = input
                    nameRecordState = nameRecord
                    inputStateError = RecipientFormError.None
                },
                onQrScanner = onScanAddress
            )
        }

        if (state.hasMemo) {
            Spacer(modifier = Modifier.size(space4))
            MemoTextField(
                value = memoState.value,
                label = stringResource(id = R.string.transfer_memo),
                onValueChange = { memoState.value = it },
                error = state.memoError,
                onQrScanner = onScanMemo,
            )
        }
    }
}

@Composable
private fun MemoTextField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    error: RecipientFormError = RecipientFormError.None,
    onQrScanner: (() -> Unit)? = null,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val clipboardManager = LocalClipboardManager.current
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged {
                if (it.hasFocus) keyboardController?.show() else keyboardController?.hide()
            },
        value = value,
        singleLine = true,
        label = { Text(label) },
        onValueChange = onValueChange,
        trailingIcon = {
            TransferTextFieldActions(
                paste = { onValueChange(clipboardManager.getText()?.text ?: "") },
                qrScanner = onQrScanner
            )
        }
    )
    if (error != RecipientFormError.None) {
        Spacer(modifier = Modifier.size(space4))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = recipientErrorString(error),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun recipientErrorString(error: RecipientFormError): String {
    return when (error) {
        RecipientFormError.IncorrectAddress -> stringResource(id = R.string.errors_invalid_address_name)
        RecipientFormError.None -> ""
    }
}

@Composable
@Preview
@ExperimentalGetImage
fun PreviewRecipientForm() {
    WalletTheme {
        RecipientForm(
            assetId = AssetId(Chain.Ethereum),
            destinationAddress = "",
            addressDomain = "",
            memo = "",
            onCancel = {},
            onNext = { _, _, _, _, _, _, _ -> }
        )
    }
}