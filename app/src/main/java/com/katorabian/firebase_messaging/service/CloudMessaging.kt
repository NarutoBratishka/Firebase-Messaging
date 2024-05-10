package com.katorabian.firebase_messaging.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.katorabian.firebase_messaging.R

class CloudMessaging: FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("MyLog", "From: ${remoteMessage.from}")

        val data: Map<String, String> = remoteMessage.data
        val title = data["title"]
        val text = data["text"]

        if (title != null || text != null) {
            sendNotification(title, text)
        } else {
            sendNotification(remoteMessage.notification?.title, remoteMessage.notification?.body)
        }
    }

    private fun sendNotification(title: String?, text: String?) {

        val channelId = getString(com.google.firebase.messaging.R.string.fcm_fallback_notification_channel_label)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setStyle(
                NotificationCompat.BigTextStyle()
                .bigText(title.toString()))
            .setSmallIcon(R.drawable.ic_stat_ic_notification)
            .setColor(resources.getColor(R.color.fcm_accent))
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    resources,
                    R.mipmap.ic_launcher
                )
            )
            .setContentTitle(title.toString())
            .setContentText(text.toString())
            .setAutoCancel(true)
            .setSound(defaultSoundUri)

        notificationBuilder.priority = NotificationCompat.PRIORITY_MAX

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Напоминания сервиса",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(2 /* ID of notification */, notificationBuilder.build())
    }

}