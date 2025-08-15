package com.gemwallet.features.swap.viewmodels.models

import com.gemwallet.android.ui.models.PercentageFormattedUIModel

data class PriceImpact(
    override val percentage: Double?,
    val type: PriceImpactType,
    val isHigh: Boolean,
) : PercentageFormattedUIModel