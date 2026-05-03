package jp.developer.bbee.richuidemo.screen

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private val PipMinWidth = 140.dp
private val PipMinHeight = 100.dp
private val PipMaxWidth = 340.dp
private val PipMaxHeight = 300.dp
internal val PipHeaderHeight = 36.dp

@Stable
class PipState {
    var offsetX by mutableFloatStateOf(0f)
    var offsetY by mutableFloatStateOf(0f)
    var pipWidth by mutableStateOf(220.dp)
    var pipHeight by mutableStateOf(165.dp)
    var isMinimized by mutableStateOf(false)
    var isVisible by mutableStateOf(false)
    var initialized by mutableStateOf(false)
}

@Composable
fun rememberPipState(): PipState = remember { PipState() }

@Composable
fun PipOverlay(
    pipState: PipState,
    hue: Float,
    scanline: Float,
    modifier: Modifier = Modifier,
) {
    if (!pipState.isVisible) return

    val density = LocalDensity.current

    val animatedHeight by animateDpAsState(
        targetValue = if (pipState.isMinimized) PipHeaderHeight else pipState.pipHeight,
        animationSpec = tween(220),
        label = "pip_height",
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val maxW = constraints.maxWidth.toFloat()
        val maxH = constraints.maxHeight.toFloat()

        LaunchedEffect(constraints.maxWidth) {
            if (!pipState.initialized && constraints.maxWidth > 0) {
                val pipWidthPx = with(density) { pipState.pipWidth.toPx() }
                pipState.offsetX = maxW - pipWidthPx - with(density) { 16.dp.toPx() }
                pipState.offsetY = with(density) { 16.dp.toPx() }
                pipState.initialized = true
            }
        }

        val pipWidthPx = with(density) { pipState.pipWidth.toPx() }
        val pipHeightPx = with(density) { animatedHeight.toPx() }
        val maxOffsetX = (maxW - pipWidthPx).coerceAtLeast(0f)
        val maxOffsetY = (maxH - pipHeightPx).coerceAtLeast(0f)

        LaunchedEffect(maxOffsetX, maxOffsetY) {
            pipState.offsetX = pipState.offsetX.coerceIn(0f, maxOffsetX)
            pipState.offsetY = pipState.offsetY.coerceIn(0f, maxOffsetY)
        }

        val clampedX = pipState.offsetX.coerceIn(0f, maxOffsetX)
        val clampedY = pipState.offsetY.coerceIn(0f, maxOffsetY)

        PipWindow(
            modifier = Modifier
                .offset { IntOffset(clampedX.roundToInt(), clampedY.roundToInt()) }
                .width(pipState.pipWidth),
            pipHeight = pipState.pipHeight,
            animatedHeight = animatedHeight,
            isMinimized = pipState.isMinimized,
            hue = hue,
            scanline = scanline,
            onDragHeader = { dx, dy ->
                pipState.offsetX = (pipState.offsetX + dx).coerceIn(0f, maxOffsetX)
                pipState.offsetY = (pipState.offsetY + dy).coerceIn(0f, maxOffsetY)
            },
            onResize = { dx, dy ->
                with(density) {
                    val newWidth = (pipState.pipWidth + dx.toDp()).coerceIn(PipMinWidth, PipMaxWidth)
                    val newHeight = (pipState.pipHeight + dy.toDp()).coerceIn(PipMinHeight, PipMaxHeight)
                    pipState.pipWidth = newWidth
                    pipState.pipHeight = newHeight
                    pipState.offsetX = pipState.offsetX.coerceIn(0f, (maxW - newWidth.toPx()).coerceAtLeast(0f))
                    pipState.offsetY = pipState.offsetY.coerceIn(0f, (maxH - newHeight.toPx()).coerceAtLeast(0f))
                }
            },
            onToggleMinimize = { pipState.isMinimized = !pipState.isMinimized },
            onClose = { pipState.isVisible = false },
        )
    }
}

