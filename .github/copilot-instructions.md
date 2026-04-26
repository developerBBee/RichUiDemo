# GitHub Copilot Instructions — RichUiDemo

This is a single-module Android app written in Kotlin with Jetpack Compose (Material3).
Apply the guidelines below when reviewing pull requests.

---

## Project Structure

```
app/src/main/java/jp/developer/bbee/richuidemo/
├── component/          # Reusable animated UI components (AnimatedBorderCard, AnimatedSurfaceCard, DemoSectionLabel)
├── navigation/         # NavRoutes.kt (@Serializable NavKey routes), AppNavigation.kt (NavDisplay wiring)
├── screen/             # Individual demo screens — each receives only lambda callbacks, no ViewModel
├── ui/theme/           # Material3 theme (Color.kt, Theme.kt, Type.kt)
└── MainActivity.kt     # Single activity; hosts AppNavigation
```

## Intentional Patterns — Do NOT Flag These

- **`BlendMode.SrcIn`** in `AnimatedBorderCard`: intentional sweep-gradient mask.
- **`BlendMode.Plus` / `BlendMode.Multiply`** in `AnimatedSurfaceCard`: intentional shimmer effect that adapts to dark/light theme.
- **Direct back stack mutation** (`backStack.add(...)`, `backStack.removeLastOrNull()`): this is the correct Navigation 3 API — there is no `NavController`.
- **`drawWithContent { ... }`** blocks in animated components: required for the custom blend-mode drawing pipeline.
- **`InfiniteTransition`** in animated components: expected; verify only that it is cancelled properly when the composable leaves composition.

## Always Flag

- Any import or usage of `NavController`, `NavHostController`, or `rememberNavController` — this project uses Navigation 3, not Navigation-Compose.
- Any import of `androidx.lifecycle.viewmodel.*` or `dagger.hilt.*` — this project has no ViewModel or Hilt.
- Hard-coded `Color(0xFF...)` values inside composables that are not in `ui/theme/Color.kt`.
- Hard-coded string literals in composables instead of `stringResource(...)`.
- Missing `@Preview` on new public composables.
- New screens that are not wired in both `NavRoutes.kt` and `AppNavigation.kt`.

## Stack & Key Versions

- AGP 9.1.1 / Kotlin 2.3.20 / Compose BOM 2026.04.01
- Navigation 3 (`androidx.navigation3`) 1.0.0 — **not** the older Navigation-Compose
- minSdk 23 / compileSdk 36 / JVM target 17
- No ViewModel, no Hilt, no Room — screens are stateless composables wired via lambdas

---

## Kotlin

### Idioms & Style
- Prefer `val` over `var`; flag unnecessary mutability.
- Use expression bodies for single-expression functions.
- Prefer data classes over plain classes for value objects.
- Use named arguments when a call has multiple parameters of the same type.
- Avoid `!!` — use `?.let`, `?: return`, or require() with a message instead.
- Prefer `when` over long `if-else if` chains; ensure `when` on sealed types is exhaustive.
- Use `object` declarations for singletons; avoid companion objects that only hold constants (use top-level instead).
- Avoid redundant `return` in lambdas; use `also`, `apply`, `let`, `run`, `with` purposefully.
- Prefer Kotlin standard library functions (`filter`, `map`, `firstOrNull`, etc.) over manual loops when they improve readability.

### Coroutines & Flow (if introduced)
- Use `viewModelScope` or a scoped `CoroutineScope`; never `GlobalScope`.
- Prefer `StateFlow` / `SharedFlow` over `LiveData` for new code.
- Collect flows inside `LaunchedEffect` or `collectAsStateWithLifecycle`.
- Avoid blocking calls (`runBlocking`, `Thread.sleep`) on the main thread.

### Serialization
- Routes use `@Serializable data object` implementing `NavKey`; verify new routes follow this convention.

---

## Jetpack Compose

### Composable Naming & Structure
- Composable functions must be PascalCase and be a noun or noun phrase.
- Non-composable functions returning `Unit` must **not** be PascalCase.
- Every composable that accepts a `Modifier` must expose it as the second parameter with a default of `Modifier`.
- Prefer `modifier = modifier` (pass-through) over creating an internal chain that discards the caller's modifier.

