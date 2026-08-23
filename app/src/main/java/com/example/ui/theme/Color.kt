package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Modern Light Palette (نظام ألوان عصري واحترافي فاخر - Royal Sapphire & Cool Slate)
val ModernIndigo = Color(0xFF2563EB)          // Vibrant Royal Blue / Indigo
val ModernIndigoDark = Color(0xFF1E3A8A)      // Deep Navy Sapphire
val ModernIndigoLight = Color(0xFFEEF2FF)     // Soft Ice Indigo Tint
val ModernSlateBg = Color(0xFFF8FAFC)         // Crisp Cool Minimalist Background
val ModernBorder = Color(0xFFE2E8F0)          // Slate 200 Micro Precision Border
val ModernCharcoal = Color(0xFF0F172A)        // Deep Midnight Slate 900
val ModernMuted = Color(0xFF64748B)           // Slate 500 Subtitle
val ModernCardBg = Color(0xFFFFFFFF)          // Pure White Card
val ModernItemBg = Color(0xFFF1F5F9)          // Slate 100 Pill & Container
val ModernPillBg = Color(0xFFE2E8F0)          // Secondary Container
val ModernAccentAmber = Color(0xFFF59E0B)     // Radiant Amber Accent
val ModernGreenTone = Color(0xFF10B981)       // Vibrant Emerald Accent

// Modern Dark Mode Palette (النمط الليلي الاحترافي - Deep Midnight Obsidian & Electric Blue)
val NaturalDarkBg = Color(0xFF0B0F17)         // Deep Obsidian Canvas
val NaturalDarkSurface = Color(0xFF131C2E)    // Elevated Slate Surface for Cards
val NaturalDarkItemBg = Color(0xFF1E293B)     // Sleek Dark Item / Pill Container
val NaturalDarkBorder = Color(0xFF2D3C54)     // Precision Slate Dark Border
val NaturalDarkOlive = Color(0xFF60A5FA)      // Luminous Sky / Electric Blue (Primary)
val NaturalDarkOliveDark = Color(0xFF3B82F6)  // Vibrant Accent Blue
val NaturalDarkOliveLight = Color(0xFF1E3A8A) // Deep Navy Accent
val NaturalDarkText = Color(0xFFF8FAFC)       // Crisp Bright White Text
val NaturalDarkMuted = Color(0xFF94A3B8)      // Muted Slate Text

// Dynamic color accessors connecting seamlessly with LocalAppColors
val NaturalOlive: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.primary

val NaturalOliveDark: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.primaryDark

val NaturalOliveLight: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.primaryLight

val NaturalSandBg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.background

val NaturalSandBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.border

val NaturalCharcoal: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.textPrimary

val NaturalMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.textMuted

val NaturalCardBg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.surface

val NaturalItemBg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.itemBg

val NaturalPillBg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.itemBg

val NaturalAccentGold: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.accent

val NaturalGreenTone: Color
    @Composable
    @ReadOnlyComposable
    get() = ModernGreenTone

// Modern Gradients
val GoldGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFF59E0B), Color(0xFFD97706))
)
val OliveGradient: Brush
    @Composable
    @ReadOnlyComposable
    get() = if (LocalAppColors.current.background == NaturalDarkBg) {
        Brush.linearGradient(listOf(NaturalDarkOliveLight, NaturalDarkOliveDark))
    } else {
        Brush.linearGradient(listOf(ModernIndigoDark, ModernIndigo))
    }

val RoyalCardGradient: Brush
    @Composable
    @ReadOnlyComposable
    get() = if (LocalAppColors.current.background == NaturalDarkBg) {
        Brush.linearGradient(listOf(NaturalDarkSurface, Color(0xFF1E293B)))
    } else {
        Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF8FAFC)))
    }

val NightOliveGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF0F172A), Color(0xFF020617))
)
