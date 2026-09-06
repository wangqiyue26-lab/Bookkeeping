package com.local.bookkeeping.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.local.bookkeeping.data.database.LedgerTransaction
import com.local.bookkeeping.domain.Money
import java.time.YearMonth

@Composable
fun SectionTitle(text: String) { Text(text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) }

@Composable
fun Choice(label: String, value: String, options: List<String>, onSelect: (String) -> Unit) {
 var open by remember { mutableStateOf(false) }
 Box {
  OutlinedButton(onClick = { open = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
   Text("$label: $value", modifier = Modifier.fillMaxWidth())
  }
  DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
   options.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { open = false; onSelect(option) }) }
  }
 }
}

@Composable
fun MonthPicker(month: YearMonth, onChange: (YearMonth) -> Unit) {
 Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
  TextButton(onClick = { onChange(month.minusMonths(1)) }) { Text("Previous") }
  Text(month.toString(), modifier = Modifier.padding(top = 14.dp), style = MaterialTheme.typography.titleMedium)
  TextButton(onClick = { onChange(month.plusMonths(1)) }) { Text("Next") }
 }
}

@Composable
fun AmountBlock(cny: Long, state: LedgerState, large: Boolean = false) {
 Text(state.usd(cny), style = if (large) MaterialTheme.typography.displaySmall else MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
 if (state.settings.showCnySecondaryAmount) Text("≈ " + Money.display(cny, false), style = MaterialTheme.typography.bodyMedium)
}

@Composable
fun TransactionRow(entry: LedgerTransaction, state: LedgerState, onClick: () -> Unit) {
 val income = entry.type == "Income"
 val sign = if (income) "+" else "−"
 Card(onClick = onClick, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
  Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
   Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    Text(entry.category, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
    Text(sign + Money.display(entry.amountUsd), fontWeight = FontWeight.SemiBold,
     color = if (income) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
   }
   Text(entry.transactionDate + " · " + (state.accounts.find { it.id == entry.accountId }?.name ?: ""), style = MaterialTheme.typography.bodySmall)
   if (state.settings.showCnySecondaryAmount) Text("≈ " + sign + Money.display(entry.amountCny, false), style = MaterialTheme.typography.bodySmall)
  }
 }
}

@Composable
fun ConfirmDialog(title: String, message: String, onDismiss: () -> Unit, onConfirm: () -> Unit, confirm: String = "Delete") {
 AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = { Text(message) },
  confirmButton = { TextButton(onClick = onConfirm) { Text(confirm) } },
  dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}