### State & Recomposition
- Apply **state hoisting** — stateless leaf composables, state owned as high as needed.
- Use `remember` for expensive computations; use `rememberSaveable` only when surviveprocess death is required.
- Use `derivedStateOf` when derived state depends on another state to avoid redundant recompositions.
- Avoid reading state inside a composable that triggers frequent recompositions without wrapping in `remember { derivedStateOf { ... } }`.
- Flag inline lambda captures of unstable types without `remember` — they can cause unnecessary recompositions.
- Mark data classes passed to composables as `@Stable` or `@Immutable` when appropriate.

### Side Effects
- Use the correct effect API:
  - `LaunchedEffect(key)` — coroutine scoped to composition, re-launched on key change.
  - `SideEffect` — synchronize Compose state to non-Compose code after every recomposition.
  - `DisposableEffect(key)` — setup/teardown with `onDispose`.
  - `rememberCoroutineScope` — for coroutines triggered by events (e.g., button clicks).
- Never start a coroutine directly inside a composable body without one of the above.

### Modifiers
- Order modifiers semantically: `size`/`fillMax*` → `padding` → `background`/`border` → `clickable`/interaction.
- Avoid duplicate or conflicting modifier calls (e.g., two `.padding()` calls without intention).
- Prefer `Modifier.clickable` + `semantics` over `Modifier.pointerInput` for simple tap handling.

### Animation
- This project uses custom `BlendMode`-based drawing and `InfiniteTransition` for `AnimatedBorderCard` and `AnimatedSurfaceCard`; changes to these must preserve the drawing order and blend mode semantics.
- Infinite animations should be stopped when the composable leaves the composition (check `isActive` or use `DisposableEffect`).

### Previews
- Add `@Preview` (and `@PreviewLightDark` where meaningful) to every new composable.
- Previews must not depend on real data sources; use hard-coded preview data.

### Accessibility
- Interactive elements must have a content description or `semantics { }` block.
- Ensure touch target size meets the 48 dp minimum (use `Modifier.minimumInteractiveComponentSize()` or explicit padding).

---

## Navigation (Navigation 3)

- Routes are `@Serializable data object XxxRoute : NavKey` in `navigation/NavRoutes.kt`.
- Back stack is mutated directly: `backStack.add(route)` / `backStack.removeLastOrNull()` — **do not** introduce `NavController`.
- Transition specs are declared per-route via `NavDisplay.transitionSpec` / `NavDisplay.popTransitionSpec` metadata in `AppNavigation.kt`.
- Screens receive navigation via **lambda callbacks only** — no route object or nav host reference leaks into screen composables.

---

## Android & Material3

### Theming
- Use Material3 tokens (`MaterialTheme.colorScheme.*`, `MaterialTheme.typography.*`) instead of hard-coded colors or text styles.
- Support both light and dark themes; avoid hard-coded `Color(0xFF...)` in composables (use theme tokens or the existing `Color.kt`).

### Resources
- Prefer vector drawables and `painterResource` / `ImageVector` over bitmap assets.
- Use `stringResource` for all user-visible strings (no hard-coded strings in composables).

### Performance
- Avoid allocations inside composable bodies that run every recomposition (e.g., `listOf(...)`, `Regex(...)`).
- Use `key(item.id) { ... }` inside `LazyColumn`/`LazyRow` to preserve item identity.
- Prefer `LazyColumn`/`LazyRow` over `Column`/`Row` + `verticalScroll` for unbounded lists.

### Lifecycle & Memory
- Do not hold references to `Context`, `Activity`, or `View` in long-lived objects.
- Flag any usage of `LocalContext.current` passed into a lambda that outlives the composition.

---

## Code Quality

- No unused imports, variables, or parameters (use `_` for unused lambda params).
- No `TODO`/`FIXME` left in PR-ready code without an associated issue reference.
- Test coverage: new public composables should have at least one screenshot or semantic test.
- Build must pass `./gradlew assembleDebug lint test` without errors or new warnings.
