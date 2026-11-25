package pl.shotclock.budzik.data

/**
 * Przedmiot, który trzeba sfotografować, żeby wyłączyć alarm.
 *
 * [modelLabel] to nazwa klasy w pliku labels.txt od zespołu AI (kontrakt opisany w CLAUDE.md).
 * Losowy przedmiot nie ma własnej etykiety, bo konkretny wybierzemy dopiero wtedy, gdy alarm zadzwoni.
 *
 * Room zapisuje w bazie nazwę stałej (np. "FIRST"), więc nie zmieniamy tych nazw.
 * Jak przedmiot wygląda w aplikacji (nazwa, ilustracja, kolor), opisuje ui/objects/ObjectVisual.kt.
 */
enum class RequiredObject(val modelLabel: String?) {
    FIRST("przedmiot_1"),
    SECOND("przedmiot_2"),
    RANDOM(null),
}
