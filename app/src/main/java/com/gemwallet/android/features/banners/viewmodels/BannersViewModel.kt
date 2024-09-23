package com.gemwallet.android.features.banners.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.banners.CancelBannerCase
import com.gemwallet.android.data.banners.GetBannersCase
import com.gemwallet.android.data.repositories.session.SessionRepository
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Banner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BannersViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val getBannersCase: GetBannersCase,
    private val cancelBannerCase: CancelBannerCase,
) : ViewModel() {

    val banners = MutableStateFlow<List<Banner>>(emptyList())

    fun init(asset: Asset?, isGlobal: Boolean) {
        val wallet = if (isGlobal) {
            null
        } else {
            sessionRepository.getSession()?.wallet
        }
        viewModelScope.launch {
            val banner = getBannersCase.getActiveBanners(wallet, asset)
            banners.update { listOf(banner).mapNotNull { it } }
        }
    }

    fun onCancel(banner: Banner) = viewModelScope.launch {
        cancelBannerCase.cancelBanner(banner)
        init(banner.asset, banner.wallet == null)
    }
}