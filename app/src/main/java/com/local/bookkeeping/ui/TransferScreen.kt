package com.local.bookkeeping.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.local.bookkeeping.domain.Money
import java.time.LocalDate

@Composable
fun TransferScreen(
 state: LedgerState,
 vm: LedgerViewModel,
 onSettings: () -> Unit,
 done: () -> Unit
) {
 val accounts = state.assetAccounts
 val options = accounts.associateBy { it.name + " (#" + it.id + ")" }
 var fromId by rememberSaveable(accounts.size) { mutableLongStateOf(accounts.getOrNull(0)?.id ?: 0L) }
 var toId by rememberSaveable(accounts.size) { mutableLongStateOf(accounts.getOrNull(1)?.id ?: 0L) }
 var amount by rememberSaveable { mutableStateOf("") }
 var note by rememberSaveable { mutableStateOf("") }
 var error by remember { mutableStateOf<String?>(null) }
 val busy by vm.busy.collectAsStateWithLifecycle()
 val rate = state.rate
 val cents = runCatching { Money.cents(amount) }.getOrNull()

 Column(
  Modifier
   .fillMaxSize()
   .verticalScroll(rememberScrollState())
   .padding(20.dp),
  verticalArrangement = Arrangement.spacedBy(16.dp)
 ) {
  Text("Transfer", style = MaterialTheme.typography.headlineMedium, color = Navy)
  Text(
   "Move money between your asset accounts. The transfer creates matching outgoing and incoming entries, so your overall balance does not change.",
   color = MaterialTheme.colorScheme.onSurfaceVariant
  )

  if (accounts.size < 2) {
   Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
    Text("Create at least two non-credit accounts before making a transfer.", Modifier.padding(18.dp))
   }
   return@Column
  }

  Choice(
   "From account",
   options.entries.find { it.value.id == fromId }?.key ?: options.keys.first(),
   options.keys.toList()
  ) { fromId = options.getValue(it).id }

  Choice(
   "To account",
   options.entries.find { it.value.id == toId }?.key ?: options.keys.drop(1).firstOrNull() ?: options.keys.first(),
   options.keys.toList()
  ) { toId = options.getValue(it).id }

  OutlinedTextField(
   value = amount,
   onValueChange = { if (it.matches(Regex("[0-9]{0,12}(\\.[0-9]{0,2})?"))) amount = it },
   prefix = { Text("¥") },
   label = { Text("Transfer amount CNY") },
   placeholder = { Text("0.00") },
   textStyle = MaterialTheme.typography.headlineLarge,
   singleLine = true,
   keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
   modifier = Modifier.fillMaxWidth()
  )

  Text(
   if (cents != null && rate != null) "≈ ${Money.display(Money.usdCents(cents, rate))} USD" else "≈ — USD",
   style = MaterialTheme.typography.titleLarge,
   color = Navy
  )

  if (rate == null) {
   Text("Set an exchange rate before saving a transfer.")
   OutlinedButton(onClick = onSettings) { Text("Set exchange rate") }
  }

  OutlinedTextField(
   value = note,
   onValueChange = { if (it.length <= 2000) note = it },
   label = { Text("Note (optional)") },
   modifier = Modifier.fillMaxWidth(),
   minLines = 2
  )

  error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

  Button(
   onClick = {
    try {
     val value = Money.cents(amount)
     require(fromId != toId) { "Choose two different accounts." }
     require(rate != null) { "Set an exchange rate first." }
     vm.saveTransfer(
      fromAccountId = fromId,
      toAccountId = toId,
      amountCny = value,
      exchangeRate = rate,
      exchangeRateDate = state.rateDate,
      transactionDate = LocalDate.now().toString(),
      note = note,
      done = done
     )
    } catch (e: IllegalArgumentException) {
     error = e.message
    }
   },
   enabled = !busy && cents != null && rate != null && fromId != 0L && toId != 0L && fromId != toId,
   modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
  ) {
   Text("Transfer now")
  }
 }
}
