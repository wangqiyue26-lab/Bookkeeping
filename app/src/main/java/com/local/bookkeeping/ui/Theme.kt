package com.local.bookkeeping.ui

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

val Navy = Color(0xFF0A2856)
val AnodaBlue = Color(0xFF123E86)
val SoftBlue = Color(0xFFEAF0FA)
val AppBackground = Color(0xFFF5F7FB)
val IncomeGreen = Color(0xFF2E9B63)
val ExpenseRed = Color(0xFFC83B4D)
val MutedText = Color(0xFF7C879C)

@Composable
fun LedgerTheme(mode: String, content: @Composable () -> Unit) {
 val dark = mode == "Dark" || (mode == "System" && isSystemInDarkTheme())
 val view = LocalView.current
 SideEffect {
  (view.context as? Activity)?.window?.let { window ->
   window.statusBarColor = (if (dark) Color(0xFF0D1624) else AppBackground).toArgb()
   window.navigationBarColor = (if (dark) Color(0xFF111D2C) else Color.White).toArgb()
   WindowCompat.getInsetsController(window, view).apply {
    isAppearanceLightStatusBars = !dark
    isAppearanceLightNavigationBars = !dark
   }
  }
 }
 val colors = if (dark) darkColorScheme(
  primary = Color(0xFFA9C7FF),
  onPrimary = Color(0xFF061A3A),
  primaryContainer = Color(0xFF1C355D),
  background = Color(0xFF0D1624),
  surface = Color(0xFF152234),
  surfaceVariant = Color(0xFF1C2B40),
  onBackground = Color(0xFFE8EEF8),
  onSurface = Color(0xFFE8EEF8),
  onSurfaceVariant = Color(0xFFB9C4D5),
  secondary = Color(0xFFB6C9E8),
  outline = Color(0xFF718097),
  outlineVariant = Color(0xFF2B3B51)
 ) else lightColorScheme(
  primary = AnodaBlue,
  onPrimary = Color.White,
  primaryContainer = SoftBlue,
  onPrimaryContainer = Navy,
  background = AppBackground,
  surface = Color.White,
  surfaceVariant = Color(0xFFF0F3F8),
  onBackground = Navy,
  onSurface = Navy,
  onSurfaceVariant = MutedText,
  secondary = Color(0xFF60708A),
  outline = Color(0xFFC8CFDA),
  outlineVariant = Color(0xFFE6EAF0)
 )
 MaterialTheme(
  colorScheme = colors,
  shapes = Shapes(
   small = RoundedCornerShape(10.dp),
   medium = RoundedCornerShape(16.dp),
   large = RoundedCornerShape(24.dp)
  ),
  content = content
 )
}
