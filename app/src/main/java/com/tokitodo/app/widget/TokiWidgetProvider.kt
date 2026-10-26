package com.tokitodo.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.tokitodo.app.MainActivity
import com.tokitodo.app.R
import com.tokitodo.app.data.Stats
import com.tokitodo.app.data.TokiStore
import java.time.LocalDate

class TokiWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { id ->
            appWidgetManager.updateAppWidget(id, buildViews(context))
        }
    }

    companion object {
        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, TokiWidgetProvider::class.java),
            )
            ids.forEach { id -> manager.updateAppWidget(id, buildViews(context)) }
        }

        private fun buildViews(context: Context): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.toki_widget)
            val store = TokiStore(context)
            val settings = store.loadSettings()
            views.setInt(
                R.id.widget_root,
                "setBackgroundResource",
                when (settings.widgetStyle) {
                    com.tokitodo.app.data.WidgetStyle.Paper -> R.drawable.widget_background_paper
                    com.tokitodo.app.data.WidgetStyle.Sunrise -> R.drawable.widget_background_sunrise
                    else -> R.drawable.widget_background
                },
            )
            val todos = store.loadTodos()
            val sessions = store.loadSessions()
            val today = LocalDate.now()
            val todayMinutes = Stats.minutesOn(sessions, today)
            val streak = Stats.streak(sessions, today)
            val next = todos.firstOrNull { !it.done }?.title ?: "所有待办都完成了"
            views.setTextViewText(R.id.widget_title, next)
            views.setTextViewText(
                R.id.widget_stats,
                "今日 $todayMinutes 分钟 · 连续 $streak 天 · 待办 ${todos.count { !it.done }} 件",
            )
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pending = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            views.setOnClickPendingIntent(R.id.widget_root, pending)
            return views
        }
    }
}
