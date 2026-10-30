package com.tokitodo.app.ui.todos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tokitodo.app.data.AppSettings
import com.tokitodo.app.data.FocusSession
import com.tokitodo.app.data.Stats
import com.tokitodo.app.data.Todo
import com.tokitodo.app.data.TodoCollection
import com.tokitodo.app.ui.components.CircleAction
import com.tokitodo.app.ui.components.EmptyState
import com.tokitodo.app.ui.components.PageTopBar
import com.tokitodo.app.ui.components.SheetGrabber
import com.tokitodo.app.ui.components.StudyModeChip
import com.tokitodo.app.ui.theme.SheetShape
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodosScreen(
    todos: List<Todo>,
    collections: List<TodoCollection>,
    settings: AppSettings,
    sessions: List<FocusSession>,
    contentPadding: PaddingValues,
    onSaveTodo: (Todo) -> Unit,
    onDeleteTodo: (String) -> Unit,
    onToggleDone: (String) -> Unit,
    onStartTodo: (Todo) -> Unit,
    onStartFreeFocus: (Int) -> Unit,
    onOpenStudyMode: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenCollections: () -> Unit,
    onClearCompleted: () -> Unit,
    onCreateCollection: (String) -> TodoCollection,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenTodoStats: (Todo) -> Unit,
) {
    var editing by remember { mutableStateOf<Todo?>(null) }
    var editorOpen by remember { mutableStateOf(false) }
    var quickFocusOpen by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    val quickSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val today = LocalDate.now()
    val todayMinutes = Stats.minutesOn(sessions, today)
    val active = todos.filterNot { it.done }
    val completed = todos.filter { it.done }
    val collectionsById = collections.associateBy { it.id }
    val listState = rememberLazyListState()
    LaunchedEffect(completed.size) {
        if (completed.isNotEmpty()) {
            runCatching { listState.scrollToItem(0) }
        }
    }

    fun openEditor(todo: Todo?) {
        editing = todo
        editorOpen = true
    }

    Column(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
        PageTopBar(
            title = "待办",
            subtitle = "今天，把注意力留给重要的事",
            chip = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StudyModeChip(
                        enabled = settings.studyMode || settings.strictMode,
                        onClick = onOpenStudyMode,
                    )
                    Spacer(Modifier.weight(1f))
                    TodayPill(minutes = todayMinutes, onClick = onOpenStats)
                }
            },
            actions = {
                CircleAction(
                    icon = Icons.Outlined.Timer,
                    contentDescription = "快速专注",
                    onClick = { quickFocusOpen = true },
                )
                CircleAction(
                    icon = Icons.Outlined.Add,
                    contentDescription = "添加待办",
                    onClick = { openEditor(null) },
                )
                Box {
                    CircleAction(
                        icon = Icons.Outlined.MoreVert,
                        contentDescription = "更多",
                        onClick = { menuOpen = true },
                    )
                    TodoMoreMenu(
                        expanded = menuOpen,
                        onDismiss = { menuOpen = false },
                        hasCompleted = completed.isNotEmpty(),
                        onStudyMode = onOpenStudyMode,
                        onStats = onOpenStats,
                        onCollections = onOpenCollections,
                        onClearCompleted = onClearCompleted,
                    )
                }
            },
        )

        if (todos.isEmpty()) {
            Spacer(Modifier.height(70.dp))
            EmptyState(
                icon = Icons.Outlined.Checklist,
                title = "还没有待办，添加一件要专注的事吧",
                hint = "点击右上角 + 号，给它一个时长和一张封面",
                actionLabel = "添加第一件待办",
                onAction = { openEditor(null) },
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(
                    start = 24.dp,
                    end = 24.dp,
                    top = 18.dp,
                    bottom = 148.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(active, key = { it.id }) { todo ->
                    TodoCard(
                        todo = todo,
                        collectionName = todo.collectionId?.let { collectionsById[it]?.name },
                        onClick = { onStartTodo(todo) },
                        onLongClick = { openEditor(todo) },
                        onToggleDone = { onToggleDone(todo.id) },
                    )
                }
                if (completed.isNotEmpty()) {
                    item(key = "completed-header") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, start = 4.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "已完成 · ${completed.size}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = "已经做过的事",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            )
                        }
                    }
                    items(completed, key = { it.id }) { todo ->
                        TodoCard(
                            todo = todo,
                            collectionName = todo.collectionId?.let { collectionsById[it]?.name },
                            onClick = { onStartTodo(todo) },
                            onLongClick = { openEditor(todo) },
                            onToggleDone = { onToggleDone(todo.id) },
                        )
                    }
                }
            }
        }
    }

    if (editorOpen) {
        TodoEditorSheet(
            initial = editing,
            collections = collections,
            settings = settings,
            defaultMinutes = settings.focusMinutes,
            onDismiss = { editorOpen = false },
            onSave = { todo ->
                onSaveTodo(todo)
                editorOpen = false
            },
            onDelete = { id ->
                onDeleteTodo(id)
                editorOpen = false
            },
            onCreateCollection = onCreateCollection,
            onUpdateSettings = onUpdateSettings,
            onOpenHistory = {
                editorOpen = false
                onOpenHistory()
            },
            onOpenStats = { todo ->
                editorOpen = false
                onOpenTodoStats(todo)
            },
        )
    }

    if (quickFocusOpen) {
        ModalBottomSheet(
            onDismissRequest = { quickFocusOpen = false },
            sheetState = quickSheetState,
            shape = SheetShape,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            dragHandle = { SheetGrabber() },
        ) {
            QuickFocusSheet(
                defaultMinutes = settings.focusMinutes,
                onStart = { minutes ->
                    quickFocusOpen = false
                    onStartFreeFocus(minutes)
                },
            )
        }
    }
}

