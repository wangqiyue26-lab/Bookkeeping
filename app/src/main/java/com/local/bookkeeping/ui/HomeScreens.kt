package com.local.bookkeeping.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.local.bookkeeping.data.database.*
import com.local.bookkeeping.domain.Money
import java.time.YearMonth

@Composable
fun HomeScreen(state: LedgerState, vm: LedgerViewModel, navigate: (String) -> Unit) {
 LazyColumn(
  contentPadding = PaddingValues(bottom = 28.dp),
  verticalArrangement = Arrangement.spacedBy(20.dp)
 ) {
  item { BankingHeader() }

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
     colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
     modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
    ) {
     Text("Preparing your accounts…", Modifier.padding(24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
   } else {
    LazyRow(
     horizontalArrangement = Arrangement.spacedBy(14.dp),
     contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
     itemsIndexed(state.activeAccounts, key = { _, account -> "bank-card-${account.id}" }) { index, account ->
      BankAccountCard(account, state.balance(account.id), state, index) { navigate("account/${account.id}") }
     }
    }
   }
  }

  item {
   QuickActions(
    onExpense = { navigate("add/Expense") },
    onIncome = { navigate("add/Income") },
    onTransactions = { navigate("transactions") },
    onStatistics = { navigate("statistics") },
    modifier = Modifier.padding(horizontal = 20.dp)
   )
  }

  item {
   BalanceAndRateCard(
    state = state,
    vm = vm,
    onSettings = { navigate("settings") },
    modifier = Modifier.padding(horizontal = 20.dp)
   )
  }

  item {
   RecentActivityCard(
    state = state,
    onSeeAll = { navigate("transactions") },
    onTransaction = { navigate("transaction/$it") },
    modifier = Modifier.padding(horizontal = 20.dp)
   )
  }
 }
}

@Composable
private fun BankingHeader() {
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
  Box {
   Surface(
    modifier = Modifier.size(48.dp),
    shape = CircleShape,
    color = Color.White.copy(alpha = 0.14f)
   ) {
    Box(contentAlignment = Alignment.Center) {
     Text("$", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
    }
   }
   Box(
    Modifier
     .size(10.dp)
     .align(Alignment.TopEnd)
     .background(Color(0xFFEC2C4B), CircleShape)
   )
  }
 }
}

private data class BankVisual(
 val colors: List<Color>,
 val accent: Color,
 val badge: String,
 val network: String
)

private fun bankVisual(account: Account, index: Int): BankVisual {
 val name = account.name.lowercase()
 return when {
  "visa" in name || "全币种" in name -> BankVisual(
   listOf(Color(0xFF111827), Color(0xFF273750), Color(0xFF45556F)),
   Color(0xFFD9E0EA), "BOC", "VISA PLATINUM"
  )
  "工商" in name || "icbc" in name -> BankVisual(
   listOf(Color(0xFFC91D32), Color(0xFFA70E24), Color(0xFF7F071A)),
   Color(0xFFFFD5DB), "工", "ICBC"
  )
  "北京银行" in name || "bank of beijing" in name -> BankVisual(
   listOf(Color(0xFF174D98), Color(0xFF103A7B), Color(0xFF09285A)),
   Color(0xFFFF6977), "京", "BANK OF BEIJING"
  )
  "中国银行" in name || "bank of china" in name -> BankVisual(
   listOf(Color(0xFFB52335), Color(0xFF94162A), Color(0xFF73101F)),
   Color(0xFFFFD5DB), "中", "BANK OF CHINA"
  )
  else -> listOf(
   BankVisual(listOf(Color(0xFF153F84), Color(0xFF0A2856)), Color(0xFFBFD4FF), "DL", "DOLLAR LEDGER"),
   BankVisual(listOf(Color(0xFF8D2031), Color(0xFF551421)), Color(0xFFFFD2DA), "DL", "DOLLAR LEDGER"),
   BankVisual(listOf(Color(0xFF30506F), Color(0xFF182B42)), Color(0xFFC9DBEC), "DL", "DOLLAR LEDGER"),
   BankVisual(listOf(Color(0xFF3A315F), Color(0xFF211C3B)), Color(0xFFDCD4FF), "DL", "DOLLAR LEDGER")
  )[index % 4]
 }
}

