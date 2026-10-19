package moe.ktandth.songmeld.ui

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.CancellationException
import moe.ktandth.songmeld.R
import moe.ktandth.songmeld.data.local.MediaStoreSongRepository
import moe.ktandth.songmeld.data.model.Song
import moe.ktandth.songmeld.ui.components.SongMeldHeader
import moe.ktandth.songmeld.ui.icons.MaterialSymbolAppearance
import moe.ktandth.songmeld.ui.icons.AppIconShape
import moe.ktandth.songmeld.ui.icons.IconReference
import moe.ktandth.songmeld.ui.settings.IconAppearanceScreen
import moe.ktandth.songmeld.ui.settings.IconPickerScreen
import moe.ktandth.songmeld.ui.settings.AppTheme
import moe.ktandth.songmeld.ui.settings.SettingsPageScaffold
import moe.ktandth.songmeld.ui.settings.AppearanceSettingsScreen
import moe.ktandth.songmeld.ui.settings.AboutSettingsScreen
import moe.ktandth.songmeld.ui.settings.LicensesScreen
import moe.ktandth.songmeld.ui.settings.LicenseDetailScreen
import moe.ktandth.songmeld.ui.settings.LicenseDocument
import moe.ktandth.songmeld.ui.settings.ChangelogScreen
import moe.ktandth.songmeld.ui.settings.TermsScreen
import moe.ktandth.songmeld.ui.settings.SettingsScreen
import moe.ktandth.songmeld.ui.songs.MusicPermissionScreen
import moe.ktandth.songmeld.ui.songs.SongsScreen

