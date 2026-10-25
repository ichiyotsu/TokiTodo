package com.tokitodo.app.ui.todos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tokitodo.app.data.Todo
import com.tokitodo.app.data.TodoCollection
import com.tokitodo.app.ui.components.Artwork
import com.tokitodo.app.ui.components.SheetGrabber
import com.tokitodo.app.ui.theme.SheetShape
import com.tokitodo.app.ui.theme.TodoArt
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoEditorSheet(
    initial: Todo?,
    collections: List<TodoCollection>,
    defaultMinutes: Int,
    onDismiss: () -> Unit,
    onSave: (Todo) -> Unit,
    onDelete: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember(initial) { mutableStateOf(initial?.title.orEmpty()) }
    var minutes by remember(initial) { mutableIntStateOf(initial?.minutes ?: defaultMinutes) }
    var art by remember(initial) { mutableStateOf(initial?.art ?: TodoArt.Sky) }
    var collectionId by remember(initial) { mutableStateOf(initial?.collectionId) }
    var note by remember(initial) { mutableStateOf(initial?.note.orEmpty()) }
    var confirmDelete by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val durationOptions = listOf(5, 10, 15, 25, 45, 60, 90)

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
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (initial == null) "新建待办" else "编辑待办",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "给它一个时长，开始后就是一段完整的专注",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "关闭")
                }
            }
            Spacer(Modifier.height(18.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it.take(60) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("要专注的事，比如「复习第三章」") },
                textStyle = MaterialTheme.typography.titleMedium,
                shape = RoundedCornerShape(18.dp),
                maxLines = 2,
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            )

            Spacer(Modifier.height(22.dp))
            FieldLabel("时长")
            Spacer(Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                durationOptions.forEach { option ->
                    ChoicePill(
                        label = "$option 分钟",
                        selected = option == minutes,
                        onClick = { minutes = option },
                    )
                }
            }

            Spacer(Modifier.height(22.dp))
            FieldLabel("封面")
            Spacer(Modifier.height(10.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(end = 20.dp),
            ) {
                items(TodoArt.entries) { option ->
                    val selected = option == art
                    Box(
                        modifier = Modifier
                            .size(width = 58.dp, height = 72.dp)
                            .clip(RoundedCornerShape(17.dp))
                            .border(
                                width = if (selected) 2.5.dp else 1.dp,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                },
                                shape = RoundedCornerShape(17.dp),
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { art = option },
                    ) {
                        Artwork(
                            art = option,
                            modifier = Modifier.fillMaxWidth().height(72.dp),
                            scrim = false,
                        )
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 8.dp, bottom = 6.dp),
                        )
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(5.dp)
                                    .size(17.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(11.dp),
                                )
                            }
                        }
                    }
                }
            }

            if (collections.isNotEmpty()) {
                Spacer(Modifier.height(22.dp))
                FieldLabel("待办集")
                Spacer(Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ChoicePill(
                        label = "不加入",
                        selected = collectionId == null,
                        onClick = { collectionId = null },
                    )
                    collections.forEach { collection ->
                        ChoicePill(
                            label = collection.name,
                            selected = collectionId == collection.id,
                            onClick = { collectionId = collection.id },
                        )
                    }
                }
            }

            Spacer(Modifier.height(22.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it.take(200) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("备注（可选）") },
                textStyle = MaterialTheme.typography.bodyMedium,
                shape = RoundedCornerShape(18.dp),
                minLines = 2,
                maxLines = 4,
            )

            Spacer(Modifier.height(26.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (initial != null) {
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(
                            Icons.Rounded.DeleteOutline,
                            contentDescription = "删除待办",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onDismiss) { Text("取消") }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        val now = System.currentTimeMillis()
                        onSave(
                            (initial ?: Todo(
                                id = UUID.randomUUID().toString(),
                                title = "",
                                createdAt = now,
                            )).copy(
                                title = title.trim(),
                                minutes = minutes,
                                art = art,
                                collectionId = collectionId,
                                note = note.trim(),
                            ),
                        )
                    },
                    enabled = title.isNotBlank(),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    modifier = Modifier.height(48.dp),
                ) {
                    Text(
                        text = if (initial == null) "保存" else "保存修改",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 10.dp),
                    )
                }
            }
        }
    }

    if (confirmDelete && initial != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("删除这件待办？") },
            text = { Text("专注记录会保留在统计数据里，但待办本身不能再恢复。") },
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
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 9.dp),
    )
}
