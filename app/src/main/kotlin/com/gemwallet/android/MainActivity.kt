package com.gemwallet.android

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.gemwallet.android.cases.security.AuthRequester
import com.gemwallet.android.model.AuthRequest
import com.gemwallet.android.model.AuthState
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.WalletApp
import com.gemwallet.android.ui.components.RootWarningDialog
import com.gemwallet.android.ui.components.isDeviceRooted
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.WalletTheme
import com.gemwallet.android.ui.theme.paddingDefault
import com.gemwallet.features.bridge.views.ProposalScene
import com.gemwallet.features.bridge.views.RequestScene
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executor
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity : FragmentActivity(), AuthRequester {
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
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        prepareBiometricAuth()

        viewModel.handleIntent(this@MainActivity.intent)
        viewModel.maintain()
        setContent {
            MainContent()
            RootWarning()
        }
    }

    @Composable
    private fun RootWarning() {
        WalletTheme {
            var showRootWarningDialog by remember { mutableStateOf(isDeviceRooted()) }

            if (showRootWarningDialog) {
                RootWarningDialog(
                    onCancel = { this.finishAffinity() },
                    onIgnore = { showRootWarningDialog = false }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainContent() {
        val navController = rememberNavController()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val intent by viewModel.intent.collectAsStateWithLifecycle()
        val enableSysAuth = enabledSysAuth()
        val authState = state.initialAuth == AuthState.Required || state.authState == AuthState.Required
        if (authState && enableSysAuth) {
            biometricPrompt.authenticate(promptInfo)
        } else if (state.authState == AuthState.Success) {
            onSuccessAuth?.invoke()
            onSuccessAuth = null
        }
        LaunchedEffect(intent) {
            try {
                navController.handleDeepLink(intent)
            } catch (_: Throwable) {}
        }
        WalletTheme {
            if (state.initialAuth == AuthState.Success || !enableSysAuth) {
                WalletApp(navController)
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
                        contentDescription = "splash",
                    )
                }
            }

            ResetWCPair()
            ShowWCError(state.wcError)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onActivityResumed()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onActivityPaused()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.handleIntent(intent)
    }

    @Composable
    fun ResetWCPair() {
        if (viewModel.resetWCPairing()) {
            makeText(LocalContext.current, stringResource(id = R.string.wallet_connect_connection_title), Toast.LENGTH_SHORT).show()
        }
    }

    @Composable
    private fun ShowWCError(wcError: String?) {
        if (!wcError.isNullOrEmpty()) {
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
                        modifier = Modifier.padding(start = paddingDefault, end = paddingDefault, top = paddingDefault),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = wcError,
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

    @Composable
    private fun OnWalletConnect() {
        val walletConnect by walletConnectViewModel.uiState.collectAsStateWithLifecycle()

        Box(
            modifier = Modifier.navigationBarsPadding(),
        ) {
            when (walletConnect) {
                is WalletConnectIntent.AuthRequest,
                is WalletConnectIntent.ConnectionState,
                WalletConnectIntent.Idle,
                WalletConnectIntent.Cancel,
                WalletConnectIntent.SessionDelete -> {}
                is WalletConnectIntent.SessionProposal -> ProposalScene(
                    proposal = (walletConnect as WalletConnectIntent.SessionProposal).sessionProposal,
                    verifyContext = (walletConnect as WalletConnectIntent.SessionProposal).verifyContext ?: return, // TODO: Show toast
                    onCancel = walletConnectViewModel::onCancel,
                )
                is WalletConnectIntent.SessionRequest -> RequestScene(
                    request = (walletConnect as WalletConnectIntent.SessionRequest).request,
                    verifyContext = (walletConnect as WalletConnectIntent.SessionRequest).verifyContext ?: return,
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
                onSuccessAuth = null
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

    override fun requestAuth(auth: AuthRequest, onSuccess: () -> Unit) {
        if (enabledSysAuth() && viewModel.isAuthRequired(auth)) {
            onSuccessAuth = onSuccess
            viewModel.requestAuth(auth)
        } else {
            onSuccess()
        }
    }
}

fun Context.getActivity(): MainActivity? = when (this) {
    is MainActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}