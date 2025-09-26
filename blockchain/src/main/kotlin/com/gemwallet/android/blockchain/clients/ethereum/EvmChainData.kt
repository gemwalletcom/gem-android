package com.gemwallet.android.blockchain.clients.ethereum

import com.gemwallet.android.model.ChainSignData
import com.wallet.core.primitives.StakeData
import uniffi.gemstone.GemTransactionLoadMetadata
import java.math.BigInteger

data class EvmChainData(
    val chainId: Int,
    val nonce: BigInteger,
    val stakeData: StakeData?,
) : ChainSignData


fun GemTransactionLoadMetadata.Evm.toChainData(): EvmChainData {
    return EvmChainData(
        chainId = chainId.toInt(),
        nonce = BigInteger.valueOf(nonce.toLong()),
        stakeData = stakeData?.let {
            StakeData(
                data = it.data,
                to = it.to
            )
        }
    )
}