package com.gemwallet.features.settings.settings.viewmodels

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositoreis.device.DeviceRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.wallet.core.primitives.Currency
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SupportCharViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionRepository: SessionRepository,
    private val deviceRepository: DeviceRepository,
): ViewModel() {

    private val supportId = deviceRepository.getSupportId()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val html = combine(
        sessionRepository.session().filterNotNull(),
        supportId.filterNotNull(),
    ) { session, supportId ->
        htmlContent(supportId, session.currency)
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val baseUrl = "https://support.gemwallet.com"

    private val websiteToken = "p8uDBqT21unfbTHDQzSCCTBi"


    private val sdkSourceURL = "$baseUrl/packs/js/sdk.js"

    private val sdkInitializationScript = """
        window.chatwootSDK.run({
          websiteToken: '$websiteToken',
          baseUrl: '$baseUrl'
        });
        """

    private fun chatwootSettingsScript(): String {
        return """
            window.chatwootSettings = {
              hideMessageBubble: true,
              locale: 'current',
              darkMode: 'auto',
              enableEmojiPicker: false,
              enableEndConversation: false)
            };
        """
    }

    private val toggleChatScript = "window.\$chatwoot.toggle(open);"

    private fun getDeviceIdScript(supportDeviceId: String, currency: Currency,): String {
        val os = Build.VERSION.RELEASE
        val device = "${Build.MANUFACTURER} ${Build.MODEL}"
        val appVersion = try {
            context.packageManager.getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (_: Throwable) {
            "Unknown"
        }

        return """
            window.addEventListener('chatwoot:ready', function () {
              window.${'$'}chatwoot.setCustomAttributes({
                supportDeviceId: '$supportDeviceId',
                platform: 'android',
                os: '$os',
                device: '$device',
                currency: '${currency.string}',
                app_version: '$appVersion'
              });
            });
        """
    }

    val chatOpenEventHandler = chatEventHandler(ChatwootEvent.Ready)
        
    val chatCloseEventHandler = chatEventHandler(ChatwootEvent.Closed)

    private fun htmlContent(deviceSupportId: String, currency: Currency): String =
        """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <script>
            ${chatwootSettingsScript()}
          </script>
        </head>
        <body>
          <script src="$sdkSourceURL" async onload="
            $sdkInitializationScript
            $toggleChatScript
            ${getDeviceIdScript(deviceSupportId, currency)}
            $chatOpenEventHandler
            $chatCloseEventHandler
          "></script>
        </body>
        </html>
    """

    private fun chatEventHandler(event: ChatwootEvent): String {
        return """
        window.addEventListener('${event.event}', function(event) {
            Gem.${event.handler}();
        });
        """
    }
}

enum class ChatwootEvent(val event: String, val handler: String) {
    Ready("chatwoot:ready", "ready"),
    Closed("chatwoot:closed", "closed"),
}