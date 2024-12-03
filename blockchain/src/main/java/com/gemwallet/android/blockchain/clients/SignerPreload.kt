package com.gemwallet.android.blockchain.clients

import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.SignerParams
import com.wallet.core.primitives.Account

interface SignerPreload : BlockchainClient {
    suspend operator fun invoke(owner: Account, params: ConfirmParams): Result<SignerParams>
}

interface NativeTransferPreloader {
    suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams.Native): SignerParams
}

interface TokenTransferPreloader {
    suspend fun preloadTokenTransfer(params: ConfirmParams.TransferParams.Token): SignerParams
}

interface SwapTransactionPreloader {
    suspend fun preloadSwap(params: ConfirmParams.SwapParams): SignerParams
}

interface ApprovalTransactionPreloader {
    suspend fun preloadApproval(params: ConfirmParams.TokenApprovalParams): SignerParams
}

interface StakeTransactionPreloader {
    suspend fun preloadStake(params: ConfirmParams.Stake): SignerParams
}