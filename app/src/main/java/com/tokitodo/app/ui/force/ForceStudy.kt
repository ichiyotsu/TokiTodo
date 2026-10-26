package com.tokitodo.app.ui.force

import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DoNotDisturbOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tokitodo.app.data.AppSettings
import com.tokitodo.app.ui.components.DetailScaffold
import com.tokitodo.app.ui.components.SectionCard
import com.tokitodo.app.ui.components.SheetGrabber
import com.tokitodo.app.ui.theme.SheetShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 强制学习模式弹窗：四个功能开关 + 两个二级入口。
 * 具体到系统层面的 App 拦截需要额外授权，这里先把策略与白名单完整落地。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForceStudySheet(
    settings: AppSettings,
    onDismiss: () -> Unit,
    onUpdate: ((AppSettings) -> AppSettings) -> Unit,
    onOpenEnhancements: () -> Unit,
    onOpenWhitelist: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
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
                .padding(bottom = 30.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "强制学习模式",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "把专注交给规则，而不是意志力",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "关闭")
                }
            }

            Spacer(Modifier.height(18.dp))
            SectionCard {
                Column {
                    SwitchRow(
                        icon = Icons.Rounded.Block,
                        title = "强制学习",
                        subtitle = "计时开始后阻止其他 App 打开",
                        checked = settings.studyMode,
                        onCheckedChange = { value -> onUpdate { it.copy(studyMode = value) } },
                        hint = "点击待办卡片上的按钮来计时",
                    )
                    Divider()
                    SwitchRow(
                        icon = Icons.Rounded.Lock,
                        title = "严格模式",
                        subtitle = "计时开始后不能暂停，只能提前完成或取消",
                        checked = settings.strictMode,
                        onCheckedChange = { value -> onUpdate { it.copy(strictMode = value) } },
                    )
                    Divider()
                    SwitchRow(
                        icon = Icons.Rounded.DoNotDisturbOn,
                        title = "拦截通知",
                        subtitle = "计时或锁机开始后拦截其他 App 的消息，避免打扰",
                        checked = settings.blockNotifications,
                        onCheckedChange = { value -> onUpdate { it.copy(blockNotifications = value) } },
                    )
                    Divider()
                    EntryRow(
                        icon = Icons.Rounded.Shield,
                        title = "提升强制性选项",
                        subtitle = "禁止桌面与多任务、隐藏任务列表、锁机禁止主界面",
                        onClick = onOpenEnhancements,
                    )
                    Divider()
                    EntryRow(
                        icon = Icons.Rounded.Add,
                        title = "全局白名单",
                        subtitle = if (settings.whitelistPackages.isEmpty()) {
                            "允许学习类应用避免被拦截"
                        } else {
                            "已加入 ${settings.whitelistPackages.size} 个应用"
                        },
                        onClick = onOpenWhitelist,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = "说明：强制学习需要「悬浮窗 / 使用情况访问」等系统权限才能跨 App 生效；" +
                    "当前版本会保存你的策略与白名单，并在计时页提示。未授权的系统能力不会伪造。",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
private fun SwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    hint: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(19.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (hint != null) {
                Text(
                    text = hint,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun EntryRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(19.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(19.dp),
        )
    }
}

@Composable
private fun Divider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        modifier = Modifier.padding(start = 64.dp, end = 16.dp),
    )
}

/* ----------------------------- enhancements ----------------------------- */

@Composable
fun ForceEnhancementsScreen(
    settings: AppSettings,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onUpdate: ((AppSettings) -> AppSettings) -> Unit,
) {
    DetailScaffold(
        title = "提升强制性选项",
        contentPadding = contentPadding,
        onBack = onBack,
    ) {
        Text(
            text = "这些选项会让强制学习更严格，部分能力依赖系统权限。现在保存后，会在强制学习计时中作为策略生效。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SectionCard {
            Column {
                SwitchRow(
                    icon = Icons.Rounded.Block,
                    title = "禁止桌面和多任务",
                    subtitle = "计时中禁止回到桌面、打开多任务，防止卸载或强行关闭 App",
                    checked = settings.blockRecents,
                    onCheckedChange = { value -> onUpdate { it.copy(blockRecents = value) } },
                )
                Divider()
                SwitchRow(
                    icon = Icons.Rounded.VisibilityOff,
                    title = "在多任务列表隐藏",
                    subtitle = "防止从多任务列表划掉 App 来停止强制学习",
                    checked = settings.hideInRecents,
                    onCheckedChange = { value -> onUpdate { it.copy(hideInRecents = value) } },
                )
                Divider()
                SwitchRow(
                    icon = Icons.Rounded.Lock,
                    title = "锁机时禁止打开主界面",
                    subtitle = "锁机时阻止回到 TokiTodo 主界面，防止强行关闭 App",
                    checked = settings.lockBlockHome,
                    onCheckedChange = { value -> onUpdate { it.copy(lockBlockHome = value) } },
                )
            }
        }
    }
}

/* ----------------------------- whitelist ----------------------------- */

private data class InstalledApp(
    val packageName: String,
    val label: String,
)

@Composable
fun WhitelistScreen(
    settings: AppSettings,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onToggle: (String) -> Unit,
) {
    WhitelistContent(
        selectedPackages = settings.whitelistPackages,
        title = "全局白名单",
        hint = "白名单里的应用在强制学习、锁机和拦截通知时会被放行。例如词典、网课或笔记应用。",
        contentPadding = contentPadding,
        onBack = onBack,
        onToggle = onToggle,
    )
}

@Composable
fun TodoWhitelistScreen(
    selectedPackages: Set<String>,
    title: String,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onToggle: (String) -> Unit,
) {
    WhitelistContent(
        selectedPackages = selectedPackages,
        title = title,
        hint = "这件待办专注期间，白名单里的应用会被放行；它独立于全局白名单。",
        contentPadding = contentPadding,
        onBack = onBack,
        onToggle = onToggle,
    )
}

@Composable
private fun WhitelistContent(
    selectedPackages: Set<String>,
    title: String,
    hint: String,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onToggle: (String) -> Unit,
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var apps by remember { mutableStateOf<List<InstalledApp>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        apps = withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
                .mapNotNull { info ->
                    val pkg = info.activityInfo?.packageName ?: return@mapNotNull null
                    if (pkg == context.packageName) return@mapNotNull null
                    InstalledApp(
                        packageName = pkg,
                        label = runCatching {
                            pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
                        }.getOrDefault(pkg),
                    )
                }
                .distinctBy { it.packageName }
                .sortedBy { it.label.lowercase() }
        }
        loading = false
    }

    DetailScaffold(
        title = title,
        contentPadding = contentPadding,
        onBack = onBack,
    ) {
        Text(
            text = hint,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("搜索应用") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            shape = RoundedCornerShape(18.dp),
            singleLine = true,
        )
        Text(
            text = "已选择 ${selectedPackages.size} 个应用",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp),
        )
        if (loading) {
            Text(
                text = "正在读取已安装应用…",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            val filtered = apps.filter {
                query.isBlank() ||
                    it.label.contains(query, ignoreCase = true) ||
                    it.packageName.contains(query, ignoreCase = true)
            }
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
            ) {
                LazyColumn(modifier = Modifier.fillMaxWidth().height(420.dp)) {
                    items(filtered, key = { it.packageName }) { app ->
                        val checked = selectedPackages.contains(app.packageName)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggle(app.packageName) }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = app.label.take(1),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = app.packageName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Switch(checked = checked, onCheckedChange = { onToggle(app.packageName) })
                        }
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            modifier = Modifier.padding(start = 62.dp),
                        )
                    }
                }
            }
        }
    }
}
