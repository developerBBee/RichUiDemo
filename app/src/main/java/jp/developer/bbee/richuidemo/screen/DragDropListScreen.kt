package jp.developer.bbee.richuidemo.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import jp.developer.bbee.richuidemo.component.BackNavigationIcon
import jp.developer.bbee.richuidemo.component.DemoSectionLabel

private data class DragDropItem(
    val id: Int,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DragDropListScreen(onBack: () -> Unit) {
    val items = remember {
        mutableStateListOf(
            DragDropItem(1, "Design UI Mockups", "High priority · 2h", Icons.Filled.Palette, Color(0xFFE91E63)),
            DragDropItem(2, "API Integration", "Backend · 4h", Icons.Filled.Storage, Color(0xFF2196F3)),
            DragDropItem(3, "Write Unit Tests", "QA · 3h", Icons.Filled.BugReport, Color(0xFF4CAF50)),
            DragDropItem(4, "Code Review", "Team · 1h", Icons.Filled.Code, Color(0xFF9C27B0)),
            DragDropItem(5, "Deploy to Staging", "DevOps · 0.5h", Icons.Filled.CloudUpload, Color(0xFFFF9800)),
            DragDropItem(6, "Update Documentation", "Docs · 2h", Icons.Filled.Description, Color(0xFF00BCD4)),
            DragDropItem(7, "Performance Testing", "QA · 3h", Icons.Filled.Analytics, Color(0xFFFF5722)),
            DragDropItem(8, "Security Audit", "Security · 4h", Icons.Filled.Security, Color(0xFF607D8B)),
        )
    }

    val lazyListState = rememberLazyListState()
    val hapticFeedback = LocalHapticFeedback.current
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableStateOf(0f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Drag & Drop Reorder") },
                navigationIcon = { BackNavigationIcon(onClick = onBack) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                DemoSectionLabel(
                    title = "Task Priority Queue",
                    description = "Long press & drag to reorder",
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DragHandle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "${items.size} tasks",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(top = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { offset ->
                                val item = lazyListState.layoutInfo.visibleItemsInfo.find { info ->
                                    offset.y.toInt() in info.offset until (info.offset + info.size)
                                }
                                if (item != null) {
                                    draggedIndex = item.index
                                    dragOffsetY = 0f
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            },
                            onDrag = { change, dragAmount ->
                                val current = draggedIndex ?: return@detectDragGesturesAfterLongPress
                                val currentItem = lazyListState.layoutInfo.visibleItemsInfo
                                    .find { it.index == current }
                                    ?: return@detectDragGesturesAfterLongPress

                                change.consume()
                                dragOffsetY += dragAmount.y

                                val visualCenter =
                                    currentItem.offset + currentItem.size / 2 + dragOffsetY.toInt()

                                val targetItem = lazyListState.layoutInfo.visibleItemsInfo.find { info ->
                                    visualCenter in info.offset until (info.offset + info.size) &&
                                        info.index != current
                                }

                                if (targetItem != null) {
                                    val toIdx = targetItem.index
                                    val draggedSize = currentItem.size
                                    // Step one position at a time so haptics fire per adjacent swap.
                                    // Accumulate the real layout-offset delta per step so the
                                    // correction is accurate even when item heights vary (e.g.
                                    // large font scale causes text to wrap).
                                    val step = if (toIdx > current) 1 else -1
                                    var idx = current
                                    var draggedLayoutOffset = currentItem.offset
                                    var offsetAdjustment = 0
                                    while (idx != toIdx) {
                                        val next = idx + step
                                        val crossedInfo = lazyListState.layoutInfo.visibleItemsInfo
                                            .find { it.index == next }
                                        if (crossedInfo != null) {
                                            val gap = if (step > 0) {
                                                crossedInfo.offset - draggedLayoutOffset - draggedSize
                                            } else {
                                                draggedLayoutOffset - crossedInfo.offset - crossedInfo.size
                                            }
                                            val delta = (crossedInfo.size + gap) * step
                                            offsetAdjustment += delta
                                            draggedLayoutOffset += delta
                                        }
                                        items.add(next, items.removeAt(idx))
                                        idx = next
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    draggedIndex = toIdx
                                    dragOffsetY -= offsetAdjustment.toFloat()
                                }
                            },
                            onDragEnd = {
                                draggedIndex = null
                                dragOffsetY = 0f
                            },
                            onDragCancel = {
                                draggedIndex = null
                                dragOffsetY = 0f
                            },
                        )
                    },
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                    val isDragged = index == draggedIndex
                    val scale by animateFloatAsState(
                        targetValue = if (isDragged) 1.04f else 1f,
                        animationSpec = tween(150),
                        label = "scale",
                    )
                    val alpha by animateFloatAsState(
                        targetValue = if (isDragged) 0.9f else 1f,
                        animationSpec = tween(150),
                        label = "alpha",
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (!isDragged) Modifier.animateItem() else Modifier)
                            .zIndex(if (isDragged) 1f else 0f)
                            .graphicsLayer {
                                translationY = if (isDragged) dragOffsetY else 0f
                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                                shadowElevation = if (isDragged) 24.dp.toPx() else 0f
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDragged)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceContainerHigh,
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        color = item.accentColor.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(12.dp),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = item.accentColor,
                                    modifier = Modifier.size(22.dp),
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
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

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(
                                        color = if (isDragged)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.surfaceContainerHighest,
                                        shape = CircleShape,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDragged)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            Icon(
                                imageVector = Icons.Rounded.DragHandle,
                                contentDescription = "Drag to reorder",
                                tint = if (isDragged)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
