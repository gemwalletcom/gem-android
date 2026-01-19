package com.gemwallet.features.settings.price_alerts.viewmodels

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.application.pricealerts.coordinators.IncludePriceAlert
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.model.format
import com.gemwallet.features.settings.price_alerts.viewmodels.models.PriceAlertTargetError
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.PriceAlertDirection
import com.wallet.core.primitives.PriceAlertNotificationType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PriceAlertTargetViewModel @Inject constructor(
    private val assetsRepository: AssetsRepository,
    private val includePriceAlert: IncludePriceAlert,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val value = TextFieldState()

    val assetId = savedStateHandle.getStateFlow<String?>("assetId", null)
        .filterNotNull()
        .map { it.toAssetId() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val assetInfo = assetId.filterNotNull().flatMapLatest { assetsRepository.getAssetInfo(it) }
    val currency = assetInfo.map { it?.price?.currency ?: Currency.USD }
        .stateIn(viewModelScope, SharingStarted.Eagerly, Currency.USD)
    val currentPrice = assetInfo.map { assetInfo -> assetInfo?.price?.let { it.currency.format(it.price.price) } ?: "" }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")
    val currentPriceValue = assetInfo.map { it?.price?.price?.price ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val error: StateFlow<PriceAlertTargetError?> = snapshotFlow { value.text }.map {
        val value = try { it.toString().toDouble() } catch (_: Throwable) { 0.0 }
        if (value <= 0.0) {
            PriceAlertTargetError.Zero
        } else {
            null
        }
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _direction = MutableStateFlow(PriceAlertDirection.Up)
    val direction: StateFlow<PriceAlertDirection> = _direction

    private val _type = MutableStateFlow(PriceAlertNotificationType.Price)
    val type: StateFlow<PriceAlertNotificationType> = _type

    fun onDirection(direction: PriceAlertDirection) {
        _direction.update { direction }
    }

    fun onType(type: PriceAlertNotificationType) {
        _type.update { type }
    }

    fun onConfirm() {
        val value = try {
            value.text.toString().toDouble()
        } catch (_: Throwable) {
            return
        }
        val (price, percentage, direction) = when (type.value) {
            PriceAlertNotificationType.Price -> Triple(value, null, null)
            PriceAlertNotificationType.PricePercentChange -> Triple(null, value, direction.value)
            PriceAlertNotificationType.Auto -> Triple(null, null, null)
        }
        viewModelScope.launch(Dispatchers.IO) {
            includePriceAlert.includePriceAlert(
                assetId = assetId.value ?: return@launch,
                currency = currency.value,
                price = price,
                percentage = percentage,
                direction = direction,
            )
        }
    }

}