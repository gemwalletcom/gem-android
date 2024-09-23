package com.gemwallet.android.features.confirm.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.R
import com.gemwallet.android.blockchain.clients.BroadcastProxy
import com.gemwallet.android.blockchain.clients.SignerPreload
import com.gemwallet.android.blockchain.operators.LoadPrivateKeyOperator
import com.gemwallet.android.blockchain.operators.PasswordStore
import com.gemwallet.android.blockchain.operators.SignTransfer
import com.gemwallet.android.data.asset.AssetsRepository
import com.gemwallet.android.data.repositories.session.SessionRepository
import com.gemwallet.android.data.stake.StakeRepository
import com.gemwallet.android.data.transaction.TransactionsRepository
import com.gemwallet.android.di.GemJson
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.getAddressEllipsisText
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ext.urlDecode
import com.gemwallet.android.features.confirm.models.AmountUIModel
import com.gemwallet.android.features.confirm.models.ConfirmError
import com.gemwallet.android.features.confirm.models.ConfirmState
import com.gemwallet.android.features.confirm.navigation.paramsArg
import com.gemwallet.android.features.confirm.navigation.txTypeArg
import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.TxSpeed
import com.gemwallet.android.model.format
import com.gemwallet.android.ui.components.CellEntity
import com.gemwallet.android.ui.components.CircularProgressIndicator16
import com.google.gson.Gson
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.DelegationValidator
import com.wallet.core.primitives.TransactionDirection
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.TransactionSwapMetadata
import com.wallet.core.primitives.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigInteger
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ConfirmViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val signerPreload: SignerPreload,
    private val passwordStore: PasswordStore,
    private val loadPrivateKeyOperator: LoadPrivateKeyOperator,
    private val signTransfer: SignTransfer,
    private val broadcastProxy: BroadcastProxy,
    private val transactionsRepository: TransactionsRepository,
    private val stakeRepository: StakeRepository,
    @GemJson private val gson: Gson,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val restart = MutableStateFlow(false)
    val state = MutableStateFlow<ConfirmState>(ConfirmState.Prepare)
    val txSpeed = MutableStateFlow(TxSpeed.Normal)

    private val request = savedStateHandle.getStateFlow<String?>(paramsArg, null)
        .combine(restart) { request, _ -> request }
        .filterNotNull()
        .mapNotNull { paramsPack ->
            val txTypeString = savedStateHandle.get<String?>(txTypeArg)?.urlDecode()
            val txType = TransactionType.entries.firstOrNull { it.string == txTypeString } ?: return@mapNotNull null

            state.update { ConfirmState.Prepare }

            ConfirmParams.unpack(
                when (txType) {
                    TransactionType.Transfer -> ConfirmParams.TransferParams::class.java
                    TransactionType.Swap -> ConfirmParams.SwapParams::class.java
                    TransactionType.TokenApproval -> ConfirmParams.TokenApprovalParams::class.java
                    TransactionType.StakeDelegate -> ConfirmParams.DelegateParams::class.java
                    TransactionType.StakeUndelegate -> ConfirmParams.UndelegateParams::class.java
                    TransactionType.StakeRewards -> ConfirmParams.RewardsParams::class.java
                    TransactionType.StakeRedelegate -> ConfirmParams.RedeleateParams::class.java
                    TransactionType.StakeWithdraw -> ConfirmParams.WithdrawParams::class.java
                },
                paramsPack,
            )
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val assetsInfo = request.filterNotNull().mapNotNull {
        if (it is ConfirmParams.SwapParams) {
            listOf(it.fromAssetId, it.toAssetId)
        } else {
            listOf(it.assetId)
        }
    }
    .flatMapLatest { assetsRepository.getAssetsInfo(it) }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val signerParams = request.filterNotNull().combine(txSpeed) { request, speed ->
        val owner = sessionRepository.getSession()?.wallet?.getAccount(request.assetId.chain)
        if (owner == null) {
            state.update { ConfirmState.FatalError }
            return@combine null
        }

        val preload = signerPreload(owner = owner, params = request).getOrNull()
        if (preload == null) {
            state.update { ConfirmState.Error(ConfirmError.CalculateFee) }
            return@combine null
        }
        val finalAmount = when {
            request is ConfirmParams.RewardsParams -> stakeRepository.getRewards(request.assetId, owner.address)
                .map { BigInteger(it.base.rewards) }
                .fold(BigInteger.ZERO) { acc, value -> acc + value }
            request.isMax() && request.assetId == preload.info.fee(speed).feeAssetId ->
                request.amount - preload.info.fee(speed).amount
            else -> request.amount
        }
        state.update { ConfirmState.Ready }
        preload.copy(finalAmount = finalAmount)
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val feeAssetInfo = signerParams.filterNotNull().flatMapLatest { signerParams ->
        assetsRepository.getAssetInfo(signerParams.info.fee().feeAssetId)
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val amountUIModel = combine(request, assetsInfo, signerParams) { request, assetsInfo, signerParams ->
        val fromAssetId = request?.assetId ?: return@combine null
        val assetInfo = assetsInfo?.getByAssetId(fromAssetId) ?: return@combine null
        val toAssetInfo = if (request is ConfirmParams.SwapParams) {
            assetsInfo.getByAssetId(request.toAssetId) ?: return@combine null
        } else {
            null
        }

        val amount = Crypto(signerParams?.finalAmount ?: request.amount)
        val price = assetInfo.price?.price?.price ?: 0.0
        val currency = assetInfo.price?.currency ?: Currency.USD
        val decimals = assetInfo.asset.decimals
        val symbol = assetInfo.asset.symbol

        AmountUIModel(
            txType = request.getTxType(),
            amount = amount.format(decimals, symbol, -1),
            amountEquivalent = currency.format(amount.convert(decimals, price), 2, dynamicPlace = true),
            fromAsset = assetInfo,
            fromAmount = amount.atomicValue.toString(),
            toAsset = toAssetInfo,
            toAmount = (request as? ConfirmParams.SwapParams)?.toAmount.toString(),
            currency = currency,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val txInfoUIModel = combine(request, assetsInfo) { request, assetsInfo ->
        request ?: return@combine emptyList()
        val assetInfo = assetsInfo?.getByAssetId(request.assetId) ?: return@combine emptyList()
        listOf(
            assetInfo.getFromCell(),
            request.getRecipientCell(getValidator(request)),
            request.getMemoCell(),
            assetInfo.getNetworkCell(),
        ).mapNotNull { it }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val feeUIModel = combine(signerParams, feeAssetInfo, state, txSpeed) { signerParams, feeAssetInfo, state, speed ->
        val amount = signerParams?.info?.fee(speed)?.amount
        val assetInfo = feeAssetInfo
        val result = if (amount == null || assetInfo == null) {
            CellEntity(
                label = R.string.transfer_network_fee,
                data = if ((state as? ConfirmState.Error)?.message == ConfirmError.CalculateFee) "-" else "",
                support = null,
                trailing = {
                    if (state !is ConfirmState.Error) {
                        CircularProgressIndicator16()
                    }
                }
            )
        } else {
            val feeAmount = Crypto(amount)
            val currency = assetInfo.price?.currency ?: Currency.USD
            val feeDecimals = assetInfo.asset.decimals
            val feeCrypto = assetInfo.asset.format(feeAmount, 6)
            val feeFiat = feeAssetInfo.price?.let {
                currency.format(feeAmount.convert(feeDecimals, it.price.price), 2, dynamicPlace = true)
            } ?: ""
            CellEntity(
                label = R.string.transfer_network_fee,
                data = feeCrypto,
                support = feeFiat,
            )
        }

        listOf(result)
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allFee = signerParams.filterNotNull().map { it.info.allFee() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun init(params: ConfirmParams) {
        viewModelScope.launch(Dispatchers.IO) {
            savedStateHandle[txTypeArg] = params.getTxType().string
            savedStateHandle[paramsArg] = params.pack()
        }
    }

    fun changeTxSpeed(speed: TxSpeed) {
        txSpeed.update { speed }
    }

    fun send(onFinish: (String) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        if (state.value is ConfirmState.Error) {
            restart.update { !it }
            return@launch
        }
        state.update { ConfirmState.Sending }
        val signerParams = signerParams.value
        val assetInfo = assetsInfo.value?.getByAssetId(signerParams?.input?.assetId ?: return@launch)
        val feeAssetInfo = feeAssetInfo.value ?: return@launch
        val session = sessionRepository.getSession()
        if (assetInfo == null || signerParams == null || session == null) {
            state.update { ConfirmState.Error(ConfirmError.TransactionIncorrect) }
            return@launch
        }
        val asset = assetInfo.asset
        val owner = assetInfo.owner
        val destination = signerParams.input.destination()
        val fee = signerParams.info.fee()
        val memo = signerParams.input.memo() ?: ""
        val type = signerParams.input.getTxType()

        val validBalance = validateBalance(
            asset = assetInfo.asset,
            balance = getBalance(assetInfo, signerParams.input),
            amount = signerParams.finalAmount
        )
        if (validBalance != ConfirmError.None) {
            state.update { ConfirmState.Error(validBalance) }
            return@launch
        }

        val validFeeBalance = validateFeeBalance(feeAssetInfo, fee)
        if (validFeeBalance != ConfirmError.None) {
            state.update { ConfirmState.Error(validFeeBalance) }
            return@launch
        }

        val metadata = when (val input = signerParams.input) {
            is ConfirmParams.SwapParams -> {
                gson.toJson(
                    TransactionSwapMetadata(
                        fromAsset = input.fromAssetId,
                        toAsset = input.toAssetId,
                        fromValue = input.fromAmount.toString(),
                        toValue = input.toAmount.toString(),
                    )
                )
            }
            else -> null
        }
        val password = passwordStore.getPassword(session.wallet.id)
        val privateKey = loadPrivateKeyOperator(session.wallet, asset.id.chain, password)

        val signResult = signTransfer(signerParams, privateKey)
        val sign = signResult.getOrNull()

        val broadcastResult = if (sign == null || signResult.isFailure) {
            state.update {
                ConfirmState.Error(ConfirmError.SignFail(signResult.exceptionOrNull()?.message ?: "Can't sign transfer"))
            }
            return@launch
        } else {
            broadcastProxy.broadcast(owner, sign, type)
        }

        broadcastResult.onSuccess { txHash ->
            val destinationAddress = destination?.address ?: ""
            transactionsRepository.addTransaction(
                hash = txHash,
                assetId = asset.id,
                owner = owner,
                to = destinationAddress,
                state = TransactionState.Pending,
                fee = fee,
                amount = signerParams.finalAmount,
                memo = memo,
                type = type,
                metadata = metadata,
                direction = if (destinationAddress == owner.address) TransactionDirection.SelfTransfer else TransactionDirection.Outgoing,
            )
            state.update { ConfirmState.Result(txHash = txHash) }
            viewModelScope.launch(Dispatchers.Main) { onFinish(txHash) }
        }.onFailure { err ->
            state.update { ConfirmState.Error(ConfirmError.BroadcastError(err.message ?: "Can't send asset")) }
        }
    }

    private fun validateBalance(asset: Asset, balance: BigInteger, amount: BigInteger): ConfirmError {
        if (balance < amount) {
            return ConfirmError.InsufficientBalance(asset.symbol)
        }
        return ConfirmError.None
    }

    private fun validateFeeBalance(feeAsset: AssetInfo, fee: Fee): ConfirmError {
        if (feeAsset.balances.available().atomicValue < fee.amount) {
            return ConfirmError.InsufficientFee(
                "${feeAsset.asset.id.chain.asset().name}(${feeAsset.asset.symbol})"
            )
        }
        return ConfirmError.None
    }

    private suspend fun getBalance(assetInfo: AssetInfo, params: ConfirmParams): BigInteger {
        return when (params) {
            is ConfirmParams.TransferParams,
            is ConfirmParams.SwapParams,
            is ConfirmParams.TokenApprovalParams,
            is ConfirmParams.DelegateParams -> assetInfo.balances.available().atomicValue
            is ConfirmParams.RedeleateParams -> BigInteger(stakeRepository.getDelegation(params.srcValidatorId).firstOrNull()?.base?.balance ?: "0")
            is ConfirmParams.UndelegateParams -> BigInteger(stakeRepository.getDelegation(params.validatorId, params.delegationId).firstOrNull()?.base?.balance ?: "0")
            is ConfirmParams.WithdrawParams -> BigInteger(stakeRepository.getDelegation(params.validatorId, params.delegationId).firstOrNull()?.base?.balance ?: "0")
            is ConfirmParams.RewardsParams -> stakeRepository.getRewards(assetInfo.asset.id, assetInfo.owner.address)
                .fold(BigInteger.ZERO) { acc, delegation -> acc + BigInteger(delegation.base.balance) }
        }
    }

    private suspend fun getValidator(params: ConfirmParams): DelegationValidator? {
        val validatorId = when (params) {
            is ConfirmParams.DelegateParams -> params.validatorId
            is ConfirmParams.RedeleateParams -> params.dstValidatorId
            is ConfirmParams.UndelegateParams -> params.validatorId
            is ConfirmParams.WithdrawParams -> params.validatorId
            is ConfirmParams.RewardsParams,
            is ConfirmParams.SwapParams,
            is ConfirmParams.TokenApprovalParams,
            is ConfirmParams.TransferParams -> null
        }
        return stakeRepository.getStakeValidator(params.assetId, validatorId ?: return null)
    }

    private fun List<AssetInfo>.getByAssetId(assetId: AssetId): AssetInfo? {
        val str = assetId.toIdentifier()
        return firstOrNull { it.id().toIdentifier() ==  str}
    }

    private fun ConfirmParams.getRecipientCell(validator: DelegationValidator?): CellEntity<Int>? {
        return when (this) {
            is ConfirmParams.RewardsParams -> null
            is ConfirmParams.DelegateParams,
            is ConfirmParams.RedeleateParams,
            is ConfirmParams.UndelegateParams,
            is ConfirmParams.WithdrawParams -> CellEntity(label = R.string.stake_validator, data = validator?.name ?: "")
            is ConfirmParams.SwapParams -> CellEntity(label = R.string.swap_provider, data = provider)
            is ConfirmParams.TokenApprovalParams -> CellEntity(label = R.string.swap_provider, data = provider)
            is ConfirmParams.TransferParams -> {
                return when {
                    destination.domainName.isNullOrEmpty() -> CellEntity(label = R.string.transaction_recipient, data = destination.address)
                    else -> CellEntity(label = R.string.transaction_recipient, support = destination.address, data = destination.domainName!!)
                }
            }
        }
    }

    private fun AssetInfo.getFromCell(): CellEntity<Int> {
        return CellEntity(label = R.string.transfer_from, data = "$walletName (${owner.address.getAddressEllipsisText()})")
    }

    private fun ConfirmParams.getMemoCell(): CellEntity<Int>? {
        return if (this is ConfirmParams.TransferParams) {
            val memo = memo()
            if (memo.isNullOrEmpty()) {
                return null
            }
            CellEntity(label = R.string.transfer_memo, data = memo)
        } else {
            null
        }
    }

    private fun AssetInfo.getNetworkCell(): CellEntity<Int> {
        return CellEntity(
            label = R.string.transfer_network,
            data = owner.chain.asset().name,
            trailingIcon = owner.chain.getIconUrl(),
        )
    }
}