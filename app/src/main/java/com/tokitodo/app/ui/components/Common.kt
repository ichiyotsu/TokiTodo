package com.tokitodo.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tokitodo.app.ui.TokiMotion
import com.tokitodo.app.ui.pressScale
import com.tokitodo.app.ui.theme.PillShape
import com.tokitodo.app.ui.theme.SectionCardShape

/** 顶部操作区：TokiUI 大标题 + 右侧操作 + 可选胶囊。 */
@Composable
fun PageTopBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    chip: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 20.dp, top = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            if (title != null) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 40.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            } else {
                Spacer(Modifier.weight(1f))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                content = actions,
            )
        }
        if (chip != null) {
            Spacer(Modifier.height(12.dp))
            chip()
        }
    }
}

/**
 * 页面头部：大标题 + 圆形操作按钮，下面可以挂一枚「学霸模式」小胶囊。
 * 与五张参考图的结构一致：标题在左，操作在右，胶囊在标题下方。
 */
@Composable
fun PageHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    chip: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 18.dp, top = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                content = actions,
            )
        }
        if (chip != null) {
            Spacer(Modifier.height(10.dp))
            chip()
        }
    }
}

/** 顶部的圆形操作按钮，白底、轻阴影、可按压回弹。 */
@Composable
fun CircleAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    size: Int = 46,
    hint: String = contentDescription,
) {
    val interaction = remember { MutableInteractionSource() }
    var showHint by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(showHint) {
        if (showHint) {
            kotlinx.coroutines.delay(1800)
            showHint = false
        }
    }
    Box {
        Surface(
            modifier = modifier
                .size(size.dp)
                .pressScale(interaction, pressedScale = 0.90f)
                .clip(CircleShape)
                .combinedClickable(
                    interactionSource = interaction,
                    indication = ripple(color = contentColor.copy(alpha = 0.2f)),
                    onClick = onClick,
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showHint = true
                    },
                ),
            shape = CircleShape,
            color = container,
            contentColor = contentColor,
            shadowElevation = 3.dp,
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    modifier = Modifier.size((size * 0.46f).dp),
                )
            }
        }
        DropdownMenu(
            expanded = showHint,
            onDismissRequest = { showHint = false },
            shape = MaterialTheme.shapes.small,
            containerColor = MaterialTheme.colorScheme.inverseSurface,
        ) {
            Text(
                text = hint,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.inverseOnSurface,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}

/** 「点击开启学霸模式」胶囊。开启后变成主色容器并显示星星。 */
@Composable
fun StudyModeChip(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabledLabel: String = "强制学习模式已开启",
    disabledLabel: String = "点击开启强制学习模式",
) {
    val colors = MaterialTheme.colorScheme
    val background by animateColorAsState(
        targetValue = if (enabled) colors.primary else colors.surfaceContainerLowest,
        animationSpec = androidx.compose.animation.core.tween(TokiMotion.standard),
        label = "studyChipBg",
    )
    val foreground by animateColorAsState(
        targetValue = if (enabled) colors.onPrimary else colors.onSurfaceVariant,
        animationSpec = androidx.compose.animation.core.tween(TokiMotion.standard),
        label = "studyChipFg",
    )
    val interaction = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = modifier
            .height(32.dp)
            .clip(PillShape)
            .background(background)
            .border(1.dp, colors.outlineVariant.copy(alpha = if (enabled) 0f else 0.55f), PillShape)
            .clickable(
                interactionSource = interaction,
                indication = ripple(color = foreground.copy(alpha = 0.25f)),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
            )
            .pressScale(interaction, pressedScale = 0.95f)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = null,
            tint = foreground,
            modifier = Modifier.size(15.dp),
        )
        Text(
            text = if (enabled) enabledLabel else disabledLabel,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = foreground,
        )
    }
}

/** 白底分段大卡：设置、统计都用它。 */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = SectionCardShape,
        color = color,
        shadowElevation = 1.dp,
    ) {
        Column(content = content)
    }
}

@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        trailing?.invoke()
    }
}

/** 设置行：彩色圆角图标 + 标题 / 说明 + 箭头。 */
@Composable
fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    tint: Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    showChevron: Boolean = true,
    iconBackground: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interaction,
                        indication = ripple(),
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            )
            .padding(horizontal = 18.dp, vertical = 14.dp)
            .heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        } else if (showChevron && onClick != null) {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/** 空状态：柔和的圆形图标 + 一句说明 + 可选的次级操作。 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    hint: String? = null,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    tint: Color = MaterialTheme.colorScheme.primary,
    hintColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.10f)),
            )
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(tint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(28.dp),
                )
            }
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = tint.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp, end = 8.dp)
                    .size(13.dp),
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (hint != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = hintColor,
                textAlign = TextAlign.Center,
            )
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(14.dp))
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(PillShape)
                    .clickable(onClick = onAction)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    }
}

/** 胶囊分段选择器：日 / 周 / 月 / 自定义。 */
@Composable
fun PillSegmented(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(PillShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEachIndexed { index, option ->
            val selected = index == selectedIndex
            val background by animateColorAsState(
                targetValue = if (selected) {
                    MaterialTheme.colorScheme.surfaceContainerLowest
                } else {
                    Color.Transparent
                },
                animationSpec = androidx.compose.animation.core.tween(TokiMotion.standard),
                label = "segBg",
            )
            val foreground by animateColorAsState(
                targetValue = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                animationSpec = androidx.compose.animation.core.tween(TokiMotion.standard),
                label = "segFg",
            )
            Text(
                text = option,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = foreground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .clip(PillShape)
                    .background(background)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onSelect(index) }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            )
        }
    }
}

/** 统计卡里的数字。 */
@Composable
fun Metric(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    unit: String? = null,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = valueColor,
            )
            if (unit != null) {
                Spacer(Modifier.width(2.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 5.dp),
                )
            }
        }
    }
}

/** 小圆点 / 状态徽标。 */
@Composable
fun StatusDot(color: Color, size: Int = 8) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color),
    )
}

/** 药丸强调按钮（锁机设置、查看记录等）。 */
@Composable
fun PillAction(
    label: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.tertiaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onTertiaryContainer,
) {
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        modifier = modifier.pressScale(interaction, pressedScale = 0.95f),
        shape = PillShape,
        color = container,
        contentColor = contentColor,
        interactionSource = interaction,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp))
            }
            Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** 弹层顶部的拖拽条。 */
@Composable
fun SheetGrabber(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(top = 10.dp, bottom = 6.dp)
            .width(38.dp)
            .height(4.dp)
            .clip(PillShape)
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)),
    )
}
