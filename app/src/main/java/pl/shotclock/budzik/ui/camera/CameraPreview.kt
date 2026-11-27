package pl.shotclock.budzik.ui.camera

import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import pl.shotclock.budzik.camera.CameraSession
import pl.shotclock.budzik.viewmodel.CameraViewModel

/** Tworzy sesję kamery, która przekazuje klatki do [viewModel], i wyłącza ją, gdy ekran zniknie. */
@Composable
fun rememberCameraSession(viewModel: CameraViewModel): CameraSession {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentViewModel = rememberUpdatedState(viewModel)
    val session = remember {
        CameraSession(context) { image, time -> currentViewModel.value.onFrame(image, time) }
    }
    DisposableEffect(session, lifecycleOwner) {
        session.start(lifecycleOwner)
        onDispose { session.stop() }
    }
    return session
}

/**
 * Obraz z kamery na żywo. PreviewView to zwykły widok Androida, więc wstawiamy go do Compose przez AndroidView.
 * Tryb COMPATIBLE rysuje obraz jak zwykły widok, dzięki czemu działa przenikanie między ekranami.
 */
@Composable
fun CameraPreview(session: CameraSession, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_CENTER
                controller = session.controller
            }
        },
        modifier = modifier,
    )
}
