package com.gemwallet.android.features.asset_select.viewmodels

import androidx.lifecycle.ViewModel
import com.gemwallet.android.data.repositories.buy.BuyRepository
import com.wallet.core.primitives.AssetId
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SelectBuyAssetViewModel @Inject constructor(
    buyRepository: BuyRepository,
) : ViewModel() {

    private val available = buyRepository.getAvailable()

    fun getAvailableToBuy(): List<AssetId> = available
}