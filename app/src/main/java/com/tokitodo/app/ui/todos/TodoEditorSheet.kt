package com.tokitodo.app.ui.todos

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tokitodo.app.data.AppSettings
import com.tokitodo.app.data.HabitPeriod
import com.tokitodo.app.data.Todo
import com.tokitodo.app.data.TodoCollection
import com.tokitodo.app.data.TodoTimerMode
import com.tokitodo.app.data.TodoType
import com.tokitodo.app.ui.components.CircleAction
import com.tokitodo.app.ui.theme.TodoArt
import java.time.LocalDate
import java.util.UUID

/**
 * 悬浮式待办编辑器。主页面只有标题、封面、待办集和两个入口按钮；
 * 时长 / 计时方式 / 目标 / 习惯都在「设置」子页里完成。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoEditorSheet(
    initial: Todo?,
    collections: List<TodoCollection>,
    settings: AppSettings,
    defaultMinutes: Int,
    onDismiss: () -> Unit,
    onSave: (Todo) -> Unit,
    onDelete: (String) -> Unit,
    onCreateCollection: (String) -> TodoCollection,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onOpenHistory: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var page by remember { mutableStateOf(0) }
    var title by remember(initial) { mutableStateOf(initial?.title.orEmpty()) }
    var minutes by remember(initial) {
        mutableIntStateOf((initial?.minutes ?: defaultMinutes).coerceIn(1, 1440))
    }
    var art by remember(initial) { mutableStateOf(initial?.art ?: TodoArt.Sky) }
    var collectionId by remember(initial) { mutableStateOf(initial?.collectionId) }
    var note by remember(initial) { mutableStateOf(initial?.note.orEmpty()) }
    var coverPath by remember(initial) { mutableStateOf(initial?.coverPath) }
    var timerMode by remember(initial) { mutableStateOf(initial?.timerMode ?: TodoTimerMode.CountDown) }
    var type by remember(initial) { mutableStateOf(initial?.type ?: TodoType.Pomodoro) }
    var goalDateEpochDay by remember(initial) {
        mutableStateOf(initial?.goalDateEpochDay ?: LocalDate.now().plusDays(30).toEpochDay())
    }
    var goalHoursText by remember(initial) {
        mutableStateOf(if ((initial?.goalHours ?: 0f) > 0f) initial!!.goalHours.toString() else "20")
    }
    var habitPeriod by remember(initial) { mutableStateOf(initial?.habitPeriod ?: HabitPeriod.Daily) }
    var habitHoursText by remember(initial) {
        mutableStateOf(if ((initial?.habitHours ?: 0f) > 0f) initial!!.habitHours.toString() else "1")
    }
    var presetEditIndex by remember { mutableIntStateOf(-1) }
    var presetEditValue by remember { mutableStateOf("") }
    var confirmDelete by remember { mutableStateOf(false) }
    var newCollectionOpen by remember { mutableStateOf(false) }
    var newCollectionName by remember { mutableStateOf("") }
    var cropUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val coverPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) cropUri = uri
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        scrimColor = Color.Black.copy(alpha = 0.5f),
        dragHandle = null,
        shape = RoundedCornerShape(0.dp),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 14.dp)
                .imePadding(),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            shadowElevation = 12.dp,
        ) {
            if (page == 0) {
                EditorMain(
                    title = title,
                    onTitleChange = { title = it.take(60) },
                    coverPath = coverPath,
                    art = art,
                    onPickCover = {
                        coverPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    onClose = onDismiss,
                    collections = collections,
                    collectionId = collectionId,
                    onCollectionChange = { collectionId = it },
                    onCreateCollection = { newCollectionOpen = true },
                    note = note,
                    onNoteChange = { note = it.take(200) },
                    onOpenSettings = { page = 1 },
                    onOpenHistory = onOpenHistory,
                    onSave = {
                        val now = System.currentTimeMillis()
                        onSave(
                            (initial ?: Todo(
                                id = UUID.randomUUID().toString(),
                                title = "",
                                createdAt = now,
                            )).copy(
                                title = title.trim(),
                                minutes = minutes.coerceIn(1, 1440),
                                art = art,
                                collectionId = collectionId,
                                note = note.trim(),
                                timerMode = timerMode,
                                type = type,
                                goalDateEpochDay = if (type == TodoType.Goal) goalDateEpochDay else null,
                                goalHours = goalHoursText.toFloatOrNull()?.coerceIn(0f, 9999f) ?: 0f,
                                habitPeriod = habitPeriod,
                                habitHours = habitHoursText.toFloatOrNull()?.coerceIn(0f, 9999f) ?: 0f,
                                coverPath = coverPath,
                            ),
                        )
                    },
                    canSave = title.isNotBlank(),
                )
            } else {
                EditorSettings(
                    minutes = minutes,
                    onMinutesChange = { minutes = it },
                    presets = settings.durationPresets,
                    timerMode = timerMode,
                    onTimerModeChange = { timerMode = it },
                    type = type,
                    onTypeChange = { type = it },
                    goalDateEpochDay = goalDateEpochDay,
                    onGoalDateChange = { goalDateEpochDay = it },
                    goalHoursText = goalHoursText,
                    onGoalHoursChange = { goalHoursText = it },
                    habitPeriod = habitPeriod,
                    onHabitPeriodChange = { habitPeriod = it },
                    habitHoursText = habitHoursText,
                    onHabitHoursChange = { habitHoursText = it },
                    art = art,
                    coverPath = coverPath,
                    onArtChange = {
                        art = it
                        coverPath = null
                    },
                    onBack = { page = 0 },
                    onDelete = if (initial != null) {
                        { confirmDelete = true }
                    } else {
                        null
                    },
                    onEditPreset = { index ->
                        presetEditIndex = index
                        presetEditValue = settings.durationPresets.getOrNull(index)?.toString() ?: ""
                    },
                )
            }
        }
    }

    if (cropUri != null) {
        CoverCropDialog(
            uri = cropUri!!,
            onDismiss = { cropUri = null },
            onCropped = { path ->
                coverPath = path
                cropUri = null
            },
        )
    }

    if (newCollectionOpen) {
        AlertDialog(
            onDismissRequest = { newCollectionOpen = false },
            title = { Text("新建待办集") },
            text = {
                OutlinedTextField(
                    value = newCollectionName,
                    onValueChange = { newCollectionName = it.take(20) },
                    placeholder = { Text("例如：期末复习") },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val collection = onCreateCollection(newCollectionName)
                        collectionId = collection.id
                        newCollectionName = ""
                        newCollectionOpen = false
                    },
                ) {
                    Text("创建")
                }
            },
            dismissButton = {
                TextButton(onClick = { newCollectionOpen = false }) { Text("取消") }
            },
        )
    }

    if (confirmDelete && initial != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("删除这件待办？") },
            text = { Text("专注记录会保留在统计数据里，但待办本身不能恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        onDelete(initial.id)
                    },
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("取消") }
            },
        )
    }

    if (presetEditIndex >= 0) {
        AlertDialog(
            onDismissRequest = { presetEditIndex = -1 },
            title = { Text("修改时长预设") },
            text = {
                Column {
                    Text(
                        text = "最长 24 小时（1440 分钟）",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = presetEditValue,
                        onValueChange = { value ->
                            presetEditValue = value.filter { it.isDigit() }.take(4)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        suffix = { Text("分钟") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val index = presetEditIndex
                        val value = presetEditValue.toIntOrNull()?.coerceIn(1, 1440)
                        if (index >= 0 && value != null) {
                            val list = settings.durationPresets.toMutableList()
                            if (index < list.size) list[index] = value
                            onUpdateSettings { it.copy(durationPresets = list) }
                        }
                        presetEditIndex = -1
                    },
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { presetEditIndex = -1 }) { Text("取消") }
            },
        )
    }
}

@Composable
private fun EditorMain(
    title: String,
    onTitleChange: (String) -> Unit,
    coverPath: String?,
    art: TodoArt,
    onPickCover: () -> Unit,
    onClose: () -> Unit,
    collections: List<TodoCollection>,
    collectionId: String?,
    onCollectionChange: (String?) -> Unit,
    onCreateCollection: () -> Unit,
    note: String,
    onNoteChange: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    onSave: () -> Unit,
    canSave: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            BasicTextField(
                value = title,
                onValueChange = onTitleChange,
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                maxLines = 2,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 6.dp),
                decorationBox = { inner ->
                    Box {
                        if (title.isBlank()) {
                            Text(
                                text = "待办的标题",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                            )
                        }
                        inner()
                    }
                },
            )
            Spacer(Modifier.width(8.dp))
            CoverButton(
                coverPath = coverPath,
                art = art,
                onClick = onPickCover,
            )
            Spacer(Modifier.width(8.dp))
            CircleAction(
                icon = Icons.Rounded.Close,
                contentDescription = "关闭",
                hint = "关闭编辑器",
                onClick = onClose,
                size = 42,
                container = MaterialTheme.colorScheme.surfaceContainerLow,
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(
            text = "待办集",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ChoicePill(
                label = "不加入",
                selected = collectionId == null,
                onClick = { onCollectionChange(null) },
            )
            collections.forEach { collection ->
                ChoicePill(
                    label = collection.name,
                    selected = collectionId == collection.id,
                    onClick = { onCollectionChange(collection.id) },
                )
            }
            ChoicePill(
                label = "＋ 新建",
                selected = false,
                onClick = onCreateCollection,
            )
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = note,
            onValueChange = onNoteChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("备注（可选）") },
            textStyle = MaterialTheme.typography.bodyMedium,
            shape = RoundedCornerShape(18.dp),
            minLines = 2,
            maxLines = 4,
        )
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(
                onClick = onOpenSettings,
                shape = CircleShape,
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Rounded.Tune, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(6.dp))
                Text("设置", style = MaterialTheme.typography.labelLarge)
            }
            FilledTonalButton(
                onClick = onOpenHistory,
                shape = CircleShape,
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Rounded.History, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(6.dp))
                Text("专注历史", style = MaterialTheme.typography.labelLarge)
            }
        }
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = onSave,
            enabled = canSave,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Text("保存待办", style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun CoverButton(coverPath: String?, art: TodoArt, onClick: () -> Unit) {
    var hintOpen by remember { mutableStateOf(false) }
    LaunchedEffect(hintOpen) {
        if (hintOpen) {
            kotlinx.coroutines.delay(1800)
            hintOpen = false
        }
    }
    Box {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape,
                )
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = androidx.compose.material3.ripple(bounded = true),
                    onClick = onClick,
                    onLongClick = { hintOpen = true },
                ),
            contentAlignment = Alignment.Center,
        ) {
            CoverArt(
                art = art,
                coverPath = coverPath,
                modifier = Modifier.fillMaxSize(),
                scrim = false,
            )
            if (coverPath == null) {
                Icon(
                    Icons.Rounded.Image,
                    contentDescription = "选择封面",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        DropdownMenu(
            expanded = hintOpen,
            onDismissRequest = { hintOpen = false },
            shape = MaterialTheme.shapes.small,
            containerColor = MaterialTheme.colorScheme.inverseSurface,
        ) {
            Text(
                text = "选择封面图片，可裁剪",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.inverseOnSurface,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun EditorSettings(
    minutes: Int,
    onMinutesChange: (Int) -> Unit,
    presets: List<Int>,
    timerMode: TodoTimerMode,
    onTimerModeChange: (TodoTimerMode) -> Unit,
    type: TodoType,
    onTypeChange: (TodoType) -> Unit,
    goalDateEpochDay: Long,
    onGoalDateChange: (Long) -> Unit,
    goalHoursText: String,
    onGoalHoursChange: (String) -> Unit,
    habitPeriod: HabitPeriod,
    onHabitPeriodChange: (HabitPeriod) -> Unit,
    habitHoursText: String,
    onHabitHoursChange: (String) -> Unit,
    art: TodoArt,
    coverPath: String?,
    onArtChange: (TodoArt) -> Unit,
    onBack: () -> Unit,
    onDelete: (() -> Unit)?,
    onEditPreset: (Int) -> Unit,
) {
    var customText by remember { mutableStateOf("") }
    var datePickerOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 16.dp)
            .imePadding(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircleAction(
                icon = Icons.Rounded.ArrowBack,
                contentDescription = "返回",
                hint = "返回待办",
                onClick = onBack,
                size = 40,
                container = MaterialTheme.colorScheme.surfaceContainerLow,
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "待办设置",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(
            text = "时长",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TodoTimerMode.entries.forEach { mode ->
                ChoicePill(
                    label = mode.label,
                    selected = timerMode == mode,
                    onClick = { onTimerModeChange(mode) },
                )
            }
        }
        if (timerMode == TodoTimerMode.CountDown) {
            Spacer(Modifier.height(14.dp))
            Text(
                text = "预设时长（长按可修改）",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                presets.forEachIndexed { index, preset ->
                    val selected = preset == minutes
                    Text(
                        text = "$preset 分钟",
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
                            .combinedClickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = androidx.compose.material3.ripple(),
                                onClick = { onMinutesChange(preset) },
                                onLongClick = { onEditPreset(index) },
                            )
                            .padding(horizontal = 13.dp, vertical = 9.dp),
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = if (minutes !in presets) minutes.toString() else customText,
                onValueChange = { value ->
                    customText = value.filter { it.isDigit() }.take(4)
                    customText.toIntOrNull()?.let { onMinutesChange(it.coerceIn(1, 1440)) }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("自定义分钟（1–1440）") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(
            text = "类型",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TodoType.entries.forEach { item ->
                ChoicePill(
                    label = item.label,
                    selected = type == item,
                    onClick = { onTypeChange(item) },
                )
            }
        }
        if (type == TodoType.Goal) {
            Spacer(Modifier.height(14.dp))
            GoalEditor(
                epochDay = goalDateEpochDay,
                hours = goalHoursText,
                onHoursChange = onGoalHoursChange,
                onPickDate = { datePickerOpen = true },
            )
        }
        if (type == TodoType.Habit) {
            Spacer(Modifier.height(14.dp))
            HabitEditor(
                period = habitPeriod,
                onPeriodChange = onHabitPeriodChange,
                hours = habitHoursText,
                onHoursChange = onHabitHoursChange,
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(
            text = "封面插画",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TodoArt.entries.forEach { item ->
                val selected = coverPath == null && art == item
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerLow
                            },
                        )
                        .clickable { onArtChange(item) }
                        .padding(horizontal = 11.dp, vertical = 8.dp),
                )
            }
        }
        if (onDelete != null) {
            Spacer(Modifier.height(24.dp))
            OutlinedButton(
                onClick = onDelete,
                shape = CircleShape,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    Icons.Rounded.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(17.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text("删除这件待办", color = MaterialTheme.colorScheme.error)
            }
        }
        Spacer(Modifier.height(8.dp))
    }

    if (datePickerOpen) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = goalDateEpochDay * 86_400_000L,
            selectableDates = object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val today = LocalDate.now().toEpochDay() * 86_400_000L
                    return utcTimeMillis >= today
                }
            },
        )
        DatePickerDialog(
            onDismissRequest = { datePickerOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { millis ->
                            onGoalDateChange(LocalDate.ofEpochDay(millis / 86_400_000L).toEpochDay())
                        }
                        datePickerOpen = false
                    },
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { datePickerOpen = false }) { Text("取消") }
            },
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
private fun GoalEditor(
    epochDay: Long,
    hours: String,
    onHoursChange: (String) -> Unit,
    onPickDate: () -> Unit,
) {
    val date = LocalDate.ofEpochDay(epochDay)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(14.dp),
    ) {
        FlowRow(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("我想在", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "${date.monthValue} 月 ${date.dayOfMonth} 日",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onPickDate)
                    .padding(horizontal = 4.dp, vertical = 1.dp),
            )
            Text("之前共计完成", style = MaterialTheme.typography.bodyMedium)
            InlineNumberField(value = hours, onValueChange = onHoursChange)
            Text("小时", style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = "日期不可早于今天；目标会保存在待办里。",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HabitEditor(
    period: HabitPeriod,
    onPeriodChange: (HabitPeriod) -> Unit,
    hours: String,
    onHoursChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(14.dp),
    ) {
        FlowRow(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            Text("我想", style = MaterialTheme.typography.bodyMedium)
            HabitPeriod.entries.forEach { item ->
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (period == item) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (period == item) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                        )
                        .clickable { onPeriodChange(item) }
                        .padding(horizontal = 9.dp, vertical = 5.dp),
                )
            }
            Text("完成", style = MaterialTheme.typography.bodyMedium)
            InlineNumberField(value = hours, onValueChange = onHoursChange)
            Text("小时", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun InlineNumberField(value: String, onValueChange: (String) -> Unit) {
    BasicTextField(
        value = value,
        onValueChange = { text ->
            onValueChange(text.filter { it.isDigit() || it == '.' }.take(5))
        },
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier
            .width(52.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(horizontal = 6.dp, vertical = 3.dp),
    )
}

@Composable
private fun ChoicePill(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
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
                indication = androidx.compose.material3.ripple(),
                onClick = onClick,
            )
            .padding(horizontal = 13.dp, vertical = 9.dp),
    )
}
