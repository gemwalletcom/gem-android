package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.blockchain.clients.StakeTransactionPreloader
import com.gemwallet.android.blockchain.clients.SwapTransactionPreloader
import com.gemwallet.android.blockchain.clients.TokenTransferPreloader
import com.gemwallet.android.blockchain.clients.solana.SolanaSignerPreloader.SolanaChainData
import com.gemwallet.android.blockchain.clients.solana.services.SolanaAccountsService
import com.gemwallet.android.blockchain.clients.solana.services.SolanaFeeService
import com.gemwallet.android.blockchain.clients.solana.services.SolanaNetworkInfoService
import com.gemwallet.android.blockchain.clients.solana.services.getBlockhash
import com.gemwallet.android.blockchain.clients.solana.services.getTokenAccountByOwner
import com.gemwallet.android.blockchain.clients.solana.services.getTokenInfo
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.Chain
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
) : NativeTransferPreloader, TokenTransferPreloader, SwapTransactionPreloader, StakeTransactionPreloader {

    private val feeCalculator = SolanaFeeCalculator(feeService)

    override suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams.Native): SignerParams {
        return preload(params, "", null, SolanaTokenProgramId.Token)
    }

    override suspend fun preloadTokenTransfer(params: ConfirmParams.TransferParams.Token): SignerParams  = withContext(Dispatchers.IO) {
        val tokenId = params.assetId.tokenId!!
        val senderTokenAddressJob = async { accountsService.getTokenAccountByOwner(params.from.address, tokenId) }
        val recipientTokenAddressJob = async { accountsService.getTokenAccountByOwner(params.destination.address, tokenId) }
        val tokenProgramJob = async {
            val owner = networkInfoService.getTokenInfo(tokenId)

            if (owner != null) {
                SolanaTokenProgramId.entries.firstOrNull {
                    it.string == Config().getSolanaTokenProgramId(owner)
                } ?: SolanaTokenProgramId.Token
            } else {
                SolanaTokenProgramId.Token
            }
        }

        val senderTokenAddress  = senderTokenAddressJob.await()
        val recipientTokenAddress = recipientTokenAddressJob.await()
        val tokenProgram = tokenProgramJob.await()

        if (senderTokenAddress.isNullOrEmpty()) {
            throw Exception("Sender token address is empty")
        }

        preload(params, senderTokenAddress, recipientTokenAddress, tokenProgram)
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
        override fun fee(speed: TxSpeed): Fee = fees.firstOrNull { it.speed == speed } ?: fees.first()

        override fun allFee(): List<Fee> = fees
    }
}