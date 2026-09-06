package com.local.bookkeeping.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.app.Activity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val Navy = Color(0xFF092448)
val IncomeGreen = Color(0xFF28764D)
@Composable
fun LedgerTheme(mode: String, content: @Composable () -> Unit) {
 val dark = mode == "Dark" || (mode == "System" && isSystemInDarkTheme())
 val view = LocalView.current
 SideEffect {
  (view.context as? Activity)?.window?.let { window ->
   WindowCompat.getInsetsController(window, view).apply {
    isAppearanceLightStatusBars = !dark
    isAppearanceLightNavigationBars = !dark
   }
  }
 }
 val colors = if (dark) darkColorScheme(
  primary = Color(0xFF9AC6FF), background = Color(0xFF101A28), surface = Color(0xFF182536),
  onBackground = Color(0xFFE5ECF5), onSurface = Color(0xFFE5ECF5), secondary = Color(0xFFAAC7E8)
 ) else lightColorScheme(
  primary = Color(0xFF0052A4), onPrimary = Color.White, background = Color(0xFFF3F6FA),
  surface = Color.White, onBackground = Navy, onSurface = Navy, secondary = Color(0xFF425976),
  outline = Color(0xFF758295)
 )
 MaterialTheme(colorScheme = colors, shapes = Shapes(
  small = RoundedCornerShape(4.dp), medium = RoundedCornerShape(8.dp), large = RoundedCornerShape(12.dp)
 ), content = content)
}
