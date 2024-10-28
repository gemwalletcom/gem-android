package com.gemwallet.android.cases.pricealerts

import com.wallet.core.primitives.AssetId

interface EnablePriceAlertCase {
    suspend fun enabled(assetId: AssetId, enabled: Boolean)
}