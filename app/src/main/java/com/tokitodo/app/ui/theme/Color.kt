package com.tokitodo.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/*
 * TokiToDo palette — a quiet pine-green study seed with a persimmon accent.
 * Tokens follow the Material 3 Expressive tonal roles; the artwork layer adds
 * the saturated colour so the chrome itself stays calm.
 */

val primaryLight = Color(0xFF356859)
val onPrimaryLight = Color(0xFFFFFFFF)
val primaryContainerLight = Color(0xFFB9ECD9)
val onPrimaryContainerLight = Color(0xFF00201A)
val secondaryLight = Color(0xFF4E635C)
val onSecondaryLight = Color(0xFFFFFFFF)
val secondaryContainerLight = Color(0xFFD1E8DF)
val onSecondaryContainerLight = Color(0xFF0A1F19)
val tertiaryLight = Color(0xFFA34E33)
val onTertiaryLight = Color(0xFFFFFFFF)
val tertiaryContainerLight = Color(0xFFFFDBCF)
val onTertiaryContainerLight = Color(0xFF3B0A00)
val errorLight = Color(0xFFBA1A1A)
val onErrorLight = Color(0xFFFFFFFF)
val errorContainerLight = Color(0xFFFFDAD6)
val onErrorContainerLight = Color(0xFF93000A)
val backgroundLight = Color(0xFFF7FAF6)
val onBackgroundLight = Color(0xFF191C1A)
val surfaceLight = Color(0xFFF7FAF6)
val onSurfaceLight = Color(0xFF191C1A)
val surfaceVariantLight = Color(0xFFDBE5DF)
val onSurfaceVariantLight = Color(0xFF404944)
val outlineLight = Color(0xFF707974)
val outlineVariantLight = Color(0xFFBFC9C3)
val scrimLight = Color(0xFF000000)
val inverseSurfaceLight = Color(0xFF2E312F)
val inverseOnSurfaceLight = Color(0xFFEFF1EE)
val inversePrimaryLight = Color(0xFF9CD0BE)
val surfaceDimLight = Color(0xFFD8DBD7)
val surfaceBrightLight = Color(0xFFF7FAF6)
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
val surfaceContainerLowLight = Color(0xFFF1F4F0)
val surfaceContainerLight = Color(0xFFECEFEB)
val surfaceContainerHighLight = Color(0xFFE6E9E5)
val surfaceContainerHighestLight = Color(0xFFE0E3DF)

val primaryDark = Color(0xFF9CD0BE)
val onPrimaryDark = Color(0xFF00382D)
val primaryContainerDark = Color(0xFF1C5145)
val onPrimaryContainerDark = Color(0xFFB9ECD9)
val secondaryDark = Color(0xFFB5CCC3)
val onSecondaryDark = Color(0xFF20352E)
val secondaryContainerDark = Color(0xFF364B44)
val onSecondaryContainerDark = Color(0xFFD1E8DF)
val tertiaryDark = Color(0xFFFFB59F)
val onTertiaryDark = Color(0xFF5F1600)
val tertiaryContainerDark = Color(0xFF7E2A17)
val onTertiaryContainerDark = Color(0xFFFFDBCF)
val errorDark = Color(0xFFFFB4AB)
val onErrorDark = Color(0xFF690005)
val errorContainerDark = Color(0xFF93000A)
val onErrorContainerDark = Color(0xFFFFDAD6)
val backgroundDark = Color(0xFF0F1512)
val onBackgroundDark = Color(0xFFDFE4E0)
val surfaceDark = Color(0xFF0F1512)
val onSurfaceDark = Color(0xFFDFE4E0)
val surfaceVariantDark = Color(0xFF404944)
val onSurfaceVariantDark = Color(0xFFBFC9C3)
val outlineDark = Color(0xFF89938E)
val outlineVariantDark = Color(0xFF404944)
val scrimDark = Color(0xFF000000)
val inverseSurfaceDark = Color(0xFFDFE4E0)
val inverseOnSurfaceDark = Color(0xFF2C312E)
val inversePrimaryDark = Color(0xFF356859)
val surfaceDimDark = Color(0xFF0F1512)
val surfaceBrightDark = Color(0xFF353A37)
val surfaceContainerLowestDark = Color(0xFF0A0F0D)
val surfaceContainerLowDark = Color(0xFF171D1A)
val surfaceContainerDark = Color(0xFF1B211E)
val surfaceContainerHighDark = Color(0xFF252B28)
val surfaceContainerHighestDark = Color(0xFF303633)

