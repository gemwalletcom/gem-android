package com.gemwallet.android

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositoreis.bridge.BridgesRepository
import com.gemwallet.android.data.repositoreis.config.UserConfig
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.features.bridge.proposal.ProposalScene
import com.gemwallet.android.features.bridge.request.RequestScene
import com.gemwallet.android.interactors.CheckAccounts
import com.gemwallet.android.services.SyncService
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.WalletApp
import com.gemwallet.android.ui.components.designsystem.Spacer16
import com.gemwallet.android.ui.components.designsystem.padding16
import com.gemwallet.android.ui.theme.WalletTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executor
import javax.inject.Inject
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.system.exitProcess

@AndroidEntryPoint
class MainActivity : SecureBaseFragmentActivity() {
    private val authenticators = if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.Q) {
        BIOMETRIC_STRONG or DEVICE_CREDENTIAL
    } else {
        BIOMETRIC_WEAK or DEVICE_CREDENTIAL
    }

    private val viewModel: MainViewModel by viewModels()
    private val walletConnectViewModel: WalletConnectViewModel by viewModels()

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private var onSuccessAuth: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prepareBiometricAuth()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onActivityResumed()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onActivityPaused()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun MainContent() {
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val enableSysAuth = enabledSysAuth()
        val authState = (state.initialAuth == AuthState.Required || state.authState == AuthState.Required)
        if (authState && enableSysAuth) {
            biometricPrompt.authenticate(promptInfo)
        } else {
            if (state.authState == AuthState.Success) {
                onSuccessAuth?.invoke()
            }
        }
        WalletTheme {
            if (state.initialAuth == AuthState.Success || !enableSysAuth) {
                WalletApp()
                OnWalletConnect()
            } else {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)) {
                    Image(
                        modifier = Modifier
                            .size(100.dp)
                            .align(Alignment.Center),
                        painter = painterResource(id = R.drawable.ic_splash),
                        contentDescription = "splash"
                    )
                }
            }

            if (viewModel.resetWCPairing()) {
                makeText(LocalContext.current, stringResource(id = R.string.wallet_connect_connection_title), Toast.LENGTH_SHORT).show()
            }

            if (!state.wcError.isNullOrEmpty()) {
                BasicAlertDialog(
                    onDismissRequest = viewModel::resetWcError,
                ) {
                    Box(
                        contentAlignment= Alignment.Center,
                        modifier = Modifier.background(
                            MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(8.dp)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(start = padding16, end = padding16, top = padding16),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = state.wcError!!,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.W400,
                                textAlign = TextAlign.Center,
                            )
                            Spacer16()
                            TextButton(onClick = viewModel::resetWcError) {
                                Text(text = stringResource(id = R.string.common_cancel))
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.hasExtra("walletIndex") || intent.hasExtra("assetId")) {
            val walletIndex = intent.getIntExtra("walletIndex", -1)
            val assetId = intent.getStringExtra("assetId")
            viewModel.onNotification(walletIndex, assetId)
        }
        val data = intent.data ?: return
        when (data.scheme) {
            "wc" -> viewModel.addPairing(data.toString())
        }
    }

    @Composable
    private fun OnWalletConnect() {
        val walletConnect by walletConnectViewModel.uiState.collectAsStateWithLifecycle()

        Box(
            modifier = Modifier.navigationBarsPadding(),
        ) {
            when (walletConnect) {
                is WalletConnectIntent.AuthRequest -> {}
                is WalletConnectIntent.ConnectionState -> {}
                WalletConnectIntent.None -> {}
                WalletConnectIntent.SessionDelete -> {}
                is WalletConnectIntent.SessionProposal -> ProposalScene(
                    proposal = (walletConnect as WalletConnectIntent.SessionProposal).sessionProposal,
                    onCancel = walletConnectViewModel::onCancel,
                )
                is WalletConnectIntent.SessionRequest -> RequestScene(
                    request = (walletConnect as WalletConnectIntent.SessionRequest).request,
                    onBuy = {},
                    onCancel = walletConnectViewModel::onCancel,
                )
            }
        }
    }

    private fun enabledSysAuth(): Boolean {
        val canAuth = BiometricManager.from(this).canAuthenticate(authenticators)
        return canAuth != BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
    }

    private fun prepareBiometricAuth() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (viewModel.uiState.value.initialAuth != AuthState.Success) {
                    finish()
                    exitProcess(0)
                } else {
                    viewModel.onAuth(AuthState.Fail)
                }
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccessAuth?.invoke()
                viewModel.onAuth(AuthState.Success)
            }

            override fun onAuthenticationFailed() {
                viewModel.onAuth(AuthState.Fail)
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.settings_security_authentication))
            .setAllowedAuthenticators(authenticators)
            .build()
    }

    fun auth(authRequest: AuthRequest) {
        if (viewModel.isAuthRequired(authRequest)) {
            viewModel.requestAuth(authRequest)
        } else {
            onSuccessAuth?.invoke()
        }
    }

    companion object {
        fun requestAuth(context: Context, auth: AuthRequest, onSuccess: () -> Unit) {
            val activity = context.getActivity() ?: exitProcess(0)
            if (activity.enabledSysAuth()) {
                activity.onSuccessAuth = onSuccess
                activity.auth(auth)
            } else {
                onSuccess()
            }
        }
    }
}

@OptIn(ExperimentalAtomicApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val userConfig: UserConfig,
    private val bridgesRepository: BridgesRepository,
    private val sessionRepository: SessionRepository,
    private val walletsRepository: WalletsRepository,
    private val syncService: SyncService,
    private val checkAccounts: CheckAccounts,
) : ViewModel() {

    private val state = MutableStateFlow(
        MainState(
            initialAuth = if (userConfig.authRequired()) AuthState.Required else AuthState.Success
        )
    )

    private val pauseTime = AtomicLong(0)

    val uiState = state.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, MainUIState())

    init {
        maintain()
    }

    fun isAuthRequired(authRequest: AuthRequest): Boolean =
        authRequest == AuthRequest.Enable || userConfig.authRequired()

    private fun maintain() {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.IO) { syncService.sync() }
            withContext(Dispatchers.IO) { checkAccounts() }
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
        state.update {
            it.copy(showWCPairing = false)
        }
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

    fun onNotification(walletIndex: Int, assetId: String?) = viewModelScope.launch(Dispatchers.IO) {
        walletIndex.takeIf { it > 0 }?.let { walletIndex ->
            if (sessionRepository.getSession()?.wallet?.index != walletIndex) {
                walletsRepository.getAll().firstOrNull()?.firstOrNull { it.index == walletIndex }?.let {
                    sessionRepository.setWallet(it)
                }
            }
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

enum class AuthRequest {
    Initial,
    Enable,
    Phrase,
}

enum class AuthState {
    Required,
    Success,
    Fail,
}

fun Context.getActivity(): MainActivity? = when (this) {
    is MainActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}