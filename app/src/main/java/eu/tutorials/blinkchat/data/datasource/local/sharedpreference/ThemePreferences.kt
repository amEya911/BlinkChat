package eu.tutorials.blinkchat.data.datasource.local.sharedpreference

import android.content.Context
import android.content.SharedPreferences
import eu.tutorials.blinkchat.data.event.app.AppTheme

class ThemePreferences(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "theme_preferences"
        private const val KEY_SELECTED_THEME = "selected_theme"
    }

    fun saveTheme(theme: AppTheme) {
        sharedPreferences.edit()
            .putString(KEY_SELECTED_THEME, theme.name)
            .apply()

    }

    fun loadTheme(): AppTheme {
        val themeName = sharedPreferences.getString(KEY_SELECTED_THEME, AppTheme.SYSTEM_DEFAULT.name)
        return AppTheme.valueOf(themeName ?: AppTheme.SYSTEM_DEFAULT.name)
    }
}