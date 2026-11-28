package pl.shotclock.budzik.ui.camera

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import pl.shotclock.budzik.R
import pl.shotclock.budzik.alarm.RecognitionHint
import pl.shotclock.budzik.data.RequiredObject
import pl.shotclock.budzik.ui.components.AppButton
import pl.shotclock.budzik.ui.components.AppButtonStyle
import pl.shotclock.budzik.ui.components.AppCard
import pl.shotclock.budzik.ui.components.AppIconButton
import pl.shotclock.budzik.ui.objects.ObjectImage
import pl.shotclock.budzik.ui.objects.ObjectVisual
import pl.shotclock.budzik.ui.theme.Accent
import pl.shotclock.budzik.ui.theme.AppShapes
import pl.shotclock.budzik.ui.theme.CameraBackground
import pl.shotclock.budzik.ui.theme.SectionLabelText
import pl.shotclock.budzik.ui.theme.ShotClockTheme
import pl.shotclock.budzik.ui.theme.Sizes
import pl.shotclock.budzik.ui.theme.Spacing
import pl.shotclock.budzik.ui.theme.SurfaceColor
import pl.shotclock.budzik.ui.theme.TextPrimary
import pl.shotclock.budzik.ui.theme.TextSecondary
import pl.shotclock.budzik.ui.theme.entranceFrom
import pl.shotclock.budzik.ui.theme.glassSurface
import pl.shotclock.budzik.ui.theme.rememberEntrance
import pl.shotclock.budzik.viewmodel.AppViewModelProvider
import pl.shotclock.budzik.viewmodel.CameraUiState
import pl.shotclock.budzik.viewmodel.CameraViewModel
import kotlin.math.roundToInt

// Tyle widać znaczek sukcesu na celowniku, zanim pojawi się ekran sukcesu.
private const val SuccessDisplayMillis = 1_200L

// Tylko do testów modelu: pokazuje pod paskiem pewności wyniki dla wszystkich klas. Przed demo ustaw false.
private const val ShowModelScores = true

/**
 * Ekran kamery z ViewModelem: pyta o zgodę, uruchamia kamerę i rozpoznawanie.
 * Po rozpoznaniu przedmiotu od razu wywołuje [onRecognized] (wtedy alarm cichnie),
 * pokazuje na celowniku znaczek sukcesu i po chwili wywołuje [onFinished].
 * Podgląd z ustawień nie podaje [onRecognized], więc niczego nie wyłącza.
 */
@Composable
fun CameraRoute(
    onBack: () -> Unit,
    onFinished: () -> Unit,
    onRecognized: (confidence: Float) -> Unit = {},
    emergencyExitAvailable: Boolean = false,
    onEmergencyExit: () -> Unit = {},
    viewModel: CameraViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraAccess = rememberCameraAccess()
    // Najnowsze wersje funkcji, bo efekt niżej działa dłużej niż jedno przerysowanie ekranu.
    val currentOnRecognized by rememberUpdatedState(onRecognized)
    val currentOnFinished by rememberUpdatedState(onFinished)

    LaunchedEffect(Unit) { viewModel.onCameraOpened() }
    LaunchedEffect(uiState.isRecognized) {
        if (uiState.isRecognized) {
            currentOnRecognized(uiState.confidence)
            delay(SuccessDisplayMillis)
            currentOnFinished()
        }
    }
    val close = { if (uiState.isRecognized) onFinished() else onBack() }
    // Po rozpoznaniu „Wstecz” od razu zamyka ekran, zamiast wracać do dzwonienia, które już ucichło.
    BackHandler(onBack = close)

    if (cameraAccess.access == CameraAccess.Granted) {
        // Kamera startuje dopiero, gdy jest zgoda. Inaczej CameraX zgłosiłby błąd.
        val session = rememberCameraSession(viewModel)
        CameraScreen(
            uiState = uiState,
            onBack = close,
            emergencyExitAvailable = emergencyExitAvailable,
            onEmergencyExit = onEmergencyExit,
            onTakePhoto = {
                viewModel.onTakePhotoStarted()
                session.takePhoto(onPhoto = viewModel::onPhotoTaken, onError = viewModel::onPhotoFailed)
            },
            preview = { CameraPreview(session, Modifier.fillMaxSize()) },
        )
    } else {
        CameraPermissionScreen(
            objectToFind = uiState.objectToFind,
            access = cameraAccess.access,
            onBack = close,
            onRequestAccess = cameraAccess.onRequest,
        )
    }
}

