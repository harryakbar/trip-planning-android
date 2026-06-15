package com.tripplanner.android.core.i18n

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Per-app language selection, persisted locally and applied by wrapping the
 * Activity's base context with the chosen locale (works on all supported API
 * levels with a plain ComponentActivity). Changing the language persists the
 * choice and recreates the Activity so Compose `stringResource` picks up the
 * localized resources.
 */
object LocaleManager {
    private const val PREFS = "settings"
    private const val KEY_LANGUAGE = "app_language"

    fun getLanguage(context: Context): AppLanguage =
        AppLanguage.fromTag(prefs(context).getString(KEY_LANGUAGE, null))

    fun setLanguage(context: Context, language: AppLanguage) {
        prefs(context).edit().putString(KEY_LANGUAGE, language.tag).apply()
    }

    /** Returns a context configured to the persisted language. */
    fun wrap(context: Context): Context {
        val locale = Locale(getLanguage(context).tag)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
