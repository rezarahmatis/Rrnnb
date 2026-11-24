package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Pelagia", appName)
  }

  @Test
  fun `catalogs contain expected worlds and species`() {
    assertEquals(38, com.example.model.WorldCatalog.worlds.size)
    assertEquals(74, com.example.model.FishCatalog.species.size)
  }
}
