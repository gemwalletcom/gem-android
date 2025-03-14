package com.gemwallet.android.features.settings.settings.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.device.GetDeviceIdCase
import com.gemwallet.android.cases.device.GetPushEnabledCase
import com.gemwallet.android.cases.device.GetPushTokenCase
import com.gemwallet.android.cases.device.SwitchPushEnabledCase
import com.gemwallet.android.cases.device.SyncDeviceInfoCase
import com.gemwallet.android.cases.device.SyncSubscriptionCase
import com.gemwallet.android.data.repositoreis.config.UserConfig
import com.gemwallet.android.data.repositoreis.session.OnSessionChange
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.model.Session
import com.wallet.core.primitives.Currency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userConfig: UserConfig,
    private val walletsRepository: WalletsRepository,
    private val sessionRepository: SessionRepository,
    private val syncDeviceInfoCase: SyncDeviceInfoCase,
    private val getDeviceIdCase: GetDeviceIdCase,
    private val switchPushEnabledCase: SwitchPushEnabledCase,
    private val getPushTokenCase: GetPushTokenCase,
    private val getPushEnabledCase: GetPushEnabledCase,
    private val syncSubscriptionCase: SyncSubscriptionCase,
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
                pushEnabled = getPushEnabledCase.getPushEnabled(),
                developEnabled = userConfig.developEnabled(),
                deviceId = getDeviceIdCase.getDeviceId(),
                pushToken = getPushTokenCase.getPushToken()
            )
        }
    }

    fun developEnable() {
        userConfig.developEnabled(!userConfig.developEnabled())
        refresh()
    }

    fun notificationEnable() {
        val pushEnabled = !state.value.pushEnabled
        state.update { it.copy(pushEnabled = pushEnabled) }
        viewModelScope.launch {
            switchPushEnabledCase.switchPushEnabledCase(pushEnabled)
            syncDeviceInfoCase.syncDeviceInfo()
            syncSubscriptionCase.syncSubscription(walletsRepository.getAll().firstOrNull() ?: emptyList())
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