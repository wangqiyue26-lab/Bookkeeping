package com.local.bookkeeping.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.local.bookkeeping.data.database.*
import com.local.bookkeeping.domain.*
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneOffset

@Composable
fun AccountEditor(id: Long, state: LedgerState, vm: LedgerViewModel, done: () -> Unit) {
 val existing = state.accounts.find { it.id == id }
 var name by rememberSaveable(id) { mutableStateOf(existing?.name ?: "") }
 var type by rememberSaveable(id) { mutableStateOf(existing?.accountType ?: "Checking") }
 var opening by rememberSaveable(id) { mutableStateOf(existing?.openingBalanceCny?.let(Money::input) ?: "0.00") }
 var debtUsd by rememberSaveable(id) { mutableStateOf(existing?.creditCardDebtUsdCents?.let(Money::input) ?: "0.00") }
 var delete by remember { mutableStateOf(false) }
 var error by remember { mutableStateOf<String?>(null) }
 val busy by vm.busy.collectAsStateWithLifecycle()
 val isCreditCard = type == "Credit Card"
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
  SectionTitle(if (id == 0L) "Create Account" else "Edit Account")
  OutlinedTextField(value = name, onValueChange = { if (it.length <= 60) name = it }, label = { Text("Account name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
  Choice("Account type", type, Categories.accountTypes) { type = it }
  if (isCreditCard) {
   OutlinedTextField(
    value = debtUsd,
    onValueChange = { if (it.matches(Regex("[0-9]{0,12}(\\.[0-9]{0,2})?"))) debtUsd = it },
    prefix = { Text("$") },
    placeholder = { Text("0.00") },
    label = { Text("Outstanding debt USD") },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
    modifier = Modifier.fillMaxWidth(),
    singleLine = true
   )
   Text("This USD debt is deducted directly from the app's Total Balance.")
  } else {
   OutlinedTextField(value = opening, onValueChange = { if (it.matches(Regex("[0-9]{0,12}(\\.[0-9]{0,2})?"))) opening = it },
    label = { Text("Opening balance CNY") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(), singleLine = true)
   Text("Opening balance is included before all income and expenses.")
  }
  error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
  Button(onClick = {
   try {
    require(name.isNotBlank()) { "Enter an account name." }
    val openingValue = Money.cents(opening, true)
    val debtValue = if (isCreditCard) Money.cents(debtUsd, true) else 0L
    vm.saveAccount(
     (existing ?: Account(name = name.trim(), accountType = type, sortOrder = state.accounts.size)).copy(
      name = name.trim(),
      accountType = type,
      openingBalanceCny = openingValue,
      creditCardDebtUsdCents = debtValue
     ),
     done
    )
   } catch (e: IllegalArgumentException) { error = e.message }
  }, enabled = !busy, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Save Account") }
  existing?.let {
   OutlinedButton(onClick = { vm.saveAccount(it.copy(isArchived = !it.isArchived), done) }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
    Text(if (it.isArchived) "Restore account" else "Archive account")
   }
   Text("Archived accounts are excluded from total balance. Their transaction history remains.")
   TextButton(onClick = { delete = true }, enabled = !busy) { Text("Delete Empty Account", color = MaterialTheme.colorScheme.error) }
  }
 }
 if (delete) ConfirmDialog("Delete account?", "Only accounts with no transactions, zero opening balance, and zero credit-card debt can be deleted.", { delete = false }, { delete = false; vm.deleteAccount(id, done) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEditor(id: Long, initialType: String, state: LedgerState, vm: LedgerViewModel, settings: () -> Unit, done: () -> Unit) {
 val existing = state.transactions.find { it.id == id }
 var type by rememberSaveable(id) { mutableStateOf(existing?.type ?: initialType) }
 var amount by rememberSaveable(id) { mutableStateOf(existing?.amountCny?.let(Money::input) ?: "") }
 var accountId by rememberSaveable(id) { mutableLongStateOf(existing?.accountId ?: state.activeAccounts.find { it.id == state.settings.defaultAccountId }?.id ?: state.activeAccounts.firstOrNull()?.id ?: 0) }
 var category by rememberSaveable(id) { mutableStateOf(existing?.category ?: Categories.forType(type).first()) }
 var date by rememberSaveable(id) { mutableStateOf(existing?.transactionDate ?: LocalDate.now().toString()) }
 var note by rememberSaveable(id) { mutableStateOf(existing?.note ?: "") }
 var latest by rememberSaveable(id) { mutableStateOf(false) }
 var showDate by remember { mutableStateOf(false) }
 var error by remember { mutableStateOf<String?>(null) }
 val busy by vm.busy.collectAsStateWithLifecycle()
 val rate = if (existing != null && !latest) existing.exchangeRate else state.rate
 val rateDate = if (existing != null && !latest) existing.exchangeRateDate else state.rateDate
 val cents = runCatching { Money.cents(amount) }.getOrNull()
 val accounts = state.activeAccounts.associateBy { it.name + " (#" + it.id + ")" }
 val chosen = accounts.entries.find { it.value.id == accountId }?.key ?: "Select account"
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
  SectionTitle(if (id == 0L) "Add Transaction" else "Edit Transaction")
  SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
   listOf("Expense", "Income").forEachIndexed { index, option ->
    SegmentedButton(selected = type == option, onClick = { type = option; category = Categories.forType(option).first() }, shape = SegmentedButtonDefaults.itemShape(index, 2)) { Text(option) }
   }
  }
  OutlinedTextField(value = amount, onValueChange = { if (it.matches(Regex("[0-9]{0,12}(\\.[0-9]{0,2})?"))) amount = it },
   prefix = { Text("¥") }, placeholder = { Text("0.00") }, label = { Text("Amount CNY") },
   textStyle = MaterialTheme.typography.headlineLarge, singleLine = true,
   keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
  Text(if (cents != null && rate != null) "≈ " + Money.display(Money.usdCents(cents, rate)) + " USD" else "≈ — USD", style = MaterialTheme.typography.headlineSmall)
  if (rate == null) {
   Text("Set an exchange rate before saving your first transaction.")
   OutlinedButton(onClick = settings) { Text("Set Rate") }
  } else {
   Text("1 CNY = $" + rate + " · " + rateDate)
   if (existing != null) Text(if (latest) "Latest rate selected" else "Using this transaction's historical rate")
  }
  if (existing != null) OutlinedButton(onClick = { latest = !latest }, enabled = state.rate != null) { Text(if (latest) "Keep original rate" else "Use latest rate") }
  Choice("Account", chosen, accounts.keys.toList()) { accountId = accounts.getValue(it).id }
  if (accounts.isEmpty()) Text("Create or restore an account in Settings before saving.")
  Choice("Category", category, Categories.forType(type)) { category = it }
  OutlinedButton(onClick = { showDate = true }, modifier = Modifier.fillMaxWidth()) { Text("Date: $date") }
  OutlinedTextField(value = note, onValueChange = { if (it.length <= 2000) note = it }, label = { Text("Note") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
  error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
  Button(onClick = {
   try {
    val value = Money.cents(amount)
    require(rate != null) { "Set an exchange rate first." }
    require(state.activeAccounts.any { it.id == accountId }) { "Choose an active account." }
    val entry = LedgerTransaction(id = id, accountId = accountId, type = type, amountCny = value,
     amountUsd = Money.usdCents(value, rate), exchangeRate = rate, exchangeRateDate = rateDate,
     category = category, note = note.trim(), transactionDate = date, createdAt = existing?.createdAt ?: System.currentTimeMillis())
    vm.saveTransaction(entry, done)
   } catch (e: IllegalArgumentException) { error = e.message }
  }, enabled = !busy && cents != null && rate != null && accountId != 0L, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Save Transaction") }
 }
 if (showDate) {
  val picker = rememberDatePickerState(initialSelectedDateMillis = LocalDate.parse(date).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
  DatePickerDialog(onDismissRequest = { showDate = false }, confirmButton = {
   TextButton(onClick = { picker.selectedDateMillis?.let { date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString() }; showDate = false }) { Text("Set date") }
  }, dismissButton = { TextButton(onClick = { showDate = false }) { Text("Cancel") } }) { DatePicker(picker) }
 }
}

@Composable
fun TransactionDetail(id: Long, state: LedgerState, vm: LedgerViewModel, navigate: (String) -> Unit, done: () -> Unit) {
 val entry = state.transactions.find { it.id == id }
 var delete by remember { mutableStateOf(false) }
 val busy by vm.busy.collectAsStateWithLifecycle()
 if (entry == null) { Text("Transaction unavailable", Modifier.padding(20.dp)); return }
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
  SectionTitle(entry.category)
  Text((if (entry.type == "Income") "+" else "−") + Money.display(entry.amountUsd), style = MaterialTheme.typography.displaySmall)
  listOf("Type" to entry.type, "CNY Amount" to Money.display(entry.amountCny, false), "USD Amount" to Money.display(entry.amountUsd),
   "Exchange Rate" to ("1 CNY = $" + entry.exchangeRate), "Exchange Rate Date" to entry.exchangeRateDate,
   "Account" to (state.accounts.find { it.id == entry.accountId }?.name ?: ""), "Category" to entry.category,
   "Transaction Date" to entry.transactionDate, "Note" to entry.note.ifBlank { "—" }).forEach { (label, value) ->
   Column { Text(label, style = MaterialTheme.typography.labelMedium); Text(value, style = MaterialTheme.typography.bodyLarge) }
  }
  Button(onClick = { navigate("edit/$id") }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text("Edit") }
  OutlinedButton(onClick = { delete = true }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text("Delete") }
 }
 if (delete) ConfirmDialog("Delete transaction?", "This permanently removes the transaction and updates your CNY balance.", { delete = false }, { delete = false; vm.deleteTransaction(id, done) })
}
