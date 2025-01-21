package com.gemwallet.android.features.confirm.models

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.chain
import com.gemwallet.android.ext.feeUnitType
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.SignMode
import com.gemwallet.android.model.TxSpeed
import com.gemwallet.android.ui.R
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FeeUnitType

data class FeeRateUIModel(
    val fee: Fee,
) {

    val speedLabel: String @Composable get() {
        return when (fee.speed) {
            TxSpeed.Fast -> "\uD83D\uDE80  ${stringResource(R.string.fee_rates_fast)}"
            TxSpeed.Normal -> "\uD83D\uDC8E  ${stringResource(R.string.fee_rates_normal)}"
            TxSpeed.Slow -> "\uD83D\uDC22  ${stringResource(R.string.fee_rates_slow)}"
        }
    }

    val price: String get() {
        val asset = fee.feeAssetId.chain.asset()
        val feeUnitType = asset.chain().feeUnitType()
        val decimals = when (feeUnitType) {
            FeeUnitType.SatVb -> 0
            FeeUnitType.Gwei -> 9
            FeeUnitType.Native -> asset.decimals
            null -> 0
        }
        val symbol = when (feeUnitType) {
            FeeUnitType.SatVb, FeeUnitType.Gwei -> feeUnitType.string
            FeeUnitType.Native -> asset.symbol
            null -> ""
        }
        val value = when (asset.chain()) {
            Chain.Solana -> fee.amount
            else -> (fee as? GasFee)?.maxGasPrice ?: fee.amount
        }
        val formattedValue = Crypto(value).format(decimals, symbol, 6, -1, SignMode.NoSign, true)
        return formattedValue
    }
}