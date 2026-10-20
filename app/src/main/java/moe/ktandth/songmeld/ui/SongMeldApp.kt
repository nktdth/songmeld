package moe.ktandth.songmeld.ui

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import moe.ktandth.songmeld.R
import moe.ktandth.songmeld.data.local.MediaStoreSongRepository
import moe.ktandth.songmeld.ui.songs.LibraryLoadState
import moe.ktandth.songmeld.ui.components.SongMeldHeader
import moe.ktandth.songmeld.ui.icons.MaterialSymbolAppearance
import moe.ktandth.songmeld.ui.icons.AppIconShape
import moe.ktandth.songmeld.ui.icons.IconReference
import moe.ktandth.songmeld.ui.settings.IconAppearanceScreen
import moe.ktandth.songmeld.ui.settings.IconPickerScreen
import moe.ktandth.songmeld.ui.settings.InterfaceOptions
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
    onInterfaceOptionsChanged: (InterfaceOptions) -> Unit,
    onResetAppearance: () -> Unit,
    dynamicColorEnabled: Boolean,
    amoledBlackEnabled: Boolean,
    onAmoledBlackChanged: (Boolean) -> Unit,
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
    var libraryWasShown by rememberSaveable { mutableStateOf(false) }
    // One saveable destination prevents conflicting combinations of page flags.
    var currentPage by rememberSaveable { mutableStateOf(SongMeldPage.LIBRARY) }
    var selectedLicense by rememberSaveable { mutableStateOf(LicenseDocument.SONGMELD) }
    fun navigateBack() {
        currentPage = currentPage.parent
    }
    BackHandler(enabled = currentPage != SongMeldPage.LIBRARY) { navigateBack() }
    var libraryState by remember { mutableStateOf(LibraryLoadState()) }
    var mediaChangeKey by remember { mutableIntStateOf(0) }
    var foreground by remember(activity) {
        mutableStateOf(activity?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) ?: true)
    }

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
                if (event == Lifecycle.Event.ON_START) foreground = true
                if (event == Lifecycle.Event.ON_STOP) foreground = false
                if (event == Lifecycle.Event.ON_RESUME) {
                    val nowGranted = context.hasPermission(permission)
                    if (nowGranted && (!permissionGranted || libraryState.hasLoaded)) refreshKey++
                    permissionGranted = nowGranted
                }
            }
            activity.lifecycle.addObserver(observer)
            onDispose { activity.lifecycle.removeObserver(observer) }
        }
    }

    DisposableEffect(context, permissionGranted, foreground) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) { mediaChangeKey++ }
        }
        var registered = false
        if (permissionGranted && foreground) {
            try {
                context.contentResolver.registerContentObserver(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, observer,
                )
                registered = true
            } catch (_: SecurityException) {
                permissionGranted = context.hasPermission(permission)
            }
        }
        onDispose { if (registered) context.contentResolver.unregisterContentObserver(observer) }
    }
    LaunchedEffect(mediaChangeKey) {
        if (mediaChangeKey > 0) {
            delay(350) // Coalesce bursts from downloads, file copies and metadata updates.
            refreshKey++
        }
    }

    LaunchedEffect(permissionGranted, refreshKey, foreground) {
        if (!permissionGranted) {
            libraryState = LibraryLoadState()
            return@LaunchedEffect
        }

        if (!foreground) return@LaunchedEffect
        libraryState = libraryState.refreshing()
        libraryState = try {
            libraryState.loaded(repository.loadSongs())
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: SecurityException) {
            permissionGranted = context.hasPermission(permission)
            libraryState.failed()
        } catch (_: Exception) {
            libraryState.failed()
        }
    }

    val pageStateHolder = rememberSaveableStateHolder()
    val pageKey = if (currentPage == SongMeldPage.LICENSE_DETAIL) {
        "${currentPage.name}:${selectedLicense.name}"
    } else currentPage.name
    pageStateHolder.SaveableStateProvider(pageKey) {
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
                        onInterfaceOptionsChanged = onInterfaceOptionsChanged,
                        onResetAppearance = onResetAppearance,
                        dynamicColorEnabled = dynamicColorEnabled,
                        amoledBlackEnabled = amoledBlackEnabled,
                        onAmoledBlackChanged = onAmoledBlackChanged,
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
                        theme = theme,
                        dynamicColorEnabled = dynamicColorEnabled,
                        amoledBlackEnabled = amoledBlackEnabled,
                        onBack = { navigateBack() },
                        onOpenAppearance = { currentPage = SongMeldPage.APPEARANCE },
                        onOpenAbout = { currentPage = SongMeldPage.ABOUT },
                    )
                }

                SongMeldPage.LIBRARY -> Unit // Handled by the outer branch.
            }
        } else {
            // Capture only on mounting this destination; recomposition must not cancel the entrance.
            val animateHeader = remember { !libraryWasShown }
            SideEffect { libraryWasShown = true }
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = { SongMeldHeader(animateEntrance = animateHeader,
                    onOpenSettings = { currentPage = SongMeldPage.SETTINGS }) },
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding),
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
                        SongsScreen(
                            modifier = Modifier.fillMaxSize(),
                            songs = libraryState.songs,
                            isLoading = libraryState.loading && !libraryState.hasLoaded,
                            isRefreshing = libraryState.loading && libraryState.hasLoaded,
                            refreshFailed = libraryState.error && libraryState.hasLoaded,
                            errorMessage = if (libraryState.error && !libraryState.hasLoaded) "load_error" else null,
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