@Composable
private fun PipWindow(
    modifier: Modifier,
    pipHeight: Dp,
    animatedHeight: Dp,
    isMinimized: Boolean,
    hue: Float,
    scanline: Float,
    onDragHeader: (Float, Float) -> Unit,
    onResize: (Float, Float) -> Unit,
    onToggleMinimize: () -> Unit,
    onClose: () -> Unit,
) {
    val showContent = animatedHeight > PipHeaderHeight + 8.dp

    Box(
        modifier = modifier
            .height(animatedHeight)
            .shadow(elevation = 20.dp, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PipHeaderHeight)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .pointerInput(Unit) {
                        detectDragGestures { _, dragAmount ->
                            onDragHeader(dragAmount.x, dragAmount.y)
                        }
                    }
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(Modifier.size(8.dp).background(Color(0xFFFF5F57), CircleShape))
                    Box(Modifier.size(8.dp).background(Color(0xFFFFBD2E), CircleShape))
                    Box(Modifier.size(8.dp).background(Color(0xFF28CA41), CircleShape))
                }
                Text(
                    text = if (isMinimized) "PiP (最小化中)"
                    else "${pipHeight.value.toInt()} dp",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Row {
                    IconButton(
                        onClick = onToggleMinimize,
                        modifier = Modifier.size(PipHeaderHeight),
                    ) {
                        Icon(
                            imageVector = if (isMinimized) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                            contentDescription = if (isMinimized) "展開" else "最小化",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(PipHeaderHeight),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "閉じる",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }

            if (showContent) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.sweepGradient(
                                    colors = listOf(
                                        Color.hsv(hue, 0.8f, 0.55f),
                                        Color.hsv((hue + 72f) % 360f, 0.8f, 0.55f),
                                        Color.hsv((hue + 144f) % 360f, 0.8f, 0.55f),
                                        Color.hsv((hue + 216f) % 360f, 0.8f, 0.55f),
                                        Color.hsv((hue + 288f) % 360f, 0.8f, 0.55f),
                                        Color.hsv(hue, 0.8f, 0.55f),
                                    ),
                                ),
                            ),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.18f),
                                        Color.Transparent,
                                    ),
                                    startY = scanline * 600f - 30f,
                                    endY = scanline * 600f + 30f,
                                ),
                            ),
                    )
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(36.dp),
                    )
                }
            }
        }

        if (showContent) {
            ResizeHandle(
                modifier = Modifier.align(Alignment.BottomEnd),
                onResize = onResize,
            )
        }
    }
}

@Composable
private fun ResizeHandle(modifier: Modifier, onResize: (Float, Float) -> Unit) {
    Box(
        modifier = modifier
            .size(32.dp)
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    onResize(dragAmount.x, dragAmount.y)
                }
            },
        contentAlignment = Alignment.BottomEnd,
    ) {
        Column(
            modifier = Modifier.padding(5.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(3) {
                    Box(Modifier.size(3.dp).background(Color.White.copy(alpha = 0.7f), CircleShape))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(2) {
                    Box(Modifier.size(3.dp).background(Color.White.copy(alpha = 0.7f), CircleShape))
                }
            }
            Box(Modifier.size(3.dp).background(Color.White.copy(alpha = 0.7f), CircleShape))
        }
    }
}

// Shown when the activity enters Android native PiP mode — fills the shrunken window
@Composable
fun PipContent(hue: Float, scanline: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.sweepGradient(
                        colors = listOf(
                            Color.hsv(hue, 0.8f, 0.55f),
                            Color.hsv((hue + 72f) % 360f, 0.8f, 0.55f),
                            Color.hsv((hue + 144f) % 360f, 0.8f, 0.55f),
                            Color.hsv((hue + 216f) % 360f, 0.8f, 0.55f),
                            Color.hsv((hue + 288f) % 360f, 0.8f, 0.55f),
                            Color.hsv(hue, 0.8f, 0.55f),
                        ),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.18f),
                            Color.Transparent,
                        ),
                        startY = scanline * 600f - 30f,
                        endY = scanline * 600f + 30f,
                    ),
                ),
        )
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(48.dp),
        )
    }
}
