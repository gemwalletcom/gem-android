package com.gemwallet.android.features.settings.price_alerts.viewmodels

import com.gemwallet.android.cases.pricealerts.GetPriceAlertsCase
import com.gemwallet.android.cases.tokens.SearchTokensCase
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.features.asset_select.viewmodels.BaseAssetSelectViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class PriceAlertsSelectViewModel @Inject constructor(
    private val getPriceAlertsCase: GetPriceAlertsCase,
    sessionRepository: SessionRepository,
    assetsRepository: AssetsRepository,
    searchTokensCase: SearchTokensCase,
) : BaseAssetSelectViewModel(
    sessionRepository,
    assetsRepository,
    searchTokensCase,
    { getPriceAlertsCase.getPriceAlerts().map { it.map { it.assetId } } },
) {

    override fun isSearchByAllWallets(): Boolean = true
}