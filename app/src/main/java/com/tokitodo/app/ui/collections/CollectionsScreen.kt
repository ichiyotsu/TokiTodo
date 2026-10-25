package com.tokitodo.app.ui.collections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tokitodo.app.data.Todo
import com.tokitodo.app.data.TodoCollection
import com.tokitodo.app.ui.components.CircleAction
import com.tokitodo.app.ui.components.EmptyState
import com.tokitodo.app.ui.components.GradientBar
import com.tokitodo.app.ui.components.PageTopBar
import com.tokitodo.app.ui.components.SheetGrabber
import com.tokitodo.app.ui.components.StudyModeChip
import com.tokitodo.app.ui.theme.SectionCardShape
import com.tokitodo.app.ui.theme.SheetShape
import com.tokitodo.app.ui.theme.collectionTint
import com.tokitodo.app.ui.theme.LocalDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    collections: List<TodoCollection>,
    todos: List<Todo>,
    studyMode: Boolean,
    contentPadding: PaddingValues,
    onOpenStudyMode: () -> Unit,
    onAddCollection: (String, Int) -> Unit,
    onUpdateCollection: (String, String, Int) -> Unit,
    onDeleteCollection: (String) -> Unit,
    onStartTodo: (Todo) -> Unit,
    onStartFreeFocus: (Int) -> Unit,
) {
    var showCreate by remember { mutableStateOf(false) }
    var quickOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<TodoCollection?>(null) }
    var showInfo by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var expandedId by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
        PageTopBar(
            chip = {
                StudyModeChip(enabled = studyMode, onClick = onOpenStudyMode)
            },
            actions = {
                CircleAction(
                    icon = Icons.Outlined.Timer,
                    contentDescription = "快速专注",
                    onClick = { quickOpen = true },
                )
                CircleAction(
                    icon = Icons.Rounded.Add,
                    contentDescription = "创建待办集",
                    onClick = { showCreate = true },
                )
                Box {
                    CircleAction(
                        icon = Icons.Rounded.MoreVert,
                        contentDescription = "更多",
                        onClick = { menuOpen = true },
                    )
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("什么是待办集？") },
                            onClick = {
                                menuOpen = false
                                showInfo = true
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("新建待办集") },
                            onClick = {
                                menuOpen = false
                                showCreate = true
                            },
                        )
                    }
                }
            },
        )

        if (collections.isEmpty()) {
            Spacer(Modifier.height(64.dp))
            EmptyState(
                icon = Icons.Outlined.MailOutline,
                title = "当前没有待办集，点击右上角 + 号创建一个吧",
                hint = "把同一类待办收在一起，例如「期末复习」或「生活小事」",
                actionLabel = "什么是待办集？",
                onAction = { showInfo = true },
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 18.dp,
                    bottom = 132.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(collections, key = { it.id }) { collection ->
                    val members = todos.filter { it.collectionId == collection.id }
                    val done = members.count { it.done }
                    val expanded = expandedId == collection.id
                    CollectionCard(
                        collection = collection,
                        members = members,
                        doneCount = done,
                        expanded = expanded,
                        onToggleExpand = {
                            expandedId = if (expanded) null else collection.id
                        },
                        onEdit = { editing = collection },
                        onStartTodo = onStartTodo,
                    )
                }
                item {
                    Text(
                        text = "待办集只保存待办本身，不会复制它们；删除待办集不会删掉其中的待办。",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 12.dp),
                    )
                }
            }
        }
    }

    if (showCreate) {
        CollectionEditorSheet(
            initial = null,
            onDismiss = { showCreate = false },
            onSave = { name, tint ->
                onAddCollection(name, tint)
                showCreate = false
            },
            onDelete = null,
        )
    }

    editing?.let { collection ->
        CollectionEditorSheet(
            initial = collection,
            onDismiss = { editing = null },
            onSave = { name, tint ->
                onUpdateCollection(collection.id, name, tint)
                editing = null
            },
            onDelete = {
                onDeleteCollection(collection.id)
                editing = null
            },
        )
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text("什么是待办集？") },
            text = {
                Text(
                    "待办集像一只贴了标签的盒子：把同一类待办放进去，主页仍然按你的顺序展示，" +
                        "但统计和回顾时可以按集查看。待办集只记录待办的 id，删除集子不会删除待办。",
                )
            },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) { Text("知道了") }
            },
        )
    }

    if (quickOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var minutes by remember { mutableIntStateOf(25) }
        ModalBottomSheet(
            onDismissRequest = { quickOpen = false },
            sheetState = sheetState,
            shape = SheetShape,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            dragHandle = { SheetGrabber() },
        ) {
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
                    listOf(15, 25, 45, 60, 90).forEach { option ->
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
                                .clickable { minutes = option }
                                .padding(horizontal = 13.dp, vertical = 9.dp),
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        quickOpen = false
                        onStartFreeFocus(minutes)
                    },
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
    }
}

