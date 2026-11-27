package pl.shotclock.budzik.ui.camera

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect

/** Stan zgody na kamerę. */
enum class CameraAccess {
    Granted,

    /** Jeszcze nie wiadomo: systemowe okno z pytaniem jest właśnie na ekranie. */
    Asking,

    /** Odmowa, ale można zapytać jeszcze raz. */
    Denied,

    /** Odmowa „na stałe”: system już nie pokaże pytania, trzeba wejść w ustawienia telefonu. */
    Blocked,
}

/** Stan zgody i akcja przycisku: ponowne pytanie albo otwarcie ustawień, zależnie od stanu. */
class CameraAccessState(val access: CameraAccess, val onRequest: () -> Unit)

/**
 * Pyta o kamerę przy pierwszym otwarciu ekranu i pamięta odpowiedź.
 * Po powrocie z ustawień telefonu sprawdza zgodę jeszcze raz, więc ekran sam się odświeży.
 */
@Composable
fun rememberCameraAccess(): CameraAccessState {
    val context = LocalContext.current
    val activity = LocalActivity.current
    var granted by remember { mutableStateOf(context.hasCameraPermission()) }
    var asked by rememberSaveable { mutableStateOf(false) }
    var blocked by rememberSaveable { mutableStateOf(false) }
    // true, gdy systemowe okno z pytaniem jest na ekranie.
    var waiting by rememberSaveable { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        waiting = false
        granted = ok
        // Po odmowie system mówi, czy wolno jeszcze raz zapytać. Jeśli nie, zostają tylko ustawienia.
        blocked = !ok && activity != null &&
            !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)
    }

    LifecycleResumeEffect(Unit) {
        granted = context.hasCameraPermission()
        onPauseOrDispose { }
    }
    LaunchedEffect(Unit) {
        if (!granted && !asked) {
            asked = true
            waiting = true
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    val access = when {
        granted -> CameraAccess.Granted
        waiting || !asked -> CameraAccess.Asking
        blocked -> CameraAccess.Blocked
        else -> CameraAccess.Denied
    }
    return CameraAccessState(access) {
        if (blocked) {
            context.openAppSettings()
        } else {
            waiting = true
            launcher.launch(Manifest.permission.CAMERA)
        }
    }
}

private fun Context.hasCameraPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

/** Otwiera stronę tej aplikacji w ustawieniach telefonu, gdzie można włączyć uprawnienia. */
private fun Context.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}
