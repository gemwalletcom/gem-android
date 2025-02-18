package com.gemwallet.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.BuildConfig
import com.gemwallet.android.cases.update.CheckForUpdateCase
import com.gemwallet.android.cases.update.DownloadLatestApkCase
import com.gemwallet.android.cases.update.SkipVersionCase
import com.gemwallet.android.data.repositoreis.config.UserConfig
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.features.onboarding.OnboardingDest
import com.gemwallet.android.model.Session
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val userConfig: UserConfig,
    private val checkForUpdateCase: CheckForUpdateCase,
    private val skipVersionCase: SkipVersionCase,
    private val downloadLatestApkCase: DownloadLatestApkCase,
) : ViewModel() {

    private val _appIntent = MutableSharedFlow<AppIntent>()
    val appIntent = _appIntent.asSharedFlow()

    private val state = MutableStateFlow(AppState())
    val uiState = state.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppUIState())

    init {
        viewModelScope.launch {
            handleAppVersion()
            rateAs()
            sessionRepository.session().collectLatest {
                onSession(it ?: return@collectLatest)
            }
        }
    }

    fun onSkip(version: String) {
        viewModelScope.launch {
            skipVersionCase.skipVersion(version)
        }
    }

    private fun hasSession(): Boolean = sessionRepository.hasSession()

    private suspend fun handleAppVersion() = withContext(Dispatchers.IO) {
        val newVersion = checkForUpdateCase.checkForUpdate()
        if (newVersion != null) {
            viewModelScope.launch {
                _appIntent.emit(AppIntent.ShowUpdate)
            }
        }
    }

    private suspend fun rateAs() {
        if (userConfig.getLaunchNumber() == 10) {
            _appIntent.emit(AppIntent.ShowReview)
        }
        userConfig.increaseLaunchNumber()
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

    fun onConfirmUpdate() {
        viewModelScope.launch {
            if (BuildConfig.FLAVOR == "universal") {
                downloadLatestApkCase.downloadLatestApk()
            } else {
                _appIntent.emit(AppIntent.OpenUrl(BuildConfig.UPDATE_URL))
            }
        }
    }
}

data class AppState(
    val session: Session? = null,
    val version: String = "",
) {
    fun toUIState() = AppUIState(
        session = session,
        version = version,
    )
}

data class AppUIState(
    val session: Session? = null,
    val version: String = "",
)

sealed class AppIntent {
    object ShowUpdate : AppIntent()
    object ShowReview : AppIntent()
    class OpenUrl(val url: String) : AppIntent()
}