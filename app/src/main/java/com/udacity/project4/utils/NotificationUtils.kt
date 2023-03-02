package com.udacity.project4.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.locationreminders.ReminderDescriptionActivity
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

private const val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel"

fun sendNotification(context: Context, reminderDataItem: ReminderDataItem) {
    val notificationManager = context
        .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val intent = ReminderDescriptionActivity.newIntent(context.applicationContext, reminderDataItem)

    //create a pending intent that opens ReminderDescriptionActivity when the user clicks on the notification
    val notificationId = getUniqueId()
    val stackBuilder = TaskStackBuilder.create(context)
        .addParentStack(ReminderDescriptionActivity::class.java)
        .addNextIntent(intent)
            .getPendingIntent(notificationId, PendingIntent.FLAG_UPDATE_CURRENT)


//    build the notification object with the data to be shown
    val channelId = context.resources.getString(R.string.reminder_notification_channel_id)
    val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(reminderDataItem.title)
        .setContentText(reminderDataItem.location)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setStyle(NotificationCompat.BigTextStyle().bigText(reminderDataItem.description))
        .setContentIntent(stackBuilder)
        .setChannelId(channelId)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(notificationId, notification)
}

private fun getUniqueId() = ((System.currentTimeMillis() % 10000).toInt())