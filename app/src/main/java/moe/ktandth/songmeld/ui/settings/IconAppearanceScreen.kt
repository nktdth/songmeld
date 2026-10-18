package moe.ktandth.songmeld.ui.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import moe.ktandth.songmeld.R
import moe.ktandth.songmeld.ui.icons.AppIconShape
import moe.ktandth.songmeld.ui.icons.CatalogIcon
import moe.ktandth.songmeld.ui.icons.IconReference
import moe.ktandth.songmeld.ui.icons.AppIconStyle
import moe.ktandth.songmeld.ui.icons.MaterialSymbolAppearance
import moe.ktandth.songmeld.ui.icons.SongMeldIcon
import moe.ktandth.songmeld.ui.icons.SongMeldIconId
import moe.ktandth.songmeld.ui.icons.iconBackgroundShape

/** Material Symbols Variable Font settings: the live preview uses the same renderer as the app. */
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
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_icon_preview),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 18.dp, top = 8.dp),
        )
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 26.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                listOf(
                    SongMeldIconId.MusicNote,
                    SongMeldIconId.LibraryMusic,
                    SongMeldIconId.Search,
                    SongMeldIconId.Settings,
                ).forEach { icon ->
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = iconBackgroundShape(iconShape),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            SongMeldIcon(
                                id = icon,
                                contentDescription = null,
                                size = 28.dp,
                                appearance = appearance,
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = onBrowseIcons,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.icon_browse_title))
        }
        if (selectedCatalogIcon != null) {
            Row(
                modifier = Modifier.padding(start = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CatalogIcon(
                    reference = selectedCatalogIcon,
                    contentDescription = null,
                    appearance = appearance,
                    size = 30.dp,
                )
                Text(
                    text = stringResource(R.string.icon_saved_selection),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        GroupHeader(
            title = stringResource(R.string.settings_icon_style),
            description = stringResource(R.string.settings_icon_style_description),
        )
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        ) {
            Column {
                AppIconStyle.entries.forEach { style ->
                    IconChoiceRow(
                        title = stringResource(
                            when (style) {
                                AppIconStyle.ROUNDED -> R.string.settings_icon_style_rounded
                                AppIconStyle.OUTLINED -> R.string.settings_icon_style_outlined
                                AppIconStyle.SHARP -> R.string.settings_icon_style_sharp
                            },
                        ),
                        selected = appearance.style == style,
                        onClick = { onAppearanceChanged(appearance.copy(style = style)) },
                    ) {
                        SongMeldIcon(
                            id = SongMeldIconId.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            size = 28.dp,
                            appearance = appearance.copy(style = style),
                        )
                    }
                }
            }
        }

        GroupHeader(
            title = stringResource(R.string.settings_icon_variable_axes),
            description = stringResource(R.string.settings_icon_variable_axes_summary),
        )
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                IconAxisSlider(
                    label = stringResource(R.string.settings_icon_fill),
                    valueLabel = "${(appearance.fill * 100).roundToInt()}%",
                    value = appearance.fill,
                    range = 0f..1f,
                    onChange = { onAppearanceChanged(appearance.copy(fill = it)) },
                )
                IconAxisSlider(
                    label = stringResource(R.string.settings_icon_weight),
                    valueLabel = appearance.weight.toString(),
                    value = appearance.weight.toFloat(),
                    range = 100f..700f,
                    onChange = { onAppearanceChanged(appearance.copy(weight = it.roundToInt())) },
                )
                IconAxisSlider(
                    label = stringResource(R.string.settings_icon_grade),
                    valueLabel = appearance.grade.toString(),
                    value = appearance.grade.toFloat(),
                    range = -50f..200f,
                    onChange = { onAppearanceChanged(appearance.copy(grade = it.roundToInt())) },
                )
                IconAxisSlider(
                    label = stringResource(R.string.settings_icon_optical_size),
                    valueLabel = appearance.opticalSize.toString(),
                    value = appearance.opticalSize.toFloat(),
                    range = 20f..48f,
                    onChange = { onAppearanceChanged(appearance.copy(opticalSize = it.roundToInt())) },
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = {
                onAppearanceChanged(MaterialSymbolAppearance())
                onIconShapeChanged(AppIconShape.CIRCLE)
            }) {
                Text(stringResource(R.string.settings_icon_reset))
            }
        }

        GroupHeader(
            title = stringResource(R.string.settings_icon_shape),
            description = stringResource(R.string.settings_icon_shape_description),
        )
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        ) {
            Column {
                AppIconShape.entries.forEach { shape ->
                    IconChoiceRow(
                        title = stringResource(
                            when (shape) {
                                AppIconShape.CIRCLE -> R.string.settings_icon_shape_circle
                                AppIconShape.ROUNDED_SQUARE -> R.string.settings_icon_shape_rounded
                                AppIconShape.SQUARE -> R.string.settings_icon_shape_square
                            }
                        ),
                        selected = iconShape == shape,
                        onClick = { onIconShapeChanged(shape) },
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(iconBackgroundShape(shape))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            SongMeldIcon(
                                id = SongMeldIconId.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                size = 22.dp,
                                appearance = appearance,
                            )
                        }
                    }
                }
            }
        }
        Text(
            text = stringResource(R.string.settings_icon_font_note),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 18.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun IconAxisSlider(
    label: String,
    valueLabel: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, style = MaterialTheme.typography.titleSmall)
            Text(text = valueLabel, color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge)
        }
        Slider(value = value, onValueChange = onChange, valueRange = range)
    }
}

@Composable
private fun GroupHeader(title: String, description: String) {
    Column(modifier = Modifier.padding(start = 18.dp, end = 18.dp)) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun IconChoiceRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    preview: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(start = 22.dp, end = 18.dp, top = 13.dp, bottom = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) { preview() }
        Text(text = title, modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge)
        RadioButton(selected = selected, onClick = onClick)
    }
}
