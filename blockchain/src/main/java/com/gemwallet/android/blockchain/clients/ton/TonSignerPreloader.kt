package com.gemwallet.android.blockchain.clients.ton

import com.gemwallet.android.blockchain.clients.SignerPreload
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

internal const val tokenAccountCreationKey: String = "tokenAccountCreation"

class TonSignerPreloader(
    private val rpcClient: TonRpcClient,
) : SignerPreload {

    override suspend fun invoke(owner: Account, params: ConfirmParams): Result<SignerParams> = withContext(Dispatchers.IO) {
        when (params.assetId.type()) {
            AssetSubtype.NATIVE -> coinSign(owner, params)
            AssetSubtype.TOKEN -> tokenSign(owner, params)
        }

    }

    override fun maintainChain(): Chain = Chain.Ton

    private suspend fun coinSign(owner: Account, params: ConfirmParams): Result<SignerParams> {
        val fee = TonFee().invoke(rpcClient, params.assetId, params.destination()?.address!!, params.memo())
        return rpcClient.walletInfo(owner.address).mapCatching {
            SignerParams(
                input = params,
                owner = owner.address,
                info = Info(sequence = it.result.seqno ?: 0, fee = fee)
            )
        }
    }

    private suspend fun tokenSign(owner: Account, params: ConfirmParams): Result<SignerParams> = withContext(Dispatchers.IO) {
        val getWalletInfo = async { rpcClient.walletInfo(owner.address).getOrNull() }
        val getJettonAddress = async { jettonAddress(rpcClient, params.assetId.tokenId!!, owner.address) }
        val feeJob = async { TonFee().invoke(rpcClient, params.assetId, params.destination()?.address!!, params.memo()) }
        val walletInfo = getWalletInfo.await()
            ?: return@withContext Result.failure(Exception("can't get wallet info. check internet."))
        val jettonAddress = getJettonAddress.await() ?: return@withContext Result.failure(Exception("can't get jetton address. check internet."))
        val fee = feeJob.await()

        val signerParams = SignerParams(
            input = params,
            owner = owner.address,
            info = Info(
                sequence = walletInfo.result.seqno ?: 0,
                jettonAddress = jettonAddress,
                fee = fee,
            )
        )

        Result.success(signerParams)
    }

    data class Info(
        val sequence: Int,
        val jettonAddress: String? = null,
        val fee: Fee,
    ) : SignerInputInfo {
        override fun fee(): Fee = fee
    }
}