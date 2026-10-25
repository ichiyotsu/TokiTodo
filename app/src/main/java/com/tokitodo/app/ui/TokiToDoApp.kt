package com.tokitodo.app.ui

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tokitodo.app.data.AppSettings
import com.tokitodo.app.data.AppViewModel
import com.tokitodo.app.data.CountdownEvent
import com.tokitodo.app.data.FocusSession
import com.tokitodo.app.data.LockSchedule
import com.tokitodo.app.data.SessionKind
import com.tokitodo.app.data.Stats
import com.tokitodo.app.data.TimerEvent
import com.tokitodo.app.data.TimerMode
import com.tokitodo.app.data.TimerViewModel
import com.tokitodo.app.data.Todo
import com.tokitodo.app.data.TodoCollection
import com.tokitodo.app.data.Wish
import com.tokitodo.app.ui.collections.CollectionsScreen
import com.tokitodo.app.ui.components.FloatingNavBar
import com.tokitodo.app.ui.extras.CountdownScreen
import com.tokitodo.app.ui.extras.VipScreen
import com.tokitodo.app.ui.extras.WishWallScreen
import com.tokitodo.app.ui.force.ForceEnhancementsScreen
import com.tokitodo.app.ui.force.ForceStudySheet
import com.tokitodo.app.ui.force.WhitelistScreen
import com.tokitodo.app.ui.history.HistoryCenterScreen
import com.tokitodo.app.ui.lock.LockScreen
import com.tokitodo.app.ui.profile.ProfileScreen
import com.tokitodo.app.ui.settings.SettingsDetailScreen
import com.tokitodo.app.ui.stats.StatsScreen
import com.tokitodo.app.ui.theme.LocalDarkTheme
import com.tokitodo.app.ui.theme.tokiGlass
import com.tokitodo.app.ui.timer.CompletionInfo
import com.tokitodo.app.ui.timer.CompletionScreen
import com.tokitodo.app.ui.timer.TimerScreen
import com.tokitodo.app.ui.todos.TodosScreen
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import java.time.LocalDateTime
import kotlinx.coroutines.delay

