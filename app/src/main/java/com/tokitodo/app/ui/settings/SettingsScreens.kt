package com.tokitodo.app.ui.settings

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.SettingsBrightness
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tokitodo.app.data.AppSettings
import com.tokitodo.app.data.WebUiServer
import com.tokitodo.app.data.ThemeMode
import com.tokitodo.app.data.WidgetStyle
import com.tokitodo.app.ui.SettingsPage
import com.tokitodo.app.ui.Tab
import com.tokitodo.app.ui.components.Artwork
import com.tokitodo.app.ui.components.DetailScaffold
import com.tokitodo.app.ui.components.PillSegmented
import com.tokitodo.app.ui.components.SectionCard
import com.tokitodo.app.ui.theme.ColorPreset
import com.tokitodo.app.ui.theme.SectionCardShape
import com.tokitodo.app.ui.theme.TodoArt
import com.tokitodo.app.ui.theme.collectionTint
import com.tokitodo.app.ui.theme.LocalDarkTheme

@Composable
fun SettingsDetailScreen(
    page: SettingsPage,
    settings: AppSettings,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onUpdate: ((AppSettings) -> AppSettings) -> Unit,
    onClearSessions: () -> Unit,
    onClearAll: () -> Unit,
    exportJson: () -> String,
    onImport: (String) -> Boolean,
    onOpenHistory: () -> Unit,
) {
    DetailScaffold(
        title = page.title,
        contentPadding = contentPadding,
        onBack = onBack,
    ) {
        when (page) {
            SettingsPage.Timer -> TimerSettings(settings, onUpdate)
            SettingsPage.Appearance -> AppearanceSettings(settings, onUpdate)
            SettingsPage.NavBar -> NavBarSettings(settings, onUpdate)
            SettingsPage.Theme -> ThemeSettings(settings, onUpdate)
            SettingsPage.Widget -> WidgetSettings(settings, onUpdate)
            SettingsPage.Desktop -> DesktopSettings(settings, onUpdate)
            SettingsPage.Other -> OtherSettings(
                settings = settings,
                onClearSessions = onClearSessions,
                onClearAll = onClearAll,
                exportJson = exportJson,
                onImport = onImport,
                onOpenHistory = onOpenHistory,
            )
        }
    }
}

@Composable
private fun TimerSettings(
    settings: AppSettings,
    onUpdate: ((AppSettings) -> AppSettings) -> Unit,
) {
    SectionCard {
        Column(Modifier.padding(vertical = 8.dp)) {
            SettingTitle("专注节奏", "每次专注结束后自动切到休息，需要你手动开始下一段")
            StepperRow(
                label = "专注时长",
                value = settings.focusMinutes,
                unit = "分钟",
                range = 5..120,
                step = 5,
                onChange = { value -> onUpdate { it.copy(focusMinutes = value) } },
            )
            StepperRow(
                label = "短休息",
                value = settings.shortBreakMinutes,
                unit = "分钟",
                range = 1..30,
                step = 1,
                onChange = { value -> onUpdate { it.copy(shortBreakMinutes = value) } },
            )
            StepperRow(
                label = "长休息",
                value = settings.longBreakMinutes,
                unit = "分钟",
                range = 5..60,
                step = 5,
                onChange = { value -> onUpdate { it.copy(longBreakMinutes = value) } },
            )
            StepperRow(
                label = "长休息间隔",
                value = settings.sessionsBeforeLongBreak,
                unit = "次专注",
                range = 2..8,
                step = 1,
                onChange = { value -> onUpdate { it.copy(sessionsBeforeLongBreak = value) } },
            )
        }
    }
    SectionCard {
        Column(Modifier.padding(vertical = 8.dp)) {
            SettingTitle("提醒与记录", null)
            ToggleRow(
                title = "声音提示",
                subtitle = "计时结束时播放轻提示音",
                checked = settings.soundEnabled,
                onCheckedChange = { value -> onUpdate { it.copy(soundEnabled = value) } },
            )
            ToggleRow(
                title = "震动反馈",
                subtitle = "点击与完成时给出触感反馈",
                checked = settings.vibrationEnabled,
                onCheckedChange = { value -> onUpdate { it.copy(vibrationEnabled = value) } },
            )
        }
    }
    SectionCard {
        Column(Modifier.padding(vertical = 8.dp)) {
            SettingTitle("今日目标", "统计页的目标进度会跟着这里变化")
            StepperRow(
                label = "每日专注目标",
                value = settings.dailyGoalMinutes,
                unit = "分钟",
                range = 30..480,
                step = 15,
                onChange = { value -> onUpdate { it.copy(dailyGoalMinutes = value) } },
            )
        }
    }
}

