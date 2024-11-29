package com.gemwallet.android.features.asset_select.models

import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Session
import kotlinx.coroutines.flow.Flow

fun interface SelectSearch {
    operator fun invoke(session: Flow<Session?>, query: Flow<String>): Flow<List<AssetInfo>>
}