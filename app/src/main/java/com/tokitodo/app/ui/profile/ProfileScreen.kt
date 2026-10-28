package com.tokitodo.app.ui.profile

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VideoCameraFront
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.WavingHand
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tokitodo.app.data.AppSettings
import com.tokitodo.app.data.FocusSession
import com.tokitodo.app.data.Stats
import com.tokitodo.app.ui.SettingsPage
import com.tokitodo.app.ui.components.SectionCard
import com.tokitodo.app.ui.components.SettingRow
import com.tokitodo.app.ui.components.SheetGrabber
import com.tokitodo.app.ui.theme.LocalDarkTheme
import com.tokitodo.app.ui.theme.SectionCardShape
import com.tokitodo.app.ui.theme.SheetShape
import java.io.File
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    settings: AppSettings,
    sessions: List<FocusSession>,
    wishesCount: Int,
    contentPadding: PaddingValues,
    onOpenPage: (SettingsPage) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenCountdown: () -> Unit,
    onOpenWishWall: () -> Unit,
    onOpenVip: () -> Unit,
    onOpenAbout: () -> Unit,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
) {
    var editOpen by remember { mutableStateOf(false) }
    var helpOpen by remember { mutableStateOf(false) }
    var moreOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val avatarPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            copyAvatar(context, uri)?.let { path ->
                onUpdateSettings { it.copy(userAvatarPath = path) }
            }
        }
    }
    val today = LocalDate.now()
    val activeDays = Stats.activeDays(sessions)
    val streak = Stats.streak(sessions, today)
    val totalMinutes = Stats.totalMinutes(sessions)
    val dark = LocalDarkTheme.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (dark) {
                        listOf(
                            Color(0xFF3B2430),
                            Color(0xFF4B2D2A),
                            Color(0xFF3A2A2D),
                            Color(0xFF262020),
                            MaterialTheme.colorScheme.surface,
                        )
                    } else {
                        listOf(
                            Color(0xFFE07E97),
                            Color(0xFFECA387),
                            Color(0xFFF6CDB1),
                            Color(0xFFF0DCE4),
                            MaterialTheme.colorScheme.surface,
                        )
                    },
                ),
            )
            .verticalScroll(rememberScrollState())
            .padding(bottom = 152.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp),
        ) {
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    HeroAction(Icons.Rounded.HelpOutline, "帮助") { helpOpen = true }
                    Spacer(Modifier.width(8.dp))
                    HeroAction(Icons.Rounded.Settings, "外观设置") { onOpenPage(SettingsPage.Appearance) }
                    Spacer(Modifier.width(8.dp))
                    HeroAction(Icons.Rounded.MoreVert, "更多") { moreOpen = true }
                }
                Spacer(Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center) {
                        val avatarBitmap by produceState<androidx.compose.ui.graphics.ImageBitmap?>(
                            initialValue = null,
                            settings.userAvatarPath,
                        ) {
                            value = settings.userAvatarPath
                                ?.takeIf { File(it).exists() }
                                ?.let { path ->
                                    runCatching {
                                        BitmapFactory.decodeFile(path)?.asImageBitmap()
                                    }.getOrNull()
                                }
                        }
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.92f))
                                .clickable {
                                    avatarPicker.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly,
                                        ),
                                    )
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            val bitmap = avatarBitmap
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = "个人头像",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            } else {
                                Text(text = settings.userEmoji, fontSize = 42.sp)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(27.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable { editOpen = true },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "编辑资料",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = settings.userName,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    shadow = Shadow(
                                        color = Color(0xFF5B2A3A).copy(alpha = 0.35f),
                                        offset = Offset(0f, 2f),
                                        blurRadius = 10f,
                                    ),
                                ),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.25f))
                                    .padding(horizontal = 7.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    text = "本地",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                )
                            }
                        }
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = settings.userSignature.ifBlank { "写一句自己的签名" },
                            style = MaterialTheme.typography.bodySmall.copy(
                                shadow = Shadow(
                                    color = Color(0xFF4A1F2C).copy(alpha = 0.45f),
                                    offset = Offset(0f, 1f),
                                    blurRadius = 6f,
                                ),
                            ),
                            color = Color.White.copy(alpha = 0.95f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    HeroStat("共专注 $activeDays 天", Icons.Rounded.LocalFireDepartment)
                    HeroStat("连续专注 $streak 天", Icons.Rounded.WavingHand)
                    HeroStat("${totalMinutes} 分钟", Icons.Rounded.Timer)
                }
            }

        Column(modifier = Modifier.padding(horizontal = 18.dp)) {
            Spacer(Modifier.height(16.dp))
            // 快捷入口
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = SectionCardShape,
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shadowElevation = 1.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                ) {
                    QuickEntry(
                        icon = Icons.Rounded.Timer,
                        label = "未来倒计时",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenCountdown,
                    )
                    QuickDivider()
                    QuickEntry(
                        icon = Icons.Rounded.History,
                        label = "专注历史记录",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenHistory,
                    )
                    QuickDivider()
                    QuickEntry(
                        icon = Icons.Rounded.WavingHand,
                        label = "许愿墙",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenWishWall,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))

            // VIP / 备份
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = SectionCardShape,
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shadowElevation = 1.dp,
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onOpenVip)
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(7.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                        ) {
                            Text(
                                text = "VIP",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "季度卡",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "众多新颖高级功能，助你养成专注好习惯",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Icon(
                            Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(start = 18.dp, end = 18.dp),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenPage(SettingsPage.Backup) }
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Rounded.Cloud,
                            contentDescription = null,
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "备份 | 恢复",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "本地 JSON 导入导出，不上传云端",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Icon(
                            Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))

            SectionCard {
                SettingRow(
                    icon = Icons.Rounded.Timer,
                    title = "专注计时核心设置",
                    subtitle = "铃声震动 | 休息时长等",
                    tint = MaterialTheme.colorScheme.primary,
                    iconBackground = false,
                    onClick = { onOpenPage(SettingsPage.Timer) },
                )
                SettingsDivider()
                SettingRow(
                    icon = Icons.Rounded.Wallpaper,
                    title = "海报背景和外观",
                    subtitle = "待办和专注背景 | 深色模式",
                    tint = Color(0xFFF87171),
                    iconBackground = false,
                    onClick = { onOpenPage(SettingsPage.Appearance) },
                )
                SettingsDivider()
                SettingRow(
                    icon = Icons.Rounded.Tune,
                    title = "自定义底栏模块",
                    subtitle = "选择最喜欢的功能放在主界面",
                    tint = Color(0xFF38BDF8),
                    iconBackground = false,
                    onClick = { onOpenPage(SettingsPage.NavBar) },
                )
                SettingsDivider()
                SettingRow(
                    icon = Icons.Rounded.Widgets,
                    title = "桌面小组件选项",
                    subtitle = "小组件背景图等",
                    tint = Color(0xFFFBBF24),
                    iconBackground = false,
                    onClick = { onOpenPage(SettingsPage.Widget) },
                )
                SettingsDivider()
                SettingRow(
                    icon = Icons.Rounded.VideoCameraFront,
                    title = "连接电脑端专注助手",
                    subtitle = "可设置连接电脑端计时工具",
                    tint = Color(0xFF60A5FA),
                    iconBackground = false,
                    onClick = { onOpenPage(SettingsPage.Desktop) },
                )
                SettingsDivider()
                SettingRow(
                    icon = Icons.Rounded.Settings,
                    title = "其他设置",
                    subtitle = "清除数据设置等",
                    tint = Color(0xFF34D399),
                    onClick = { onOpenPage(SettingsPage.Other) },
                    iconBackground = false,
                )
                SettingsDivider()
                SettingRow(
                    icon = Icons.Rounded.Info,
                    title = "关于 TokiTodo",
                    subtitle = "版本信息 | 软件更新",
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = onOpenAbout,
                    iconBackground = false,
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = "TokiTodo 0.1.0 Beta.1 · 无账号、无广告 · 仅检查更新时联网",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 6.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "许愿墙上已经有 $wishesCount 个愿望",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                modifier = Modifier.padding(start = 6.dp),
            )
        }
    }

    if (editOpen) {
        EditProfileSheet(
            settings = settings,
            onDismiss = { editOpen = false },
            onSave = { name, signature, emoji ->
                onUpdateSettings {
                    it.copy(userName = name, userSignature = signature, userEmoji = emoji)
                }
                editOpen = false
            },
        )
    }

    if (helpOpen) {
        AlertDialog(
            onDismissRequest = { helpOpen = false },
            title = { Text("怎么使用 TokiTodo") },
            text = {
                Text(
                    "· 在「待办」右上角 + 添加要专注的事\n" +
                        "· 点击卡片开始一段专注，长按卡片编辑或删除\n" +
                        "· 「锁机」是更严格的自控：锁机期间不能暂停\n" +
                        "· 所有记录自动进入「统计数据」，按月查看热力图\n" +
                        "· 数据只存在本机，可在「其他设置」里导出备份",
                )
            },
            confirmButton = {
                TextButton(onClick = { helpOpen = false }) { Text("开始专注") }
            },
        )
    }

    if (moreOpen) {
        AlertDialog(
            onDismissRequest = { moreOpen = false },
            title = { Text("关于 TokiTodo") },
            text = {
                Text(
                    "一个安静的自控工具：待办、专注、锁机、统计。没有账号体系，" +
                        "只有检查软件更新时访问 GitHub；不会上传你的待办和专注记录。",
                )
            },
            confirmButton = {
                TextButton(onClick = { moreOpen = false }) { Text("知道了") }
            },
        )
    }
}

