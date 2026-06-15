package com.tripplanner.android.core.i18n

/**
 * App-supported UI languages. The [tag] is the Android resource/locale language
 * code — note Indonesian uses the legacy code "in" (matching `res/values-in`),
 * which is how Android resolves Indonesian resources and `Locale("in")`.
 */
enum class AppLanguage(val tag: String, val displayName: String) {
    English("en", "English"),
    Indonesian("in", "Bahasa Indonesia");

    companion object {
        fun fromTag(tag: String?): AppLanguage = entries.firstOrNull { it.tag == tag } ?: English
    }
}
