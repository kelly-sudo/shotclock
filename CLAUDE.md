# CLAUDE.md – Inteligentny budzik (projekt „Aplikacje Mobilne”)

## O projekcie

Aplikacja na Androida: budzik, który przestaje dzwonić dopiero wtedy, gdy użytkownik zrobi zdjęcie wskazanego przedmiotu (np. szczoteczki do zębów). Przedmiot rozpoznaje model AI działający lokalnie na telefonie (offline).

Projekt studencki, zespół 4 osób **bez doświadczenia w programowaniu**. Kod musi być prosty, czytelny i zrozumiały dla początkujących, bo zespół będzie go bronił przed prowadzącym.

Podział pracy:
- **Aplikacja (ten kod):** jedna osoba buduje całą aplikację z UI/UX.
- **Model AI:** reszta zespołu zbiera zdjęcia (ok. 500 na przedmiot) i trenuje model. Dostarczą plik `model.tflite` + `labels.txt`.
- Do czasu dostarczenia modelu aplikacja działa na `FakeObjectClassifier`. Integracja = podmiana plików w `assets/`.

Zakres uzgodniony z prowadzącym: **2 przedmioty**.

## Wymagania z kursu (obowiązkowe)

- Kotlin, Android Studio, **minimum API 26**
- Architektura **MVVM**
- Komponent hardware: **kamera** (dodatkowo wibracje i głośnik przy alarmie)
- Trwałe dane: **SQLite** (przez Room)
- Funkcja AI/ML: rozpoznawanie przedmiotu na zdjęciu, lokalnie
- Testy, które można pokazać w dokumentacji
- Demo na fizycznym urządzeniu

## Urządzenia testowe

- Na co dzień: **emulator** Android Studio (kamera emulatora ustawiona na Webcam0, czyli kamerę laptopa).
- Testy na fizycznym urządzeniu: **starszy Samsung** i jeden nowszy Android. Pamiętaj o ograniczeniach starszych telefonów (słabsza wydajność, brak rozmycia tła poniżej API 31, agresywne oszczędzanie baterii w One UI).

## Stos technologiczny

- Kotlin, Jetpack Compose, Material 3 (jako baza, z własnym stylem „glass”)
- Navigation Compose
- ViewModel + StateFlow, Kotlin Coroutines
- Room (SQLite), KSP
- DataStore (ustawienia użytkownika)
- CameraX (podgląd, analiza klatek, zdjęcie)
- TensorFlow Lite / LiteRT (klasyfikacja obrazu)
- AlarmManager (`setAlarmClock`) + powiadomienie z full-screen intent + foreground service do dzwonienia
- Proste ręczne wstrzykiwanie zależności (klasa `AppContainer`), **bez Hilta**
- Opcjonalnie biblioteka Haze do rozmycia tła pod szklanymi panelami (zapytaj przed dodaniem)

Zanim dodasz nową bibliotekę, zapytaj i wyjaśnij, po co jest potrzebna.

## Struktura pakietów

```
pl.shotclock.budzik
├── ui/
│   ├── theme/        kolory, typografia, kształty, komponenty glass
│   ├── components/   GlassCard, GlassButton, GlassToggle, TimeDisplay, ...
│   ├── onboarding/   powitanie + uprawnienia
│   ├── alarms/       lista alarmów
│   ├── edit/         dodawanie/edycja alarmu
│   ├── ring/         ekran dzwonienia
│   ├── camera/       ekran kamery z rozpoznawaniem
│   ├── objects/      przedmioty do rozpoznawania
│   ├── history/      historia i statystyki pobudek
│   └── settings/     ustawienia
├── viewmodel/        jeden ViewModel na ekran
├── data/             Room (encje, DAO, baza), DataStore, repozytoria
├── alarm/            planowanie, receiver, serwis dzwonienia, boot receiver
├── camera/           obsługa CameraX
└── ml/               ObjectClassifier, FakeObjectClassifier, TfliteObjectClassifier
```