@Composable
private fun BankAccountCard(account: Account, balanceCny: Long, state: LedgerState, index: Int, onClick: () -> Unit) {
 val visual = bankVisual(account, index)
 val isCreditCard = account.accountType == "Credit Card"
 Card(
  onClick = onClick,
  modifier = Modifier.width(316.dp).height(194.dp),
  shape = RoundedCornerShape(24.dp),
  colors = CardDefaults.cardColors(containerColor = Color.Transparent),
  elevation = CardDefaults.cardElevation(defaultElevation = 8.dp, pressedElevation = 3.dp)
 ) {
  Box(
   Modifier
    .fillMaxSize()
    .background(Brush.linearGradient(visual.colors))
    .padding(20.dp)
  ) {
   Box(
    Modifier
     .size(150.dp)
     .offset(x = 212.dp, y = (-55).dp)
     .background(Color.White.copy(alpha = 0.06f), CircleShape)
   )
   Box(
    Modifier
     .size(100.dp)
     .offset(x = 245.dp, y = 100.dp)
     .background(visual.accent.copy(alpha = 0.08f), CircleShape)
   )

   Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
     Column(Modifier.weight(1f)) {
      Text(
       account.name,
       color = Color.White,
       style = MaterialTheme.typography.titleMedium,
       fontWeight = FontWeight.Bold,
       maxLines = 1,
       overflow = TextOverflow.Ellipsis
      )
      Text(visual.network, color = Color.White.copy(alpha = 0.72f), style = MaterialTheme.typography.labelSmall)
     }
     Surface(shape = RoundedCornerShape(10.dp), color = Color.White.copy(alpha = 0.14f)) {
      Text(
       visual.badge,
       Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
       color = Color.White,
       fontWeight = FontWeight.Bold,
       style = MaterialTheme.typography.labelLarge
      )
     }
    }

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
     Text(
      if (isCreditCard) "OUTSTANDING BALANCE" else "CURRENT BALANCE",
      color = Color.White.copy(alpha = 0.72f),
      style = MaterialTheme.typography.labelSmall
     )
     Text(
      if (isCreditCard) Money.display(account.creditCardDebtUsdCents) else state.usd(balanceCny),
      color = Color.White,
      style = MaterialTheme.typography.headlineMedium,
      fontWeight = FontWeight.SemiBold
     )
     if (isCreditCard) {
      Text("USD debt · deducted from Total Balance", color = Color.White.copy(alpha = 0.78f), style = MaterialTheme.typography.bodySmall)
     } else if (state.settings.showCnySecondaryAmount) {
      Text("≈ ${Money.display(balanceCny, false)}", color = Color.White.copy(alpha = 0.78f), style = MaterialTheme.typography.bodySmall)
     }
    }

    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween) {
     Text(
      "••••  ••••  ••••  ${account.id.toString().padStart(4, '0').takeLast(4)}",
      color = Color.White.copy(alpha = 0.92f),
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.Medium
     )
     Text(
      if (isCreditCard) "CREDIT CARD" else account.accountType.uppercase(),
      color = Color.White,
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold
     )
    }
   }
  }
 }
}

@Composable
private fun QuickActions(
 onExpense: () -> Unit,
 onIncome: () -> Unit,
 onTransactions: () -> Unit,
 onStatistics: () -> Unit,
 modifier: Modifier = Modifier
) {
 Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
  QuickAction("−", "Expense", onExpense, Modifier.weight(1f))
  QuickAction("+", "Income", onIncome, Modifier.weight(1f))
  QuickAction("≡", "Activity", onTransactions, Modifier.weight(1f))
  QuickAction("↗", "Stats", onStatistics, Modifier.weight(1f))
 }
}

