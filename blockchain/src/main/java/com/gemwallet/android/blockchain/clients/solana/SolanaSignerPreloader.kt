package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.clients.SignerPreload
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.ext.type
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.SignerInputInfo
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
) : SignerPreload {

    override suspend fun invoke(
        owner: Account,
        params: ConfirmParams,
    ): Result<SignerParams> = withContext(Dispatchers.IO) {
        val blockhashJob = async {
            rpcClient.getBlockhash(JSONRpcRequest.create(SolanaMethod.GetLatestBlockhash, emptyList()))
                .getOrNull()?.result?.value?.blockhash
        }
        val feeJob = async { SolanaFee().invoke(rpcClient, params.getTxType(), params.assetId.type()) }
        val (fee, blockhash) = Pair(feeJob.await(), blockhashJob.await())

        if (blockhash.isNullOrEmpty()) {
            return@withContext Result.failure(Exception("Can't get latest blockhash"))
        }
        val info = when (params) {
            is ConfirmParams.TransferParams -> when (params.assetId.type()) {
                AssetSubtype.NATIVE -> {
                    Info(blockhash, "", null, SolanaTokenProgramId.Token, fee)
                }
                AssetSubtype.TOKEN -> {
                    val (senderTokenAddress, recipientTokenAddress, tokenProgram) = getTokenAccounts(params.assetId.tokenId!!, owner.address, params.destination.address)

                    if (senderTokenAddress.isNullOrEmpty()) {
                        return@withContext Result.failure(Exception("Sender token address is empty"))
                    }
                    Info(
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
            is ConfirmParams.SwapParams -> Info(blockhash, "", null, SolanaTokenProgramId.Token, fee)
            is ConfirmParams.DelegateParams,
            is ConfirmParams.RedeleateParams,
            is ConfirmParams.RewardsParams,
            is ConfirmParams.TokenApprovalParams,
            is ConfirmParams.UndelegateParams,
            is ConfirmParams.WithdrawParams -> Info(blockhash, "", null, SolanaTokenProgramId.Token, fee)
        }
        Result.success(
            SignerParams(
                input = params,
                owner = owner.address,
                info = info,
            )
        )
    }

    private suspend fun getTokenAccounts(
        tokenId: String,
        senderAddress: String,
        recipientAddress: String,
    ): Triple<String?, String?, SolanaTokenProgramId> = withContext(Dispatchers.IO) {
        val senderTokenAddressJob = async {
            rpcClient.getTokenAccountByOwner(senderAddress, tokenId)
                .getOrNull()?.result?.value?.firstOrNull()?.pubkey
        }
        val recipientTokenAddressJob = async {
            rpcClient.getTokenAccountByOwner(recipientAddress, tokenId)
                .getOrNull()?.result?.value?.firstOrNull()?.pubkey
        }
        val tokenProgramJob = async {
            val owner = rpcClient.getTokenInfo(
                JSONRpcRequest(
                    SolanaMethod.GetAccountInfo.value,
                    params = listOf(
                        tokenId,
                        mapOf(
                            "encoding" to "jsonParsed"
                        ),
                    )
                )
            ).getOrNull()?.result?.value?.owner

            if (owner != null) {
                SolanaTokenProgramId.entries.firstOrNull { it.string == Config().getSolanaTokenProgramId(owner) } ?: SolanaTokenProgramId.Token
            } else {
                SolanaTokenProgramId.Token
            }
        }
        Triple(senderTokenAddressJob.await(), recipientTokenAddressJob.await(), tokenProgramJob.await())
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

    data class Info(
        val blockhash: String,
        val senderTokenAddress: String,
        val recipientTokenAddress: String?,
        val tokenProgram: SolanaTokenProgramId,
        val fee: GasFee,
    ) : SignerInputInfo {
        override fun fee(speed: TxSpeed): Fee = fee
    }
}