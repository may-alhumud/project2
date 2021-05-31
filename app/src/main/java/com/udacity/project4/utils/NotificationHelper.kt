package com.udacity.project4.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_HIGH
import androidx.core.app.NotificationManagerCompat
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.locationreminders.ReminderDescriptionActivity
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem


class NotificationHelper(base: Context?) : ContextWrapper(base) {
    private var notificationManager: NotificationManagerCompat? = null

    init {
        createNotificationChannels()
        notificationManager = NotificationManagerCompat.from(this)

    }

    companion object {
        const val CHANNEL_MESSAGE = "Location Reminder"
        private const val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel"
         const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

    }


    fun createNotification(reminderDataItem: ReminderDataItem) {

        val notification: Notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(reminderDataItem.title)
            .setContentText(reminderDataItem.location)
            .setPriority(PRIORITY_HIGH)
            .setContentIntent(pendingIntent(reminderDataItem))
            .setAutoCancel(true)

            .build()
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notificationManager?.notify(getUniqueId(), notification)
    }

    private fun pendingIntent( reminderDataItem: ReminderDataItem): PendingIntent {

        val activityIntent = Intent(this, ReminderDescriptionActivity::class.java)
        activityIntent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)

        return PendingIntent.getActivity(
            this,
            0, activityIntent, 0
        )

    }


    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                CHANNEL_MESSAGE,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.name = CHANNEL_MESSAGE
            channel.description = "Reminder channel"

            val manager = getSystemService(
                NotificationManager::class.java
            )

            manager?.createNotificationChannel(channel)
        }
    }

    private fun getUniqueId() = ((System.currentTimeMillis() % 10000).toInt())


}