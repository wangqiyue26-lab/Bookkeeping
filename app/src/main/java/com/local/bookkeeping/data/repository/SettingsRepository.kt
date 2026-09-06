package com.local.bookkeeping.data.repository

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

private val Context.ledgerSettings by preferencesDataStore("ledger_settings")

@Serializable
data class LedgerSettings(
 val manualRate: String = "",
 val manualRateEnabled: Boolean = false,
 val showCnySecondaryAmount: Boolean = true,
 val themeMode: String = "System",
 val defaultAccountId: Long = 1,
 val manualRateDate: String = ""
)

class SettingsRepository(context: Context) {
 private val store = context.ledgerSettings
 private val manual = stringPreferencesKey("manualRate")
 private val enabled = booleanPreferencesKey("manualEnabled")
 private val show = booleanPreferencesKey("showCny")
 private val theme = stringPreferencesKey("theme")
 private val account = longPreferencesKey("defaultAccount")
 private val date = stringPreferencesKey("manualDate")
 val settings = store.data.map { p -> LedgerSettings(p[manual] ?: "", p[enabled] ?: false, p[show] ?: true, p[theme] ?: "System", p[account] ?: 1, p[date] ?: "") }
 suspend fun save(s: LedgerSettings) {
  store.edit { p ->
   p[manual] = s.manualRate; p[enabled] = s.manualRateEnabled
   p[show] = s.showCnySecondaryAmount; p[theme] = s.themeMode
   p[account] = s.defaultAccountId; p[date] = s.manualRateDate
  }
 }
}
