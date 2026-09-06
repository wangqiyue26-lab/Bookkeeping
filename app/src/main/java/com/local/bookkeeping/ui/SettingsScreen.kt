package com.local.bookkeeping.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.Instant
import java.time.LocalDate
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

 LazyColumn(
  contentPadding = PaddingValues(bottom = 30.dp),
  verticalArrangement = Arrangement.spacedBy(16.dp)
 ) {
  item {
   Column(
    Modifier.fillMaxWidth().background(Navy).padding(horizontal = 22.dp, vertical = 26.dp),
    verticalArrangement = Arrangement.spacedBy(5.dp)
   ) {
    Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
    Text("Personalize Dollar Ledger", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.72f))
   }
  }

  item {
   SettingsCard("Currency", "$") {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
     SettingMetric("Base currency", "CNY", Modifier.weight(1f))
     SettingMetric("Display currency", "USD", Modifier.weight(1f))
    }
   }
  }

  item {
   SettingsCard("Exchange Rate", "↻") {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
     Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
      Column(Modifier.weight(1f)) {
       Text("Current rate", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
       Text(state.rate?.let { "1 CNY = $$it" } ?: "Unavailable", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
       Text(
        "Rate date: " + state.rateDate.ifBlank { "Unavailable" },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
       )
       Text(
        "Last fetched: " + (state.cache?.let {
         Instant.ofEpochMilli(it.lastFetchedAt).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        } ?: "Never"),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
       )
      }
      FilledTonalButton(onClick = vm::refreshRate, enabled = !refreshing && !state.settings.manualRateEnabled) {
       Text(if (refreshing) "Updating…" else "Refresh")
      }
     }
     HorizontalDivider()
     Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
      Column(Modifier.weight(1f)) {
       Text("Manual exchange rate", fontWeight = FontWeight.SemiBold)
       Text("Override the saved online rate", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      Switch(
       checked = state.settings.manualRateEnabled || showManual,
       enabled = !busy,
       onCheckedChange = {
        if (it) showManual = true else { showManual = false; vm.disableManual() }
       }
      )
     }
     if (state.settings.manualRateEnabled || showManual) {
      OutlinedTextField(
       value = manual,
       onValueChange = { if (it.matches(Regex("[0-9]{0,4}(\\.[0-9]{0,8})?"))) manual = it },
       label = { Text("1 CNY = … USD") },
       keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
       singleLine = true,
       modifier = Modifier.fillMaxWidth()
      )
      Button(onClick = { vm.manualRate(manual) }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text("Save manual rate") }
     }
    }
   }
  }

  item {
   SettingsCard("Display", "Aa") {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
     Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
      Column(Modifier.weight(1f)) {
       Text("Show CNY under USD", fontWeight = FontWeight.SemiBold)
       Text("Keep the converted RMB amount visible", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      Switch(
       checked = state.settings.showCnySecondaryAmount,
       enabled = !busy,
       onCheckedChange = { vm.settings(state.settings.copy(showCnySecondaryAmount = it)) }
      )
     }
     Choice("Theme", state.settings.themeMode, listOf("System", "Light", "Dark")) {
      vm.settings(state.settings.copy(themeMode = it))
     }
     val options = state.activeAccounts.associateBy { it.name + " (#" + it.id + ")" }
     Choice(
      "Default account",
      options.entries.find { it.value.id == state.settings.defaultAccountId }?.key ?: "First active account",
      options.keys.toList()
     ) { vm.settings(state.settings.copy(defaultAccountId = options.getValue(it).id)) }
    }
   }
  }

  item {
   SettingsCard("Accounts", "▣") {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
     Button(onClick = { navigate("accountEdit/0") }, modifier = Modifier.fillMaxWidth()) { Text("Create Account") }
     state.accounts.forEach { account ->
      Surface(
       onClick = { navigate("accountEdit/${account.id}") },
       modifier = Modifier.fillMaxWidth(),
       shape = RoundedCornerShape(14.dp),
       color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
      ) {
       Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(Modifier.size(36.dp), shape = CircleShape, color = SoftBlue) {
         Box(contentAlignment = Alignment.Center) { Text(account.name.take(1), color = Navy, fontWeight = FontWeight.Bold) }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
         Text(account.name, fontWeight = FontWeight.SemiBold)
         Text(
          account.accountType + if (account.isArchived) " · Archived" else "",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
         )
        }
        Text("›", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
       }
      }
     }
    }
   }
  }

  item {
   SettingsCard("Data & Backup", "⇩") {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
     OutlinedButton(
      onClick = { backup.launch("DollarLedger_Backup_${LocalDate.now()}.json") },
      enabled = !busy,
      modifier = Modifier.fillMaxWidth()
     ) { Text("Export Backup") }
     OutlinedButton(
      onClick = { restore.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) },
      enabled = !busy,
      modifier = Modifier.fillMaxWidth()
     ) { Text("Import Backup") }
     OutlinedButton(
      onClick = { csv.launch("DollarLedger_Transactions_${LocalDate.now()}.csv") },
      enabled = !busy,
      modifier = Modifier.fillMaxWidth()
     ) { Text("Export CSV") }
    }
   }
  }

  item {
   SettingsCard("About", "i") {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
     Text("Dollar Ledger", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
     Text("Version 2.0.1", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
     Text(
      "Local personal finance tracker. No bank is connected. Your data stays on this device unless you export it. Exchange rates are provided by Frankfurter v2.",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
     )
     Spacer(Modifier.height(4.dp))
     TextButton(onClick = { deleteAll = true }, enabled = !busy) {
      Text("Delete All Data", color = MaterialTheme.colorScheme.error)
     }
    }
   }
  }
 }

 if (deleteAll) {
  ConfirmDialog(
   "Delete all ledger data?",
   "All transactions and accounts will be deleted. The four default bank accounts will be recreated with zero balances. Export a backup first if needed.",
   { deleteAll = false },
   { deleteAll = false; vm.reset() }
  )
 }
}

@Composable
private fun SettingsCard(title: String, symbol: String, content: @Composable () -> Unit) {
 Card(
  modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
  shape = RoundedCornerShape(22.dp),
  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
  elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
 ) {
  Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
   Row(verticalAlignment = Alignment.CenterVertically) {
    Surface(Modifier.size(38.dp), shape = CircleShape, color = SoftBlue) {
     Box(contentAlignment = Alignment.Center) { Text(symbol, color = Navy, fontWeight = FontWeight.Bold) }
    }
    Spacer(Modifier.width(12.dp))
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
   }
   content()
  }
 }
}

@Composable
private fun SettingMetric(label: String, value: String, modifier: Modifier) {
 Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)) {
  Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
   Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
   Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
  }
 }
}
