# GitHub Copilot Instructions — RichUiDemo

This is a single-module Android app written in Kotlin with Jetpack Compose (Material3).
Apply the guidelines below when reviewing pull requests.

---

## Review Scope

**Only flag issues in the following categories:**

1. **Functional bugs** — code that produces incorrect behavior at runtime (wrong logic, incorrect state management, missing lifecycle handling, etc.)
2. **Misleading names or comments** — identifiers, comments, or documentation that contradict what the code actually does
3. **Deprecated or removed APIs** — usage of APIs that are deprecated or removed in the versions used by this project
4. **Dead code** — unused imports, unreachable code, unused variables/parameters, unused private declarations

**Do NOT flag** style preferences, naming conventions, localization (stringResource), missing @Preview annotations, hard-coded colors, test coverage, or any issue that does not fall into the four categories above.

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
- **Hard-coded strings and colors in composables**: intentional for this demo app — do not flag.
- **Missing `@Preview`**: not required — do not flag.

## Always Flag (Functional Correctness)

- Any import or usage of `NavController`, `NavHostController`, or `rememberNavController` — this project uses Navigation 3, not Navigation-Compose.
- Any import of `androidx.lifecycle.viewmodel.*` or `dagger.hilt.*` — this project has no ViewModel or Hilt.
- New screens that are not wired in both `NavRoutes.kt` and `AppNavigation.kt` — they will be unreachable.
- Coroutines started directly inside a composable body without `LaunchedEffect`, `SideEffect`, `DisposableEffect`, or `rememberCoroutineScope`.
- State reads inside composables that should be inside `remember { derivedStateOf { ... } }` to avoid redundant recompositions.

## Stack & Key Versions

- AGP 9.1.1 / Kotlin 2.3.20 / Compose BOM 2026.04.01
- Navigation 3 (`androidx.navigation3`) 1.0.0 — **not** the older Navigation-Compose
- minSdk 23 / compileSdk 36 / JVM target 17
- No ViewModel, no Hilt, no Room — screens are stateless composables wired via lambdas

---

## Kotlin — Flag Only Functional Issues

- Flag `!!` (non-null assertion) where a null value would cause a crash — suggest `?.let`, `?: return`, or `requireNotNull()`.
- Flag `GlobalScope` usage — coroutines must be scoped to composition or a managed scope.
- Flag blocking calls (`runBlocking`, `Thread.sleep`) on the main thread.
- Flag non-exhaustive `when` on sealed/enum types that could throw at runtime.
- Flag unused imports, unused variables, and unused private declarations.

---

## Jetpack Compose — Flag Only Functional Issues

- Flag composables that start coroutines directly in their body (not in an effect handler).
- Flag `LaunchedEffect` or `DisposableEffect` with keys that will never change when they should re-run on state changes.
- Flag infinite animations (`InfiniteTransition`) that are not cancelled when the composable leaves composition.
- Flag `remember` missing around expensive objects that would be reallocated every recomposition (e.g., `Regex`, large lists built from scratch).

---

## Navigation (Navigation 3)

- Routes are `@Serializable data object XxxRoute : NavKey` in `navigation/NavRoutes.kt`.
- Back stack is mutated directly: `backStack.add(route)` / `backStack.removeLastOrNull()` — **do not** flag this as incorrect.
- Screens receive navigation via **lambda callbacks only**.
