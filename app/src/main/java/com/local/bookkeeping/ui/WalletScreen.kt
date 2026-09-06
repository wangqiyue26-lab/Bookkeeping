package com.local.bookkeeping.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.local.bookkeeping.domain.Money

@Composable
fun WalletScreen(state: LedgerState, vm: LedgerViewModel, navigate: (String) -> Unit) {
 LazyColumn(
  contentPadding = PaddingValues(bottom = 28.dp),
  verticalArrangement = Arrangement.spacedBy(18.dp)
 ) {
  item {
   Column(
    Modifier.fillMaxWidth().background(Navy).padding(horizontal = 22.dp, vertical = 28.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
   ) {
    Text("Wallet", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
    Spacer(Modifier.height(6.dp))
    Text("TOTAL BALANCE", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.72f))
    Text(state.netUsdDisplay(), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = Color.White)
    if (state.settings.showCnySecondaryAmount) {
     Text("≈ ${state.netCnyDisplay()}", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.76f))
    }
    if (state.totalCreditCardDebtUsdCents > 0L) {
     Surface(shape = RoundedCornerShape(14.dp), color = Color.White.copy(alpha = 0.12f)) {
      Text(
       "Credit-card debt  −${Money.display(state.totalCreditCardDebtUsdCents)}",
       modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
       style = MaterialTheme.typography.labelMedium,
       color = Color.White
      )
     }
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

  item {
   Card(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
   ) {
    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
     Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
      Column {
       Text("Exchange rate", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
       Text(
        state.rate?.let { "1 CNY = $$it" } ?: "Unavailable",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
       )
       Text(state.rateDate.ifBlank { "No saved rate" }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      TextButton(onClick = vm::refreshRate, enabled = !state.settings.manualRateEnabled) { Text("Refresh") }
     }
     if (state.rate == null) {
      OutlinedButton(onClick = { navigate("settings") }, modifier = Modifier.fillMaxWidth()) { Text("Set exchange rate") }
     }
    }
   }
  }

  item {
   Row(
    Modifier.fillMaxWidth().padding(horizontal = 20.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
   ) {
    Text("Accounts", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    TextButton(onClick = { navigate("accountEdit/0") }) { Text("+ Add") }
   }
  }

  state.activeAccounts.forEach { account ->
   item(key = "wallet-${account.id}") {
    val credit = account.accountType == "Credit Card"
    Card(
     onClick = { navigate("account/${account.id}") },
     modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
     shape = RoundedCornerShape(18.dp),
     colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
     elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
     Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
      Surface(modifier = Modifier.size(44.dp), shape = CircleShape, color = if (credit) Color(0xFFF5E8EA) else SoftBlue) {
       Box(contentAlignment = Alignment.Center) {
        Text(if (credit) "V" else account.name.take(1), fontWeight = FontWeight.Bold, color = Navy)
       }
      }
      Spacer(Modifier.width(14.dp))
      Column(Modifier.weight(1f)) {
       Text(account.name, fontWeight = FontWeight.SemiBold)
       Text(if (credit) "Credit Card · USD debt" else account.accountType, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      Text(
       if (credit) "−${Money.display(account.creditCardDebtUsdCents)}" else state.usd(state.balance(account.id)),
       fontWeight = FontWeight.Bold,
       color = if (credit) ExpenseRed else MaterialTheme.colorScheme.onSurface
      )
     }
    }
   }
  }
 }
}

@Composable
private fun WalletMetric(label: String, value: String, modifier: Modifier) {
 Card(modifier = modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
  Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
   Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
   Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
  }
 }
}
