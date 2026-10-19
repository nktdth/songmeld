#!/usr/bin/env bash
# Compile real preference and motion policy code with in-memory platform test doubles.
set -euo pipefail
kotlin_distribution=${1:?Usage: bash tools/check_appearance_preferences.sh /path/to/kotlinc}
repo_root=$(cd "$(dirname "$0")/.." && pwd)
check_dir=$(mktemp -d)
trap 'rm -rf "$check_dir"' EXIT
cat > "$check_dir/Context.kt" <<'KOTLIN'
package android.content
class Context {
    val applicationContext get() = this
    private val stores = mutableMapOf<String, Preferences>()
    fun getSharedPreferences(name: String, @Suppress("UNUSED_PARAMETER") mode: Int) = stores.getOrPut(name) { Preferences() }
    companion object { const val MODE_PRIVATE = 0 }
}
class Preferences {
    private val data = mutableMapOf<String, Any>()
    fun getString(key: String, fallback: String?) = data[key] as? String ?: fallback
    fun getBoolean(key: String, fallback: Boolean) = data[key] as? Boolean ?: fallback
    fun getInt(key: String, fallback: Int) = data[key] as? Int ?: fallback
    fun getFloat(key: String, fallback: Float) = data[key] as? Float ?: fallback
    fun contains(key: String) = data.containsKey(key)
    fun edit() = Editor()
    inner class Editor {
        private val changes = mutableMapOf<String, Any?>()
        fun putString(k: String, v: String) = apply { changes[k] = v }
        fun putBoolean(k: String, v: Boolean) = apply { changes[k] = v }
        fun putInt(k: String, v: Int) = apply { changes[k] = v }
        fun putFloat(k: String, v: Float) = apply { changes[k] = v }
        fun remove(k: String) = apply { changes[k] = null }
        fun apply() { changes.forEach { (k, v) -> if (v == null) data.remove(k) else data[k] = v } }
    }
}
KOTLIN
# UI/icon rendering isn't part of this test. These carry the same preference values/defaults.
cat > "$check_dir/Icons.kt" <<'KOTLIN'
package moe.ktandth.songmeld.ui.icons
enum class AppIconStyle(val preferenceValue: String) {
    ROUNDED("rounded"), OUTLINED("outlined"), SHARP("sharp");
    companion object { fun fromPreference(v: String?) = entries.firstOrNull { it.preferenceValue == v } ?: ROUNDED }
}
enum class AppIconShape(val preferenceValue: String) {
    CIRCLE("circle"), SQUARE("square");
    companion object { fun fromPreference(v: String?) = entries.firstOrNull { it.preferenceValue == v } ?: CIRCLE }
}
data class MaterialSymbolAppearance(val style: AppIconStyle = AppIconStyle.ROUNDED, val fill: Float = 0f,
    val weight: Int = 400, val grade: Int = 0, val opticalSize: Int = 24) {
    fun validated() = this
}
KOTLIN
java -cp "$kotlin_distribution/lib/*" org.jetbrains.kotlin.cli.jvm.K2JVMCompiler \
  -kotlin-home "$kotlin_distribution" \
  "$check_dir/Context.kt" "$check_dir/Icons.kt" \
  "$repo_root/app/src/main/java/moe/ktandth/songmeld/ui/icons/IconModels.kt" \
  "$repo_root/app/src/main/java/moe/ktandth/songmeld/ui/settings/AppearancePreferences.kt" \
  "$repo_root/app/src/main/java/moe/ktandth/songmeld/ui/settings/InterfaceOptions.kt" \
  "$repo_root/app/src/main/java/moe/ktandth/songmeld/ui/settings/MotionPolicy.kt" \
  "$repo_root/tools/tests/AppearancePreferencesCheck.kt" -d "$check_dir/checks.jar"
java -cp "$check_dir/checks.jar:$kotlin_distribution/lib/*" moe.ktandth.songmeld.ui.settings.AppearancePreferencesCheckKt
