package com.tokitodo.app.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.tokitodo.app.reminder.ReminderReceiver
import java.time.LocalDateTime
import java.time.ZoneId

object ReminderScheduler {
    fun schedule(context: Context, todo: Todo) {
        val minute = todo.reminderMinute
        if (minute == null || todo.done) {
            cancel(context, todo.id)
            return
        }
        val now = LocalDateTime.now()
        var target = now.toLocalDate().atTime(minute / 60, minute % 60)
        if (!target.isAfter(now)) target = target.plusDays(1)
        val triggerAt = target.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val pending = pending(context, todo)
        val manager = context.getSystemService(AlarmManager::class.java) ?: return
        runCatching {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        }.onFailure {
            manager.set(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        }
    }

    fun cancel(context: Context, todoId: String) {
        val intent = Intent(context, ReminderReceiver::class.java)
            .putExtra("todoId", todoId)
        val pending = PendingIntent.getBroadcast(
            context,
            todoId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        context.getSystemService(AlarmManager::class.java)?.cancel(pending)
    }

    private fun pending(context: Context, todo: Todo): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("todoId", todo.id)
            putExtra("title", todo.title)
        }
        return PendingIntent.getBroadcast(
            context,
            todo.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
