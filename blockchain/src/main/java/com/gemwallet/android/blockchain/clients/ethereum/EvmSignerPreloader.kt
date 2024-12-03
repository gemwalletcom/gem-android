package com.gemwallet.android.blockchain.clients.ethereum

import com.gemwallet.android.blockchain.clients.ApprovalTransactionPreloader
import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.blockchain.clients.SignerPreload
import com.gemwallet.android.blockchain.clients.StakeTransactionPreloader
import com.gemwallet.android.blockchain.clients.SwapTransactionPreloader
import com.gemwallet.android.blockchain.clients.TokenTransferPreloader
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.ext.type
import com.gemwallet.android.math.append0x
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.EVMChain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.BigInteger
import java.util.Locale

class EvmSignerPreloader(
    private val chain: Chain,
    private val rpcClient: EvmRpcClient,
) : SignerPreload, NativeTransferPreloader, TokenTransferPreloader, SwapTransactionPreloader, StakeTransactionPreloader, ApprovalTransactionPreloader {

    private val NATIVE_GAS_LIMIT = BigInteger.valueOf(21_000L)

    private val wcCoinType = WCChainTypeProxy().invoke(chain)
    private val feeCalculator = EvmFeeCalculator(rpcClient, wcCoinType)

    override suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams): SignerParams =
        preload(params.assetId, params.from, params.destination().address, params.amount, params.memo, params)

    override suspend fun preloadTokenTransfer(params: ConfirmParams.TransferParams): SignerParams =
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
                Chain.SmartChain -> StakeHub().encodeStake(params)
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

        val chainIdJob = async { rpcClient.getNetVersion(wcCoinType) }
        val nonceJob = async { rpcClient.getNonce(from.address) }
        val gasLimitJob = async { getGasLimit(assetId, from.address, recipient, outputAmount, payload) }

        val chainId = chainIdJob.await()
        val nonce = nonceJob.await()
        val gasLimit = gasLimitJob.await()

        val fee = feeCalculator.calculate(params, chainId, nonce, gasLimit)

        SignerParams(
            input = params,
            chainData = EvmChainData(chainId, nonce, fee),
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

    private suspend fun getGasLimit(
        assetId: AssetId,
        from: String,
        recipient: String,
        outputAmount: BigInteger,
        payload: String?,
    ): BigInteger {
        val (amount, to, data) = when (assetId.type()) {
            AssetSubtype.NATIVE -> Triple(outputAmount, recipient, payload)
            AssetSubtype.TOKEN -> Triple(
                BigInteger.ZERO,    // Amount
                assetId.tokenId!!.lowercase(Locale.ROOT), // token
                EVMChain.encodeTransactionData(assetId, payload, outputAmount, recipient).toHexString()
            )
        }

        val gasLimit = rpcClient.getGasLimit(from, to, amount, data)
        return if (gasLimit == NATIVE_GAS_LIMIT) {
            gasLimit
        } else {
            gasLimit.add(gasLimit.toBigDecimal().multiply(BigDecimal.valueOf(0.5)).toBigInteger())
        }
    }

    override suspend fun invoke(
        owner: Account,
        params: ConfirmParams
    ): Result<SignerParams> {
        TODO("Not yet implemented")
    }

    data class EvmChainData(
        val chainId: BigInteger,
        val nonce: BigInteger,
        val fee: Fee,
    ) : ChainSignData {
        override fun fee(speed: TxSpeed): Fee = fee
    }
}