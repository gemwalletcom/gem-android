package com.gemwallet.features.settings.settings.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.device.GetDeviceIdCase
import com.gemwallet.android.cases.device.GetPushEnabled
import com.gemwallet.android.cases.device.GetPushToken
import com.gemwallet.android.cases.device.SwitchPushEnabled
import com.gemwallet.android.data.repositoreis.config.UserConfig
import com.gemwallet.android.data.repositoreis.session.OnSessionChange
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.model.NotificationsAvailable
import com.gemwallet.android.model.Session
import com.wallet.core.primitives.Currency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
    private val getDeviceIdCase: GetDeviceIdCase,
    private val switchPushEnabled: SwitchPushEnabled,
    private val getPushToken: GetPushToken,
    private val getPushEnabled: GetPushEnabled,
    private val notificationsAvailable: NotificationsAvailable,
) : ViewModel(), OnSessionChange {

    private val state = MutableStateFlow(SettingsViewModelState())
    val uiState = state.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SettingsUIState.General())

    val pushEnabled = getPushEnabled.getPushEnabled()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        viewModelScope.launch {
            sessionRepository.session().collectLatest {
                refresh()
            }
        }
        refresh()
    }

    private fun refresh() = viewModelScope.launch(Dispatchers.IO) {
        state.update {
            it.copy(
                currency = sessionRepository.session().firstOrNull()?.currency ?: Currency.USD,
                developEnabled = userConfig.developEnabled(),
                deviceId = getDeviceIdCase.getDeviceId(),
                pushToken = getPushToken.getPushToken()
            )
        }
    }

    fun developEnable() {
        userConfig.developEnabled(!userConfig.developEnabled())
        refresh()
    }

    fun notificationEnable() {
        val pushEnabled = !pushEnabled.value
        viewModelScope.launch(Dispatchers.IO) {
            switchPushEnabled.switchPushEnabledCase(
                pushEnabled,
                walletsRepository.getAll().firstOrNull() ?: emptyList()
            )
        }
    }

    override fun onSessionChange(session: Session?) {
        state.update { it.copy(currency = session?.currency ?: Currency.USD) }
    }

    fun isNotificationsAvailable(): Boolean {
        return notificationsAvailable
    }
}

data class SettingsViewModelState(
    val currency: Currency = Currency.USD,
    val developEnabled: Boolean = false,
    val deviceId: String = "",
    val pushToken: String = "???",
) {
    fun toUIState(): SettingsUIState.General {
        return SettingsUIState.General(
            currency = currency,
            developEnabled = developEnabled,
        )
    }
}

sealed interface SettingsUIState {

    data class General(
        val currency: Currency = Currency.USD,
        val developEnabled: Boolean = false,
    ) : SettingsUIState
}