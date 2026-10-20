package moe.ktandth.songmeld.ui.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
    SettingsPageScaffold(title = R.string.settings_about, onBack = onBack, modifier = modifier) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)
                .verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AboutHero(version)
            AboutSectionTitle(stringResource(R.string.about_app_section))
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                AboutLinkRow(
                    title = stringResource(R.string.about_license),
                    summary = stringResource(R.string.info_licenses_summary),
                    icon = SongMeldIconId.License,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 6.dp, bottomEnd = 6.dp),
                    onClick = onOpenLicenses,
                )
                AboutLinkRow(
                    title = stringResource(R.string.about_source),
                    summary = stringResource(R.string.about_source_summary),
                    icon = SongMeldIconId.Code,
                    shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 28.dp, bottomEnd = 28.dp),
                    onClick = { openAboutLink(context, REPOSITORY) },
                )
            }
            AboutLinkRow(
                title = stringResource(R.string.info_changelog),
                summary = stringResource(R.string.info_changelog_summary),
                icon = SongMeldIconId.History,
                onClick = onOpenChangelog,
            )
            AboutLinkRow(
                title = stringResource(R.string.info_terms),
                summary = stringResource(R.string.info_terms_summary),
                icon = SongMeldIconId.Description,
                onClick = onOpenTerms,
            )
            AboutPeopleSection(stringResource(R.string.about_primary_developers), AboutCredits.primaryDevelopers)
            AboutPeopleSection(
                stringResource(R.string.about_secondary_developers), AboutCredits.secondaryDevelopers,
                emptyText = stringResource(R.string.about_secondary_empty),
            )
            AboutPeopleSection(
                stringResource(R.string.about_contributors), AboutCredits.contributors,
                emptyText = stringResource(R.string.about_contributors_empty),
                description = stringResource(R.string.about_contributors_summary),
            )
            AboutLinkRow(
                title = stringResource(R.string.about_contribute),
                summary = stringResource(R.string.about_contribute_summary),
                icon = SongMeldIconId.Volunteer,
                onClick = { openAboutLink(context, REPOSITORY) },
            )
            Text(
                stringResource(R.string.about_footer), modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AboutHero(version: String) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(shape = RoundedCornerShape(30.dp), color = colors.primaryContainer) {
            Image(painterResource(R.drawable.songmeld_logo), contentDescription = null,
                modifier = Modifier.size(108.dp).padding(17.dp))
        }
        Text(
            buildAnnotatedString {
                append("Song")
                withStyle(SpanStyle(color = colors.primary)) { append("Meld") }
            }, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold,
        )
        Text(stringResource(R.string.about_description), textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        Surface(shape = CircleShape, color = colors.secondaryContainer, contentColor = colors.onSecondaryContainer) {
            Text(stringResource(R.string.settings_version, version),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge)
        }
        Text(stringResource(R.string.about_development), style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant)
    }
}

@Composable
private fun AboutSectionTitle(title: String) {
    Text(title, modifier = Modifier.padding(start = 14.dp, top = 18.dp),
        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun AboutLinkRow(
    title: String, summary: String, icon: SongMeldIconId,
    shape: RoundedCornerShape = RoundedCornerShape(28.dp), onClick: () -> Unit,
) {
    val accent = when (icon) {
        SongMeldIconId.License, SongMeldIconId.Description -> settingsAccent(2)
        SongMeldIconId.History -> settingsAccent(1)
        else -> settingsAccent(0)
    }
    Surface(onClick = onClick, shape = shape, color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = CircleShape, color = accent.background,
                contentColor = accent.foreground) {
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    SongMeldIcon(icon, contentDescription = null, size = 24.dp)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(summary, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            SongMeldIcon(SongMeldIconId.ChevronRight, contentDescription = null, size = 20.dp)
        }
    }
}

@Composable
private fun AboutPeopleSection(
    title: String, people: List<AboutPerson>, emptyText: String = "", description: String? = null,
) {
    AboutSectionTitle(title)
    if (description != null) {
        Text(description, modifier = Modifier.padding(horizontal = 14.dp),
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (people.isEmpty()) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
            Text(emptyText, modifier = Modifier.fillMaxWidth().padding(18.dp),
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        people.forEach { person -> key(person.name) { AboutPersonCard(person) } }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AboutPersonCard(person: AboutPerson) {
    val context = LocalContext.current
    Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                if (person.avatar != null) {
                    Image(painterResource(person.avatar), stringResource(R.string.about_avatar, person.name),
                        modifier = Modifier.size(72.dp).clip(RoundedCornerShape(24.dp)), contentScale = ContentScale.Crop)
                } else {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer) {
                        Box(Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                            Text(person.name.take(1), style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(person.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    person.displayName?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Text(stringResource(person.contribution), style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                person.githubUrl?.let { url ->
                    FilledTonalButton(onClick = { openAboutLink(context, url) }) { Text(stringResource(R.string.about_github)) }
                }
                person.email?.let { email ->
                    OutlinedButton(onClick = { openAboutLink(context, "mailto:$email") }) { Text(stringResource(R.string.about_contact)) }
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
