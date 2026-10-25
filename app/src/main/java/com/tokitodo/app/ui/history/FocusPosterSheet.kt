package com.tokitodo.app.ui.history

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.draw.drawWithContent
import com.tokitodo.app.data.FocusSession
import com.tokitodo.app.data.SessionKind
import com.tokitodo.app.data.Stats
import com.tokitodo.app.ui.components.SheetGrabber
import com.tokitodo.app.ui.formatDate
import com.tokitodo.app.ui.theme.SheetShape
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private enum class PosterRange(val label: String) {
    Day("日"),
    Week("周"),
    Month("月"),
    Custom("自定义"),
}

private data class PieSlice(
    val label: String,
    val minutes: Int,
    val color: Color,
)

/**
 * 分享海报：日期、专注天数 / 连续天数、次数与时长、时长分布饼图、Logo。
 * 支持日 / 周 / 月 / 自定义范围，支持保存到相册。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusPosterSheet(
    sessions: List<FocusSession>,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var rangeIndex by remember { mutableIntStateOf(0) }
    var customStart by remember { mutableStateOf(LocalDate.now().minusDays(6)) }
    var customEnd by remember { mutableStateOf(LocalDate.now()) }
    var pickTarget by remember { mutableIntStateOf(-1) }

    val range: Pair<LocalDate, LocalDate> = when (PosterRange.entries[rangeIndex]) {
        PosterRange.Day -> selectedDate to selectedDate
        PosterRange.Week -> {
            val monday = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            monday to monday.plusDays(6)
        }

        PosterRange.Month -> YearMonth.from(selectedDate).atDay(1) to YearMonth.from(selectedDate).atEndOfMonth()
        PosterRange.Custom -> customStart to customEnd
    }
    val rangeSessions = sessions.filter {
        val date = Stats.dateOf(it.endedAt)
        !date.isBefore(range.first) && !date.isAfter(range.second)
    }
    val daySessions = sessions.filter { Stats.dateOf(it.endedAt) == selectedDate }
    val dayMinutes = daySessions.sumOf { it.minutes }
    val dayCount = daySessions.size
    val activeDays = sessions
        .filter { it.kind == SessionKind.Focus }
        .map { Stats.dateOf(it.endedAt) }
        .distinct()
        .size
    val streak = Stats.streak(sessions, LocalDate.now())

    val slices = buildSlices(rangeSessions)
    val totalSliceMinutes = slices.sumOf { it.minutes }.coerceAtLeast(1)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF101513)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        onClick = onDismiss,
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.10f),
                        contentColor = Color.White,
                        modifier = Modifier.size(40.dp),
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Close, contentDescription = "关闭")
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "专注海报",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
                    Spacer(Modifier.weight(1f))
                    Surface(
                        onClick = {
                            scope.launch {
                                val bitmap = runCatching { graphicsLayer.toImageBitmap() }.getOrNull()
                                val ok = bitmap?.let { savePoster(context, it.asAndroidBitmap()) } ?: false
                                Toast.makeText(
                                    context,
                                    if (ok) "海报已保存到相册" else "保存失败",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        },
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.10f),
                        contentColor = Color.White,
                        modifier = Modifier.size(40.dp),
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Download, contentDescription = "下载海报")
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawWithContent {
                                graphicsLayer.record { this@drawWithContent.drawContent() }
                                drawLayer(graphicsLayer)
                            },
                        shape = RoundedCornerShape(28.dp),
                        color = Color(0xFFFFF8F2),
                    ) {
                        PosterContent(
                            selectedDate = selectedDate,
                            rangeSessions = rangeSessions,
                            dayMinutes = dayMinutes,
                            dayCount = dayCount,
                            activeDays = activeDays,
                            streak = streak,
                            slices = slices,
                            totalSliceMinutes = totalSliceMinutes,
                            rangeLabel = PosterRange.entries[rangeIndex].label,
                            onPrevious = {
                                if (rangeIndex != 3) selectedDate = selectedDate.minusDays(1)
                            },
                            onNext = {
                                if (rangeIndex != 3) selectedDate = selectedDate.plusDays(1)
                            },
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PosterRange.entries.forEachIndexed { index, item ->
                            val selected = rangeIndex == index
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) Color(0xFF254A3F) else Color.White.copy(alpha = 0.8f),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(
                                        if (selected) Color(0xFFD9EAD9) else Color.White.copy(alpha = 0.10f),
                                    )
                                    .clickable { rangeIndex = index }
                                    .padding(horizontal = 17.dp, vertical = 9.dp),
                            )
                        }
                    }
                    if (rangeIndex == 3) {
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            DateChip(
                                label = "开始",
                                date = customStart,
                                modifier = Modifier.weight(1f),
                                onClick = { pickTarget = 0 },
                            )
                            DateChip(
                                label = "结束",
                                date = customEnd,
                                modifier = Modifier.weight(1f),
                                onClick = { pickTarget = 1 },
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "海报会保存到系统相册 Pictures/TokiToDo",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.45f),
                        modifier = Modifier.padding(start = 4.dp),
                    )
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }

    if (pickTarget >= 0) {
        val initial = if (pickTarget == 0) customStart else customEnd
        val state = rememberDatePickerState(initialSelectedDateMillis = initial.toEpochDay() * 86_400_000L)
        DatePickerDialog(
            onDismissRequest = { pickTarget = -1 },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { millis ->
                            val date = LocalDate.ofEpochDay(millis / 86_400_000L)
                            if (pickTarget == 0) {
                                customStart = date
                                if (customEnd.isBefore(date)) customEnd = date
                            } else {
                                customEnd = date
                                if (customStart.isAfter(date)) customStart = date
                            }
                        }
                        pickTarget = -1
                    },
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { pickTarget = -1 }) { Text("取消") }
            },
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
private fun PosterContent(
    selectedDate: LocalDate,
    rangeSessions: List<FocusSession>,
    dayMinutes: Int,
    dayCount: Int,
    activeDays: Int,
    streak: Int,
    slices: List<PieSlice>,
    totalSliceMinutes: Int,
    rangeLabel: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(22.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${selectedDate.year}年${selectedDate.monthValue}月${selectedDate.dayOfMonth}日",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                    ),
                    color = Color(0xFF25332C),
                )
                Text(
                    text = "TokiToDo · 专注记录",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF6A7B70),
                )
            }
            Surface(
                onClick = onPrevious,
                shape = CircleShape,
                color = Color(0xFFEDE3D8),
                modifier = Modifier.size(30.dp),
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.ChevronLeft,
                        contentDescription = "前一天",
                        tint = Color(0xFF5A4C40),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Spacer(Modifier.width(6.dp))
            Surface(
                onClick = onNext,
                shape = CircleShape,
                color = Color(0xFFEDE3D8),
                modifier = Modifier.size(30.dp),
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.ChevronRight,
                        contentDescription = "后一天",
                        tint = Color(0xFF5A4C40),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PosterStat("$activeDays", "天", "累计专注", Modifier.weight(1f))
            PosterStat("$streak", "天", "连续专注", Modifier.weight(1f))
        }
        Spacer(Modifier.height(14.dp))
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFFF3E9DE),
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "当日专注",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF7A6758),
                    )
                    Text(
                        text = "$dayCount 次",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF25332C),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "时长",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF7A6758),
                    )
                    Text(
                        text = "$dayMinutes 分钟",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF25332C),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "范围",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF7A6758),
                    )
                    Text(
                        text = rangeLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF25332C),
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(
            text = "专注时长分布 · $totalSliceMinutes 分钟",
            style = MaterialTheme.typography.titleSmall,
            color = Color(0xFF25332C),
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(148.dp)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center,
            ) {
                if (slices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(148.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1E7DC)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "暂无数据",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8A7768),
                        )
                    }
                } else {
                    Canvas(Modifier.size(148.dp)) {
                        var startAngle = -90f
                        slices.forEach { slice ->
                            val sweep = 360f * slice.minutes / totalSliceMinutes
                            drawArc(
                                color = slice.color,
                                startAngle = startAngle,
                                sweepAngle = sweep,
                                useCenter = true,
                            )
                            startAngle += sweep
                        }
                    }
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                slices.take(6).forEach { slice ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(slice.color),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = slice.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4C4038),
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                        )
                        Text(
                            text = "${(slice.minutes * 100f / totalSliceMinutes).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8A7768),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Canvas(Modifier.size(22.dp)) {
                drawCircle(Color(0xFFE4573D), radius = size.minDimension * 0.42f)
                drawCircle(
                    color = Color(0xFF6FA287),
                    radius = size.minDimension * 0.16f,
                    center = Offset(size.width * 0.62f, size.height * 0.12f),
                )
            }
            Spacer(Modifier.width(7.dp))
            Text(
                text = "TokiToDo",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                ),
                color = Color(0xFF3D4A42),
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = "把注意力留给真正重要的事",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF8A7768),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PosterStat(
    value: String,
    unit: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF3E9DE),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF25332C),
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF7A6758),
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF7A6758),
            )
        }
    }
}

@Composable
private fun DateChip(label: String, date: LocalDate, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.10f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.55f),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = formatDate(date),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            modifier = Modifier.weight(1f),
        )
    }
}

private fun buildSlices(sessions: List<FocusSession>): List<PieSlice> {
    val bounds = listOf(
        "Pomodoro" to Color(0xFF356859),
        "Lock" to Color(0xFFA9502F),
        "Break" to Color(0xFF7FA98E),
        "Nap" to Color(0xFFC9A86A),
        "Other" to Color(0xFF8C7CB0),
    )
    val focus = sessions.filter { it.kind == SessionKind.Focus || it.kind == SessionKind.Lock }
    val grouped = focus.groupBy { session ->
        session.title.ifBlank {
            when (session.kind) {
                SessionKind.Lock -> "锁机"
                SessionKind.Focus -> "自由专注"
                SessionKind.Nap -> "小憩"
                SessionKind.Break -> "休息"
            }
        }
    }.map { (title, list) -> title to list.sumOf { it.minutes } }
        .sortedByDescending { it.second }
    val top = grouped.take(5)
    val rest = grouped.drop(5).sumOf { it.second }
    val result = top.mapIndexed { index, (title, minutes) ->
        PieSlice(title, minutes, bounds[index % bounds.size].second)
    }.toMutableList()
    if (rest > 0) result.add(PieSlice("其他", rest, bounds.last().second))
    return result
}

private fun savePoster(context: android.content.Context, bitmap: Bitmap): Boolean = runCatching {
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "TokiToDo-${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(
            MediaStore.Images.Media.RELATIVE_PATH,
            Environment.DIRECTORY_PICTURES + "/TokiToDo",
        )
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }
    val uri = context.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        values,
    ) ?: return false
    context.contentResolver.openOutputStream(uri)?.use { stream ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
    }
    values.clear()
    values.put(MediaStore.Images.Media.IS_PENDING, 0)
    context.contentResolver.update(uri, values, null, null)
    true
}.getOrDefault(false)
