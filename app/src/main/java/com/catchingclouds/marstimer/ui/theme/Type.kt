package com.catchingclouds.marstimer.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.catchingclouds.marstimer.R

val NotoSans: FontFamily by lazy {
    try {
        FontFamily(
            Font(R.font.notosans_light, FontWeight.Light),
            Font(R.font.notosans_regular, FontWeight.Normal)
        )
    } catch (e: Throwable) {
        // In some preview or tooling environments resource loading can fail during class init.
        // Fall back to the default font to avoid NoClassDefFoundError.
        FontFamily.Default
    }
}

// Set of Material typography styles to start with
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = NotoSans,
        fontWeight = FontWeight.Light,
        fontSize = 57.sp
    ),
    displayMedium = TextStyle(
        fontFamily = NotoSans,
        fontWeight = FontWeight.Light,
        fontSize = 45.sp
    ),
    titleMedium = TextStyle(
        fontFamily = NotoSans,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = NotoSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = NotoSans,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 2.sp
    )
)
