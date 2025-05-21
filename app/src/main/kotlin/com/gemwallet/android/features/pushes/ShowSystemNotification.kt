package com.gemwallet.android.features.pushes

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.gemwallet.android.MainActivity
import com.gemwallet.android.cases.pushes.ShowSystemNotification
import com.gemwallet.android.ui.R
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject

class ShowSystemNotification @Inject constructor(@ApplicationContext val applicationContext: Context) : ShowSystemNotification {

    override fun showNotification(title: String?, subtitle: String?, channelId: String?) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE,
        )
        val builder = NotificationCompat.Builder(applicationContext, channelId ?: "default")
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