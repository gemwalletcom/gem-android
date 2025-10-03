package com.gemwallet.android.ui.models

import com.gemwallet.android.domains.asset.getIconUrl
import com.wallet.core.primitives.Asset

interface AssetUIModel {
    val asset: Asset
}