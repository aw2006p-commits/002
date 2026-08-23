package com.example.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppThemeColors(
    val background: Color,
    val surface: Color,
    val itemBg: Color,
    val border: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val primary: Color,
    val primaryDark: Color,
    val primaryLight: Color,
    val accent: Color
)

val LightThemeColors = AppThemeColors(
    background = ModernSlateBg,
    surface = ModernCardBg,
    itemBg = ModernItemBg,
    border = ModernBorder,
    textPrimary = ModernCharcoal,
    textMuted = ModernMuted,
    primary = ModernIndigo,
    primaryDark = ModernIndigoDark,
    primaryLight = ModernIndigoLight,
    accent = ModernAccentAmber
)

val DarkThemeColors = AppThemeColors(
    background = NaturalDarkBg,
    surface = NaturalDarkSurface,
    itemBg = NaturalDarkItemBg,
    border = NaturalDarkBorder,
    textPrimary = NaturalDarkText,
    textMuted = NaturalDarkMuted,
    primary = NaturalDarkOlive,
    primaryDark = NaturalDarkOliveDark,
    primaryLight = NaturalDarkOliveLight,
    accent = ModernAccentAmber
)

val LocalAppColors = compositionLocalOf { LightThemeColors }
val LocalFontScale = compositionLocalOf { 1.0f }
