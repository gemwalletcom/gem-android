package com.gemwallet.android.features.asset_select.viewmodels

import com.gemwallet.android.cases.tokens.SearchTokensCase
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.buy.BuyRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.AssetInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class BuySelectViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    assetsRepository: AssetsRepository,
    searchTokensCase: SearchTokensCase,
    buyRepository: BuyRepository,
) : BaseAssetSelectViewModel(sessionRepository, assetsRepository, searchTokensCase, { MutableStateFlow(emptyList()) }) {

    private val available = buyRepository.getAvailable()

    override fun filterAsset(assetInfo: AssetInfo): Boolean {
        return available.contains(assetInfo.asset.id.toIdentifier())
    }
}