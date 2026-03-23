package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.model.ChainSignData
import com.wallet.core.primitives.SolanaTokenProgramId
import uniffi.gemstone.GemTransactionLoadMetadata

data class SolanaChainData(
    val blockHash: String,
    val senderTokenAddress: String?,
    val recipientTokenAddress: String?,
    val tokenProgram: SolanaTokenProgramId?,
) : ChainSignData {
    override fun toDto(): GemTransactionLoadMetadata {
        return GemTransactionLoadMetadata.Solana(
            blockHash = blockHash,
            senderTokenAddress = senderTokenAddress,
            recipientTokenAddress = recipientTokenAddress,
            tokenProgram = when (tokenProgram) {
                SolanaTokenProgramId.Token -> uniffi.gemstone.SolanaTokenProgramId.TOKEN
                SolanaTokenProgramId.Token2022 -> uniffi.gemstone.SolanaTokenProgramId.TOKEN2022
                null -> null
            },
        )
    }
}

fun GemTransactionLoadMetadata.Solana.toChainData(): SolanaChainData {
    return SolanaChainData(
        blockHash = blockHash,
        senderTokenAddress = senderTokenAddress,
        recipientTokenAddress = recipientTokenAddress,
        tokenProgram = when (tokenProgram) {
            uniffi.gemstone.SolanaTokenProgramId.TOKEN -> SolanaTokenProgramId.Token
            uniffi.gemstone.SolanaTokenProgramId.TOKEN2022 -> SolanaTokenProgramId.Token2022
            null -> null
        },
    )
}