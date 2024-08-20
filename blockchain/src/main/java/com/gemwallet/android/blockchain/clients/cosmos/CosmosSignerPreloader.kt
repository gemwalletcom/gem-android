package com.gemwallet.android.blockchain.clients.cosmos

import com.gemwallet.android.blockchain.clients.SignerPreload
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerInputInfo
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class CosmosSignerPreloader(
    private val chain: Chain,
    private val rpcClient: CosmosRpcClient,
) : SignerPreload {
    override suspend fun invoke(
        owner: Account,
        params: ConfirmParams,
    ): Result<SignerParams> = withContext(Dispatchers.IO) {
        val accountJob = async {
            if (chain == Chain.Injective) {
                rpcClient.getInjectiveAccountData(owner.address).getOrNull()?.account?.base_account
            } else {
                rpcClient.getAccountData(owner.address).getOrNull()?.account
            }
        }
        val nodeInfoJob = async { rpcClient.getNodeInfo() }
        val feeJob = async { CosmosFee(txType = params.getTxType()).invoke(chain) }

        val (account, nodeInfo, fee) = Triple(
            accountJob.await(),
            nodeInfoJob.await().getOrNull(),
            feeJob.await()
        )
        if (account != null && nodeInfo != null) {
            Result.success(
                SignerParams(
                    input = params,
                    owner = owner.address,
                    info = Info(
                        chainId = nodeInfo.block.header.chain_id,
                        accountNumber = account.account_number.toLong(),
                        sequence = account.sequence.toLong(),
                        fee = fee,
                    )
                )
            )
        } else {
            Result.failure(Exception("Can't get data for sign"))
        }
    }

    override fun maintainChain(): Chain = chain

    data class Info(
        val chainId: String,
        val accountNumber: Long,
        val sequence: Long,
        val fee: Fee,
    ) : SignerInputInfo {
        override fun fee(speed: TxSpeed): Fee = fee
    }
}