private fun copyAvatar(context: android.content.Context, uri: Uri): String? = runCatching {
    val dir = File(context.filesDir, "profile").apply { mkdirs() }
    val file = File(dir, "avatar.jpg")
    context.contentResolver.openInputStream(uri)?.use { input ->
        file.outputStream().use { output -> input.copyTo(output) }
    }
    file.takeIf { it.length() > 0 }?.absolutePath
}.getOrNull()

@Composable
private fun ProfileHero() {
    val dark = LocalDarkTheme.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(286.dp)
            .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp)),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFE07E97), Color(0xFFECA387), Color(0xFFF6CDB1)),
                ),
            )
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color.Black.copy(alpha = 0.16f), Color.Transparent),
                    startY = 0f,
                    endY = h * 0.34f,
                ),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color.White.copy(alpha = 0.65f), Color.Transparent),
                    center = Offset(w * 0.72f, h * 0.72f),
                    radius = w * 0.42f,
                ),
                radius = w * 0.42f,
                center = Offset(w * 0.72f, h * 0.72f),
            )
            drawCircle(
                color = Color(0xFFFFF4E4).copy(alpha = 0.9f),
                radius = h * 0.09f,
                center = Offset(w * 0.74f, h * 0.75f),
            )
            val backHill = Path().apply {
                moveTo(0f, h * 0.88f)
                cubicTo(w * 0.25f, h * 0.72f, w * 0.52f, h * 0.92f, w, h * 0.80f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = backHill,
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFC88BA8).copy(alpha = 0.55f), Color(0xFFA9748F).copy(alpha = 0.80f)),
                    startY = h * 0.74f,
                    endY = h,
                ),
            )
            repeat(3) { index ->
                val y = h * (0.90f + index * 0.025f)
                drawRect(
                    color = Color.White.copy(alpha = 0.05f),
                    topLeft = Offset(0f, y),
                    size = androidx.compose.ui.geometry.Size(w, h * 0.012f),
                )
            }
            // 左侧压一点暗，保证白字在浅色日出上也够清楚
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFF4A1F2C).copy(alpha = 0.22f), Color.Transparent, Color.Transparent),
                    startX = 0f,
                    endX = w,
                ),
            )
            if (dark) {
                drawRect(color = Color.Black.copy(alpha = 0.24f))
            }
        }
    }
}

