package com.gemwallet.android.blockchain.clients.ethereum

import android.util.Log
import com.gemwallet.android.blockchain.clients.SignerPreload
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.ext.type
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerInputInfo
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.EVMChain
import com.wallet.core.primitives.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.BigInteger
import java.util.Locale

class EvmSignerPreloader(
    private val chain: Chain,
    private val rpcClient: EvmRpcClient,
) : SignerPreload {
    override suspend fun invoke(owner: Account, params: ConfirmParams): Result<SignerParams> = withContext(Dispatchers.IO) {
        val assetId = if (params.getTxType() == TransactionType.Swap) {
            AssetId(params.assetId.chain)
        } else {
            params.assetId
        }
        val coinType = WCChainTypeProxy().invoke(chain)
        val chainIdJob = async {
            try {
                rpcClient.getNetVersion(JSONRpcRequest.create(EvmMethod.GetNetVersion, emptyList()))
                    .fold({ it.result?.value }) { null } ?: BigInteger(coinType.chainId())
            } catch (err: Throwable) {
                Log.d("ERROR", "Err: ", err)
                BigInteger(coinType.chainId())
            }
        }
        val nonceJob = async {
            try {
                val nonceParams = listOf(owner.address, "latest")
                rpcClient.getNonce(JSONRpcRequest.create(EvmMethod.GetNonce, nonceParams))
                    .fold({ it.result?.value }) { null } ?: BigInteger.ZERO
            } catch (err: Throwable) {
                Log.d("ERROR", "Err: ", err)
                BigInteger.ZERO
            }
        }
        val gasLimitJob = async {
            try {
                getGasLimit(
                    assetId = assetId,
                    rpcClient = rpcClient,
                    from = owner.address,
                    recipient = when (params) {
                        is ConfirmParams.SwapParams -> params.to
                        is ConfirmParams.TokenApprovalParams -> params.contract
                        is ConfirmParams.TransferParams -> params.destination().address
                        is ConfirmParams.RedeleateParams,
                        is ConfirmParams.WithdrawParams,
                        is ConfirmParams.UndelegateParams,
                        is ConfirmParams.RewardsParams,
                        is ConfirmParams.DelegateParams -> StakeHub.address

                        else -> throw IllegalArgumentException()
                    },
                    outputAmount = when (params) {
                        is ConfirmParams.SwapParams -> BigInteger(params.value)
                        is ConfirmParams.TokenApprovalParams -> BigInteger.ZERO
                        is ConfirmParams.TransferParams,
                        is ConfirmParams.DelegateParams -> params.amount

                        is ConfirmParams.RedeleateParams,
                        is ConfirmParams.WithdrawParams,
                        is ConfirmParams.UndelegateParams -> BigInteger.ZERO

                        else -> throw IllegalArgumentException()
                    },
                    payload = when (params) {
                        is ConfirmParams.SwapParams -> params.swapData
                        is ConfirmParams.TokenApprovalParams -> params.data
                        is ConfirmParams.RedeleateParams,
                        is ConfirmParams.WithdrawParams,
                        is ConfirmParams.UndelegateParams,
                        is ConfirmParams.DelegateParams -> when (params.assetId.chain) {
                            Chain.SmartChain -> StakeHub().encodeStake(params)
                            else -> throw IllegalArgumentException()
                        }

                        else -> params.memo()
                    },
                )
            } catch (err: Throwable) {
                Log.d("ERROR", "Err: ", err)
                throw err
            }
        }
        val chainId = chainIdJob.await()
        val nonce = nonceJob.await()
        val gasLimit = gasLimitJob.await()
        val fee = try {
            EvmFee().invoke(
                rpcClient,
                params,
                chainId,
                nonce,
                gasLimit,
                WCChainTypeProxy().invoke(chain)
            )
        } catch (err: Throwable) {
            return@withContext Result.failure(err)
        }
        Result.success(
            SignerParams(
                input = params,
                owner = owner.address,
                info = Info(chainId, nonce, fee),
            )
        )
    }

    override fun maintainChain(): Chain = chain

    private suspend fun getGasLimit(
        assetId: AssetId,
        rpcClient: EvmRpcClient,
        from: String,
        recipient: String,
        outputAmount: BigInteger,
        payload: String?,
    ): BigInteger {
        val (amount, to, data) = when (assetId.type()) {
            AssetSubtype.NATIVE -> Triple(outputAmount, recipient, payload)
            AssetSubtype.TOKEN -> Triple(
                BigInteger.ZERO,    // Amount
                assetId.tokenId!!.lowercase(Locale.ROOT), // token
                EVMChain.encodeTransactionData(assetId, payload, outputAmount, recipient)
                    .toHexString()
            )
            else -> throw IllegalArgumentException()
        }
        val transaction = EvmRpcClient.Transaction(
            from = from,
            to = to,
            value = "0x${amount.toString(16)}",
            data = if (data.isNullOrEmpty()) "0x" else data,
        )
        val request = JSONRpcRequest.create(EvmMethod.GetGasLimit, listOf(transaction))
        val gasLimitResult = rpcClient.getGasLimit(request)
        val gasLimit = gasLimitResult.fold({ it.result?.value ?: BigInteger.ZERO}) {
            BigInteger.ZERO
        }
        return if (gasLimit == BigInteger.valueOf(21_000L)) {
            gasLimit
        } else {
            gasLimit.add(
                gasLimit.toBigDecimal().multiply(
                    BigDecimal.valueOf(0.5)
                ).toBigInteger()
            )
        }
    }

    data class Info(
        val chainId: BigInteger,
        val nonce: BigInteger,
        val fee: Fee,
    ) : SignerInputInfo {
        override fun fee(speed: TxSpeed): Fee = fee
    }
}