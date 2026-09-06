package com.local.bookkeeping.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SettingsScreen(state: LedgerState, vm: LedgerViewModel, navigate: (String) -> Unit) {
 var manual by rememberSaveable(state.settings.manualRate) { mutableStateOf(state.settings.manualRate) }
 var showManual by rememberSaveable { mutableStateOf(false) }
 var deleteAll by remember { mutableStateOf(false) }
 val busy by vm.busy.collectAsStateWithLifecycle()
 val refreshing by vm.refreshing.collectAsStateWithLifecycle()
 val backup = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { vm.export(it, false) }
 val csv = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { vm.export(it, true) }
 val restore = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument(), vm::readImport)
 LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
  item { SectionTitle("Settings") }
  item { SectionTitle("Currency") }
  item { Text("Base Currency: CNY\nDisplay Currency: USD") }
  item { HorizontalDivider() }
  item { SectionTitle("Exchange Rate") }
  item { Text("Current rate: " + (state.rate?.let { "1 CNY = $$it" } ?: "Unavailable")) }
  item { Text("Rate date: " + state.rateDate.ifBlank { "Unavailable" }) }
  item { Text("Last fetched: " + (state.cache?.let { Instant.ofEpochMilli(it.lastFetchedAt).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) } ?: "Never"), style = MaterialTheme.typography.bodySmall) }
  item { OutlinedButton(onClick = vm::refreshRate, enabled = !refreshing && !state.settings.manualRateEnabled) { Text(if (refreshing) "Updating…" else "Refresh") } }
  item {
   Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    Text("Use manual rate", Modifier.padding(top = 14.dp))
    Switch(checked = state.settings.manualRateEnabled || showManual, enabled = !busy, onCheckedChange = {
     if (it) showManual = true else { showManual = false; vm.disableManual() }
    })
   }
  }
  if (state.settings.manualRateEnabled || showManual) item {
   Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    OutlinedTextField(value = manual, onValueChange = { if (it.matches(Regex("[0-9]{0,4}(\\.[0-9]{0,8})?"))) manual = it },
     label = { Text("1 CNY = … USD") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
    Button(onClick = { vm.manualRate(manual) }, enabled = !busy) { Text("Save manual rate") }
    Text("Manual rates are preserved until you turn this mode off.", style = MaterialTheme.typography.bodySmall)
   }
  }
  item { HorizontalDivider() }
  item { SectionTitle("Display") }
  item {
   Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    Text("Show CNY underneath USD", Modifier.weight(1f).padding(top = 14.dp))
    Switch(checked = state.settings.showCnySecondaryAmount, enabled = !busy, onCheckedChange = { vm.settings(state.settings.copy(showCnySecondaryAmount = it)) })
   }
  }
  item { Choice("Theme", state.settings.themeMode, listOf("System", "Light", "Dark")) { vm.settings(state.settings.copy(themeMode = it)) } }
  item {
   val options = state.activeAccounts.associateBy { it.name + " (#" + it.id + ")" }
   Choice("Default account", options.entries.find { it.value.id == state.settings.defaultAccountId }?.key ?: "First active account", options.keys.toList()) {
    vm.settings(state.settings.copy(defaultAccountId = options.getValue(it).id))
   }
  }
  item { SectionTitle("Manage Accounts") }
  item { OutlinedButton(onClick = { navigate("accountEdit/0") }) { Text("Create Account") } }
  state.accounts.forEach { account ->
   item { TextButton(onClick = { navigate("accountEdit/" + account.id) }) { Text(account.name + if (account.isArchived) " · Archived" else "") } }
  }
  item { HorizontalDivider() }
  item { SectionTitle("Data") }
  item { OutlinedButton(onClick = { backup.launch("DollarLedger_Backup_" + LocalDate.now() + ".json") }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text("Export Backup") } }
  item { OutlinedButton(onClick = { restore.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text("Import Backup") } }
  item { OutlinedButton(onClick = { csv.launch("DollarLedger_Transactions_" + LocalDate.now() + ".csv") }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text("Export CSV") } }
  item { TextButton(onClick = { deleteAll = true }, enabled = !busy) { Text("Delete All Data", color = MaterialTheme.colorScheme.error) } }
  item { HorizontalDivider() }
  item { SectionTitle("About") }
  item { Text("Dollar Ledger\nVersion 1.0.0\nLocal personal finance tracker") }
  item { Text("Accounts are local records. No bank is connected. Personal data stays on this device unless you export it. Exchange rates provided by Frankfurter v2.", style = MaterialTheme.typography.bodySmall) }
 }
 if (deleteAll) ConfirmDialog("Delete all ledger data?", "All transactions and accounts will be deleted. Checking, Savings and Cash will be recreated with zero balances. Export a backup first if needed.", { deleteAll = false }, { deleteAll = false; vm.reset() })
}
