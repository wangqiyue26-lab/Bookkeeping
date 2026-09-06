package com.local.bookkeeping.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.local.bookkeeping.domain.Money

@Composable
fun WalletScreen(state: LedgerState) {
 LazyColumn(
  contentPadding = PaddingValues(bottom = 32.dp),
  verticalArrangement = Arrangement.spacedBy(20.dp)
 ) {
  item {
   Column(
    modifier = Modifier
     .fillMaxWidth()
     .background(Navy)
     .padding(horizontal = 20.dp, vertical = 54.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(10.dp)
   ) {
    Text(
     text = state.netUsdDisplay(),
     color = Color.White,
     fontSize = 64.sp,
     lineHeight = 68.sp,
     fontWeight = FontWeight.Bold
    )
    if (state.settings.showCnySecondaryAmount) {
     Text(
      text = "≈ ${state.netCnyDisplay()}",
      style = MaterialTheme.typography.titleMedium,
      color = Color.White.copy(alpha = 0.76f)
     )
    }
   }
  }

  item {
   Row(
    Modifier.fillMaxWidth().padding(horizontal = 20.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp)
   ) {
    WalletMetric("ASSETS", state.usd(state.totalAssetCny), Modifier.weight(1f))
    WalletMetric("DEBT", Money.display(state.totalCreditCardDebtUsdCents), Modifier.weight(1f))
   }
  }

  item {
   Card(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
    shape = RoundedCornerShape(28.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
   ) {
    DottedWorldMap(
     modifier = Modifier
      .fillMaxWidth()
      .height(250.dp)
      .padding(horizontal = 18.dp, vertical = 20.dp)
    )
   }
  }
 }
}

@Composable
private fun WalletMetric(label: String, value: String, modifier: Modifier) {
 Card(
  modifier = modifier,
  shape = RoundedCornerShape(18.dp),
  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
 ) {
  Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
   Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
   Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
  }
 }
}

@Composable
private fun DottedWorldMap(modifier: Modifier = Modifier) {
 Canvas(modifier) {
  fun ellipse(x: Float, y: Float, cx: Float, cy: Float, rx: Float, ry: Float): Boolean {
   val dx = (x - cx) / rx
   val dy = (y - cy) / ry
   return dx * dx + dy * dy <= 1f
  }

  val stepX = 0.025f
  val stepY = 0.055f
  var y = 0.08f
  while (y <= 0.90f) {
   var x = 0.05f
   while (x <= 0.95f) {
    val northAmerica = ellipse(x, y, 0.25f, 0.30f, 0.16f, 0.16f) || ellipse(x, y, 0.16f, 0.20f, 0.08f, 0.07f)
    val centralAmerica = ellipse(x, y, 0.31f, 0.43f, 0.05f, 0.08f)
    val southAmerica = ellipse(x, y, 0.38f, 0.62f, 0.08f, 0.21f)
    val greenland = ellipse(x, y, 0.40f, 0.13f, 0.055f, 0.065f)
    val europe = ellipse(x, y, 0.53f, 0.29f, 0.075f, 0.075f)
    val africa = ellipse(x, y, 0.55f, 0.51f, 0.10f, 0.18f)
    val asia = ellipse(x, y, 0.69f, 0.31f, 0.20f, 0.13f) || ellipse(x, y, 0.76f, 0.43f, 0.12f, 0.09f)
    val japan = ellipse(x, y, 0.88f, 0.38f, 0.025f, 0.07f)
    val australia = ellipse(x, y, 0.82f, 0.68f, 0.10f, 0.075f)
    val madagascar = ellipse(x, y, 0.66f, 0.67f, 0.025f, 0.07f)
    val land = northAmerica || centralAmerica || southAmerica || greenland || europe || africa || asia || japan || australia || madagascar
    if (land) {
     drawCircle(
      color = Navy.copy(alpha = 0.82f),
      radius = 2.1.dp.toPx(),
      center = Offset(x * size.width, y * size.height)
     )
    }
    x += stepX
   }
   y += stepY
  }
 }
}
