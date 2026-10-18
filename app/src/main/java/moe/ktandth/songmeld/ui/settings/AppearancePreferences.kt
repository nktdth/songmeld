package moe.ktandth.songmeld.ui.settings

import android.content.Context
import moe.ktandth.songmeld.ui.icons.AppIconShape
import moe.ktandth.songmeld.ui.icons.AppIconStyle
import moe.ktandth.songmeld.ui.icons.MaterialSymbolAppearance

/** Theme selection is kept independent from the device theme. */
enum class AppTheme(val preferenceValue: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromPreference(value: String?): AppTheme =
            entries.firstOrNull { it.preferenceValue == value } ?: SYSTEM
    }
}

/** Small persistent settings store. Can be replaced with DataStore as settings grow. */
class AppearancePreferences(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "songmeld_appearance",
        Context.MODE_PRIVATE,
    )

    val theme: AppTheme
        get() = AppTheme.fromPreference(preferences.getString(KEY_THEME, null))

    val dynamicColorEnabled: Boolean
        get() = preferences.getBoolean(KEY_DYNAMIC_COLOR, true)

    val symbols: MaterialSymbolAppearance
        get() {
            val savedStyle = preferences.getString(KEY_ICON_STYLE, null)
            return MaterialSymbolAppearance(
                // Previous builds saved a separate "filled" style. Migrate its appearance
                // into the variable font's FILL axis without resetting user preferences.
                style = AppIconStyle.fromPreference(savedStyle),
                fill = if (preferences.contains(KEY_ICON_FILL)) {
                    preferences.getFloat(KEY_ICON_FILL, 0f)
                } else if (savedStyle == "filled") 1f else 0f,
                weight = preferences.getInt(KEY_ICON_WEIGHT, 400),
                grade = preferences.getInt(KEY_ICON_GRADE, 0),
                opticalSize = preferences.getInt(KEY_ICON_OPTICAL_SIZE, 24),
            ).validated()
        }

    val iconShape: AppIconShape
        get() = AppIconShape.fromPreference(preferences.getString(KEY_ICON_SHAPE, null))

    fun setSymbols(appearance: MaterialSymbolAppearance) {
        val config = appearance.validated()
        preferences.edit()
            .putString(KEY_ICON_STYLE, config.style.preferenceValue)
            .putFloat(KEY_ICON_FILL, config.fill)
            .putInt(KEY_ICON_WEIGHT, config.weight)
            .putInt(KEY_ICON_GRADE, config.grade)
            .putInt(KEY_ICON_OPTICAL_SIZE, config.opticalSize)
            .apply()
    }

    fun setIconShape(shape: AppIconShape) {
        preferences.edit().putString(KEY_ICON_SHAPE, shape.preferenceValue).apply()
    }

    fun setTheme(theme: AppTheme) {
        preferences.edit().putString(KEY_THEME, theme.preferenceValue).apply()
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
    }

    private companion object {
        const val KEY_THEME = "theme"
        const val KEY_DYNAMIC_COLOR = "dynamic_color"
        const val KEY_ICON_STYLE = "icon_style"
        const val KEY_ICON_FILL = "icon_fill"
        const val KEY_ICON_WEIGHT = "icon_weight"
        const val KEY_ICON_GRADE = "icon_grade"
        const val KEY_ICON_OPTICAL_SIZE = "icon_optical_size"
        const val KEY_ICON_SHAPE = "icon_shape"
    }
}
