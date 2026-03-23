package com.gemwallet.android.blockchain.clients.ethereum

import com.gemwallet.android.model.ChainSignData
import com.wallet.core.primitives.ContractCallData
import com.wallet.core.primitives.swap.ApprovalData
import uniffi.gemstone.GemApprovalData
import uniffi.gemstone.GemContractCallData
import uniffi.gemstone.GemTransactionLoadMetadata
import java.math.BigInteger

data class EvmChainData(
    val chainId: Int,
    val nonce: BigInteger,
    val contractCall: ContractCallData?,
) : ChainSignData {
    override fun toDto(): GemTransactionLoadMetadata {
        return GemTransactionLoadMetadata.Evm(
            chainId = chainId.toLong().toULong(),
            nonce = nonce.toLong().toULong(),
            contractCall = contractCall?.let {
                GemContractCallData(
                    contractAddress = it.contractAddress,
                    callData = it.callData,
                    approval = it.approval?.let {
                        GemApprovalData(
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
}


fun GemTransactionLoadMetadata.Evm.toChainData(): EvmChainData {
    return EvmChainData(
        chainId = chainId.toInt(),
        nonce = BigInteger.valueOf(nonce.toLong()),
        contractCall = contractCall?.let {
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