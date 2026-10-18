package moe.ktandth.songmeld.ui.settings

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
                description = stringResource(R.string.settings_appearance_summary),
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
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsPageScaffold(
        title = R.string.settings_appearance,
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SettingsSectionTitle(stringResource(R.string.settings_theme))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ThemeTile(
                    title = stringResource(R.string.settings_theme_system),
                    icon = SongMeldIconId.SettingsBrightness,
                    selected = theme == AppTheme.SYSTEM,
                    onClick = { onThemeChanged(AppTheme.SYSTEM) },
                    modifier = Modifier.weight(1f),
                )
                ThemeTile(
                    title = stringResource(R.string.settings_theme_light),
                    icon = SongMeldIconId.LightMode,
                    selected = theme == AppTheme.LIGHT,
                    onClick = { onThemeChanged(AppTheme.LIGHT) },
                    modifier = Modifier.weight(1f),
                )
                ThemeTile(
                    title = stringResource(R.string.settings_theme_dark),
                    icon = SongMeldIconId.DarkMode,
                    selected = theme == AppTheme.DARK,
                    onClick = { onThemeChanged(AppTheme.DARK) },
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                text = stringResource(R.string.settings_theme_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 10.dp, bottom = 4.dp),
            )

            val dynamicAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = dynamicAvailable && dynamicColorEnabled,
                            enabled = dynamicAvailable,
                            role = Role.Switch,
                            onValueChange = onDynamicColorChanged,
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_dynamic_color),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = stringResource(
                                if (dynamicAvailable) R.string.settings_dynamic_color_description
                                else R.string.settings_dynamic_color_unavailable,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Switch(
                        checked = dynamicAvailable && dynamicColorEnabled,
                        onCheckedChange = null, // The entire row is the touch/semantics target.
                        enabled = dynamicAvailable,
                    )
                }
            }

            SettingsSectionTitle(stringResource(R.string.settings_icons))
            ColorfulSettingsEntry(
                title = stringResource(R.string.settings_icon_appearance),
                description = stringResource(R.string.settings_icon_appearance_summary),
                icon = SongMeldIconId.MusicNote,
                accent = settingsAccent(2),
                shape = RoundedCornerShape(28.dp),
                onClick = onOpenIconAppearance,
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Suppress("DEPRECATION")
@Composable
fun AboutSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val version = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
        }.getOrDefault("")
    }
    SettingsPageScaffold(
        title = R.string.settings_about,
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
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SettingsIconCircle(SongMeldIconId.Info, settingsAccent(1))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = stringResource(R.string.settings_about_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (version.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.settings_version, version),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
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
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
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
    Surface(
        modifier = modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = if (selected) colors.primaryContainer else colors.surfaceContainerLow,
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
                tint = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant,
            )
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (selected) colors.onPrimaryContainer else colors.onSurface,
            )
        }
    }
}
