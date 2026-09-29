package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val DarkColorScheme =
  darkColorScheme(
    primary = StiDarkPrimary,
    onPrimary = StiDarkOnPrimary,
    primaryContainer = StiDarkPrimaryContainer,
    onPrimaryContainer = StiDarkOnPrimaryContainer,
    secondary = StiDarkSecondary,
    onSecondary = StiDarkOnSecondary,
    secondaryContainer = StiDarkSecondaryContainer,
    onSecondaryContainer = StiDarkOnSecondaryContainer,
    background = StiDarkBackground,
    onBackground = StiDarkOnBackground,
    surface = StiDarkSurface,
    onSurface = StiDarkOnSurface,
    surfaceVariant = StiDarkSurfaceVariant,
    onSurfaceVariant = StiDarkOnSurfaceVariant,
    outline = StiDarkOutline,
    outlineVariant = StiDarkOutlineVariant
  )

private val LightColorScheme =
  lightColorScheme(
    primary = StiLightPrimary,
    onPrimary = StiLightOnPrimary,
    primaryContainer = StiLightPrimaryContainer,
    onPrimaryContainer = Color(0xFF001A3F),
    secondary = StiLightSecondary,
    onSecondary = Color(0xFFFFFFFF),
    background = StiLightBackground,
    onBackground = Color(0xFF0F172A),
    surface = StiLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
  )

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(colorScheme = LightColorScheme, typography = Typography, content = content)
}
