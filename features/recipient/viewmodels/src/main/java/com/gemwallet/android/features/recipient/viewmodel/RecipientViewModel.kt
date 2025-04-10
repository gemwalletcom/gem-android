package com.gemwallet.android.features.recipient.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.blockchain.operators.ValidateAddressOperator
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.ext.chain
import com.gemwallet.android.ext.isMemoSupport
import com.gemwallet.android.ext.mutableStateIn
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.features.recipient.viewmodel.models.QrScanField
import com.gemwallet.android.features.recipient.viewmodel.models.RecipientError
import com.gemwallet.android.model.AmountParams
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.DestinationAddress
import com.gemwallet.android.ui.models.actions.AmountTransactionAction
import com.gemwallet.android.ui.models.actions.ConfirmTransactionAction
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.NameRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigInteger
import javax.inject.Inject

val assetIdArg = "assetId"

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RecipientViewModel @Inject constructor(
    private val assetsRepository: AssetsRepository,
    private val validateAddressOperator: ValidateAddressOperator,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val addressState = mutableStateOf("")
    val memoState = mutableStateOf("")
    val nameRecordState = mutableStateOf<NameRecord?>(null)

    val assetId = savedStateHandle.getStateFlow(assetIdArg, "")
        .mapNotNull { it.toAssetId() }
    val asset = assetId.flatMapLatest { assetsRepository.getAssetInfo(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val addressError = combine(
        asset,
        snapshotFlow { addressState.value },
        snapshotFlow { nameRecordState.value },
    ) { assetInfo, address, nameRecord ->
        val nameAddress = nameRecord?.address
        if (assetInfo == null || (address.isEmpty() && nameAddress.isNullOrEmpty())) return@combine RecipientError.None
        val destination = DestinationAddress(nameAddress ?: address, nameRecord?.name)
        val validation = validateDestination(assetInfo.asset.id.chain, destination)
        validation
    }.mutableStateIn(viewModelScope, RecipientError.None)

    val memoErrorState = MutableStateFlow<RecipientError>(RecipientError.None)

    fun hasMemo(): Boolean = asset.value?.asset?.chain()?.isMemoSupport() == true

    fun onNext(amountAction: AmountTransactionAction) = viewModelScope.launch {
        val assetId = asset.value?.id() ?: return@launch
        val destination = DestinationAddress(
            address = nameRecordState.value?.address ?: addressState.value,
            domainName = nameRecordState.value?.name,
        )
        val memo = memoState.value
        val addressError = validateDestination(assetId.chain, destination)
        if (addressError != RecipientError.None) {
            this@RecipientViewModel.addressError.update { addressError }
            return@launch
        }
        amountAction(AmountParams.buildTransfer(assetId, destination, memo))
    }

    fun setQrData(type: QrScanField, data: String, confirmAction: ConfirmTransactionAction) {
        val paymentWrapper = uniffi.gemstone.paymentDecodeUrl(data)
        val amount = try {
            BigInteger(paymentWrapper.amount ?: throw IllegalArgumentException())
        } catch (_: Throwable) {
            null
        }
        val address = paymentWrapper.address
        val memo = paymentWrapper.memo
        val assetInfo = asset.value ?: return

        if (
            address.isNotEmpty()
            && amount != null
            && (assetInfo.asset.chain().isMemoSupport() || !memo.isNullOrEmpty())
        ) {
            val params = ConfirmParams.Builder(assetInfo.asset, assetInfo.owner!!, amount).transfer(DestinationAddress(address), memo)
            confirmAction(params)
            return
        }

        when (type) {
            QrScanField.None -> {}
            QrScanField.Address -> {
                addressState.value = address.ifEmpty { data }
                memoState.value = memo?.ifEmpty { memoState.value } ?: memoState.value
            }
            QrScanField.Memo -> {
                addressState.value = address.ifEmpty { addressState.value }
                memoState.value = paymentWrapper.memo ?: data
            }
        }
    }

    private fun validateDestination(chain: Chain, destination: DestinationAddress?): RecipientError {
        return if (validateAddressOperator(destination?.address ?: "", chain).getOrNull() != true) {
            RecipientError.IncorrectAddress
        } else {
            RecipientError.None
        }
    }
}