## Design: styl „liquid glass”

Nowoczesny, ciemny, półprzezroczysty interfejs inspirowany stylem liquid glass / glassmorphism.

**Zasady wizualne:**
- Tło: ciemny, płynny gradient (np. granat → fiolet → głęboki błękit), lekko animowany na ekranie dzwonienia.
- Panele „szklane”: półprzezroczyste powierzchnie (biel z alfą ok. 8–15%), cienka jasna krawędź (biel z alfą ok. 20–30%), subtelny połysk u góry panelu (gradient od jaśniejszego do przezroczystego).
- Rozmycie tła pod panelami tylko na **API 31+** (RenderEffect / Haze). Na starszych wersjach **fallback**: mocniej kryjący półprzezroczysty panel bez rozmycia. Aplikacja musi wyglądać dobrze w obu przypadkach.
- Duże zaokrąglenia (24–32 dp dla kart, pełne zaokrąglenie dla przycisków), miękkie cienie, dużo przestrzeni.
- Typografia: bardzo duża, cienka cyfra godziny na liście i ekranie dzwonienia; reszta tekstów czytelna, średniej grubości.
- Kolor akcentu jeden (np. jasny turkus lub ciepły pomarańcz), używany oszczędnie: aktywne przełączniki, główne przyciski, sukces rozpoznania.
- Animacje: płynne przejścia między ekranami, sprężyste reakcje przycisków, animacja sukcesu po rozpoznaniu przedmiotu. Bez przesady – animacje nie mogą spowalniać starszego Samsunga.
- Obsługa trybu ciemnego jako domyślnego; tryb jasny opcjonalnie później.

**Komponenty wielokrotnego użytku** (w `ui/components`, wszystkie ekrany z nich korzystają):
`GlassBackground`, `GlassCard`, `GlassButton`, `GlassIconButton`, `GlassToggle`, `GlassChip` (dni tygodnia), `GlassBottomBar`, `TimeDisplay`, `ScreenContainer` (wspólny układ zwykłego ekranu).

**Dostępność:** kontrast tekstu na szkle sprawdzony (tekst główny prawie biały), elementy dotykowe min. 48 dp, opisy `contentDescription` dla ikon.

## Ekrany i UX

1. **Onboarding (pierwsze uruchomienie):** 2–3 karty z wyjaśnieniem, jak działa budzik, potem prośby o uprawnienia z wyjaśnieniem, po co są (powiadomienia, dokładne alarmy, kamera, full-screen intent). Obsługa odmowy z przejściem do ustawień systemu.
2. **Lista alarmów (ekran główny):** karty glass z godziną, dniami tygodnia, etykietą, ikoną wymaganego przedmiotu i przełącznikiem. Na górze informacja „Następny alarm za 7 h 20 min”. Pływający przycisk dodawania. Pusty stan z ilustracją i zachętą do dodania pierwszego alarmu. Przesunięcie karty = usunięcie z możliwością cofnięcia.
3. **Edycja alarmu:** duży wybór godziny, chipy dni tygodnia, etykieta, wybór przedmiotu (przedmiot 1 / przedmiot 2 / losowy), dźwięk, wibracje, stopniowe zwiększanie głośności. Zapis i usuwanie.
4. **Ekran dzwonienia (full screen, także na zablokowanym ekranie):** animowane tło, ogromna godzina, komunikat „Zrób zdjęcie: szczoteczka do zębów” z ikoną przedmiotu, główny przycisk „Otwórz kamerę”. **Brak przycisku drzemki** (to cel aplikacji).
5. **Ekran kamery:** podgląd na pełnym ekranie, szklana ramka-celownik na środku, na dole szklany panel z podpowiedzią. Rozpoznawanie na żywo (analiza klatek co ok. 300–500 ms) z paskiem pewności; gdy pewność przekroczy próg przez ok. 1 sekundę → alarm się wyłącza. Przycisk ręcznego zdjęcia jako alternatywa. Komunikaty: „Nie widzę przedmiotu”, „Przybliż”, „Jeszcze chwilę…”.
6. **Sukces:** animacja potwierdzenia, czas wstania („Wstałeś w 1 min 42 s”), seria dni z rzędu.
7. **Wyjście awaryjne:** po np. 10 nieudanych próbach lub 10 minutach dzwonienia pojawia się opcja „Nie mogę znaleźć przedmiotu” → wyłączenie przez zadanie matematyczne. Zapisywane w historii jako wyłączenie awaryjne.
8. **Przedmioty:** dwie karty przedmiotów, które rozpoznaje model (nazwa, ikona, przykładowe zdjęcie), informacja o wersji modelu.
9. **Historia i statystyki:** lista pobudek (data, czas wyłączenia, liczba prób, rozpoznany przedmiot, pewność), średni czas wstania, seria dni. Prosty wykres z ostatnich 7 dni.
10. **Ustawienia:** domyślny dźwięk, wibracje, próg pewności (tylko w trybie deweloperskim), **przełącznik trybu testowego** (FakeObjectClassifier), informacje o aplikacji.

