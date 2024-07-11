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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.R
import com.gemwallet.android.blockchain.PayloadType
import com.gemwallet.android.blockchain.memo
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.features.amount.navigation.OnAmount
import com.gemwallet.android.features.recipient.models.RecipientFormError
import com.gemwallet.android.features.recipient.models.RecipientScreenModel
import com.gemwallet.android.features.recipient.models.RecipientScreenState
import com.gemwallet.android.features.recipient.viewmodels.RecipientFormViewModel
import com.gemwallet.android.ui.components.AddressChainField
import com.gemwallet.android.ui.components.LoadingScene
import com.gemwallet.android.ui.components.MainActionButton
import com.gemwallet.android.ui.components.Scene
import com.gemwallet.android.ui.components.TransferTextFieldActions
import com.gemwallet.android.ui.components.qrcodescanner.qrCodeRequest
import com.gemwallet.android.ui.theme.WalletTheme
import com.gemwallet.android.ui.theme.padding16
import com.gemwallet.android.ui.theme.space4
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.NameRecord

@ExperimentalGetImage
@Composable
fun RecipientScreen(
    onCancel: () -> Unit,
    onNext: OnAmount,
    viewModel: RecipientFormViewModel = hiltViewModel(),
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val screenModel by viewModel.screenModel.collectAsStateWithLifecycle()
    val assetId by viewModel.assetId.collectAsStateWithLifecycle()

    LaunchedEffect(assetId?.toIdentifier()) {
        viewModel.input()
    }

    when {
        screenState is RecipientScreenState.Idle && assetId != null -> Idle(
            assetId = assetId!!,
            model = screenModel,
            addressState = viewModel.addressState,
            memoState = viewModel.memoState,
            nameRecordState = viewModel.nameRecordState,
            onScanAddress = viewModel::scanAddress,
            onScanMemo = viewModel::scanMemo,
            onNext = { viewModel.onNext(onNext) },
            onCancel = onCancel,
        )
        screenState == RecipientScreenState.ScanAddress ||
        screenState == RecipientScreenState.ScanMemo -> qrCodeRequest(viewModel::scanCancel, viewModel::setQrData)
        else -> LoadingScene(
            title = stringResource(R.string.transaction_recipient),
            onCancel = onCancel
        )
    }
}

@Composable
private fun Idle(
    assetId: AssetId,
    model: RecipientScreenModel,
    addressState: MutableState<String>,
    memoState: MutableState<String>,
    nameRecordState: MutableState<NameRecord?>,
    onScanAddress: () -> Unit,
    onScanMemo: () -> Unit,
    onNext: () -> Unit,
    onCancel: () -> Unit,
) {
    Scene(
        title = stringResource(id = R.string.transaction_recipient),
        padding = PaddingValues(padding16),
        onClose = onCancel,
        mainAction = {
            MainActionButton(
                title = stringResource(id = R.string.common_continue),
                enabled = model.addressError == RecipientFormError.None,
                onClick = onNext,
            )
        }
    ) {
        AddressChainField(
            chain = assetId.chain,
            value = addressState.value,
            label = stringResource(id = R.string.transfer_recipient_address_field),
            error = recipientErrorString(error = model.addressError),
            onValueChange = { input, nameRecord ->
                addressState.value = input
                nameRecordState.value = nameRecord
            },
            onQrScanner = onScanAddress
        )
        if (assetId.chain.memo() != PayloadType.None) {
            Spacer(modifier = Modifier.size(space4))
            MemoTextField(
                value = memoState.value,
                label = stringResource(id = R.string.transfer_memo),
                onValueChange = { memoState.value = it },
                error = model.memoError,
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
        RecipientScreen(
            onCancel = {},
            onNext = { _, _, _, _, _, _, _ -> }
        )
    }
}