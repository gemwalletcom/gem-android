package com.gemwallet.android.features.settings.settings.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.pricealerts.EnablePriceAlertCase
import com.gemwallet.android.data.repositoreis.config.ConfigRepository
import com.gemwallet.android.data.repositoreis.session.OnSessionChange
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.interactors.sync.SyncDevice
import com.gemwallet.android.interactors.sync.SyncSubscription
import com.gemwallet.android.model.Session
import com.wallet.core.primitives.Currency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val gemApiClient: GemApiClient, // TODO: Redesign - Encapsulate api clients (remote source)
    private val config: ConfigRepository,
    private val walletsRepository: WalletsRepository,
    private val sessionRepository: SessionRepository,
    private val enablePriceAlertCase: EnablePriceAlertCase,
) : ViewModel(), OnSessionChange {

    private val state = MutableStateFlow(SettingsViewModelState())
    val uiState = state.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SettingsUIState.General())

    init {
        viewModelScope.launch {
            sessionRepository.session().collectLatest {
                refresh()
            }
        }
        refresh()
    }

    private fun refresh() {
        state.update {
            it.copy(
                currency = sessionRepository.getSession()?.currency ?: Currency.USD,
                pushEnabled = config.pushEnabled(),
                developEnabled = config.developEnabled(),
                deviceId = config.getDeviceId(),
                pushToken = config.getPushToken()
            )
        }
    }

    fun developEnable() {
        config.developEnabled(!config.developEnabled())
        refresh()
    }

    fun notificationEnable() {
        val pushEnabled = !state.value.pushEnabled
        state.update { it.copy(pushEnabled = pushEnabled) }
        viewModelScope.launch {
            config.pushEnabled(pushEnabled) // TODO: Redesign this and next actions
            SyncDevice(gemApiClient, config, sessionRepository, enablePriceAlertCase).invoke()
            SyncSubscription(gemApiClient = gemApiClient, configRepository = config, walletsRepository = walletsRepository).invoke() // TODO: Redesign injection
        }
    }

    override fun onSessionChange(session: Session?) {
        state.update { it.copy(currency = session?.currency ?: Currency.USD) }
    }
}

data class SettingsViewModelState(
    val currency: Currency = Currency.USD,
    val pushEnabled: Boolean = false,
    val developEnabled: Boolean = false,
    val deviceId: String = "",
    val pushToken: String = "???",
) {
    fun toUIState(): SettingsUIState.General {
        return SettingsUIState.General(
            currency = currency,
            pushEnabled = pushEnabled,
            developEnabled = developEnabled,
        )
    }
}

sealed interface SettingsUIState {

    data class General(
        val currency: Currency = Currency.USD,
        val pushEnabled: Boolean = false,
        val developEnabled: Boolean = false,
    ) : SettingsUIState
}