package moe.ktandth.songmeld.ui.settings

import android.content.Context
import moe.ktandth.songmeld.ui.icons.*

fun main() {
    val context = Context()
    val original = AppearancePreferences(context)
    check(original.interfaceOptions == InterfaceOptions())
    original.setInterfaceOptions(InterfaceOptions(reduceMotion = true, compactLibrary = true))
    original.setTheme(AppTheme.DARK)
    original.setDynamicColorEnabled(false)
    original.setSymbols(MaterialSymbolAppearance(style = AppIconStyle.SHARP, fill = 1f, weight = 600))
    original.setIconShape(AppIconShape.SQUARE)
    original.setIconPickerSelection(IconReference(IconPack.SYMBOLS, 0xE405))
    val reopened = AppearancePreferences(context)
    check(reopened.interfaceOptions == InterfaceOptions(true, true))
    check(reopened.theme == AppTheme.DARK && !reopened.dynamicColorEnabled)
    check(reopened.symbols.style == AppIconStyle.SHARP && reopened.symbols.fill == 1f)
    reopened.resetVisualAppearance()
    val reset = AppearancePreferences(context)
    check(reset.theme == AppTheme.SYSTEM && reset.dynamicColorEnabled)
    check(reset.symbols == MaterialSymbolAppearance() && reset.iconShape == AppIconShape.CIRCLE)
    check(reset.interfaceOptions == InterfaceOptions(reduceMotion = true, compactLibrary = false))
    check(reset.iconPickerSelection?.codepoint == 0xE405)
    reset.setInterfaceOptions(reset.interfaceOptions.copy(compactLibrary = true))
    check(AppearancePreferences(context).interfaceOptions.reduceMotion)
    check(AppTheme.fromPreference("obsolete") == AppTheme.SYSTEM)
    check(decorativeMotionEnabled(reduceMotion = false, systemAnimationsEnabled = true))
    check(!decorativeMotionEnabled(reduceMotion = true, systemAnimationsEnabled = true))
    check(!decorativeMotionEnabled(reduceMotion = false, systemAnimationsEnabled = false))
    context.getSharedPreferences("songmeld_appearance", Context.MODE_PRIVATE).edit()
        .putInt("selected_icon_codepoint", Int.MAX_VALUE).apply()
    check(AppearancePreferences(context).iconPickerSelection == null)
    println("Appearance preference checks passed: persistence, reset scope, accessibility retention and motion precedence.")
}
