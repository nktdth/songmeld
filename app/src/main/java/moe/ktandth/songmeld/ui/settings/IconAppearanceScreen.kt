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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import moe.ktandth.songmeld.R
import moe.ktandth.songmeld.ui.icons.AppIconShape
import moe.ktandth.songmeld.ui.icons.AppIconStyle
import moe.ktandth.songmeld.ui.icons.LocalSongMeldIconStyle
import moe.ktandth.songmeld.ui.icons.SongMeldIconId
import moe.ktandth.songmeld.ui.icons.iconBackgroundShape
import moe.ktandth.songmeld.ui.icons.songMeldIcon

/** First usable appearance page. These are real static icon presets, not variable-font sliders. */
@Composable
fun IconAppearanceScreen(
    iconStyle: AppIconStyle,
    iconShape: AppIconShape,
    onIconStyleChanged: (AppIconStyle) -> Unit,
    onIconShapeChanged: (AppIconShape) -> Unit,
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
                            Icon(
                                imageVector = songMeldIcon(icon),
                                contentDescription = null,
                                modifier = Modifier.size(27.dp),
                            )
                        }
                    }
                }
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
                                AppIconStyle.FILLED -> R.string.settings_icon_style_filled
                            }
                        ),
                        selected = iconStyle == style,
                        onClick = { onIconStyleChanged(style) },
                    ) {
                        CompositionLocalProvider(LocalSongMeldIconStyle provides style) {
                            Icon(
                                imageVector = songMeldIcon(SongMeldIconId.MusicNote),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                    }
                }
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
                            Icon(
                                imageVector = songMeldIcon(SongMeldIconId.MusicNote),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                }
            }
        }
        Text(
            text = stringResource(R.string.settings_icon_future_note),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 18.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
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
        Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            preview()
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
        )
        RadioButton(selected = selected, onClick = onClick)
    }
}
