package moe.ktandth.songmeld.ui.settings

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.snap
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import moe.ktandth.songmeld.R
import moe.ktandth.songmeld.ui.icons.SongMeldIcon
import moe.ktandth.songmeld.ui.icons.SongMeldIconId

/**
 * Reusable Material 3 top app bar for Settings and its child pages.
 * The Scaffold owns both the toolbar and the scrollable content to avoid double status-bar insets.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPageScaffold(
    title: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text = stringResource(title),
                        fontWeight = FontWeight.Bold,
                        fontSize = if (title == R.string.settings_title) {
                            (30f - 8f * scrollBehavior.state.collapsedFraction).sp
                        } else {
                            (26f - 5f * scrollBehavior.state.collapsedFraction).sp
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        SongMeldIcon(
                            id = SongMeldIconId.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back),
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        content = content,
    )
}

/** Only the two entry points appear on the Settings overview. */
@Composable
fun SettingsScreen(
    theme: AppTheme,
    dynamicColorEnabled: Boolean,
    onBack: () -> Unit,
    onOpenAppearance: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsPageScaffold(
        title = R.string.settings_title,
        onBack = onBack,
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            // Rounded grouped surfaces and bright circular icon containers are inspired by v1.
            ColorfulSettingsEntry(
                title = stringResource(R.string.settings_appearance),
                description = stringResource(
                    R.string.settings_appearance_current,
                    stringResource(themeLabel(theme)),
                    stringResource(if (dynamicColorEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        R.string.settings_palette_dynamic else R.string.settings_palette_warm),
                ),
                icon = SongMeldIconId.Palette,
                accent = settingsAccent(0),
                shape = RoundedCornerShape(
                    topStart = 28.dp, topEnd = 28.dp,
                    bottomStart = 10.dp, bottomEnd = 10.dp,
                ),
                onClick = onOpenAppearance,
            )
            ColorfulSettingsEntry(
                title = stringResource(R.string.settings_about),
                description = stringResource(R.string.settings_about_description),
                icon = SongMeldIconId.Info,
                accent = settingsAccent(1),
                shape = RoundedCornerShape(
                    topStart = 10.dp, topEnd = 10.dp,
                    bottomStart = 28.dp, bottomEnd = 28.dp,
                ),
                onClick = onOpenAbout,
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun AppearanceSettingsScreen(
    theme: AppTheme,
    dynamicColorEnabled: Boolean,
    onThemeChanged: (AppTheme) -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit,
    onOpenIconAppearance: () -> Unit,
    onInterfaceOptionsChanged: (InterfaceOptions) -> Unit,
    onResetAppearance: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = LocalInterfaceOptions.current
    var showReset by rememberSaveable { mutableStateOf(false) }
    SettingsPageScaffold(title = R.string.settings_appearance, onBack = onBack, modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).consumeWindowInsets(innerPadding)
                .verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppearancePreview(options.compactLibrary)
            SettingsSectionTitle(stringResource(R.string.settings_theme))
            ThemeChoices(theme, onThemeChanged)
            Text(stringResource(R.string.settings_theme_description),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp))
            val dynamicAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            SettingsToggle(
                title = stringResource(R.string.settings_dynamic_color),
                description = stringResource(if (dynamicAvailable) R.string.settings_dynamic_color_description
                    else R.string.settings_dynamic_color_unavailable),
                checked = dynamicAvailable && dynamicColorEnabled,
                enabled = dynamicAvailable,
                onCheckedChange = onDynamicColorChanged,
            )
            SettingsSectionTitle(stringResource(R.string.settings_icons))
            ColorfulSettingsEntry(
                title = stringResource(R.string.settings_icon_appearance),
                description = stringResource(R.string.settings_icon_appearance_summary),
                icon = SongMeldIconId.MusicNote, accent = settingsAccent(2),
                shape = RoundedCornerShape(28.dp), onClick = onOpenIconAppearance,
            )
            SettingsSectionTitle(stringResource(R.string.settings_library_display))
            SettingsToggle(
                title = stringResource(R.string.settings_compact_library),
                description = stringResource(R.string.settings_compact_library_summary),
                checked = options.compactLibrary,
                onCheckedChange = { onInterfaceOptionsChanged(options.copy(compactLibrary = it)) },
            )
            SettingsSectionTitle(stringResource(R.string.settings_motion))
            SettingsToggle(
                title = stringResource(R.string.settings_reduce_motion),
                description = stringResource(R.string.settings_reduce_motion_summary),
                checked = options.reduceMotion,
                onCheckedChange = { onInterfaceOptionsChanged(options.copy(reduceMotion = it)) },
            )
            TextButton(onClick = { showReset = true }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.settings_reset_appearance))
            }
            Spacer(Modifier.height(12.dp))
        }
    }
    if (showReset) {
        AlertDialog(
            onDismissRequest = { showReset = false },
            title = { Text(stringResource(R.string.settings_reset_appearance)) },
            text = { Text(stringResource(R.string.settings_reset_appearance_body)) },
            confirmButton = { TextButton(onClick = { onResetAppearance(); showReset = false }) {
                Text(stringResource(R.string.settings_reset_confirm))
            } },
            dismissButton = { TextButton(onClick = { showReset = false }) {
                Text(stringResource(R.string.settings_cancel))
            } },
        )
    }
}

