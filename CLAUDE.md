# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew assembleDebug        # Build debug APK
./gradlew assembleRelease      # Build release APK (requires signing env vars)
./gradlew bundleRelease        # Build release AAB
./gradlew test                 # Run unit tests
./gradlew connectedAndroidTest # Run instrumented tests
./gradlew lint                 # Run lint checks
```

Release signing requires env vars: `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.

## Architecture

**Photodrop** — Android photo/video gallery app where users swipe through media (Tinder-style) to decide what to keep or trash.

### Sail — Custom Action-based MVVM

The `arch/` package defines a lightweight state management framework:

- **SailViewModel\<S, E\>** — Base ViewModel holding `StateFlow<S>` and a `Channel<E>` for one-shot events. Exposes `dispatch(action, dispatcher)` to run actions on a given coroutine dispatcher.
- **UiAction\<D, S, E\>** — Pure function receiving dependencies `D` and an `ActionScope` to update state/emit events.
- **ActionScope\<S, E\>** — Provides `setState { copy(...) }` and `emitEvent(...)` inside actions.

**Flow:** UI gesture → ViewModel.dispatch(Action) → Action updates state via ActionScope → StateFlow emits → Composable recomposes.

### Layers

- **data/** — `MediaRepositoryImpl` queries MediaStore (images + videos) via ContentResolver. `TrashManager` (singleton) holds in-memory trash state. `trash/TrashRepository` + `TrashRepositoryImpl` provide Flow-based trash storage with `kotlinx-collections-immutable`.
- **presentation/home/** — `SwipeViewModel` + actions (`LoadMediaAction`, `SwipeLeftAction`, `SwipeRightAction`, `UndoAction`, `ToggleMuteAction`). `SwipeCard` handles drag gestures with animated overlays. `ActionHistory` tracks swipe history for undo.
- **presentation/trash/** — `TrashNode` shows a grid of trashed items with restore/delete-all.
- **presentation/main/** — `MainNode` sets up Navigation3 with `Home` and `Trash` routes (serializable `@NavKey` objects).
- **ui/block/** — `PermissionGate` (media permission check), `VideoPlayer` (ExoPlayer wrapper).

### Navigation

Uses experimental **Navigation3** (`androidx.navigation3`). Routes are `@Serializable` data objects. 
Transitions use slide animations.

### Media Handling

- Images: Coil3 + telephoto (ZoomableAsyncImage for pinch-zoom)
- Videos: Media3 ExoPlayer with Compose integration, mute toggle
- Deletion: `MediaStore.createDeleteRequest` on Android R+, `RecoverableSecurityException` fallback on API 29

### Permissions

`PermissionGate` checks `READ_MEDIA_IMAGES` + `READ_MEDIA_VIDEO` (API 33+) or `READ_EXTERNAL_STORAGE` (API 29–32). Falls back to app settings if permanently denied.

## Key Conventions

- Target: API 29–36, Java 17, Kotlin 2.3.20
- Use `kotlinx-collections-immutable` (`persistentListOf`, etc.) for collections held in reactive state (StateFlow, MutableState)
- Actions are pure functions — all dependencies injected via a `Dependencies` class
- State updates use `copy()` inside `setState {}`
- Version catalog at `gradle/libs.versions.toml`
