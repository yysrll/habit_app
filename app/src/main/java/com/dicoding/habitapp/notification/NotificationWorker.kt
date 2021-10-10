package com.dicoding.habitapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dicoding.habitapp.R
import com.dicoding.habitapp.ui.detail.DetailHabitActivity
import com.dicoding.habitapp.utils.HABIT_ID
import com.dicoding.habitapp.utils.HABIT_TITLE
import com.dicoding.habitapp.utils.NOTIFICATION_CHANNEL_ID
import com.dicoding.habitapp.utils.NOTIF_UNIQUE_WORK

class NotificationWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    private val habitId = inputData.getInt(HABIT_ID, 0)
    private val habitTitle = inputData.getString(HABIT_TITLE)

    private fun getPendingIntent(id: Int): PendingIntent? {
        val intent = Intent(applicationContext, DetailHabitActivity::class.java).apply {
            putExtra(HABIT_ID, id)
        }
        return TaskStackBuilder.create(applicationContext).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    override fun doWork(): Result {
        val prefManager =
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val shouldNotify =
            prefManager.getBoolean(applicationContext.getString(R.string.pref_key_notify), false)

        //TODO 12 : If notification preference on, show notification with pending intent
        if (shouldNotify) {
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notification =
                NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID).apply {
                    setContentTitle(habitTitle)
                    setContentText(applicationContext.getString(R.string.notify_content))
                    setSmallIcon(R.drawable.ic_notifications)
                    setContentIntent(getPendingIntent(habitId))
                    setColor(ContextCompat.getColor(applicationContext, android.R.color.transparent))
                    setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                    setSound(alarmSound)


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val channel = NotificationChannel(
                            NOTIFICATION_CHANNEL_ID,
                            NOTIF_UNIQUE_WORK,
                            NotificationManager.IMPORTANCE_HIGH
                        )
                        channel.enableVibration(true)
                        channel.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
                        setChannelId(NOTIFICATION_CHANNEL_ID)
                        notificationManager.createNotificationChannel(channel)
                    }
                }.build()
            notificationManager.notify(habitId, notification)
        }

        return Result.success()
    }

}
