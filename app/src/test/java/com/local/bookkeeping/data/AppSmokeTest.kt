package com.local.bookkeeping.data

import android.content.Intent
import com.local.bookkeeping.MainActivity
import com.local.bookkeeping.LedgerApplication
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = LedgerApplication::class)
class AppSmokeTest {
 @Test fun appLaunchesWithoutLoginOrCachedRate() {
  Robolectric.buildActivity(MainActivity::class.java, Intent(Intent.ACTION_MAIN)).use { controller ->
   val activity = controller.setup().get()
   assertFalse(activity.isFinishing)
   assertNotNull(activity.window.decorView)
  }
 }
}
