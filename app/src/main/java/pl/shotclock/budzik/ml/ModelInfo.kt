package pl.shotclock.budzik.ml

import android.content.Context

/** Pliki modelu od zespołu AI w folderze app/src/main/assets (kontrakt w CLAUDE.md). */
object ModelFiles {
    const val MODEL = "model.tflite"
    const val LABELS = "labels.txt"

    /**
     * Wersja modelu pokazywana na ekranie „Przedmioty”.
     * Zespół AI zmienia ją przy każdym nowym pliku model.tflite, żeby na demo było widać, który model działa.
     */
    const val VERSION = "1.0"
}

/** Czy prawdziwy model jest w aplikacji i jaka to wersja. */
data class ModelInfo(
    val isAvailable: Boolean,
    val version: String,
) {
    companion object {
        /** Sprawdza, czy w assets są oba pliki modelu. Nie wczytuje modelu, więc jest szybkie. */
        fun read(context: Context): ModelInfo {
            val files = context.assets.list("").orEmpty()
            return ModelInfo(
                isAvailable = ModelFiles.MODEL in files && ModelFiles.LABELS in files,
                version = ModelFiles.VERSION,
            )
        }
    }
}

/** Którego klasyfikatora używa aplikacja. */
enum class ClassifierMode {
    /** Udawany model, bo tak wybrano w ustawieniach. */
    Test,

    /** Udawany model, bo zespół AI nie dostarczył jeszcze pliku model.tflite. */
    NoModel,

    /** Prawdziwy model z assets. */
    Real,
}

/** Czysta funkcja: prawdziwy model działa tylko wtedy, gdy jest w aplikacji i tryb testowy jest wyłączony. */
fun classifierMode(testMode: Boolean, modelAvailable: Boolean): ClassifierMode = when {
    testMode -> ClassifierMode.Test
    !modelAvailable -> ClassifierMode.NoModel
    else -> ClassifierMode.Real
}
