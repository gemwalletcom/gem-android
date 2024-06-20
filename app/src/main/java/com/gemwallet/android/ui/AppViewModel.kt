package com.gemwallet.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.BuildConfig
import com.gemwallet.android.data.config.ConfigRepository
import com.gemwallet.android.data.session.SessionRepository
import com.gemwallet.android.features.onboarding.OnboardingDest
import com.gemwallet.android.model.Session
import com.gemwallet.android.services.GemApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val configRepository: ConfigRepository,
    private val gemApiClient: GemApiClient,
) : ViewModel() {

    private val state = MutableStateFlow(AppState())
    val uiState = state.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppUIState())

    init {
        sessionRepository.subscribe(this::onSession)
        viewModelScope.launch {
            handleAppVersion()
        }
    }

    fun onSkip(version: String) {
        configRepository.setAppVersionSkip(version)
        state.update { it.copy(intent = AppIntent.None) }
    }

    fun onCancelUpdate() {
        state.update { it.copy(intent = AppIntent.None) }
    }

    private fun hasSession(): Boolean = sessionRepository.hasSession()

    private suspend fun handleAppVersion() = withContext(Dispatchers.IO) {
        if (BuildConfig.DEBUG) {
            return@withContext
        }
        val current = gemApiClient.getConfig().mapCatching {
            it.app.android.version.production
        }.getOrNull() ?: BuildConfig.VERSION_NAME
        val skipVersion = configRepository.getAppVersionSkip()
        if (current.compareTo(BuildConfig.VERSION_NAME) > 0 && skipVersion != current) {
            state.update {
                it.copy(intent = AppIntent.ShowUpdate, version = configRepository.getAppVersion())
            }
        }
    }

    private fun onSession(session: Session) {
        state.update {
            it.copy(session = session)
        }
    }

    fun getStartDestination(): String = if (hasSession()) {
        "/"
    } else {
        OnboardingDest.route
    }
}

data class AppState(
    val session: Session? = null,
    val version: String = "",
    val intent: AppIntent = AppIntent.None,
) {
    fun toUIState() = AppUIState(
        session = session,
        version = version,
        intent = intent,
    )
}

data class AppUIState(
    val session: Session? = null,
    val version: String = "",
    val intent: AppIntent = AppIntent.None,
)

enum class AppIntent {
    None,
    ShowUpdate,
}