/* ----------------------------- appearance ----------------------------- */

@Composable
private fun AppearanceSettings(
    settings: AppSettings,
    onUpdate: ((AppSettings) -> AppSettings) -> Unit,
) {
    val modes = listOf("跟随系统", "浅色", "深色")
    val selectedMode = when (settings.themeMode) {
        ThemeMode.System -> 0
        ThemeMode.Light -> 1
        ThemeMode.Dark -> 2
    }
    SectionCard {
        Column(Modifier.padding(18.dp)) {
            SettingTitle("主题模式", "跟随系统时会自动切换深浅色")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                modes.forEachIndexed { index, label ->
                    val selected = index == selectedMode
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (selected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                },
                            )
                            .clickable {
                                onUpdate {
                                    it.copy(
                                        themeMode = when (index) {
                                            1 -> ThemeMode.Light
                                            2 -> ThemeMode.Dark
                                            else -> ThemeMode.System
                                        },
                                    )
                                }
                            }
                            .padding(vertical = 11.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = when (index) {
                                1 -> Icons.Rounded.LightMode
                                2 -> Icons.Rounded.DarkMode
                                else -> Icons.Rounded.SettingsBrightness
                            },
                            contentDescription = null,
                            tint = if (selected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
            }
        }
    }
    SectionCard {
        Column(Modifier.padding(vertical = 8.dp)) {
            ToggleRow(
                title = "动态取色",
                subtitle = "用系统壁纸的颜色生成主题（Android 12+）",
                checked = settings.dynamicColor,
                onCheckedChange = { value -> onUpdate { it.copy(dynamicColor = value) } },
            )
            ToggleRow(
                title = "纯黑模式",
                subtitle = "深色主题使用纯黑背景，更省电",
                checked = settings.amoled,
                onCheckedChange = { value -> onUpdate { it.copy(amoled = value) } },
            )
            ToggleRow(
                title = "减弱动效",
                subtitle = "关闭页面转场与按压回弹",
                checked = settings.reduceMotion,
                onCheckedChange = { value -> onUpdate { it.copy(reduceMotion = value) } },
            )
        }
    }
    SectionCard {
        Column(Modifier.padding(18.dp)) {
            SettingTitle("待办封面", "封面由应用实时绘制，不下载任何图片")
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TodoArt.entries.take(4).forEach { art ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(66.dp)
                            .clip(RoundedCornerShape(14.dp)),
                    ) {
                        Artwork(art = art, modifier = Modifier.fillMaxSize(), scrim = false)
                    }
                }
            }
        }
    }
}

/* ----------------------------- nav bar ----------------------------- */

