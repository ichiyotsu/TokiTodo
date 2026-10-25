package com.tokitodo.app.ui

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.CHINA)
private val dateFormatter = DateTimeFormatter.ofPattern("M月d日", Locale.CHINA)

fun formatClock(totalSeconds: Int): String {
    val safe = totalSeconds.coerceAtLeast(0)
    val hours = safe / 3600
    val minutes = (safe % 3600) / 60
    val seconds = safe % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

/** 25 分钟 / 1 小时 20 分钟。 */
fun formatDuration(minutes: Int): String {
    if (minutes <= 0) return "0 分钟"
    val hours = minutes / 60
    val rest = minutes % 60
    return when {
        hours == 0 -> "$rest 分钟"
        rest == 0 -> "$hours 小时"
        else -> "$hours 小时 $rest 分钟"
    }
}

fun formatShortDuration(minutes: Int): String {
    if (minutes <= 0) return "0"
    return if (minutes < 60) "$minutes" else "${minutes / 60}h${minutes % 60}".trimEnd('h')
}

fun formatDate(date: LocalDate): String = date.format(dateFormatter)

fun formatTime(epochMillis: Long): String = LocalDateTime.ofInstant(
    java.time.Instant.ofEpochMilli(epochMillis),
    java.time.ZoneId.systemDefault(),
).format(timeFormatter)

fun formatMinuteOfDay(minute: Int): String {
    val safe = ((minute % 1440) + 1440) % 1440
    return "%02d:%02d".format(safe / 60, safe % 60)
}

fun dayTitle(date: LocalDate, today: LocalDate = LocalDate.now()): String = when (date) {
    today -> "今天"
    today.minusDays(1) -> "昨天"
    else -> formatDate(date)
}

fun weekdayShort(isoDay: Int): String = when (isoDay) {
    1 -> "一"
    2 -> "二"
    3 -> "三"
    4 -> "四"
    5 -> "五"
    6 -> "六"
    else -> "日"
}

fun relativeDayLabel(epochDay: Long): String {
    val today = LocalDate.now()
    val target = LocalDate.ofEpochDay(epochDay)
    val delta = java.time.temporal.ChronoUnit.DAYS.between(today, target)
    return when {
        delta > 0 -> "还有 $delta 天"
        delta == 0L -> "就是今天"
        else -> "已过去 ${-delta} 天"
    }
}
