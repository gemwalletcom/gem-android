package com.gemwallet.android.blockchain.clients.ethereum

import com.gemwallet.android.blockchain.clients.ApprovalTransactionPreloader
import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.blockchain.clients.NftTransactionPreloader
import com.gemwallet.android.blockchain.clients.StakeTransactionPreloader
import com.gemwallet.android.blockchain.clients.SwapTransactionPreloader
import com.gemwallet.android.blockchain.clients.TokenTransferPreloader
import com.gemwallet.android.blockchain.clients.ethereum.services.EvmCallService
import com.gemwallet.android.blockchain.clients.ethereum.services.EvmFeeService
import com.gemwallet.android.blockchain.clients.ethereum.services.getNonce
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.ext.getNetworkId
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.SignerParams
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.EVMChain
import com.wallet.core.primitives.FeePriority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wallet.core.jni.AnyAddress
import wallet.core.jni.CoinType
import java.math.BigInteger

class EvmSignerPreloader(
    private val chain: Chain,
    private val feeService: EvmFeeService,
    callService: EvmCallService,
) : NativeTransferPreloader,
    TokenTransferPreloader,
    SwapTransactionPreloader,
    StakeTransactionPreloader,
    ApprovalTransactionPreloader,
    NftTransactionPreloader {

    private val wcCoinType = WCChainTypeProxy().invoke(chain)
    private val feeCalculator = EvmFeeCalculator(feeService, callService, wcCoinType)

    override suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams.Native): SignerParams =
        preload(params.assetId, params.from, params.destination().address, params.amount, params.memo, params)

    override suspend fun preloadTokenTransfer(params: ConfirmParams.TransferParams.Token): SignerParams =
        preload(params.assetId, params.from, params.destination().address, params.amount, params.memo, params)

    override suspend fun preloadNft(params: ConfirmParams.NftParams): SignerParams {
        val memo = EVMChain.encodeNFT(params)
        return preload(AssetId(params.assetId.chain, params.nftAsset.contractAddress), params.from, params.destination.address, params.amount, memo, params)
    }

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

    override suspend fun preloadSwap(params: ConfirmParams.SwapParams): SignerParams {
        val approval = params.approval
        val approvalData = if (approval != null) {
            preloadApproval(
                params = ConfirmParams.Builder(params.asset, params.from).approval(
                    approvalData = encodeApprove(AnyAddress(approval.spender, CoinType.ETHEREUM).data()).toHexString(),
                    provider = "",
                    contract = approval.token,
                ),
            )
        } else {
            null
        }

        val data = if (approvalData == null) {
            preload(
                assetId = AssetId(params.assetId.chain),
                from = params.from,
                recipient = params.to,
                outputAmount = params.value.toBigInteger(),
                payload = params.swapData,
                params = params
            )
        } else {
            // Add approval fee
            val chainData = (approvalData.chainData as EvmChainData)
            val fees = chainData.fees.map {
                GasFee(
                    feeAssetId = it.feeAssetId,
                    priority = it.priority,
                    limit = it.limit,
                    maxGasPrice = it.maxGasPrice,
                    minerFee = it.minerFee,
                    amount = it.limit.multiply(it.maxGasPrice).add(params.gasLimit!!.multiply(it.maxGasPrice))
                )
            }
            SignerParams(
                input = params,
                chainData = chainData.copy(fees = fees),
            )
        }
        return data
    }

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
        val chainId = chain.getNetworkId().toIntOrNull() ?: throw Exception("Invalid chain")
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
        val chainId: Int,
        val nonce: BigInteger,
        val fees: List<GasFee>,
    ) : ChainSignData {
        override fun fee(speed: FeePriority): Fee = fees.firstOrNull { it.priority == speed } ?: fees.first()

        override fun allFee(): List<Fee> = fees
    }
}