@Composable
private fun CollectionCard(
    collection: TodoCollection,
    members: List<Todo>,
    doneCount: Int,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onEdit: () -> Unit,
    onStartTodo: (Todo) -> Unit,
) {
    val dark = LocalDarkTheme.current
    val tint = collectionTint(collection.tintIndex, dark)
    var menuOpen by remember { mutableStateOf(false) }
    val total = members.size
    val progress = if (total == 0) 0f else doneCount.toFloat() / total

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = SectionCardShape,
        color = tint,
        shadowElevation = 0.dp,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = collection.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = when {
                            total == 0 -> "集里还没有待办"
                            doneCount == total -> "$total 件全部完成"
                            else -> "$doneCount / $total 已完成"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(
                            imageVector = Icons.Rounded.MoreHoriz,
                            contentDescription = "待办集更多操作",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("重命名 / 改颜色") },
                            leadingIcon = { Icon(Icons.Rounded.Edit, null, Modifier.size(19.dp)) },
                            onClick = {
                                menuOpen = false
                                onEdit()
                            },
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            GradientBar(
                progress = progress,
                colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.tertiary,
                ),
                track = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
            )
            if (members.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onToggleExpand)
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (expanded) "收起待办" else "查看 ${members.size} 件待办",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = if (expanded) "收起" else "展开",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                AnimatedVisibility(visible = expanded) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        members.forEach { todo ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onStartTodo(todo) }
                                    .padding(vertical = 9.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.PlayArrow,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(15.dp),
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = todo.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    text = "${todo.minutes}′",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollectionEditorSheet(
    initial: TodoCollection?,
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit,
    onDelete: (() -> Unit)?,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember(initial) { mutableStateOf(initial?.name.orEmpty()) }
    var tintIndex by remember(initial) { mutableIntStateOf(initial?.tintIndex ?: 0) }
    var confirmDelete by remember { mutableStateOf(false) }
    val dark = LocalDarkTheme.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = SheetShape,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        dragHandle = { SheetGrabber() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
        ) {
            Text(
                text = if (initial == null) "新建待办集" else "编辑待办集",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(20) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("给这个集子起个名字") },
                textStyle = MaterialTheme.typography.titleMedium,
                shape = RoundedCornerShape(18.dp),
                singleLine = true,
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = "底色",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(6) { index ->
                    val selected = tintIndex == index
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(collectionTint(index, dark))
                            .then(
                                if (selected) {
                                    Modifier.background(Color.Transparent)
                                } else {
                                    Modifier
                                },
                            )
                            .clickable { tintIndex = index },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(26.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onDelete != null) {
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(
                            Icons.Rounded.DeleteOutline,
                            contentDescription = "删除待办集",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onDismiss) { Text("取消") }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { onSave(name.trim().ifBlank { "新待办集" }, tintIndex) },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                    modifier = Modifier.height(46.dp),
                ) {
                    Text(
                        text = if (initial == null) "创建" else "保存",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 10.dp),
                    )
                }
            }
        }
    }

    if (confirmDelete && onDelete != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("删除这个待办集？") },
            text = { Text("集子里的待办会回到「未分类」，不会被删除。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        onDelete()
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
}
