package pl.shotclock.budzik.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import pl.shotclock.budzik.R
import pl.shotclock.budzik.ui.alarms.AlarmsRoute
import pl.shotclock.budzik.ui.camera.CameraRoute
import pl.shotclock.budzik.ui.components.AppBackground
import pl.shotclock.budzik.ui.components.AppBottomBar
import pl.shotclock.budzik.ui.components.BottomBarItem
import pl.shotclock.budzik.ui.edit.EditAlarmRoute
import pl.shotclock.budzik.ui.history.HistoryRoute
import pl.shotclock.budzik.ui.objects.ObjectsRoute
import pl.shotclock.budzik.ui.onboarding.OnboardingScreen
import pl.shotclock.budzik.ui.ring.RingScreen
import pl.shotclock.budzik.ui.ring.sampleRingUiState
import pl.shotclock.budzik.ui.settings.SettingsRoute
import pl.shotclock.budzik.ui.theme.ScreenTransitionMillis
import pl.shotclock.budzik.viewmodel.AlarmsViewModel
import pl.shotclock.budzik.viewmodel.AppStartViewModel
import pl.shotclock.budzik.viewmodel.AppViewModelProvider
import pl.shotclock.budzik.viewmodel.EditAlarmViewModel.Companion.ALARM_ID_ARG
import pl.shotclock.budzik.viewmodel.EditAlarmViewModel.Companion.NEW_ALARM_ID
import pl.shotclock.budzik.viewmodel.StartScreen

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
fun AppNavigation(
    startViewModel: AppStartViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val startScreen by startViewModel.startScreen.collectAsStateWithLifecycle()

    AppBackground {
        // Zanim ustawienia się wczytają, widać samo tło (ten sam kolor co okno przy starcie), bez mignięcia listy.
        if (startScreen == StartScreen.Loading) return@AppBackground

        NavHost(
            navController = navController,
            // Przy pierwszym uruchomieniu aplikacja zaczyna od onboardingu.
            startDestination = if (startScreen == StartScreen.Onboarding) Routes.ONBOARDING else Routes.ALARMS,
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
            composable(Routes.HISTORY) { HistoryRoute() }
            composable(Routes.OBJECTS) { ObjectsRoute() }
            composable(Routes.SETTINGS) {
                SettingsRoute(
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
                        defaultValue = NEW_ALARM_ID
                    },
                ),
            ) { backStackEntry ->
                // ViewModel listy alarmów (ten sam obiekt, którego używa lista). Usuwa alarm, żeby lista mogła go przywrócić.
                val alarmsEntry = remember(backStackEntry) { navController.getBackStackEntry(Routes.ALARMS) }
                val alarmsViewModel: AlarmsViewModel = viewModel(alarmsEntry, factory = AppViewModelProvider.Factory)

                EditAlarmRoute(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                    onDelete = { alarmId ->
                        alarmsViewModel.delete(alarmId)
                        navController.popBackStack()
                    },
                )
            }
            composable(Routes.RING) {
                // Podgląd na przykładowych danych. Prawdziwy alarm otwiera osobne okno RingActivity.
                RingScreen(uiState = sampleRingUiState(), onOpenCamera = { navController.navigate(Routes.CAMERA) })
            }
            composable(Routes.CAMERA) {
                // Podgląd z ustawień: rozpoznaje pierwszy przedmiot, ale nie wyłącza żadnego alarmu.
                CameraRoute(
                    onBack = { navController.popBackStack() },
                    onFinished = { navController.popBackStack() },
                )
            }
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onFinish = {
                        startViewModel.finishOnboarding()
                        if (navController.previousBackStackEntry != null) {
                            // Otwarty z ustawień (podgląd): wracamy tam, skąd przyszliśmy.
                            navController.popBackStack()
                        } else {
                            // Pierwsze uruchomienie: lista alarmów zastępuje onboarding, więc „wstecz” już do niego nie wróci.
                            navController.navigate(Routes.ALARMS) {
                                popUpTo(Routes.ONBOARDING) { inclusive = true }
                            }
                        }
                    },
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
        // Lista alarmów jest zawsze na dole stosu (także gdy aplikacja wystartowała od onboardingu).
        popUpTo(Routes.ALARMS) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