@Composable
fun TokiToDoApp(appViewModel: AppViewModel) {
    val todos by appViewModel.todos.collectAsStateWithLifecycle()
    val collections by appViewModel.collections.collectAsStateWithLifecycle()
    val schedules by appViewModel.schedules.collectAsStateWithLifecycle()
    val sessions by appViewModel.sessions.collectAsStateWithLifecycle()
    val wishes by appViewModel.wishes.collectAsStateWithLifecycle()
    val countdowns by appViewModel.countdowns.collectAsStateWithLifecycle()
    val settings by appViewModel.settings.collectAsStateWithLifecycle()

    val timerViewModel: TimerViewModel = viewModel()
    val timer by timerViewModel.state.collectAsStateWithLifecycle()

    val hazeState = rememberHazeState()
    val dark = LocalDarkTheme.current
    val smoke = if (dark) Color.Black.copy(alpha = 0.22f) else Color.Black.copy(alpha = 0.04f)
    val frost = if (dark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.28f)
    val fallback = if (dark) {
        Color(0xFF1A211D).copy(alpha = 0.94f)
    } else {
        Color(0xFFF3F7F2).copy(alpha = 0.94f)
    }
    val glass = Modifier.tokiGlass(
        hazeState = hazeState,
        blurRadius = 30.dp,
        tints = listOf(smoke, frost),
        fallback = fallback,
    )

    var tab by rememberSaveable {
        mutableStateOf(
            runCatching { Tab.valueOf(settings.defaultTab) }.getOrDefault(Tab.Todos),
        )
    }
    val stack = remember { mutableStateListOf<Dest>() }
    val current: Dest? = stack.lastOrNull()
    var completion by remember { mutableStateOf<CompletionInfo?>(null) }
    val context = LocalContext.current
    val currentSettings by rememberUpdatedState(settings)
    val currentSchedules by rememberUpdatedState(schedules)
    var autoLockKey by remember { mutableStateOf<String?>(null) }
    val tone = remember {
        runCatching { ToneGenerator(AudioManager.STREAM_NOTIFICATION, 55) }.getOrNull()
    }
    @Suppress("DEPRECATION")
    val vibrator = remember { context.getSystemService(Vibrator::class.java) }
    DisposableEffect(tone) {
        onDispose { runCatching { tone?.release() } }
    }

    fun feedback() {
        if (currentSettings.soundEnabled) {
            tone?.startTone(ToneGenerator.TONE_PROP_BEEP2, 220)
        }
        if (currentSettings.vibrationEnabled) {
            runCatching {
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(180, VibrationEffect.DEFAULT_AMPLITUDE),
                )
            }
        }
    }

    LaunchedEffect(timerViewModel) {
        timerViewModel.events.collect { event ->
            when (event) {
                is TimerEvent.Finished -> {
                    appViewModel.recordSession(
                        minutes = event.minutes,
                        kind = when (event.mode) {
                            TimerMode.Lock -> SessionKind.Lock
                            TimerMode.Nap -> SessionKind.Nap
                            TimerMode.Break -> SessionKind.Break
                            TimerMode.Focus -> SessionKind.Focus
                        },
                        todoId = event.todoId,
                        title = event.title,
                        startedAt = event.startedAt,
                        plannedMinutes = event.plannedMinutes,
                        completed = event.completed,
                    )
                    feedback()
                    if (event.final) {
                        if (event.mode == TimerMode.Focus && event.completed) {
                            event.todoId?.let { appViewModel.completeTodo(it) }
                            completion = CompletionInfo(
                                title = event.title,
                                minutes = event.minutes,
                                todoId = event.todoId,
                            )
                        } else if (event.mode == TimerMode.Lock) {
                            completion = CompletionInfo(
                                title = "锁机完成",
                                minutes = event.minutes,
                                todoId = null,
                            )
                        } else if (event.mode == TimerMode.Nap) {
                            completion = CompletionInfo(
                                title = "小憩结束",
                                minutes = event.minutes,
                                todoId = null,
                            )
                        } else {
                            stack.clear()
                            tab = Tab.Todos
                        }
                    }
                }

                is TimerEvent.Completed -> {
                    event.todoId?.let { appViewModel.completeTodo(it) }
                    feedback()
                    completion = CompletionInfo(
                        title = event.title,
                        minutes = event.minutes,
                        todoId = event.todoId,
                    )
                }

                TimerEvent.PauseReminder -> feedback()

                TimerEvent.Cancelled -> {
                    stack.clear()
                    tab = Tab.Todos
                }
            }
        }
    }

    // 定时锁机：每 20 秒检查一次是否进入设定时段。
    LaunchedEffect(timerViewModel) {
        while (true) {
            val now = LocalDateTime.now()
            val active = currentSchedules.firstOrNull { Stats.inSchedule(it, now) }
            val key = active?.let { "${it.id}-${now.toLocalDate()}" }
            if (active != null && timerViewModel.state.value == null) {
                if (autoLockKey != key) {
                    autoLockKey = key
                    val minute = now.hour * 60 + now.minute
                    val remaining = if (active.startMinute <= active.endMinute) {
                        active.endMinute - minute
                    } else {
                        1440 - minute + active.endMinute
                    }
                    if (remaining > 0) {
                        timerViewModel.startLock(remaining.coerceAtMost(180))
                    }
                }
            } else if (active == null) {
                autoLockKey = null
            }
            delay(20_000)
        }
    }

    fun open(dest: Dest) {
        stack.add(dest)
    }

    fun back() {
        if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex)
    }

    BackHandler(enabled = stack.isNotEmpty() && timer == null) { back() }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surface,
            contentWindowInsets = WindowInsets.safeDrawing,
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(hazeState),
            ) {
                AnimatedContent(
                    targetState = current,
                    transitionSpec = {
                        when {
                            initialState == null && targetState == null ->
                                EnterTransition.None togetherWith ExitTransition.None

                            targetState != null ->
                                (slideInHorizontally(tween(TokiMotion.expressive)) { it / 8 } +
                                    fadeIn(tween(TokiMotion.standard))) togetherWith
                                    fadeOut(tween(TokiMotion.quick))

                            else ->
                                fadeIn(tween(TokiMotion.standard)) togetherWith
                                    (slideOutHorizontally(tween(TokiMotion.quick)) { it / 8 } +
                                        fadeOut(tween(TokiMotion.quick)))
                        }
                    },
                    label = "destination",
                ) { dest ->
                    if (dest == null) {
                        TabContent(
                            tab = tab,
                            padding = padding,
                            appViewModel = appViewModel,
                            todos = todos,
                            collections = collections,
                            schedules = schedules,
                            sessions = sessions,
                            wishesCount = wishes.size,
                            settings = settings,
                            onSelectTab = { selected -> tab = selected },
                            onStartTodo = { todo ->
                                timerViewModel.startFocus(
                                    todoId = todo.id,
                                    title = todo.title,
                                    minutes = todo.minutes,
                                    art = todo.art,
                                    strict = settings.strictMode,
                                    mode = todo.timerMode,
                                    cycleEnabled = settings.cycleEnabled,
                                    cycleStudyMinutes = settings.cycleStudyMinutes,
                                    cycleBreakMinutes = settings.cycleBreakMinutes,
                                    cycleTotal = settings.cycleCount,
                                    cycleInfinite = settings.cycleInfinite,
                                )
                            },
                            onStartFreeFocus = { minutes ->
                                timerViewModel.startFreeFocus(
                                    minutes = minutes,
                                    strict = settings.strictMode,
                                    cycleEnabled = settings.cycleEnabled,
                                    cycleStudyMinutes = settings.cycleStudyMinutes,
                                    cycleBreakMinutes = settings.cycleBreakMinutes,
                                    cycleTotal = settings.cycleCount,
                                    cycleInfinite = settings.cycleInfinite,
                                )
                            },
                            onStartNap = { minutes -> timerViewModel.startNap(minutes) },
                            onStartLock = { minutes -> timerViewModel.startLock(minutes) },
                            onOpen = ::open,
                        )
                    } else {
                        DetailContent(
                            dest = dest,
                            padding = padding,
                            appViewModel = appViewModel,
                            settings = settings,
                            sessions = sessions,
                            wishes = wishes,
                            countdowns = countdowns,
                            onBack = ::back,
                            onOpen = ::open,
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = current == null && timer == null,
            enter = fadeIn(tween(TokiMotion.standard)),
            exit = fadeOut(tween(TokiMotion.quick)),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            FloatingNavBar(
                current = tab,
                onSelect = { selected -> tab = selected },
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                glass = glass,
                labels = settings.navLabels,
            )
        }

        timer?.let { active ->
            TimerScreen(
                timer = active,
                showLockCountdown = settings.lockShowCountdown,
                allowEmergencyUnlock = settings.lockAllowEmergency,
                keepAwakeDefault = settings.keepScreenAwake,
                onKeepAwakeChange = { value ->
                    appViewModel.updateSettings { it.copy(keepScreenAwake = value) }
                },
                onPause = timerViewModel::startPauseCountdown,
                onResume = timerViewModel::resumeFromPause,
                onDismissPauseReminder = timerViewModel::dismissPauseReminder,
                onAbandon = timerViewModel::abandon,
                onFinishEarly = timerViewModel::finishEarly,
                onSkipRest = timerViewModel::skipRest,
                onCycleChanged = { enabled, study, breakMinutes, count, infinite ->
                    appViewModel.updateSettings {
                        it.copy(
                            cycleEnabled = enabled,
                            cycleStudyMinutes = study,
                            cycleBreakMinutes = breakMinutes,
                            cycleCount = count,
                            cycleInfinite = infinite,
                        )
                    }
                    timerViewModel.updateCycle(enabled, study, breakMinutes, count, infinite)
                },
            )
        }
    }

    completion?.let { info ->
        CompletionScreen(
            info = info,
            onHome = {
                completion = null
                stack.clear()
                tab = Tab.Todos
            },
            onHistory = {
                completion = null
                stack.clear()
                tab = Tab.Profile
                stack.add(Dest.History)
            },
            onAutoHome = {
                completion = null
                stack.clear()
                tab = Tab.Todos
            },
        )
    }

    // 从「其他设置」导出备份时，用系统分享面板把 JSON 交给用户自选位置。
}

@Composable
private fun TabContent(
    tab: Tab,
    padding: PaddingValues,
    appViewModel: AppViewModel,
    todos: List<Todo>,
    collections: List<TodoCollection>,
    schedules: List<LockSchedule>,
    sessions: List<FocusSession>,
    wishesCount: Int,
    settings: AppSettings,
    onSelectTab: (Tab) -> Unit,
    onStartTodo: (Todo) -> Unit,
    onStartFreeFocus: (Int) -> Unit,
    onStartNap: (Int) -> Unit,
    onStartLock: (Int) -> Unit,
    onOpen: (Dest) -> Unit,
) {
    when (tab) {
        Tab.Todos -> TodosScreen(
            todos = todos,
            collections = collections,
            settings = settings,
            sessions = sessions,
            contentPadding = padding,
            onSaveTodo = { todo ->
                if (todos.any { it.id == todo.id }) {
                    appViewModel.updateTodo(todo)
                } else {
                    appViewModel.addTodo(
                        title = todo.title,
                        minutes = todo.minutes,
                        art = todo.art,
                        collectionId = todo.collectionId,
                        note = todo.note,
                    )
                }
            },
            onDeleteTodo = appViewModel::deleteTodo,
            onToggleDone = appViewModel::toggleDone,
            onStartTodo = onStartTodo,
            onStartFreeFocus = onStartFreeFocus,
            onOpenStudyMode = { onOpen(Dest.StudyMode) },
            onOpenStats = { onSelectTab(Tab.Stats) },
            onOpenCollections = { onSelectTab(Tab.Collections) },
            onClearCompleted = appViewModel::clearCompleted,
            onCreateCollection = { name ->
                appViewModel.addCollection(name, appViewModel.collections.value.size % 6)
            },
            onUpdateSettings = appViewModel::updateSettings,
            onOpenHistory = { onOpen(Dest.History) },
        )

        Tab.Collections -> CollectionsScreen(
            collections = collections,
            todos = todos,
            studyMode = settings.studyMode || settings.strictMode,
            contentPadding = padding,
            onOpenStudyMode = { onOpen(Dest.StudyMode) },
            onAddCollection = { name, tint ->
                appViewModel.addCollection(name, tint)
            },
            onUpdateCollection = appViewModel::updateCollection,
            onDeleteCollection = appViewModel::deleteCollection,
            onStartTodo = onStartTodo,
            onStartFreeFocus = onStartFreeFocus,
        )

        Tab.Lock -> LockScreen(
            schedules = schedules,
            settings = settings,
            contentPadding = padding,
            onStartNap = onStartNap,
            onStartLock = onStartLock,
            onAddSchedule = appViewModel::addSchedule,
            onToggleSchedule = appViewModel::setScheduleEnabled,
            onDeleteSchedule = appViewModel::deleteSchedule,
            onUpdateSettings = appViewModel::updateSettings,
        )

        Tab.Stats -> StatsScreen(
            sessions = sessions,
            settings = settings,
            contentPadding = padding,
            onOpenHistory = { onOpen(Dest.History) },
            onOpenTimerSettings = { onOpen(Dest.Detail(SettingsPage.Timer)) },
            onGoTodos = { onSelectTab(Tab.Todos) },
            onClearSessions = appViewModel::clearSessions,
        )

        Tab.Profile -> ProfileScreen(
            settings = settings,
            sessions = sessions,
            wishesCount = wishesCount,
            contentPadding = padding,
            onOpenPage = { page -> onOpen(Dest.Detail(page)) },
            onOpenHistory = { onOpen(Dest.History) },
            onOpenCountdown = { onOpen(Dest.Countdown) },
            onOpenWishWall = { onOpen(Dest.WishWall) },
            onOpenVip = { onOpen(Dest.Vip) },
            onUpdateSettings = appViewModel::updateSettings,
        )
    }
}

@Composable
private fun DetailContent(
    dest: Dest,
    padding: PaddingValues,
    appViewModel: AppViewModel,
    settings: AppSettings,
    sessions: List<FocusSession>,
    wishes: List<Wish>,
    countdowns: List<CountdownEvent>,
    onBack: () -> Unit,
    onOpen: (Dest) -> Unit,
) {
    when (dest) {
        is Dest.Detail -> SettingsDetailScreen(
            page = dest.page,
            settings = settings,
            contentPadding = padding,
            onBack = onBack,
            onUpdate = appViewModel::updateSettings,
            onClearSessions = appViewModel::clearSessions,
            onClearAll = appViewModel::clearAllData,
            exportJson = appViewModel::exportJson,
            onImport = appViewModel::importJson,
            onOpenHistory = { onOpen(Dest.History) },
        )

        Dest.History -> HistoryCenterScreen(
            sessions = sessions,
            contentPadding = padding,
            onBack = onBack,
            onUpdateSession = appViewModel::updateSession,
            onDeleteSession = appViewModel::deleteSession,
            onClearSessions = appViewModel::clearSessions,
        )

        Dest.Countdown -> CountdownScreen(
            events = countdowns,
            contentPadding = padding,
            onBack = onBack,
            onAdd = appViewModel::addCountdown,
            onDelete = appViewModel::deleteCountdown,
        )

        Dest.WishWall -> WishWallScreen(
            wishes = wishes,
            contentPadding = padding,
            onBack = onBack,
            onAdd = appViewModel::addWish,
            onToggle = appViewModel::toggleWish,
            onDelete = appViewModel::deleteWish,
        )

        Dest.Vip -> VipScreen(
            contentPadding = padding,
            onBack = onBack,
            onGoTodos = onBack,
        )

        Dest.StudyMode -> {
            Box(Modifier.fillMaxSize().padding(padding))
            ForceStudySheet(
                settings = settings,
                onDismiss = onBack,
                onUpdate = appViewModel::updateSettings,
                onOpenEnhancements = {
                    onBack()
                    onOpen(Dest.ForceEnhancements)
                },
                onOpenWhitelist = {
                    onBack()
                    onOpen(Dest.Whitelist)
                },
            )
        }

        Dest.ForceEnhancements -> ForceEnhancementsScreen(
            settings = settings,
            contentPadding = padding,
            onBack = onBack,
            onUpdate = appViewModel::updateSettings,
        )

        Dest.Whitelist -> WhitelistScreen(
            settings = settings,
            contentPadding = padding,
            onBack = onBack,
            onToggle = appViewModel::toggleWhitelistPackage,
        )

        else -> SettingsDetailScreen(
            page = SettingsPage.Timer,
            settings = settings,
            contentPadding = padding,
            onBack = onBack,
            onUpdate = appViewModel::updateSettings,
            onClearSessions = appViewModel::clearSessions,
            onClearAll = appViewModel::clearAllData,
            exportJson = appViewModel::exportJson,
            onImport = appViewModel::importJson,
            onOpenHistory = { onOpen(Dest.History) },
        )
    }
}
