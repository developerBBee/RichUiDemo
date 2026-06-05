# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run a single test class
./gradlew test --tests "jp.developer.bbee.richuidemo.ExampleUnitTest"

# Lint check
./gradlew lint
```

## Architecture

Single-module Android app (`app/`) using Jetpack Compose with Material3.

**Key versions** (via `gradle/libs.versions.toml`):
- AGP 9.1.1, Kotlin 2.3.20, Compose BOM 2026.04.01
- Navigation 3 (`androidx.navigation3`) 1.0.0
- media3 (ExoPlayer) 1.6.0
- core-pip 1.0.0-alpha02
- minSdk 23, compileSdk 36, JVM target 17

**Navigation** uses Jetpack Navigation 3, not the older Navigation-Compose:
- Routes are `@Serializable data object` types implementing `NavKey` (`navigation/NavRoutes.kt`)
- `AppNavigation.kt` wires the back stack with `rememberNavBackStack` + `NavDisplay`, and per-route enter/pop transition specs via `NavDisplay.transitionSpec` / `NavDisplay.popTransitionSpec` metadata
- Back stack is mutated directly (`backStack.add(...)`, `backStack.removeLastOrNull()`) — no `NavController`

## Package structure

```
jp.developer.bbee.richuidemo/
├── MainActivity.kt
├── component/          # Reusable composables
├── navigation/         # NavRoutes.kt + AppNavigation.kt
├── screen/             # One file per screen
└── ui/theme/           # Color, Theme, Type
```

## Reusable components (`component/`)

- `AnimatedBorderCard` — rotating sweep-gradient border using `drawWithContent` + `BlendMode.SrcIn` and an infinite rotation animation
- `AnimatedSurfaceCard` — shimmer sweep effect via a translating horizontal gradient drawn with `BlendMode.Plus` (dark) / `BlendMode.Multiply` (light); both duration and interval are configurable
- `BackNavigationIcon` — `IconButton` wrapping `ic_arrow_back`; used across every screen for consistent back navigation
- `BubbleMenuButton` — reusable expandable FAB menu; takes a `List<BubbleMenuItem>` (each with a unique `id`), renders sub-items with spring slide+fade animations; the main FAB icon rotates 45° when expanded
- `CountdownTimerWidget` — countdown timer UI widget used inside `WidgetSamplesScreen`
- `DemoSectionLabel` — simple title + description label used as section headers inside screens
- `MusicPlayerWidget` — music player UI widget used inside `WidgetSamplesScreen`
- `MyScaffold` — minimal `Scaffold` wrapper with title + content slots; used for quick previews and early scaffolding
- `StatsRingWidget` — circular stats ring UI widget used inside `WidgetSamplesScreen`
- `WeatherWidget` — weather card UI widget used inside `WidgetSamplesScreen`

## Screens (`screen/`)

Each screen receives only lambda callbacks (no ViewModel), keeping all navigation decisions in `AppNavigation.kt`. Local UI state is held directly in composables with `remember`.

| Screen file | Route | Description |
|---|---|---|
| `BorderDemoScreen` | `BorderDemoRoute` | Animated rotating gradient border demo |
| `SurfaceDemoScreen` | `SurfaceDemoRoute` | Shimmer sweep surface effect demo |
| `CombinedDemoScreen` | `CombinedDemoRoute` | Border + surface combined effect demo |
| `HorizontalScrollCardsScreen` | `HorizontalScrollCardsRoute` | Snap pager with category chips; holds `selectedCategory` + `pagerState` locally |
| `WidgetSamplesScreen` | `WidgetSamplesRoute` | Music player, stats rings, weather, and countdown timer widgets |
| `DragDropListScreen` | `DragDropListRoute` | Long-press drag-and-drop reorder list with haptic feedback |
| `BubbleMenuScreen` | `BubbleMenuRoute` | Expandable bubble FAB menu overlay; uses `BubbleMenuButton` component |
| `PictureInPictureScreen` | `PictureInPictureRoute` | Draggable & resizable floating PiP overlay; uses `BoxWithConstraints` for bounds clamping, hoists `animateDpAsState` at screen level so boundary checks use the rendered animated height |
| `HideNavOnScrollScreen` | `HideNavOnScrollRoute` | Bottom nav bar auto-hides on downward scroll and reappears on upward scroll via `NestedScrollConnection`; visibility driven by `AnimatedVisibility` + `slideInVertically/slideOutVertically` |
| `SwipeableDualLayoutScreen` | `SwipeableDualLayoutRoute` | Hero image + swipeable bottom-sheet detail panel via `AnchoredDraggableState`; hero content fades as the sheet rises; anchors updated in `SideEffect` inside `BoxWithConstraints` |
| `ImageSelectionOverlayScreen` | `ImageSelectionOverlayRoute` | Pick an image via `PickVisualMedia`, draw a draggable + corner-resizable selection rect on a Canvas overlay, save the cropped region to MediaStore (handles API 28 vs Q+ storage paths) |
| `VideoPlayerScreen` | `VideoPlayerRoute` | ExoPlayer-backed player; portrait = Scaffold layout with source selector (URL or local file picker); landscape = immersive full-screen with auto-hiding `VideoControls`; hides/shows system bars based on orientation |
| `SimpleVideoPlayerScreen` | `SimpleVideoPlayerRoute` | Always-immersive full-screen ExoPlayer player (hardcoded Big Buck Bunny URL); saves position and play state with `rememberSaveable`; auto-hides controls after 3 s, timer reset pauses during seek |

## PiP overlay (`screen/PipOverlay.kt`)

`PipState` is a `@Stable` class that hoists the overlay's position (`offsetX`, `offsetY`), dimensions (`pipWidth`, `pipHeight`), and visibility. `PipOverlay` is a composable that reads `WindowInsets` for bounds clamping and renders a `PipWindow` with a drag handle header and a bottom-right resize handle. `PipContent` fills the native Android PiP window when the activity enters system PiP mode.

## Adding a new demo screen

1. Add a `@Serializable data object XxxRoute : NavKey` in `NavRoutes.kt`
2. Add an `entry<XxxRoute>(metadata = ...)` block in `AppNavigation.kt` with transition specs
3. Wire the navigation callback from `HomeScreen` through `AppNavigation`
4. Add a demo card to `HomeScreen` using the existing `DemoSectionCard` + component pattern

## Key conventions

- **No ViewModels** — all state lives in composables via `remember`/`rememberSaveable`
- **Back navigation** — always use `BackNavigationIcon` component in `TopAppBar.navigationIcon`
- **System bars** — `VideoPlayerScreen` and `SimpleVideoPlayerScreen` hide system bars via `WindowInsetsControllerCompat`; always restore them in `onDispose`
- **Insets-aware overlays** — use `WindowInsets.statusBars` / `WindowInsets.navigationBars` for bounds clamping (see `PipOverlay`)
- **ExoPlayer** — released in `DisposableEffect.onDispose`; always set `useController = false` and provide a custom controls composable
- **Photo/video access** — use `PickVisualMedia` / `GetContent` launchers; handle API 28 vs Q+ storage separately in save helpers
