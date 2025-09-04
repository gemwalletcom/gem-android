package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.model.ChainSignData
import com.wallet.core.primitives.SolanaTokenProgramId
import uniffi.gemstone.GemSolanaTokenProgramId
import uniffi.gemstone.GemTransactionLoadMetadata

data class SolanaChainData(
    val blockhash: String,
    val senderTokenAddress: String?,
    val recipientTokenAddress: String?,
    val tokenProgram: SolanaTokenProgramId?,
) : ChainSignData

fun GemTransactionLoadMetadata.Solana.toChainData(): SolanaChainData {
    return SolanaChainData(
        blockhash = blockHash,
        senderTokenAddress = senderTokenAddress,
        recipientTokenAddress = recipientTokenAddress,
        tokenProgram = when (tokenProgram) {
            GemSolanaTokenProgramId.TOKEN -> SolanaTokenProgramId.Token
            GemSolanaTokenProgramId.TOKEN2022 -> SolanaTokenProgramId.Token2022
            null -> null
        },
    )
}