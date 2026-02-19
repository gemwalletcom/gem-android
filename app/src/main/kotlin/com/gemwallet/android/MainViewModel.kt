package com.gemwallet.android

import android.content.Intent
import android.os.SystemClock
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.parseNotificationData
import com.gemwallet.android.data.repositoreis.bridge.BridgesRepository
import com.gemwallet.android.data.repositoreis.config.UserConfig
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.model.AuthRequest
import com.gemwallet.android.model.AuthState
import com.gemwallet.android.model.PushNotificationData
import com.gemwallet.android.services.CheckAccountsService
import com.gemwallet.android.services.SyncService
import com.gemwallet.android.ui.navigation.routes.assetRouteUri
import com.gemwallet.android.ui.navigation.routes.referralRouteUriGem
import com.gemwallet.android.ui.navigation.routes.supportUri
import com.wallet.core.primitives.PushNotificationTypes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val userConfig: UserConfig,
    private val bridgesRepository: BridgesRepository,
    private val sessionRepository: SessionRepository,
    private val walletsRepository: WalletsRepository,
    private val syncService: SyncService,
    private val checkAccountsService: CheckAccountsService,
) : ViewModel() {

    val intent = MutableStateFlow<Intent?>(null)

    private val state = MutableStateFlow(
        MainState(
            initialAuth = if (userConfig.authRequired()) AuthState.Required else AuthState.Success
        )
    )

    private val pauseTime = AtomicLong(0)

    val uiState = state.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Companion.Eagerly, MainUIState())

    fun isAuthRequired(authRequest: AuthRequest): Boolean =
        authRequest == AuthRequest.Enable || userConfig.authRequired()

    internal fun maintain() {
        viewModelScope.launch(Dispatchers.IO) {
            syncService.sync()
        }
        viewModelScope.launch(Dispatchers.IO) {
            checkAccountsService()
        }
    }

    fun requestAuth(request: AuthRequest = AuthRequest.Initial) {
        state.update { it.copy(authRequest = request, authState = AuthState.Required) }
    }

    fun onAuth(authState: AuthState) {
        when {
            state.value.initialAuth != AuthState.Success -> state.update { it.copy(initialAuth = authState) }
            state.value.authRequest != null -> state.update { it.copy(authState = authState) }
        }
    }

    fun addPairing(uri: String) {
        state.update {
            it.copy(showWCPairing = true)
        }
        if (uri.contains("requestId")) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            bridgesRepository.addPairing(
                uri = uri,
                onSuccess = {},
                onError = { error ->
                    state.update {
                        it.copy(wcError = error)
                    }
                }
            )
        }
    }

    fun resetWCPairing(): Boolean {
        val showWCPairing = state.value.showWCPairing
        state.update { it.copy(showWCPairing = false) }
        return showWCPairing
    }

    fun resetWcError() {
        state.update { it.copy(wcError = null) }
    }

    fun onActivityResumed() {
        viewModelScope.launch(Dispatchers.IO) {
            val interval = SystemClock.uptimeMillis() - pauseTime.load()
            if (userConfig.authRequired() && interval > (userConfig.getLockInterval().firstOrNull() ?: 0) * 60 * 1000) {
                state.update { it.copy(initialAuth = AuthState.Required) }
            }
        }
    }

    fun onActivityPaused() {
        pauseTime.store(SystemClock.uptimeMillis())
    }


    suspend fun onWallet(walletId: String) {
        walletId.takeIf { it.isNotEmpty() }?.let { walletId ->
            if (sessionRepository.session().firstOrNull()?.wallet?.id != walletId) {
                walletsRepository.getAll().firstOrNull()?.firstOrNull { it.id == walletId }?.let {
                    sessionRepository.setWallet(it)
                }
            }
        }
    }

    fun handleIntent(intent: Intent) = viewModelScope.launch(Dispatchers.IO) {
        // Handle push notification if app in background or unloaded
        val intent = if (intent.hasExtra("walletId")) {
            val walletId = intent.getStringExtra("walletId") ?: ""
            onWallet(walletId)
            intent
        } else if (intent.extras != null) {
            val data = parseNotificationData(
                intent.getStringExtra("type"),
                intent.getStringExtra("data")
            )
            if (intent.getStringExtra("type") == PushNotificationTypes.Support.string) {
                Intent().apply {
                    setData(supportUri.toUri())
                }
            } else {
                when (data) {
                    is PushNotificationData.Asset -> Intent().apply {
                        setData("${assetRouteUri}/${data.assetId}".toUri())
                    }

                    is PushNotificationData.Transaction -> {
                        onWallet(data.walletId)
                        Intent().apply {
                            setData("${assetRouteUri}/${data.assetId}".toUri())
                        }
                    }

                    is PushNotificationData.Reward -> {
                        Intent().apply {
                            setData(referralRouteUriGem.toUri())
                        }
                    }

                    is PushNotificationData.PushNotificationPayloadType,
                    is PushNotificationData.Swap,
                    null -> intent
                }
            }
        } else {
            intent
        }

        val data = intent.data ?: return@launch

        when (data.scheme) {
            "wc" -> addPairing(data.toString())
            else -> this@MainViewModel.intent.update { intent }
        }
    }

    data class MainState(
        val initialAuth: AuthState = AuthState.Required,
        val authRequest: AuthRequest? = null,
        val authState: AuthState? = null,
        val showWCPairing: Boolean = false,
        val wcError: String? = null,
    ) {
        fun toUIState(): MainUIState {
            return MainUIState(
                initialAuth = initialAuth,
                authState = authState,
                wcError = wcError,
            )
        }
    }

    class MainUIState(
        val initialAuth: AuthState = AuthState.Required,
        val authState: AuthState? = null,
        val wcError: String? = null,
    )
}