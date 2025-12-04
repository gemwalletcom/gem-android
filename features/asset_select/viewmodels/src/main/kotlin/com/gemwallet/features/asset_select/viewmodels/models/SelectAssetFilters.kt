package com.gemwallet.features.asset_select.viewmodels.models

import com.gemwallet.android.model.Session
import com.wallet.core.primitives.AssetTag
import com.wallet.core.primitives.Chain

class SelectAssetFilters(
    val session: Session?,
    val query: String,
    val chainFilter: List<Chain>,
    val hasBalance: Boolean,
    val tag: AssetTag?,
)