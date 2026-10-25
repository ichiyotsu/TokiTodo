package com.tokitodo.app.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tokitodo.app.ui.theme.ColorPreset
import com.tokitodo.app.ui.theme.TodoArt
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val store = TokiStore(application)

    private val _todos = MutableStateFlow(store.loadTodos())
    val todos: StateFlow<List<Todo>> = _todos.asStateFlow()

    private val _collections = MutableStateFlow(store.loadCollections())
    val collections: StateFlow<List<TodoCollection>> = _collections.asStateFlow()

    private val _schedules = MutableStateFlow(store.loadSchedules())
    val schedules: StateFlow<List<LockSchedule>> = _schedules.asStateFlow()

    private val _sessions = MutableStateFlow(store.loadSessions())
    val sessions: StateFlow<List<FocusSession>> = _sessions.asStateFlow()

    private val _wishes = MutableStateFlow(store.loadWishes())
    val wishes: StateFlow<List<Wish>> = _wishes.asStateFlow()

    private val _countdowns = MutableStateFlow(store.loadCountdowns())
    val countdowns: StateFlow<List<CountdownEvent>> = _countdowns.asStateFlow()

    private val _settings = MutableStateFlow(store.loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    init {
        if (!store.isSeeded()) {
            _todos.value = seedTodos()
            _collections.value = seedCollections()
            store.markSeeded()
            store.saveTodos(_todos.value)
            store.saveCollections(_collections.value)
        }
    }

    /* ----------------------------- todos ----------------------------- */

    fun addTodo(title: String, minutes: Int, art: TodoArt, collectionId: String?, note: String) {
        val todo = Todo(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            minutes = minutes.coerceIn(1, 1440),
            art = art,
            collectionId = collectionId,
            note = note.trim(),
        )
        _todos.update { listOf(todo) + it }
        persistTodos()
    }

    /** 编辑器保存：已存在则更新，否则插到最前。 */
    fun upsertTodo(todo: Todo) {
        _todos.update { list ->
            if (list.any { it.id == todo.id }) {
                list.map { if (it.id == todo.id) todo else it }
            } else {
                listOf(todo) + list
            }
        }
        persistTodos()
    }

    fun updateTodo(todo: Todo) {
        _todos.update { list -> list.map { if (it.id == todo.id) todo else it } }
        persistTodos()
    }

    fun deleteTodo(id: String) {
        _todos.update { list -> list.filterNot { it.id == id } }
        persistTodos()
    }

    fun toggleDone(id: String) {
        _todos.update { list ->
            list.map { if (it.id == id) it.copy(done = !it.done) else it }
        }
        persistTodos()
    }

    fun clearCompleted() {
        _todos.update { list -> list.filterNot { it.done } }
        persistTodos()
    }

    fun markFocused(todoId: String, minutes: Int, complete: Boolean = false) {
        _todos.update { list ->
            list.map {
                if (it.id == todoId) {
                    it.copy(
                        focusCount = it.focusCount + 1,
                        focusedMinutes = it.focusedMinutes + minutes,
                        lastFocusedAt = System.currentTimeMillis(),
                        done = it.done || complete,
                    )
                } else {
                    it
                }
            }
        }
        persistTodos()
    }

    fun completeTodo(todoId: String) {
        _todos.update { list ->
            list.map { if (it.id == todoId) it.copy(done = true) else it }
        }
        persistTodos()
    }

    /* ----------------------------- collections ----------------------------- */

    fun addCollection(name: String, tintIndex: Int): TodoCollection {
        val collection = TodoCollection(
            id = UUID.randomUUID().toString(),
            name = name.trim().ifBlank { "新待办集" },
            tintIndex = tintIndex,
        )
        _collections.update { listOf(collection) + it }
        persistCollections()
        return collection
    }

    fun updateCollection(id: String, name: String, tintIndex: Int) {
        _collections.update { list ->
            list.map {
                if (it.id == id) {
                    it.copy(name = name.trim().ifBlank { it.name }, tintIndex = tintIndex)
                } else {
                    it
                }
            }
        }
        persistCollections()
    }

    fun deleteCollection(id: String) {
        _collections.update { list -> list.filterNot { it.id == id } }
        _todos.update { list -> list.map { if (it.collectionId == id) it.copy(collectionId = null) else it } }
        persistCollections()
        persistTodos()
    }

    /* ----------------------------- lock schedules ----------------------------- */

    fun addSchedule(label: String, startMinute: Int, endMinute: Int, days: Set<Int>) {
        val schedule = LockSchedule(
            id = UUID.randomUUID().toString(),
            label = label.trim().ifBlank { "锁机时段" },
            startMinute = startMinute,
            endMinute = endMinute,
            days = days.ifEmpty { setOf(1, 2, 3, 4, 5, 6, 7) },
        )
        _schedules.update { it + schedule }
        persistSchedules()
    }

    fun setScheduleEnabled(id: String, enabled: Boolean) {
        _schedules.update { list -> list.map { if (it.id == id) it.copy(enabled = enabled) else it } }
        persistSchedules()
    }

    fun deleteSchedule(id: String) {
        _schedules.update { list -> list.filterNot { it.id == id } }
        persistSchedules()
    }

    /* ----------------------------- sessions ----------------------------- */

    fun recordSession(
        minutes: Int,
        kind: SessionKind,
        todoId: String? = null,
        title: String = "",
        startedAt: Long = System.currentTimeMillis() - minutes * 60_000L,
        plannedMinutes: Int = minutes,
        completed: Boolean = true,
        note: String = "",
    ) {
        if (minutes <= 0) return
        val session = FocusSession(
            id = UUID.randomUUID().toString(),
            endedAt = System.currentTimeMillis(),
            minutes = minutes,
            kind = kind,
            todoId = todoId,
            title = title,
            startedAt = startedAt,
            plannedMinutes = plannedMinutes,
            completed = completed,
            note = note,
        )
        _sessions.update { it + session }
        if (todoId != null && (kind == SessionKind.Focus || kind == SessionKind.Lock)) {
            markFocused(todoId, minutes, complete = completed && kind == SessionKind.Focus)
        }
        persistSessions()
    }

    fun updateSession(
        id: String,
        title: String,
        minutes: Int,
        note: String,
    ) {
        _sessions.update { list ->
            list.map {
                if (it.id == id) {
                    val delta = (minutes - it.minutes).coerceIn(-5, 5)
                    val newMinutes = (it.minutes + delta).coerceAtLeast(1)
                    it.copy(
                        title = title.trim().ifBlank { it.title },
                        minutes = newMinutes,
                        note = note,
                        endedAt = it.startedAt + newMinutes * 60_000L,
                    )
                } else {
                    it
                }
            }
        }
        persistSessions()
    }

    fun deleteSession(id: String) {
        _sessions.update { list -> list.filterNot { it.id == id } }
        persistSessions()
    }

    fun clearSessions() {
        _sessions.value = emptyList()
        persistSessions()
    }

    /* ----------------------------- wishes / countdowns ----------------------------- */

    fun addWish(text: String) {
        if (text.isBlank()) return
        _wishes.update {
            it + Wish(id = UUID.randomUUID().toString(), text = text.trim())
        }
        persistWishes()
    }

    fun toggleWish(id: String) {
        _wishes.update { list -> list.map { if (it.id == id) it.copy(done = !it.done) else it } }
        persistWishes()
    }

    fun deleteWish(id: String) {
        _wishes.update { list -> list.filterNot { it.id == id } }
        persistWishes()
    }

    fun addCountdown(title: String, targetEpochDay: Long, emoji: String) {
        if (title.isBlank()) return
        _countdowns.update {
            it + CountdownEvent(
                id = UUID.randomUUID().toString(),
                title = title.trim(),
                targetEpochDay = targetEpochDay,
                emoji = emoji,
            )
        }
        persistCountdowns()
    }

    fun deleteCountdown(id: String) {
        _countdowns.update { list -> list.filterNot { it.id == id } }
        persistCountdowns()
    }

    /* ----------------------------- settings ----------------------------- */

    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        _settings.update(transform)
        store.saveSettings(_settings.value)
    }

    fun setStudyMode(enabled: Boolean) = updateSettings { it.copy(studyMode = enabled) }

    fun setForceStudy(enabled: Boolean) = updateSettings { it.copy(studyMode = enabled) }

    fun toggleWhitelistPackage(packageName: String) = updateSettings { settings ->
        settings.copy(
            whitelistPackages = if (settings.whitelistPackages.contains(packageName)) {
                settings.whitelistPackages - packageName
            } else {
                settings.whitelistPackages + packageName
            },
        )
    }

    fun clearAllData() {
        store.clearAll()
        _todos.value = emptyList()
        _collections.value = emptyList()
        _schedules.value = emptyList()
        _sessions.value = emptyList()
        _wishes.value = emptyList()
        _countdowns.value = emptyList()
        _settings.value = AppSettings()
        store.markSeeded()
    }

    fun exportJson(): String = store.exportJson(
        todos = _todos.value,
        collections = _collections.value,
        schedules = _schedules.value,
        sessions = _sessions.value,
        wishes = _wishes.value,
        countdowns = _countdowns.value,
        settings = _settings.value,
    )

    /**
     * 从导出的 JSON 恢复全部本地数据；返回是否成功。
     * 这是一个「覆盖导入」：导入成功后当前数据会被备份内容替换。
     */
    fun importJson(raw: String): Boolean = runCatching {
        val root = JSONObject(raw)
        if (root.optString("app") !in setOf("TokiTodo", "TokiToDo")) return false

        val todos = buildList {
            val array = root.optJSONArray("todos") ?: JSONArray()
            for (index in 0 until array.length()) {
                val json = array.optJSONObject(index) ?: continue
                add(
                    Todo(
                        id = json.optString("id").ifBlank { UUID.randomUUID().toString() },
                        title = json.optString("title", "未命名待办"),
                        minutes = json.optInt("minutes", 25).coerceIn(1, 1440),
                        art = runCatching { TodoArt.valueOf(json.optString("art")) }
                            .getOrDefault(TodoArt.Sky),
                        collectionId = json.optString("collectionId").takeUnless { it.isBlank() },
                        note = json.optString("note", ""),
                        done = json.optBoolean("done", false),
                        createdAt = json.optLong("createdAt", System.currentTimeMillis()),
                        lastFocusedAt = if (json.has("lastFocusedAt")) {
                            json.optLong("lastFocusedAt")
                        } else {
                            null
                        },
                        focusCount = json.optInt("focusCount", 0),
                        focusedMinutes = json.optInt("focusedMinutes", 0),
                        timerMode = runCatching {
                            TodoTimerMode.valueOf(json.optString("timerMode"))
                        }.getOrDefault(TodoTimerMode.CountDown),
                        type = runCatching { TodoType.valueOf(json.optString("type")) }
                            .getOrDefault(TodoType.Pomodoro),
                        goalDateEpochDay = if (json.has("goalDateEpochDay")) {
                            json.optLong("goalDateEpochDay")
                        } else {
                            null
                        },
                        goalHours = json.optDouble("goalHours", 0.0).toFloat(),
                        habitPeriod = runCatching {
                            HabitPeriod.valueOf(json.optString("habitPeriod"))
                        }.getOrDefault(HabitPeriod.Daily),
                        habitHours = json.optDouble("habitHours", 0.0).toFloat(),
                        coverPath = json.optString("coverPath").takeUnless { it.isBlank() },
                    ),
                )
            }
        }

        val collections = buildList {
            val array = root.optJSONArray("collections") ?: JSONArray()
            for (index in 0 until array.length()) {
                val json = array.optJSONObject(index) ?: continue
                add(
                    TodoCollection(
                        id = json.optString("id").ifBlank { UUID.randomUUID().toString() },
                        name = json.optString("name", "待办集"),
                        tintIndex = json.optInt("tintIndex", 0),
                        createdAt = json.optLong("createdAt", System.currentTimeMillis()),
                    ),
                )
            }
        }

        val schedules = buildList {
            val array = root.optJSONArray("schedules") ?: JSONArray()
            for (index in 0 until array.length()) {
                val json = array.optJSONObject(index) ?: continue
                val days = buildSet {
                    val values = json.optJSONArray("days") ?: JSONArray()
                    for (day in 0 until values.length()) add(values.optInt(day))
                }
                add(
                    LockSchedule(
                        id = json.optString("id").ifBlank { UUID.randomUUID().toString() },
                        label = json.optString("label", "锁机时段"),
                        startMinute = json.optInt("startMinute", 23 * 60),
                        endMinute = json.optInt("endMinute", 7 * 60),
                        days = days.ifEmpty { setOf(1, 2, 3, 4, 5, 6, 7) },
                        enabled = json.optBoolean("enabled", true),
                        createdAt = json.optLong("createdAt", System.currentTimeMillis()),
                    ),
                )
            }
        }

        val sessions = buildList {
            val array = root.optJSONArray("sessions") ?: JSONArray()
            for (index in 0 until array.length()) {
                val json = array.optJSONObject(index) ?: continue
                add(
                    FocusSession(
                        id = json.optString("id").ifBlank { UUID.randomUUID().toString() },
                        endedAt = json.optLong("endedAt", System.currentTimeMillis()),
                        minutes = json.optInt("minutes", 0).coerceAtLeast(0),
                        kind = runCatching { SessionKind.valueOf(json.optString("kind")) }
                            .getOrDefault(SessionKind.Focus),
                        todoId = json.optString("todoId").takeUnless { it.isBlank() },
                        title = json.optString("title", ""),
                        startedAt = json.optLong(
                            "startedAt",
                            json.optLong("endedAt", System.currentTimeMillis()) -
                                json.optInt("minutes", 0) * 60_000L,
                        ),
                        plannedMinutes = json.optInt(
                            "plannedMinutes",
                            json.optInt("minutes", 0),
                        ),
                        completed = json.optBoolean("completed", true),
                        note = json.optString("note", ""),
                    ),
                )
            }
        }

        val wishes = buildList {
            val array = root.optJSONArray("wishes") ?: JSONArray()
            for (index in 0 until array.length()) {
                val json = array.optJSONObject(index) ?: continue
                add(
                    Wish(
                        id = json.optString("id").ifBlank { UUID.randomUUID().toString() },
                        text = json.optString("text", ""),
                        done = json.optBoolean("done", false),
                        createdAt = json.optLong("createdAt", System.currentTimeMillis()),
                    ),
                )
            }
        }

        val countdowns = buildList {
            val array = root.optJSONArray("countdowns") ?: JSONArray()
            for (index in 0 until array.length()) {
                val json = array.optJSONObject(index) ?: continue
                add(
                    CountdownEvent(
                        id = json.optString("id").ifBlank { UUID.randomUUID().toString() },
                        title = json.optString("title", "重要日子"),
                        targetEpochDay = json.optLong("targetEpochDay", System.currentTimeMillis() / 86_400_000L),
                        emoji = json.optString("emoji", "🎯"),
                        createdAt = json.optLong("createdAt", System.currentTimeMillis()),
                    ),
                )
            }
        }

        _todos.value = todos
        _collections.value = collections
        _schedules.value = schedules
        _sessions.value = sessions
        _wishes.value = wishes
        _countdowns.value = countdowns
        persistTodos()
        persistCollections()
        persistSchedules()
        persistSessions()
        persistWishes()
        persistCountdowns()

        root.optJSONObject("settings")?.let { json ->
            updateSettings {
                it.copy(
                    themeMode = runCatching { ThemeMode.valueOf(json.optString("themeMode")) }
                        .getOrDefault(it.themeMode),
                    colorPreset = runCatching { ColorPreset.valueOf(json.optString("colorPreset")) }
                        .getOrDefault(it.colorPreset),
                    focusMinutes = json.optInt("focusMinutes", it.focusMinutes).coerceIn(1, 180),
                    shortBreakMinutes = json.optInt("shortBreakMinutes", it.shortBreakMinutes).coerceIn(1, 60),
                    longBreakMinutes = json.optInt("longBreakMinutes", it.longBreakMinutes).coerceIn(1, 90),
                    sessionsBeforeLongBreak = json.optInt(
                        "sessionsBeforeLongBreak",
                        it.sessionsBeforeLongBreak,
                    ).coerceIn(1, 12),
                    dailyGoalMinutes = json.optInt("dailyGoalMinutes", it.dailyGoalMinutes).coerceIn(15, 720),
                    studyMode = json.optBoolean("studyMode", it.studyMode),
                    strictMode = json.optBoolean("strictMode", it.strictMode),
                    blockNotifications = json.optBoolean("blockNotifications", it.blockNotifications),
                    blockRecents = json.optBoolean("blockRecents", it.blockRecents),
                    hideInRecents = json.optBoolean("hideInRecents", it.hideInRecents),
                    lockBlockHome = json.optBoolean("lockBlockHome", it.lockBlockHome),
                    whitelistPackages = buildSet {
                        val values = json.optJSONArray("whitelistPackages") ?: JSONArray()
                        for (index in 0 until values.length()) add(values.optString(index))
                    },
                    cycleEnabled = json.optBoolean("cycleEnabled", it.cycleEnabled),
                    cycleStudyMinutes = json.optInt("cycleStudyMinutes", it.cycleStudyMinutes).coerceIn(1, 180),
                    cycleBreakMinutes = json.optInt("cycleBreakMinutes", it.cycleBreakMinutes).coerceIn(1, 60),
                    cycleCount = json.optInt("cycleCount", it.cycleCount).coerceIn(1, 24),
                    cycleInfinite = json.optBoolean("cycleInfinite", it.cycleInfinite),
                    keepScreenAwake = json.optBoolean("keepScreenAwake", it.keepScreenAwake),
                    durationPresets = buildList {
                        val values = json.optJSONArray("durationPresets")
                        if (values == null) {
                            addAll(it.durationPresets)
                        } else {
                            for (index in 0 until values.length()) {
                                add(values.optInt(index, 25).coerceIn(1, 1440))
                            }
                        }
                    },
                    reduceMotion = json.optBoolean("reduceMotion", it.reduceMotion),
                    userName = json.optString("userName", it.userName),
                    userEmoji = json.optString("userEmoji", it.userEmoji),
                )
            }
        }
        store.markSeeded()
        true
    }.getOrDefault(false)

    /* ----------------------------- persistence ----------------------------- */

    private fun persistTodos() = viewModelScope.launch {
        store.saveTodos(_todos.value)
    }

    private fun persistCollections() = viewModelScope.launch {
        store.saveCollections(_collections.value)
    }

    private fun persistSchedules() = viewModelScope.launch {
        store.saveSchedules(_schedules.value)
    }

    private fun persistSessions() = viewModelScope.launch {
        store.saveSessions(_sessions.value)
    }

    private fun persistWishes() = viewModelScope.launch {
        store.saveWishes(_wishes.value)
    }

    private fun persistCountdowns() = viewModelScope.launch {
        store.saveCountdowns(_countdowns.value)
    }

    /* ----------------------------- seed ----------------------------- */

    private fun seedCollections(): List<TodoCollection> = listOf(
        TodoCollection(id = "seed-study", name = "期末复习", tintIndex = 0),
        TodoCollection(id = "seed-life", name = "生活小事", tintIndex = 1),
    )

    private fun seedTodos(): List<Todo> = listOf(
        Todo(
            id = "seed-1",
            title = "复习线性代数第三章",
            minutes = 45,
            art = TodoArt.Dusk,
            collectionId = "seed-study",
        ),
        Todo(
            id = "seed-2",
            title = "背 50 个英语单词",
            minutes = 15,
            art = TodoArt.Mint,
            collectionId = "seed-study",
        ),
        Todo(
            id = "seed-3",
            title = "写一页晨间日记",
            minutes = 10,
            art = TodoArt.Sky,
            collectionId = "seed-life",
        ),
        Todo(
            id = "seed-4",
            title = "整理明天的待办",
            minutes = 5,
            art = TodoArt.Blossom,
            collectionId = "seed-life",
        ),
    )
}
