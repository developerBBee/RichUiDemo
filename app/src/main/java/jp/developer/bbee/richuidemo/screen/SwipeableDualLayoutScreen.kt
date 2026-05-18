package jp.developer.bbee.richuidemo.screen

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

private enum class PanelAnchor { COLLAPSED, EXPANDED }

@Composable
fun SwipeableDualLayoutScreen(onBack: () -> Unit) {
    val density = LocalDensity.current
    var bookmarked by remember { mutableStateOf(false) }

    val state = remember(density) {
        AnchoredDraggableState(
            initialValue = PanelAnchor.COLLAPSED,
            positionalThreshold = { d -> d * 0.5f },
            velocityThreshold = { with(density) { 125.dp.toPx() } },
            snapAnimationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
            decayAnimationSpec = exponentialDecay(),
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val peekHeight = 120.dp
        val expandedFraction = 0.72f
        val screenHeight = maxHeight
        val expandedHeight = (screenHeight * expandedFraction).coerceAtLeast(peekHeight)
        val maxDragPx = with(density) { (expandedHeight - peekHeight).toPx() }.coerceAtLeast(0f)

        SideEffect {
            state.updateAnchors(
                DraggableAnchors {
                    PanelAnchor.COLLAPSED at 0f
                    PanelAnchor.EXPANDED at maxDragPx
                },
            )
        }

        val rawOffset = if (state.offset.isNaN()) 0f else state.offset
        val progress = (rawOffset / maxDragPx.coerceAtLeast(1f)).coerceIn(0f, 1f)
        val topHeightDp = (screenHeight - peekHeight - with(density) { rawOffset.toDp() })
            .coerceAtLeast(0.dp)

        // Top hero section — shrinks as the panel expands
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(topHeightDp),
        ) {
            // Gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1A237E),
                                Color(0xFF283593),
                                Color(0xFF3949AB),
                                Color(0xFF5C6BC0),
                            ),
                        ),
                    ),
            )

            // Hero content — fades out as the panel rises
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .alpha((1f - progress * 2.2f).coerceIn(0f, 1f)),
            ) {
                Spacer(modifier = Modifier.height(56.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF7986CB),
                                    Color(0xFF9575CD),
                                    Color(0xFFAB47BC),
                                ),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(text = "🗼", fontSize = 56.sp)
                        Text(
                            text = "TOKYO",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                        )
                        Text(
                            text = "東京都, Japan",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatChip(icon = "⭐", value = "4.8", label = "Rating")
                    StatChip(icon = "🌡️", value = "18°C", label = "Now")
                    StatChip(icon = "✈️", value = "12h", label = "Flight")
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TagChip("Cultural")
                    TagChip("Historic")
                    TagChip("UNESCO")
                }
            }

            // Back button — always visible
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(top = 8.dp, start = 4.dp)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                )
            }

            // Compact header — fades in as the panel nears full expansion
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(((progress - 0.55f) / 0.35f).coerceIn(0f, 1f))
                    .padding(start = 56.dp, end = 16.dp, top = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "TOKYO, Japan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = " 4.8",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                    )
                }
            }

            // "Swipe up" hint — disappears as soon as the user starts swiping
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp)
                    .alpha((1f - progress * 5f).coerceIn(0f, 1f)),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "↑  Swipe up for details",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.75f),
                )
            }
        }

        // Bottom detail panel — slides up from the bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(expandedHeight)
                .align(Alignment.BottomCenter)
                .offset { IntOffset(0, (maxDragPx - rawOffset).roundToInt()) }
                .anchoredDraggable(state, Orientation.Vertical, reverseDirection = true)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(MaterialTheme.colorScheme.surface),
        ) {
            DetailPanel(bookmarked = bookmarked, onBookmarkToggle = { bookmarked = !bookmarked })
        }
    }
}

@Composable
private fun StatChip(icon: String, value: String, label: String) {
    Box(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = icon, fontSize = 18.sp)
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun TagChip(text: String) {
    Box(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
        )
    }
}

@Composable
private fun DetailPanel(bookmarked: Boolean, onBookmarkToggle: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
    ) {
        // Drag handle indicator
        Box(
            modifier = Modifier
                .padding(top = 12.dp, bottom = 8.dp)
                .align(Alignment.CenterHorizontally),
        ) {
            Box(
                modifier = Modifier
                    .size(width = 40.dp, height = 4.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        CircleShape,
                    ),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Destination Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Row {
                IconButton(onClick = onBookmarkToggle) {
                    Icon(
                        imageVector = if (bookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (bookmarked) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            InfoCard(modifier = Modifier.weight(1f), value = "¥150k", label = "Avg. Budget")
            InfoCard(modifier = Modifier.weight(1f), value = "7 days", label = "Recommend")
            InfoCard(modifier = Modifier.weight(1f), value = "4.8 ★", label = "Rating")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "About",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "東京は日本の首都であり、世界最大の都市圏のひとつです。伝統と現代が融合した独特の文化を持ち、豊富な観光地と美食が世界中から旅行者を引き付けています。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Highlights",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(8.dp))

        listOf(
            "🛕  浅草寺 — 東京最古の寺院",
            "🌸  上野公園 — 桜の名所",
            "🗼  東京タワー — 展望台",
            "🛒  原宿・渋谷 — ショッピング",
            "🎎  明治神宮 — 神道の聖地",
        ).forEach { item ->
            Text(
                text = item,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun InfoCard(modifier: Modifier = Modifier, value: String, label: String) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
