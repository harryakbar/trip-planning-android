package com.tripplanner.android.core.i18n

import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageTest {

    @Test
    fun `fromTag resolves known tags`() {
        assertEquals(AppLanguage.English, AppLanguage.fromTag("en"))
        assertEquals(AppLanguage.Indonesian, AppLanguage.fromTag("in"))
    }

    @Test
    fun `fromTag falls back to English for null or unknown`() {
        assertEquals(AppLanguage.English, AppLanguage.fromTag(null))
        assertEquals(AppLanguage.English, AppLanguage.fromTag("fr"))
        assertEquals(AppLanguage.English, AppLanguage.fromTag(""))
    }

    @Test
    fun `indonesian uses the legacy in language code`() {
        // Android resolves res/values-in and Locale("in") for Indonesian.
        assertEquals("in", AppLanguage.Indonesian.tag)
    }
}