/** 主色预设：都来自同一套暖中性底色，只替换主色相。 */
enum class ColorPreset(val label: String) {
    Pine("松针绿"),
    Persimmon("柿子橙"),
    Ocean("海盐蓝"),
    Wisteria("紫藤"),
}

val PresetSeedLight = mapOf(
    ColorPreset.Pine to Color(0xFF356859),
    ColorPreset.Persimmon to Color(0xFF9C4A2F),
    ColorPreset.Ocean to Color(0xFF2F5C86),
    ColorPreset.Wisteria to Color(0xFF66508E),
)

val PresetSeedDark = mapOf(
    ColorPreset.Pine to Color(0xFF9CD0BE),
    ColorPreset.Persimmon to Color(0xFFFFB59F),
    ColorPreset.Ocean to Color(0xFFA8C8EA),
    ColorPreset.Wisteria to Color(0xFFCFBBF5),
)

/** 待办卡片的插画主题。 */
enum class TodoArt(val label: String) {
    Sky("晴空"),
    Dusk("黄昏"),
    Mint("薄荷"),
    Blossom("花霞"),
    Forest("森林"),
    Aurora("极光"),
    Dune("沙丘"),
    Berry("莓夜"),
}

/** 每张卡片的背景渐变（顶部 / 中部 / 底部）。 */
data class ArtPalette(
    val top: Color,
    val mid: Color,
    val bottom: Color,
    val glow: Color,
)

val ArtPalettes: Map<TodoArt, ArtPalette> = mapOf(
    TodoArt.Sky to ArtPalette(
        top = Color(0xFF3E6E9E),
        mid = Color(0xFF7FAACB),
        bottom = Color(0xFFD9E7EF),
        glow = Color(0xFFFFF3D6),
    ),
    TodoArt.Dusk to ArtPalette(
        top = Color(0xFF745E86),
        mid = Color(0xFFD6A0A6),
        bottom = Color(0xFFF7D3BC),
        glow = Color(0xFFFFE1B8),
    ),
    TodoArt.Mint to ArtPalette(
        top = Color(0xFF2C7A70),
        mid = Color(0xFF6FB3A4),
        bottom = Color(0xFFCBE7D6),
        glow = Color(0xFFEFFFE9),
    ),
    TodoArt.Blossom to ArtPalette(
        top = Color(0xFF6B4E8E),
        mid = Color(0xFFBD8CB2),
        bottom = Color(0xFFF7C8D6),
        glow = Color(0xFFFFE0EA),
    ),
    TodoArt.Forest to ArtPalette(
        top = Color(0xFF263F36),
        mid = Color(0xFF4E6D57),
        bottom = Color(0xFFA8BF94),
        glow = Color(0xFFE7F2C8),
    ),
    TodoArt.Aurora to ArtPalette(
        top = Color(0xFF1F3355),
        mid = Color(0xFF3E6C74),
        bottom = Color(0xFF9CC7B6),
        glow = Color(0xFFD6FFE9),
    ),
    TodoArt.Dune to ArtPalette(
        top = Color(0xFF8A5A34),
        mid = Color(0xFFC9935F),
        bottom = Color(0xFFF0D3A6),
        glow = Color(0xFFFFE9C4),
    ),
    TodoArt.Berry to ArtPalette(
        top = Color(0xFF3A2233),
        mid = Color(0xFF6E3B50),
        bottom = Color(0xFFB56A80),
        glow = Color(0xFFF7C9D8),
    ),
)

/** 待办集底色：柔和纸色，跟随深浅色主题。 */
data class CollectionTint(val light: Color, val dark: Color)

val CollectionTints = listOf(
    CollectionTint(Color(0xFFDDEFE4), Color(0xFF1C3329)),
    CollectionTint(Color(0xFFDDE8F7), Color(0xFF1C2A3A)),
    CollectionTint(Color(0xFFF2E0D5), Color(0xFF39281E)),
    CollectionTint(Color(0xFFE8E0F7), Color(0xFF2B2440)),
    CollectionTint(Color(0xFFF7E0E4), Color(0xFF3A242A)),
    CollectionTint(Color(0xFFEFF0D9), Color(0xFF31331F)),
)

@Composable
fun collectionTint(index: Int, dark: Boolean): Color {
    val base = CollectionTints[((index % CollectionTints.size) + CollectionTints.size) % CollectionTints.size]
    val color = if (dark) base.dark else base.light
    return lerp(color, MaterialTheme.colorScheme.primary, 0.06f)
}
