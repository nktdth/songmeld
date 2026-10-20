package moe.ktandth.songmeld.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.ktandth.songmeld.R
import moe.ktandth.songmeld.ui.icons.SongMeldIcon
import moe.ktandth.songmeld.ui.icons.SongMeldIconId
import org.json.JSONObject
import java.time.LocalDate
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
private fun InformationPage(
    @StringRes title: Int,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    SettingsPageScaffold(title = title, onBack = onBack) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)
                .verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
fun LicensesScreen(onBack: () -> Unit, onOpenLicense: (LicenseDocument) -> Unit) {
    InformationPage(R.string.info_licenses, onBack) {
        InformationHeader(SongMeldIconId.License, stringResource(R.string.info_licenses_intro))
        LicenseDocument.entries.forEachIndexed { index, document ->
            if (index == 0) InformationSectionTitle(stringResource(R.string.info_project_license))
            if (index == 1) InformationSectionTitle(stringResource(R.string.info_bundled_notices))
            Surface(
                onClick = { onOpenLicense(document) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(stringResource(document.title), style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)
                        Text(stringResource(document.summary), style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    SongMeldIcon(SongMeldIconId.ChevronRight, null, size = 24.dp)
                }
            }
        }
        Text(stringResource(R.string.info_notices_scope), modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun LicenseDetailScreen(document: LicenseDocument, onBack: () -> Unit) {
    InformationPage(R.string.about_license, onBack) {
        InformationSectionTitle(stringResource(document.title))
        Text(stringResource(document.summary), style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        AssetContent(document.asset) { text ->
            // Bound each text layout when font scaling makes a long license very tall.
            val paragraphs = remember(text) { text.split(Regex("\\n[ \\t]*\\n")) }
            SelectionContainer {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    paragraphs.forEach { paragraph ->
                        Text(paragraph, style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

/** Loads packaged documents off the main thread, with a visible retry state. */
@Composable
private fun AssetContent(path: String, content: @Composable (String) -> Unit) {
    val context = LocalContext.current
    var retry by remember(path) { mutableIntStateOf(0) }
    val result by produceState<Result<String>?>(null, context, path, retry) {
        value = null
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
    val loaded = result
    when {
        loaded == null -> Row(Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
            Text(stringResource(R.string.info_loading), style = MaterialTheme.typography.bodyMedium)
        }
        loaded.isFailure -> {
            Text(stringResource(R.string.info_load_error), color = MaterialTheme.colorScheme.error)
            TextButton(onClick = { retry++ }) { Text(stringResource(R.string.retry)) }
        }
        else -> content(loaded.getOrThrow())
    }
}

private data class ChangelogRelease(
    val version: String,
    val status: String,
    val lastUpdated: LocalDate?,
    val changes: List<String>,
)

private val changelogDateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private fun parseChangelog(text: String): List<ChangelogRelease> {
    val array = JSONObject(text).getJSONArray("releases")
    require(array.length() > 0)
    return List(array.length()) { index ->
        val release = array.getJSONObject(index)
        val changes = release.getJSONArray("changes")
        val status = release.getString("status")
        require(status == "development" || status == "released")
        require(changes.length() > 0)
        ChangelogRelease(
            release.getString("version").also { require(it.isNotBlank()) }, status,
            // Older entries may omit this field; malformed metadata must not hide their changes.
            release.optString("lastUpdated").takeIf { it.isNotBlank() }?.let {
                runCatching { LocalDate.parse(it) }.getOrNull()
            },
            List(changes.length()) { changes.getString(it).also { change -> require(change.isNotBlank()) } },
        )
    }
}

@Composable
fun ChangelogScreen(onBack: () -> Unit) {
    InformationPage(R.string.info_changelog, onBack) {
        InformationHeader(SongMeldIconId.History, stringResource(R.string.info_changelog_intro))
        AssetContent("changelog/vi.json") { json ->
            val releases = remember(json) { runCatching { parseChangelog(json) }.getOrNull() }
            if (releases == null) {
                Text(stringResource(R.string.info_load_error), color = MaterialTheme.colorScheme.error)
            } else {
                releases.forEach { release ->
                    Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
                        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text(release.version, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer) {
                                Text(stringResource(if (release.status == "development") R.string.about_development else R.string.info_released),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium)
                            }
                            release.lastUpdated?.let { date ->
                                Text(
                                    stringResource(R.string.info_last_updated, date.format(changelogDateFormat)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            release.changes.forEach { change ->
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("•", color = MaterialTheme.colorScheme.primary)
                                    Text(change, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
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
        InformationHeader(SongMeldIconId.Description, stringResource(R.string.terms_intro))
        sections.forEach { (title, body) ->
            Surface(shape = RoundedCornerShape(26.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(stringResource(body), style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun InformationHeader(icon: SongMeldIconId, description: String) {
    Column(Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer) {
            Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                SongMeldIcon(icon, null, size = 30.dp)
            }
        }
        Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun InformationSectionTitle(title: String) {
    Text(title, modifier = Modifier.padding(start = 12.dp, top = 8.dp),
        style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold)
}