@Composable
fun SongMeldApp(
    theme: AppTheme,
    dynamicColorEnabled: Boolean,
    symbols: MaterialSymbolAppearance,
    iconShape: AppIconShape,
    selectedIcon: IconReference?,
    onSelectedIconChanged: (IconReference) -> Unit,
    onSymbolsChanged: (MaterialSymbolAppearance) -> Unit,
    onIconShapeChanged: (AppIconShape) -> Unit,
    onThemeChanged: (AppTheme) -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val permission = remember { requiredAudioPermission() }
    val repository = remember(context) { MediaStoreSongRepository(context.applicationContext) }
    val permissionPreferences = remember(context) {
        context.getSharedPreferences("songmeld_permissions", Context.MODE_PRIVATE)
    }

    var permissionGranted by remember {
        mutableStateOf(context.hasPermission(permission))
    }
    var permissionRequestedBefore by remember {
        mutableStateOf(permissionPreferences.getBoolean("audio_permission_requested", false))
    }
    var refreshKey by remember { mutableIntStateOf(0) }
    // One saveable destination prevents conflicting combinations of page flags.
    var currentPage by rememberSaveable { mutableStateOf(SongMeldPage.LIBRARY) }
    var selectedLicense by rememberSaveable { mutableStateOf(LicenseDocument.SONGMELD) }
    fun navigateBack() {
        currentPage = currentPage.parent
    }
    BackHandler(enabled = currentPage != SongMeldPage.LIBRARY) { navigateBack() }
    var libraryState by remember { mutableStateOf<LibraryState>(LibraryState.Idle) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        permissionGranted = granted
        if (granted) refreshKey++
    }

    DisposableEffect(activity, permission) {
        if (activity == null) {
            onDispose { }
        } else {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    val nowGranted = context.hasPermission(permission)
                    if (nowGranted && !permissionGranted) refreshKey++
                    permissionGranted = nowGranted
                }
            }
            activity.lifecycle.addObserver(observer)
            onDispose { activity.lifecycle.removeObserver(observer) }
        }
    }

    LaunchedEffect(permissionGranted, refreshKey) {
        if (!permissionGranted) {
            libraryState = LibraryState.Idle
            return@LaunchedEffect
        }

        libraryState = LibraryState.Loading
        libraryState = try {
            LibraryState.Loaded(repository.loadSongs())
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: SecurityException) {
            permissionGranted = context.hasPermission(permission)
            LibraryState.Error
        } catch (_: Exception) {
            LibraryState.Error
        }
    }

    if (currentPage != SongMeldPage.LIBRARY) {
        // Own Scaffold per destination: collapsing app bars must receive nested scroll directly
        // from their content. Keeping the library screen out of this branch avoids double insets.
        when (currentPage) {
            SongMeldPage.ICON_PICKER -> key("icon_picker") {
                SettingsPageScaffold(
                    title = R.string.icon_browse_title,
                    onBack = { navigateBack() },
                ) { innerPadding ->
                    IconPickerScreen(
                        modifier = Modifier.fillMaxSize()
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding),
                        appearance = symbols,
                        selected = selectedIcon,
                        onSelected = onSelectedIconChanged,
                    )
                }
            }

            SongMeldPage.ICON_APPEARANCE -> key("icon_appearance") {
                SettingsPageScaffold(
                    title = R.string.settings_icon_appearance,
                    onBack = { navigateBack() },
                ) { innerPadding ->
                    IconAppearanceScreen(
                        modifier = Modifier.fillMaxSize()
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding),
                        appearance = symbols,
                        iconShape = iconShape,
                        onAppearanceChanged = onSymbolsChanged,
                        onIconShapeChanged = onIconShapeChanged,
                        onBrowseIcons = { currentPage = SongMeldPage.ICON_PICKER },
                        selectedCatalogIcon = selectedIcon,
                    )
                }
            }

            SongMeldPage.APPEARANCE -> key("appearance") {
                AppearanceSettingsScreen(
                    theme = theme,
                    dynamicColorEnabled = dynamicColorEnabled,
                    onThemeChanged = onThemeChanged,
                    onDynamicColorChanged = onDynamicColorChanged,
                    onOpenIconAppearance = { currentPage = SongMeldPage.ICON_APPEARANCE },
                    onBack = { navigateBack() },
                )
            }

            SongMeldPage.ABOUT -> key("about") {
                AboutSettingsScreen(
                    onBack = { navigateBack() },
                    onOpenLicenses = { currentPage = SongMeldPage.LICENSES },
                    onOpenChangelog = { currentPage = SongMeldPage.CHANGELOG },
                    onOpenTerms = { currentPage = SongMeldPage.TERMS },
                )
            }

            SongMeldPage.LICENSES -> key("licenses") {
                LicensesScreen(onBack = { navigateBack() }, onOpenLicense = {
                    selectedLicense = it
                    currentPage = SongMeldPage.LICENSE_DETAIL
                })
            }
            SongMeldPage.LICENSE_DETAIL -> key(selectedLicense) {
                LicenseDetailScreen(document = selectedLicense, onBack = { navigateBack() })
            }
            SongMeldPage.CHANGELOG -> key("changelog") {
                ChangelogScreen(onBack = { navigateBack() })
            }
            SongMeldPage.TERMS -> key("terms") {
                TermsScreen(onBack = { navigateBack() })
            }
            SongMeldPage.SETTINGS -> key("settings") {
                SettingsScreen(
                    onBack = { navigateBack() },
                    onOpenAppearance = { currentPage = SongMeldPage.APPEARANCE },
                    onOpenAbout = { currentPage = SongMeldPage.ABOUT },
                )
            }

            SongMeldPage.LIBRARY -> Unit // Handled by the outer branch.
        }
    } else {
        // The branded header stays composed while the audio permission/library state changes.
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = { SongMeldHeader(onOpenSettings = { currentPage = SongMeldPage.SETTINGS }) },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (!permissionGranted) {
                    val showSettingsAction = permissionRequestedBefore &&
                        activity != null &&
                        !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)

                    MusicPermissionScreen(
                        modifier = Modifier.fillMaxSize(),
                        showSettingsAction = showSettingsAction,
                        onRequestPermission = {
                            permissionRequestedBefore = true
                            permissionPreferences.edit()
                                .putBoolean("audio_permission_requested", true)
                                .apply()
                            permissionLauncher.launch(permission)
                        },
                        onOpenSettings = {
                            context.startActivity(
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                            )
                        },
                    )
                } else {
                    when (val state = libraryState) {
                        LibraryState.Idle,
                        LibraryState.Loading -> SongsScreen(
                            modifier = Modifier.fillMaxSize(),
                            songs = emptyList(),
                            isLoading = true,
                            errorMessage = null,
                            onRetry = { refreshKey++ },
                        )

                        is LibraryState.Loaded -> SongsScreen(
                            modifier = Modifier.fillMaxSize(),
                            songs = state.songs,
                            isLoading = false,
                            errorMessage = null,
                            onRetry = { refreshKey++ },
                        )

                        LibraryState.Error -> SongsScreen(
                            modifier = Modifier.fillMaxSize(),
                            songs = emptyList(),
                            isLoading = false,
                            errorMessage = "load_error",
                            onRetry = { refreshKey++ },
                        )
                    }
                }
            }
        }
    }
}

/** Parent destinations for the Settings flow; saved/restored as one enum value. */
private enum class SongMeldPage {
    LIBRARY, SETTINGS, APPEARANCE, ABOUT, ICON_APPEARANCE, ICON_PICKER,
    LICENSES, LICENSE_DETAIL, CHANGELOG, TERMS;

    val parent: SongMeldPage
        get() = when (this) {
            LIBRARY, SETTINGS -> LIBRARY
            APPEARANCE, ABOUT -> SETTINGS
            LICENSES, CHANGELOG, TERMS -> ABOUT
            LICENSE_DETAIL -> LICENSES
            ICON_APPEARANCE -> APPEARANCE
            ICON_PICKER -> ICON_APPEARANCE
        }
}

private sealed interface LibraryState {
    data object Idle : LibraryState
    data object Loading : LibraryState
    data class Loaded(val songs: List<Song>) : LibraryState
    data object Error : LibraryState
}

private fun requiredAudioPermission(): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

private fun Context.hasPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

private fun Context.findActivity(): ComponentActivity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is ComponentActivity) return current
        current = current.baseContext
    }
    return null
}
