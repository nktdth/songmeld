package moe.ktandth.songmeld.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.ktandth.songmeld.R
import moe.ktandth.songmeld.ui.icons.SongMeldIcon
import moe.ktandth.songmeld.ui.icons.SongMeldIconId
import java.time.format.DateTimeFormatter

/** Only notices actually bundled in this project; not an inferred dependency audit. */
enum class LicenseDocument(
    @get:StringRes val title: Int,
    @get:StringRes val summary: Int,
    val asset: String,
) {
    SONGMELD(R.string.app_name, R.string.about_license_name, "licenses/songmeld-gpl-3.0.txt"),
    MATERIAL(R.string.info_material, R.string.info_apache, "licenses/Apache-2.0-Material-Symbols.txt"),
    FONTS(R.string.info_fonts, R.string.info_ofl, "licenses/OFL-1.1.txt"),
    MDI(R.string.info_mdi, R.string.info_pictogrammers, "licenses/Pictogrammers-Free-License.txt"),
}

@Composable
fun LicensesScreen(onBack: () -> Unit, onOpenLicense: (LicenseDocument) -> Unit) {
    InformationPage(R.string.info_licenses, onBack) {
        item {
            InformationHero(SongMeldIconId.License, stringResource(R.string.info_open_source),
                stringResource(R.string.info_licenses_intro))
        }
        item { InformationSectionTitle(stringResource(R.string.info_project_license)) }
        item { LicenseRow(LicenseDocument.SONGMELD, 0, 1, onOpenLicense) }
        item { InformationSectionTitle(stringResource(R.string.info_bundled_notices)) }
        item {
            val notices = LicenseDocument.entries.filter { it != LicenseDocument.SONGMELD }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                notices.forEachIndexed { index, document -> LicenseRow(document, index, notices.size, onOpenLicense) }
            }
        }
        item {
            Text(stringResource(R.string.info_notices_scope), Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LicenseRow(document: LicenseDocument, index: Int, count: Int, onOpen: (LicenseDocument) -> Unit) {
    val project = document == LicenseDocument.SONGMELD
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = { onOpen(document) }, modifier = Modifier.fillMaxWidth(),
        shape = informationGroupShape(index, count),
        color = if (project) colors.secondaryContainer else colors.surfaceContainerLow,
        contentColor = if (project) colors.onSecondaryContainer else colors.onSurface,
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = CircleShape, color = if (project) colors.surface.copy(alpha = 0.6f) else colors.surfaceContainerHigh) {
                Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    SongMeldIcon(if (project) SongMeldIconId.Code else SongMeldIconId.Description, null, size = 21.dp)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(stringResource(document.title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(stringResource(document.summary), style = MaterialTheme.typography.bodySmall)
            }
            SongMeldIcon(SongMeldIconId.ChevronRight, null, size = 20.dp)
        }
    }
}

@Composable
fun LicenseDetailScreen(document: LicenseDocument, onBack: () -> Unit) {
    val asset = rememberAssetText(document.asset)
    val text = asset.result?.getOrNull()
    val paragraphs = remember(text) { text?.split(Regex("\\n[ \\t]*\\n")).orEmpty() }
    val context = LocalContext.current
    val title = stringResource(document.title)
    InformationPage(R.string.about_license, onBack) {
        item { InformationHero(SongMeldIconId.Description, title, stringResource(document.summary)) }
        if (text == null) {
            item { AssetStatus(asset) }
        } else {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.info_original_document), Modifier.weight(1f).padding(start = 12.dp),
                        style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText(title, text))
                        if (android.os.Build.VERSION.SDK_INT < 33) {
                            Toast.makeText(context, R.string.info_copied, Toast.LENGTH_SHORT).show()
                        }
                    }) { Text(stringResource(R.string.info_copy_document)) }
                }
            }
            // Layout only visible paragraphs. Copy all still uses the exact, unmodified asset.
            itemsIndexed(paragraphs) { _, paragraph ->
                SelectionContainer {
                    Text(paragraph, Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

private data class AssetTextState(val result: Result<String>?, val retry: () -> Unit)

/** Reads stay off the main thread; path changes reset the result before showing it. */
@Composable
private fun rememberAssetText(path: String): AssetTextState {
    val context = LocalContext.current
    var retry by remember(path) { mutableIntStateOf(0) }
    val result by key(path, retry) {
        produceState<Result<String>?>(null, context) {
            value = try {
                Result.success(withContext(Dispatchers.IO) {
                    context.assets.open(path).bufferedReader().use { it.readText() }
                })
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Result.failure(error)
            }
        }
    }
    return AssetTextState(result) { retry++ }
}

@Composable
private fun AssetStatus(asset: AssetTextState, invalid: Boolean = false) {
    if (asset.result == null && !invalid) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
            Text(stringResource(R.string.info_loading), style = MaterialTheme.typography.bodyMedium)
        }
    } else {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.errorContainer) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.info_load_error), color = MaterialTheme.colorScheme.onErrorContainer)
                TextButton(onClick = asset.retry) { Text(stringResource(R.string.retry)) }
            }
        }
    }
}