@Composable
private fun NavBarSettings(
    settings: AppSettings,
    onUpdate: ((AppSettings) -> AppSettings) -> Unit,
) {
    SectionCard {
        Column(Modifier.padding(vertical = 8.dp)) {
            SettingTitle("底栏", null)
            ToggleRow(
                title = "显示文字标签",
                subtitle = "关闭后底栏只保留图标，页面更安静",
                checked = settings.navLabels,
                onCheckedChange = { value -> onUpdate { it.copy(navLabels = value) } },
            )
        }
    }
    SectionCard {
        Column(Modifier.padding(18.dp)) {
            SettingTitle("默认打开", "每次启动 TokiTodo 时先进入哪个页面")
            Spacer(Modifier.height(8.dp))
            listOf(Tab.Todos, Tab.Collections, Tab.Lock, Tab.Stats, Tab.Profile).forEach { tab ->
                val selected = settings.defaultTab == tab.name
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onUpdate { it.copy(defaultTab = tab.name) } }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    if (selected) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
    SectionCard {
        Column(Modifier.padding(18.dp)) {
            Text(
                text = "底栏始终保留五个入口，避免某一页被藏起来之后忘记它。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/* ----------------------------- theme ----------------------------- */

@Composable
private fun ThemeSettings(
    settings: AppSettings,
    onUpdate: ((AppSettings) -> AppSettings) -> Unit,
) {
    val dark = LocalDarkTheme.current
    SectionCard {
        Column(Modifier.padding(18.dp)) {
            SettingTitle("主题颜色", "主色会同时影响按钮、进度和选中态")
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ColorPreset.entries.forEach { preset ->
                    val selected = settings.colorPreset == preset
                    val seed = if (dark) {
                        // 直接用深色下的主色做展示
                        when (preset) {
                            ColorPreset.Pine -> Color(0xFF9CD0BE)
                            ColorPreset.Persimmon -> Color(0xFFFFB59F)
                            ColorPreset.Ocean -> Color(0xFFA8C8EA)
                            ColorPreset.Wisteria -> Color(0xFFCFBBF5)
                        }
                    } else {
                        when (preset) {
                            ColorPreset.Pine -> Color(0xFF356859)
                            ColorPreset.Persimmon -> Color(0xFF9C4A2F)
                            ColorPreset.Ocean -> Color(0xFF2F5C86)
                            ColorPreset.Wisteria -> Color(0xFF66508E)
                        }
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onUpdate { it.copy(colorPreset = preset) } }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(seed)
                                .then(
                                    if (selected) {
                                        Modifier.border(
                                            3.dp,
                                            MaterialTheme.colorScheme.surfaceContainerLowest,
                                            CircleShape,
                                        )
                                    } else {
                                        Modifier
                                    },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (selected) {
                                Icon(
                                    Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = preset.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
            }
        }
    }
    SectionCard {
        Column(Modifier.padding(18.dp)) {
            SettingTitle("预览", null)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0, 1, 2).forEach { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(74.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(collectionTint(index, dark)),
                    ) {
                        Text(
                            text = "待办集",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(10.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "动态取色",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = settings.dynamicColor,
                    onCheckedChange = { value -> onUpdate { it.copy(dynamicColor = value) } },
                )
            }
        }
    }
}

/* ----------------------------- widget ----------------------------- */

@Composable
private fun WidgetSettings(
    settings: AppSettings,
    onUpdate: ((AppSettings) -> AppSettings) -> Unit,
) {
    SectionCard {
        Column(Modifier.padding(18.dp)) {
            SettingTitle("小组件背景", "选好的样式会用在桌面小组件上")
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                WidgetStyle.entries.forEach { style ->
                    val selected = settings.widgetStyle == style
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                },
                                shape = RoundedCornerShape(18.dp),
                            )
                            .clickable { onUpdate { it.copy(widgetStyle = style) } }
                            .padding(6.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(92.dp)
                                .clip(RoundedCornerShape(13.dp)),
                        ) {
                            WidgetPreview(style)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = style.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = "桌面长按空白处 → 小组件 → 找到 TokiTodo，即可添加。小组件显示今日专注、连续天数与下一件待办。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WidgetPreview(style: WidgetStyle) {
    when (style) {
        WidgetStyle.Forest -> {
            Box(Modifier.fillMaxSize()) {
                Artwork(art = TodoArt.Forest, modifier = Modifier.fillMaxSize(), scrim = false)
                Column(Modifier.align(Alignment.BottomStart).padding(8.dp)) {
                    Text(
                        text = "25:00",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "专注中",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }
            }
        }

        WidgetStyle.Paper -> {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                Column(Modifier.align(Alignment.BottomStart).padding(8.dp)) {
                    Text(
                        text = "25:00",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "专注中",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        WidgetStyle.Sunrise -> {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color(0xFFF7A8BC), Color(0xFFFBE6D8)))),
            ) {
                Column(Modifier.align(Alignment.BottomStart).padding(8.dp)) {
                    Text(
                        text = "25:00",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "专注中",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }
            }
        }
    }
}

/* ----------------------------- desktop ----------------------------- */

@Composable
private fun DesktopSettings(
    settings: AppSettings,
    onUpdate: ((AppSettings) -> AppSettings) -> Unit,
) {
    val context = LocalContext.current
    val pairingCode = settings.desktopPairingCode.ifBlank { "------" }
    SectionCard {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "电脑端专注助手",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Rounded.PhoneAndroid,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = "在电脑上查看当前专注状态，需要保持同一局域网。TokiTodo 不会把记录上传到云端。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .clickable {
                        onUpdate { it.copy(desktopAssistant = !it.desktopAssistant) }
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "开启局域网 WebUI",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (settings.desktopAssistant) "运行中" else "关闭",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (settings.desktopAssistant) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
                Switch(
                    checked = settings.desktopAssistant,
                    onCheckedChange = { value ->
                        onUpdate { it.copy(desktopAssistant = value) }
                    },
                )
            }
            if (settings.desktopAssistant) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "http://${WebUiServer.localAddress()}:8080",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "配对码",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = pairingCode,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp,
                        ),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Surface(
                    onClick = {
                        val clipboard = context.getSystemService(
                            android.content.ClipboardManager::class.java,
                        )
                        clipboard?.setPrimaryClip(
                            android.content.ClipData.newPlainText(
                                "TokiTodo WebUI",
                                "http://${WebUiServer.localAddress()}:8080  配对码：$pairingCode",
                            ),
                        )
                    },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Rounded.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(Modifier.width(5.dp))
                        Text("复制", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            listOf(
                "在电脑浏览器打开上面的 http:// 地址（需与手机同一 WiFi）",
                "输入六位配对码",
                "电脑端会显示今日专注、累计数据和待办；刷新即可获取最新数据",
            ).forEachIndexed { index, text ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (settings.desktopAssistant) {
                    "WebUI 只在应用运行期间提供服务，关闭开关即停止。"
                } else {
                    "打开开关后，手机和电脑需要连接同一个 WiFi；不会上传到云端。"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}

/* ----------------------------- other ----------------------------- */

@Composable
private fun OtherSettings(
    settings: AppSettings,
    onClearSessions: () -> Unit,
    onClearAll: () -> Unit,
    exportJson: () -> String,
    onImport: (String) -> Boolean,
    onOpenHistory: () -> Unit,
) {
    var confirmClearSessions by remember { mutableStateOf(false) }
    var confirmClearAll by remember { mutableStateOf(false) }
    var privacyOpen by remember { mutableStateOf(false) }
    var pendingImport by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            val ok = runCatching {
                context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)
                    ?.use { it.write(exportJson()) }
            }.isSuccess
            Toast.makeText(
                context,
                if (ok) "备份已保存" else "备份保存失败",
                Toast.LENGTH_SHORT,
            ).show()
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            val text = runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)
                    ?.use { it.readText() }
            }.getOrNull()
            if (text.isNullOrBlank()) {
                Toast.makeText(context, "读取备份失败", Toast.LENGTH_SHORT).show()
            } else {
                pendingImport = text
            }
        }
    }

    SectionCard {
        Column(Modifier.padding(18.dp)) {
            SettingTitle("数据", "备份是一个 JSON 文件，由你保管")
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { exportLauncher.launch("TokiTodo-backup.json") },
                    shape = CircleShape,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Rounded.FileDownload, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("导出备份", style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(
                    onClick = {
                        importLauncher.launch(
                            arrayOf("application/json", "text/plain", "application/octet-stream"),
                        )
                    },
                    shape = CircleShape,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Rounded.FileUpload, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("导入备份", style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onOpenHistory,
                shape = CircleShape,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("查看专注记录", style = MaterialTheme.typography.labelMedium)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "导入会覆盖当前的待办、待办集、锁机时段、记录和愿望，请确认备份是最新的。",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
            )
        }
    }
    SectionCard {
        Column(Modifier.padding(vertical = 8.dp)) {
            SettingTitle("清理", null)
            SettingRowInline(
                title = "清空专注记录",
                subtitle = "统计归零，待办与设置保留",
                tint = MaterialTheme.colorScheme.tertiary,
            ) {
                TextButton(onClick = { confirmClearSessions = true }) {
                    Text("清空", color = MaterialTheme.colorScheme.error)
                }
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                modifier = Modifier.padding(start = 18.dp, end = 18.dp),
            )
            SettingRowInline(
                title = "清空所有数据",
                subtitle = "待办、待办集、锁机时段、记录全部删除",
                tint = MaterialTheme.colorScheme.error,
            ) {
                TextButton(onClick = { confirmClearAll = true }) {
                    Text("重置", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
    SectionCard {
        Column(Modifier.padding(18.dp)) {
            SettingTitle("关于", null)
            Spacer(Modifier.height(6.dp))
            listOf("版本" to "0.2.0", "应用" to "TokiTodo", "网络权限" to "未申请").forEach { (key, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = key,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    .clickable { privacyOpen = true }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Rounded.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "隐私说明：没有账号，没有统计上报，没有广告。",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    if (confirmClearSessions) {
        AlertDialog(
            onDismissRequest = { confirmClearSessions = false },
            title = { Text("清空专注记录？") },
            text = { Text("所有统计会归零，待办、待办集和设置保留。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmClearSessions = false
                        onClearSessions()
                    },
                ) {
                    Text("清空", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClearSessions = false }) { Text("取消") }
            },
        )
    }
    if (privacyOpen) {
        AlertDialog(
            onDismissRequest = { privacyOpen = false },
            title = { Text("隐私说明") },
            text = {
                Text(
                    "TokiTodo 只在检查软件更新时访问 GitHub。待办、专注记录、锁机时段和个人档案都保存在本机，" +
                        "不会同步到任何服务器；卸载应用或清空数据后再无副本，建议定期使用导出备份。",
                )
            },
            confirmButton = {
                TextButton(onClick = { privacyOpen = false }) { Text("知道了") }
            },
        )
    }
    if (confirmClearAll) {
        AlertDialog(
            onDismissRequest = { confirmClearAll = false },
            title = { Text("重置所有数据？") },
            text = { Text("待办、待办集、锁机时段、专注记录和设置都会被删除，且不能恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmClearAll = false
                        onClearAll()
                    },
                ) {
                    Text("全部删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClearAll = false }) { Text("取消") }
            },
        )
    }

    pendingImport?.let { raw ->
        AlertDialog(
            onDismissRequest = { pendingImport = null },
            title = { Text("导入这份备份？") },
            text = {
                Text(
                    "导入会覆盖当前全部待办、待办集、锁机时段、专注记录、愿望和倒计时。" +
                        "如果当前数据还有用，请先导出一份。",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingImport = null
                        val ok = onImport(raw)
                        Toast.makeText(
                            context,
                            if (ok) "备份已恢复" else "备份格式不正确",
                            Toast.LENGTH_SHORT,
                        ).show()
                    },
                ) {
                    Text("覆盖导入", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingImport = null }) { Text("取消") }
            },
        )
    }
}

/* ----------------------------- shared ----------------------------- */

@Composable
private fun SettingTitle(title: String, subtitle: String?) {
    Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StepperRow(
    label: String,
    value: Int,
    unit: String,
    range: IntRange,
    step: Int,
    onChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        StepperButton(
            icon = Icons.Rounded.Remove,
            enabled = value > range.first,
            onClick = { onChange((value - step).coerceIn(range.first, range.last)) },
        )
        Text(
            text = "$value $unit",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(88.dp),
        )
        StepperButton(
            icon = Icons.Rounded.Add,
            enabled = value < range.last,
            onClick = { onChange((value + step).coerceIn(range.first, range.last)) },
        )
    }
}

@Composable
private fun StepperButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.size(32.dp),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
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
        }
        Spacer(Modifier.width(10.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingRowInline(
    title: String,
    subtitle: String,
    tint: Color,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = tint,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        trailing()
    }
}
