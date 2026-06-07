package com.example.recipeapp.ui.theme

import android.app.Activity
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

private val LightColorScheme = lightColorScheme(
    primary = SageGreen,
    onPrimary = WarmWhite,
    primaryContainer = Color(0xFFD4E4D6),
    onPrimaryContainer = SageGreenDark,
    secondary = AccentOrange,
    onSecondary = WarmWhite,
    background = Cream,
    onBackground = Charcoal,
    surface = WarmWhite,
    onSurface = Charcoal,
    surfaceVariant = LightGray,
    onSurfaceVariant = SoftGray,
    outline = LightGray
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9BBFA0),
    onPrimary = Color(0xFF1A2E1D),
    primaryContainer = SageGreenDark,
    onPrimaryContainer = Color(0xFFD4E4D6),
    secondary = Color(0xFFF4A896),
    onSecondary = Color(0xFF3D1F14),
    background = Color(0xFF1A1A1A),
    onBackground = Color(0xFFE8E4DF),
    surface = Color(0xFF242424),
    onSurface = Color(0xFFE8E4DF),
    surfaceVariant = Color(0xFF3A3A3A),
    onSurfaceVariant = Color(0xFFB0B0B0),
    outline = Color(0xFF4A4A4A)
)

@Composable
fun RecipeAppTheme(
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
