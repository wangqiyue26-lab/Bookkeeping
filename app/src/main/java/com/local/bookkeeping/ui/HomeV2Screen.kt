package com.local.bookkeeping.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.local.bookkeeping.data.database.Account
import com.local.bookkeeping.domain.Money

@Composable
fun HomeV2Screen(state: LedgerState, navigate: (String) -> Unit) {
 LazyColumn(
  contentPadding = PaddingValues(bottom = 28.dp),
  verticalArrangement = Arrangement.spacedBy(20.dp)
 ) {
  item { V2HomeHeader(onSettings = { navigate("settings") }) }
  item {
   Row(
    Modifier.fillMaxWidth().padding(horizontal = 20.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
   ) {
    Text("My Accounts", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    TextButton(onClick = { navigate("accountEdit/0") }) { Text("+ Add") }
   }
  }
  item {
   if (state.activeAccounts.isEmpty()) {
    Card(
     modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
     colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
     shape = RoundedCornerShape(20.dp)
    ) {
     Text("Preparing your accounts…", Modifier.padding(24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
   } else {
    LazyRow(
     contentPadding = PaddingValues(horizontal = 20.dp),
     horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
     itemsIndexed(state.activeAccounts, key = { _, account -> "v2-card-${account.id}" }) { index, account ->
      V2BankCard(account, state, index) { navigate("account/${account.id}") }
     }
    }
   }
  }
  item {
   Row(
    Modifier.fillMaxWidth().padding(horizontal = 20.dp),
    horizontalArrangement = Arrangement.spacedBy(10.dp)
   ) {
    V2QuickAction("−", "Expense", { navigate("add/Expense") }, Modifier.weight(1f))
    V2QuickAction("+", "Income", { navigate("add/Income") }, Modifier.weight(1f))
    V2QuickAction("⇄", "Transfer", { navigate("transfer") }, Modifier.weight(1f))
    V2QuickAction("↗", "Stats", { navigate("statistics") }, Modifier.weight(1f))
   }
  }
  item {
   Card(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(20.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
   ) {
    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
     Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
      Text("Recent Transactions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
      TextButton(onClick = { navigate("transactions") }) { Text("See all") }
     }
     if (state.transactions.isEmpty()) {
      Text("No transactions yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
     } else {
      state.transactions.take(5).forEach { entry ->
       TransactionRow(entry, state) { navigate("transaction/${entry.id}") }
      }
     }
    }
   }
  }
 }
}

@Composable
private fun V2HomeHeader(onSettings: () -> Unit) {
 Row(
  Modifier.fillMaxWidth().background(Navy).padding(horizontal = 20.dp, vertical = 24.dp),
  verticalAlignment = Alignment.CenterVertically,
  horizontalArrangement = Arrangement.SpaceBetween
 ) {
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
   Text(
    "Hello!",
    style = MaterialTheme.typography.headlineMedium,
    fontWeight = FontWeight.Bold,
    fontStyle = FontStyle.Italic,
    color = Color.White
   )
   Text("Dollar Ledger", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.82f))
  }
  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
   IconButton(onClick = onSettings, modifier = Modifier.size(42.dp)) {
    Text("⚙", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
   }
   Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.14f)) {
    Box(contentAlignment = Alignment.Center) {
     Text("$", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
    }
   }
  }
 }
}

private data class V2BankVisual(val colors: List<Color>, val badge: String, val network: String)

private fun v2BankVisual(account: Account, index: Int): V2BankVisual {
 val name = account.name.lowercase()
 return when {
  "visa" in name || "全币种" in name -> V2BankVisual(listOf(Color(0xFF111827), Color(0xFF273750), Color(0xFF45556F)), "BOC", "VISA PLATINUM")
  "工商" in name || "icbc" in name -> V2BankVisual(listOf(Color(0xFFC91D32), Color(0xFFA70E24), Color(0xFF7F071A)), "工", "ICBC")
  "北京银行" in name || "bank of beijing" in name -> V2BankVisual(listOf(Color(0xFF174D98), Color(0xFF103A7B), Color(0xFF09285A)), "京", "BANK OF BEIJING")
  "中国银行" in name || "bank of china" in name -> V2BankVisual(listOf(Color(0xFFB52335), Color(0xFF94162A), Color(0xFF73101F)), "中", "BANK OF CHINA")
  else -> listOf(
   V2BankVisual(listOf(Color(0xFF153F84), Color(0xFF0A2856)), "DL", "DOLLAR LEDGER"),
   V2BankVisual(listOf(Color(0xFF8D2031), Color(0xFF551421)), "DL", "DOLLAR LEDGER"),
   V2BankVisual(listOf(Color(0xFF30506F), Color(0xFF182B42)), "DL", "DOLLAR LEDGER"),
   V2BankVisual(listOf(Color(0xFF3A315F), Color(0xFF211C3B)), "DL", "DOLLAR LEDGER")
  )[index % 4]
 }
}

@Composable
private fun V2BankCard(account: Account, state: LedgerState, index: Int, onClick: () -> Unit) {
 val visual = v2BankVisual(account, index)
 val credit = account.accountType == "Credit Card"
 Card(
  onClick = onClick,
  modifier = Modifier.width(316.dp).height(194.dp),
  shape = RoundedCornerShape(24.dp),
  colors = CardDefaults.cardColors(containerColor = Color.Transparent),
  elevation = CardDefaults.cardElevation(defaultElevation = 8.dp, pressedElevation = 3.dp)
 ) {
  Box(Modifier.fillMaxSize().background(Brush.linearGradient(visual.colors)).padding(20.dp)) {
   Box(Modifier.size(150.dp).offset(x = 212.dp, y = (-55).dp).background(Color.White.copy(alpha = 0.06f), CircleShape))
   Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
     Column(Modifier.weight(1f)) {
      Text(account.name, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
      Text(visual.network, color = Color.White.copy(alpha = 0.72f), style = MaterialTheme.typography.labelSmall)
     }
     Surface(shape = RoundedCornerShape(10.dp), color = Color.White.copy(alpha = 0.14f)) {
      Text(visual.badge, Modifier.padding(horizontal = 10.dp, vertical = 7.dp), color = Color.White, fontWeight = FontWeight.Bold)
     }
    }
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
     Text(if (credit) "OUTSTANDING BALANCE" else "CURRENT BALANCE", color = Color.White.copy(alpha = 0.72f), style = MaterialTheme.typography.labelSmall)
     Text(
      if (credit) Money.display(account.creditCardDebtUsdCents) else state.usd(state.balance(account.id)),
      color = Color.White,
      style = MaterialTheme.typography.headlineMedium,
      fontWeight = FontWeight.SemiBold
     )
     if (credit) Text("USD debt", color = Color.White.copy(alpha = 0.78f), style = MaterialTheme.typography.bodySmall)
     else if (state.settings.showCnySecondaryAmount) Text("≈ ${Money.display(state.balance(account.id), false)}", color = Color.White.copy(alpha = 0.78f), style = MaterialTheme.typography.bodySmall)
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
     Text("••••  ••••  ••••  ${account.id.toString().padStart(4, '0').takeLast(4)}", color = Color.White.copy(alpha = 0.92f))
     Text(if (credit) "CREDIT CARD" else account.accountType.uppercase(), color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
   }
  }
 }
}

@Composable
private fun V2QuickAction(symbol: String, label: String, onClick: () -> Unit, modifier: Modifier) {
 Surface(onClick = onClick, modifier = modifier.height(92.dp), shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
  Column(Modifier.fillMaxSize().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
   Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(40.dp)) {
    Box(contentAlignment = Alignment.Center) { Text(symbol, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
   }
   Text(label, style = MaterialTheme.typography.labelMedium)
  }
 }
}
