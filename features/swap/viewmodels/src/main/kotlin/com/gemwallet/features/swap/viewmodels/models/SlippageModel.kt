package com.gemwallet.features.swap.viewmodels.models

import com.gemwallet.android.ui.models.PercentageFormattedUIModel

class SlippageModel(
    override val percentage: Double?,
) : PercentageFormattedUIModel {
    override val percentageShowSign: Boolean
        get() = false

    override val minimumFractionDigits: Int
        get() = 1
}