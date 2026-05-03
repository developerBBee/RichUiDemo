package jp.developer.bbee.richuidemo.navigation

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import jp.developer.bbee.richuidemo.MainActivity
import jp.developer.bbee.richuidemo.screen.BorderDemoScreen
import jp.developer.bbee.richuidemo.screen.BubbleMenuScreen
import jp.developer.bbee.richuidemo.screen.CombinedDemoScreen
import jp.developer.bbee.richuidemo.screen.DragDropListScreen
import jp.developer.bbee.richuidemo.screen.HomeScreen
import jp.developer.bbee.richuidemo.screen.HorizontalScrollCardsScreen
import jp.developer.bbee.richuidemo.screen.PictureInPictureScreen
import jp.developer.bbee.richuidemo.screen.PipContent
import jp.developer.bbee.richuidemo.screen.PipOverlay
import jp.developer.bbee.richuidemo.screen.SurfaceDemoScreen
import jp.developer.bbee.richuidemo.screen.WidgetSamplesScreen
import jp.developer.bbee.richuidemo.screen.rememberPipState

@Composable
fun AppNavigation() {
    val activity = LocalContext.current as MainActivity
    val backStack = rememberNavBackStack(HomeRoute)
    val pipState = rememberPipState()

    // Keep activity informed and pre-register PiP params so setAutoEnterEnabled takes effect.
    // SideEffect runs after every successful recomposition, so params stay in sync with
    // pipState.isVisible without needing a separate LaunchedEffect.
    SideEffect {
        activity.shouldEnterPip = { pipState.isVisible }
        activity.updatePipParams()
    }

    val isInPipMode = activity.isInPipMode.value

    val infiniteTransition = rememberInfiniteTransition(label = "pip_global")
    val hue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
        label = "hue",
    )
    val scanline by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing)),
        label = "scanline",
    )

    Box(modifier = Modifier.fillMaxSize()) {
        if (isInPipMode) {
            // Activity window has shrunk to PiP — show only the video content
            PipContent(hue = hue, scanline = scanline)
        } else {
            NavDisplay(
                backStack = backStack,
                onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
                entryProvider = entryProvider {
                    entry<HomeRoute> {
                        HomeScreen(
                            onNavigateToBorderDemo = { backStack.add(BorderDemoRoute) },
                            onNavigateToSurfaceDemo = { backStack.add(SurfaceDemoRoute) },
                            onNavigateToCombinedDemo = { backStack.add(CombinedDemoRoute) },
                            onNavigateToHorizontalScrollCards = { backStack.add(HorizontalScrollCardsRoute) },
                            onNavigateToWidgetSamples = { backStack.add(WidgetSamplesRoute) },
                            onNavigateToDragDropList = { backStack.add(DragDropListRoute) },
                            onNavigateToBubbleMenu = { backStack.add(BubbleMenuRoute) },
                            onNavigateToPictureInPicture = { backStack.add(PictureInPictureRoute) },
                        )
                    }

                    // Horizontal slide: standard Android forward/back navigation feel
                    entry<BorderDemoRoute>(metadata = horizontalSlideMetadata) {
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

                    entry<WidgetSamplesRoute>(metadata = horizontalSlideMetadata) {
                        WidgetSamplesScreen(onBack = { backStack.removeLastOrNull() })
                    }

                    entry<HorizontalScrollCardsRoute>(metadata = horizontalSlideMetadata) {
                        HorizontalScrollCardsScreen(onBack = { backStack.removeLastOrNull() })
                    }

                    entry<DragDropListRoute>(metadata = horizontalSlideMetadata) {
                        DragDropListScreen(onBack = { backStack.removeLastOrNull() })
                    }

                    entry<BubbleMenuRoute>(metadata = horizontalSlideMetadata) {
                        BubbleMenuScreen(onBack = { backStack.removeLastOrNull() })
                    }

                    entry<PictureInPictureRoute>(metadata = horizontalSlideMetadata) {
                        PictureInPictureScreen(
                            pipState = pipState,
                            onBack = { backStack.removeLastOrNull() },
                        )
                    }
                },
            )

            PipOverlay(
                pipState = pipState,
                hue = hue,
                scanline = scanline,
            )
        }
    }
}

private val horizontalSlideMetadata =
    NavDisplay.transitionSpec {
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
    }
