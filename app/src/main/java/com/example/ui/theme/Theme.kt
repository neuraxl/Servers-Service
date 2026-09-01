package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = ElectricCyan,
    onPrimary = Color(0xFF003640),
    primaryContainer = Color(0xFF004E5C),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = InfoBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1E3A8A),
    onSecondaryContainer = Color(0xFFDBEAFE),
    tertiary = PurpleAccent,
    onTertiary = Color.White,
    background = DarkNocBackground,
    onBackground = DarkOnBackground,
    surface = DarkNocSurface,
    onSurface = DarkOnBackground,
    surfaceVariant = DarkNocSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkNocBorder,
    error = CriticalRed,
    onError = Color.White
  )

private val LightColorScheme =
  lightColorScheme(
    primary = LightOpsPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBAE6FD),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = InfoBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDBEAFE),
    onSecondaryContainer = Color(0xFF1E40AF),
    tertiary = PurpleAccent,
    onTertiary = Color.White,
    background = LightOpsBackground,
    onBackground = LightOpsOnBackground,
    surface = LightOpsSurface,
    onSurface = LightOpsOnBackground,
    surfaceVariant = LightOpsSurfaceVariant,
    onSurfaceVariant = LightOpsOnSurfaceVariant,
    outline = Color(0xFFCBD5E1),
    error = CriticalRed,
    onError = Color.White
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to sleek NOC dark theme for server monitoring
  dynamicColor: Boolean = false, // Keep NOC branding colors consistent
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