/**
 * Wygląd ekranu kamery. [preview] to obraz z kamery; w podglądach @Preview zastępuje go zwykłe tło.
 * Na górze: powrót i szukany przedmiot. W środku celownik. Na dole szklany panel z podpowiedzią i migawka.
 * Gdy [emergencyExitAvailable], w panelu jest też przycisk „Nie mogę znaleźć przedmiotu”.
 */
@Composable
fun CameraScreen(
    uiState: CameraUiState,
    onBack: () -> Unit,
    onTakePhoto: () -> Unit,
    modifier: Modifier = Modifier,
    emergencyExitAvailable: Boolean = false,
    onEmergencyExit: () -> Unit = {},
    preview: @Composable () -> Unit = {},
) {
    Box(modifier.fillMaxSize().background(CameraBackground)) {
        preview()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = Spacing.screenEdge, vertical = Spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CameraTopBar(objectToFind = uiState.objectToFind, onBack = onBack)
            // Celownik zajmuje wolne miejsce, ale na niskim ekranie maleje, żeby panel i migawka zawsze się mieściły.
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Reticle(
                    recognized = uiState.isRecognized,
                    modifier = Modifier
                        .sizeIn(maxWidth = Sizes.reticle, maxHeight = Sizes.reticle)
                        .aspectRatio(1f, matchHeightConstraintsFirst = true),
                )
            }
            HintPanel(
                uiState = uiState,
                // Po rozpoznaniu alarm już ucichł, więc wyjście awaryjne nie jest potrzebne.
                showEmergencyExit = emergencyExitAvailable && !uiState.isRecognized,
                onEmergencyExit = onEmergencyExit,
                modifier = Modifier.fillMaxWidth().entranceFrom(rememberEntrance(order = 1)),
            )
            ShutterButton(
                onClick = onTakePhoto,
                enabled = uiState.hasFrame && !uiState.isTakingPhoto && !uiState.isRecognized,
                modifier = Modifier
                    .padding(top = Spacing.medium)
                    .entranceFrom(rememberEntrance(order = 2)),
            )
        }
    }
}

