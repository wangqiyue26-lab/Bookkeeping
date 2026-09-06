package com.local.bookkeeping.ui

import android.util.Base64
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
import kotlin.math.min

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
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
   ) {
    ReferenceWorldMap(
     modifier = Modifier
      .fillMaxWidth()
      .aspectRatio(MAP_WIDTH / MAP_HEIGHT)
      .padding(8.dp)
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

private const val MAP_WIDTH = 1536f
private const val MAP_HEIGHT = 833f

@Composable
private fun ReferenceWorldMap(modifier: Modifier = Modifier) {
 Canvas(modifier) {
  val scale = min(size.width / MAP_WIDTH, size.height / MAP_HEIGHT)
  val offsetX = (size.width - MAP_WIDTH * scale) / 2f
  val offsetY = (size.height - MAP_HEIGHT * scale) / 2f
  val radius = 3.0f * scale
  var index = 0
  while (index < worldMapDots.size) {
   drawCircle(
    color = Color.Black,
    radius = radius,
    center = Offset(
     offsetX + worldMapDots[index] * scale,
     offsetY + worldMapDots[index + 1] * scale
    )
   )
   index += 2
  }
 }
}

private val worldMapDots: IntArray by lazy {
 val bytes = Base64.decode(WORLD_MAP_DOTS, Base64.NO_WRAP)
 IntArray(bytes.size / 2) { index ->
  val offset = index * 2
  ((bytes[offset].toInt() and 0xFF) shl 8) or (bytes[offset + 1].toInt() and 0xFF)
 }
}

// Exact dot centers extracted from the 1536 × 833 reference image supplied for the Wallet screen.
private const val WORLD_MAP_DOTS = "AkUAHQJSAB0CXgAdAmoAHQJ2AB0BjQApAZkAKQGmACkBsgApAb4AKQHKACkB1wApAeMAKQIUACkCIAApAiwAKQI5ACkCRQApAlEAKQJeACkCagApAnYAKQKCACkCjwApAWgANQF1ADUBgQA1AY0ANQGZADUBpgA1AbIANQG+ADUBygA1AdcANQHjADUB7wA1AfsANQIIADUCFAA1AiAANQItADUCOQA1AkUANQJRADUCXgA1AmoANQJ2ADUCgwA1Ao8ANQKbADUCpwA1ArQANQFcAEIBaABCAXUAQgGBAEIBjQBCAZkAQgGmAEIBsgBCAb4AQgHXAEIB4wBCAe8AQgH8AEICCABCAhQAQgIgAEICLQBCAjkAQgJFAEICUQBCAl4AQgJqAEICdgBCAoMAQgKPAEICmwBCAWgATgF1AE4BgQBOAY0ATgGZAE4BpgBOAb4ATgHLAE4B1wBOAeMATgHvAE4B/ABOAggATgIUAE4CIABOAi0ATgI5AE4CRQBOAlEATgJeAE4CagBOAnYATgKDAE4CjwBOBGEAVQGBAFoBjQBaAZkAWgG+AFoBygBaAdcAWgHjAFoB7wBaAfwAWgIIAFoCFABaAiAAWgItAFoCOQBaAkUAWgJSAFoCXgBaAmoAWgJ2AFoCggBaAo8AWgE3AGEDIgBhAy4AYQM7AGEDRwBhBFUAYQRhAGEEbQBhAcsAZgHjAGYB/ABmAggAZgIUAGYCOQBmAl4AZgJqAGYCdgBmAoIAZgHXAGcB7wBnAiAAZwItAGcCRQBnAlEAZwKPAGcBEgBtASsAbQE3AG0DCgBtAxYAbQMiAG0DLgBtBGEAbQRtAG0EegBtBIYAbQFDAG4COQByAkUAcgH7AHMCCABzAhQAcwIgAHMCLQBzAlEAcwJeAHMCagBzAnYAcwKDAHMCjwBzAO0AegD6AHoBBgB6ARIAegErAHoBUAB6AxYAegMiAHoDOwB6BHoAegSGAHoCCAB/AhQAfwIgAH8CLAB/AjkAfwJGAH8CUQB/Al4AfwJqAH8CdgB/AoMAfwDhAIYA7QCGAPoAhgEGAIYBEgCGAR8AhgErAIYBNwCGAUMAhgFcAIYBaACGA84AhgPaAIYD5wCGA/MAhgRVAIYEYQCGBG4AhgR6AIYEhgCGBJIAhgSfAIYEqwCGBQ0AhgUaAIYFJgCGAggAiwIUAIsCIACLAi0AiwI5AIsCRQCLAlIAiwJeAIsCagCLAnYAiwKDAIsA4QCSAQYAkgFoAJIBdQCSAYEAkgGNAJIDwgCSA84AkgQ8AJIESQCSBFUAkgRhAJIEbgCSBHoAkgSGAJIEkgCSBJ8AkgSrAJIFDQCSBRkAkgUmAJIFMgCSBT4AkgIIAJcCFACXAiAAlwI5AJcCRQCXAmoAlwJ2AJcCLQCYAlEAmAJeAJgCgwCYANUAngDhAJ4A7gCeAR8AngE3AJ4BQwCeAVAAngFcAJ4BdQCeAYEAngGNAJ4DtQCeA8IAngPzAJ4D/wCeBAsAngQXAJ4EMACeBDwAngRJAJ4EVQCeBGEAngRtAJ4EegCeBIYAngSSAJ4EngCeBKsAngS3AJ4EwwCeBNAAngTcAJ4FGQCeBSUAngD6AJ8BKwCfAWgAnwQkAJ8E6ACfAggApAIUAKQCIACkAi0ApAI5AKQCRQCkAlIApAJeAKQCagCkAY0AqgNHAKoD/wCqBFUAqgRhAKoEqwCqBLcAqgTcAKoFDQCqAE4AqwBaAKsA1QCrAOEAqwDtAKsA+gCrAQYAqwESAKsBHgCrASsAqwFEAKsBUACrAWgAqwF0AKsBgQCrAZoAqwGmAKsBsgCrA1MAqwOpAKsDtQCrA8IAqwPmAKsD8wCrBAsAqwQYAKsEJACrBDAAqwQ8AKsESQCrBG4AqwR6AKsEhgCrBJIAqwSfAKsEwwCrBNAAqwToAKsE9QCrBQEAqwUZAKsFJgCrBTIAqwU+AKsFSgCrBbkAqwXFAKsCCACwAhQAsAIgALACLQCwAjkAsAJFALACUgCwAEIAtwBOALcAWgC3AGYAtwBzALcAfwC3AIsAtwCYALcAvAC3AMkAtwDVALcA4QC3APoAtwEGALcBEgC3AR4AtwErALcBNwC3AUMAtwFQALcBXAC3AXUAtwGBALcBjQC3AZoAtwGmALcBsgC3Ab4AtwMiALcDLgC3AzsAtwNHALcDUwC3A18AtwNsALcDeAC3A4QAtwORALcDnQC3A6kAtwO1ALcDwgC3A84AtwPaALcD5gC3A/MAwP8AwwQLAMMEGADD..."