package moe.ktandth.songmeld.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import moe.ktandth.songmeld.ui.icons.AppIconShape
import moe.ktandth.songmeld.ui.icons.SongMeldIcon
import moe.ktandth.songmeld.ui.icons.SongMeldIconId
import moe.ktandth.songmeld.ui.icons.iconBackgroundShape

/** A bounded reading column, with one owner for insets and scroll state. */
@Composable
internal fun InformationPage(
    @StringRes title: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit,
) {
    SettingsPageScaffold(title = title, onBack = onBack, modifier = modifier) { padding ->
        Box(
            Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content,
            )
        }
    }
}

internal fun informationGroupShape(index: Int, count: Int) = RoundedCornerShape(
    topStart = if (index == 0) 24.dp else 6.dp,
    topEnd = if (index == 0) 24.dp else 6.dp,
    bottomStart = if (index == count - 1) 24.dp else 6.dp,
    bottomEnd = if (index == count - 1) 24.dp else 6.dp,
)

@Composable
internal fun InformationSectionTitle(title: String, trailing: String? = null) {
    Row(
        Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            title, Modifier.weight(1f).semantics { heading() },
            style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        if (trailing != null) InformationBadge(trailing)
    }
}

@Composable
internal fun InformationBadge(
    text: String,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
) {
    Surface(shape = CircleShape, color = containerColor, contentColor = contentColor) {
        Text(text, Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
internal fun InformationHero(icon: SongMeldIconId, title: String, description: String) {
    Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Surface(
                    shape = iconBackgroundShape(AppIconShape.FLOWER),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) {
                        SongMeldIcon(icon, null, size = 24.dp)
                    }
                }
                Text(title, Modifier.weight(1f).semantics { heading() },
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Text(description, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Static musical wave: decorative only, with no perpetual animation or semantics. */
@Composable
internal fun InformationWave(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary.copy(alpha = 0.32f)
    Canvas(modifier.fillMaxWidth().height(12.dp)) {
        val path = Path()
        val wavelength = 24.dp.toPx()
        val middle = size.height / 2f
        path.moveTo(0f, middle)
        var x = 0f
        while (x < size.width) {
            path.cubicTo(x + wavelength / 4f, 0f, x + wavelength / 4f, size.height, x + wavelength / 2f, middle)
            x += wavelength / 2f
        }
        drawPath(path, color, style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round))
    }
}
