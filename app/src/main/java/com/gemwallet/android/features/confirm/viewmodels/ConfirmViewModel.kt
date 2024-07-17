package com.gemwallet.android.features.confirm.viewmodels

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
import com.gemwallet.android.ext.getAddressEllipsisText
import com.gemwallet.android.features.confirm.models.ConfirmError
import com.gemwallet.android.features.confirm.models.ConfirmSceneState
import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.ui.components.CellEntity
import com.gemwallet.android.ui.components.titles.getTitle
import com.google.gson.Gson
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.DelegationValidator
import com.wallet.core.primitives.TransactionDirection
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.TransactionSwapMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigInteger
import javax.inject.Inject

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
) : ViewModel() {

    private val state = MutableStateFlow(State())
    val uiState = state.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ConfirmSceneState.Loading)

    internal fun init(params: ConfirmParams, feeMultiplicator: Float = 1f) = viewModelScope.launch(Dispatchers.IO) {
        val session = sessionRepository.getSession() ?: return@launch
        val wallet = session.wallet
        val assetInfo = assetsRepository.getById(wallet, params.assetId).getOrNull()?.firstOrNull()
        if (assetInfo == null) {
            state.update { State(fatalError = ConfirmError.Init("Init error - asset doesn't find")) }
            return@launch
        }
        val signerParams = withContext(Dispatchers.IO) {
            signerPreload(
                owner = assetInfo.owner,
                params = params,
            ).getOrNull()
        }
        if (signerParams?.info == null) {
            state.update { State(fatalError = ConfirmError.Init("Init error - not transaction info")) }
            return@launch
        }
        val fee = signerParams.info.fee()
        val feeAssetInfo = assetsRepository.getById(wallet, fee.feeAssetId).getOrNull()?.firstOrNull()
        if (feeAssetInfo == null) {
            state.update { State(fatalError = ConfirmError.Init("Init error - fee asset doesn't find")) }
            return@launch
        }
        val finalParams = when {
            params is ConfirmParams.RewardsParams -> signerParams.copy( finalAmount = stakeRepository.getRewards(params.assetId, assetInfo.owner.address)
                .map { BigInteger(it.base.rewards) }.fold(BigInteger.ZERO) { acc, value -> acc + value }
            )
            params.isMax() && params.assetId == feeAssetInfo.asset.id -> signerParams.copy(finalAmount = params.amount - fee.amount)
            else -> signerParams.copy(finalAmount = params.amount)
        }
        val balance = getBalance(assetInfo = assetInfo, params = params)
        val error = validateBalance(
            asset = assetInfo.asset,
            feeAsset = feeAssetInfo,
            fee = fee,
            balance = balance,
            amount = finalParams.finalAmount
        )
        val (toAssetInfo, toAmount) = if (params is ConfirmParams.SwapParams) {
            Pair(
                assetsRepository.getById(wallet, params.toAssetId).getOrNull()?.firstOrNull(),
                params.toAmount
            )
        } else {
            Pair(null, null)
        }
        state.update {
            State(
                walletName = session.wallet.name,
                currency = session.currency,
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
        val destinationAddress = currentState.signerParams.input.destination()
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
                val privateKey = loadPrivateKeyOperator(session.wallet.id, asset.id.chain, password)

                val signResult = signTransfer(currentState.signerParams, privateKey)
                val sign = signResult.getOrNull()
                    ?: return@withContext Result.failure(signResult.exceptionOrNull() ?: Exception("Sign error"))

                broadcastProxy.broadcast(owner, sign, type)
            }

            broadcastResult.onSuccess { txHash ->
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
                            recipient(),
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

        private fun recipient(): CellEntity<Int>? {
            val input = signerParams?.input ?: return null
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
                        input.domainName.isNullOrEmpty() -> CellEntity(label = R.string.transaction_recipient, data = input.to)
                        else -> CellEntity(label = R.string.transaction_recipient, support = input.to, data = input.domainName!!)
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
}