@file:OptIn(ExperimentalTextApi::class)

package com.tokitodo.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tokitodo.app.R

/**
 * Google Sans Flex (variable). The weight axis makes the 600/700 cuts look
 * like the rounded display face Material 3 Expressive uses.
 */
val GoogleSansFlex = FontFamily(
    Font(
        resId = R.font.google_sans_flex,
        weight = FontWeight.Normal,
        style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
    ),
    Font(
        resId = R.font.google_sans_flex,
        weight = FontWeight.Medium,
        style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
    ),
    Font(
        resId = R.font.google_sans_flex,
        weight = FontWeight.SemiBold,
        style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(600)),
    ),
    Font(
        resId = R.font.google_sans_flex,
        weight = FontWeight.Bold,
        style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(700)),
    ),
)

private fun Typography.withFont(family: FontFamily): Typography = copy(
    displayLarge = displayLarge.copy(fontFamily = family),
    displayMedium = displayMedium.copy(fontFamily = family),
    displaySmall = displaySmall.copy(fontFamily = family),
    headlineLarge = headlineLarge.copy(fontFamily = family),
    headlineMedium = headlineMedium.copy(fontFamily = family),
    headlineSmall = headlineSmall.copy(fontFamily = family),
    titleLarge = titleLarge.copy(fontFamily = family),
    titleMedium = titleMedium.copy(fontFamily = family),
    titleSmall = titleSmall.copy(fontFamily = family),
    bodyLarge = bodyLarge.copy(fontFamily = family),
    bodyMedium = bodyMedium.copy(fontFamily = family),
    bodySmall = bodySmall.copy(fontFamily = family),
    labelLarge = labelLarge.copy(fontFamily = family),
    labelMedium = labelMedium.copy(fontFamily = family),
    labelSmall = labelSmall.copy(fontFamily = family),
)

val TokiTypography: Typography = Typography()
    .withFont(GoogleSansFlex)
    .copy(
        displayLarge = Typography().displayLarge.copy(
            fontFamily = GoogleSansFlex,
            fontWeight = FontWeight.SemiBold,
            fontSize = 64.sp,
            lineHeight = 68.sp,
            letterSpacing = (-2).sp,
        ),
        displayMedium = Typography().displayMedium.copy(
            fontFamily = GoogleSansFlex,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-1.2).sp,
        ),
        displaySmall = Typography().displaySmall.copy(
            fontFamily = GoogleSansFlex,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.8).sp,
        ),
        headlineLarge = Typography().headlineLarge.copy(
            fontFamily = GoogleSansFlex,
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp,
            lineHeight = 40.sp,
            letterSpacing = (-0.8).sp,
        ),
        headlineMedium = Typography().headlineMedium.copy(
            fontFamily = GoogleSansFlex,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            letterSpacing = (-0.4).sp,
        ),
        headlineSmall = Typography().headlineSmall.copy(
            fontFamily = GoogleSansFlex,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            lineHeight = 30.sp,
            letterSpacing = (-0.2).sp,
        ),
        titleLarge = Typography().titleLarge.copy(
            fontFamily = GoogleSansFlex,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
        ),
        titleMedium = Typography().titleMedium.copy(
            fontFamily = GoogleSansFlex,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp,
        ),
        titleSmall = Typography().titleSmall.copy(
            fontFamily = GoogleSansFlex,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            lineHeight = 20.sp,
        ),
        bodyLarge = Typography().bodyLarge.copy(
            fontFamily = GoogleSansFlex,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 25.sp,
        ),
        bodyMedium = Typography().bodyMedium.copy(
            fontFamily = GoogleSansFlex,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 21.sp,
        ),
        bodySmall = Typography().bodySmall.copy(
            fontFamily = GoogleSansFlex,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 17.sp,
        ),
        labelLarge = Typography().labelLarge.copy(
            fontFamily = GoogleSansFlex,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            letterSpacing = 0.1.sp,
        ),
        labelMedium = Typography().labelMedium.copy(
            fontFamily = GoogleSansFlex,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            letterSpacing = 0.2.sp,
        ),
        labelSmall = Typography().labelSmall.copy(
            fontFamily = GoogleSansFlex,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            letterSpacing = 0.2.sp,
        ),
    )
