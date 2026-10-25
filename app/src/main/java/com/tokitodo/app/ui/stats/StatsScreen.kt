package com.tokitodo.app.ui.stats

import android.content.Intent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.tokitodo.app.data.AppSettings
import com.tokitodo.app.data.FocusSession
import com.tokitodo.app.data.Stats
import com.tokitodo.app.ui.components.BarDatum
import com.tokitodo.app.ui.components.CircleAction
import com.tokitodo.app.ui.components.FocusBarChart
import com.tokitodo.app.ui.components.GradientBar
import com.tokitodo.app.ui.components.Metric
import com.tokitodo.app.ui.components.MonthHeatmap
import com.tokitodo.app.ui.components.PageTopBar
import com.tokitodo.app.ui.components.PillAction
import com.tokitodo.app.ui.components.PillSegmented
import com.tokitodo.app.ui.components.SectionCard
import com.tokitodo.app.ui.components.TimeOfDayBars
import com.tokitodo.app.ui.formatDate
import com.tokitodo.app.ui.formatDuration
import com.tokitodo.app.ui.formatShortDuration
import com.tokitodo.app.ui.theme.SectionCardShape
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

private val rangeLabels = listOf("日", "周", "月", "自定义")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    sessions: List<FocusSession>,
    settings: AppSettings,
    contentPadding: PaddingValues,
    onOpenHistory: () -> Unit,
    onOpenTimerSettings: () -> Unit,
    onGoTodos: () -> Unit,
    onClearSessions: () -> Unit,
) {
    val today = LocalDate.now()
    var day by remember { mutableStateOf(today) }
    var month by remember { mutableStateOf(YearMonth.from(today)) }
    var distributionMonth by remember { mutableStateOf(YearMonth.from(today)) }
    var rangeIndex by remember { mutableIntStateOf(0) }
    var anchor by remember { mutableStateOf(today) }
    var customStart by remember { mutableStateOf(today.minusDays(6)) }
    var customEnd by remember { mutableStateOf(today) }
    var highlight by remember { mutableStateOf<Int?>(null) }
    var menuOpen by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }
    var pickTarget by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current

    val totalMinutes = Stats.totalMinutes(sessions)
    val totalCount = Stats.totalCount(sessions)
    val average = Stats.dailyAverage(sessions, today)
    val streak = Stats.streak(sessions, today)
    val activeDays = Stats.activeDays(sessions)
    val dayMinutes = Stats.minutesOn(sessions, day)
    val dayCount = Stats.countOn(sessions, day)
    val dayGoalProgress = (dayMinutes.toFloat() / settings.dailyGoalMinutes.coerceAtLeast(1)).coerceIn(0f, 1f)

    val distribution = when (rangeIndex) {
        0 -> {
            val hourly = Stats.hourly(sessions, anchor)
            (0 until 8).map { index ->
                val value = hourly.drop(index * 3).take(3).sum()
                BarDatum(
                    label = if (index % 2 == 0) "${index * 3}时" else "",
                    value = value,
                    highlightLabel = "${index * 3}:00 – ${index * 3 + 3}:00 · $value 分钟",
                )
            }
        }

        1 -> Stats.week(sessions, anchor).mapIndexed { index, (date, value) ->
            BarDatum(
                label = listOf("一", "二", "三", "四", "五", "六", "日")[index],
                value = value,
                highlightLabel = "${formatDate(date)} · $value 分钟",
            )
        }

        2 -> {
            val days = Stats.month(sessions, YearMonth.from(anchor))
            days.mapIndexed { index, (date, value) ->
                BarDatum(
                    label = if (index % 5 == 0 || index == days.lastIndex) "${date.dayOfMonth}" else "",
                    value = value,
                    highlightLabel = "${formatDate(date)} · $value 分钟",
                )
            }
        }

        else -> {
            val days = Stats.range(sessions, customStart, customEnd)
            val step = (days.size / 10).coerceAtLeast(1)
            days.mapIndexed { index, (date, value) ->
                BarDatum(
                    label = if (index % step == 0) "${date.monthValue}/${date.dayOfMonth}" else "",
                    value = value,
                    highlightLabel = "${formatDate(date)} · $value 分钟",
                )
            }
        }
    }
    val distributionTotal = distribution.sumOf { it.value }
    val rangeTitle = when (rangeIndex) {
        0 -> formatDate(anchor)
        1 -> {
            val monday = anchor.minusDays((anchor.dayOfWeek.value - 1).toLong())
            "${monday.monthValue}月${monday.dayOfMonth}日 – ${monday.plusDays(6).monthValue}月${monday.plusDays(6).dayOfMonth}日"
        }

        2 -> "${YearMonth.from(anchor).year}年${YearMonth.from(anchor).monthValue}月"
        else -> "${formatDate(customStart)} – ${formatDate(customEnd)}"
    }

    val monthDays = Stats.month(sessions, month)
    val monthTotal = Stats.monthTotal(sessions, month)
    val monthActive = monthDays.count { it.second > 0 }
    val monthAverage = if (monthActive == 0) 0 else monthTotal / monthActive
    val monthTimeBuckets = Stats.timeOfDay(
        Stats.counted(sessions).filter { YearMonth.from(Stats.dateOf(it.endedAt)) == distributionMonth },
    )
    val distributionMonthTotal = monthTimeBuckets.sum()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                    0.22f to MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                    0.45f to MaterialTheme.colorScheme.surface,
                ),
            ),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(contentPadding),
            contentPadding = PaddingValues(bottom = 152.dp),
        ) {
            item {
                PageTopBar(
                    actions = {
                        CircleAction(
                            icon = Icons.Rounded.History,
                            contentDescription = "专注记录",
                            onClick = onOpenHistory,
                        )
                        CircleAction(
                            icon = Icons.Rounded.Share,
                            contentDescription = "分享专注总结",
                            onClick = {
                                val text = buildString {
                                    appendLine("我在 TokiTodo 的专注记录")
                                    appendLine("累计专注 ${formatDuration(totalMinutes)} · $totalCount 次")
                                    appendLine("连续专注 $streak 天，今天 ${dayMinutes} 分钟")
                                    append("#TokiTodo")
                                }
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                context.startActivity(Intent.createChooser(intent, "分享专注总结"))
                            },
                        )
                        CircleAction(
                            icon = Icons.Rounded.Settings,
                            contentDescription = "计时设置",
                            onClick = onOpenTimerSettings,
                        )
                        Box {
                            CircleAction(
                                icon = Icons.Rounded.MoreVert,
                                contentDescription = "更多",
                                onClick = { menuOpen = true },
                            )
                            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                DropdownMenuItem(
                                    text = { Text("查看专注记录") },
                                    onClick = {
                                        menuOpen = false
                                        onOpenHistory()
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("清空所有专注记录") },
                                    onClick = {
                                        menuOpen = false
                                        confirmClear = true
                                    },
                                )
                            }
                        }
                    },
                )
            }
            item {
                Spacer(Modifier.height(18.dp))
                SectionCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "累计专注",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                            Icon(
                                imageVector = Icons.Rounded.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(19.dp),
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Metric(
                                value = "$totalCount",
                                label = "次数",
                                modifier = Modifier.weight(1f),
                            )
                            Metric(
                                value = "$totalMinutes",
                                unit = "分钟",
                                label = "时长",
                                modifier = Modifier.weight(1f),
                            )
                            Metric(
                                value = "$average",
                                unit = "分钟",
                                label = "日均时长",
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .clickable(onClick = onOpenHistory)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "已专注 $activeDays 天 · 连续 $streak 天",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                            )
                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
            item {
                Spacer(Modifier.height(14.dp))
                SectionCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "当日专注",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = day.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.weight(1f))
                            DayArrow(
                                back = true,
                                enabled = day.isAfter(LocalDate.of(2020, 1, 1)),
                                onClick = { day = day.minusDays(1) },
                            )
                            DayArrow(
                                back = false,
                                enabled = day.isBefore(today),
                                onClick = { day = day.plusDays(1) },
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Row {
                            Metric(value = "$dayCount", label = "次数", modifier = Modifier.weight(1f))
                            Metric(
                                value = "$dayMinutes",
                                unit = "分钟",
                                label = "时长",
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (dayMinutes >= settings.dailyGoalMinutes) {
                                    "已达到今日目标"
                                } else {
                                    "距离今日目标还有 ${settings.dailyGoalMinutes - dayMinutes} 分钟"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = "${(dayGoalProgress * 100).roundToInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        GradientBar(progress = dayGoalProgress)
                    }
                }
            }
            item {
                Spacer(Modifier.height(14.dp))
                SectionCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "专注时长分布",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = rangeTitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                text = "分享",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "我在 TokiTodo 的专注时长分布（$rangeTitle）共 $distributionTotal 分钟。",
                                            )
                                        }
                                        context.startActivity(
                                            Intent.createChooser(intent, "分享专注分布"),
                                        )
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            )
                            DayArrow(
                                back = true,
                                enabled = rangeIndex != 3,
                                onClick = {
                                    anchor = when (rangeIndex) {
                                        0 -> anchor.minusDays(1)
                                        1 -> anchor.minusWeeks(1)
                                        2 -> anchor.minusMonths(1)
                                        else -> anchor
                                    }
                                    highlight = null
                                },
                            )
                            DayArrow(
                                back = false,
                                enabled = rangeIndex != 3 && anchor.isBefore(today),
                                onClick = {
                                    anchor = when (rangeIndex) {
                                        0 -> anchor.plusDays(1)
                                        1 -> anchor.plusWeeks(1)
                                        2 -> anchor.plusMonths(1)
                                        else -> anchor
                                    }
                                    highlight = null
                                },
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        PillSegmented(
                            options = rangeLabels,
                            selectedIndex = rangeIndex,
                            onSelect = {
                                rangeIndex = it
                                highlight = null
                            },
                        )
                        if (rangeIndex == 3) {
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                RangeField(
                                    label = "开始",
                                    value = formatDate(customStart),
                                    modifier = Modifier.weight(1f),
                                    onClick = { pickTarget = 0 },
                                )
                                RangeField(
                                    label = "结束",
                                    value = formatDate(customEnd),
                                    modifier = Modifier.weight(1f),
                                    onClick = { pickTarget = 1 },
                                )
                            }
                        }
                        Spacer(Modifier.height(18.dp))
                        if (distributionTotal == 0) {
                            ChartEmpty()
                        } else {
                            FocusBarChart(
                                bars = distribution,
                                highlightIndex = highlight,
                                onHighlight = { highlight = it },
                                height = 172.dp,
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "共 ${formatDuration(distributionTotal)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                            )
                            PillAction(
                                label = "查看专注记录",
                                onClick = onOpenHistory,
                                container = MaterialTheme.colorScheme.surfaceContainerLow,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            item {
                Spacer(Modifier.height(14.dp))
                SectionCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "本月专注时段分布",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = "${distributionMonth.year}年${distributionMonth.monthValue}月",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            DayArrow(
                                back = true,
                                enabled = true,
                                onClick = { distributionMonth = distributionMonth.minusMonths(1) },
                            )
                            DayArrow(
                                back = false,
                                enabled = distributionMonth.isBefore(YearMonth.now()),
                                onClick = { distributionMonth = distributionMonth.plusMonths(1) },
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        if (distributionMonthTotal == 0) {
                            ChartEmpty()
                        } else {
                            TimeOfDayBars(
                                values = monthTimeBuckets.toList(),
                                labels = listOf("凌晨", "上午", "下午", "夜晚"),
                            )
                            Spacer(Modifier.height(14.dp))
                            val bestIndex = monthTimeBuckets.indices.maxByOrNull { monthTimeBuckets[it] } ?: 0
                            Text(
                                text = "你最常在${listOf("凌晨", "上午", "下午", "夜晚")[bestIndex]}专注，" +
                                    "共 ${monthTimeBuckets[bestIndex]} 分钟。",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            item {
                Spacer(Modifier.height(14.dp))
                SectionCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "月度专注统计",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = "${month.year}年${month.monthValue}月",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            DayArrow(
                                back = true,
                                enabled = true,
                                onClick = { month = month.minusMonths(1) },
                            )
                            DayArrow(
                                back = false,
                                enabled = month.isBefore(YearMonth.now()),
                                onClick = { month = month.plusMonths(1) },
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Row {
                            Metric(
                                value = "$monthTotal",
                                unit = "分钟",
                                label = "本月累计",
                                modifier = Modifier.weight(1f),
                            )
                            Metric(
                                value = "$monthActive",
                                unit = "天",
                                label = "专注天数",
                                modifier = Modifier.weight(1f),
                            )
                            Metric(
                                value = "$monthAverage",
                                unit = "分钟",
                                label = "日均",
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Spacer(Modifier.height(18.dp))
                        MonthHeatmap(
                            days = monthDays,
                            onSelect = { date ->
                                day = date
                                anchor = date
                                rangeIndex = 0
                            },
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "颜色越深，专注越久",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.weight(1f),
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(0.25f, 0.45f, 0.65f, 0.9f).forEach { alpha ->
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha)),
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(18.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "最长单次专注",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = formatDuration(Stats.longestSession(
                                    Stats.counted(sessions).filter {
                                        YearMonth.from(Stats.dateOf(it.endedAt)) == month
                                    },
                                )),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    TextButton(onClick = onGoTodos, modifier = Modifier.align(Alignment.Center)) {
                        Text("回到待办，开始下一段专注")
                    }
                }
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("清空所有专注记录？") },
            text = { Text("统计会归零，待办和待办集不受影响。这个操作不能撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmClear = false
                        onClearSessions()
                    },
                ) {
                    Text("清空", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("取消") }
            },
        )
    }

    if (pickTarget != null) {
        val initial = if (pickTarget == 0) customStart else customEnd
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { pickTarget = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = pickerState.selectedDateMillis
                        if (millis != null) {
                            val picked = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            if (pickTarget == 0) {
                                customStart = picked
                                if (customEnd.isBefore(picked)) customEnd = picked
                            } else {
                                customEnd = picked
                                if (customStart.isAfter(picked)) customStart = picked
                            }
                            rangeIndex = 3
                        }
                        pickTarget = null
                    },
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { pickTarget = null }) { Text("取消") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun DayArrow(back: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Icon(
        imageVector = if (back) Icons.Rounded.ChevronLeft else Icons.Rounded.ChevronRight,
        contentDescription = if (back) "上一段" else "下一段",
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 0.9f else 0.25f),
        modifier = Modifier
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(3.dp)
            .size(20.dp),
    )
}

@Composable
private fun RangeField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Rounded.CalendarMonth,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun ChartEmpty() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "暂无专注数据，点击待办上的开始按钮来专注计时吧",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
    }
}
