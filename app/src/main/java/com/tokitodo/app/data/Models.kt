package com.tokitodo.app.data

import com.tokitodo.app.ui.theme.TodoArt

/** 一件要专注的事。时长是「一次完整专注」的长度。 */
data class Todo(
    val id: String,
    val title: String,
    val minutes: Int = 25,
    val art: TodoArt = TodoArt.Sky,
    val collectionId: String? = null,
    val note: String = "",
    val done: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastFocusedAt: Long? = null,
    val focusCount: Int = 0,
    val focusedMinutes: Int = 0,
)

/** 待办集：一组待办的集合，只保存 todoId，不复制待办。 */
data class TodoCollection(
    val id: String,
    val name: String,
    val tintIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)

/** 自定义锁机时间段；startMinute / endMinute 是当天的第几分钟。 */
data class LockSchedule(
    val id: String,
    val label: String,
    val startMinute: Int,
    val endMinute: Int,
    val days: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7),
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
)

enum class SessionKind { Focus, Break, Lock, Nap }

/** 一段已完成的专注 / 锁机记录。 */
data class FocusSession(
    val id: String,
    val endedAt: Long,
    val minutes: Int,
    val kind: SessionKind = SessionKind.Focus,
    val todoId: String? = null,
    val title: String = "",
)

data class Wish(
    val id: String,
    val text: String,
    val done: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

/** 未来倒计时事件；targetEpochDay 用 LocalDate.toEpochDay()。 */
data class CountdownEvent(
    val id: String,
    val title: String,
    val targetEpochDay: Long,
    val emoji: String = "🎯",
    val createdAt: Long = System.currentTimeMillis(),
)
