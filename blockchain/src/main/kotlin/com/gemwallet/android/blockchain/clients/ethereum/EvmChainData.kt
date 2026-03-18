package com.gemwallet.android.blockchain.clients.ethereum

import com.gemwallet.android.model.ChainSignData
import com.wallet.core.primitives.ContractCallData
import com.wallet.core.primitives.swap.ApprovalData
import uniffi.gemstone.GemTransactionLoadMetadata
import java.math.BigInteger
import kotlin.String

data class EvmChainData(
    val chainId: Int,
    val nonce: BigInteger,
    val stakeData: ContractCallData?,
) : ChainSignData


fun GemTransactionLoadMetadata.Evm.toChainData(): EvmChainData {
    return EvmChainData(
        chainId = chainId.toInt(),
        nonce = BigInteger.valueOf(nonce.toLong()),
        stakeData = contractCall?.let {
            ContractCallData(
                contractAddress = it.contractAddress,
                callData = it.callData,
                approval = it.approval?.let {
                    ApprovalData(
                        token = it.token,
                        spender = it.spender,
                        value = it.value,
                    )
                },
                gasLimit = it.gasLimit,
            )
        }
    )
}