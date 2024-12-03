package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.blockchain.clients.SignerPreload
import com.gemwallet.android.blockchain.clients.StakeTransactionPreloader
import com.gemwallet.android.blockchain.clients.SwapTransactionPreloader
import com.gemwallet.android.blockchain.clients.TokenTransferPreloader
import com.gemwallet.android.blockchain.clients.solana.SolanaSignerPreloader.SolanaChainData
import com.gemwallet.android.ext.type
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.SolanaTokenProgramId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import uniffi.gemstone.Config

class SolanaSignerPreloader(
    private val chain: Chain,
    private val rpcClient: SolanaRpcClient,
) : SignerPreload, NativeTransferPreloader, TokenTransferPreloader, SwapTransactionPreloader, StakeTransactionPreloader {

    private val feeCalculator = SolanaFeeCalculator(rpcClient)

    override suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams.Native): SignerParams {
        return preload(params, "", null, SolanaTokenProgramId.Token)
    }

    override suspend fun preloadTokenTransfer(params: ConfirmParams.TransferParams.Token): SignerParams  = withContext(Dispatchers.IO) {
        val tokenId = params.assetId.tokenId!!
        val senderTokenAddressJob = async { rpcClient.getTokenAccountByOwner(params.from.address, tokenId) }
        val recipientTokenAddressJob = async { rpcClient.getTokenAccountByOwner(params.destination.address, tokenId) }
        val tokenProgramJob = async {
            val owner = rpcClient.getTokenInfo(tokenId)

            if (owner != null) {
                SolanaTokenProgramId.entries.firstOrNull { it.string == Config().getSolanaTokenProgramId(owner) } ?: SolanaTokenProgramId.Token
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
        val blockhashJob = async { rpcClient.getBlockhash() }
        val feeJob = async { feeCalculator.calculate(params) }

        val (fee, blockhash) = Pair(feeJob.await(), blockhashJob.await())

        val chainData = SolanaChainData(blockhash, senderTokenAddress, recipientTokenAddress, tokenProgram, fee)
        SignerParams(params, chainData)
    }

    override suspend fun invoke(
        owner: Account,
        params: ConfirmParams,
    ): Result<SignerParams> = withContext(Dispatchers.IO) {
        val blockhashJob = async { rpcClient.getBlockhash() }
        val feeJob = async { feeCalculator.calculate(params) }

        val (fee, blockhash) = Pair(feeJob.await(), blockhashJob.await())

        val info = when (params) {
            is ConfirmParams.TransferParams -> when (params.assetId.type()) {
                AssetSubtype.NATIVE -> {
                    SolanaChainData(blockhash, "", null, SolanaTokenProgramId.Token, fee)
                }
                AssetSubtype.TOKEN -> {
                    val (senderTokenAddress, recipientTokenAddress, tokenProgram) = getTokenAccounts(params.assetId.tokenId!!, owner.address, params.destination.address)

                    if (senderTokenAddress.isNullOrEmpty()) {
                        return@withContext Result.failure(Exception("Sender token address is empty"))
                    }
                    SolanaChainData(
                        blockhash = blockhash,
                        senderTokenAddress = senderTokenAddress,
                        recipientTokenAddress = recipientTokenAddress,
                        tokenProgram = tokenProgram,
                        fee = if (recipientTokenAddress.isNullOrEmpty()) {
                            fee.withOptions("tokenAccountCreation")
                        } else {
                            fee
                        },
                    )
                }
            }
            is ConfirmParams.SwapParams -> SolanaChainData(blockhash, "", null, SolanaTokenProgramId.Token, fee)
            is ConfirmParams.Stake -> SolanaChainData(blockhash, "", null, SolanaTokenProgramId.Token, fee)
            is ConfirmParams.TokenApprovalParams -> throw IllegalArgumentException("Token approval doesn't supported")
        }
        Result.success(
            SignerParams(
                input = params,
                chainData = info,
            )
        )
    }

    private suspend fun getTokenAccounts(
        tokenId: String,
        senderAddress: String,
        recipientAddress: String,
    ): Triple<String?, String?, SolanaTokenProgramId> = withContext(Dispatchers.IO) {
        val senderTokenAddressJob = async { rpcClient.getTokenAccountByOwner(senderAddress, tokenId) }
        val recipientTokenAddressJob = async { rpcClient.getTokenAccountByOwner(recipientAddress, tokenId) }
        val tokenProgramJob = async {
            val owner = rpcClient.getTokenInfo(tokenId)

            if (owner != null) {
                SolanaTokenProgramId.entries.firstOrNull { it.string == Config().getSolanaTokenProgramId(owner) } ?: SolanaTokenProgramId.Token
            } else {
                SolanaTokenProgramId.Token
            }
        }
        Triple(
            senderTokenAddressJob.await(),
            recipientTokenAddressJob.await(),
            tokenProgramJob.await()
        )
    }
    override fun supported(chain: Chain): Boolean = this.chain == chain

    data class SolanaChainData(
        val blockhash: String,
        val senderTokenAddress: String,
        val recipientTokenAddress: String?,
        val tokenProgram: SolanaTokenProgramId,
        val fee: GasFee,
    ) : ChainSignData {
        override fun fee(speed: TxSpeed): Fee = fee
    }
}