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
import com.gemwallet.android.features.confirm.models.ConfirmSceneState
import com.gemwallet.android.features.confirm.navigation.paramsArg
import com.gemwallet.android.features.confirm.navigation.txTypeArg
import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.format
import com.gemwallet.android.ui.components.CellEntity
import com.gemwallet.android.ui.components.titles.getTitle
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
import kotlinx.coroutines.withContext
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

    private val request = savedStateHandle.getStateFlow<String?>(paramsArg, null)
        .filterNotNull()
        .mapNotNull { paramsPack ->
            val txTypeString = savedStateHandle.get<String?>(txTypeArg)?.urlDecode()
            val txType = TransactionType.entries.firstOrNull { it.string == txTypeString } ?: return@mapNotNull null

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

    private val signerParams = request.filterNotNull().map { request ->
        val owner = sessionRepository.getSession()?.wallet?.getAccount(request.assetId.chain) ?: return@map null
        val preload = signerPreload(owner = owner, params = request).getOrNull() ?: return@map null // TODO: Handle error
        val finalAmount = when {
            request is ConfirmParams.RewardsParams -> stakeRepository.getRewards(request.assetId, owner.address)
                .map { BigInteger(it.base.rewards) }
                .fold(BigInteger.ZERO) { acc, value -> acc + value }
            request.isMax() && request.assetId == preload.info.fee().feeAssetId -> request.amount - preload.info.fee().amount
            else -> request.amount
        }
        preload.copy(finalAmount = finalAmount)
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val feeAssetInfo = signerParams.filterNotNull().flatMapLatest { signerParams ->
        assetsRepository.getAssetInfo(signerParams.info.fee().feeAssetId)
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val amountUIModel = combine(request, assetsInfo, signerParams, feeAssetInfo) { request, assetsInfo, signerParams, feeAssetInfo ->
        if (assetsInfo.isNullOrEmpty()) {
            assetsInfo ?: return@combine null
        }
        val fromAssetId = request?.assetId ?: return@combine null
        val assetInfo = assetsInfo.getByAssetId(fromAssetId) ?: return@combine null
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
            amount = amount.format(decimals, symbol, -1),
            amountEquivalent = currency.format(amount.convert(decimals, price), 2, dynamicPlace = true),
            fromAsset = assetInfo,
            fromAmount = amount.toString(),
            toAsset = toAssetInfo,
            toAmount = (request as? ConfirmParams.SwapParams)?.toAmount.toString(),
        )
    }

    val txInfoUIModel = combine(request, assetsInfo) { request, assetsInfo ->
        request ?: return@combine null
        val assetInfo = assetsInfo?.getByAssetId(request.assetId) ?: return@combine null
        listOf(
            assetInfo.getFromCell(),
            request.getRecipientCell(getValidator(request)),
            request.getMemoCell(),
            assetInfo.getNetworkCell(),
        ).mapNotNull { it }
    }

    val feeUIModel = combine(signerParams, feeAssetInfo) { signerParams, feeAssetInfo ->
        val feeAmount = Crypto(signerParams?.info?.fee()?.amount ?: return@combine null)
        val assetInfo = feeAssetInfo ?: return@combine null
        val currency = assetInfo.price?.currency ?: Currency.USD
        val feeDecimals = assetInfo.asset.decimals
        val feeCrypto = assetInfo.asset.format(feeAmount, 6)
        val feeFiat = feeAssetInfo.price?.let {
            currency.format(feeAmount.convert(feeDecimals, it.price.price),2, dynamicPlace = true)
        } ?: ""
        CellEntity(
            label = R.string.transfer_network_fee,
            data = feeCrypto,
            support = feeFiat,
            dropDownActions = null,
        )
    }

    private val state = MutableStateFlow(State())
    val uiState = state.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ConfirmSceneState.Loading)

    fun init(params: ConfirmParams) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                load(params)
            } catch (err: ConfirmError) {
                state.update { State(fatalError = err) }
            }
        }
    }

    private suspend fun load(params: ConfirmParams) {
        val assetInfo = assetsRepository.getAssetInfo(params.assetId).firstOrNull()
            ?: throw ConfirmError.Init("Init error - asset doesn't find")
        val signerParams = signerPreload(owner = assetInfo.owner, params = params).getOrNull() // TODO: Remove result???
            ?: throw ConfirmError.Init("Init error - not transaction info")
        val fee = signerParams.info.fee()
        val feeAssetInfo = assetsRepository.getAssetInfo(fee.feeAssetId).firstOrNull()
            ?: throw ConfirmError.Init("Init error - fee asset doesn't find")
        val finalAmount = when {
            params is ConfirmParams.RewardsParams -> stakeRepository.getRewards(params.assetId, assetInfo.owner.address)
                .map { BigInteger(it.base.rewards) }
                .fold(BigInteger.ZERO) { acc, value -> acc + value }
            params.isMax() && params.assetId == feeAssetInfo.asset.id -> params.amount - fee.amount
            else -> params.amount
        }
        val finalParams = signerParams.copy(finalAmount = finalAmount)
        val balance = getBalance(assetInfo = assetInfo, params = params)
        val error = validateBalance(assetInfo.asset, feeAssetInfo, fee, balance, finalParams.finalAmount)
        val toAssetInfo = if (params is ConfirmParams.SwapParams) {
            assetsRepository.getAssetInfo(params.toAssetId).firstOrNull()
        } else {
            null
        }
        val toAmount = (params as? ConfirmParams.SwapParams)?.toAmount
        state.update {
            State(
                walletName = assetInfo.walletName,
                currency = assetInfo.price?.currency ?: Currency.USD,
                assetInfo = assetInfo,
                feeAssetInfo = feeAssetInfo,
                toAssetInfo = toAssetInfo,
                toAmount = toAmount,
                signerParams = finalParams,
                validator = getValidator(params),
                error = error,
            )
        }
    }

    fun send() {
        state.update { it.copy(sending = true) }
        val currentState = state.value.copy()
        if (currentState.assetInfo == null || currentState.signerParams == null) {
            state.update { State(fatalError = ConfirmError.TransactionIncorrect) }
            return
        }
        val asset = currentState.assetInfo.asset
        val owner = currentState.assetInfo.owner
        val destination = currentState.signerParams.input.destination()
        val fee = currentState.signerParams.info.fee()
        val memo = currentState.signerParams.input.memo() ?: ""
        val type = currentState.signerParams.input.getTxType()

        val metadata = when (val input = currentState.signerParams.input) {
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

        viewModelScope.launch {
            val session = sessionRepository.getSession()
            if (session == null) {
                state.update { it.copy(fatalError = ConfirmError.WalletNotAvailable) }
                return@launch
            }
            val broadcastResult = withContext(Dispatchers.IO) {
                val password = passwordStore.getPassword(session.wallet.id)
                val privateKey = loadPrivateKeyOperator(session.wallet, asset.id.chain, password)

                val signResult = signTransfer(currentState.signerParams, privateKey)
                val sign = signResult.getOrNull()
                    ?: return@withContext Result.failure(signResult.exceptionOrNull() ?: Exception("Sign error"))

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
                    amount = currentState.signerParams.finalAmount,
                    memo = memo,
                    type = type,
                    metadata = metadata,
                    direction = if (destinationAddress == owner.address) TransactionDirection.SelfTransfer else TransactionDirection.Outgoing,
                )
                state.update {
                    it.copy(txHash = txHash)
                }
            }.onFailure { err ->
                state.update {
                    it.copy(sending = false, fatalError = ConfirmError.BroadcastError(err.message ?: "Can't send asset"))
                }
            }
        }
    }

    private fun validateBalance(
        asset: Asset,
        feeAsset: AssetInfo,
        fee: Fee,
        balance: BigInteger,
        amount: BigInteger,
    ): ConfirmError {
        if (feeAsset.balances.available().atomicValue < fee.amount) {
            return ConfirmError.InsufficientFee(
                "${feeAsset.asset.id.chain.asset().name}(${feeAsset.asset.symbol})"
            )
        }
        if (balance < amount) {
            return ConfirmError.InsufficientBalance(asset.symbol)
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

    private data class State(
        val fatalError: ConfirmError = ConfirmError.None,
        val currency: Currency = Currency.USD,
        val walletName: String? = null,
        val assetInfo: AssetInfo? = null,
        val feeAssetInfo: AssetInfo? = null,
        val toAssetInfo: AssetInfo? = null,
        val toAmount: BigInteger? = null,
        val signerParams: SignerParams? = null,
        val validator: DelegationValidator? = null,
        val error: ConfirmError = ConfirmError.None,
        val sending: Boolean = false,
        val txHash: String = "",
    ) {
        fun toUIState(): ConfirmSceneState {
            return when {
                fatalError != ConfirmError.None -> ConfirmSceneState.Fatal(fatalError)
                assetInfo == null || signerParams == null -> ConfirmSceneState.Loading
                else -> {
                    val decimals = assetInfo.asset.decimals
                    val symbol = assetInfo.asset.symbol
                    val price = assetInfo.price?.price?.price ?: 0.0
                    val amount = Crypto(signerParams.finalAmount)
                    ConfirmSceneState.Loaded(
                        error = error,
                        type = signerParams.input.getTxType(),
                        title = signerParams.input.getTxType().getTitle(),
                        amount = amount.format(decimals, symbol, -1),
                        amountEquivalent = amount.convert(decimals, price).format(0, currency.string, 2),
                        fromAsset = assetInfo,
                        fromAmount = signerParams.finalAmount.toString(),
                        toAsset = toAssetInfo,
                        toAmount = toAmount?.toString(),
                        cells = listOf(
                            from(),
                            recipient(signerParams.input),
                            memo(),
                            network(),
                            fee(),
                        ).mapNotNull { it },
                        txHash = txHash,
                        sending = sending,
                        currency = currency,
                    )
                }
            }
        }

        private fun from(): CellEntity<Int> {
            return CellEntity(label = R.string.transfer_from, data = "${walletName ?: ""} (${assetInfo?.owner?.address?.getAddressEllipsisText()})")
        }

        private fun recipient(input: ConfirmParams?): CellEntity<Int>? {
            input ?: return null
            return when (input) {
                is ConfirmParams.RewardsParams -> null
                is ConfirmParams.DelegateParams,
                is ConfirmParams.RedeleateParams,
                is ConfirmParams.UndelegateParams,
                is ConfirmParams.WithdrawParams -> CellEntity(label = R.string.stake_validator, data = validator?.name ?: "")
                is ConfirmParams.SwapParams -> CellEntity(label = R.string.swap_provider, data = input.provider)
                is ConfirmParams.TokenApprovalParams -> CellEntity(label = R.string.swap_provider, data = input.provider)
                is ConfirmParams.TransferParams -> {
                    return when {
                        input.destination.domainName.isNullOrEmpty() -> CellEntity(label = R.string.transaction_recipient, data = input.destination.address)
                        else -> CellEntity(label = R.string.transaction_recipient, support = input.destination.address, data = input.destination.domainName!!)
                    }
                }
            }
        }

        private fun memo(): CellEntity<Int>? {
            return if (signerParams?.input is ConfirmParams.TransferParams) {
                val memo = signerParams.input.memo()
                if (memo.isNullOrEmpty()) {
                    return null
                }
                CellEntity(label = R.string.transfer_memo, data = memo)
            } else {
                null
            }
        }

        private fun network(): CellEntity<Int>? {
            val owner = assetInfo?.owner ?: return null
            return CellEntity(
                label = R.string.transfer_network,
                data = owner.chain.asset().name,
                trailingIcon = owner.chain.getIconUrl(),
            )
        }

        private fun fee(): CellEntity<Int>? {
            val feeAmount = Crypto(signerParams?.info?.fee()?.amount ?: return null)
            val asset = feeAssetInfo?.asset ?: return null
            val feeDecimals = asset.decimals
            val feeCrypto = feeAmount.format(feeDecimals, asset.symbol, 6)
            val feeFiat = feeAssetInfo.price?.let {
                feeAmount.convert(feeDecimals, it.price.price)
                    .format(0, currency.string, 2, dynamicPlace = true)
            } ?: ""
            return CellEntity(
                label = R.string.transfer_network_fee,
                data = feeCrypto,
                support = feeFiat,
                dropDownActions = null, /*{
                    DropdownMenuItem(
                        text = { Text("Default") },
                        onClick = { it() }
                    )
                    DropdownMenuItem(
                        text = { Text("Fast") },
                        onClick = { it() }
                    )
                }*/
            )
        }
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