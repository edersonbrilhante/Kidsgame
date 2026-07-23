# Kids Puzzle — a native Android game for ages 3–4

A tiny, offline, ad-free Android game built as an **extensible minigame platform**. The first
minigame is a **jigsaw puzzle** made from a photo you import (or a built-in sample picture).
Adding a new minigame later means implementing one interface and adding one line to a registry.

- **Language / UI:** Kotlin + Jetpack Compose (Material 3)
- **Min SDK 26, target SDK 35**, single-activity, state-based navigation
- **No internet, no ads, no analytics, no in-app purchases, zero runtime permissions**
- Photo import uses the Android Photo Picker (no storage permission), stored in app-internal
  storage only. "Grown-up" actions (import a picture) sit behind a simple **parental gate**.

> Heads-up: this project was generated as source and has **not been compiled** by the author.
> It's structured to build cleanly, but when you first open it, Android Studio may prompt you
> to update the Android Gradle Plugin / Gradle version (click **Sync / Update**), and you may
> need to fix a stray import. That's normal for hand-authored Compose projects.

---

## Three ways to build it

You only need **one** of these. All three need internet (to download the SDK + dependencies).

### 1. Android Studio (easiest, also lets you use an emulator)
1. Install Android Studio (free).
2. **File → Open** this folder. Let it sync (accept any AGP/Gradle upgrade prompt).
3. Press **Run ▶**. Choose the built-in emulator, or plug in your Android phone with
   **USB debugging** enabled (Settings → About phone → tap "Build number" 7× → Developer
   options → USB debugging).

### 2. Docker (build the APK in a container, no Android Studio)
Requires Docker with internet access.
```bash
./docker-build.sh
```
The APK is written to `./out/app-debug.apk`. Copy it to an Android phone and tap to install
(you'll need to allow "install unknown apps"). If a download 404s, bump the `ARG` versions at
the top of the `Dockerfile`.

### 3. GitHub Actions (build in the cloud, download the file)
Push this project to a GitHub repo. The workflow in `.github/workflows/build.yml` runs on every
push to `main`, builds the debug APK on a hosted runner, and uploads it. Download it from the
run's **Artifacts** section (`app-debug`). You can also trigger it manually via
**Actions → Build APK → Run workflow**.

---

## Installing the APK on your phone
1. Transfer `app-debug.apk` to the phone (USB, email to yourself, cloud drive).
2. Tap it. Android will ask permission to "install unknown apps" from that source — allow it.
3. Open **Kids Puzzle**.

The debug APK is fine for your own family. To publish on Google Play you'll need a **signed
release** build (`assembleRelease` + a signing key) and a Play Console account — a separate,
multi-week process.

---

## How to add a new minigame

The whole point of the structure. Three steps:

1. Create a new package under `feature/`, e.g. `feature/memorymatch/`.
2. Implement the `MiniGame` interface (see `feature/tapballoon/TapBalloonMiniGame.kt` for the
   smallest possible example):
   ```kotlin
   class MemoryMatchMiniGame : MiniGame {
       override val info = MiniGameInfo(
           id = "memorymatch",
           titleRes = R.string.game_memorymatch,
           iconRes = R.drawable.ic_memory,
       )
       @Composable
       override fun Screen(services: GameServices, onExit: () -> Unit) { /* your UI */ }
   }
   ```
3. Add one line to `framework/MiniGameRegistry.kt`:
   ```kotlin
   val games = listOf(JigsawMiniGame(), TapBalloonMiniGame(), MemoryMatchMiniGame())
   ```
It now appears automatically on the home carousel. Your game gets `services.audio`,
`services.imageStore`, `services.settings`, and `services.celebrate()` for free.

---

## Project layout
```
app/src/main/java/com/example/kidsgames/
  MainActivity.kt
  framework/     MiniGame, MiniGameInfo, MiniGameRegistry, GameServices, HomeScreen, AppRoot
  core/          AudioService, ImageStore, SettingsRepository, ParentalGate
  feature/jigsaw/      PuzzleLogic, JigsawScreen, JigsawMiniGame
  feature/tapballoon/  TapBalloonMiniGame  (stub proving extensibility)
  ui/theme/      Color, Theme
```