Nawigacja: dolny pasek glass z zakładkami Alarmy / Historia / Przedmioty / Ustawienia. Ekrany dzwonienia i kamery bez paska.

## Kontrakt z zespołem AI (integracja modelu)

- Pliki: `app/src/main/assets/model.tflite` i `app/src/main/assets/labels.txt` (jedna etykieta na linię, w kolejności wyjść modelu).
- Klasy: `przedmiot_1`, `przedmiot_2`, `inne`. Klasa `inne` jest obowiązkowa – bez niej model „rozpozna” cokolwiek jako przedmiot.
- Model przyjmuje jeden obraz RGB, zwraca prawdopodobieństwa dla każdej klasy.
- `TfliteObjectClassifier` **odczytuje rozmiar wejścia i typ danych (float32 / uint8) z modelu w czasie działania**, zamiast mieć je wpisane na sztywno. Dzięki temu działa z modelem z Teachable Machine, MediaPipe Model Maker lub własnym.
- Mapowanie etykiet na nazwy i ikony przedmiotów w jednym miejscu w kodzie.
- Wynik klasyfikacji: `ClassificationResult(label: String, confidence: Float)` + pełna lista wyników.
- Próg pewności: stała domyślna 0.85, nadpisywana w ustawieniach deweloperskich.

## Kluczowe decyzje techniczne

- **Interfejs `ObjectClassifier`:** reszta aplikacji zna tylko interfejs. `FakeObjectClassifier` (do czasu modelu i w trybie testowym) oraz `TfliteObjectClassifier`.
- **Decyzja o wyłączeniu alarmu** w osobnej, czystej funkcji (bez zależności od Androida), łatwej do przetestowania: wymagany przedmiot + próg + czas utrzymania wyniku.
- **Alarm:** `setAlarmClock` (najbardziej niezawodny), foreground service odtwarzający dźwięk i wibracje, full-screen intent otwierający ekran dzwonienia, ponowne ustawienie alarmów po restarcie (`BOOT_COMPLETED`) i zmianie strefy czasowej.
- **Uprawnienia:** CAMERA, POST_NOTIFICATIONS (13+), SCHEDULE_EXACT_ALARM / USE_EXACT_ALARM, USE_FULL_SCREEN_INTENT (14+), RECEIVE_BOOT_COMPLETED, FOREGROUND_SERVICE (+ odpowiedni typ usługi), VIBRATE. Zawsze sprawdzaj wersję API i obsłuż odmowę.
- **Samsung:** dodaj w onboardingu podpowiedź o wyłączeniu optymalizacji baterii dla aplikacji, jeśli alarmy nie działają.
- Zdjęcia z kamery analizowane w pamięci, **nie zapisujemy ich na dysku** (prywatność).

