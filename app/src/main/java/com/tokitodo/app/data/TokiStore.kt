package com.tokitodo.app.data

import android.content.Context
import com.tokitodo.app.ui.theme.ColorPreset
import com.tokitodo.app.ui.theme.TodoArt
import org.json.JSONArray
import org.json.JSONObject

/**
 * 本地存储：SharedPreferences + JSON。所有数据只留在这台设备上，
 * 不申请网络权限，也没有账号体系。
 */
class TokiStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /* ----------------------------- todos ----------------------------- */

    fun loadTodos(): List<Todo> = readArray(KEY_TODOS) { json ->
        Todo(
            id = json.getString("id"),
            title = json.optString("title", ""),
            minutes = json.optInt("minutes", 25).coerceIn(1, 1440),
            art = enumOrDefault(json.optString("art"), TodoArt.Sky),
            collectionId = json.optString("collectionId").takeUnless { it.isBlank() },
            note = json.optString("note", ""),
            done = json.optBoolean("done", false),
            createdAt = json.optLong("createdAt", System.currentTimeMillis()),
            lastFocusedAt = if (json.has("lastFocusedAt")) json.optLong("lastFocusedAt") else null,
            focusCount = json.optInt("focusCount", 0),
            focusedMinutes = json.optInt("focusedMinutes", 0),
            timerMode = enumOrDefault(json.optString("timerMode"), TodoTimerMode.CountDown),
            type = enumOrDefault(json.optString("type"), TodoType.Pomodoro),
            goalDateEpochDay = if (json.has("goalDateEpochDay")) {
                json.optLong("goalDateEpochDay")
            } else {
                null
            },
            goalHours = json.optDouble("goalHours", 0.0).toFloat(),
            habitPeriod = enumOrDefault(json.optString("habitPeriod"), HabitPeriod.Daily),
            habitHours = json.optDouble("habitHours", 0.0).toFloat(),
            coverPath = json.optString("coverPath").takeUnless { it.isBlank() },
        )
    }

    fun saveTodos(todos: List<Todo>) = writeArray(KEY_TODOS) { json ->
        todos.forEach {
            json.put(
                JSONObject().apply {
                    put("id", it.id)
                    put("title", it.title)
                    put("minutes", it.minutes)
                    put("art", it.art.name)
                    put("collectionId", it.collectionId ?: "")
                    put("note", it.note)
                    put("done", it.done)
                    put("createdAt", it.createdAt)
                    it.lastFocusedAt?.let { value -> put("lastFocusedAt", value) }
                    put("focusCount", it.focusCount)
                    put("focusedMinutes", it.focusedMinutes)
                    put("timerMode", it.timerMode.name)
                    put("type", it.type.name)
                    it.goalDateEpochDay?.let { value -> put("goalDateEpochDay", value) }
                    put("goalHours", it.goalHours.toDouble())
                    put("habitPeriod", it.habitPeriod.name)
                    put("habitHours", it.habitHours.toDouble())
                    put("coverPath", it.coverPath ?: "")
                },
            )
        }
    }

    /* ----------------------------- collections ----------------------------- */

    fun loadCollections(): List<TodoCollection> = readArray(KEY_COLLECTIONS) { json ->
        TodoCollection(
            id = json.getString("id"),
            name = json.optString("name", "待办集"),
            tintIndex = json.optInt("tintIndex", 0),
            createdAt = json.optLong("createdAt", System.currentTimeMillis()),
        )
    }

    fun saveCollections(collections: List<TodoCollection>) = writeArray(KEY_COLLECTIONS) { json ->
        collections.forEach {
            json.put(
                JSONObject().apply {
                    put("id", it.id)
                    put("name", it.name)
                    put("tintIndex", it.tintIndex)
                    put("createdAt", it.createdAt)
                },
            )
        }
    }

    /* ----------------------------- lock schedules ----------------------------- */

    fun loadSchedules(): List<LockSchedule> = readArray(KEY_SCHEDULES) { json ->
        LockSchedule(
            id = json.getString("id"),
            label = json.optString("label", "锁机时段"),
            startMinute = json.optInt("startMinute", 23 * 60),
            endMinute = json.optInt("endMinute", 7 * 60),
            days = buildSet {
                val array = json.optJSONArray("days") ?: JSONArray()
                for (i in 0 until array.length()) add(array.getInt(i))
            },
            enabled = json.optBoolean("enabled", true),
            createdAt = json.optLong("createdAt", System.currentTimeMillis()),
        )
    }

    fun saveSchedules(schedules: List<LockSchedule>) = writeArray(KEY_SCHEDULES) { json ->
        schedules.forEach {
            json.put(
                JSONObject().apply {
                    put("id", it.id)
                    put("label", it.label)
                    put("startMinute", it.startMinute)
                    put("endMinute", it.endMinute)
                    put("days", JSONArray(it.days.sorted()))
                    put("enabled", it.enabled)
                    put("createdAt", it.createdAt)
                },
            )
        }
    }

    /* ----------------------------- sessions ----------------------------- */

    fun loadSessions(): List<FocusSession> = readArray(KEY_SESSIONS) { json ->
        val endedAt = json.optLong("endedAt", 0L)
        val minutes = json.optInt("minutes", 0)
        val kind = enumOrDefault(json.optString("kind"), SessionKind.Focus)
        FocusSession(
            id = json.optString("id", ""),
            endedAt = endedAt,
            minutes = minutes,
            kind = kind,
            todoId = json.optString("todoId").takeUnless { it.isBlank() },
            title = json.optString("title", ""),
            startedAt = json.optLong("startedAt", endedAt - minutes * 60_000L),
            plannedMinutes = json.optInt("plannedMinutes", minutes),
            completed = json.optBoolean("completed", kind != SessionKind.Break),
            note = json.optString("note", ""),
        )
    }

    fun saveSessions(sessions: List<FocusSession>) = writeArray(KEY_SESSIONS) { json ->
        sessions.forEach {
            json.put(
                JSONObject().apply {
                    put("id", it.id)
                    put("endedAt", it.endedAt)
                    put("startedAt", it.startedAt)
                    put("minutes", it.minutes)
                    put("plannedMinutes", it.plannedMinutes)
                    put("completed", it.completed)
                    put("kind", it.kind.name)
                    put("todoId", it.todoId ?: "")
                    put("title", it.title)
                    put("note", it.note)
                },
            )
        }
    }

    /* ----------------------------- wishes ----------------------------- */

    fun loadWishes(): List<Wish> = readArray(KEY_WISHES) { json ->
        Wish(
            id = json.getString("id"),
            text = json.optString("text", ""),
            done = json.optBoolean("done", false),
            createdAt = json.optLong("createdAt", System.currentTimeMillis()),
        )
    }

    fun saveWishes(wishes: List<Wish>) = writeArray(KEY_WISHES) { json ->
        wishes.forEach {
            json.put(
                JSONObject().apply {
                    put("id", it.id)
                    put("text", it.text)
                    put("done", it.done)
                    put("createdAt", it.createdAt)
                },
            )
        }
    }

    /* ----------------------------- countdowns ----------------------------- */

    fun loadCountdowns(): List<CountdownEvent> = readArray(KEY_COUNTDOWNS) { json ->
        CountdownEvent(
            id = json.getString("id"),
            title = json.optString("title", ""),
            targetEpochDay = json.optLong("targetEpochDay", 0L),
            emoji = json.optString("emoji", "🎯"),
            createdAt = json.optLong("createdAt", System.currentTimeMillis()),
        )
    }

    fun saveCountdowns(events: List<CountdownEvent>) = writeArray(KEY_COUNTDOWNS) { json ->
        events.forEach {
            json.put(
                JSONObject().apply {
                    put("id", it.id)
                    put("title", it.title)
                    put("targetEpochDay", it.targetEpochDay)
                    put("emoji", it.emoji)
                    put("createdAt", it.createdAt)
                },
            )
        }
    }

    /* ----------------------------- settings ----------------------------- */

    fun loadSettings(): AppSettings {
        val raw = prefs.getString(KEY_SETTINGS, null) ?: return AppSettings()
        return runCatching {
            val json = JSONObject(raw)
            AppSettings(
                themeMode = enumOrDefault(json.optString("themeMode"), ThemeMode.System),
                dynamicColor = json.optBoolean("dynamicColor", false),
                amoled = json.optBoolean("amoled", false),
                colorPreset = enumOrDefault(json.optString("colorPreset"), ColorPreset.Pine),
                reduceMotion = json.optBoolean("reduceMotion", false),
                focusMinutes = json.optInt("focusMinutes", 25).coerceIn(1, 180),
                shortBreakMinutes = json.optInt("shortBreakMinutes", 5).coerceIn(1, 60),
                longBreakMinutes = json.optInt("longBreakMinutes", 15).coerceIn(1, 90),
                sessionsBeforeLongBreak = json.optInt("sessionsBeforeLongBreak", 4).coerceIn(1, 12),
                autoStartNext = json.optBoolean("autoStartNext", true),
                soundEnabled = json.optBoolean("soundEnabled", true),
                vibrationEnabled = json.optBoolean("vibrationEnabled", true),
                studyMode = json.optBoolean("studyMode", false),
                strictMode = json.optBoolean("strictMode", false),
                blockNotifications = json.optBoolean("blockNotifications", false),
                blockRecents = json.optBoolean("blockRecents", false),
                hideInRecents = json.optBoolean("hideInRecents", false),
                lockBlockHome = json.optBoolean("lockBlockHome", false),
                whitelistPackages = buildSet {
                    val array = json.optJSONArray("whitelistPackages") ?: JSONArray()
                    for (index in 0 until array.length()) add(array.optString(index))
                },
                cycleEnabled = json.optBoolean("cycleEnabled", false),
                cycleStudyMinutes = json.optInt("cycleStudyMinutes", 25).coerceIn(1, 180),
                cycleBreakMinutes = json.optInt("cycleBreakMinutes", 5).coerceIn(1, 60),
                cycleCount = json.optInt("cycleCount", 4).coerceIn(1, 24),
                cycleInfinite = json.optBoolean("cycleInfinite", true),
                keepScreenAwake = json.optBoolean("keepScreenAwake", true),
                durationPresets = buildList {
                    val array = json.optJSONArray("durationPresets")
                    if (array == null) {
                        addAll(listOf(5, 10, 15, 25, 45, 60, 90))
                    } else {
                        for (index in 0 until array.length()) {
                            add(array.optInt(index, 25).coerceIn(1, 1440))
                        }
                    }
                },
                lockBlockNotifications = json.optBoolean("lockBlockNotifications", true),
                lockAllowEmergency = json.optBoolean("lockAllowEmergency", true),
                lockShowCountdown = json.optBoolean("lockShowCountdown", true),
                navLabels = json.optBoolean("navLabels", true),
                defaultTab = json.optString("defaultTab", "Todos"),
                widgetStyle = enumOrDefault(json.optString("widgetStyle"), WidgetStyle.Forest),
                desktopAssistant = json.optBoolean("desktopAssistant", false),
                dailyGoalMinutes = json.optInt("dailyGoalMinutes", 120).coerceIn(15, 720),
                userName = json.optString("userName", "番茄同学"),
                userEmoji = json.optString("userEmoji", "🍅"),
            )
        }.getOrDefault(AppSettings())
    }

    fun saveSettings(settings: AppSettings) {
        val json = JSONObject().apply {
            put("themeMode", settings.themeMode.name)
            put("dynamicColor", settings.dynamicColor)
            put("amoled", settings.amoled)
            put("colorPreset", settings.colorPreset.name)
            put("reduceMotion", settings.reduceMotion)
            put("focusMinutes", settings.focusMinutes)
            put("shortBreakMinutes", settings.shortBreakMinutes)
            put("longBreakMinutes", settings.longBreakMinutes)
            put("sessionsBeforeLongBreak", settings.sessionsBeforeLongBreak)
            put("autoStartNext", settings.autoStartNext)
            put("soundEnabled", settings.soundEnabled)
            put("vibrationEnabled", settings.vibrationEnabled)
            put("studyMode", settings.studyMode)
            put("strictMode", settings.strictMode)
            put("blockNotifications", settings.blockNotifications)
            put("blockRecents", settings.blockRecents)
            put("hideInRecents", settings.hideInRecents)
            put("lockBlockHome", settings.lockBlockHome)
            put("whitelistPackages", JSONArray(settings.whitelistPackages.toList()))
            put("cycleEnabled", settings.cycleEnabled)
            put("cycleStudyMinutes", settings.cycleStudyMinutes)
            put("cycleBreakMinutes", settings.cycleBreakMinutes)
            put("cycleCount", settings.cycleCount)
            put("cycleInfinite", settings.cycleInfinite)
            put("keepScreenAwake", settings.keepScreenAwake)
            put("durationPresets", JSONArray(settings.durationPresets))
            put("lockBlockNotifications", settings.lockBlockNotifications)
            put("lockAllowEmergency", settings.lockAllowEmergency)
            put("lockShowCountdown", settings.lockShowCountdown)
            put("navLabels", settings.navLabels)
            put("defaultTab", settings.defaultTab)
            put("widgetStyle", settings.widgetStyle.name)
            put("desktopAssistant", settings.desktopAssistant)
            put("dailyGoalMinutes", settings.dailyGoalMinutes)
            put("userName", settings.userName)
            put("userEmoji", settings.userEmoji)
        }
        prefs.edit().putString(KEY_SETTINGS, json.toString()).apply()
    }

    /* ----------------------------- seed / backup ----------------------------- */

    fun isSeeded(): Boolean = prefs.getBoolean(KEY_SEEDED, false)

    fun markSeeded() = prefs.edit().putBoolean(KEY_SEEDED, true).apply()

    fun clearAll() = prefs.edit().clear().apply()

    fun exportJson(
        todos: List<Todo>,
        collections: List<TodoCollection>,
        schedules: List<LockSchedule>,
        sessions: List<FocusSession>,
        wishes: List<Wish>,
        countdowns: List<CountdownEvent>,
        settings: AppSettings,
    ): String {
        val root = JSONObject()
        root.put("app", "TokiToDo")
        root.put("version", 2)
        root.put("exportedAt", System.currentTimeMillis())
        root.put(
            "todos",
            JSONArray().apply {
                todos.forEach { todo ->
                    put(
                        JSONObject().apply {
                            put("id", todo.id)
                            put("title", todo.title)
                            put("minutes", todo.minutes)
                            put("art", todo.art.name)
                            put("collectionId", todo.collectionId ?: "")
                            put("note", todo.note)
                            put("done", todo.done)
                            put("createdAt", todo.createdAt)
                            todo.lastFocusedAt?.let { put("lastFocusedAt", it) }
                            put("focusCount", todo.focusCount)
                            put("focusedMinutes", todo.focusedMinutes)
                            put("timerMode", todo.timerMode.name)
                            put("type", todo.type.name)
                            todo.goalDateEpochDay?.let { put("goalDateEpochDay", it) }
                            put("goalHours", todo.goalHours.toDouble())
                            put("habitPeriod", todo.habitPeriod.name)
                            put("habitHours", todo.habitHours.toDouble())
                            put("coverPath", todo.coverPath ?: "")
                        },
                    )
                }
            },
        )
        root.put(
            "collections",
            JSONArray().apply {
                collections.forEach { collection ->
                    put(
                        JSONObject().apply {
                            put("id", collection.id)
                            put("name", collection.name)
                            put("tintIndex", collection.tintIndex)
                            put("createdAt", collection.createdAt)
                        },
                    )
                }
            },
        )
        root.put(
            "schedules",
            JSONArray().apply {
                schedules.forEach { schedule ->
                    put(
                        JSONObject().apply {
                            put("id", schedule.id)
                            put("label", schedule.label)
                            put("startMinute", schedule.startMinute)
                            put("endMinute", schedule.endMinute)
                            put("days", JSONArray(schedule.days.sorted()))
                            put("enabled", schedule.enabled)
                            put("createdAt", schedule.createdAt)
                        },
                    )
                }
            },
        )
        root.put(
            "sessions",
            JSONArray().apply {
                sessions.forEach { session ->
                    put(
                        JSONObject().apply {
                            put("id", session.id)
                            put("endedAt", session.endedAt)
                            put("startedAt", session.startedAt)
                            put("minutes", session.minutes)
                            put("plannedMinutes", session.plannedMinutes)
                            put("completed", session.completed)
                            put("kind", session.kind.name)
                            put("todoId", session.todoId ?: "")
                            put("title", session.title)
                            put("note", session.note)
                        },
                    )
                }
            },
        )
        root.put(
            "wishes",
            JSONArray().apply {
                wishes.forEach { wish ->
                    put(
                        JSONObject().apply {
                            put("id", wish.id)
                            put("text", wish.text)
                            put("done", wish.done)
                            put("createdAt", wish.createdAt)
                        },
                    )
                }
            },
        )
        root.put(
            "countdowns",
            JSONArray().apply {
                countdowns.forEach { event ->
                    put(
                        JSONObject().apply {
                            put("id", event.id)
                            put("title", event.title)
                            put("targetEpochDay", event.targetEpochDay)
                            put("emoji", event.emoji)
                            put("createdAt", event.createdAt)
                        },
                    )
                }
            },
        )
        root.put(
            "settings",
            JSONObject().apply {
                put("themeMode", settings.themeMode.name)
                put("colorPreset", settings.colorPreset.name)
                put("focusMinutes", settings.focusMinutes)
                put("shortBreakMinutes", settings.shortBreakMinutes)
                put("longBreakMinutes", settings.longBreakMinutes)
                put("sessionsBeforeLongBreak", settings.sessionsBeforeLongBreak)
                put("dailyGoalMinutes", settings.dailyGoalMinutes)
                put("studyMode", settings.studyMode)
                put("strictMode", settings.strictMode)
                put("blockNotifications", settings.blockNotifications)
                put("blockRecents", settings.blockRecents)
                put("hideInRecents", settings.hideInRecents)
                put("lockBlockHome", settings.lockBlockHome)
                put("whitelistPackages", JSONArray(settings.whitelistPackages.toList()))
                put("cycleEnabled", settings.cycleEnabled)
                put("cycleStudyMinutes", settings.cycleStudyMinutes)
                put("cycleBreakMinutes", settings.cycleBreakMinutes)
                put("cycleCount", settings.cycleCount)
                put("cycleInfinite", settings.cycleInfinite)
                put("keepScreenAwake", settings.keepScreenAwake)
                put("durationPresets", JSONArray(settings.durationPresets))
                put("reduceMotion", settings.reduceMotion)
                put("userName", settings.userName)
                put("userEmoji", settings.userEmoji)
            },
        )
        return root.toString(2)
    }

    /* ----------------------------- helpers ----------------------------- */

    private fun <T> readArray(key: String, mapper: (JSONObject) -> T): List<T> {
        val raw = prefs.getString(key, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    runCatching { mapper(array.getJSONObject(index)) }.getOrNull()?.let(::add)
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun writeArray(key: String, builder: (JSONArray) -> Unit) {
        val array = JSONArray()
        builder(array)
        prefs.edit().putString(key, array.toString()).apply()
    }

    private inline fun <reified T : Enum<T>> enumOrDefault(name: String?, fallback: T): T =
        runCatching { enumValueOf<T>(name.orEmpty()) }.getOrDefault(fallback)

    private companion object {
        const val PREFS_NAME = "tokitodo_prefs"
        const val KEY_TODOS = "todos"
        const val KEY_COLLECTIONS = "collections"
        const val KEY_SCHEDULES = "schedules"
        const val KEY_SESSIONS = "sessions"
        const val KEY_WISHES = "wishes"
        const val KEY_COUNTDOWNS = "countdowns"
        const val KEY_SETTINGS = "settings"
        const val KEY_SEEDED = "seeded"
    }
}
