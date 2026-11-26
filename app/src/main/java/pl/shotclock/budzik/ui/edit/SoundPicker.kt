package pl.shotclock.budzik.ui.edit

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.core.content.IntentCompat
import androidx.core.net.toUri
import pl.shotclock.budzik.R

/**
 * Otwiera systemowy wybór dźwięku alarmu (ten sam, co w ustawieniach telefonu).
 * Zwraca funkcję, którą wywołujemy po kliknięciu wiersza „Dźwięk”.
 * [onPicked] dostaje adres wybranego dźwięku albo null dla domyślnego dźwięku telefonu.
 */
@Composable
fun rememberSoundPicker(currentSound: String?, onPicked: (String?) -> Unit): () -> Unit {
    val pickerTitle = stringResource(R.string.edit_sound)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data ?: return@rememberLauncherForActivityResult
        val picked = IntentCompat.getParcelableExtra(data, RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
        // Wybór „Domyślny” zapisujemy jako null, żeby alarm szedł za zmianą domyślnego dźwięku w telefonie.
        onPicked(picked?.takeIf { it != Settings.System.DEFAULT_ALARM_ALERT_URI }?.toString())
    }
    return {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
            .putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, pickerTitle)
            .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            .putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_ALARM_ALERT_URI)
            // Cisza nie ma sensu w budziku.
            .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            .putExtra(
                RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                currentSound?.toUri() ?: Settings.System.DEFAULT_ALARM_ALERT_URI,
            )
        try {
            launcher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            // Niektóre telefony nie mają systemowego wyboru dźwięku. Wtedy zostaje domyślny.
        }
    }
}

/** Nazwa dźwięku do pokazania w wierszu „Dźwięk”. */
@Composable
fun soundTitle(sound: String?): String {
    val defaultTitle = stringResource(R.string.edit_sound_default)
    val customTitle = stringResource(R.string.edit_sound_custom)
    // W podglądzie Android Studio nie ma dzwonków telefonu.
    if (sound == null || LocalInspectionMode.current) return defaultTitle
    val context = LocalContext.current
    return remember(sound) { readRingtoneTitle(context, sound.toUri()) } ?: customTitle
}

private fun readRingtoneTitle(context: Context, uri: Uri): String? =
    try {
        RingtoneManager.getRingtone(context, uri)?.getTitle(context)
    } catch (e: Exception) {
        // Np. plik dźwięku usunięto z telefonu.
        null
    }
