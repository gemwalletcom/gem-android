package com.gemwallet.android.blockchain.clients

import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.SignerParams
import com.wallet.core.primitives.Account

interface NativeTransferPreloader : BlockchainClient {
    suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams.Native): SignerParams
}

interface TokenTransferPreloader : BlockchainClient {
    suspend fun preloadTokenTransfer(params: ConfirmParams.TransferParams.Token): SignerParams
}

interface SwapTransactionPreloader : BlockchainClient {
    suspend fun preloadSwap(params: ConfirmParams.SwapParams): SignerParams
}

interface ApprovalTransactionPreloader : BlockchainClient {
    suspend fun preloadApproval(params: ConfirmParams.TokenApprovalParams): SignerParams
}

interface StakeTransactionPreloader : BlockchainClient {
    suspend fun preloadStake(params: ConfirmParams.Stake): SignerParams
}