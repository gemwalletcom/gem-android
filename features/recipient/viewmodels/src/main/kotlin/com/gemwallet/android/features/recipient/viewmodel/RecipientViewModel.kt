package com.gemwallet.android.features.recipient.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.blockchain.operators.ValidateAddressOperator
import com.gemwallet.android.cases.nft.GetAssetNft
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.domains.asset.chain
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.getAccount
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
import com.wallet.core.primitives.NFTAsset
import com.wallet.core.primitives.NameRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.math.BigInteger
import javax.inject.Inject

const val assetIdArg = "assetId"
const val nftAssetIdArg = "nftAssetId"

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RecipientViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val walletsRepository: WalletsRepository,
    private val assetsRepository: AssetsRepository,
    private val getAssetNft: GetAssetNft,
    private val validateAddressOperator: ValidateAddressOperator,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val addressState = mutableStateOf("")
    val memoState = mutableStateOf("")
    val nameRecordState = mutableStateOf<NameRecord?>(null)
    val session = sessionRepository.session()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val assetId = savedStateHandle.getStateFlow(assetIdArg, "")
        .mapNotNull { it.toAssetId() }
    val nftAssetId = savedStateHandle.getStateFlow(nftAssetIdArg, "")
    val asset = assetId.flatMapLatest { assetsRepository.getAssetInfo(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val nftAsset = nftAssetId.filterNotNull().flatMapLatest { getAssetNft.getAssetNft(it) }
        .map { it.assets.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val wallets = session.combine(walletsRepository.getAll()) { session, wallets ->
        wallets.filter { it.id != session?.wallet?.id }
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

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

    fun hasMemo(): Boolean = asset.value?.asset?.chain?.isMemoSupport() == true

    fun onNext(destination: DestinationAddress?, amountAction: AmountTransactionAction, confirmAction: ConfirmTransactionAction)  {
        val assetId = asset.value?.id() ?: return
        val destination = destination ?: DestinationAddress(
            address = nameRecordState.value?.address ?: addressState.value,
            name = nameRecordState.value?.name,
        )
        val memo = memoState.value
        val addressError = validateDestination(assetId.chain, destination)
        if (addressError != RecipientError.None) {
            this@RecipientViewModel.addressError.update { addressError }
            return
        }
        val nftAsset = nftAsset.value
        when {
            nftAsset != null -> onNftConfirm(nftAsset, destination, confirmAction)
            else -> amountAction(AmountParams.buildTransfer(assetId, destination, memo))
        }
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
            && (assetInfo.asset.chain.isMemoSupport() || !memo.isNullOrEmpty())
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

    private fun onNftConfirm(nftAsset: NFTAsset, destination: DestinationAddress, confirmAction: ConfirmTransactionAction) {
        val params = ConfirmParams.NftParams(
            asset = nftAsset.chain.asset(),
            from = session.value?.wallet?.getAccount(nftAsset.chain) ?: return,
            destination = destination,
            nftAsset = nftAsset,
        )
        confirmAction(params)
    }

    private fun validateDestination(chain: Chain, destination: DestinationAddress?): RecipientError {
        return if (validateAddressOperator(destination?.address ?: "", chain).getOrNull() != true) {
            RecipientError.IncorrectAddress
        } else {
            RecipientError.None
        }
    }
}