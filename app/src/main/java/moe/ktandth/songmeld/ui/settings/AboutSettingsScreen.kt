package moe.ktandth.songmeld.ui.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import moe.ktandth.songmeld.R
import moe.ktandth.songmeld.ui.icons.SongMeldIcon
import moe.ktandth.songmeld.ui.icons.SongMeldIconId

private const val REPOSITORY = "https://github.com/nktdth/songmeld"
private const val PEOPLE_PREVIEW_LIMIT = 5

@Suppress("DEPRECATION")
@Composable
fun AboutSettingsScreen(
    onBack: () -> Unit,
    onOpenLicenses: () -> Unit,
    onOpenChangelog: () -> Unit,
    onOpenTerms: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val version = remember(context) {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
    }
    InformationPage(R.string.settings_about, onBack, modifier) {
        item { AboutHero(version) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AboutLinkRow(
                    stringResource(R.string.info_changelog), stringResource(R.string.info_changelog_summary),
                    SongMeldIconId.History, informationGroupShape(0, 3), onOpenChangelog,
                )
                AboutLinkRow(
                    stringResource(R.string.info_licenses), stringResource(R.string.info_licenses_summary),
                    SongMeldIconId.License, informationGroupShape(1, 3), onOpenLicenses,
                )
                AboutLinkRow(
                    stringResource(R.string.info_terms), stringResource(R.string.info_terms_summary),
                    SongMeldIconId.Description, informationGroupShape(2, 3), onOpenTerms,
                )
            }
        }
        item {
            AboutPeopleSection(
                title = stringResource(R.string.about_developers), people = AboutCredits.developers,
                emptyText = stringResource(R.string.about_developers_empty),
            )
        }
        item {
            AboutPeopleSection(
                title = stringResource(R.string.about_contributors), people = AboutCredits.contributors,
                emptyText = stringResource(R.string.about_contributors_empty),
                description = stringResource(R.string.about_contributors_summary),
            )
        }
        item { InformationSectionTitle(stringResource(R.string.about_project_section)) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AboutLinkRow(
                    stringResource(R.string.about_source), stringResource(R.string.about_source_summary),
                    SongMeldIconId.Code, informationGroupShape(0, 2),
                    onClick = { openAboutLink(context, REPOSITORY) },
                )
                AboutLinkRow(
                    stringResource(R.string.about_contribute), stringResource(R.string.about_contribute_summary),
                    SongMeldIconId.Volunteer, informationGroupShape(1, 2),
                    onClick = { openAboutLink(context, "$REPOSITORY/issues") },
                )
            }
        }
        item {
            Text(stringResource(R.string.about_footer), Modifier.fillMaxWidth().padding(vertical = 12.dp),
                style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AboutHero(version: String) {
    val colors = MaterialTheme.colorScheme
    Surface(shape = RoundedCornerShape(32.dp), color = colors.surfaceContainerLow) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(shape = RoundedCornerShape(24.dp), color = colors.primaryContainer) {
                    Image(painterResource(R.drawable.songmeld_logo), contentDescription = null,
                        modifier = Modifier.size(72.dp).padding(10.dp))
                }
                Text(
                    buildAnnotatedString {
                        append("Song")
                        withStyle(SpanStyle(color = colors.primary)) { append("Meld") }
                    }, modifier = Modifier.weight(1f).semantics { heading() },
                    style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold,
                )
            }
            Text(stringResource(R.string.about_description), style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InformationBadge(stringResource(R.string.settings_version, version))
                InformationBadge(stringResource(R.string.about_development), colors.tertiaryContainer, colors.onTertiaryContainer)
            }
            InformationWave()
        }
    }
}

