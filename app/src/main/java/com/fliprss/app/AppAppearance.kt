package com.fliprss.app

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.fliprss.app.data.AppTheme

internal fun appColors(theme: AppTheme, systemDark: Boolean): ColorScheme {
    val dark = theme == AppTheme.DARK || theme == AppTheme.OLED || (theme == AppTheme.SYSTEM && systemDark)
    val colors = if (dark) darkColorScheme(primary = Color(0xFFFFB4A8), background = Color(0xFF191C18),
        surface = Color(0xFF191C18), surfaceVariant = Color(0xFF2B3029))
    else lightColorScheme(primary = Color(0xFFB83224), background = Color(0xFFF7F5EF), surface = Color(0xFFF7F5EF),
        onSurface = Color(0xFF242820), onBackground = Color(0xFF242820), surfaceVariant = Color(0xFFEAE8E0))
    return if (theme == AppTheme.OLED) colors.copy(background = Color.Black, surface = Color.Black,
        surfaceVariant = Color.Black, surfaceDim = Color.Black, surfaceBright = Color.Black,
        surfaceContainerLowest = Color.Black, surfaceContainerLow = Color.Black, surfaceContainer = Color.Black,
        surfaceContainerHigh = Color.Black, surfaceContainerHighest = Color.Black, surfaceTint = Color.Transparent)
    else colors
}
