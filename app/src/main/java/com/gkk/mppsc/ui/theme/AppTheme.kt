package com.gkk.mppsc.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────
//  Colour tokens — direct match to your CSS variables
//  navy     → primary
//  saff     → secondary
//  gold     → tertiary
//  bg       → background
//  card     → surface
//  border   → surfaceVariant
//  text     → onBackground
//  muted    → onSurfaceVariant
//  success  → (custom ext)
//  danger   → error
// ─────────────────────────────────────────────────────────────

enum class GKKTheme(val id: String, val displayName: String, val emoji: String) {
    DEFAULT( "default",  "Classic Blue",    "🔵"),
    DARK(    "dark",     "Dark Night",      "🌙"),
    AMOLED(  "amoled",   "AMOLED Black",    "⚫"),
    EMERALD( "emerald",  "Emerald Forest",  "🌿"),
    OCEAN(   "ocean",    "Ocean Breeze",    "🌊"),
    PURPLE(  "purple",   "Royal Purple",    "💜"),
    ROSE(    "rose",     "Rose Petal",      "🌸"),
    SUNSET(  "sunset",   "Sunset Warm",     "🌅"),
    MINT(    "mint",     "Mint Fresh",      "🍃"),
    SAFFRON( "saffron",  "Saffron India",   "🇮🇳")
}

data class GKKColors(
    val navy:       Color,
    val saff:       Color,
    val gold:       Color,
    val bg:         Color,
    val card:       Color,
    val border:     Color,
    val text:       Color,
    val muted:      Color,
    val success:    Color,
    val danger:     Color,
    val warn:       Color,
    val sidebar:    Color,
    val isDark:     Boolean
)

