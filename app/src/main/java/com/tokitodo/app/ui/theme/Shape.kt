package com.tokitodo.app.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Material 3 Expressive corner scale — noticeably rounder than the baseline. */
val TokiShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(38.dp),
)

/** 待办插画卡：TokiUI 26dp 等圆角。 */
val ArtCardShape = RoundedCornerShape(26.dp)

/** 统计 / 设置的分段白卡。 */
val SectionCardShape = RoundedCornerShape(28.dp)

/** 大号卡片（锁机页），TokiUI 26dp。 */
val LockCardShape = RoundedCornerShape(26.dp)

/** 顶部圆形操作按钮。 */
val ActionButtonShape = CircleShape

/** 底部导航 / 悬浮胶囊。 */
val NavBarShape = RoundedCornerShape(34.dp)

/** 底部弹层。 */
val SheetShape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp)

/** 小药丸按钮。 */
val PillShape = CircleShape
