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
import java.io.ByteArrayInputStream
import java.util.zip.InflaterInputStream
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
 val compressed = Base64.decode(WORLD_MAP_DOTS_ZLIB, Base64.NO_WRAP)
 val bytes = InflaterInputStream(ByteArrayInputStream(compressed)).use { it.readBytes() }
 IntArray(bytes.size / 2) { index ->
  val offset = index * 2
  ((bytes[offset].toInt() and 0xFF) shl 8) or (bytes[offset + 1].toInt() and 0xFF)
 }
}

// Exact black-dot centers extracted from the user's 1536 × 833 reference image.
private const val WORLD_MAP_DOTS_ZLIB = "eNod13FonPd9x/HnOUmj7RBmbUl7E2tLqE+UbCEc14Uu4RDKOi8XIdKZ6GQR2ppDTujsHFevwUinmWxBO2RCG3EocYvti3Dd2ZJcY+LdjmQm+HJkSYP9KCFNZh6uDSbu7WgWjB9rI6nxXs4fb2Kiu9/383l/f8+dlPp28Gepv8N38QM8GfxZ+EzwjfAwTuAszuMNvIsrwTdSX8TX8Of4Fr6N7+C7+AGexD/hx17/ePDN8Ed4Cs/gME7gLM7jDbyLK/gInwTfTH0GX8TX8Bf4Fr6N7+C7+AGexD/jx/gZfoEXvf/RYNzMcTPHzRw3c9zMcTPHzRw3c9y8cfPGzRsP/xCMmzdu3rh54+aNmzdu3rh54+aNmzdu3rh54+aNm3d7xqQZk2ZMmjFpxqQZk86fDH+Fd3EFH+EPwaQZk2ZMmjFpxqQZk2ZMmjFpxqQZk2ZMmjFpxuTg7uARZ884e8bZM86d4WrGuTPOnXHujHNnnDvj3Bnnzjh3xrkzzp2x0xnnzjh3xrkz9jHj3Jnw3mD3wJ24G/dhR7B78BHsxr5gt+x7nL/H2XucvcfZe5y5x1l7nLXHWXuctUeOx2R4zNzHzH3MzMd0ecyMx8LPB/vCu3BvsG/gc7gDd+LuYJ85+8zZNziHRa95IHjC+T/0/h/a/X4z95u537n7nbvfmfvN3m/2frP387PfjP3Bh8Fc8HEwF/4R/o+78HAwZ9acWXO6zZkxZ8acMw8686AzD7qzB807mPobfAffxQ/wJP45OBi8Hyw6e9HZi85edPZi+FXchXvxAB7F48HiwEVcxu9wPVjkcVG/xcEnMIdFLOMY1oLFoWH8KTLBokyHZDok0yE9D8l0iINDMh2S6ZBMh2Q6JNMhmZZlWTZz2Z1bdi+W3YvlgQu4GCwP3o8H8Qh24wnMYRHLOIa1YNn85aE0MsgiHyzL8Zwcz8nxnAzPyfCc2c+Z/Zxcz8vzvDzPy/F88E5wVJajwf8ERzk5ysdRPo7yfpSTo5Idle2obEcHzuECruNWcHTwj/El3IP78SAewW7swxwWsYyjWEMTbUSIg6OyHx3abv7HwTH7OMbHscGvoxcc0+O4Hsf1OC73cV2O63Lcc3Bc/uM6HZfrlPt+Sp5TZp8y+5Q5p8w55fxT/JwKJoO1YAbv4H18iI+DNf7X3IW18Cu4C3+Nh/E4/gFP4ac4gbPB2sBOnMQ5XMBVXA/WeFgb/DK+jntwPx7EE5jDIpZxDG1E6OFGsDYUIo0MssijgBY6wRoHZzg4w8EZDs5wcIaDMxycCcaDpm5N3ZrBHuzHQRzC83gZr+MdvI+Pg6bOTZ2bOjd1btp3076bejftu2nfTb2bvDZ1b+re1L3ps6rpGWx63puewybnTT6aA0dwEZdxFddxK2hy0uSkyUmTkyYnTU6adtS0oyY3TW6a3DS5aXLTtLemvTUHX0WEGD0kQZOnpl02uWpy1eSqyVWTq+ZQESWUUUUNdTSwETT5e5m/l/l7mb//CL4RtINvYhyTmMEe7MdBHMLzOI4zeBmv4x28jw/xcdD2vLQ5bHPY5rDNYZvDtvvTdn/aPLa5a3PX9jnf9jnW5rDNYZvDNodtDtsD38NeHMDTeBZHcBLncAEXcRlXcR23gjbHbY7bHLc5bnPc5rjNcZvjtmewzXGb4zbHbY7bHLc5bruHbY7bHLc5bnPc5rjNcZvjNsdtjtsctzluc9zmuM1xe2geNdTRwAZa6Oh5OHiV4//k/HXOX+f8dW4vuVeXdL6k6yX9Lsl9SdZLcl2S65IZl5xzyW4iu4nsJrKbyG4ie4nsJbKXyF4ie4nsJbKXyOmRvUT2EtlLZC+ROx6545H9RPYT2U9kP5H9RPYT2U9kN5HdRHYT2U3kuy8a+CzuwJ24GzuwE3txAE/jCE7iHC7gIi7jKq7jVhDZS2QnkZ1EdhLZR2QfkX1E9hHZR2QPkT1E9hDZQ2QPkT1E9hDZQ2QPkT1E9hDZQ2QPkR1EdhDZQWQHkR1EdhDZQcR/xH80tIluEHH/Dvfv8BnzGfMZ8xnzGQePYT8O4hCex3GcwX/gdbyD9/EhPg5iTuPwC/gq7sK9eAAP41E8jh/hKRzGCZzFefwqiFM3g5jfmN+Y35jf2H2IOY45jt2LmOeY55jn2B2JuY65jrmOuY65jrmOuY65jrmOuY59B8V8x3zHfMfuVcx5zHnMecx5zHnsrsXuWsx9zH3Mfcx9zH3Mfcx9zH3Mfcx97Hsp5j7mPnY/Y/5j/mP+Y/5j/mP+Y/5jdze2g5j/OPjLoMd3j+8e3z33v8d3j+8e3z2+e3z3+O65wz2+e3z3+O75Hu7x3eO75w73fE73+O7x3eO7x3eP7x7PPZ/PPZ57HPfc4V7qGm4GPb+f9XjucdzjuMdxj+Mexz2Oexz3OO4N/ARHcBLncAEXcRlXcT3o8dvzrPb47fHb47c3+Ld4BLvxBOawiGUcwxqaaCNCjB6SoMdvj98evz13u8dvj98ev72hKZRQRhU11NHAOloy3Qr+231O+E34TfhN3OeE34TfhN+E34TfhN+E34TfhN+E38R9TvhN+E34TdznhN+E34TfhN/E/U14TdzfhNOE08TdTThN3N2E04TThM+Ez4TPhM+Ez4TPhM+Ez4TPhM+Ez4TPRPaEy4TLhMuEy8RdTbhM3NOEx4THhMeEx4TDhMOEw4TDhMOEw4TDhMOEw4TDhMOEw4TDxB1N+Evcz4S/hL/E7m/Y5Q27vGFnN+znRvBOGIR3hUHqWhgM7MCtMBhcQxNxGAyVwsBfRKG/dEJ/YYT+Agj9thUGZ/AyXsf7+B98HIbhH+Hz+CruxV/jYTyKwziBsziPN8IwdTMMBz6HO3AfduJ72IsDeBrP4ghO4hwu4CIu4yquh+HgH+PL+Druwf14EI9gN57AHBaxjGN4FZfQw40wHBJpaBhplPUZD4f1HtZ7WN9hfYf1HeZsWOfh4EN8HA7rPKzzcPgV3IV78QAexqN4HCdwFufxK7wbDus+rPfwwN24DzuwE9/HXhzA03gWR3AS53ABF3EZV3Edt8JhDoY5GOZgmINhDoY5GOZgmIPhwX2YwyKWcQxraKKNCDF6uBEO8zHMx7A7MMzHsL/40/4KT/OS5iPNR5qPNB9pPtJ8pPlI85HmI+0OpPlI85HmI81Hmo80H+nwR3gKh3ECZ3Eeb+BdXAnT/KQHPos7cCfuxn3YgZ34HvbiAJ7GsziCkziHC7iIy7iK67gVpnlK85TmKc1Tmqc0T2me0jyleUrzlOYpzVN68CjW0EQbEWL0kIRpntI8pd2b9FARJZQ/dbFd7u3ybpdvu5nbvXe712/nMsNdhrsMdxnuMrxleMvwluEtw1uGtwxvGd4yvGV4y/CW4S3DW4a3DG8Z3jK8ZXjLhB+FGc9zhr+MZyvDX4a7DHcZ3jLuV4a3DG+ZgZ/gCE7iHC7gIi7jKq7jVpjhLMNXhq8MXxm+Mnxl+MrwleErw1eGr4x7leErw1eGrwxXGa4yXGV4yvCUGcqg9KmDLAdZDrIcZDnL8pDlIctDlocsD1kesjxkecjykOUhy0M2fAaH8XOcxXl8FGZT7+EDXMPNMOseZXnI2keWiywXWXvJ8pF1j7J8ZPnI2lPWPcpykuUky0mWkywnWU6ynGQ5yXKS5SQ7+CV8HffgfjyIR7Ab+zCHRSzjGNbQRBsRYvSQhFn3IstPlp8sP7ed5H2u5jnJc5LnJO8zJs9JnpO8z9Y8J3lO8pzkw3/AU3gGh3ECZ3Eeb+AKPsIfwjw/eX7y/OT5ybsneX7y/OT5yQ/8FXZgJ76PvTiAp/ETHMFJ/Bsu4CIu4yqu41aY5yfvzuf5yfOT5yc/+Ld4BLvxBOawiGUcwxqaaCNCjB6SMM9Pnp88P3l+8vwU3JkCPwVuCu5LgZsCNwVuCtwUeCnwUuCkwEmBkwInBU4KnBQ8MwUuClwUeCjwUOChwEOBh4J7UuChwEPBPSnwUOChwEPBPSnoX9C/oH9B/4L+Bf0L+hf0L7gfBf0L+hf0L7gfBd0Luhd0L+he0L2ge0H3gu4F3Qu+lwq6F3Qv6F7QvaB7QfeC7gX34SF7f8i+H7LDh+zjIft4iNuH/GyKhykepvx8Svep1Iu4hpvhlD5TXj/l9VNyT8k8Je+UvFPePyXflCxTskyZN8Vzkeciz0WeizwXPY9FfosyFDkueh6L/Bb5LfJbTL2CN/EePgiLvBY5LXJa5LTIX5G/oueryF+RuyJvRfelyFeRryJfRb6KnqMiV0WuilwVuSpyVHQ/ihwVOSpyVOSoyE1R3pLnpiRviYuSzCXPTYmTEicl2Uuyl2QvyV5yP0o8lXgqhT/FibDEV0mHkg4lHUq8ldyLkvwl+UscltyJkmej5E6U3ImSTiXPRonbErcld6OkW4njkn4lnks6lrgucV3StaRrSdeSriXPRon/Ev8l96Okc0nnks4l30ElOynZSUn3ku4l3Uu6l+yopHPZjso6l3Uuh1/AV3EX7sUDeBiP4nH8CE/hGRwOy/qW9S3rW/ZZWda1rGtZ17KuZV3L7n9Z17KuZV3L9lfWtaxr2R7LupZ1LetaHvgdruNWWNa17Dko61rWtaxr2V7LupZ1LdtvWdeyrmVdy/Zb1rWsa1nXsp5lPcvuf1nX2x3ndZzXbV6neZ3mdZrXZ96u5mWcl2ternmZ5mWYN3vevHnz5s2aN2PemfPuzLwz57mruidVzqp8VfmquhdVfqr8VLmpclN1l6ueo6r7UOWjykWVi6q9Vzmo6l/Vv2rXVf2rulftuap3Ve+q3lV9q7pWda3qWdWzqmfVHa7qePv1/6JjTceaTDWZarrW5KrJVdO5pnNN55o91vSuyVmzw5qcNTlrPNRkrclak7VmpzX7rNlnjZ+a3DW5a3LXuKpxVZO/xldNh5oONR1qOtT4q+lR47CmR02Pmh41Pmt81vSpcVrTqaZTjduaXjW9anrV9Lrdpa5L3XNY16WuS12Xui51Xeq61Pmu61LXpa5LXZe6LnVd6rrUdanrUvdZUnc/6z5P6jrVdaq7o3V3tG4ndf3q+tX1q7ujdf3q+tX1q+tX16+uX90dretX16+uX533uo51Hes61nWs61jXse55rOtY17GuY13Huo51Hev+jqj7fbmuY0PHhn01dGukfoEX8QrexHv4ANdwM2zo0dCjoUdDj4YeDT0aPlMaOjR0aOjQsKOG7A3ZG7I3ZG/YTUP2htwNuRtyN+RuyN2QuyF3Q+6G3A25G3I35G7I3ZC7YS8N979hFy/YxQucv8DfC1y9wMcLet3+/+upn+FFvII38R6u4Wa4LvO6vOtyrsu47r3rPK97/7pc6zKsy7Bu/rr56+auO3edow2ONsze4GmDpw1eNtzRDT42+NjgYkOeDfvc4GBD1w2fIRt6bui5oeeGnhvu3oZeGz4fN3Ta0OPfnduSveV3gJZ71fKMtHyut3yut3w3tVI/xs/wC7yIV/Am3sMHuIabYct+WvK05Gnp2vI7UUvfllwtuVp21NK9JV9L/5b+Lf1bPvdbdtWSucVFi4uWHbVkb8nekr3ls77FT4uflh21OGpx1NKlZUctHTo6dPjpeEY6npGOLh2fRR2/63b06PgdpuN3mI4uHV06unR06ejSSf0XPsA13Aw73HZ06ejS0aXDb0eXji4dXTq6dPToeF46enT06LhzHdk7vHdk78jekbfDeUfejrwdWTu8d/y+cjvnppybcm5yvulZ3pRtU7ZN2TZl25Rtk+dNuTbl2uR4U65NuTbl2pRpU6ZNeTa53ZRnU55NeTZl2ZRj0+xNsze52jR70+xNnt7S9y293tLnLZnfkvMtWbqydGXpctaVpytPV56uPF15uvJ0vbcrU1emrkxdmboydWXqOrMrV1eurrO7snVl6/LVla8rV9d+u7J1zezK15WvK19Xvq7v6q7vsK7Pt74cfXe0b49997EvU98e+/bY97toX7a+bH3Z+rL1ZevL1petL1tftr5sfdn6svVl68vWl63vc6MvW1+2vmx9d7LPXZ+7vnx996wvV1+evjy3M2zJsCXDlgxbMmyFv8aVcCv1r3gRr+BNvIcPcA03wy33aUuOLTm25NiSY0uOLTm25NiSY0uOLTm2ONqSY0uOLY625NjiaMtnwZYsW7JscfK/4dlUKjyPN/AuruCjVCr1GbyIV3AzlRq4A3fiPuzATuzFATydSg3OYRFN7z+cGghP4JPUwMDncDe+h5+kBgZ3Yx/W/PyZ1Lbwp/g5zuI83sCvcQUf4ZPUNlm2ybDNWdvk2CbHNmduk2WbLNtk2eb8bfJsk2ebPNsGn8AcFnEMa2jiVUSI0XP+M6kRGUbkHZFhRIYRGUa4GJFhRIYRGUZkGEl9EV/DzdTIwGdxB+7E3bgPO7AT38deHEiNyDAiw4gMIzKMyDAiw8jgJcToIUmNDIUYxp+a90xqlMNRmUZlGpVpVKZRmUZlGpVpNPxDalSmUZlGZRpN/QW+hW+nRnkalW1UtlHZRmUblW1UtlGeRmUblW1UrtHBZbQRIU6NyjEqx+hQGhlkkUfh00w5mXIy5WTKyZSTKSdTTqacTDmZcjLlZMrJlJMpJ1NOnpw8OXly8uTkycmTkycnT06enCw5nnI85XjKyZWTKydXbvBGKidbTracbDnZcrLlhooomX04NSbbmGxjso2Fv8K7uIKP8ElqTLYx2cZkG5NtTLYxucbkGpNrTK4xucbkGpNrTK4xucbsa0yOMRnGuLg9Z8KcCXMmOJgwZ8KcCXMmOJgwZ8KcCXMmUn+Ob6Um7GTCrAmzJsyaGPgr7MBOfB97cQBHcDI1ofOEuzHhbkzoPaHv7ZnTZk6bOW3WtDnT5kybM23GtPOnnT/t/Gldpp097dxp5047b9p5086a5nCaw2lnTsu+S/Zd/OziZJd7vUuuXfrvGngWR1K77GGX+7pLll2c75JhVoZZ75313llZZr1/Vp5ZeWblmXXWrDyz8szKMyvPrHNnZZp19qxcs86fdf6s82edPyvjrIyzPM/KOSvnrJyzcs6aOzs0j5pZ51MV8yvmV8yvmF/hvWJ+xfyK+RXzK+ZXzK+YXzG/Yn7F/Ir5FfMr5lfMr7hvFfetYs8VOSpyVOSo6FuRoyJHRY6KHBU5Ku5ARZbK0L+Yez61IMuCLAuyLMiywMWCLAuyLMiwIMOCDAsyLMiwIMOCDAsDf49nUwvmL5i/wMOCe7Zg/oL5CzwsmL9g9oLZC2YvmL3gWVyQ6R/dhyXzl9z1JfOXzF8yf8n8JfOXzF0yd8ncJXOXzF0ya8msJbOWzFpy7pJ5S+Ytmbdk3pJ5S2atmLWi64pZK2atmLXC+4pZK2atmLVi1opZK2asOn/V+avOX3XmqjNXnbnqzFVnrjpzdajhvWdTp5152pmnnXHaGaedcdprT3vtaa897XWnhza85t3UL838pZm/9P5feu9L3vsSxy/52Ut+9pIzXnLGS0PbkUUDG2j5+YnUa97zmve8Zt5r3vOa17zmNa8Nrade897/9H33tte87TVvO/dtd/rtoToan/6/3/j3b5zzW6/5rZ/91r9/79+/97Pf+/f/+ff/hScGBsOzOP/pf//Ef/8kfONTvvL/2CcWDQ=="