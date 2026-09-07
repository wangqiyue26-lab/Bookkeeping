package com.local.bookkeeping.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.local.bookkeeping.domain.Money

@Composable
fun WalletScreen(state: LedgerState) {
 LazyColumn(
  contentPadding = PaddingValues(bottom = 32.dp),
  verticalArrangement = Arrangement.spacedBy(20.dp)
 ) {
  item {
   Column(
    modifier = Modifier
     .fillMaxWidth()
     .background(Navy)
     .padding(horizontal = 20.dp, vertical = 54.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(10.dp)
   ) {
    Text(
     text = state.netUsdDisplay(),
     color = Color.White,
     fontSize = 64.sp,
     lineHeight = 68.sp,
     fontWeight = FontWeight.Bold
    )
    if (state.settings.showCnySecondaryAmount) {
     Text(
      text = "≈ ${state.netCnyDisplay()}",
      style = MaterialTheme.typography.titleMedium,
      color = Color.White.copy(alpha = 0.76f)
     )
    }
   }
  }

  item {
   Row(
    Modifier.fillMaxWidth().padding(horizontal = 20.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp)
   ) {
    WalletMetric("ASSETS", state.usd(state.totalAssetCny), Modifier.weight(1f))
    WalletMetric("DEBT", Money.display(state.totalCreditCardDebtUsdCents), Modifier.weight(1f))
   }
  }
 }
}

@Composable
private fun WalletMetric(label: String, value: String, modifier: Modifier) {
 Card(
  modifier = modifier,
  shape = RoundedCornerShape(18.dp),
  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
 ) {
  Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
   Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
   Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
  }
 }
}
