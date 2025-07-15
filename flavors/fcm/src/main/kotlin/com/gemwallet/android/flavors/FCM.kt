package com.gemwallet.android.flavors

import com.gemwallet.android.cases.device.GetPushEnabled
import com.gemwallet.android.cases.device.SetPushToken
import com.gemwallet.android.cases.device.SyncDeviceInfo
import com.gemwallet.android.cases.pushes.ShowSystemNotification
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject
import kotlin.let

@AndroidEntryPoint
class FCM : FirebaseMessagingService() {

    @Inject
    lateinit var syncDeviceInfo: SyncDeviceInfo
    @Inject
    lateinit var getPushEnabled: GetPushEnabled
    @Inject
    lateinit var setPushToken: SetPushToken
    @Inject
    lateinit var showSystemNotification: ShowSystemNotification

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onMessageReceived(message: RemoteMessage) {
        if (!getPushEnabled.getPushEnabled()) {
            return
        }
        scope.launch {
            val (assetId, walletIndex) = message.data["data"].let { rawData ->
                try {
                    JSONObject(rawData).let {
                        Pair(
                            it.getString("assetId"),
                            it.getString("walletIndex"),
                        )
                    }
                } catch (_: Throwable) {
                    Pair(null, null)
                }
            }
            val title = message.notification?.title
            val subtitle = message.notification?.body
            val channelId = message.data["type"]
            showSystemNotification.showNotification(title, subtitle, channelId, walletIndex, assetId)
        }
    }

    override fun onNewToken(token: String) {
        scope.launch {
            setPushToken.setPushToken(token)
            syncDeviceInfo.syncDeviceInfo()
        }

    }

    private data class MessageData (
        val walletIndex: Int?,
        val assetId: String?,
    )
}