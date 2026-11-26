package pl.shotclock.budzik.alarm

import kotlin.random.Random
import pl.shotclock.budzik.data.RequiredObject

/** Przedmioty, które model naprawdę rozpoznaje (wszystkie poza „losowym”). */
val concreteObjects: List<RequiredObject> = RequiredObject.entries.filter { it.modelLabel != null }

/**
 * Przedmiot do sfotografowania, gdy alarm zaczyna dzwonić.
 * Przy „losowym” wybieramy jeden z konkretnych. [random] podajemy w testach, żeby wynik był powtarzalny.
 */
fun pickObjectToFind(required: RequiredObject, random: Random = Random.Default): RequiredObject =
    if (required == RequiredObject.RANDOM) concreteObjects.random(random) else required