## Model danych (Room)

- `Alarm`: id, godzina, minuta, dni tygodnia, włączony, etykieta, wymagany przedmiot (konkretny lub losowy), dźwięk, wibracje, stopniowa głośność
- `WakeEvent`: id, id alarmu, czas startu dzwonienia, czas wyłączenia, liczba prób, rozpoznany przedmiot, pewność, czy wyłączenie awaryjne

Ustawienia aplikacji w DataStore, nie w Room.

## Zasady pracy

- **Małe kroki.** Jedno zadanie = jedna funkcjonalność. Po każdym kroku aplikacja musi się kompilować i uruchamiać.
- **Najpierw plan.** Przy większym zadaniu najpierw opisz plan i pliki, które zmienisz, poczekaj na akceptację.
- **Wyjaśniaj.** Po napisaniu kodu krótko wyjaśnij po polsku, co robi i dlaczego tak. Zespół musi rozumieć każdy plik.
- **Komentarze** w kodzie po polsku, tylko tam, gdzie coś nie jest oczywiste. Nazwy klas, funkcji i zmiennych po angielsku.
- **Teksty w UI** po polsku, w `strings.xml`.
- Kolory, odstępy i kształty tylko z `ui/theme`, nigdy wpisane na sztywno w ekranie.
- Każdy komponent UI ma `@Preview`.
- Nie zmieniaj plików niezwiązanych z zadaniem.
- Przy błędach kompilacji pokaż przyczynę, zamiast zgadywać kolejne poprawki.
- Po zakończeniu zadania zaproponuj treść commita i zaktualizuj listę „Plan budowy”.

## Testy

- Testy jednostkowe (JUnit): obliczanie najbliższego terminu alarmu, decyzja o wyłączeniu alarmu, losowanie przedmiotu, statystyki (średni czas, seria dni).
- Testy Room na bazie w pamięci.
- Test `TfliteObjectClassifier` na kilku zdjęciach testowych z `androidTest/assets` (po dostarczeniu modelu).
- Ręczne scenariusze testowe na fizycznym telefonie: alarm przy zablokowanym ekranie, po restarcie, w trybie samolotowym, przy słabym świetle.

## Plan budowy

Realizuj etapami, w tej kolejności. Po każdym etapie zatrzymaj się i poczekaj na sprawdzenie na telefonie.

- [x] **Etap 1 – Fundament:** zależności, struktura pakietów, AppContainer, motyw glass i komponenty z podglądami, nawigacja z dolnym paskiem, ekrany-zaślepki
- [ ] **Etap 2 – Alarmy w bazie:** Room, lista alarmów, edycja alarmu, testy DAO
- [ ] **Etap 3 – Dzwonienie:** AlarmManager, foreground service, dźwięk z narastającą głośnością, wibracje, full-screen intent, ekran dzwonienia, boot receiver
- [ ] **Etap 4 – Kamera:** uprawnienia, CameraX, celownik, analiza klatek z FakeObjectClassifier, decyzja o wyłączeniu + testy
- [ ] **Etap 5 – Pełny scenariusz:** dzwonienie → kamera → sukces, zapis WakeEvent, wyjście awaryjne
- [ ] **Etap 6 – Historia, przedmioty, ustawienia:** statystyki, wykres, DataStore, tryb testowy
- [ ] **Etap 7 – Onboarding i dopracowanie:** uprawnienia z wyjaśnieniem, animacje, puste stany, fallback bez rozmycia na starszym Androidzie
- [ ] **Etap 8 – Integracja modelu:** TfliteObjectClassifier, podmiana plików od zespołu AI, testy na zdjęciach
- [ ] **Etap 9 – Stabilizacja:** testy na Samsungu, poprawki wydajności, przygotowanie do demo
