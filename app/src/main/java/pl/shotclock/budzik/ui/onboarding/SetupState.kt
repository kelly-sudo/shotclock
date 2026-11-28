package pl.shotclock.budzik.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import pl.shotclock.budzik.alarm.AlarmPermissions
import pl.shotclock.budzik.alarm.SetupStep
import pl.shotclock.budzik.alarm.setupStepsFor

/** Jeden krok na liście zgód: czy jest gotowy i czy zostały już tylko ustawienia telefonu. */
data class SetupStepState(
    val step: SetupStep,
    val done: Boolean,
    // true: okienka z pytaniem już nie będzie (odmowa „na stałe” albo zgoda włączana tylko w ustawieniach).
    val opensSettings: Boolean,
)

/** Lista kroków i akcja przycisku przy kroku. */
class SetupState(
    val steps: List<SetupStepState>,
    val onRequest: (SetupStep) -> Unit,
)

/**
 * Sprawdza zgody z onboardingu i prosi o nie. Okienko systemowe jest tylko dla powiadomień i kamery,
 * resztę włącza się w ustawieniach telefonu. Po powrocie na ekran stan sprawdzamy jeszcze raz,
 * bo użytkownik mógł właśnie coś włączyć w ustawieniach.
 */
@Composable
fun rememberSetupState(): SetupState {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val steps = remember { setupStepsFor(Build.VERSION.SDK_INT) }

    fun readDone() = steps.associateWith { AlarmPermissions.isDone(context, it) }

    var done by remember { mutableStateOf(readDone()) }
    // Kroki, o które system już nie zapyta w okienku, bo użytkownik odmówił „na stałe”.
    var blocked by remember { mutableStateOf(emptySet<SetupStep>()) }
    // Krok, o który właśnie pyta okienko systemowe.
    var asking by remember { mutableStateOf<SetupStep?>(null) }

    LifecycleResumeEffect(Unit) {
        done = readDone()
        onPauseOrDispose { }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val step = asking
        val permission = step?.runtimePermission()
        // Po odmowie system mówi, czy wolno zapytać jeszcze raz. Jeśli nie, zostają tylko ustawienia telefonu.
        if (!granted && step != null && permission != null && activity != null &&
            !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        ) {
            blocked = blocked + step
        }
        asking = null
        done = readDone()
    }

    return SetupState(
        steps = steps.map { step ->
            SetupStepState(
                step = step,
                done = done[step] == true,
                opensSettings = step.runtimePermission() == null || step in blocked,
            )
        },
        onRequest = { step ->
            val permission = step.runtimePermission()
            if (permission != null && step !in blocked) {
                asking = step
                launcher.launch(permission)
            } else {
                context.startActivity(AlarmPermissions.settingsIntent(context, step))
            }
        },
    )
}

/** Uprawnienie, o które można zapytać w okienku systemowym, albo null, gdy zostają tylko ustawienia telefonu. */
private fun SetupStep.runtimePermission(): String? = when {
    this == SetupStep.CAMERA -> Manifest.permission.CAMERA
    this == SetupStep.NOTIFICATIONS && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
        Manifest.permission.POST_NOTIFICATIONS
    else -> null
}