@Composable
private fun HeroAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.26f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(21.dp),
        )
    }
}

@Composable
private fun HeroStat(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFFFF1E4).copy(alpha = 0.9f),
            modifier = Modifier.size(15.dp),
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                shadow = Shadow(
                    color = Color(0xFF3B1A24).copy(alpha = 0.4f),
                    offset = Offset(0f, 1f),
                    blurRadius = 5f,
                ),
            ),
            color = Color.White,
        )
    }
}

@Composable
private fun QuickEntry(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}

@Composable
private fun QuickDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(48.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    )
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
        modifier = Modifier.padding(start = 68.dp, end = 16.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileSheet(
    settings: AppSettings,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf(settings.userName) }
    var signature by remember { mutableStateOf(settings.userSignature) }
    var emoji by remember { mutableStateOf(settings.userEmoji) }
    val emojis = listOf("🍅", "🌿", "🌙", "☕", "🐈", "📚", "✨", "🪴")

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
                text = "本地档案",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "这张档案只存在这台设备上，不需要登录，也不会上传。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                emojis.forEach { option ->
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                if (option == emoji) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                },
                            )
                            .clickable { emoji = option },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = option, fontSize = 22.sp)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(16) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("ID / 昵称") },
                shape = RoundedCornerShape(18.dp),
                singleLine = true,
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = signature,
                onValueChange = { signature = it.take(40) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("个人签名") },
                shape = RoundedCornerShape(18.dp),
                singleLine = true,
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    onSave(
                        name.trim().ifBlank { "番茄同学" },
                        signature.trim(),
                        emoji,
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text("保存", style = MaterialTheme.typography.titleSmall)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "头像可从相册选择，图片只保存在本机；不选择时使用 emoji。",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}
