package com.local.bookkeeping

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  enableEdgeToEdge()
  setContent { LedgerHome() }
 }
}

@Composable
fun LedgerHome() {
 MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF0052A4), background = Color(0xFFF4F6FA))) {
  Scaffold { padding ->
   Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
    Text("Dollar Ledger", style = MaterialTheme.typography.headlineMedium)
    Text("LOCAL PERSONAL FINANCE", style = MaterialTheme.typography.labelLarge)
    HorizontalDivider()
    Text("TOTAL BALANCE", style = MaterialTheme.typography.labelLarge)
    Text("¥0.00", style = MaterialTheme.typography.displaySmall)
    Text("Exchange rate unavailable")
   }
  }
 }
}
