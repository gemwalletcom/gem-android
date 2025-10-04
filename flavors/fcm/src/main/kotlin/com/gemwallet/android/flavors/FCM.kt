package com.gemwallet.android.flavors

import com.gemwallet.android.cases.device.GetPushEnabled
import com.gemwallet.android.cases.device.SetPushToken
import com.gemwallet.android.cases.device.SyncDeviceInfo
import com.gemwallet.android.cases.parseNotificationData
import com.gemwallet.android.cases.pushes.ShowSystemNotification
import com.gemwallet.android.model.PushNotificationData
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

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
            val rawType = message.data["type"]
            val rawData = message.data["data"]
            val data = parseNotificationData(rawType, rawData)
            val title = message.notification?.title
            val subtitle = message.notification?.body
            val channelId = message.data["type"]
            when (data) {
                is PushNotificationData.Transaction -> showSystemNotification.showNotification(title, subtitle, channelId, data)
                is PushNotificationData.Asset -> showSystemNotification.showNotification(title, subtitle, channelId, data)
                is PushNotificationData.PushNotificationPayloadType,
                is PushNotificationData.Swap,
                null -> showSystemNotification.showNotification(title, subtitle, channelId)
            }
        }
    }

    override fun onNewToken(token: String) {
        scope.launch {
            setPushToken.setPushToken(token)
            syncDeviceInfo.syncDeviceInfo()
        }
    }
}