@Composable
private fun TodayPill(minutes: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(start = 10.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.LocalFireDepartment,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "今日 $minutes 分钟",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(13.dp),
        )
    }
}

@Composable
private fun TodoMoreMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    hasCompleted: Boolean,
    onStudyMode: () -> Unit,
    onStats: () -> Unit,
    onCollections: () -> Unit,
    onClearCompleted: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.medium,
    ) {
        DropdownMenuItem(
            text = { Text("强制学习模式") },
            leadingIcon = { Icon(Icons.Rounded.AutoAwesome, null, modifier = Modifier.size(20.dp)) },
            onClick = {
                onDismiss()
                onStudyMode()
            },
        )
        DropdownMenuItem(
            text = { Text("统计数据") },
            leadingIcon = { Icon(Icons.Outlined.Timer, null, modifier = Modifier.size(20.dp)) },
            onClick = {
                onDismiss()
                onStats()
            },
        )
        DropdownMenuItem(
            text = { Text("管理待办集") },
            leadingIcon = { Icon(Icons.Outlined.Checklist, null, modifier = Modifier.size(20.dp)) },
            onClick = {
                onDismiss()
                onCollections()
            },
        )
        if (hasCompleted) {
            DropdownMenuItem(
                text = { Text("清除已完成") },
                onClick = {
                    onDismiss()
                    onClearCompleted()
                },
            )
        }
    }
}

@Composable
private fun QuickFocusSheet(
    defaultMinutes: Int,
    onStart: (Int) -> Unit,
) {
    val options = listOf(15, 25, 45, 60, 90)
    var minutes by remember {
        mutableIntStateOf(if (defaultMinutes in options) defaultMinutes else 25)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 28.dp),
    ) {
        Text(
            text = "快速专注",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "不绑定待办，直接开始一段专注。结束后会记进统计数据。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { option ->
                val selected = option == minutes
                Text(
                    text = "$option 分钟",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerLow
                            },
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { minutes = option }
                        .padding(horizontal = 13.dp, vertical = 9.dp),
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { onStart(minutes) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Icon(Icons.Outlined.Timer, null, modifier = Modifier.size(19.dp))
            Spacer(Modifier.width(8.dp))
            Text("开始 $minutes 分钟", style = MaterialTheme.typography.titleSmall)
        }
    }
}
