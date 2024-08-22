package com.gemwallet.android.blockchain.clients.near

import com.gemwallet.android.blockchain.clients.SignerPreload
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerInputInfo
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import wallet.core.jni.Base58
import java.math.BigInteger

class NearSignerPreloader(
    private val chain: Chain,
    private val rpcClient: NearRpcClient,
) : SignerPreload {
    override suspend fun invoke(owner: Account, params: ConfirmParams): Result<SignerParams> = withContext(Dispatchers.IO) {
        val getAccountJob = async {
            val publicKey = "ed25519:" + Base58.encodeNoCheck(owner.address.decodeHex())
            rpcClient.accountAccessKey(
                JSONRpcRequest(
                    method = NearMethod.Query.value,
                    params = mapOf(
                        "request_type" to "view_access_key",
                        "finality" to "final",
                        "account_id" to owner.address,
                        "public_key" to publicKey,
                    )
                )
            ).getOrNull()
        }
        val blockJob = async {
            rpcClient.latestBlock(
                JSONRpcRequest(
                    method = NearMethod.LatestBlock.value,
                    params = mapOf(
                        "finality" to "final",
                    )
                )
            ).getOrNull()
        }
        val gasPriceJob = async {
            rpcClient.getGasPrice(
                JSONRpcRequest(
                    method = NearMethod.GasPrice.value,
                    params = listOf(null),
                )
            ).getOrNull()
        }
        val account = getAccountJob.await()?.result ?: throw IllegalStateException("Can't get account")
        val block = blockJob.await() ?: throw IllegalStateException("Can't get block")
        val gasPrice = gasPriceJob.await() ?: throw IllegalStateException("Can't get gas price")

        val fee = BigInteger("900000000000000000000")

        Result.success(
            SignerParams(
                input = params,
                owner = owner.address,
                info = Info(
                    sequence = account.nonce + 1L,
                    block = block.result.header.hash,
                    fee = Fee(
                        feeAssetId = AssetId(chain),
                        speed = TxSpeed.Normal,
                        amount = fee,
                    )
                )
            )
        )
    }

    override fun maintainChain(): Chain  = chain

    data class Info(
        val block: String,
        val sequence: Long,
        val fee: Fee,
    ) : SignerInputInfo {
        override fun fee(speed: TxSpeed): Fee = fee
    }
}