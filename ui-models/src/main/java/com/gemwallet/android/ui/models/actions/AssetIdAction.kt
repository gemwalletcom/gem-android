package com.gemwallet.android.ui.models.actions

import com.wallet.core.primitives.AssetId

fun interface AssetIdAction {
    fun invoke(assetId: AssetId)
}