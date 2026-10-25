package com.tokitodo.app.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokitodo.app.ui.theme.TodoArt
import java.util.UUID
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

data class TimerState(
    val mode: TimerMode,
    val title: String,
    val art: TodoArt,
    val totalSeconds: Int,
    val remainingSeconds: Int,
    val running: Boolean,
    val todoId: String? = null,
    val startedAt: Long = System.currentTimeMillis(),
    /** 学霸模式：专注中禁止暂停，退出前需要二次确认。 */
    val strict: Boolean = false,
    val sessionsDone: Int = 0,
) {
    val progress: Float
        get() = if (totalSeconds <= 0) 0f else {
            (1f - remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
        }

    val elapsedMinutes: Int
        get() = ((totalSeconds - remainingSeconds) / 60).coerceAtLeast(0)
}

sealed interface TimerEvent {
    data class Finished(
        val mode: TimerMode,
        val minutes: Int,
        val title: String,
        val todoId: String?,
    ) : TimerEvent

    data object Cancelled : TimerEvent
}

/**
 * 一个进程内的计时器：专注、短休 / 长休、小憩、锁机共用。
 * 用墙钟差推进剩余时间，退到后台再回来也不会漂移。
 */
class TimerViewModel : ViewModel() {

    private val _state = MutableStateFlow<TimerState?>(null)
    val state: StateFlow<TimerState?> = _state.asStateFlow()

    private val _events = MutableSharedFlow<TimerEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<TimerEvent> = _events.asSharedFlow()

    private var lastTickAt = 0L
    private var ticker: Job? = null
    private var completedToday = 0

    fun startFocus(
        todoId: String,
        title: String,
        minutes: Int,
        art: TodoArt,
        strict: Boolean,
    ) {
        completedToday = 0
        begin(
            TimerState(
                mode = TimerMode.Focus,
                title = title,
                art = art,
                totalSeconds = minutes * 60,
                remainingSeconds = minutes * 60,
                running = true,
                todoId = todoId,
                strict = strict,
            ),
        )
    }

    fun startFreeFocus(minutes: Int, strict: Boolean = false) {
        begin(
            TimerState(
                mode = TimerMode.Focus,
                title = "自由专注",
                art = TodoArt.Aurora,
                totalSeconds = minutes * 60,
                remainingSeconds = minutes * 60,
                running = true,
                strict = strict,
            ),
        )
    }

    fun startBreak(minutes: Int) {        begin(
            TimerState(
                mode = TimerMode.Break,
                title = "休息一下",
                art = TodoArt.Mint,
                totalSeconds = minutes * 60,
                remainingSeconds = minutes * 60,
                running = true,
            ),
        )
    }

    fun startNap(minutes: Int) {
        begin(
            TimerState(
                mode = TimerMode.Nap,
                title = "小憩",
                art = TodoArt.Forest,
                totalSeconds = minutes * 60,
                remainingSeconds = minutes * 60,
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
                running = true,
                strict = true,
            ),
        )
    }

    fun toggle() {
        val current = _state.value ?: return
        if (current.strict && current.mode == TimerMode.Focus && current.running) return
        if (current.remainingSeconds <= 0) return
        lastTickAt = System.currentTimeMillis()
        _state.value = current.copy(running = !current.running)
        if (!current.running) startTicker()
    }

    fun extend(minutes: Int = 5) {
        val current = _state.value ?: return
        val extra = minutes * 60
        _state.value = current.copy(
            totalSeconds = current.totalSeconds + extra,
            remainingSeconds = current.remainingSeconds + extra,
        )
    }

    fun finish() {
        val current = _state.value ?: return
        val minutes = if (current.remainingSeconds <= 1) {
            current.totalSeconds / 60
        } else {
            current.elapsedMinutes
        }
        _state.value = null
        stopTicker()
        if (minutes > 0) {
            emit(TimerEvent.Finished(current.mode, minutes, current.title, current.todoId))
        }
    }

    fun abandon() {
        val current = _state.value ?: return
        val minutes = current.elapsedMinutes
        _state.value = null
        stopTicker()
        if (current.mode == TimerMode.Focus && minutes >= 1) {
            emit(TimerEvent.Finished(current.mode, minutes, current.title, current.todoId))
        } else {
            emit(TimerEvent.Cancelled)
        }
    }

    fun dismissEvent() = Unit

    private fun begin(state: TimerState) {
        _state.value = state
        startTicker()
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
        if (!current.running) {
            lastTickAt = System.currentTimeMillis()
            return
        }
        val now = System.currentTimeMillis()
        val deltaSeconds = ((now - lastTickAt) / 1000).toInt()
        if (deltaSeconds <= 0) return
        lastTickAt += deltaSeconds * 1000L
        val remaining = (current.remainingSeconds - deltaSeconds).coerceAtLeast(0)
        _state.value = current.copy(remainingSeconds = remaining)
        if (remaining == 0) {
            val minutes = current.totalSeconds / 60
            _state.value = null
            stopTicker()
            emit(TimerEvent.Finished(current.mode, minutes, current.title, current.todoId))
        }
    }

    private fun emit(event: TimerEvent) {
        viewModelScope.launch { _events.emit(event) }
    }

    override fun onCleared() {
        stopTicker()
        super.onCleared()
    }
}
