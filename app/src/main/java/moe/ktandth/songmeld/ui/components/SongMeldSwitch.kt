package moe.ktandth.songmeld.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import moe.ktandth.songmeld.ui.icons.SongMeldIcon
import moe.ktandth.songmeld.ui.icons.SongMeldIconId

/**
 * Shared Material 3 toggle with a check mark in the enabled thumb and
 * a cross in the disabled/off thumb. Semantics are owned by the surrounding
 * toggleable row when onCheckedChange is null.
 */
@Composable
fun SongMeldSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        thumbContent = {
            SongMeldIcon(
                id = if (checked) SongMeldIconId.Check else SongMeldIconId.Close,
                contentDescription = null,
                tint = if (!enabled) {
                    colors.onSurfaceVariant.copy(alpha = 0.38f)
                } else if (checked) {
                    colors.primary
                } else {
                    colors.onSurfaceVariant
                },
                size = SwitchDefaults.IconSize,
            )
        },
    )
}
