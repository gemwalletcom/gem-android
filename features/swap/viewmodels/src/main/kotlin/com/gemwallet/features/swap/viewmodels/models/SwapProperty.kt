package com.gemwallet.features.swap.viewmodels.models

import com.gemwallet.android.ui.models.PercentageFormattedUIModel

sealed interface SwapProperty {

    class Rate(
        val forward: String,
        val reverse: String,
    ) : SwapProperty

    class Estimate(val data: String) : SwapProperty

    class PriceImpact(
        override val percentage: Double?,
        val type: PriceImpactType,
        val isHigh: Boolean,
    ) : PercentageFormattedUIModel, SwapProperty

    class MinReceive(val data: String) : SwapProperty

    class Slippage(override val percentage: Double?,
    ) : PercentageFormattedUIModel, SwapProperty {

        override val percentageShowSign: Boolean
            get() = false

        override val minimumFractionDigits: Int
            get() = 1
    }
}