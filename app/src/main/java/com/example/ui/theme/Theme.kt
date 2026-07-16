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
    primary = NeonGreen,
    onPrimary = ObsidianBlack,
    secondary = NeonBlue,
    onSecondary = ObsidianBlack,
    tertiary = WarmGold,
    background = ObsidianBlack,
    onBackground = Color(0xFFECEFF1),
    surface = DeepSlateSurface,
    onSurface = Color(0xFFECEFF1),
    surfaceVariant = CardSlate,
    onSurfaceVariant = SoftGrayText,
    outline = BorderGray
  )

private val LightColorScheme = DarkColorScheme // Minimalist Dark theme by default, as requested for nighttime tracking

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark mode as the app focuses on nighttime tracking and high contrast gamification
  dynamicColor: Boolean = false, // Disable dynamic colors to enforce our custom high-fidelity brand palette
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