@Composable
private fun AboutLinkRow(
    title: String, summary: String, icon: SongMeldIconId,
    shape: Shape, onClick: () -> Unit,
) {
    val accent = when (icon) {
        SongMeldIconId.License, SongMeldIconId.Description -> settingsAccent(2)
        SongMeldIconId.History -> settingsAccent(1)
        else -> settingsAccent(0)
    }
    Surface(onClick = onClick, shape = shape, color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Row(Modifier.fillMaxWidth().heightIn(min = 72.dp).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = CircleShape, color = accent.background, contentColor = accent.foreground) {
                Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) { SongMeldIcon(icon, null, size = 21.dp) }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            SongMeldIcon(SongMeldIconId.ChevronRight, null, size = 20.dp)
        }
    }
}

@Composable
internal fun AboutPeopleSection(
    title: String, people: List<AboutPerson>, emptyText: String, description: String? = null,
) {
    var showAll by rememberSaveable { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        InformationSectionTitle(title, people.size.takeIf { it > 0 }?.toString())
        if (description != null) {
            Text(description, Modifier.padding(horizontal = 12.dp),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
            Column(Modifier.fillMaxWidth()) {
                if (people.isEmpty()) {
                    Text(emptyText, Modifier.padding(20.dp), style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    people.take(PEOPLE_PREVIEW_LIMIT).forEachIndexed { index, person ->
                        if (index > 0) HorizontalDivider(Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        // The first person is prominent without introducing another role or section.
                        AboutPersonRow(person, featured = index == 0)
                    }
                    if (people.size > PEOPLE_PREVIEW_LIMIT) {
                        TextButton(onClick = { showAll = true }, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                            Text(stringResource(R.string.about_people_view_all, people.size))
                        }
                    }
                }
            }
        }
    }
    if (showAll && people.size > PEOPLE_PREVIEW_LIMIT) {
        AboutPeopleSheet(title, people, onDismiss = { showAll = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutPeopleSheet(title: String, people: List<AboutPerson>, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        // The list receives the remaining sheet height; header and close action stay reachable.
        Column(Modifier.fillMaxWidth().fillMaxHeight(0.85f)) {
            Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(title, Modifier.weight(1f).semantics { heading() },
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                InformationBadge(people.size.toString())
                IconButton(onClick = onDismiss) {
                    SongMeldIcon(SongMeldIconId.Close, stringResource(R.string.about_close))
                }
            }
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 20.dp)) {
                itemsIndexed(people) { index, person ->
                    if (index > 0) HorizontalDivider(Modifier.padding(horizontal = 24.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    AboutPersonRow(person, featured = index == 0)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AboutPersonRow(person: AboutPerson, featured: Boolean) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    Surface(color = if (featured) colors.secondaryContainer.copy(alpha = 0.45f) else colors.surfaceContainerLow) {
        Column(Modifier.fillMaxWidth().padding(if (featured) 20.dp else 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val avatarSize = if (featured) 64.dp else 40.dp
                if (person.avatar != null) {
                    Image(painterResource(person.avatar), contentDescription = null,
                        modifier = Modifier.size(avatarSize).clip(if (featured) RoundedCornerShape(22.dp) else CircleShape),
                        contentScale = ContentScale.Crop)
                } else {
                    Surface(shape = CircleShape, color = colors.primaryContainer, contentColor = colors.onPrimaryContainer) {
                        Box(Modifier.size(avatarSize), contentAlignment = Alignment.Center) {
                            Text(person.name.take(1).uppercase(),
                                style = if (featured) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium)
                        }
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(person.name, style = if (featured) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                        fontWeight = if (featured) FontWeight.Bold else FontWeight.SemiBold)
                    person.displayName?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                    if (!featured) Text(stringResource(person.contribution),
                        style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
            if (featured) Text(stringResource(person.contribution),
                style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            if (person.githubUrl != null || person.email != null) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    person.githubUrl?.let { url ->
                        TextButton(onClick = { openAboutLink(context, url) }) { Text(stringResource(R.string.about_github)) }
                    }
                    person.email?.let { email ->
                        TextButton(onClick = { openAboutLink(context, "mailto:$email") }) { Text(stringResource(R.string.about_contact)) }
                    }
                }
            }
        }
    }
}

private fun openAboutLink(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, R.string.about_link_unavailable, Toast.LENGTH_SHORT).show()
    }
}
