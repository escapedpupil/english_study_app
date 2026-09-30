package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.LevelDefinitions
import com.example.model.WordRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    assertEquals("萌宝英语岛", appName)
  }

  @Test
  fun `verify level definitions and word repository`() {
    assertTrue(LevelDefinitions.levels.isNotEmpty())
    assertTrue(WordRepository.allWords.isNotEmpty())
    assertEquals(8, LevelDefinitions.levels.size)
  }
}
