package com.tokitodo.app.ui.todos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tokitodo.app.data.Todo
import com.tokitodo.app.ui.components.Artwork
import com.tokitodo.app.ui.pressScale
import com.tokitodo.app.ui.theme.ArtCardShape

/**
 * 待办插画卡：整张卡片就是一张海报。
 * 标题与时长压在左下角的暗角里，「开始」是一枚半透明玻璃药丸。
 */
@Composable
fun TodoCard(
    todo: Todo,
    collectionName: String?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = ArtCardShape
    val interaction = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .alpha(if (todo.done) 0.62f else 1f)
            .shadow(elevation = 10.dp, shape = shape, clip = false)
            .clip(shape)
            .combinedClickable(
                interactionSource = interaction,
                indication = ripple(bounded = true, color = Color.White),
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                },
            )
            .pressScale(interaction, pressedScale = 0.975f),
    ) {
        CoverArt(
            art = todo.art,
            coverPath = todo.coverPath,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.22f),
                    shape = shape,
                ),
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 18.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = todo.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 24.sp,
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.30f),
                            offset = Offset(0f, 2f),
                            blurRadius = 10f,
                        ),
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (todo.done) TextDecoration.LineThrough else null,
                )
                Spacer(Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.82f),
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${todo.minutes} 分钟",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.88f),
                    )
                    if (collectionName != null) {
                        Spacer(Modifier.width(7.dp))
                        Box(
                            modifier = Modifier
                                .size(2.5.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.5f)),
                        )
                        Spacer(Modifier.width(7.dp))
                        Text(
                            text = collectionName,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.78f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (todo.focusCount > 0) {
                        Spacer(Modifier.width(7.dp))
                        Box(
                            modifier = Modifier
                                .size(2.5.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.5f)),
                        )
                        Spacer(Modifier.width(7.dp))
                        Text(
                            text = "专注 ${todo.focusCount} 次",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.88f),
                            maxLines = 1,
                        )
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Surface(
                onClick = onToggleDone,
                shape = CircleShape,
                color = Color.White.copy(alpha = if (todo.done) 0.95f else 0.16f),
                contentColor = if (todo.done) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Color.White
                },
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color.White.copy(alpha = 0.30f),
                ),
                modifier = Modifier.size(32.dp),
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Check, contentDescription = "完成待办", modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.width(8.dp))
            Surface(
                onClick = onClick,
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.20f),
                contentColor = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.38f)),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "开始",
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}
