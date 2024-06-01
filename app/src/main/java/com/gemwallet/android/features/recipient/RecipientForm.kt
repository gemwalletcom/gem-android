package com.gemwallet.android.features.recipient

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.features.amount.navigation.OnAmount
import com.gemwallet.android.ui.components.AddressChainField
import com.gemwallet.android.ui.components.FatalStateScene
import com.gemwallet.android.ui.components.LoadingScene
import com.gemwallet.android.ui.components.MainActionButton
import com.gemwallet.android.ui.components.Scene
import com.gemwallet.android.ui.components.TransferTextFieldActions
import com.gemwallet.android.ui.components.qrcodescanner.QRScanner
import com.gemwallet.android.ui.theme.WalletTheme
import com.gemwallet.android.ui.theme.padding16
import com.gemwallet.android.ui.theme.space4
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
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
) {
    val viewModel: RecipientFormViewModel = hiltViewModel()
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
        is RecipientFormUIState.Form -> (uiState as RecipientFormUIState.Form).Scene(
            onScanAddress = viewModel::scanAddress,
            onScanMemo = viewModel::scanMemo,
            onNext = { input, nameRecord, memoInput -> viewModel.onNext(input, nameRecord, memoInput, onNext)  },
            onCancel = onCancel,
        )
        RecipientFormUIState.Loading -> LoadingScene(
            title = stringResource(R.string.transaction_recipient),
            onCancel = onCancel
        )
        RecipientFormUIState.ScanQr -> qrCodeRequest(onResult = viewModel::setQrData, onCancel = viewModel::scanCancel)
    }
}

@Composable
fun RecipientFormUIState.Form.Scene(
    onScanAddress: () -> Unit,
    onScanMemo: () -> Unit,
    onNext: (input: String, nameRecord: NameRecord?, memoInput: String) -> Unit,
    onCancel: () -> Unit,
) {
    
    var inputState by remember(address, addressDomain) {
        mutableStateOf(address.ifEmpty { addressDomain })
    }
    var inputStateError by remember(address, addressError) {
        mutableStateOf(addressError)
    }
    var nameRecordState by remember(addressDomain) {
        mutableStateOf<NameRecord?>(null)
    }
    var memoState by remember(memo) {
        mutableStateOf(memo)
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
                    onNext(inputState, nameRecordState, memoState)
                },
            )
        }
    ) {
        val assetInfo = assetInfo
        if (assetInfo != null) {
            AddressChainField(
                chain = assetInfo.asset.id.chain,
                value = inputState,
                label = stringResource(id = R.string.transfer_recipient_address_field),
                error = recipientErrorString(error = inputStateError),
                onValueChange = { input, nameRecord ->
                    inputState = input
                    nameRecordState = nameRecord
                    inputStateError = RecipientFormError.None
                },
                onQrScanner = onScanAddress
            )
        }

        if (hasMemo) {
            Spacer(modifier = Modifier.size(space4))
            MemoTextField(
                value = memoState,
                label = stringResource(id = R.string.transfer_memo),
                onValueChange = { memoState = it },
                error = memoError,
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

@OptIn(ExperimentalPermissionsApi::class)
@ExperimentalGetImage
@Composable
fun qrCodeRequest(
    onResult: (String) -> Unit,
    onCancel: () -> Unit,
): Boolean {
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    BackHandler(true) {
        onCancel()
    }
    return if (cameraPermissionState.status.isGranted) {
        Scene(
            title = stringResource(id = R.string.wallet_scan_qr_code),
            padding = PaddingValues(padding16),
            onClose = onCancel
        ) {
            QRScanner(
                listener = onResult
            )
        }
        true
    } else {
        AlertDialog(
            onDismissRequest = onCancel,
            text = {
                Text(text = stringResource(id = R.string.camera_permission_request_camera))
            },
            confirmButton = {
                Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                    Text(text = stringResource(id = R.string.common_grant_permission))
                }
            },
            dismissButton = {
                Button(onClick = onCancel) {
                    Text(text = stringResource(id = R.string.common_cancel))
                }
            }
        )
        false
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