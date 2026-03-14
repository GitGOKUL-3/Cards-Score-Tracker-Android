package com.cardscoretracker.pro.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MediumBlue,
    onPrimary = White,
    primaryContainer = RoyalBlue,
    onPrimaryContainer = PaleBlue,
    secondary = VibrantOrange,
    onSecondary = White,
    secondaryContainer = Color(0xFF7A3200),
    onSecondaryContainer = LightOrange,
    tertiary = SuccessGreen,
    background = DarkSurface,
    onBackground = White,
    surface = DarkCard,
    onSurface = White,
    surfaceVariant = DarkCardVariant,
    onSurfaceVariant = LightGray,
    error = ErrorRed,
    outline = MediumGray
)

private val LightColorScheme = lightColorScheme(
    primary = RoyalBlue,
    onPrimary = White,
    primaryContainer = LightChipBg,
    onPrimaryContainer = IndigoText,
    secondary = VibrantOrange,
    onSecondary = White,
    secondaryContainer = Color(0xFFFFDDC1),
    onSecondaryContainer = Color(0xFF5A2000),
    tertiary = SuccessGreen,
    background = LightBgStart,
    onBackground = IndigoText,
    surface = LightCard,
    onSurface = IndigoText,
    surfaceVariant = LightChipBg,
    onSurfaceVariant = IndigoSoft,
    error = ErrorRed,
    outline = LightCardBorder
)

@Composable
fun CardScoreTrackerProTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