private val changelogDateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@Composable
fun ChangelogScreen(onBack: () -> Unit) {
    val asset = rememberAssetText("changelog/vi.json")
    val text = asset.result?.getOrNull()
    val releases = remember(text) { text?.let { runCatching { parseChangelog(it) }.getOrNull() } }
    InformationPage(R.string.info_changelog, onBack) {
        item {
            InformationHero(SongMeldIconId.History, stringResource(R.string.info_changelog_heading),
                stringResource(R.string.info_changelog_intro))
        }
        if (releases == null) {
            item { AssetStatus(asset, invalid = text != null) }
        } else {
            itemsIndexed(releases) { index, release -> ChangelogReleaseCard(release, initiallyExpanded = index == 0) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChangelogReleaseCard(release: ChangelogRelease, initiallyExpanded: Boolean) {
    var expanded by rememberSaveable(release.version, release.status) { mutableStateOf(initiallyExpanded) }
    val motion = LocalDecorativeMotionEnabled.current
    val expansionState = stringResource(if (expanded) R.string.info_expanded else R.string.info_collapsed)
    Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(Modifier.fillMaxWidth()) {
            Surface(onClick = { expanded = !expanded }, color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.fillMaxWidth().semantics { stateDescription = expansionState }) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(release.version, Modifier.weight(1f).semantics { heading() },
                            style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        SongMeldIcon(SongMeldIconId.ChevronRight, null, Modifier.rotate(if (expanded) 270f else 90f), size = 24.dp)
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        InformationBadge(stringResource(if (release.status == "development") R.string.about_development else R.string.info_released),
                            MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface)
                        InformationBadge(stringResource(R.string.info_change_count, release.changeCount),
                            MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    release.lastUpdated?.let { date ->
                        Text(stringResource(R.string.info_last_updated, date.format(changelogDateFormat)),
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            AnimatedVisibility(
                visible = expanded,
                enter = if (motion) expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn() else EnterTransition.None,
                exit = if (motion) shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut() else ExitTransition.None,
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    InformationWave()
                    release.groups.forEach { group -> ChangeGroup(group) }
                }
            }
        }
    }
}

@Composable
private fun ChangeGroup(group: ChangelogGroup) {
    val colors = MaterialTheme.colorScheme
    val (title, icon) = when (group.category) {
        ChangeCategory.ADDED -> R.string.changelog_added to SongMeldIconId.Add
        ChangeCategory.IMPROVED -> R.string.changelog_improved to SongMeldIconId.AutoAwesome
        ChangeCategory.FIXED -> R.string.changelog_fixed to SongMeldIconId.Build
        ChangeCategory.REMOVED -> R.string.changelog_removed to SongMeldIconId.Remove
        ChangeCategory.OTHER -> R.string.changelog_other to SongMeldIconId.History
    }
    val (background, foreground) = when (group.category) {
        ChangeCategory.ADDED -> colors.primaryContainer to colors.onPrimaryContainer
        ChangeCategory.IMPROVED -> colors.tertiaryContainer to colors.onTertiaryContainer
        ChangeCategory.FIXED -> colors.secondaryContainer to colors.onSecondaryContainer
        else -> colors.surfaceContainerHighest to colors.onSurface
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(shape = RoundedCornerShape(16.dp), color = background, contentColor = foreground) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SongMeldIcon(icon, null, size = 20.dp)
                Text(stringResource(title), Modifier.weight(1f).semantics { heading() },
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(group.changes.size.toString(), style = MaterialTheme.typography.labelMedium)
            }
        }
        group.changes.forEach { change ->
            Row(Modifier.padding(horizontal = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("•", color = colors.primary, style = MaterialTheme.typography.bodyMedium)
                Text(change, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun TermsScreen(onBack: () -> Unit) {
    val sections = listOf(
        R.string.terms_project_title to R.string.terms_project_body,
        R.string.terms_content_title to R.string.terms_content_body,
        R.string.terms_files_title to R.string.terms_files_body,
        R.string.terms_third_party_title to R.string.terms_third_party_body,
        R.string.terms_warranty_title to R.string.terms_warranty_body,
    )
    InformationPage(R.string.info_terms, onBack) {
        item {
            InformationHero(SongMeldIconId.Description, stringResource(R.string.terms_heading), stringResource(R.string.terms_intro))
        }
        itemsIndexed(sections) { index, (title, body) ->
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        InformationBadge((index + 1).toString().padStart(2, '0'))
                        Text(stringResource(title), Modifier.weight(1f).semantics { heading() },
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                    SelectionContainer {
                        Text(stringResource(body), style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
