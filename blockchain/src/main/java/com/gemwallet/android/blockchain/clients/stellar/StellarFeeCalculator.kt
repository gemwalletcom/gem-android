package com.gemwallet.android.blockchain.clients.stellar

import com.gemwallet.android.blockchain.clients.stellar.services.StellarFeeService
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
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
                speed = TxSpeed.Slow,
                feeAssetId = assetId,
                amount = min,
            ),
            Fee(
                speed = TxSpeed.Normal,
                feeAssetId = assetId,
                amount = min,
            ),
            Fee(
                speed = TxSpeed.Fast,
                feeAssetId = assetId,
                amount = BigInteger(fees.fee_charged.p95) * BigInteger("2"),
            ),
        )
    }
}