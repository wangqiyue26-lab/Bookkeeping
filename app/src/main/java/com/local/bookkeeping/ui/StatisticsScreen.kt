package com.local.bookkeeping.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.local.bookkeeping.domain.Money
import java.time.YearMonth
import java.math.BigDecimal
import java.math.MathContext

@Composable
fun StatisticsScreen(state: LedgerState) {
 var month by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
 val entries = state.transactions.filter { it.transactionDate.startsWith(month) }
 val income = entries.filter { it.type == "Income" }.sumOf { it.amountUsd }
 val expense = entries.filter { it.type == "Expense" }.sumOf { it.amountUsd }
 val categories = entries.filter { it.type == "Expense" }.groupBy { it.category }.mapValues { (_, list) -> list.sumOf { it.amountUsd } }.toList().sortedByDescending { it.second }
 val colors = listOf(Color(0xFF15579C), Color(0xFF3B789F), Color(0xFF709AAF), Color(0xFF678773), Color(0xFFA09D79), Color(0xFF9C7373))
 LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
  item { SectionTitle("Statistics") }
  item { MonthPicker(YearMonth.parse(month)) { month = it.toString() } }
  item { Text(if (month == YearMonth.now().toString()) "This Month" else month) }
  item {
   Card(modifier = Modifier.fillMaxWidth()) {
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
     listOf("Income" to income, "Expenses" to expense, "Net" to (income - expense)).forEach { (label, amount) ->
      Text(label, style = MaterialTheme.typography.labelLarge)
      Text((if (label == "Net" && amount > 0) "+" else "") + Money.display(amount), style = MaterialTheme.typography.headlineSmall)
     }
    }
   }
  }
  item { Text("Uses each transaction's saved USD amount and historical rate.", style = MaterialTheme.typography.bodySmall) }
  item { SectionTitle("Spending by Category") }
  if (expense == 0L) item { Text("No spending recorded for this month.") }
  else item {
   Canvas(Modifier.fillMaxWidth().height(220.dp).semantics { contentDescription = "Spending by category donut chart. Category amounts are listed below." }) {
    var start = -90f
    categories.forEachIndexed { index, (_, amount) ->
     val sweep = BigDecimal.valueOf(amount).divide(BigDecimal.valueOf(expense), MathContext.DECIMAL64).multiply(BigDecimal("360")).toFloat()
     val side = size.minDimension - 40.dp.toPx()
     drawArc(colors[index % colors.size], start, sweep, false,
      topLeft = androidx.compose.ui.geometry.Offset((size.width - side) / 2, (size.height - side) / 2),
      size = androidx.compose.ui.geometry.Size(side, side), style = Stroke(28.dp.toPx()))
     start += sweep
    }
   }
  }
  items(categories) { (category, amount) ->
   Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(category + " · " + Money.display(amount))
    val fraction = if (expense > 0) BigDecimal.valueOf(amount).divide(BigDecimal.valueOf(expense), MathContext.DECIMAL64).toFloat() else 0f
    LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth().height(8.dp))
   }
  }
 }
}
