@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.gemwallet.features.settings.settings.presents.views

import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.TabsBar
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.features.settings.settings.viewmodels.SupportChatViewModel
import com.kevinnzou.web.AccompanistWebChromeClient
import com.kevinnzou.web.AccompanistWebViewClient
import com.kevinnzou.web.WebView
import com.kevinnzou.web.WebViewNavigator
import com.kevinnzou.web.WebViewState
import com.kevinnzou.web.rememberWebViewNavigator
import com.kevinnzou.web.rememberWebViewState
import com.kevinnzou.web.rememberWebViewStateWithHTMLData
import uniffi.gemstone.Config
import uniffi.gemstone.DocsUrl

@Composable
fun SupportChatScreen(
    onCancel: () -> Unit,
    viewModel: SupportChatViewModel = hiltViewModel(),
) {
    CookieManager.getInstance().setAcceptCookie(true)
    val html by viewModel.html.collectAsStateWithLifecycle()
    val isReady = remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(SupportType.Chat) }

    val helpCenterUrl = Config().getDocsUrl(DocsUrl.Start).toUri()
        .buildUpon()
        .appendQueryParameter("utm_source", "gemwallet_android")
        .build()
        .toString()

    val chatState = rememberWebViewStateWithHTMLData(data = html, baseUrl = viewModel.baseUrl)
    val helpCenterState = rememberWebViewState(helpCenterUrl)
    val isCancel = remember { mutableStateOf(false) }

    LaunchedEffect(isCancel.value) {
        if (isCancel.value) {
            onCancel()
        }
    }

    Scene(
        titleContent = {
            TabsBar(SupportType.entries, selectedType, { selectedType = it }) { item ->
                Text(
                    stringResource(
                        when (item) {
                            SupportType.Chat ->  R.string.settings_support
                            SupportType.HelpCenter -> R.string.settings_help_center
                        }
                    ),
                )
            }
        },
        onClose = onCancel,
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            GemWebView(
                state = chatState,
                navigator = rememberWebViewNavigator(),
                selectedType = selectedType,
                isReady = isReady,
                isCancel = isCancel,
            )
            if (selectedType == SupportType.HelpCenter) {
                GemWebView(
                    state = helpCenterState,
                    navigator = rememberWebViewNavigator(),
                    selectedType = selectedType,
                    isReady = isReady,
                    isCancel = isCancel,
                )
            }
            if (!isReady.value) {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
private fun GemWebView(
    state: WebViewState,
    navigator: WebViewNavigator = rememberWebViewNavigator(),
    selectedType: SupportType,
    isReady: MutableState<Boolean>,
    isCancel: MutableState<Boolean>,
) {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        WebView(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            state = state,
            navigator = navigator,
            onCreated = { webView ->
                webView.settings.javaScriptEnabled = true
                webView.addJavascriptInterface(
                    JSInterface({ isReady.value = true }, isCancel),
                    "Gem"
                )
            },
            captureBackPresses = false,
            client = object : AccompanistWebViewClient() {
                override fun onReceivedError(
                    view: WebView,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                }
            },
            chromeClient = object : AccompanistWebChromeClient() {
                override fun onProgressChanged(view: WebView, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)

                    if (selectedType == SupportType.HelpCenter) {
                        if (newProgress == 100) {
                            isReady.value = true
                        } else {
                            isReady.value = false
                        }
                    }
                }
            }
        )
    }
}

private class JSInterface(
    private val onReady: () -> Unit,
    private val isCancel: MutableState<Boolean>,
) {

    @JavascriptInterface
    fun closed() {
        isCancel.value = true
    }

    @JavascriptInterface
    fun ready() {
        onReady()
    }
}

enum class SupportType {
    Chat,
    HelpCenter,
}