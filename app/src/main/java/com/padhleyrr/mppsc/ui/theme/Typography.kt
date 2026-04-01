package com.padhleyrr.mppsc.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.padhleyrr.mppsc.R

// ── Font families matching your CSS (DM Sans body, Syne headings) ──
val DMSans = FontFamily(
    Font(R.font.dmsans_regular,  FontWeight.Normal),
    Font(R.font.dmsans_medium,   FontWeight.Medium),
    Font(R.font.dmsans_semibold, FontWeight.SemiBold)
)

val Syne = FontFamily(
    Font(R.font.syne_semibold, FontWeight.SemiBold),
    Font(R.font.syne_bold,     FontWeight.Bold),
    Font(R.font.syne_extrabold,FontWeight.ExtraBold)
)

val GKKTypography = Typography(
    // Page titles — font-size:22px font-weight:800 (Syne)
    headlineLarge = TextStyle(
        fontFamily = Syne,
        fontWeight = FontWeight.ExtraBold,
        fontSize   = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.3).sp
    ),
    // Section titles — font-size:15px font-weight:700 (Syne)
    headlineMedium = TextStyle(
        fontFamily = Syne,
        fontWeight = FontWeight.Bold,
        fontSize   = 15.sp,
        lineHeight = 20.sp
    ),
    // Stat values — font-size:28px font-weight:800 (Syne)
    displayLarge = TextStyle(
        fontFamily = Syne,
        fontWeight = FontWeight.ExtraBold,
        fontSize   = 28.sp,
        lineHeight = 34.sp
    ),
    // Body text — font-size:14px (DM Sans)
    bodyLarge = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        lineHeight = 21.sp
    ),
    // Question text — font-size:15px font-weight:600
    bodyMedium = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 15.sp,
        lineHeight = 23.sp
    ),
    // Small labels — font-size:12px
    bodySmall = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.Normal,
        fontSize   = 12.sp,
        lineHeight = 18.sp
    ),
    // Nav items — font-size:13px font-weight:500
    labelLarge = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.Medium,
        fontSize   = 13.sp
    ),
    // Badges — font-size:10px font-weight:700
    labelSmall = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.Bold,
        fontSize   = 10.sp,
        letterSpacing = 0.8.sp
    )
)
