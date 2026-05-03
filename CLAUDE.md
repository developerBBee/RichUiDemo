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
- minSdk 23, compileSdk 36, JVM target 17

**Navigation** uses Jetpack Navigation 3, not the older Navigation-Compose:
- Routes are `@Serializable data object` types implementing `NavKey` (`navigation/NavRoutes.kt`)
- `AppNavigation.kt` wires the back stack with `rememberNavBackStack` + `NavDisplay`, and per-route enter/pop transition specs via `NavDisplay.transitionSpec` / `NavDisplay.popTransitionSpec` metadata
- Back stack is mutated directly (`backStack.add(...)`, `backStack.removeLastOrNull()`) — no `NavController`

**Reusable animation components** (`component/`):
- `AnimatedBorderCard` — rotating sweep-gradient border using `drawWithContent` + `BlendMode.SrcIn` and an infinite rotation animation
- `AnimatedSurfaceCard` — shimmer sweep effect via a translating horizontal gradient drawn with `BlendMode.Plus` (dark) / `BlendMode.Multiply` (light); both duration and interval are configurable
- `DemoSectionLabel` — simple title + description label used as section headers inside screens

**Screens** (`screen/`): Each screen receives only lambda callbacks (no ViewModel), keeping all navigation decisions in `AppNavigation.kt`. `HorizontalScrollCardsScreen` holds local UI state (`selectedCategory`, `pagerState`) directly in the composable.

Current screens:
- `BorderDemoScreen` — animated rotating gradient border demo
- `SurfaceDemoScreen` — shimmer sweep surface effect demo
- `CombinedDemoScreen` — border + surface combined effect demo
- `HorizontalScrollCardsScreen` — snap pager with category chips
- `WidgetSamplesScreen` — music player, stats rings, weather, countdown timer widgets
- `DragDropListScreen` — long-press drag-and-drop reorder list with haptic feedback
- `BubbleMenuScreen` — expandable bubble menu overlay
- `PictureInPictureScreen` — draggable & resizable floating PiP overlay; uses `BoxWithConstraints` for bounds clamping, hoists `animateDpAsState` at screen level so boundary checks use the rendered animated height

## Adding a new demo screen

1. Add a `@Serializable data object XxxRoute : NavKey` in `NavRoutes.kt`
2. Add an `entry<XxxRoute>(metadata = ...)` block in `AppNavigation.kt` with transition specs
3. Wire the navigation callback from `HomeScreen` through `AppNavigation`
4. Add a demo card to `HomeScreen` using the existing `DemoSectionCard` + component pattern
