package com.local.bookkeeping.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.local.bookkeeping.LedgerApplication
import com.local.bookkeeping.data.database.*
import com.local.bookkeeping.data.repository.*
import com.local.bookkeeping.domain.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate

data class LedgerState(
 val accounts: List<Account> = emptyList(),
 val transactions: List<LedgerTransaction> = emptyList(),
 val cache: ExchangeRateCache? = null,
 val settings: LedgerSettings = LedgerSettings(),
 val loaded: Boolean = false
) {
 val rate: String? get() = if (settings.manualRateEnabled) settings.manualRate.takeIf { it.isNotBlank() } else cache?.rate
 val rateDate: String get() = if (settings.manualRateEnabled) settings.manualRateDate else cache?.rateDate ?: ""
 val activeAccounts get() = accounts.filterNot { it.isArchived }
 val assetAccounts get() = activeAccounts.filterNot { it.accountType == "Credit Card" }
 val creditCardAccounts get() = activeAccounts.filter { it.accountType == "Credit Card" }
 fun balance(id: Long): Long = Money.balance(accounts.find { it.id == id }?.openingBalanceCny ?: 0, transactions.filter { it.accountId == id }.map { it.type to it.amountCny })
 val totalAssetCny get() = assetAccounts.fold(0L) { sum, a -> Math.addExact(sum, balance(a.id)) }
 val totalCreditCardDebtUsdCents get() = creditCardAccounts.fold(0L) { sum, a -> Math.addExact(sum, a.creditCardDebtUsdCents) }
 val totalCny get() = totalAssetCny
 val netTotalUsdCents: Long? get() = rate?.let { Money.netUsdCents(totalAssetCny, it, totalCreditCardDebtUsdCents) }
 val netTotalCnyCents: Long? get() = rate?.let { Money.netCnyCents(totalAssetCny, it, totalCreditCardDebtUsdCents) }
 fun netUsdDisplay() = netTotalUsdCents?.let { Money.display(it) } ?: "—"
 fun netCnyDisplay() = netTotalCnyCents?.let { Money.display(it, false) } ?: "—"
 fun usd(cny: Long) = rate?.let { Money.display(Money.usdCents(cny, it)) } ?: "—"
}

class LedgerViewModel(application: Application) : AndroidViewModel(application) {
 private val app = application as LedgerApplication
 private val mutation = Mutex()
 private val _busy = MutableStateFlow(false)
 val busy = _busy.asStateFlow()
 private val _refreshing = MutableStateFlow(false)
 val refreshing = _refreshing.asStateFlow()
 private val _message = MutableStateFlow<String?>(null)
 val message = _message.asStateFlow()
 val state = combine(app.ledger.accounts, app.ledger.transactions, app.ledger.rate, app.settings.settings) { a, t, r, s ->
  LedgerState(a, t, r, s, true)
 }.catch { _message.value = "Unable to read local data. Please reopen the app." }
  .stateIn(viewModelScope, SharingStarted.Eagerly, LedgerState())
 private val _pendingImport = MutableStateFlow<LedgerBackup?>(null)
 val pendingImport = _pendingImport.asStateFlow()
 init {
  action { app.ledger.initialize() }
  viewModelScope.launch { refresh(false) }
 }
 fun dismissMessage() { _message.value = null }
 fun action(success: (() -> Unit)? = null, work: suspend () -> Unit) {
  if (_busy.value) return
  _busy.value = true
  viewModelScope.launch {
   try { mutation.withLock { withContext(Dispatchers.IO) { work() } }; success?.invoke() }
   catch (e: CancellationException) { throw e }
   catch (e: IllegalArgumentException) { _message.value = e.message ?: "Please check your input." }
   catch (_: Exception) { _message.value = "Unable to save local data. Please try again." }
   finally { _busy.value = false }
  }
 }
 fun refreshRate() { viewModelScope.launch { refresh(true) } }
 private suspend fun refresh(force: Boolean) {
  if (_refreshing.value) return
  _refreshing.value = true
  try { withContext(Dispatchers.IO) { app.exchange.refresh(force) } }
  catch (e: CancellationException) { throw e }
  catch (_: Exception) { _message.value = "Unable to update exchange rate. Using your last saved rate, if available." }
  finally { _refreshing.value = false }
 }
 fun saveAccount(account: Account, done: () -> Unit) = action(done) { app.ledger.saveAccount(account) }
 fun deleteAccount(id: Long, done: () -> Unit) = action(done) { app.ledger.deleteAccount(id) }
 fun saveTransaction(entry: LedgerTransaction, done: () -> Unit) = action(done) { app.ledger.saveTransaction(entry) }
 fun deleteTransaction(id: Long, done: () -> Unit) = action(done) { app.ledger.deleteTransaction(id) }
 fun settings(value: LedgerSettings) = action {
  if (value.manualRateEnabled) Money.validRate(value.manualRate)
  app.settings.save(value)
 }
 fun manualRate(value: String) = action {
  val rate = Money.validRate(value).stripTrailingZeros().toPlainString()
  app.settings.save(app.settings.settings.first().copy(manualRate = rate, manualRateEnabled = true, manualRateDate = LocalDate.now().toString()))
 }
 fun disableManual() = action(success = { refreshRate() }) {
  app.settings.save(app.settings.settings.first().copy(manualRateEnabled = false))
 }
 fun reset() = action {
  app.ledger.reset()
  val first = app.ledger.dao.accountSnapshot().first().id
  app.settings.save(app.settings.settings.first().copy(defaultAccountId = first))
 }
 fun export(uri: Uri?, csv: Boolean) {
  if (uri == null) return
  action {
   val text = if (csv) app.backup.exportCsv() else app.backup.exportJson()
   val stream = app.contentResolver.openOutputStream(uri, "wt") ?: error("Unavailable destination")
   stream.bufferedWriter(Charsets.UTF_8).use { it.write(text) }
   _message.value = if (csv) "CSV exported." else "Backup exported."
  }
 }
 fun readImport(uri: Uri?) {
  if (uri == null) return
  action {
   try {
    val input = app.contentResolver.openInputStream(uri) ?: error("Unavailable file")
    val bytes = input.use { stream ->
     val output = java.io.ByteArrayOutputStream()
     val buffer = ByteArray(8192)
     var count = stream.read(buffer)
     while (count >= 0) {
      require(output.size() + count <= 20 * 1024 * 1024)
      output.write(buffer, 0, count)
      count = stream.read(buffer)
     }
     output.toByteArray()
    }
    require(bytes.size <= 20 * 1024 * 1024)
    _pendingImport.value = BackupCodec.decode(bytes.toString(Charsets.UTF_8))
   } catch (e: CancellationException) { throw e }
   catch (_: Exception) { _message.value = "Invalid backup. Choose a Dollar Ledger JSON backup (up to 20 MB)." }
  }
 }
 fun cancelImport() { _pendingImport.value = null }
 fun confirmImport() {
  val backup = _pendingImport.value ?: return
  action(success = { _pendingImport.value = null }) {
   app.backup.restore(backup)
   _message.value = "Backup restored."
  }
 }
}
