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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.features.settings.settings.viewmodels.SupportCharViewModel
import com.kevinnzou.web.AccompanistWebViewClient
import com.kevinnzou.web.WebView
import com.kevinnzou.web.rememberWebViewNavigator
import com.kevinnzou.web.rememberWebViewStateWithHTMLData
import com.wallet.core.primitives.FiatQuoteType
import uniffi.gemstone.Config
import uniffi.gemstone.PublicUrl

@Composable
fun SupportChatScreen(
    onCancel: () -> Unit,
    viewModel: SupportCharViewModel = hiltViewModel(),
) {
    CookieManager.getInstance().setAcceptCookie(true);
    val html by viewModel.html.collectAsStateWithLifecycle()
    var isReady by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(SupportType.Chat) }
    val chatWebViewState = rememberWebViewStateWithHTMLData(data = html, baseUrl = viewModel.baseUrl)
    val helpCenterUrl = Config().getPublicUrl(PublicUrl.SUPPORT).toUri()
        .buildUpon()
        .appendQueryParameter("utm_source", "gemwallet_android")
        .build()
        .toString()
    val navigator = rememberWebViewNavigator()


    Scene(
        titleContent = {
            SingleChoiceSegmentedButtonRow {
                SupportType.entries.forEachIndexed { index, entry ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = FiatQuoteType.entries.size
                        ),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.primary,
                            activeContentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        icon = {},
                        onClick = {
                            selectedType = entry
                            when (selectedType) {
                                SupportType.Chat -> navigator.loadHtml(html, baseUrl = viewModel.baseUrl)
                                SupportType.HelpCenter -> navigator.loadUrl(helpCenterUrl)
                            }
                        },
                        selected = entry == selectedType,
                        label = {
                            Text(
                                stringResource(
                                    when (entry) {
                                        SupportType.Chat ->  R.string.settings_support
                                        SupportType.HelpCenter -> R.string.settings_help_center
                                    }
                                )
                            )
                        }
                    )
                }
            }
        }
    ) {
        if (!isReady && selectedType == SupportType.Chat) {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            WebView(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                state = chatWebViewState,
                navigator = navigator,
                onCreated = { webView ->
                    webView.settings.javaScriptEnabled = true
                    webView.addJavascriptInterface(
                        JSInterface({ isReady = true }, onCancel),
                        "Gem"
                    )
                },
                client = object : AccompanistWebViewClient() {
                    override fun onReceivedError(
                        view: WebView,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        super.onReceivedError(view, request, error)
                    }
                },
            )
        }
    }

}

private class JSInterface(
    private val onReady: () -> Unit,
    private val onCancel: () -> Unit,
) {

    @JavascriptInterface
    fun closed() {
        onCancel()
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