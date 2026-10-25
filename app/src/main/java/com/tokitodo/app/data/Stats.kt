package com.tokitodo.app.data

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/** 纯函数统计工具，页面只负责把数字画出来。 */
object Stats {

    fun counted(sessions: List<FocusSession>): List<FocusSession> =
        sessions.filter { it.kind == SessionKind.Focus || it.kind == SessionKind.Lock }

    fun totalMinutes(sessions: List<FocusSession>): Int =
        counted(sessions).sumOf { it.minutes }

    fun totalCount(sessions: List<FocusSession>): Int = counted(sessions).size

    fun dateOf(epochMillis: Long): LocalDate = Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    fun hourOf(epochMillis: Long): Int = Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .hour

    fun minutesOn(sessions: List<FocusSession>, date: LocalDate): Int =
        counted(sessions).filter { dateOf(it.endedAt) == date }.sumOf { it.minutes }

    fun countOn(sessions: List<FocusSession>, date: LocalDate): Int =
        counted(sessions).count { dateOf(it.endedAt) == date }

    /** 连续专注天数：今天有记录从今天往前数，否则从昨天往前数。 */
    fun streak(sessions: List<FocusSession>, today: LocalDate): Int {
        val days = counted(sessions)
            .filter { it.minutes > 0 }
            .map { dateOf(it.endedAt) }
            .toSet()
        if (days.isEmpty()) return 0
        var cursor = if (days.contains(today)) today else today.minusDays(1)
        var streak = 0
        while (days.contains(cursor)) {
            streak += 1
            cursor = cursor.minusDays(1)
        }
        return streak
    }

    fun activeDays(sessions: List<FocusSession>): Int =
        counted(sessions).filter { it.minutes > 0 }.map { dateOf(it.endedAt) }.toSet().size

    /** 一天内按小时分布（0..23）。 */
    fun hourly(sessions: List<FocusSession>, date: LocalDate): IntArray {
        val buckets = IntArray(24)
        counted(sessions)
            .filter { dateOf(it.endedAt) == date }
            .forEach { buckets[hourOf(it.endedAt)] += it.minutes }
        return buckets
    }

    /** 一周的每日分钟数，周一开头。 */
    fun week(sessions: List<FocusSession>, anyDayInWeek: LocalDate): List<Pair<LocalDate, Int>> {
        val monday = anyDayInWeek.minusDays((anyDayInWeek.dayOfWeek.value - 1).toLong())
        return (0 until 7).map { offset ->
            val day = monday.plusDays(offset.toLong())
            day to minutesOn(sessions, day)
        }
    }

    /** 一个月的每日分钟数。 */
    fun month(sessions: List<FocusSession>, month: YearMonth): List<Pair<LocalDate, Int>> =
        (1..month.lengthOfMonth()).map { day ->
            val date = month.atDay(day)
            date to minutesOn(sessions, date)
        }

    /** 任意日期区间的每分钟数。 */
    fun range(
        sessions: List<FocusSession>,
        start: LocalDate,
        end: LocalDate,
    ): List<Pair<LocalDate, Int>> {
        if (end.isBefore(start)) return emptyList()
        val days = ChronoUnit.DAYS.between(start, end).toInt()
        return (0..days).map { offset ->
            val day = start.plusDays(offset.toLong())
            day to minutesOn(sessions, day)
        }
    }

    /** 四个时段的分钟数：凌晨 0-6 / 上午 6-12 / 下午 12-18 / 夜晚 18-24。 */
    fun timeOfDay(sessions: List<FocusSession>): IntArray {
        val buckets = IntArray(4)
        counted(sessions).forEach { session ->
            val hour = hourOf(session.endedAt)
            buckets[(hour / 6).coerceIn(0, 3)] += session.minutes
        }
        return buckets
    }

    fun longestSession(sessions: List<FocusSession>): Int =
        counted(sessions).maxOfOrNull { it.minutes } ?: 0

    /** 平均每日分钟数：从第一次专注到今天的自然日跨度。 */
    fun dailyAverage(sessions: List<FocusSession>, today: LocalDate): Int {
        val counted = counted(sessions).filter { it.minutes > 0 }
        if (counted.isEmpty()) return 0
        val first = counted.minOf { dateOf(it.endedAt) }
        val span = (ChronoUnit.DAYS.between(first, today) + 1).coerceAtLeast(1).toInt()
        return counted.sumOf { it.minutes } / span
    }

    fun monthTotal(sessions: List<FocusSession>, month: YearMonth): Int {
        val start = month.atDay(1)
        val end = month.atEndOfMonth()
        return counted(sessions)
            .filter {
                val date = dateOf(it.endedAt)
                !date.isBefore(start) && !date.isAfter(end)
            }
            .sumOf { it.minutes }
    }

    fun startOfToday(): LocalDateTime = LocalDate.now().atStartOfDay()

    /** 判断某个时间点是否落在锁机时段内（支持跨零点）。 */
    fun inSchedule(schedule: LockSchedule, now: LocalDateTime = LocalDateTime.now()): Boolean {
        if (!schedule.enabled) return false
        val day = now.dayOfWeek.value
        val minute = now.hour * 60 + now.minute
        val start = schedule.startMinute
        val end = schedule.endMinute
        return if (start <= end) {
            schedule.days.contains(day) && minute in start until end
        } else {
            // 跨零点：今天的时间段属于「昨天」开启的那一次，或今天开启的一次。
            (schedule.days.contains(day) && minute >= start) ||
                (schedule.days.contains((day + 5) % 7 + 1) && minute < end)
        }
    }
}
