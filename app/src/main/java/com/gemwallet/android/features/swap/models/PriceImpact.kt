package com.gemwallet.android.features.swap.models

import com.gemwallet.android.ui.models.PercentageFormattedUIModel

data class PriceImpact(
    override val percentage: Double?,
    val type: PriceImpactType,
) : PercentageFormattedUIModel