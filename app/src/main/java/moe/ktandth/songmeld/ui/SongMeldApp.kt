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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.CancellationException
import moe.ktandth.songmeld.data.local.MediaStoreSongRepository
import moe.ktandth.songmeld.data.model.Song
import moe.ktandth.songmeld.ui.songs.MusicPermissionScreen
import moe.ktandth.songmeld.ui.songs.SongsScreen

@Composable
fun SongMeldApp() {
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

    if (!permissionGranted) {
        val showSettingsAction = permissionRequestedBefore &&
            activity != null &&
            !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)

        MusicPermissionScreen(
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
                songs = emptyList(),
                isLoading = true,
                errorMessage = null,
                onRetry = { refreshKey++ },
            )

            is LibraryState.Loaded -> SongsScreen(
                songs = state.songs,
                isLoading = false,
                errorMessage = null,
                onRetry = { refreshKey++ },
            )

            LibraryState.Error -> SongsScreen(
                songs = emptyList(),
                isLoading = false,
                errorMessage = "load_error",
                onRetry = { refreshKey++ },
            )
        }
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
