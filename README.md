# Matteo Games — a native Android learning game for ages 3–4

A tiny, **offline, ad-free** Android app built as an **extensible minigame platform**, with a
trilingual (**English / Polish / Portuguese**) speak-aloud layer so a pre-reader learns first
words while playing. Adding a new minigame means implementing one interface and adding one line
to a registry.

- **Language / UI:** Kotlin + Jetpack Compose (Material 3), single-activity, state-based navigation
- **Min SDK 26, target SDK 35**
- **No internet, no ads, no analytics, no in-app purchases, zero runtime permissions**
- Photo import uses the Android Photo Picker (no storage permission), stored in app-internal
  storage only. "Grown-up" actions (import a picture) sit behind a simple **parental gate** (tap the 7).
- Designed toward Google Play "Families" expectations: no fail states, timers, or scores.

> Heads-up: this project is authored as source. On first open, Android Studio may prompt you to
> sync/update the Android Gradle Plugin — that's normal. CI builds it clean on every push.

---

## The games

All eight appear on the home carousel; every card is a big colored tile with an emoji.

| Game | What it does |
|------|--------------|
| 🧩 **Puzzle** | Drag emoji-picture pieces into place. Pick from ~170 built-in pictures or import a photo (behind the parental gate). Picking a picture speaks its name in all three languages. |
| 🔢 **Numbers** | 1–10 with that many objects; hear each number per language. Arrows wait for the audio to finish. |
| 🔤 **Letters** | Choose a language; the alphabet and words follow it (letter → a random word starting with it, shown in that language), with the other two languages as tap-to-hear chips. |
| 🔟 **Counting** | "How many?" — count the objects and tap the right number; it says the number and pauses. |
| 🎨 **Colors & Shapes** | Hear a color or shape (one random language per round) and tap the matching tile. |
| 🧠 **Memory Match** | Find the pairs; choose 3 / 4 / 6 pairs. Flipping a card speaks the word. |
| 🔎 **Find It** | Hear a word in a rotating language and tap the matching picture. |
| 🎈 **Pop the Balloons** | Tap the drifting balloon/soccer/star/dragon glyphs. |

The trilingual vocabulary lives in `feature/jigsaw/Vocab.kt` (~170 words: animals, body parts,
food, objects, vehicles, nature) — each word is automatically a puzzle picture, a Find-It option,
and a spoken flashcard. Speech uses the device Text-to-Speech engine (English = en-GB for clarity).

---

## Build & run

You only need **one** of these. All need internet to download the SDK + dependencies.

### 1. Android Studio (easiest; includes an emulator)
1. Install Android Studio.
2. **File → Open** this folder. Let it sync (accept any AGP/Gradle upgrade prompt).
3. Press **Run ▶** on an emulator or a USB-debugging phone.

### 2. Docker (build the APK in a container)
```bash
./docker-build.sh
```
The APK is written to `./out/app-debug.apk`. If a download 404s, bump the `ARG` versions in the `Dockerfile`.

### 3. GitHub Actions (build in the cloud)
`.github/workflows/build.yml` runs on every push to `main` (and manual dispatch):
1. Builds the debug APK.
2. On success, **publishes it as a GitHub Release** (`MatteoGames-v1.0.<run>.apk`) and prunes older releases.

The build signs with a **stable committed debug keystore** and sets `versionCode` from the run
number, so each new release **installs over the previous one** (upgrade in place).

---

## Installing on your phone

1. Download the latest APK from the repo's **Releases** page.
2. Tap it; allow "install unknown apps" from that source.
3. **First install only:** if an older, differently-signed build is present, uninstall it once
   (`adb uninstall com.example.kidsgames` or long-press → Uninstall). From then on, updates
   install in place.

Sideloaded apps trigger a Play Protect prompt — tap **More details → Install anyway**. Removing
that warning entirely requires publishing through Google Play (a signed release + Play Console).

---

## How to add a new minigame

1. Create a package under `feature/`, e.g. `feature/shapes/`.
2. Implement the `MiniGame` interface (copy `feature/tapballoon` as the smallest template):
   ```kotlin
   class ShapesMiniGame : MiniGame {
       override val info = MiniGameInfo(
           id = "shapes",
           titleRes = R.string.game_shapes,
           emoji = "🔺",                 // shown on the home card
           gradient = listOf(SkyBlue, Grape),
       )
       @Composable
       override fun Screen(services: GameServices, onExit: () -> Unit) { /* your UI */ }
   }
   ```
3. Add one line to `framework/MiniGameRegistry.kt` and a title string.

It appears on the home carousel automatically and gets `services.audio`, `services.speech`,
`services.imageStore`, `services.settings`, and `services.celebrate()` for free. Use the shared
`KidScreen`, `KidButton`, and `KidCircleButton` from `framework/KidUi.kt`.

---

## Project layout
```
app/src/main/java/com/example/kidsgames/
  MainActivity.kt
  framework/   MiniGame, MiniGameInfo, MiniGameRegistry, GameServices, HomeScreen, AppRoot, KidUi
  core/        AudioService, SpeechService (EN/PL/PT TTS), Trilingual, ImageStore,
               SettingsRepository, ParentalGate
  feature/jigsaw/      PuzzleLogic (emoji pictures), Vocab (trilingual words), JigsawScreen
  feature/numbers/     NumbersMiniGame
  feature/letters/     LettersMiniGame
  feature/counting/    CountingMiniGame
  feature/colors/      ColorsShapesMiniGame
  feature/memory/      MemoryMatchMiniGame
  feature/findit/      FindItMiniGame
  feature/tapballoon/  TapBalloonMiniGame
  ui/theme/    Color, Theme (KidsShapes + KidsTypography)
```
