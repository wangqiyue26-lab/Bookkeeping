package com.local.bookkeeping.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.local.bookkeeping.data.database.*
import java.time.YearMonth

@Composable
fun HomeScreen(state: LedgerState, vm: LedgerViewModel, navigate: (String) -> Unit) {
 LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
  item {
   Text("Dollar Ledger", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
   Text("YOUR MONEY, CLEARLY.", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 6.dp))
  }
  item {
   Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), modifier = Modifier.fillMaxWidth()) {
    Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
     Text("TOTAL BALANCE", style = MaterialTheme.typography.labelLarge)
     AmountBlock(state.totalCny, state, true)
     if (state.rate == null) {
      Text("Exchange rate unavailable")
      TextButton(onClick = { navigate("settings") }) { Text("Set Rate") }
     } else {
      Text("1 CNY = $" + state.rate, style = MaterialTheme.typography.bodySmall)
      Text(if (state.settings.manualRateEnabled) "Manual rate · " + state.rateDate else "Saved rate · " + state.rateDate, style = MaterialTheme.typography.bodySmall)
     }
     TextButton(onClick = vm::refreshRate, enabled = !state.settings.manualRateEnabled) { Text("Refresh rate") }
    }
   }
  }
  item {
   Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
    Button(onClick = { navigate("add/Expense") }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text("Expense") }
    OutlinedButton(onClick = { navigate("add/Income") }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text("Income") }
   }
  }
  item {
   Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    SectionTitle("Accounts")
    TextButton(onClick = { navigate("accountEdit/0") }) { Text("Add account") }
   }
  }
  items(state.activeAccounts, key = { "a" + it.id }) { account ->
   Card(onClick = { navigate("account/" + account.id) }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
     Text(account.name, style = MaterialTheme.typography.titleMedium)
     Text("Local ID •••• " + account.id.toString().padStart(4, '0').takeLast(4), style = MaterialTheme.typography.labelSmall)
     AmountBlock(state.balance(account.id), state)
     Text("Available balance", style = MaterialTheme.typography.bodySmall)
    }
   }
  }
  item { SectionTitle("Recent Activity") }
  if (state.transactions.isEmpty()) item { Text("No transactions yet. Add your first income or expense.") }
  items(state.transactions.take(7), key = { "t" + it.id }) { t -> TransactionRow(t, state) { navigate("transaction/" + t.id) } }
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
 LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
  item { SectionTitle(account.name) }
  item { Text(account.accountType + if (account.isArchived) " · Archived" else " · Local account") }
  item { AmountBlock(state.balance(id), state, true) }
  item { Text("Current Exchange Rate: " + (state.rate?.let { "1 CNY = $$it" } ?: "Unavailable")) }
  item { OutlinedButton(onClick = { navigate("accountEdit/$id") }) { Text("Manage account") } }
  item { SectionTitle("Transactions") }
  item { MonthPicker(YearMonth.parse(monthText)) { monthText = it.toString() } }
  if (entries.isEmpty()) item { Text("No transactions this month.") }
  items(entries, key = { it.id }) { t -> TransactionRow(t, state) { navigate("transaction/" + t.id) } }
 }
}
