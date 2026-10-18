package moe.ktandth.songmeld.ui.settings

import android.content.Context
import moe.ktandth.songmeld.ui.icons.AppIconShape
import moe.ktandth.songmeld.ui.icons.AppIconStyle

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

    val iconStyle: AppIconStyle
        get() = AppIconStyle.fromPreference(preferences.getString(KEY_ICON_STYLE, null))

    val iconShape: AppIconShape
        get() = AppIconShape.fromPreference(preferences.getString(KEY_ICON_SHAPE, null))

    fun setIconStyle(style: AppIconStyle) {
        preferences.edit().putString(KEY_ICON_STYLE, style.preferenceValue).apply()
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
        const val KEY_ICON_SHAPE = "icon_shape"
    }
}
