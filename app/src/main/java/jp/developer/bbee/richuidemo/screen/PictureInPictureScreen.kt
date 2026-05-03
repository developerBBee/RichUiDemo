package jp.developer.bbee.richuidemo.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import jp.developer.bbee.richuidemo.component.BackNavigationIcon
import jp.developer.bbee.richuidemo.component.DemoSectionLabel
import kotlin.math.roundToInt

private val PipMinWidth = 140.dp
private val PipMinHeight = 100.dp
private val PipMaxWidth = 340.dp
private val PipMaxHeight = 300.dp

private data class FeedItem(val title: String, val subtitle: String, val color: Color)

private val feedItems = listOf(
    FeedItem("最新ニュース", "2分前 • 国内", Color(0xFF1565C0)),
    FeedItem("スポーツ速報", "5分前 • スポーツ", Color(0xFFC62828)),
    FeedItem("テクノロジー", "10分前 • Tech", Color(0xFF2E7D32)),
    FeedItem("エンタメ情報", "15分前 • エンタメ", Color(0xFF6A1B9A)),
    FeedItem("天気予報", "更新済 • 天気", Color(0xFF00838F)),
    FeedItem("経済動向", "30分前 • 経済", Color(0xFFE65100)),
    FeedItem("科学トピック", "1時間前 • 科学", Color(0xFF37474F)),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PictureInPictureScreen(onBack: () -> Unit) {
    val density = LocalDensity.current

    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var pipWidth by remember { mutableStateOf(220.dp) }
    var pipHeight by remember { mutableStateOf(165.dp) }
    var isMinimized by remember { mutableStateOf(false) }
    var initialized by remember { mutableStateOf(false) }

    val animatedHeight by animateDpAsState(
        targetValue = if (isMinimized) PipHeaderHeight else pipHeight,
        animationSpec = tween(220),
        label = "pip_height",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pip_anim")
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Picture in Picture") },
                navigationIcon = { BackNavigationIcon(onClick = onBack) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            )
        },
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            val maxW = constraints.maxWidth.toFloat()
            val maxH = constraints.maxHeight.toFloat()

            LaunchedEffect(constraints.maxWidth) {
                if (!initialized && constraints.maxWidth > 0) {
                    val pipWidthPx = with(density) { pipWidth.toPx() }
                    offsetX = maxW - pipWidthPx - with(density) { 16.dp.toPx() }
                    offsetY = with(density) { 16.dp.toPx() }
                    initialized = true
                }
            }

            val pipWidthPx = with(density) { pipWidth.toPx() }
            val pipHeightPx = with(density) { animatedHeight.toPx() }
            val maxOffsetX = (maxW - pipWidthPx).coerceAtLeast(0f)
            val maxOffsetY = (maxH - pipHeightPx).coerceAtLeast(0f)

            // Re-clamp stored offset state when the available bounds shrink
            // (e.g. configuration change or PiP resize), so subsequent drags
            // don't have to first walk the offset back into range.
            LaunchedEffect(maxOffsetX, maxOffsetY) {
                offsetX = offsetX.coerceIn(0f, maxOffsetX)
                offsetY = offsetY.coerceIn(0f, maxOffsetY)
            }

            val clampedX = offsetX.coerceIn(0f, maxOffsetX)
            val clampedY = offsetY.coerceIn(0f, maxOffsetY)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    DemoSectionLabel(
                        title = "フローティング PiP ウィンドウ",
                        description = "ヘッダーをドラッグして移動 · 右下コーナーをドラッグしてリサイズ",
                    )
                }
                items(feedItems) { item ->
                    FeedCard(item = item)
                }
            }

            PipWindow(
                modifier = Modifier
                    .offset { IntOffset(clampedX.roundToInt(), clampedY.roundToInt()) }
                    .width(pipWidth),
                pipHeight = pipHeight,
                animatedHeight = animatedHeight,
                isMinimized = isMinimized,
                hue = hue,
                scanline = scanline,
                onDragHeader = { dx, dy ->
                    offsetX = (offsetX + dx).coerceIn(0f, maxOffsetX)
                    offsetY = (offsetY + dy).coerceIn(0f, maxOffsetY)
                },
                onResize = { dx, dy ->
                    with(density) {
                        val newWidth = (pipWidth + dx.toDp()).coerceIn(PipMinWidth, PipMaxWidth)
                        val newHeight = (pipHeight + dy.toDp()).coerceIn(PipMinHeight, PipMaxHeight)
                        pipWidth = newWidth
                        pipHeight = newHeight
                        offsetX = offsetX.coerceIn(0f, (maxW - newWidth.toPx()).coerceAtLeast(0f))
                        offsetY = offsetY.coerceIn(0f, (maxH - newHeight.toPx()).coerceAtLeast(0f))
                    }
                },
                onToggleMinimize = { isMinimized = !isMinimized },
            )
        }
    }
}

@Composable
private fun FeedCard(item: FeedItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = item.color.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(item.color.copy(alpha = 0.7f), CircleShape),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
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
                    .padding(horizontal = 10.dp),
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
                IconButton(
                    onClick = onToggleMinimize,
                    modifier = Modifier.size(PipHeaderHeight),
                ) {
                    Icon(
                        imageVector = if (isMinimized) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                        contentDescription = if (isMinimized) "展開" else "最小化",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp),
                    )
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
