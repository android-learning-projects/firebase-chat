package com.example.chatapp.notifications

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.chatapp.R
import com.example.chatapp.ui.MessageActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.app.NotificationManager

import android.app.PendingIntent

import com.example.chatapp.ui.MainActivity
import android.app.NotificationChannel
import timber.log.Timber


class MyFirebaseMessaging : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val sented = remoteMessage.data["sented"]
        val user = remoteMessage.data["user"]

        val preferences = getSharedPreferences("PREFS", MODE_PRIVATE)
        val currentUser = preferences.getString("currentuser", "none")

        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser != null && sented.equals(firebaseUser.uid)) {
            if (!currentUser.equals(user)) {


                val intent = Intent(this, MessageActivity::class.java)
                intent.putExtra("userid", user)
                showNotification(this, remoteMessage, intent)
                /* showNotification(
                     this,
                     remoteMessage.data["title"],
                     remoteMessage.data["body"],
                     intent
                 )*/
            }
        }
    }


    /*@RequiresApi(Build.VERSION_CODES.O)
    private fun sendOreoNotification(remoteMessage: RemoteMessage) {

        val user = remoteMessage.data["user"]
        val icon = remoteMessage.data["icon"]
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]

        val notification: RemoteMessage.Notification? = remoteMessage.notification

//        val j = Integer.parseInt(user!!.replace("[\\D]", ""))

        val intent = Intent(this, MessageActivity::class.java)
        val bundle = Bundle()
        bundle.putString("userid", user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val oreoNotification = OreoNotification(this)

        if (title == null || body == null || icon == null) return
        val builder =
            oreoNotification.getOreoNotification(title, body, pendingIntent, defaultSound, icon)

        *//*   var i = 0
           if (j > 0) {
               i = j
           }*//*
        oreoNotification.getManager()?.notify(0, builder.build())
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        val user = remoteMessage.data["user"]
        val icon = remoteMessage.data["icon"]
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]

        val j = Integer.parseInt(user!!.replace("[\\D]", ""))

        val intent = Intent(this, MessageActivity::class.java)
        val bundle = Bundle()
        bundle.putString("userid", user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT)

        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(this)
            .setSmallIcon(Integer.parseInt(icon))
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSound)
            .setContentIntent(pendingIntent)

        val noti = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        var i = 0
        if (j > 0) {
            i = j
        }

        noti.notify(i, builder.build())
    }
*/
    fun showNotification(
        context: Context,
        title: String?,
        message: String?,
        intent: Intent?,
        id: Int = 101
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val contentViewSmall = RemoteViews(context.packageName, R.layout.layout_custom_notification)

        //Share intent
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
        shareIntent.putExtra(Intent.EXTRA_TITLE, "Share Link")
        shareIntent.putExtra(Intent.EXTRA_TEXT, message)
        val pendingPrevIntent =
            PendingIntent.getService(context, 0, shareIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        contentViewSmall.setTextViewText(R.id.textViewNotificationTitle, title)
        contentViewSmall.setTextViewText(R.id.textViewSub, message)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(
                Constants.DAILY_NOTIFICATION_CHANNEL_ID,
                Constants.DAILY_NOTIFICATION_CHANNEL_NAME,
                importance
            )
            notificationManager.createNotificationChannel(mChannel)
        }
        val mBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(context, Constants.DAILY_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_chat)
                .setContentTitle(title)
                .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)//
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Make it as popup
                .setContentText(title)
                .setAutoCancel(true)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentText(message)
                .setAutoCancel(true)
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addNextIntent(intent)
        val resultPendingIntent =
            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        mBuilder.setContentIntent(resultPendingIntent)
        val notification = mBuilder.build()
        notification.contentView = contentViewSmall
//        notificationManager.notify(Constants.DAILY_NOTIFICATION_ID, notification)
        notificationManager.notify(id, notification)
    }

    fun showNotification(context: Context, remoteMessage: RemoteMessage, intent: Intent?) {

        val user = remoteMessage.data["user"]
        val icon = remoteMessage.data["icon"]
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]

        Timber.d("notification-message: $remoteMessage")

        val notificationId = 100
        val chanelid = this.packageName
        intent?.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // you must create a notification channel for API 26 and Above
            val name = title
            val description = body
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(chanelid, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val mBuilder = NotificationCompat.Builder(this, chanelid)
            .setSmallIcon(R.drawable.ic_chat)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // cancel the notification when clicked
        /*.addAction(
            R.drawable.ic_check,
            "YES",
            pendingIntent
        ) *///add a btn to the Notification with a corresponding intent

        val notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId, mBuilder.build());
    }
}