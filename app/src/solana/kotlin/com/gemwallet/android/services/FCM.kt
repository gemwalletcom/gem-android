package com.gemwallet.android.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.gemwallet.android.MainActivity
import com.gemwallet.android.cases.device.GetPushEnabled
import com.gemwallet.android.cases.device.SetPushToken
import com.gemwallet.android.cases.device.SyncDeviceInfo
import com.gemwallet.android.cases.pricealerts.EnablePriceAlertCase
import com.gemwallet.android.data.repositoreis.config.UserConfig
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.ui.R
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
    lateinit var userConfig: UserConfig
    @Inject
    lateinit var gemApiClient: GemApiClient
    @Inject
    lateinit var sessionRepository: SessionRepository
    @Inject
    lateinit var enablePriceAlertCase: EnablePriceAlertCase
    @Inject
    lateinit var syncDeviceInfoCase: SyncDeviceInfo
    @Inject
    lateinit var getPushEnabledCase: GetPushEnabled
    @Inject
    lateinit var setPushTokenCase: SetPushToken

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onMessageReceived(message: RemoteMessage) {
        if (!getPushEnabledCase.getPushEnabled()) {
            return
        }
        scope.launch {
            val title = message.notification?.title
            val subtitle = message.notification?.body
            val channelId = message.data["type"] ?: "default"
            val intent = Intent(applicationContext, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                0, intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE,
            )
            val builder = NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(R.drawable.ic_gem_notification)
                .setContentTitle(title)
                .setContentText(subtitle)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
            val notificationManager = NotificationManagerCompat.from(applicationContext)
            if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return@launch
            }
            val channel = NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
            notificationManager.notify(0, builder.build())
        }
    }

    override fun onNewToken(token: String) {
        scope.launch {
            setPushTokenCase.setPushToken(token)
            syncDeviceInfoCase.syncDeviceInfo()
        }

    }
}