package com.example.memp.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.memp.receiver.ReminderReceiver

object ReminderScheduler {

    fun scheduleStepwiseReminders(
        context: Context,
        memoId: Long,
        title: String,
        contentText: String,
        targetTimeMillis: Long
    ) {
        val now = System.currentTimeMillis()
        val diffMinutes = (targetTimeMillis - now) / (1000 * 60)

        if (diffMinutes < 0) return

        val offsetsToSchedule = mutableListOf<Int>()

        when {
            diffMinutes > 60 -> {
                offsetsToSchedule.add(60) // 1시간 전
                offsetsToSchedule.add(10) // 10분 전
                offsetsToSchedule.add(0)  // 정시
            }
            diffMinutes in 31..60 -> {
                offsetsToSchedule.add(30) // 30분 전
                offsetsToSchedule.add(10) // 10분 전
                offsetsToSchedule.add(0)  // 정시
            }
            diffMinutes in 11..30 -> {
                offsetsToSchedule.add(10) // 10분 전
                offsetsToSchedule.add(0)  // 정시
            }
            diffMinutes in 6..10 -> {
                offsetsToSchedule.add(5)  // 5분 전
                offsetsToSchedule.add(0)  // 정시
            }
            diffMinutes in 2..5 -> {
                offsetsToSchedule.add(1)  // 1분 전
                offsetsToSchedule.add(0)  // 정시
            }
            else -> {
                offsetsToSchedule.add(0)  // 정시
            }
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        for (minutesBefore in offsetsToSchedule) {
            val alarmTimeMillis = targetTimeMillis - (minutesBefore * 60 * 1000L)

            if (alarmTimeMillis > System.currentTimeMillis() + 1000L) {
                val intent = Intent(context, ReminderReceiver::class.java).apply {
                    putExtra(ReminderReceiver.EXTRA_MEMO_ID, memoId)
                    putExtra(ReminderReceiver.EXTRA_TITLE, title)
                    putExtra(ReminderReceiver.EXTRA_CONTENT, contentText)
                    putExtra(ReminderReceiver.EXTRA_MINUTES_BEFORE, minutesBefore)
                }

                val requestCode = (memoId.toString() + minutesBefore.toString()).hashCode()
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                alarmTimeMillis,
                                pendingIntent
                            )
                        } else {
                            alarmManager.setAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                alarmTimeMillis,
                                pendingIntent
                            )
                        }
                    } else {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            alarmTimeMillis,
                            pendingIntent
                        )
                    }
                } catch (_: SecurityException) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmTimeMillis,
                        pendingIntent
                    )
                }
            }
        }
    }
}