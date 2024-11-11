package com.gemwallet.android.features.asset_select.viewmodels

import com.gemwallet.android.cases.tokens.SearchTokensCase
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.model.AssetInfo
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class SwapSelectViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    assetsRepository: AssetsRepository,
    searchTokensCase: SearchTokensCase,
): BaseAssetSelectViewModel(sessionRepository, assetsRepository, searchTokensCase, { MutableStateFlow(emptyList()) }) {

    override fun filterAsset(assetInfo: AssetInfo): Boolean {
        return super.filterAsset(assetInfo)
    }
}