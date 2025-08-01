package com.gemwallet.android.flavors

import com.gemwallet.android.cases.device.GetPushEnabled
import com.gemwallet.android.cases.device.SetPushToken
import com.gemwallet.android.cases.device.SyncDeviceInfo
import com.gemwallet.android.cases.pushes.ShowSystemNotification
import com.gemwallet.android.serializer.jsonEncoder
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.wallet.core.primitives.PushNotificationAsset
import com.wallet.core.primitives.PushNotificationTransaction
import com.wallet.core.primitives.PushNotificationTypes
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
            val data = parseData(rawType, rawData)
            val title = message.notification?.title
            val subtitle = message.notification?.body
            val channelId = message.data["type"]
            when (data) {
                is PushNotificationAsset -> showSystemNotification.showNotification(title, subtitle, channelId, data)
                is PushNotificationTransaction -> showSystemNotification.showNotification(title, subtitle, channelId, data)
                else -> showSystemNotification.showNotification(title, subtitle, channelId)
            }
        }
    }

    override fun onNewToken(token: String) {
        scope.launch {
            setPushToken.setPushToken(token)
            syncDeviceInfo.syncDeviceInfo()
        }
    }

    companion object {
        internal fun parseData(rawType: String?, rawData: String?): Any? {
            if (rawType.isNullOrEmpty() || rawData.isNullOrEmpty()) {
                return null
            }
            val type = PushNotificationTypes.entries.firstOrNull { it.string == rawType } ?: return null
            return try {
                when (type) {
                    PushNotificationTypes.Transaction -> jsonEncoder.decodeFromString<PushNotificationTransaction>(rawData)
                    PushNotificationTypes.PriceAlert,
                    PushNotificationTypes.BuyAsset,
                    PushNotificationTypes.Asset -> jsonEncoder.decodeFromString<PushNotificationAsset>(rawData).assetId
                    PushNotificationTypes.Test,
                    PushNotificationTypes.SwapAsset -> null
                }
            } catch (_: Throwable) {
                null
            }
        }
    }
}