package com.local.bookkeeping.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.*

@Composable
fun LedgerApp(vm: LedgerViewModel) {
 val state by vm.state.collectAsStateWithLifecycle()
 val busy by vm.busy.collectAsStateWithLifecycle()
 val message by vm.message.collectAsStateWithLifecycle()
 val pending by vm.pendingImport.collectAsStateWithLifecycle()
 val nav = rememberNavController()
 val entry by nav.currentBackStackEntryAsState()
 val route = entry?.destination?.route ?: "home"
 val tabs = listOf("home" to "Home", "transactions" to "Transactions", "statistics" to "Statistics", "settings" to "Settings")
 val snack = remember { SnackbarHostState() }
 LaunchedEffect(message) { message?.let { snack.showSnackbar(it); vm.dismissMessage() } }
 LedgerTheme(state.settings.themeMode) {
  Scaffold(
   containerColor = MaterialTheme.colorScheme.background,
   snackbarHost = { SnackbarHost(snack) },
   bottomBar = {
    if (route in tabs.map { it.first }) {
     NavigationBar(
      containerColor = MaterialTheme.colorScheme.surface,
      tonalElevation = 0.dp
     ) {
      tabs.forEachIndexed { index, (path, title) ->
       NavigationBarItem(
        selected = route == path,
        onClick = {
         nav.navigate(path) { popUpTo("home") { saveState = true }; launchSingleTop = true; restoreState = true }
        },
        icon = { Icon(navIcon(index), contentDescription = title) },
        label = { Text(title) },
        colors = NavigationBarItemDefaults.colors(
         selectedIconColor = MaterialTheme.colorScheme.primary,
         selectedTextColor = MaterialTheme.colorScheme.primary,
         indicatorColor = MaterialTheme.colorScheme.primaryContainer,
         unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
         unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
       )
      }
     }
    }
   }
  ) { padding ->
   Column(Modifier.fillMaxSize().padding(padding)) {
    if (route !in tabs.map { it.first }) {
     TextButton(onClick = { nav.popBackStack() }, enabled = !busy) { Text("‹ Back") }
    }
    if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
    NavHost(navController = nav, startDestination = "home", modifier = Modifier.weight(1f)) {
     composable("home") { HomeScreen(state, vm, { nav.navigate(it) }) }
     composable("transactions") { TransactionsScreen(state) { nav.navigate("transaction/$it") } }
     composable("statistics") { StatisticsScreen(state) }
     composable("settings") { SettingsScreen(state, vm) { nav.navigate(it) } }
     composable("account/{id}") { back ->
      AccountScreen(back.arguments?.getString("id")?.toLongOrNull() ?: 0, state) { nav.navigate(it) }
     }
     composable("accountEdit/{id}") { back ->
      AccountEditor(back.arguments?.getString("id")?.toLongOrNull() ?: 0, state, vm) { nav.popBackStack() }
     }
     composable("add/{type}") { back ->
      TransactionEditor(0, back.arguments?.getString("type") ?: "Expense", state, vm, { nav.navigate("settings") }) { nav.popBackStack() }
     }
     composable("edit/{id}") { back ->
      TransactionEditor(back.arguments?.getString("id")?.toLongOrNull() ?: 0, "Expense", state, vm, { nav.navigate("settings") }) { nav.popBackStack() }
     }
     composable("transaction/{id}") { back ->
      TransactionDetail(back.arguments?.getString("id")?.toLongOrNull() ?: 0, state, vm, { nav.navigate(it) }) { nav.popBackStack() }
     }
    }
   }
  }
  if (pending != null) ConfirmDialog("Import backup?", "This will replace your current local data.", vm::cancelImport, vm::confirmImport, "Import")
 }
}

private fun navIcon(index: Int): ImageVector = ImageVector.Builder("Navigation", 24.dp, 24.dp, 24f, 24f).apply {
 path(fill = androidx.compose.ui.graphics.SolidColor(androidx.compose.ui.graphics.Color.Black)) {
  when (index) {
   0 -> { moveTo(3f, 11f); lineTo(12f, 3f); lineTo(21f, 11f); lineTo(19f, 11f); lineTo(19f, 21f); lineTo(14f, 21f); lineTo(14f, 14f); lineTo(10f, 14f); lineTo(10f, 21f); lineTo(5f, 21f); lineTo(5f, 11f); close() }
   1 -> { for (y in listOf(5f, 11f, 17f)) { moveTo(4f,y); lineTo(20f,y); lineTo(20f,y+2); lineTo(4f,y+2); close() } }
   2 -> { for ((x,y) in listOf(4f to 14f, 10f to 9f, 16f to 3f)) { moveTo(x,y); lineTo(x+4,y); lineTo(x+4,21f); lineTo(x,21f); close() } }
   else -> { moveTo(4f,4f); lineTo(20f,4f); lineTo(20f,20f); lineTo(4f,20f); close(); moveTo(8f,8f); lineTo(8f,16f); lineTo(16f,16f); lineTo(16f,8f); close() }
  }
 }
}.build()
