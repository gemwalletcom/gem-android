package com.gemwallet.android.blockchain.clients.stellar

import com.gemwallet.android.blockchain.clients.stellar.services.StellarFeeService
import com.gemwallet.android.model.Fee
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FeePriority
import java.math.BigInteger

class StellarFeeCalculator(
    private val chain: Chain,
    private val feeService: StellarFeeService,
) {
    suspend fun calculate(): List<Fee> {
        val fees = feeService.fee().getOrNull() ?: throw Exception("fee load error")
        val min = BigInteger(fees.fee_charged.min).max(BigInteger(fees.last_ledger_base_fee))
        val assetId = AssetId(chain)
        return listOf(
            Fee(
                priority = FeePriority.Slow,
                feeAssetId = assetId,
                amount = min,
            ),
            Fee(
                priority = FeePriority.Normal,
                feeAssetId = assetId,
                amount = min,
            ),
            Fee(
                priority = FeePriority.Fast,
                feeAssetId = assetId,
                amount = BigInteger(fees.fee_charged.p95) * BigInteger("2"),
            ),
        )
    }
}