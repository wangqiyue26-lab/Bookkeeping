package com.local.bookkeeping.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.local.bookkeeping.domain.Money
import java.math.BigDecimal
import java.math.MathContext
import java.time.YearMonth

@Composable
fun StatisticsScreen(state: LedgerState) {
 var month by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
 val entries = state.transactions.filter { it.transactionDate.startsWith(month) }
 val income = entries.filter { it.type == "Income" }.sumOf { it.amountUsd }
 val expense = entries.filter { it.type == "Expense" }.sumOf { it.amountUsd }
 val balance = income - expense
 val categories = entries.filter { it.type == "Expense" }
  .groupBy { it.category }
  .mapValues { (_, list) -> list.sumOf { it.amountUsd } }
  .toList()
  .sortedByDescending { it.second }
 val colors = listOf(
  Color(0xFF123E86), Color(0xFF315EA8), Color(0xFF5C82BD),
  Color(0xFF7EA2CB), Color(0xFF9FB8D5), Color(0xFFC4D3E4)
 )

 LazyColumn(
  contentPadding = PaddingValues(bottom = 28.dp),
  verticalArrangement = Arrangement.spacedBy(18.dp)
 ) {
  item {
   Box(
    Modifier.fillMaxWidth().background(Navy).padding(horizontal = 22.dp, vertical = 26.dp)
   ) {
    Text("Statistics", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
   }
  }

  item {
   Card(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
   ) {
    Column(
     Modifier.fillMaxWidth().padding(vertical = 16.dp),
     horizontalAlignment = Alignment.CenterHorizontally,
     verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
     Text(
      if (month == YearMonth.now().toString()) "This month spending" else "$month spending",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.SemiBold
     )
     if (expense == 0L) {
      Box(Modifier.size(220.dp), contentAlignment = Alignment.Center) {
       Canvas(Modifier.size(188.dp)) {
        drawCircle(Color(0xFFE7ECF4), style = Stroke(28.dp.toPx()))
       }
       Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$0.00", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("No spending", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
       }
      }
     } else {
      Box(Modifier.size(220.dp), contentAlignment = Alignment.Center) {
       Canvas(
        Modifier.size(210.dp).semantics {
         contentDescription = "Spending by category donut chart. Category amounts are listed below."
        }
       ) {
        var start = -90f
        val side = size.minDimension - 26.dp.toPx()
        categories.forEachIndexed { index, (_, amount) ->
         val sweep = BigDecimal.valueOf(amount)
          .divide(BigDecimal.valueOf(expense), MathContext.DECIMAL64)
          .multiply(BigDecimal("360"))
          .toFloat()
         drawArc(
          color = colors[index % colors.size],
          startAngle = start,
          sweepAngle = sweep,
          useCenter = false,
          topLeft = Offset((size.width - side) / 2, (size.height - side) / 2),
          size = Size(side, side),
          style = Stroke(28.dp.toPx())
         )
         start += sweep
        }
       }
       Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(Money.display(expense), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Expenses", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
       }
      }
     }
    }
   }
  }

  item {
   Card(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = Navy)
   ) {
    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
     Text(
      if (month == YearMonth.now().toString()) "THIS MONTH" else month,
      style = MaterialTheme.typography.labelMedium,
      color = Color.White.copy(alpha = 0.72f)
     )
     Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
      SummaryMetric("Income", Money.display(income), Modifier.weight(1f))
      SummaryMetric("Expenses", Money.display(expense), Modifier.weight(1f))
      SummaryMetric("Balance", (if (balance > 0) "+" else "") + Money.display(balance), Modifier.weight(1f))
     }
    }
   }
  }

  item {
   Card(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
   ) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
     Text("Month", style = MaterialTheme.typography.labelLarge)
     MonthPicker(YearMonth.parse(month)) { month = it.toString() }
    }
   }
  }

  if (categories.isNotEmpty()) {
   item {
    Text(
     "Spending by Category",
     modifier = Modifier.padding(horizontal = 20.dp),
     style = MaterialTheme.typography.titleLarge,
     fontWeight = FontWeight.Bold
    )
   }
   items(categories) { (category, amount) ->
    Card(
     modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
     shape = RoundedCornerShape(16.dp),
     colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
     Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
      Surface(Modifier.size(12.dp), shape = CircleShape, color = colors[categories.indexOfFirst { it.first == category } % colors.size]) {}
      Spacer(Modifier.width(12.dp))
      Text(category, Modifier.weight(1f), fontWeight = FontWeight.Medium)
      Text(Money.display(amount), fontWeight = FontWeight.SemiBold)
     }
    }
   }
  }
 }
}

@Composable
private fun SummaryMetric(label: String, value: String, modifier: Modifier) {
 Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
  Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.68f))
  Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
 }
}