@Composable
private fun QuickAction(symbol: String, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
 Surface(
  onClick = onClick,
  modifier = modifier.height(92.dp),
  shape = RoundedCornerShape(18.dp),
  color = MaterialTheme.colorScheme.surface,
  shadowElevation = 2.dp
 ) {
  Column(
   Modifier.fillMaxSize().padding(vertical = 12.dp),
   horizontalAlignment = Alignment.CenterHorizontally,
   verticalArrangement = Arrangement.SpaceBetween
  ) {
   Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(40.dp)) {
    Box(contentAlignment = Alignment.Center) {
     Text(symbol, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
   }
   Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
  }
 }
}

@Composable
private fun BalanceAndRateCard(state: LedgerState, vm: LedgerViewModel, onSettings: () -> Unit, modifier: Modifier = Modifier) {
 Card(
  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
  modifier = modifier.fillMaxWidth(),
  shape = RoundedCornerShape(20.dp),
  elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
 ) {
  Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
   Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
    Column(Modifier.weight(1f)) {
     Text("TOTAL BALANCE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
     Text(state.netUsdDisplay(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
     if (state.settings.showCnySecondaryAmount) {
      Text("≈ ${state.netCnyDisplay()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
     }
     if (state.totalCreditCardDebtUsdCents > 0L) {
      Text(
       "Credit-card debt −${Money.display(state.totalCreditCardDebtUsdCents)}",
       style = MaterialTheme.typography.labelSmall,
       color = ExpenseRed,
       modifier = Modifier.padding(top = 4.dp)
      )
     }
    }
    Column(horizontalAlignment = Alignment.End) {
     Text("CNY → USD", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
     Text(state.rate ?: "Unavailable", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
     Text(state.rateDate.ifBlank { "No saved rate" }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
   }
   HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
   Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
    Text(
     if (state.settings.manualRateEnabled) "Manual exchange rate" else "Saved exchange rate",
     color = MaterialTheme.colorScheme.onSurfaceVariant,
     style = MaterialTheme.typography.bodySmall
    )
    if (state.rate == null) {
     TextButton(onClick = onSettings) { Text("Set rate") }
    } else {
     TextButton(onClick = vm::refreshRate, enabled = !state.settings.manualRateEnabled) { Text("Refresh") }
    }
   }
  }
 }
}

@Composable
private fun RecentActivityCard(
 state: LedgerState,
 onSeeAll: () -> Unit,
 onTransaction: (Long) -> Unit,
 modifier: Modifier = Modifier
) {
 Card(
  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
  modifier = modifier.fillMaxWidth(),
  shape = RoundedCornerShape(20.dp),
  elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
 ) {
  Column {
   Row(
    Modifier.fillMaxWidth().padding(start = 18.dp, end = 10.dp, top = 16.dp, bottom = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
   ) {
    Text("Transactions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    TextButton(onClick = onSeeAll) { Text("See all") }
   }
   if (state.transactions.isEmpty()) {
    Text(
     "No transactions yet. Add your first income or expense.",
     modifier = Modifier.padding(horizontal = 18.dp, vertical = 22.dp),
     color = MaterialTheme.colorScheme.onSurfaceVariant
    )
   } else {
    state.transactions.take(5).forEachIndexed { index, entry ->
     if (index > 0) HorizontalDivider(Modifier.padding(start = 78.dp), color = MaterialTheme.colorScheme.outlineVariant)
     HomeTransactionRow(entry, state) { onTransaction(entry.id) }
    }
   }
  }
 }
}

@Composable
private fun HomeTransactionRow(entry: LedgerTransaction, state: LedgerState, onClick: () -> Unit) {
 val income = entry.type == "Income"
 val sign = if (income) "+" else "−"
 val title = entry.note.trim().ifBlank { entry.category }
 Row(
  Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 18.dp, vertical = 13.dp),
  verticalAlignment = Alignment.CenterVertically
 ) {
  Surface(
   modifier = Modifier.size(46.dp),
   shape = RoundedCornerShape(13.dp),
   color = MaterialTheme.colorScheme.primaryContainer
  ) {
   Box(contentAlignment = Alignment.Center) {
    Text(entry.category.take(1).uppercase(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
   }
  }
  Spacer(Modifier.width(14.dp))
  Column(Modifier.weight(1f)) {
   Text(title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
   Text(
    "${entry.category} · ${state.accounts.find { it.id == entry.accountId }?.name ?: entry.transactionDate}",
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis
   )
  }
  Spacer(Modifier.width(10.dp))
  Column(horizontalAlignment = Alignment.End) {
   Text(
    sign + Money.display(entry.amountUsd),
    fontWeight = FontWeight.SemiBold,
    color = if (income) IncomeGreen else MaterialTheme.colorScheme.onSurface
   )
   if (state.settings.showCnySecondaryAmount) {
    Text(
     "≈ $sign${Money.display(entry.amountCny, false)}",
     style = MaterialTheme.typography.labelSmall,
     color = MaterialTheme.colorScheme.onSurfaceVariant
    )
   }
  }
 }
}

@Composable
fun TransactionsScreen(state: LedgerState, onTransaction: (Long) -> Unit) {
 var monthText by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
 var type by rememberSaveable { mutableStateOf("All types") }
 var account by rememberSaveable { mutableStateOf("All accounts") }
 var category by rememberSaveable { mutableStateOf("All categories") }
 val accountOptions = state.accounts.associateBy { it.name + " (#" + it.id + ")" }
 val entries = state.transactions.filter {
  it.transactionDate.startsWith(monthText) && (type == "All types" || it.type == type) &&
   (account == "All accounts" || it.accountId == accountOptions[account]?.id) &&
   (category == "All categories" || it.category == category)
 }
 LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
  item { SectionTitle("Transactions") }
  item { MonthPicker(YearMonth.parse(monthText)) { monthText = it.toString() } }
  item { Choice("Type", type, listOf("All types", "Income", "Expense")) { type = it } }
  item { Choice("Account", account, listOf("All accounts") + accountOptions.keys) { account = it } }
  item { Choice("Category", category, listOf("All categories") + (com.local.bookkeeping.domain.Categories.expense + com.local.bookkeeping.domain.Categories.income).distinct()) { category = it } }
  if (entries.isEmpty()) item { Text("No transactions match these filters.") }
  entries.groupBy { it.transactionDate }.forEach { (date, list) ->
   item(key = "date$date") { Text(date, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 12.dp)) }
   items(list, key = { it.id }) { t -> TransactionRow(t, state) { onTransaction(t.id) } }
  }
 }
}

@Composable
fun AccountScreen(id: Long, state: LedgerState, navigate: (String) -> Unit) {
 val account = state.accounts.find { it.id == id }
 var monthText by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
 if (account == null) { Text("Account unavailable", Modifier.padding(20.dp)); return }
 val entries = state.transactions.filter { it.accountId == id && it.transactionDate.startsWith(monthText) }
 val isCreditCard = account.accountType == "Credit Card"
 LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
  item { SectionTitle(account.name) }
  item { Text(account.accountType + if (account.isArchived) " · Archived" else " · Local account") }
  if (isCreditCard) {
   item {
    Card(
     colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
     modifier = Modifier.fillMaxWidth(),
     shape = RoundedCornerShape(20.dp)
    ) {
     Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
      Text("OUTSTANDING BALANCE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Text(Money.display(account.creditCardDebtUsdCents), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.SemiBold)
      Text("USD debt · deducted from Total Balance", color = ExpenseRed, style = MaterialTheme.typography.bodySmall)
     }
    }
   }
  } else {
   item { AmountBlock(state.balance(id), state, true) }
  }
  item { Text("Current Exchange Rate: " + (state.rate?.let { "1 CNY = $$it" } ?: "Unavailable")) }
  item { OutlinedButton(onClick = { navigate("accountEdit/$id") }) { Text(if (isCreditCard) "Edit USD debt" else "Manage account") } }
  item { SectionTitle("Transactions") }
  item { MonthPicker(YearMonth.parse(monthText)) { monthText = it.toString() } }
  if (entries.isEmpty()) item { Text("No transactions this month.") }
  items(entries, key = { it.id }) { t -> TransactionRow(t, state) { navigate("transaction/" + t.id) } }
 }
}
