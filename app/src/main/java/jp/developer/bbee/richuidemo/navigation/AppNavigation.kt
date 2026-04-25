package jp.developer.bbee.richuidemo.navigation

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import jp.developer.bbee.richuidemo.screen.BorderDemoScreen
import jp.developer.bbee.richuidemo.screen.CombinedDemoScreen
import jp.developer.bbee.richuidemo.screen.HomeScreen
import jp.developer.bbee.richuidemo.screen.SurfaceDemoScreen

@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(HomeRoute)

    NavDisplay(
        backStack = backStack,
        onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<HomeRoute> {
                HomeScreen(
                    onNavigateToBorderDemo = { backStack.add(BorderDemoRoute) },
                    onNavigateToSurfaceDemo = { backStack.add(SurfaceDemoRoute) },
                    onNavigateToCombinedDemo = { backStack.add(CombinedDemoRoute) },
                )
            }

            // Horizontal slide: standard Android forward/back navigation feel
            entry<BorderDemoRoute>(
                metadata = NavDisplay.transitionSpec {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(350, easing = FastOutSlowInEasing),
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { -it / 3 },
                        animationSpec = tween(350, easing = FastOutLinearInEasing),
                    )
                } + NavDisplay.popTransitionSpec {
                    slideInHorizontally(
                        initialOffsetX = { -it / 3 },
                        animationSpec = tween(350, easing = FastOutSlowInEasing),
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(350, easing = FastOutLinearInEasing),
                    )
                },
            ) {
                BorderDemoScreen(onBack = { backStack.removeLastOrNull() })
            }

            // Vertical slide from bottom: sheet-like reveal animation
            entry<SurfaceDemoRoute>(
                metadata = NavDisplay.transitionSpec {
                    slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(420, easing = FastOutSlowInEasing),
                    ) togetherWith fadeOut(animationSpec = tween(180))
                } + NavDisplay.popTransitionSpec {
                    fadeIn(animationSpec = tween(180)) togetherWith slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(420, easing = FastOutLinearInEasing),
                    )
                },
            ) {
                SurfaceDemoScreen(onBack = { backStack.removeLastOrNull() })
            }

            // Scale + fade: modal-like emphasis animation
            entry<CombinedDemoRoute>(
                metadata = NavDisplay.transitionSpec {
                    val enter = scaleIn(
                        initialScale = 0.82f,
                        animationSpec = tween(400, easing = FastOutSlowInEasing),
                    ) + fadeIn(animationSpec = tween(350, easing = FastOutSlowInEasing))
                    val exit = scaleOut(
                        targetScale = 0.96f,
                        animationSpec = tween(280),
                    ) + fadeOut(animationSpec = tween(280))
                    enter togetherWith exit
                } + NavDisplay.popTransitionSpec {
                    val enter = scaleIn(
                        initialScale = 0.96f,
                        animationSpec = tween(280),
                    ) + fadeIn(animationSpec = tween(280))
                    val exit = scaleOut(
                        targetScale = 0.82f,
                        animationSpec = tween(400, easing = FastOutLinearInEasing),
                    ) + fadeOut(animationSpec = tween(400, easing = FastOutLinearInEasing))
                    enter togetherWith exit
                },
            ) {
                CombinedDemoScreen(onBack = { backStack.removeLastOrNull() })
            }
        },
    )
}
