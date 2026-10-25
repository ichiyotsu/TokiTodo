package com.tokitodo.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tokitodo.app.ui.LocalMotionEnabled
import com.tokitodo.app.ui.Tab
import com.tokitodo.app.ui.pressScale
import com.tokitodo.app.ui.theme.NavBarShape

private data class NavEntry(
    val tab: Tab,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
)

private val entries = listOf(
    NavEntry(Tab.Todos, Icons.Outlined.Checklist, Icons.Filled.Checklist),
    NavEntry(Tab.Collections, Icons.Outlined.ViewList, Icons.Filled.ViewList),
    NavEntry(Tab.Lock, Icons.Outlined.Lock, Icons.Filled.Lock),
    NavEntry(Tab.Stats, Icons.Outlined.PieChart, Icons.Filled.PieChart),
    NavEntry(Tab.Profile, Icons.Outlined.Person, Icons.Filled.Person),
)

/**
 * TokiNote 风格的 M3 Expressive 悬浮底栏：
 * 单层毛玻璃胶囊，未选中只留图标，选中项吞进一枚主色药丸并横向展开文字。
 * 实时模糊由调用方传入的 [glass] 提供，模拟器无 RenderEffect 时走回退底色。
 */
@Composable
fun FloatingNavBar(
    current: Tab,
    onSelect: (Tab) -> Unit,
    modifier: Modifier = Modifier,
    glass: Modifier = Modifier,
    labels: Boolean = true,
) {
    val motion = LocalMotionEnabled.current
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .shadow(elevation = 16.dp, shape = NavBarShape, clip = false)
                .clip(NavBarShape)
                .background(
                    MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.86f),
                )
                .then(glass)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    shape = NavBarShape,
                )
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            entries.forEach { entry ->
                NavItem(
                    entry = entry,
                    selected = current == entry.tab,
                    labels = labels,
                    motion = motion,
                    onSelect = onSelect,
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    entry: NavEntry,
    selected: Boolean,
    labels: Boolean,
    motion: Boolean,
    onSelect: (Tab) -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current
    val colors = MaterialTheme.colorScheme
    val background by animateColorAsState(
        targetValue = if (selected) colors.primary else Color.Transparent,
        animationSpec = tween(if (motion) 180 else 0),
        label = "navBg",
    )
    val foreground by animateColorAsState(
        targetValue = if (selected) colors.onPrimary else colors.onSurfaceVariant,
        animationSpec = tween(if (motion) 180 else 0),
        label = "navFg",
    )
    Row(
        modifier = Modifier
            .height(54.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(
                interactionSource = interaction,
                indication = ripple(color = colors.primary.copy(alpha = 0.25f)),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSelect(entry.tab)
                },
            )
            .pressScale(interaction, pressedScale = 0.92f)
            .padding(horizontal = if (selected) 16.dp else 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (selected) entry.selectedIcon else entry.icon,
            contentDescription = entry.tab.label,
            tint = foreground,
            modifier = Modifier.size(22.dp),
        )
        AnimatedVisibility(
            visible = selected && labels,
            enter = expandHorizontally(
                animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
            ) + fadeIn(),
            exit = shrinkHorizontally(
                animationSpec = spring(dampingRatio = 0.85f, stiffness = 500f),
            ) + fadeOut(),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.width(7.dp))
                Text(
                    text = entry.tab.label,
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = foreground,
                    maxLines = 1,
                )
            }
        }
    }
}
