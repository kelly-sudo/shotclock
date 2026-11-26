package pl.shotclock.budzik.ui.alarms

import android.Manifest
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import pl.shotclock.budzik.R
import pl.shotclock.budzik.alarm.AlarmPermissions
import pl.shotclock.budzik.alarm.MissingPermission
import pl.shotclock.budzik.ui.components.AppBackground
import pl.shotclock.budzik.ui.components.AppButton
import pl.shotclock.budzik.ui.components.AppButtonStyle
import pl.shotclock.budzik.ui.components.AppCard
import pl.shotclock.budzik.ui.theme.PeachTint
import pl.shotclock.budzik.ui.theme.ShotClockTheme

/** Brakujące uprawnienie i akcja, która prosi o nie użytkownika. */
class PermissionWarningState(
    val missing: MissingPermission?,
    val onFix: (MissingPermission) -> Unit,
)

/**
 * Sprawdza uprawnienia potrzebne do dzwonienia. Sprawdza je ponownie po każdym powrocie na ekran,
 * bo użytkownik mógł je właśnie włączyć w ustawieniach systemu.
 */
@Composable
fun rememberPermissionWarning(): PermissionWarningState {
    val context = LocalContext.current
    val activity = LocalActivity.current
    var missing by remember { mutableStateOf(AlarmPermissions.firstMissing(context)) }

    LifecycleResumeEffect(Unit) {
        missing = AlarmPermissions.firstMissing(context)
        onPauseOrDispose {}
    }

    // Powiadomienia (Android 13+) mają systemowe okienko z pytaniem. Gdy użytkownik odmówił na stałe,
    // okienko już się nie pokaże, więc otwieramy ustawienia aplikacji.
    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val canAskAgain = activity != null &&
            ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)
        if (!granted && !canAskAgain) {
            context.startActivity(AlarmPermissions.settingsIntent(context, MissingPermission.NOTIFICATIONS))
        }
        missing = AlarmPermissions.firstMissing(context)
    }

    return PermissionWarningState(missing) { permission ->
        if (permission == MissingPermission.NOTIFICATIONS && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            // Dokładne alarmy i pełny ekran włącza się tylko w ustawieniach systemu.
            context.startActivity(AlarmPermissions.settingsIntent(context, permission))
        }
    }
}

/** Karta z prośbą o brakujące uprawnienie, pokazywana nad listą alarmów. */
@Composable
fun PermissionWarningCard(
    permission: MissingPermission,
    onFix: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val texts = permission.texts()
    AppCard(modifier = modifier.fillMaxWidth(), tint = PeachTint) {
        Text(text = stringResource(texts.title), style = MaterialTheme.typography.titleMedium)
        Text(
            text = stringResource(texts.description),
            style = MaterialTheme.typography.bodyMedium,
            color = PeachTint.secondaryText,
        )
        AppButton(
            text = stringResource(texts.action),
            onClick = onFix,
            modifier = Modifier.fillMaxWidth(),
            style = AppButtonStyle.Secondary,
        )
    }
}

private class PermissionTexts(
    @param:StringRes val title: Int,
    @param:StringRes val description: Int,
    @param:StringRes val action: Int,
)

private fun MissingPermission.texts(): PermissionTexts = when (this) {
    MissingPermission.NOTIFICATIONS -> PermissionTexts(
        R.string.permission_notifications_title,
        R.string.permission_notifications_description,
        R.string.permission_action_allow,
    )
    MissingPermission.EXACT_ALARMS -> PermissionTexts(
        R.string.permission_exact_alarms_title,
        R.string.permission_exact_alarms_description,
        R.string.permission_action_settings,
    )
    MissingPermission.FULL_SCREEN -> PermissionTexts(
        R.string.permission_full_screen_title,
        R.string.permission_full_screen_description,
        R.string.permission_action_settings,
    )
}

@Preview(widthDp = 360)
@Composable
private fun PermissionWarningCardPreview() {
    ShotClockTheme {
        AppBackground {
            PermissionWarningCard(permission = MissingPermission.NOTIFICATIONS, onFix = {})
        }
    }
}
