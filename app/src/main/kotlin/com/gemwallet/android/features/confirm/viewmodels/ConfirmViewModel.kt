package com.gemwallet.android.features.confirm.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.blockchain.clients.BroadcastClientProxy
import com.gemwallet.android.blockchain.clients.SignClientProxy
import com.gemwallet.android.blockchain.operators.LoadPrivateKeyOperator
import com.gemwallet.android.blockchain.operators.PasswordStore
import com.gemwallet.android.cases.transactions.CreateTransaction
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.stake.StakeRepository
import com.gemwallet.android.domains.asset.chain
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.getAddressEllipsisText
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.features.confirm.models.AmountUIModel
import com.gemwallet.android.features.confirm.models.ConfirmError
import com.gemwallet.android.features.confirm.models.ConfirmState
import com.gemwallet.android.ui.navigation.routes.paramsArg
import com.gemwallet.android.ui.navigation.routes.txTypeArg
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.Session
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.format
import com.gemwallet.android.serializer.jsonEncoder
import com.gemwallet.android.services.SignerPreloaderProxy
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.CellEntity
import com.gemwallet.android.ui.components.InfoSheetEntity
import com.gemwallet.android.ui.components.image.getIconUrl
import com.gemwallet.android.ui.components.progress.CircularProgressIndicator16
import com.gemwallet.android.ui.models.actions.FinishConfirmAction
import com.gemwallet.android.ui.navigation.routes.assetRoutePath
import com.gemwallet.android.ui.navigation.routes.stakeRoute
import com.gemwallet.android.ui.navigation.routes.swapRoute
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.DelegationValidator
import com.wallet.core.primitives.FeePriority
import com.wallet.core.primitives.SwapProvider
import com.wallet.core.primitives.TransactionDirection
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.TransactionSwapMetadata
import com.wallet.core.primitives.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
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
    private val signerPreload: SignerPreloaderProxy,
    private val passwordStore: PasswordStore,
    private val loadPrivateKeyOperator: LoadPrivateKeyOperator,
    private val signClient: SignClientProxy,
    private val broadcastClientProxy: BroadcastClientProxy,
    private val createTransactionsCase: CreateTransaction,
    private val stakeRepository: StakeRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val restart = MutableStateFlow(false)
    val state = MutableStateFlow<ConfirmState>(ConfirmState.Prepare)
    val feePriority = MutableStateFlow(FeePriority.Normal)

    private val request = savedStateHandle.getStateFlow<String?>(paramsArg, null)
        .combine(restart) { request, _ -> request }
        .filterNotNull()
        .mapNotNull { paramsPack ->
            state.update { ConfirmState.Prepare }
            ConfirmParams.unpack(paramsPack)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val assetsInfo = request.filterNotNull().mapNotNull {
        if (it is ConfirmParams.SwapParams) {
            listOf(it.fromAsset.id, it.toAssetId)
        } else {
            listOf(it.assetId)
        }
    }
    .flatMapLatest { assetsRepository.getAssetsInfo(it) }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val preloadData = request.filterNotNull().map { request ->
        val owner = sessionRepository.getSession()?.wallet?.getAccount(request.assetId.chain)
        if (owner == null) {
            state.update { ConfirmState.FatalError }
            return@map null
        }

        val preload = try {
            val result = signerPreload.preload(params = request)
            when (request) {
                is ConfirmParams.SwapParams -> if (result.scanTransaction?.isMalicious != false) {
                    throw Exception("Transaction payload error")
                }
                else -> {}
            }
            result
        } catch (err: Throwable) {
            state.update { ConfirmState.Error(ConfirmError.PreloadError(err.message ?: "Preload error")) }
            return@map null
        }
        preload
    }.filterNotNull().combine(feePriority) { params, feePriority ->
        val finalAmount = when {
            params.input is ConfirmParams.Stake.RewardsParams -> stakeRepository.getRewards(params.input.assetId, params.input.from.address)
                .map { BigInteger(it.base.rewards) }
                .fold(BigInteger.ZERO) { acc, value -> acc + value }
            params.input.isMax() && params.input.assetId == params.chainData.fee(feePriority).feeAssetId ->
                params.input.amount - params.chainData.fee(feePriority).amount
            else -> params.input.amount
        }
        state.update { ConfirmState.Ready }

        if (params.input is ConfirmParams.SwapParams) {
            assembleMetadata(params)
        }

        params.copy(finalAmount = finalAmount)
    }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val feeAssetInfo = preloadData.filterNotNull().flatMapLatest { signerParams ->
        assetsRepository.getAssetInfo(signerParams.chainData.fee().feeAssetId)
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val amountUIModel = combine(request, assetsInfo, preloadData) { request, assetsInfo, signerParams ->
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
            amountEquivalent = currency.format(amount.convert(decimals, price).atomicValue),
            fromAsset = assetInfo,
            fromAmount = amount.atomicValue.toString(),
            toAsset = toAssetInfo,
            toAmount = (request as? ConfirmParams.SwapParams)?.toAmount.toString(),
            nftAsset = (request as? ConfirmParams.NftParams)?.nftAsset,
            currency = currency,
        )
    }
    .flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val txInfoUIModel = combine(request, assetsInfo) { request, assetsInfo ->
        request ?: return@combine emptyList()
        val assetInfo = assetsInfo?.getByAssetId(request.assetId) ?: return@combine emptyList()
        listOf(
            assetInfo.getFromCell(request),
            request.getRecipientCell(getValidator(request)),
            request.getMemoCell(),
            assetInfo.getNetworkCell(),
        ).mapNotNull { it }
    }
    .flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val feeValue = combine(preloadData, feeAssetInfo, state, feePriority) { signerParams, feeAssetInfo, state, speed ->
        val amount = signerParams?.chainData?.fee(speed)?.amount
        if (amount == null || feeAssetInfo == null) {
            return@combine ""
        }
        val feeAmount = Crypto(amount)
        feeAssetInfo.asset.format(feeAmount, 8, dynamicPlace = true)
    }
    .flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val feeUIModel = combine(preloadData, feeAssetInfo, state, feePriority) { signerParams, feeAssetInfo, state, speed ->
        val amount = signerParams?.chainData?.fee(speed)?.amount
        val result = if (amount == null || feeAssetInfo == null) {
            CellEntity(
                label = R.string.transfer_network_fee,
                data = if (state is ConfirmState.Error) "-" else "",
                trailing = {
                    if (state !is ConfirmState.Error) {
                        CircularProgressIndicator16()
                    }
                },
                info = feeAssetInfo?.asset?.let {
                    InfoSheetEntity.NetworkFeeInfo(it.name, it.symbol)
                },
            )
        } else {
            val feeAmount = Crypto(amount)
            val currency = feeAssetInfo.price?.currency ?: Currency.USD
            val feeDecimals = feeAssetInfo.asset.decimals
            val feeCrypto = feeAssetInfo.asset.format(feeAmount, 8, dynamicPlace = true)
            val feeFiat = feeAssetInfo.price?.let {
                currency.format(feeAmount.convert(feeDecimals, it.price.price).atomicValue, dynamicPlace = true) // TODO: Move to UI - Model
            } ?: ""

            try {
                val sendAssetInfo = assetsInfo.value?.getByAssetId(signerParams.input.assetId)
                if (sendAssetInfo != null) {
                    validateBalance(
                        signerParams,
                        feePriority.value,
                        sendAssetInfo,
                        feeAssetInfo,
                        getBalance(sendAssetInfo, signerParams.input)
                    )
                }
            } catch (err: ConfirmError) {
                this@ConfirmViewModel.state.update { ConfirmState.Error(err) }
            }

            CellEntity(
                label = R.string.transfer_network_fee,
                data = feeCrypto,
                support = feeFiat,
                info = feeAssetInfo.asset.let { InfoSheetEntity.NetworkFeeInfo(it.name, it.symbol) }
            )
        }

        listOf(result)
    }
    .flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allFee = preloadData.filterNotNull().map { it.chainData.allFee() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun init(params: ConfirmParams) {
        viewModelScope.launch(Dispatchers.IO) {
            savedStateHandle[txTypeArg] = params.getTxType().string
            savedStateHandle[paramsArg] = params.pack()
        }
    }

    fun changeFeePriority(feePriority: FeePriority) {
        if (feePriority == this.feePriority.value) {
            return
        }
        state.update { ConfirmState.Prepare }
        this.feePriority.update { feePriority }
    }

    fun send(finishAction: FinishConfirmAction) = viewModelScope.launch(Dispatchers.IO) {
        if (state.value is ConfirmState.Error) {
            restart.update { !it }
            return@launch
        }
        state.update { ConfirmState.Sending }

        val signerParams = preloadData.value
        val assetInfo = assetsInfo.value?.getByAssetId(signerParams?.input?.assetId ?: return@launch)
        val account = assetInfo?.owner
        val feeAssetInfo = feeAssetInfo.value
        val session = sessionRepository.getSession()
        val txSpeed = feePriority.value

        try {
            if (assetInfo == null || account == null || session == null || feeAssetInfo == null) {
                throw ConfirmError.TransactionIncorrect
            }
            validateBalance(
                signerParams,
                txSpeed,
                assetInfo,
                feeAssetInfo,
                getBalance(assetInfo, signerParams.input),
            )
            val signs = sign(signerParams, session, assetInfo, txSpeed)
            when (signerParams.input) {
                is ConfirmParams.TransferParams.Generic -> {
                    when ((signerParams.input as ConfirmParams.TransferParams.Generic).inputType) {
                        ConfirmParams.TransferParams.InputType.Signature -> {
                            val hash = String(signs.firstOrNull() ?: byteArrayOf())
                            state.update { ConfirmState.Result(txHash = hash) }
                            viewModelScope.launch(Dispatchers.Main) {
                                finishAction(assetId = assetInfo.id(), hash = hash, route = "")
                            }
                            return@launch
                        }
                        else -> {}
                    }
                }
                else -> {}
            }
            for (sign in signs) {
                val txHash = broadcastClientProxy.send(account, sign, signerParams.input.getTxType())
                if (sign != signs.last()) {
                    delay(500)
                } else {
                    addTransaction(txHash)
                    val finishRoute = when (signerParams.input) {
                        is ConfirmParams.Stake -> stakeRoute
                        is ConfirmParams.SwapParams,
                        is ConfirmParams.TokenApprovalParams -> swapRoute
                        is ConfirmParams.TransferParams -> assetRoutePath
                        is ConfirmParams.Activate -> assetRoutePath
                        is ConfirmParams.NftParams -> assetRoutePath
                    }
                    viewModelScope.launch(Dispatchers.Main) {
                        finishAction(assetId = assetInfo.id(), hash = txHash, route = finishRoute)
                    }
                    state.update { ConfirmState.Result(txHash = txHash) }
                }
            }
        } catch (err: ConfirmError) {
            state.update { ConfirmState.BroadcastError(err) }
        } catch (err: Throwable) {
            state.update { ConfirmState.BroadcastError(ConfirmError.BroadcastError(err.message ?: "Can't send asset")) }
        }
    }

    private suspend fun sign(signerParams: SignerParams, session: Session, assetInfo: AssetInfo, feePriority: FeePriority): List<ByteArray> {
        val sign = try {
            signClient.signTransaction(
                params = signerParams,
                feePriority = feePriority,
                privateKey = loadPrivateKeyOperator(
                    session.wallet,
                    assetInfo.id().chain,
                    passwordStore.getPassword(session.wallet.id)
                )
            )
        } catch (ex: Throwable) {
            throw ConfirmError.SignFail(ex.message ?: "Can't sign transfer")
        }
        return sign
    }

    private suspend fun getBalance(assetInfo: AssetInfo, params: ConfirmParams): BigInteger {
        return when (params) {
            is ConfirmParams.TransferParams,
            is ConfirmParams.SwapParams,
            is ConfirmParams.TokenApprovalParams,
            is ConfirmParams.Activate,
            is ConfirmParams.NftParams,
            is ConfirmParams.Stake.DelegateParams -> assetInfo.balance.balance.available.toBigInteger()
            is ConfirmParams.Stake.RedelegateParams -> BigInteger(stakeRepository.getDelegation(params.srcValidatorId).firstOrNull()?.base?.balance ?: "0")
            is ConfirmParams.Stake.UndelegateParams -> BigInteger(stakeRepository.getDelegation(params.validatorId, params.delegationId).firstOrNull()?.base?.balance ?: "0")
            is ConfirmParams.Stake.WithdrawParams -> BigInteger(stakeRepository.getDelegation(params.validatorId, params.delegationId).firstOrNull()?.base?.balance ?: "0")
            is ConfirmParams.Stake.RewardsParams -> stakeRepository.getRewards(assetInfo.asset.id, assetInfo.owner?.address ?: "")
                .fold(BigInteger.ZERO) { acc, delegation -> acc + BigInteger(delegation.base.balance) }
        }
    }

    private suspend fun getValidator(params: ConfirmParams): DelegationValidator? {
        val validatorId = when (params) {
            is ConfirmParams.Stake.DelegateParams -> params.validatorId
            is ConfirmParams.Stake.RedelegateParams -> params.dstValidatorId
            is ConfirmParams.Stake.UndelegateParams -> params.validatorId
            is ConfirmParams.Stake.WithdrawParams -> params.validatorId
            is ConfirmParams.Activate,
            is ConfirmParams.Stake.RewardsParams,
            is ConfirmParams.SwapParams,
            is ConfirmParams.TokenApprovalParams,
            is ConfirmParams.NftParams,
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
            is ConfirmParams.Activate,
            is ConfirmParams.Stake.RewardsParams -> null
            is ConfirmParams.Stake.DelegateParams,
            is ConfirmParams.Stake.RedelegateParams,
            is ConfirmParams.Stake.UndelegateParams,
            is ConfirmParams.Stake.WithdrawParams -> CellEntity(label = R.string.stake_validator, data = validator?.name ?: "")
            is ConfirmParams.SwapParams -> {
                val swapProvider = SwapProvider.entries.firstOrNull { it.string == protocolId }
                CellEntity(
                    label = R.string.common_provider,
                    data = provider,
                )
            }
            is ConfirmParams.TokenApprovalParams -> CellEntity(label = R.string.common_provider, data = provider)
            is ConfirmParams.NftParams,
            is ConfirmParams.TransferParams -> {
                val destination = destination()
                if (destination == null) {
                    state.update { ConfirmState.Error(ConfirmError.RecipientEmpty) }
                    return null
                }
                return when {
                    destination.domainName.isNullOrEmpty() -> CellEntity(label = R.string.transaction_recipient, data = destination.address)
                    else -> CellEntity(label = R.string.transaction_recipient, support = destination.address, data = destination.domainName!!)
                }
            }
        }
    }

    private fun AssetInfo.getFromCell(input: ConfirmParams): CellEntity<Int> {
        val fromData = when (input.getTxType()) {
            TransactionType.Swap -> walletName
            else -> "$walletName (${owner?.address?.getAddressEllipsisText() ?: ""})"
        }
        return CellEntity(label = R.string.transfer_from, data = fromData)
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
            data = owner?.chain?.asset()?.name ?: "",
            trailingIcon = owner?.chain?.getIconUrl() ?: "",
        )
    }

    private fun assembleMetadata(signerParams: SignerParams) = when (val input = signerParams.input) {
        is ConfirmParams.SwapParams -> {
            jsonEncoder.encodeToString(
                TransactionSwapMetadata(
                    fromAsset = input.fromAsset.id,
                    toAsset = input.toAssetId,
                    fromValue = input.fromAmount.toString(),
                    toValue = input.toAmount.toString(),
                    provider = input.protocolId,
                )
            )
        }
        is ConfirmParams.NftParams -> jsonEncoder.encodeToString(input.nftAsset)
        else -> null
    }

    private suspend fun addTransaction(txHash: String) {
        val signerParams = preloadData.value
        val assetInfo = assetsInfo.value?.getByAssetId(signerParams?.input?.assetId ?: return) ?: return
        val session = sessionRepository.getSession()
        val destinationAddress =  signerParams.input.destination()?.address ?: ""
        val txSpeed = feePriority.value

        createTransactionsCase.createTransaction(
            hash = txHash,
            walletId = session?.wallet?.id ?: return,
            assetId = assetInfo.id(),
            owner = assetInfo.owner!!,
            to = destinationAddress,
            state = TransactionState.Pending,
            fee = signerParams.chainData.fee(txSpeed),
            amount = signerParams.finalAmount,
            memo = signerParams.input.memo() ?: "",
            type = signerParams.input.getTxType(),
            metadata = assembleMetadata(signerParams),
            direction = if (destinationAddress == assetInfo.owner!!.address) {
                TransactionDirection.SelfTransfer
            } else {
                TransactionDirection.Outgoing
            },
            blockNumber = signerParams.chainData.blockNumber()
        )
    }

    companion object {
        fun validateBalance(
            signerParams: SignerParams,
            feePriority: FeePriority,
            assetInfo: AssetInfo,
            feeAssetInfo: AssetInfo,
            assetBalance: BigInteger,
        ) {
            val amount = signerParams.finalAmount
            val feeAmount = signerParams.chainData.fee(feePriority).amount

            val totalAmount = when (signerParams.input.getTxType()) {
                TransactionType.Transfer,
                TransactionType.Swap,
                TransactionType.TokenApproval,
                TransactionType.AssetActivation,
                TransactionType.StakeDelegate -> amount + if (assetInfo == feeAssetInfo) feeAmount else BigInteger.ZERO
                TransactionType.StakeUndelegate,
                TransactionType.StakeRewards,
                TransactionType.StakeRedelegate,
                TransactionType.StakeWithdraw,
                TransactionType.TransferNFT -> amount
                TransactionType.SmartContractCall -> TODO()
            }

            if (assetBalance < totalAmount) {
                val label = "${assetInfo.asset.name} (${assetInfo.asset.symbol})"
                throw ConfirmError.InsufficientBalance(label)
            }
            if (feeAssetInfo.balance.balance.available.toBigInteger() < feeAmount) {
                val label = "${feeAssetInfo.id().chain.asset().name} (${feeAssetInfo.asset.symbol})"
                throw ConfirmError.InsufficientFee(chain = feeAssetInfo.asset.chain)
            }
        }
    }
}