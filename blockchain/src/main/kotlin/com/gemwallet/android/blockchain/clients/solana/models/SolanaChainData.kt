package com.gemwallet.android.blockchain.clients.solana.models

import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.GasFee
import com.wallet.core.primitives.FeePriority
import com.wallet.core.primitives.SolanaTokenProgramId

data class SolanaChainData(
    val blockhash: String,
    val senderTokenAddress: String?,
    val recipientTokenAddress: String?,
    val tokenProgram: SolanaTokenProgramId?,
    val fees: List<GasFee>,
) : ChainSignData {
    override fun fee(speed: FeePriority): Fee = fees.firstOrNull { it.priority == speed } ?: fees.first()

    override fun allFee(): List<Fee> = fees
}