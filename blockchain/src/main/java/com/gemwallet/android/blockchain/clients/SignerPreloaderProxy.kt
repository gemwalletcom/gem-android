package com.gemwallet.android.blockchain.clients

import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.SignerParams
import com.wallet.core.primitives.Chain

class SignerPreloaderProxy(
    private val nativeTransferClients: List<NativeTransferPreloader>,
    private val tokenTransferClients: List<TokenTransferPreloader>,
    private val stakeTransactionClients: List<StakeTransactionPreloader>,
    private val swapTransactionClients: List<SwapTransactionPreloader>,
    private val approvalTransactionClients: List<ApprovalTransactionPreloader>,
) : NativeTransferPreloader, TokenTransferPreloader, StakeTransactionPreloader, SwapTransactionPreloader, ApprovalTransactionPreloader {

    suspend fun preload(params: ConfirmParams): SignerParams {
        return when (params) {
            is ConfirmParams.Stake -> preloadStake(params)
            is ConfirmParams.SwapParams -> preloadSwap(params)
            is ConfirmParams.TokenApprovalParams -> preloadApproval(params)
            is ConfirmParams.TransferParams.Native -> preloadNativeTransfer(params)
            is ConfirmParams.TransferParams.Token -> preloadTokenTransfer(params)
        }
    }

    override fun supported(chain: Chain): Boolean {
        return (nativeTransferClients
                + tokenTransferClients
                + stakeTransactionClients
                + swapTransactionClients
                + approvalTransactionClients).getClient(chain) != null
    }

    override suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams.Native): SignerParams {
        return nativeTransferClients.getClient(params.from.chain)?.preloadNativeTransfer(params = params)
            ?: throw IllegalArgumentException("Chain isn't support")
    }

    override suspend fun preloadTokenTransfer(params: ConfirmParams.TransferParams.Token): SignerParams {
        return tokenTransferClients.getClient(params.from.chain)?.preloadTokenTransfer(params = params)
            ?: throw IllegalArgumentException("Chain isn't support")
    }

    override suspend fun preloadStake(params: ConfirmParams.Stake): SignerParams {
        return stakeTransactionClients.getClient(params.from.chain)?.preloadStake(params = params)
            ?: throw IllegalArgumentException("Chain isn't support")
    }

    override suspend fun preloadSwap(params: ConfirmParams.SwapParams): SignerParams {
        return swapTransactionClients.getClient(params.from.chain)?.preloadSwap(params = params)
            ?: throw IllegalArgumentException("Chain isn't support")
    }

    override suspend fun preloadApproval(params: ConfirmParams.TokenApprovalParams): SignerParams {
        return approvalTransactionClients.getClient(params.from.chain)?.preloadApproval(params = params)
            ?: throw IllegalArgumentException("Chain isn't support")
    }
}