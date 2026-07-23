# Kids Puzzle — Project Summary / Handoff

## What it is
A native **Android** game for a 3–4 year old, built as an **extensible minigame platform**.
First minigame is a **jigsaw puzzle** built from an imported photo (or a built-in sample).
Adding a new minigame = implement one interface + add one line to a registry.

## Stack & hard constraints
- Kotlin + Jetpack Compose (Material 3), single-activity, state-based navigation.
- `minSdk 26`, `targetSdk 35`, `compileSdk 35`. AGP 8.6.1, Kotlin 2.0.20, Gradle 8.9.
- Fully **offline**: no network, no ads, no analytics, no IAP, **zero runtime permissions**.
- Photo import via Android Photo Picker (`PickVisualMedia`), stored in app-internal storage only.
- "Grown-up" actions (photo import) gated behind a simple **parental gate** (tap the 7).
- Designed toward Google Play Families policy; no fail states, timers, or scores.

## Architecture (the important part)

```
framework/   MiniGame (interface), MiniGameInfo, MiniGameRegistry, GameServices,
             HomeScreen, AppRoot, KidUi (shared playful UI components)
core/        AudioService (ToneGenerator + haptics), SpeechService (TextToSpeech,
             EN/PL/PT), ImageStore (internal), SettingsRepository (DataStore), ParentalGate
feature/jigsaw/      PuzzleLogic (slice + themed sample pictures), JigsawScreen
                     (drag/snap/win + picture picker), JigsawMiniGame
feature/tapballoon/  TapBalloonMiniGame  (deliberate stub proving the plug-in model)
ui/theme/    Color, Theme (KidsShapes + KidsTypography)
MainActivity.kt
```

- `MiniGame` exposes `info: MiniGameInfo` + `@Composable Screen(services, onExit)`.
- `MiniGameInfo` carries a `gradient: List<Color>` so each home card gets its own color.
- `MiniGameRegistry.games` is the single source of truth; `HomeScreen` renders the carousel
  dynamically from it.
- `GameServices` injects shared audio / imageStore / settings / `celebrate()` into every game.

### Adding a minigame
1. New package under `feature/`.
2. Implement `MiniGame` (copy `feature/tapballoon` as the smallest template). Pick a
   `gradient` for its card.
3. Add one line to `MiniGameRegistry.games`. It appears on the home carousel automatically.

## UI / design system (added in the layout pass)
- `ui/theme/Theme.kt` defines `KidsShapes` (big rounded corners) and `KidsTypography`
  (bold, oversized scale). `ui/theme/Color.kt` holds the full playful palette.
- `framework/KidUi.kt` provides the shared, reusable widgets — **use these instead of raw
  Material `Button`s**:
  - `KidBackground { }` — soft vertical-gradient page background.
  - `KidButton(...)` — chunky rounded button with a press-spring animation.
  - `KidCircleButton(onClick, glyph, ...)` — round emoji/glyph button (back, photo, picker).
- Home cards, the jigsaw top bar / win overlay, and the balloon game all use these.

## Themed content (child's interests: soccer, dragons, super-heroes)
- `PuzzleLogic.samples` is a list of built-in puzzle pictures drawn programmatically with
  `Canvas` (keeps the app asset-free): **soccer ball, friendly dragon, red web-hero,
  big green hero, and the original house scene**. The jigsaw screen shows an emoji picker
  row to switch between them; no photo import needed.
- The balloon game pops a rotating set of themed glyphs (balloon / soccer / star / dragon / hero).
- Art is **original, stylized interpretations**, not copies of trademarked character designs.

## Language learning (child is a pre-reader in Poland; parents speak Portuguese)
- `core/SpeechService.kt` wraps Android `TextToSpeech` and can speak a sequence of words,
  each in its own locale (English → Polish → Portuguese). Injected via `GameServices.speech`.
- Each `PuzzleLogic.SamplePicture` carries `en` / `pl` / `pt` names. Picking a picture speaks
  the word in all three languages; tappable flag chips (🇬🇧 / 🇵🇱 / 🇧🇷) replay one language.
  (Portuguese defaults to `pt-BR`; change to `pt-PT` in `JigsawScreen` if preferred.)
- The manifest declares a `<queries>` block for `TTS_SERVICE` (required on Android 11+).
- **Next:** dedicated Numbers (1–10) and Letters (A–Z) minigames using the same speak-aloud
  pattern; short spoken phrases ("Well done!", "Where is the dog?").

## Build & run (all need internet)
- **Android Studio:** Open folder → Sync (accept any AGP/Gradle upgrade) → Run on emulator or
  USB-debugging phone.
- **Docker:** `./docker-build.sh` → APK at `./out/app-debug.apk`. Bump `ARG` versions in the
  `Dockerfile` if a download 404s.
- **GitHub Actions:** `.github/workflows/build.yml` builds on push to `main`/`master` (and manual
  dispatch); download `app-debug.apk` from the run's Artifacts.

## Current status & known caveats
- **Generated as source, NOT compiled in this environment** (no Android SDK / Gradle here).
  Treat UI changes as reviewed-by-eye; first open in Android Studio may need a one-click
  Gradle-plugin update.
- No gradle-wrapper.jar committed. Docker installs Gradle directly; CI uses
  `gradle/actions/setup-gradle`. Android Studio regenerates the wrapper on open.
- No bundled audio assets — feedback uses `ToneGenerator` + vibration. Swap in SoundPool for polish.
- Puzzle state is in-memory; activity is locked to portrait for the MVP.
- Debug APK only. Release signing for Play Store is not set up yet.

## Suggested next steps
1. Add a signed **release** build (`assembleRelease` + keystore) and a release CI job.
2. Real sound effects (SoundPool) and a confetti particle celebration.
3. Persist per-picture puzzle progress; save/restore across rotation.
4. More minigames (memory match, shape sorter) to exercise the framework.
5. Optional: piece-shaped (interlocking) jigsaw cuts instead of square tiles.
