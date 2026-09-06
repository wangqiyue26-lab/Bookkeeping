package com.local.bookkeeping.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.local.bookkeeping.domain.Categories
import java.time.YearMonth

@Composable
fun TransactionsV2Screen(state: LedgerState, onTransaction: (Long) -> Unit) {
 var monthText by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
 var type by rememberSaveable { mutableStateOf("All types") }
 var account by rememberSaveable { mutableStateOf("All accounts") }
 var category by rememberSaveable { mutableStateOf("All categories") }
 val accountOptions = state.accounts.associateBy { it.name + " (#" + it.id + ")" }
 val entries = state.transactions.filter {
  it.transactionDate.startsWith(monthText) &&
   (type == "All types" || it.type == type) &&
   (account == "All accounts" || it.accountId == accountOptions[account]?.id) &&
   (category == "All categories" || it.category == category)
 }

 LazyColumn(
  contentPadding = PaddingValues(bottom = 28.dp),
  verticalArrangement = Arrangement.spacedBy(12.dp)
 ) {
  item {
   Box(
    Modifier
     .fillMaxWidth()
     .background(Navy)
     .padding(horizontal = 22.dp, vertical = 28.dp)
   ) {
    Text(
     "Transactions",
     style = MaterialTheme.typography.headlineMedium,
     fontWeight = FontWeight.Bold,
     color = Color.White
    )
   }
  }
  item {
   Column(
    Modifier.fillMaxWidth().padding(horizontal = 20.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
   ) {
    MonthPicker(YearMonth.parse(monthText)) { monthText = it.toString() }
    Choice("Type", type, listOf("All types", "Income", "Expense")) { type = it }
    Choice("Account", account, listOf("All accounts") + accountOptions.keys) { account = it }
    Choice(
     "Category",
     category,
     listOf("All categories") + (Categories.expense + Categories.income).distinct()
    ) { category = it }
   }
  }
  if (entries.isEmpty()) {
   item {
    Text(
     "No transactions match these filters.",
     modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
     style = MaterialTheme.typography.titleMedium,
     fontWeight = FontWeight.SemiBold
    )
   }
  }
  entries.groupBy { it.transactionDate }.forEach { (date, list) ->
   item(key = "v2-date-$date") {
    Text(
     date,
     style = MaterialTheme.typography.labelLarge,
     modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp)
    )
   }
   items(list, key = { it.id }) { entry ->
    Box(Modifier.padding(horizontal = 20.dp)) {
     TransactionRow(entry, state) { onTransaction(entry.id) }
    }
   }
  }
 }
}