@Composable
private fun CameraTopBar(objectToFind: RequiredObject, onBack: () -> Unit) {
    val visual = ObjectVisual.of(objectToFind)
    Row(
        modifier = Modifier.fillMaxWidth().entranceFrom(rememberEntrance(order = 0)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.smallMedium),
    ) {
        AppIconButton(
            icon = R.drawable.ic_arrow_back,
            contentDescription = stringResource(R.string.action_back),
            onClick = onBack,
        )
        // Szklana „pigułka” z przedmiotem, którego szukamy.
        Row(
            modifier = Modifier
                .weight(1f)
                .glassSurface(AppShapes.pill)
                .padding(horizontal = Spacing.medium, vertical = Spacing.small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.smallMedium),
        ) {
            ObjectImage(visual, contentDescription = null, modifier = Modifier.size(Sizes.illustrationSmall))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.camera_looking_for), style = SectionLabelText, color = TextSecondary)
                // Krótka nazwa, żeby zmieściła się w jednej linii nawet na wąskim ekranie z dużą czcionką.
                Text(
                    text = stringResource(visual.shortNameRes),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Szklany panel: duża podpowiedź, jedno zdanie wyjaśnienia, pasek pewności i ewentualnie wyjście awaryjne. */
@Composable
private fun HintPanel(
    uiState: CameraUiState,
    showEmergencyExit: Boolean,
    onEmergencyExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (title, detail) = hintTexts(uiState)
    Column(
        modifier = modifier
            .glassSurface(AppShapes.card)
            .padding(Spacing.screenEdge),
        verticalArrangement = Arrangement.spacedBy(Spacing.smallMedium),
    ) {
        // liveRegion: czytnik ekranu sam przeczyta nową podpowiedź, gdy się zmieni.
        Column(Modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = if (uiState.isRecognized) Accent else TextPrimary,
            )
            Text(text = detail, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
        ConfidenceBar(confidence = uiState.confidence, threshold = uiState.threshold)
        if (ShowModelScores && uiState.modelScores.isNotEmpty()) {
            val scores = uiState.modelScores.joinToString("  ·  ") { "${it.label} ${(it.confidence * 100).roundToInt()}%" }
            Text(
                text = stringResource(R.string.camera_model_scores, scores),
                style = SectionLabelText,
                color = TextSecondary,
            )
        }
        if (showEmergencyExit) {
            AppButton(
                text = stringResource(R.string.emergency_open),
                onClick = onEmergencyExit,
                modifier = Modifier.fillMaxWidth().padding(top = Spacing.small),
                style = AppButtonStyle.Secondary,
            )
        }
    }
}

@Composable
private fun hintTexts(uiState: CameraUiState): Pair<String, String> {
    val objectName = stringResource(ObjectVisual.of(uiState.objectToFind).nameRes)
    return when {
        uiState.isRecognized ->
            stringResource(R.string.camera_hint_recognized) to stringResource(R.string.camera_hint_recognized_detail)
        uiState.photoRejected ->
            stringResource(R.string.camera_photo_rejected) to stringResource(R.string.camera_photo_rejected_detail)
        !uiState.hasFrame ->
            stringResource(R.string.camera_starting) to stringResource(R.string.camera_starting_detail)
        else -> when (uiState.hint) {
            RecognitionHint.NotVisible ->
                stringResource(R.string.camera_hint_not_visible) to
                    stringResource(R.string.camera_hint_not_visible_detail)
            RecognitionHint.WrongObject ->
                stringResource(R.string.camera_hint_wrong_object) to
                    stringResource(R.string.camera_hint_wrong_object_detail, objectName)
            RecognitionHint.MoveCloser ->
                stringResource(R.string.camera_hint_move_closer) to
                    stringResource(R.string.camera_hint_move_closer_detail)
            RecognitionHint.HoldStill ->
                stringResource(R.string.camera_hint_hold) to stringResource(R.string.camera_hint_hold_detail)
            RecognitionHint.Recognized ->
                stringResource(R.string.camera_hint_recognized) to
                    stringResource(R.string.camera_hint_recognized_detail)
        }
    }
}

/** Ekran bez zgody na kamerę: wyjaśnienie, po co jest kamera, i przycisk do zgody albo do ustawień telefonu. */
@Composable
fun CameraPermissionScreen(
    objectToFind: RequiredObject,
    access: CameraAccess,
    onBack: () -> Unit,
    onRequestAccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = Spacing.screenEdge, vertical = Spacing.medium),
    ) {
        CameraTopBar(objectToFind = objectToFind, onBack = onBack)
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            // Gdy systemowe okno z pytaniem jest na ekranie, pod spodem nic nie pokazujemy.
            if (access != CameraAccess.Asking) {
                AppCard(Modifier.fillMaxWidth().entranceFrom(rememberEntrance(order = 1))) {
                    Text(stringResource(R.string.camera_permission_title), style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = stringResource(
                            if (access == CameraAccess.Blocked) {
                                R.string.camera_permission_blocked
                            } else {
                                R.string.camera_permission_text
                            },
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                    )
                    AppButton(
                        text = stringResource(
                            if (access == CameraAccess.Blocked) {
                                R.string.permission_action_settings
                            } else {
                                R.string.camera_permission_allow
                            },
                        ),
                        onClick = onRequestAccess,
                        modifier = Modifier.fillMaxWidth().padding(top = Spacing.small),
                        icon = R.drawable.ic_camera,
                    )
                }
            }
        }
    }
}

/** W podglądach zamiast obrazu z kamery jest jednolite tło. */
@Composable
private fun FakePreview() {
    Box(Modifier.fillMaxSize().background(SurfaceColor))
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun CameraScreenMoveCloserPreview() {
    ShotClockTheme {
        CameraScreen(
            uiState = CameraUiState(
                objectToFind = RequiredObject.FIRST,
                confidence = 0.62f,
                hint = RecognitionHint.MoveCloser,
                hasFrame = true,
            ),
            onBack = {},
            onTakePhoto = {},
            preview = { FakePreview() },
        )
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun CameraScreenEmergencyPreview() {
    ShotClockTheme {
        CameraScreen(
            uiState = CameraUiState(
                objectToFind = RequiredObject.FIRST,
                confidence = 0.12f,
                hint = RecognitionHint.NotVisible,
                hasFrame = true,
            ),
            onBack = {},
            onTakePhoto = {},
            emergencyExitAvailable = true,
            preview = { FakePreview() },
        )
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun CameraScreenRecognizedPreview() {
    ShotClockTheme {
        CameraScreen(
            uiState = CameraUiState(
                objectToFind = RequiredObject.SECOND,
                confidence = 0.94f,
                hint = RecognitionHint.Recognized,
                hasFrame = true,
            ),
            onBack = {},
            onTakePhoto = {},
            preview = { FakePreview() },
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun CameraPermissionScreenPreview() {
    ShotClockTheme {
        Box(Modifier.background(CameraBackground)) {
            CameraPermissionScreen(
                objectToFind = RequiredObject.FIRST,
                access = CameraAccess.Denied,
                onBack = {},
                onRequestAccess = {},
            )
        }
    }
}