@Composable
private fun SettingsToggle(
    title: String, description: String, checked: Boolean,
    enabled: Boolean = true, onCheckedChange: (Boolean) -> Unit,
) {
    val motion = LocalDecorativeMotionEnabled.current
    Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .animateContentSize(animationSpec = if (motion) tween(200) else snap())
                .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(description, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = null, enabled = enabled)
        }
    }
}

@Composable
private fun ThemeChoices(selected: AppTheme, onSelected: (AppTheme) -> Unit) {
    val largeText = LocalDensity.current.fontScale > 1.3f
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        if (maxWidth < 300.dp || largeText) {
            Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTheme.entries.forEach { theme ->
                    ThemeTile(stringResource(themeLabel(theme)), themeIcon(theme), theme == selected,
                        { onSelected(theme) }, Modifier.fillMaxWidth())
                }
            }
        } else {
            Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTheme.entries.forEach { theme ->
                    ThemeTile(stringResource(themeLabel(theme)), themeIcon(theme), theme == selected,
                        { onSelected(theme) }, Modifier.weight(1f))
                }
            }
        }
    }
}

private fun themeLabel(theme: AppTheme): Int = when (theme) {
    AppTheme.SYSTEM -> R.string.settings_theme_system
    AppTheme.LIGHT -> R.string.settings_theme_light
    AppTheme.DARK -> R.string.settings_theme_dark
}

private fun themeIcon(theme: AppTheme): SongMeldIconId = when (theme) {
    AppTheme.SYSTEM -> SongMeldIconId.SettingsBrightness
    AppTheme.LIGHT -> SongMeldIconId.LightMode
    AppTheme.DARK -> SongMeldIconId.DarkMode
}

private data class SettingsAccent(val background: Color, val foreground: Color)

/** Inspired by SongMeld v1's colorful settings icons, with light/dark aware palettes. */
@Composable
private fun settingsAccent(index: Int): SettingsAccent {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return when (index) {
        0 -> if (dark) SettingsAccent(Color(0xFFE7A2BC), Color(0xFF431127))
             else SettingsAccent(Color(0xFFFFD6E6), Color(0xFF7A173F))
        1 -> if (dark) SettingsAccent(Color(0xFF98D2CD), Color(0xFF103C39))
             else SettingsAccent(Color(0xFFC9F0EC), Color(0xFF175B57))
        else -> if (dark) SettingsAccent(Color(0xFFE8C184), Color(0xFF3D2906))
                else SettingsAccent(Color(0xFFFFE0AA), Color(0xFF6A4300))
    }
}

@Composable
private fun SettingsIconCircle(icon: SongMeldIconId, accent: SettingsAccent) {
    Surface(
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        color = accent.background,
        contentColor = accent.foreground,
    ) {
        Box(contentAlignment = Alignment.Center) {
            SongMeldIcon(id = icon, contentDescription = null, size = 22.dp)
        }
    }
}

@Composable
private fun ColorfulSettingsEntry(
    title: String,
    description: String,
    icon: SongMeldIconId,
    accent: SettingsAccent,
    shape: RoundedCornerShape,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SettingsIconCircle(icon, accent)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            SongMeldIcon(
                id = SongMeldIconId.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                size = 20.dp,
            )
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 18.dp, top = 8.dp, bottom = 2.dp),
    )
}

/** Compact, fully clickable M3 theme tile inspired by SongMeld v1. */
@Composable
private fun ThemeTile(
    title: String,
    icon: SongMeldIconId,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val motion = LocalDecorativeMotionEnabled.current
    val tileColor by animateColorAsState(
        if (selected) colors.primaryContainer else colors.surfaceContainerLow,
        animationSpec = if (motion) tween(220) else snap(), label = "Theme tile color",
    )
    val iconScale by animateFloatAsState(
        if (selected) 1.08f else 1f,
        animationSpec = if (motion) tween(220) else snap(), label = "Theme icon emphasis",
    )
    Surface(
        modifier = modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = tileColor,
        border = androidx.compose.foundation.BorderStroke(
            if (selected) 1.5.dp else 0.dp,
            if (selected) colors.primary else Color.Transparent,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SongMeldIcon(
                id = icon,
                contentDescription = null,
                size = 29.dp,
                modifier = Modifier.graphicsLayer { scaleX = iconScale; scaleY = iconScale },
                tint = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant,
            )
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = if (selected) colors.onPrimaryContainer else colors.onSurface,
            )
        }
    }
}
