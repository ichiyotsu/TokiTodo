package com.tokitodo.app.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokitodo.app.ui.theme.TodoArt
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class TimerMode { Focus, Break, Nap, Lock }

enum class TimerPhase { Study, Rest }

data class TimerState(
    val mode: TimerMode,
    val phase: TimerPhase = TimerPhase.Study,
    val title: String,
    val art: TodoArt,
    val totalSeconds: Int,
    val remainingSeconds: Int,
    val elapsedSeconds: Int,
    val running: Boolean,
    val todoId: String? = null,
    val startedAt: Long = System.currentTimeMillis(),
    /** 严格模式：不能暂停。 */
    val strict: Boolean = false,
    val cycleEnabled: Boolean = false,
    val cycleStudyMinutes: Int = 25,
    val cycleBreakMinutes: Int = 5,
    val cycleDone: Int = 0,
    /** -1 表示无限循环。 */
    val cycleTotal: Int = 1,
    /** 四分钟暂停倒计时，大于 0 表示正在暂停。 */
    val pauseRemainingSeconds: Int = 0,
    val pauseReminder: Boolean = false,
    val countUp: Boolean = false,
    val noTimer: Boolean = false,
) {
    val progress: Float
        get() = if (countUp || noTimer || totalSeconds <= 0) {
            0f
        } else {
            (1f - remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
        }

    val elapsedMinutes: Int
        get() = (elapsedSeconds / 60).coerceAtLeast(0)

    val isResting: Boolean
        get() = phase == TimerPhase.Rest

    val displaySeconds: Int
        get() = if (countUp) elapsedSeconds else remainingSeconds
}

sealed interface TimerEvent {
    data class Finished(
        val mode: TimerMode,
        val minutes: Int,
        val title: String,
        val todoId: String?,
        val startedAt: Long,
        val plannedMinutes: Int,
        val completed: Boolean,
        /** 是否是整段专注的最后一次记录（决定是否回主页）。 */
        val final: Boolean,
    ) : TimerEvent

    /** 循环里的所有学习阶段结束，没有新的记录，只负责标记完成与回到主页。 */
    data class Completed(
        val mode: TimerMode,
        val title: String,
        val todoId: String?,
        val minutes: Int,
    ) : TimerEvent

    data object PauseReminder : TimerEvent

    data object Cancelled : TimerEvent
}

/**
 * 专注 / 休息 / 小憩 / 锁机共用的计时器。
 * 用墙钟差推进；支持正计时、倒计时、不计时、循环学习/休息和 4 分钟暂停。
 */
class TimerViewModel : ViewModel() {

    private val _state = MutableStateFlow<TimerState?>(null)
    val state: StateFlow<TimerState?> = _state.asStateFlow()

    private val _events = MutableSharedFlow<TimerEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<TimerEvent> = _events.asSharedFlow()

    private var lastTickAt = 0L
    private var ticker: Job? = null

    fun startFocus(
        todoId: String,
        title: String,
        minutes: Int,
        art: TodoArt,
        strict: Boolean,
        mode: TodoTimerMode = TodoTimerMode.CountDown,
        cycleEnabled: Boolean = false,
        cycleStudyMinutes: Int = 25,
        cycleBreakMinutes: Int = 5,
        cycleTotal: Int = 1,
        cycleInfinite: Boolean = true,
    ) {
        val countUp = mode == TodoTimerMode.CountUp
        val noTimer = mode == TodoTimerMode.NoTimer
        val totalMinutes = if (cycleEnabled) cycleStudyMinutes else minutes
        begin(
            TimerState(
                mode = TimerMode.Focus,
                phase = TimerPhase.Study,
                title = title,
                art = art,
                totalSeconds = if (countUp || noTimer) 0 else totalMinutes * 60,
                remainingSeconds = if (countUp || noTimer) 0 else totalMinutes * 60,
                elapsedSeconds = 0,
                running = true,
                todoId = todoId,
                strict = strict,
                cycleEnabled = cycleEnabled,
                cycleStudyMinutes = cycleStudyMinutes,
                cycleBreakMinutes = cycleBreakMinutes,
                cycleDone = 0,
                cycleTotal = if (cycleEnabled) {
                    if (cycleInfinite) -1 else cycleTotal.coerceAtLeast(1)
                } else {
                    1
                },
                countUp = countUp,
                noTimer = noTimer,
            ),
        )
    }

    fun startFreeFocus(
        minutes: Int,
        strict: Boolean = false,
        cycleEnabled: Boolean = false,
        cycleStudyMinutes: Int = 25,
        cycleBreakMinutes: Int = 5,
        cycleTotal: Int = 1,
        cycleInfinite: Boolean = true,
    ) {
        begin(
            TimerState(
                mode = TimerMode.Focus,
                title = "自由专注",
                art = TodoArt.Aurora,
                totalSeconds = (if (cycleEnabled) cycleStudyMinutes else minutes) * 60,
                remainingSeconds = (if (cycleEnabled) cycleStudyMinutes else minutes) * 60,
                elapsedSeconds = 0,
                running = true,
                strict = strict,
                cycleEnabled = cycleEnabled,
                cycleStudyMinutes = cycleStudyMinutes,
                cycleBreakMinutes = cycleBreakMinutes,
                cycleTotal = if (cycleEnabled) {
                    if (cycleInfinite) -1 else cycleTotal.coerceAtLeast(1)
                } else {
                    1
                },
            ),
        )
    }

    fun startBreak(minutes: Int) {
        begin(
            TimerState(
                mode = TimerMode.Break,
                phase = TimerPhase.Rest,
                title = "休息一下",
                art = TodoArt.Mint,
                totalSeconds = minutes * 60,
                remainingSeconds = minutes * 60,
                elapsedSeconds = 0,
                running = true,
            ),
        )
    }

    fun startNap(minutes: Int) {
        begin(
            TimerState(
                mode = TimerMode.Nap,
                phase = TimerPhase.Rest,
                title = "小憩",
                art = TodoArt.Forest,
                totalSeconds = minutes * 60,
                remainingSeconds = minutes * 60,
                elapsedSeconds = 0,
                running = true,
            ),
        )
    }

    fun startLock(minutes: Int) {
        begin(
            TimerState(
                mode = TimerMode.Lock,
                title = "专注锁机",
                art = TodoArt.Aurora,
                totalSeconds = minutes * 60,
                remainingSeconds = minutes * 60,
                elapsedSeconds = 0,
                running = true,
                strict = true,
            ),
        )
    }

    /** 手动暂停按钮：开始 4 分钟暂停倒计时；严格模式下无效。 */
    fun startPauseCountdown() {
        val current = _state.value ?: return
        if (current.strict) return
        lastTickAt = System.currentTimeMillis()
        _state.value = current.copy(
            running = false,
            pauseRemainingSeconds = 4 * 60,
            pauseReminder = false,
        )
        startTicker()
    }

    fun resumeFromPause() {
        val current = _state.value ?: return
        lastTickAt = System.currentTimeMillis()
        _state.value = current.copy(
            running = true,
            pauseRemainingSeconds = 0,
            pauseReminder = false,
        )
        startTicker()
    }

    fun dismissPauseReminder() {
        val current = _state.value ?: return
        _state.value = current.copy(pauseReminder = false)
    }

    /** 计时页修改循环设置：从下一轮开始生效。 */
    fun updateCycle(
        enabled: Boolean,
        studyMinutes: Int,
        breakMinutes: Int,
        total: Int,
        infinite: Boolean,
    ) {
        val current = _state.value ?: return
        if (current.mode != TimerMode.Focus) return
        _state.value = current.copy(
            cycleEnabled = enabled,
            cycleStudyMinutes = studyMinutes,
            cycleBreakMinutes = breakMinutes,
            cycleTotal = if (enabled) {
                if (infinite) -1 else total.coerceAtLeast(1)
            } else {
                1
            },
        )
    }

    fun toggle() {
        val current = _state.value ?: return
        if (current.strict) return
        if (current.pauseRemainingSeconds > 0) {
            resumeFromPause()
            return
        }
        if (!current.countUp && !current.noTimer && current.remainingSeconds <= 0) return
        lastTickAt = System.currentTimeMillis()
        _state.value = current.copy(running = !current.running)
        if (!current.running) startTicker()
    }

    /** 提前完成：记录当前已专注的分钟数，并把 todo 标记完成。 */
    fun finishEarly() {
        val current = _state.value ?: return
        val minutes = current.elapsedMinutes.coerceAtLeast(1)
        clearState()
        if (current.mode == TimerMode.Focus && current.phase == TimerPhase.Study) {
            emit(
                TimerEvent.Finished(
                    mode = current.mode,
                    minutes = minutes,
                    title = current.title,
                    todoId = current.todoId,
                    startedAt = current.startedAt,
                    plannedMinutes = if (current.totalSeconds > 0) current.totalSeconds / 60 else minutes,
                    completed = true,
                    final = true,
                ),
            )
        } else if (current.mode == TimerMode.Lock || current.mode == TimerMode.Nap) {
            emit(
                TimerEvent.Finished(
                    mode = current.mode,
                    minutes = minutes,
                    title = current.title,
                    todoId = null,
                    startedAt = current.startedAt,
                    plannedMinutes = current.totalSeconds / 60,
                    completed = true,
                    final = true,
                ),
            )
        }
    }

    /** 放弃本轮：保留已专注分钟，但不算完成。 */
    fun abandon() {
        val current = _state.value ?: return
        val minutes = current.elapsedMinutes
        clearState()
        if (current.mode == TimerMode.Focus && current.phase == TimerPhase.Study && minutes >= 1) {
            emit(
                TimerEvent.Finished(
                    mode = current.mode,
                    minutes = minutes,
                    title = current.title,
                    todoId = current.todoId,
                    startedAt = current.startedAt,
                    plannedMinutes = if (current.totalSeconds > 0) current.totalSeconds / 60 else minutes,
                    completed = false,
                    final = true,
                ),
            )
        } else {
            emit(TimerEvent.Cancelled)
        }
    }

    /** 跳过当前状态：学习中由 UI 选择放弃/提前完成，这里只处理休息。 */
    fun skipRest() {
        val current = _state.value ?: return
        if (current.mode != TimerMode.Focus || current.phase != TimerPhase.Rest) return
        advanceAfterRest(current)
    }

    fun finish() {
        val current = _state.value ?: return
        when {
            current.mode == TimerMode.Focus && current.phase == TimerPhase.Study -> finishEarly()
            current.mode == TimerMode.Focus && current.phase == TimerPhase.Rest -> skipRest()
            else -> {
                val minutes = if (current.remainingSeconds <= 1) {
                    current.totalSeconds / 60
                } else {
                    current.elapsedMinutes.coerceAtLeast(1)
                }
                clearState()
                emit(
                    TimerEvent.Finished(
                        mode = current.mode,
                        minutes = minutes,
                        title = current.title,
                        todoId = null,
                        startedAt = current.startedAt,
                        plannedMinutes = current.totalSeconds / 60,
                        completed = true,
                        final = true,
                    ),
                )
            }
        }
    }

    private fun begin(state: TimerState) {
        _state.value = state
        startTicker()
    }

    private fun clearState() {
        _state.value = null
        stopTicker()
    }

    private fun startTicker() {
        lastTickAt = System.currentTimeMillis()
        if (ticker?.isActive == true) return
        ticker = viewModelScope.launch {
            while (true) {
                delay(250)
                tick()
            }
        }
    }

    private fun stopTicker() {
        ticker?.cancel()
        ticker = null
    }

    private fun tick() {
        val current = _state.value ?: return
        val now = System.currentTimeMillis()
        if (current.pauseRemainingSeconds > 0) {
            val delta = ((now - lastTickAt) / 1000).toInt()
            if (delta <= 0) return
            lastTickAt += delta * 1000L
            val remaining = (current.pauseRemainingSeconds - delta).coerceAtLeast(0)
            _state.value = current.copy(
                pauseRemainingSeconds = remaining,
                pauseReminder = remaining == 0,
            )
            if (remaining == 0) emit(TimerEvent.PauseReminder)
            return
        }
        if (!current.running) {
            lastTickAt = now
            return
        }
        val delta = ((now - lastTickAt) / 1000).toInt()
        if (delta <= 0) return
        lastTickAt += delta * 1000L
        val elapsed = current.elapsedSeconds + delta
        if (current.countUp || current.noTimer) {
            _state.value = current.copy(elapsedSeconds = elapsed)
            return
        }
        val remaining = (current.remainingSeconds - delta).coerceAtLeast(0)
        _state.value = current.copy(
            remainingSeconds = remaining,
            elapsedSeconds = elapsed,
        )
        if (remaining == 0) {
            handlePhaseComplete(current.copy(remainingSeconds = 0, elapsedSeconds = elapsed))
        }
    }

    private fun handlePhaseComplete(finished: TimerState) {
        if (finished.mode == TimerMode.Focus && finished.phase == TimerPhase.Study) {
            completeStudyPhase(finished)
            return
        }
        if (finished.mode == TimerMode.Focus && finished.phase == TimerPhase.Rest) {
            advanceAfterRest(finished)
            return
        }
        clearState()
        emit(
            TimerEvent.Finished(
                mode = finished.mode,
                minutes = finished.totalSeconds / 60,
                title = finished.title,
                todoId = null,
                startedAt = finished.startedAt,
                plannedMinutes = finished.totalSeconds / 60,
                completed = true,
                final = true,
            ),
        )
    }

    private fun completeStudyPhase(finished: TimerState) {
        val minutes = finished.totalSeconds / 60
        val completedRounds = finished.cycleDone + 1
        // 先记录这一轮学习；循环没有结束时 final = false。
        emit(
            TimerEvent.Finished(
                mode = TimerMode.Focus,
                minutes = minutes,
                title = finished.title,
                todoId = finished.todoId,
                startedAt = finished.startedAt,
                plannedMinutes = minutes,
                completed = true,
                final = !finished.cycleEnabled,
            ),
        )
        if (!finished.cycleEnabled) {
            clearState()
            return
        }
        // 循环开启：进入休息阶段。
        val restSeconds = finished.cycleBreakMinutes.coerceAtLeast(1) * 60
        _state.value = finished.copy(
            phase = TimerPhase.Rest,
            totalSeconds = restSeconds,
            remainingSeconds = restSeconds,
            elapsedSeconds = 0,
            startedAt = System.currentTimeMillis(),
            running = true,
            cycleDone = completedRounds,
            pauseRemainingSeconds = 0,
            pauseReminder = false,
        )
        lastTickAt = System.currentTimeMillis()
    }

    private fun advanceAfterRest(rest: TimerState) {
        val hasNextRound = rest.cycleTotal < 0 || rest.cycleDone < rest.cycleTotal
        if (!hasNextRound) {
            clearState()
            emit(
                TimerEvent.Completed(
                    mode = TimerMode.Focus,
                    title = rest.title,
                    todoId = rest.todoId,
                    minutes = rest.cycleStudyMinutes,
                ),
            )
            return
        }
        val studySeconds = rest.cycleStudyMinutes.coerceAtLeast(1) * 60
        _state.value = rest.copy(
            phase = TimerPhase.Study,
            totalSeconds = studySeconds,
            remainingSeconds = studySeconds,
            elapsedSeconds = 0,
            startedAt = System.currentTimeMillis(),
            running = true,
            pauseRemainingSeconds = 0,
            pauseReminder = false,
        )
        lastTickAt = System.currentTimeMillis()
    }

    private fun emit(event: TimerEvent) {
        viewModelScope.launch { _events.emit(event) }
    }

    override fun onCleared() {
        stopTicker()
        super.onCleared()
    }
}
