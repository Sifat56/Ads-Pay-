package com.adspay.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = SurfaceWhite,
    primaryContainer = PurpleLighter,
    onPrimaryContainer = PurpleDarker,
    secondary = PurpleDark,
    onSecondary = SurfaceWhite,
    secondaryContainer = PurpleSubtle,
    onSecondaryContainer = PurpleDark,
    tertiary = GoldAccent,
    onTertiary = SurfaceWhite,
    tertiaryContainer = GoldLight,
    onTertiaryContainer = TextPrimary,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = PurpleSubtle,
    onSurfaceVariant = TextSecondary,
    outline = BorderLight,
    error = RedError,
    onError = SurfaceWhite
)

private val DarkColorScheme = darkColorScheme(
    primary = PurpleLight,
    onPrimary = SurfaceWhite,
    primaryContainer = PurpleDarker,
    onPrimaryContainer = PurpleLighter,
    secondary = PurplePrimary,
    onSecondary = SurfaceWhite,
    secondaryContainer = SurfaceDark,
    onSecondaryContainer = PurpleLighter,
    tertiary = GoldAccent,
    onTertiary = SurfaceWhite,
    tertiaryContainer = GoldLight,
    onTertiaryContainer = TextPrimary,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark,
    error = RedError,
    onError = SurfaceWhite
)

val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun AdsPayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = colorScheme.primary.toArgb()
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
