package com.gemwallet.android.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.gemwallet.android.MainActivity
import com.gemwallet.android.cases.pushes.ShowSystemNotification
import com.gemwallet.android.model.PushNotificationData
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.navigation.routes.assetRouteUri
import com.gemwallet.android.ui.navigation.routes.referralRouteUriGem
import com.gemwallet.android.ui.navigation.routes.supportUri
import com.wallet.core.primitives.PushNotificationTypes
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject

class ShowSystemNotification @Inject constructor(@ApplicationContext val applicationContext: Context) :
    ShowSystemNotification {

    override fun showNotification(
        title: String?,
        subtitle: String?,
        channelId: String?
    ) {
        showNotification(
            title = title,
            subtitle = subtitle,
            channelId = channelId,
            data = Bundle.EMPTY,
            uri = if (channelId == PushNotificationTypes.Support.string) supportUri.toUri() else null
        )
    }

    override fun showNotification(
        title: String?,
        subtitle: String?,
        channelId: String?,
        data: PushNotificationData.Transaction
    ) {
        val extra = Bundle().apply {
            putInt("walletIndex", data.walletIndex)
        }
        showNotification(title, subtitle, channelId, extra, "${assetRouteUri}/${data.assetId}".toUri())
    }

    override fun showNotification(
        title: String?,
        subtitle: String?,
        channelId: String?,
        data: PushNotificationData.Asset,
    ) {
        showNotification(title, subtitle, channelId, Bundle.EMPTY, "${assetRouteUri}/${data.assetId}".toUri())
    }

    override fun showNotification(
        title: String?,
        subtitle: String?,
        channelId: String?,
        data: PushNotificationData.Reward,
    ) {
        showNotification(title, subtitle, channelId, Bundle.EMPTY, "${referralRouteUriGem}?code=".toUri())
    }

    private fun showNotification(
        title: String?,
        subtitle: String?,
        channelId: String?,
        data: Bundle,
        uri: Uri? = null
    ) {
        val channelId = channelId ?: "default"
        val title = title ?: "GemWallet"
        val intent = Intent(applicationContext, MainActivity::class.java)
            .putExtras(data)
            .setData(uri)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_MUTABLE,
        )
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_gem_notification)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val channel = NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)
        notificationManager.notify(0, builder.build())
    }
}