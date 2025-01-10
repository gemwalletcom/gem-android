package com.gemwallet.android.blockchain.clients.ethereum

import com.gemwallet.android.blockchain.clients.ApprovalTransactionPreloader
import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.blockchain.clients.StakeTransactionPreloader
import com.gemwallet.android.blockchain.clients.SwapTransactionPreloader
import com.gemwallet.android.blockchain.clients.TokenTransferPreloader
import com.gemwallet.android.blockchain.clients.ethereum.services.EvmCallService
import com.gemwallet.android.blockchain.clients.ethereum.services.EvmFeeService
import com.gemwallet.android.blockchain.clients.ethereum.services.getNonce
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.ext.getNetworkId
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigInteger

class EvmSignerPreloader(
    private val chain: Chain,
    private val feeService: EvmFeeService,
    callService: EvmCallService,
) : NativeTransferPreloader, TokenTransferPreloader, SwapTransactionPreloader, StakeTransactionPreloader, ApprovalTransactionPreloader {

    private val wcCoinType = WCChainTypeProxy().invoke(chain)
    private val feeCalculator = EvmFeeCalculator(feeService, callService, wcCoinType)

    override suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams.Native): SignerParams =
        preload(params.assetId, params.from, params.destination().address, params.amount, params.memo, params)

    override suspend fun preloadTokenTransfer(params: ConfirmParams.TransferParams.Token): SignerParams =
        preload(params.assetId, params.from, params.destination().address, params.amount, params.memo, params)

    override suspend fun preloadStake(params: ConfirmParams.Stake): SignerParams =
        preload(
            assetId = params.assetId,
            from = params.from,
            recipient = StakeHub.address,
            outputAmount = when (params) {
                is ConfirmParams.Stake.DelegateParams -> params.amount
                else -> BigInteger.ZERO
            },
            payload = when (params.assetId.chain) {
                Chain.SmartChain -> StakeHub.encodeStake(params)
                else -> throw IllegalArgumentException("Stake doesn't suppoted for chain ${params.assetId.chain} ")
            },
            params = params,
        )

    override suspend fun preloadSwap(params: ConfirmParams.SwapParams): SignerParams =
        preload(AssetId(params.assetId.chain), params.from, params.to, params.value.toBigInteger(), params.swapData, params)

    override suspend fun preloadApproval(params: ConfirmParams.TokenApprovalParams): SignerParams =
        preload(params.assetId, params.from, params.contract, BigInteger.ZERO, params.data, params)

    private suspend fun preload(
        assetId: AssetId,
        from: Account,
        recipient: String,
        outputAmount: BigInteger,
        payload: String?,
        params: ConfirmParams,
    ) = withContext(Dispatchers.IO) {
        val nonce = feeService.getNonce(from.address)
        val chainId = chain.getNetworkId()
        val fees = feeCalculator.calculate(
            params = params,
            assetId = assetId,
            recipient = recipient,
            outputAmount = outputAmount,
            payload = payload,
            chainId = chainId,
            nonce = nonce
        )

        SignerParams(params, EvmChainData(chainId, nonce, fees))
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

    data class EvmChainData(
        val chainId: String,
        val nonce: BigInteger,
        val fees: List<GasFee>,
    ) : ChainSignData {
        override fun fee(speed: TxSpeed): Fee = fees.firstOrNull { it.speed == speed } ?: fees.first()

        override fun allFee(): List<Fee> = fees
    }
}