fun gkkColorsFor(theme: GKKTheme): GKKColors = when (theme) {

    GKKTheme.DEFAULT -> GKKColors(
        navy    = Color(0xFF1A237E), saff    = Color(0xFFFF6B00),
        gold    = Color(0xFFF9A825), bg      = Color(0xFFF4F6FB),
        card    = Color(0xFFFFFFFF), border  = Color(0xFFE2E8F0),
        text    = Color(0xFF1E293B), muted   = Color(0xFF64748B),
        success = Color(0xFF15803D), danger  = Color(0xFFDC2626),
        warn    = Color(0xFFD97706), sidebar = Color(0xFF1A237E),
        isDark  = false
    )

    GKKTheme.DARK -> GKKColors(
        navy    = Color(0xFF818CF8), saff    = Color(0xFFFB923C),
        gold    = Color(0xFFFBBF24), bg      = Color(0xFF0F172A),
        card    = Color(0xFF1E293B), border  = Color(0xFF334155),
        text    = Color(0xFFF1F5F9), muted   = Color(0xFF94A3B8),
        success = Color(0xFF4ADE80), danger  = Color(0xFFF87171),
        warn    = Color(0xFFFCD34D), sidebar = Color(0xFF1E293B),
        isDark  = true
    )

    GKKTheme.AMOLED -> GKKColors(
        navy    = Color(0xFF60A5FA), saff    = Color(0xFFF472B6),
        gold    = Color(0xFFFACC15), bg      = Color(0xFF000000),
        card    = Color(0xFF0D0D0D), border  = Color(0xFF1F1F1F),
        text    = Color(0xFFF9FAFB), muted   = Color(0xFF9CA3AF),
        success = Color(0xFF34D399), danger  = Color(0xFFF87171),
        warn    = Color(0xFFFBBF24), sidebar = Color(0xFF0D0D0D),
        isDark  = true
    )

    GKKTheme.EMERALD -> GKKColors(
        navy    = Color(0xFF065F46), saff    = Color(0xFFF59E0B),
        gold    = Color(0xFFFCD34D), bg      = Color(0xFFECFDF5),
        card    = Color(0xFFFFFFFF), border  = Color(0xFFA7F3D0),
        text    = Color(0xFF064E3B), muted   = Color(0xFF6B7280),
        success = Color(0xFF059669), danger  = Color(0xFFDC2626),
        warn    = Color(0xFFD97706), sidebar = Color(0xFF064E3B),
        isDark  = false
    )

    GKKTheme.OCEAN -> GKKColors(
        navy    = Color(0xFF0C4A6E), saff    = Color(0xFF0EA5E9),
        gold    = Color(0xFF38BDF8), bg      = Color(0xFFF0F9FF),
        card    = Color(0xFFFFFFFF), border  = Color(0xFFBAE6FD),
        text    = Color(0xFF082F49), muted   = Color(0xFF64748B),
        success = Color(0xFF0891B2), danger  = Color(0xFFDC2626),
        warn    = Color(0xFFD97706), sidebar = Color(0xFF0C4A6E),
        isDark  = false
    )

    GKKTheme.PURPLE -> GKKColors(
        navy    = Color(0xFF5B21B6), saff    = Color(0xFFA855F7),
        gold    = Color(0xFFE879F9), bg      = Color(0xFFFAF5FF),
        card    = Color(0xFFFFFFFF), border  = Color(0xFFDDD6FE),
        text    = Color(0xFF3B0764), muted   = Color(0xFF7C3AED),
        success = Color(0xFF7C3AED), danger  = Color(0xFFDC2626),
        warn    = Color(0xFFD97706), sidebar = Color(0xFF4C1D95),
        isDark  = false
    )

    GKKTheme.ROSE -> GKKColors(
        navy    = Color(0xFF9F1239), saff    = Color(0xFFF97316),
        gold    = Color(0xFFFB923C), bg      = Color(0xFFFFF1F2),
        card    = Color(0xFFFFFFFF), border  = Color(0xFFFECDD3),
        text    = Color(0xFF881337), muted   = Color(0xFFBE123C),
        success = Color(0xFF15803D), danger  = Color(0xFFDC2626),
        warn    = Color(0xFFD97706), sidebar = Color(0xFF9F1239),
        isDark  = false
    )

    GKKTheme.SUNSET -> GKKColors(
        navy    = Color(0xFFB45309), saff    = Color(0xFFEF4444),
        gold    = Color(0xFFFBBF24), bg      = Color(0xFFFFFBEB),
        card    = Color(0xFFFFFFFF), border  = Color(0xFFFDE68A),
        text    = Color(0xFF78350F), muted   = Color(0xFF92400E),
        success = Color(0xFF65A30D), danger  = Color(0xFFDC2626),
        warn    = Color(0xFFD97706), sidebar = Color(0xFF92400E),
        isDark  = false
    )

    GKKTheme.MINT -> GKKColors(
        navy    = Color(0xFF134E4A), saff    = Color(0xFF10B981),
        gold    = Color(0xFF34D399), bg      = Color(0xFFF0FDFA),
        card    = Color(0xFFFFFFFF), border  = Color(0xFF99F6E4),
        text    = Color(0xFF0F2D2D), muted   = Color(0xFF0D9488),
        success = Color(0xFF059669), danger  = Color(0xFFDC2626),
        warn    = Color(0xFFD97706), sidebar = Color(0xFF134E4A),
        isDark  = false
    )

    GKKTheme.SAFFRON -> GKKColors(
        navy    = Color(0xFF9A3412), saff    = Color(0xFFF97316),
        gold    = Color(0xFFFCD34D), bg      = Color(0xFFFFF7ED),
        card    = Color(0xFFFFFFFF), border  = Color(0xFFFED7AA),
        text    = Color(0xFF7C2D12), muted   = Color(0xFFC2410C),
        success = Color(0xFF15803D), danger  = Color(0xFFDC2626),
        warn    = Color(0xFFD97706), sidebar = Color(0xFF7C2D12),
        isDark  = false
    )
}

/** Convert GKKColors → Material3 ColorScheme so Compose theming works */
fun GKKColors.toColorScheme(): ColorScheme =
    if (isDark) darkColorScheme(
        primary         = navy,
        secondary       = saff,
        tertiary        = gold,
        background      = bg,
        surface         = card,
        surfaceVariant  = border,
        onBackground    = text,
        onSurface       = text,
        onSurfaceVariant= muted,
        error           = danger
    ) else lightColorScheme(
        primary         = navy,
        secondary       = saff,
        tertiary        = gold,
        background      = bg,
        surface         = card,
        surfaceVariant  = border,
        onBackground    = text,
        onSurface       = text,
        onSurfaceVariant= muted,
        error           = danger
    )
