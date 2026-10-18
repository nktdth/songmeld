package moe.ktandth.songmeld.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import moe.ktandth.songmeld.R
import moe.ktandth.songmeld.ui.icons.AppIconShape
import moe.ktandth.songmeld.ui.icons.AppIconStyle
import moe.ktandth.songmeld.ui.icons.CatalogIcon
import moe.ktandth.songmeld.ui.icons.IconReference
import moe.ktandth.songmeld.ui.icons.MaterialSymbolAppearance
import moe.ktandth.songmeld.ui.icons.SongMeldIcon
import moe.ktandth.songmeld.ui.icons.SongMeldIconId
import moe.ktandth.songmeld.ui.icons.iconBackgroundShape

/** Compact icon customization, inspired by Paisa. All controls affect the real variable font. */
@Composable
fun IconAppearanceScreen(
    appearance: MaterialSymbolAppearance,
    iconShape: AppIconShape,
    onAppearanceChanged: (MaterialSymbolAppearance) -> Unit,
    onIconShapeChanged: (AppIconShape) -> Unit,
    onBrowseIcons: () -> Unit,
    selectedCatalogIcon: IconReference?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Unframed symbol alongside a symbol on the currently selected background shape.
            SongMeldIcon(
                id = SongMeldIconId.MusicNote,
                contentDescription = null,
                appearance = appearance,
                size = 58.dp,
                tint = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.size(24.dp))
            Box(
                modifier = (if (iconShape == AppIconShape.PILL) Modifier.size(width = 96.dp, height = 72.dp)
                    else Modifier.size(82.dp)).clip(iconBackgroundShape(iconShape))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                SongMeldIcon(
                    id = SongMeldIconId.MusicNote,
                    contentDescription = null,
                    size = 40.dp,
                    appearance = appearance,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        SettingsIconGroupTitle(stringResource(R.string.settings_icon_style))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            AppIconStyle.entries.forEachIndexed { index, style ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index, AppIconStyle.entries.size),
                    selected = appearance.style == style,
                    onClick = { onAppearanceChanged(appearance.copy(style = style)) },
                    label = {
                        Text(
                            stringResource(
                                when (style) {
                                    AppIconStyle.ROUNDED -> R.string.settings_icon_style_rounded
                                    AppIconStyle.OUTLINED -> R.string.settings_icon_style_outlined
                                    AppIconStyle.SHARP -> R.string.settings_icon_style_sharp
                                }
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                )
            }
        }

        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().selectable(
                        selected = appearance.fill >= 0.5f,
                        role = Role.Switch,
                        onClick = {
                            onAppearanceChanged(appearance.copy(fill = if (appearance.fill >= 0.5f) 0f else 1f))
                        },
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.settings_icon_fill), style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(R.string.settings_icon_fill_summary),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(checked = appearance.fill >= 0.5f, onCheckedChange = null)
                }
                SafeIconSlider(
                    title = stringResource(R.string.settings_icon_weight),
                    value = appearance.weight.toFloat(),
                    min = 300f, max = 600f, steps = 5,
                    onValueChange = { onAppearanceChanged(appearance.copy(weight = (it / 50).roundToInt() * 50)) },
                )
                SafeIconSlider(
                    title = stringResource(R.string.settings_icon_optical_size),
                    value = appearance.opticalSize.toFloat(),
                    min = 20f, max = 40f, steps = 4,
                    onValueChange = { onAppearanceChanged(appearance.copy(opticalSize = (it / 4).roundToInt() * 4)) },
                )
                SafeIconSlider(
                    title = stringResource(R.string.settings_icon_grade),
                    value = appearance.grade.toFloat(),
                    min = -25f, max = 75f, steps = 3,
                    onValueChange = { onAppearanceChanged(appearance.copy(grade = (it / 25).roundToInt() * 25)) },
                )
            }
        }
        Text(
            text = stringResource(R.string.settings_icon_safe_range),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp),
        )

        SettingsIconGroupTitle(stringResource(R.string.settings_icon_shape))
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppIconShape.entries.chunked(4).forEach { rowShapes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        rowShapes.forEach { shape ->
                            val selected = shape == iconShape
                            val label = stringResource(shapeLabel(shape))
                            Box(
                                modifier = Modifier.weight(1f).height(68.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(if (selected) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceContainerLow)
                                    .selectable(selected = selected, role = Role.RadioButton) {
                                        onIconShapeChanged(shape)
                                    }
                                    .semantics { contentDescription = label },
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier = (if (shape == AppIconShape.PILL) Modifier.size(width = 52.dp, height = 32.dp)
                                        else Modifier.size(47.dp))
                                        .clip(iconBackgroundShape(shape))
                                        .background(if (selected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.tertiary),
                                )
                            }
                        }
                        repeat(4 - rowShapes.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
        Text(
            text = stringResource(R.string.settings_icon_shape_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp),
        )

        Button(onClick = onBrowseIcons, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.icon_browse_title))
        }
        if (selectedCatalogIcon != null) {
            Row(modifier = Modifier.padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CatalogIcon(reference = selectedCatalogIcon, contentDescription = null,
                    appearance = appearance, size = 26.dp)
                Text(stringResource(R.string.icon_saved_selection),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall)
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = {
                onAppearanceChanged(MaterialSymbolAppearance())
                onIconShapeChanged(AppIconShape.CIRCLE)
            }) { Text(stringResource(R.string.settings_icon_reset)) }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun SafeIconSlider(
    title: String,
    value: Float,
    min: Float,
    max: Float,
    steps: Int,
    onValueChange: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(value.roundToInt().toString(), style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary)
        }
        Slider(value = value.coerceIn(min, max), onValueChange = onValueChange,
            valueRange = min..max, steps = steps)
    }
}

@Composable
private fun SettingsIconGroupTitle(title: String) {
    Text(title, modifier = Modifier.padding(start = 12.dp, top = 4.dp),
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.titleSmall)
}

private fun shapeLabel(shape: AppIconShape): Int = when (shape) {
    AppIconShape.CIRCLE -> R.string.settings_icon_shape_circle
    AppIconShape.ROUNDED_SQUARE -> R.string.settings_icon_shape_rounded
    AppIconShape.SQUARE -> R.string.settings_icon_shape_square
    AppIconShape.SQUIRCLE -> R.string.settings_icon_shape_squircle
    AppIconShape.PILL -> R.string.settings_icon_shape_pill
    AppIconShape.HEXAGON -> R.string.settings_icon_shape_hexagon
    AppIconShape.PENTAGON -> R.string.settings_icon_shape_pentagon
    AppIconShape.TRIANGLE -> R.string.settings_icon_shape_triangle
    AppIconShape.DIAMOND -> R.string.settings_icon_shape_diamond
    AppIconShape.STAR -> R.string.settings_icon_shape_star
    AppIconShape.FLOWER -> R.string.settings_icon_shape_flower
    AppIconShape.CLOVER -> R.string.settings_icon_shape_clover
}
