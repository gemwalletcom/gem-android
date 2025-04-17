package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.clients.GenericTransferPreloader
import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.blockchain.clients.StakeTransactionPreloader
import com.gemwallet.android.blockchain.clients.SwapTransactionPreloader
import com.gemwallet.android.blockchain.clients.TokenTransferPreloader
import com.gemwallet.android.blockchain.clients.solana.services.SolanaAccountsService
import com.gemwallet.android.blockchain.clients.solana.services.SolanaFeeService
import com.gemwallet.android.blockchain.clients.solana.services.SolanaNetworkInfoService
import com.gemwallet.android.blockchain.clients.solana.services.createAccountByOwnerRequest
import com.gemwallet.android.blockchain.clients.solana.services.getBlockhash
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.SignerParams
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FeePriority
import com.wallet.core.primitives.SolanaTokenProgramId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import uniffi.gemstone.Config

class SolanaSignerPreloader(
    private val chain: Chain,
    feeService: SolanaFeeService,
    private val networkInfoService: SolanaNetworkInfoService,
    private val accountsService: SolanaAccountsService,
) : GenericTransferPreloader, NativeTransferPreloader, TokenTransferPreloader, SwapTransactionPreloader, StakeTransactionPreloader {

    private val feeCalculator = SolanaFeeCalculator(feeService)

    override suspend fun preloadGeneric(params: ConfirmParams.TransferParams.Generic): SignerParams {
        return preload(params)
    }

    override suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams.Native): SignerParams {
        return preload(params, "", null, SolanaTokenProgramId.Token)
    }

    override suspend fun preloadTokenTransfer(params: ConfirmParams.TransferParams.Token): SignerParams  = withContext(Dispatchers.IO) {
        val tokenId = params.assetId.tokenId!!
        val owner = params.from.address

        val requests = listOf(
            accountsService.createAccountByOwnerRequest(owner, tokenId),
            accountsService.createAccountByOwnerRequest(params.destination.address, tokenId),
        )
        val accountsResponse = accountsService.batchAccount(requests)
        val accounts = accountsResponse.getOrNull() ?: throw Exception("Can't load account info")
        val senderToken =  accounts.getOrNull(0)?.result?.value?.firstOrNull() ?: throw Exception("Sender token address is empty")
        val recipientTokenAccounts = accounts.getOrNull(1)?.result?.value?.firstOrNull()?.pubkey
        val senderTokenAddress = senderToken.pubkey
        val tokenProgram = Config().getSolanaTokenProgramId(senderToken.account.owner)?.let {
            when (it) {
                SolanaTokenProgramId.Token.string -> SolanaTokenProgramId.Token
                SolanaTokenProgramId.Token2022.string -> SolanaTokenProgramId.Token2022
                else -> null
            }
        } ?: throw Exception("Unknow token program id")
        preload(params, senderTokenAddress, recipientTokenAccounts, tokenProgram)
    }

    override suspend fun preloadSwap(params: ConfirmParams.SwapParams): SignerParams {
        return preload(params, "", null, SolanaTokenProgramId.Token)
    }

    override suspend fun preloadStake(params: ConfirmParams.Stake): SignerParams {
        return preload(params, "", null, SolanaTokenProgramId.Token)
    }

    private suspend fun preload(
        params: ConfirmParams,
        senderTokenAddress: String = "",
        recipientTokenAddress: String? = null,
        tokenProgram: SolanaTokenProgramId = SolanaTokenProgramId.Token
    ): SignerParams = withContext(Dispatchers.IO) {
        val blockHashJob = async { networkInfoService.getBlockhash() }
        val feeJob = async { feeCalculator.calculate(params) }

        val (fee, blockHash) = Pair(feeJob.await(), blockHashJob.await())

        val chainData = SolanaChainData(blockHash, senderTokenAddress, recipientTokenAddress, tokenProgram, fee)
        SignerParams(params, chainData)
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

    data class SolanaChainData(
        val blockhash: String,
        val senderTokenAddress: String,
        val recipientTokenAddress: String?,
        val tokenProgram: SolanaTokenProgramId,
        val fees: List<GasFee>,
    ) : ChainSignData {
        override fun fee(feePriority: FeePriority): Fee = fees.firstOrNull { it.priority == feePriority } ?: fees.first()

        override fun allFee(): List<Fee> = fees
    }
}