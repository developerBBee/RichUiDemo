package jp.developer.bbee.richuidemo.screen

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState

private val CELL_HEIGHT: Dp = 140.dp
private val CELL_GAP: Dp = 8.dp

data class GridImageItem(
    val id: Int,
    val colSpan: Int,
    val rowSpan: Int,
    val gradient: List<Color>,
    val icon: ImageVector,
)

private val initialGridItems = listOf(
    // Row 1: single 2×2 spanning full width and 2 rows
    GridImageItem(0, 2, 2, listOf(Color(0xFF6200EA), Color(0xFF9C27B0)), Icons.Default.Star),
    // Row 3: two 1×2 items side by side
    GridImageItem(1, 1, 2, listOf(Color(0xFF00BCD4), Color(0xFF0097A7)), Icons.Default.Image),
    GridImageItem(2, 1, 2, listOf(Color(0xFFFF5722), Color(0xFFE64A19)), Icons.Default.Favorite),
    // Row 5: two 1×1 items
    GridImageItem(3, 1, 1, listOf(Color(0xFF4CAF50), Color(0xFF388E3C)), Icons.Default.Place),
    GridImageItem(4, 1, 1, listOf(Color(0xFFFF9800), Color(0xFFF57C00)), Icons.Default.Pets),
    // Row 6: single 2×1 spanning full width
    GridImageItem(5, 2, 1, listOf(Color(0xFF2196F3), Color(0xFF1565C0)), Icons.Default.Cloud),
    // Row 7: two 1×1 items
    GridImageItem(6, 1, 1, listOf(Color(0xFFE91E63), Color(0xFFC2185B)), Icons.Default.MusicNote),
    GridImageItem(7, 1, 1, listOf(Color(0xFF9C27B0), Color(0xFF7B1FA2)), Icons.Default.CameraAlt),
    // Row 8: single 2×2 spanning full width and 2 rows
    GridImageItem(8, 2, 2, listOf(Color(0xFF009688), Color(0xFF00695C)), Icons.Default.Map),
    // Row 10: two 1×2 items side by side
    GridImageItem(9, 1, 2, listOf(Color(0xFFF44336), Color(0xFFB71C1C)), Icons.Default.Flight),
    GridImageItem(10, 1, 2, listOf(Color(0xFF607D8B), Color(0xFF37474F)), Icons.Default.Forest),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraggableImageGridScreen(onBack: () -> Unit) {
    var items by remember { mutableStateOf(initialGridItems) }
    val haptic = LocalHapticFeedback.current

    val lazyGridState = rememberLazyGridState()
    val reorderableState = rememberReorderableLazyGridState(lazyGridState) { from, to ->
        items = items.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Draggable Image Grid",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Long-press to reorder",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            )
        },
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = lazyGridState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(CELL_GAP),
            horizontalArrangement = Arrangement.spacedBy(CELL_GAP),
            verticalArrangement = Arrangement.spacedBy(CELL_GAP),
        ) {
            items(
                items = items,
                key = { it.id },
                span = { item -> GridItemSpan(item.colSpan) },
            ) { item ->
                ReorderableItem(reorderableState, item.id) { isDragging ->
                    val elevation by animateDpAsState(
                        targetValue = if (isDragging) 20.dp else 0.dp,
                        label = "drag_elevation",
                    )
                    val itemHeight = CELL_HEIGHT * item.rowSpan + CELL_GAP * (item.rowSpan - 1)

                    Box(
                        modifier = Modifier
                            .height(itemHeight)
                            .shadow(elevation, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.linearGradient(item.gradient))
                            .longPressDraggableHandle(
                                onDragStarted = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        GridItemContent(item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun GridItemContent(item: GridImageItem) {
    val isLarge = item.colSpan > 1 || item.rowSpan > 1
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            modifier = Modifier.size(if (isLarge) 56.dp else 36.dp),
            tint = Color.White.copy(alpha = 0.9f),
        )
        Spacer(modifier = Modifier.height(if (isLarge) 8.dp else 4.dp))
        SpanBadge(colSpan = item.colSpan, rowSpan = item.rowSpan, isLarge = isLarge)
    }
}

@Composable
private fun SpanBadge(colSpan: Int, rowSpan: Int, isLarge: Boolean) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Black.copy(alpha = 0.3f),
    ) {
        Text(
            text = "${colSpan}×${rowSpan}",
            style = if (isLarge) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(
                horizontal = if (isLarge) 10.dp else 6.dp,
                vertical = if (isLarge) 4.dp else 2.dp,
            ),
        )
    }
}
