package pl.shotclock.budzik.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import pl.shotclock.budzik.R
import pl.shotclock.budzik.ui.alarms.AlarmsRoute
import pl.shotclock.budzik.ui.camera.CameraScreen
import pl.shotclock.budzik.ui.components.AppBackground
import pl.shotclock.budzik.ui.components.AppBottomBar
import pl.shotclock.budzik.ui.components.BottomBarItem
import pl.shotclock.budzik.ui.edit.EditAlarmScreen
import pl.shotclock.budzik.ui.history.HistoryScreen
import pl.shotclock.budzik.ui.objects.ObjectsScreen
import pl.shotclock.budzik.ui.onboarding.OnboardingScreen
import pl.shotclock.budzik.ui.ring.RingScreen
import pl.shotclock.budzik.ui.settings.SettingsScreen
import pl.shotclock.budzik.ui.theme.ScreenTransitionMillis

/** Nazwa argumentu z numerem alarmu w adresie ekranu edycji. 0 oznacza nowy alarm. */
private const val ALARM_ID_ARG = "alarmId"

/** Nazwy wszystkich ekranów w jednym miejscu. */
private object Routes {
    const val ALARMS = "alarms"
    const val HISTORY = "history"
    const val OBJECTS = "objects"
    const val SETTINGS = "settings"
    // Ekran edycji może dostać id alarmu, np. "edit_alarm?alarmId=3". Bez niego tworzymy nowy alarm.
    const val EDIT_ALARM = "edit_alarm?$ALARM_ID_ARG={$ALARM_ID_ARG}"
    const val RING = "ring"
    const val CAMERA = "camera"
    const val ONBOARDING = "onboarding"

    fun newAlarm() = "edit_alarm"

    fun editAlarm(alarmId: Long) = "edit_alarm?$ALARM_ID_ARG=$alarmId"
}

// Dolny pasek widać tylko na tych czterech ekranach.
private val bottomBarItems = listOf(
    BottomBarItem(Routes.ALARMS, R.string.tab_alarms, R.drawable.ic_alarm),
    BottomBarItem(Routes.HISTORY, R.string.tab_history, R.drawable.ic_history),
    BottomBarItem(Routes.OBJECTS, R.string.tab_objects, R.drawable.ic_category),
    BottomBarItem(Routes.SETTINGS, R.string.tab_settings, R.drawable.ic_settings),
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    AppBackground {
        NavHost(
            navController = navController,
            startDestination = Routes.ALARMS,
            enterTransition = { fadeIn(tween(ScreenTransitionMillis)) },
            exitTransition = { fadeOut(tween(ScreenTransitionMillis)) },
            popEnterTransition = { fadeIn(tween(ScreenTransitionMillis)) },
            popExitTransition = { fadeOut(tween(ScreenTransitionMillis)) },
        ) {
            composable(Routes.ALARMS) {
                AlarmsRoute(
                    onAddAlarm = { navController.navigate(Routes.newAlarm()) },
                    onOpenAlarm = { alarmId -> navController.navigate(Routes.editAlarm(alarmId)) },
                )
            }
            composable(Routes.HISTORY) { HistoryScreen() }
            composable(Routes.OBJECTS) { ObjectsScreen() }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onOpenOnboarding = { navController.navigate(Routes.ONBOARDING) },
                    onOpenRing = { navController.navigate(Routes.RING) },
                    onOpenCamera = { navController.navigate(Routes.CAMERA) },
                )
            }
            composable(
                route = Routes.EDIT_ALARM,
                arguments = listOf(
                    navArgument(ALARM_ID_ARG) {
                        type = NavType.LongType
                        defaultValue = 0L
                    },
                ),
            ) {
                EditAlarmScreen(
                    onBack = { navController.popBackStack() },
                    onSave = { navController.popBackStack() },
                )
            }
            composable(Routes.RING) {
                RingScreen(onOpenCamera = { navController.navigate(Routes.CAMERA) })
            }
            composable(Routes.CAMERA) {
                CameraScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onFinish = { navController.popBackStack(Routes.ALARMS, inclusive = false) },
                )
            }
        }

        if (bottomBarItems.any { it.route == currentRoute }) {
            AppBottomBar(
                items = bottomBarItems,
                selectedRoute = currentRoute,
                onItemClick = { item -> navController.openTab(item.route) },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

/** Przełącza zakładkę tak, żeby każda pamiętała swój stan i żeby na stosie nie gromadziły się kopie ekranów. */
private fun NavHostController.openTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
