package jp.developer.bbee.richuidemo.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute : NavKey

@Serializable
data object BorderDemoRoute : NavKey

@Serializable
data object SurfaceDemoRoute : NavKey

@Serializable
data object CombinedDemoRoute : NavKey

@Serializable
data object HorizontalScrollCardsRoute : NavKey

@Serializable
data object WidgetSamplesRoute : NavKey

@Serializable
data object DragDropListRoute : NavKey

@Serializable
data object BubbleMenuRoute : NavKey

@Serializable
data object PictureInPictureRoute : NavKey

@Serializable
data object HideNavOnScrollRoute : NavKey

@Serializable
data object SwipeableDualLayoutRoute : NavKey

@Serializable
data object ImageSelectionOverlayRoute : NavKey
