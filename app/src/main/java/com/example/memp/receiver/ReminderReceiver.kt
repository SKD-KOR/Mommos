package com.example.memp.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.memp.MainActivity
import com.example.memp.R

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "memo_reminder_channel"
        const val CHANNEL_NAME = "맘모스 알림"
        const val EXTRA_MEMO_ID = "extra_memo_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_CONTENT = "extra_content"
        const val EXTRA_MINUTES_BEFORE = "extra_minutes_before"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val memoId = intent.getLongExtra(EXTRA_MEMO_ID, 0L)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "메모 알림"
        val content = intent.getStringExtra(EXTRA_CONTENT) ?: "일정이 예정되어 있습니다."
        val minutesBefore = intent.getIntExtra(EXTRA_MINUTES_BEFORE, 0)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 알림 채널 생성 (Android 8.0 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "중요 메모 및 일정 사전 알림 채널입니다."
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 터치 시 앱 실행 인텐트
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            memoId.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 알림 제목 분기
        val notificationTitle = when {
            minutesBefore == 60 -> "[1시간 전 알림] $title"
            minutesBefore > 0 -> "[${minutesBefore}분 전 알림] $title"
            else -> "[일정 알림] $title"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(notificationTitle)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        val notificationId = (memoId.toString() + minutesBefore.toString()).hashCode()
        notificationManager.notify(notificationId, notification)
    }
}