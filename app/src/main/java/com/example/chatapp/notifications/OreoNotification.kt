package com.example.chatapp.notifications

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi

class OreoNotification(base: Context?) : ContextWrapper(base) {

    companion object {
        val CHANNEL_ID = "com.example.chatapp"
        val CHANNEL_NAME = "chatapp"
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }
    }

    private var notificationManager: NotificationManager? = null

    @TargetApi(Build.VERSION_CODES.O)
    private fun createChannel() {

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )

        channel.enableLights(false)
        channel.enableVibration(true)
        channel.lockscreenVisibility

        getManager()?.createNotificationChannel(channel)
    }

     fun getManager(): NotificationManager? {
        if (notificationManager != null) {
            notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }

        return notificationManager
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getOreoNotification(
        title: String,
        body: String,
        pendingIntent: PendingIntent,
        soundUri: Uri,
        icon: String
    ): Notification.Builder {

        return Notification.Builder(applicationContext, CHANNEL_ID)
            .setContentIntent(pendingIntent)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(Integer.parseInt(icon))
            .setAutoCancel(true)
    }
}