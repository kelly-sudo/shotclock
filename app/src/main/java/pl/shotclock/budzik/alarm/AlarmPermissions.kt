package pl.shotclock.budzik.alarm

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

/** Uprawnienie potrzebne do dzwonienia, którego aplikacji jeszcze brakuje. Kolejność = ważność. */
enum class MissingPermission {
    // Bez powiadomień (Android 13+) nie pokażemy ekranu dzwonienia ani paska z alarmem.
    NOTIFICATIONS,
    // Bez dokładnych alarmów (Android 12) system nie pozwoli ustawić budzika.
    EXACT_ALARMS,
    // Bez full-screen intent (Android 14+) ekran dzwonienia nie otworzy się sam na zablokowanym telefonie.
    FULL_SCREEN,
}

/**
 * Sprawdzanie uprawnień w jednym miejscu. Na starszych Androidach dane uprawnienie nie istnieje
 * albo jest przyznane od razu, więc wtedy zawsze zwracamy true.
 */
object AlarmPermissions {

    fun canPostNotifications(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    fun canScheduleExactAlarms(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()

    fun canUseFullScreenIntent(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE ||
            context.getSystemService(NotificationManager::class.java).canUseFullScreenIntent()

    /** Najważniejsze brakujące uprawnienie albo null, gdy niczego nie brakuje. */
    fun firstMissing(context: Context): MissingPermission? = when {
        !canPostNotifications(context) -> MissingPermission.NOTIFICATIONS
        !canScheduleExactAlarms(context) -> MissingPermission.EXACT_ALARMS
        !canUseFullScreenIntent(context) -> MissingPermission.FULL_SCREEN
        else -> null
    }

    /** Ekran ustawień systemu, na którym użytkownik może sam włączyć brakujące uprawnienie. */
    fun settingsIntent(context: Context, permission: MissingPermission): Intent {
        val packageUri = "package:${context.packageName}".toUri()
        return when {
            permission == MissingPermission.EXACT_ALARMS && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, packageUri)
            permission == MissingPermission.FULL_SCREEN && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE ->
                Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, packageUri)
            else ->
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
