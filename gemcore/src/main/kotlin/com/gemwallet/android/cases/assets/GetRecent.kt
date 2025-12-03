package com.gemwallet.android.cases.assets

import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.RecentType
import kotlinx.coroutines.flow.Flow

interface GetRecent {
    fun getRecent(type: RecentType): Flow<List<AssetInfo>>
}