package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.clients.SignerPreload
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.ext.type
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerInputInfo
import com.gemwallet.android.model.SignerParams
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class SolanaSignerPreloader(
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
        val feeJob = async { SolanaFee().invoke(rpcClient) }
        val (fee, blockhash) = Pair(feeJob.await(), blockhashJob.await())

        if (blockhash.isNullOrEmpty()) {
            return@withContext Result.failure(Exception("Can't get latest blockhash"))
        }
        val info = when (params) {
            is ConfirmParams.TransferParams -> when (params.assetId.type()) {
                AssetSubtype.NATIVE -> {
                    Info(blockhash, "", null, fee)
                }
                AssetSubtype.TOKEN -> {
                    val (senderTokenAddress, recipientTokenAddress) = getTokenAccounts(params.assetId.tokenId!!, owner.address, params.destination.address)

                    if (senderTokenAddress.isNullOrEmpty()) {
                        return@withContext Result.failure(Exception("Sender token address is empty"))
                    }
                    Info(
                        blockhash,
                        senderTokenAddress,
                        recipientTokenAddress,
                        if (recipientTokenAddress.isNullOrEmpty()) {
                            fee.withOptions("tokenAccountCreation")
                        } else {
                            fee
                        },
                    )
                }
            }
            is ConfirmParams.SwapParams -> Info(blockhash, "", null, fee)
            is ConfirmParams.DelegateParams,
            is ConfirmParams.RedeleateParams,
            is ConfirmParams.RewardsParams,
            is ConfirmParams.TokenApprovalParams,
            is ConfirmParams.UndelegateParams,
            is ConfirmParams.WithdrawParams -> Info(blockhash, "", null, fee)
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
    ): Pair<String?, String?> = withContext(Dispatchers.IO) {
        val senderTokenAddressJob = async {
            rpcClient.getTokenAccountByOwner(senderAddress, tokenId)
                .getOrNull()?.result?.value?.firstOrNull()?.pubkey
        }
        val recipientTokenAddressJob = async {
            rpcClient.getTokenAccountByOwner(recipientAddress, tokenId)
                .getOrNull()?.result?.value?.firstOrNull()?.pubkey
        }
        Pair(senderTokenAddressJob.await(), recipientTokenAddressJob.await())
    }

    override fun maintainChain(): Chain = Chain.Solana

    data class Info(
        val blockhash: String,
        val senderTokenAddress: String,
        val recipientTokenAddress: String?,
        val fee: Fee,
    ) : SignerInputInfo {
        override fun fee(): Fee = fee
    }
}