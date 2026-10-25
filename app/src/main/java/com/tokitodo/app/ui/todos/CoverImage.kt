package com.tokitodo.app.ui.todos

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tokitodo.app.ui.components.Artwork
import com.tokitodo.app.ui.theme.TodoArt
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

/** 待办封面：有自定义图片就用图片，否则用 Canvas 插画。 */
@Composable
fun CoverArt(
    art: TodoArt,
    coverPath: String?,
    modifier: Modifier = Modifier,
    scrim: Boolean = true,
) {
    val bitmap by produceState<Bitmap?>(null, coverPath) {
        value = coverPath?.let { path ->
            runCatching {
                val options = BitmapFactory.Options().apply { inSampleSize = 2 }
                BitmapFactory.decodeFile(path, options)
            }.getOrNull()
        }
    }
    Box(modifier) {
        val bmp = bitmap
        if (bmp != null) {
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            if (scrim) {
                Canvas(Modifier.fillMaxSize()) {
                    drawRect(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                            startY = size.height * 0.2f,
                            endY = size.height,
                        ),
                    )
                    drawRect(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(Color.Black.copy(alpha = 0.28f), Color.Transparent),
                            endX = size.width * 0.9f,
                        ),
                    )
                }
            }
        } else {
            Artwork(art = art, modifier = Modifier.fillMaxSize(), scrim = scrim)
        }
    }
}

/**
 * 简单的封面裁剪：双指缩放 / 拖动，16:9 裁切框，输出 JPEG 到应用私有目录。
 */
@Composable
fun CoverCropDialog(
    uri: Uri,
    onDismiss: () -> Unit,
    onCropped: (String) -> Unit,
) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(null, uri) {
        value = runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeStream(input, null, options)
                var sample = 1
                while (max(options.outWidth, options.outHeight) / sample > 2400) sample *= 2
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(
                        stream,
                        null,
                        BitmapFactory.Options().apply { inSampleSize = sample },
                    )
                }
            }
        }.getOrNull()
    }
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var viewport by remember { mutableStateOf(IntSize.Zero) }

    fun clampOffset(candidate: Offset, currentScale: Float): Offset {
        val bmp = bitmap ?: return Offset.Zero
        val vw = viewport.width.toFloat()
        val vh = viewport.height.toFloat()
        if (vw <= 0f || vh <= 0f) return Offset.Zero
        val base = max(vw / bmp.width, vh / bmp.height) * currentScale
        val maxX = ((bmp.width * base - vw) / 2f).coerceAtLeast(0f)
        val maxY = ((bmp.height * base - vh) / 2f).coerceAtLeast(0f)
        return Offset(
            candidate.x.coerceIn(-maxX, maxX),
            candidate.y.coerceIn(-maxY, maxY),
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0E1210)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 18.dp),
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
                        modifier = Modifier.size(42.dp),
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Close, contentDescription = "取消")
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "裁剪封面",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
                    Spacer(Modifier.weight(1f))
                    Surface(
                        onClick = {
                            val bmp = bitmap
                            if (bmp != null && viewport.width > 0) {
                                val path = saveCroppedCover(context, bmp, viewport, scale, offset)
                                if (path != null) onCropped(path)
                            }
                        },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(42.dp),
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Check, contentDescription = "完成")
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.Black)
                        .onSizeChanged { viewport = it }
                        .pointerInput(bitmap) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 5f)
                                offset = clampOffset(offset + pan, scale)
                            }
                        },
                ) {
                    val bmp = bitmap
                    if (bmp != null) {
                        Canvas(Modifier.fillMaxSize()) {
                            val vw = size.width
                            val vh = size.height
                            val base = max(vw / bmp.width, vh / bmp.height) * scale
                            val drawW = bmp.width * base
                            val drawH = bmp.height * base
                            val left = (vw - drawW) / 2f + offset.x
                            val top = (vh - drawH) / 2f + offset.y
                            drawImage(
                                image = bmp.asImageBitmap(),
                                dstOffset = IntOffset(left.toInt(), top.toInt()),
                                dstSize = IntSize(drawW.toInt(), drawH.toInt()),
                            )
                            // 三分线
                            for (i in 1..2) {
                                drawLine(
                                    color = Color.White.copy(alpha = 0.25f),
                                    start = Offset(vw * i / 3f, 0f),
                                    end = Offset(vw * i / 3f, vh),
                                    strokeWidth = 1f,
                                )
                                drawLine(
                                    color = Color.White.copy(alpha = 0.25f),
                                    start = Offset(0f, vh * i / 3f),
                                    end = Offset(vw, vh * i / 3f),
                                    strokeWidth = 1f,
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "正在读取图片…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "双指缩放、拖动调整位置，完成后的图片会裁成 16:9。",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.55f),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

private fun saveCroppedCover(
    context: android.content.Context,
    source: Bitmap,
    viewport: IntSize,
    scale: Float,
    offset: Offset,
): String? = runCatching {
    val outputW = 1280
    val outputH = 720
    val bitmap = Bitmap.createBitmap(outputW, outputH, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val factor = outputW / viewport.width.toFloat()
    val base = max(
        viewport.width / source.width.toFloat(),
        viewport.height / source.height.toFloat(),
    ) * scale
    val drawW = source.width * base * factor
    val drawH = source.height * base * factor
    val left = (outputW - drawW) / 2f + offset.x * factor
    val top = (outputH - drawH) / 2f + offset.y * factor
    canvas.drawBitmap(
        source,
        null,
        android.graphics.RectF(left, top, left + drawW, top + drawH),
        android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG),
    )
    val dir = File(context.filesDir, "covers").apply { mkdirs() }
    val file = File(dir, "cover_${UUID.randomUUID()}.jpg")
    FileOutputStream(file).use { stream ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
    }
    bitmap.recycle()
    file.absolutePath
}.getOrNull()
