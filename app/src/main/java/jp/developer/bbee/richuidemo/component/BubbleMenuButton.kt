package jp.developer.bbee.richuidemo.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.developer.bbee.richuidemo.ui.theme.RichUiDemoTheme

data class BubbleMenuItem(
    val icon: ImageVector,
    val label: String,
    val containerColor: Color = Color.Unspecified,
    val contentColor: Color = Color.Unspecified,
    val id: String,
    val onClick: () -> Unit,
)

@Composable
fun BubbleMenuButton(
    items: List<BubbleMenuItem>,
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    mainIcon: ImageVector = Icons.Default.Add,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    collapsedContentDescription: String,
    expandedContentDescription: String,
) {
    check(items.distinctBy { it.id }.size == items.size) {
        "BubbleMenuButton: each BubbleMenuItem must have a unique id"
    }

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "fab_rotation",
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val reversedItems = remember(items) { items.asReversed() }
        reversedItems.forEach { item ->
            key(item.id) {
                AnimatedVisibility(
                    visible = expanded,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                    ) + fadeIn(animationSpec = tween(220)),
                    exit = slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = spring(stiffness = Spring.StiffnessHigh),
                    ) + fadeOut(animationSpec = tween(120)),
                ) {
                    BubbleMenuItemRow(
                        item = item,
                        onDismiss = { onExpandedChange(false) },
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { onExpandedChange(!expanded) },
            containerColor = containerColor,
            contentColor = contentColor,
            shape = CircleShape,
        ) {
            Icon(
                imageVector = mainIcon,
                contentDescription = if (expanded) expandedContentDescription else collapsedContentDescription,
                modifier = Modifier.rotate(rotation),
            )
        }
    }
}

@Composable
private fun BubbleMenuItemRow(
    item: BubbleMenuItem,
    onDismiss: () -> Unit,
) {
    val fabColor = item.containerColor.takeOrElse { MaterialTheme.colorScheme.secondaryContainer }
    val fabContentColor = item.contentColor.takeOrElse {
        contentColorFor(fabColor).takeOrElse { MaterialTheme.colorScheme.onSecondaryContainer }
    }

    Row(
        modifier = Modifier
            .semantics(mergeDescendants = true) {}
            .heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Surface(
            onClick = {
                item.onClick()
                onDismiss()
            },
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 3.dp,
            tonalElevation = 2.dp,
        ) {
            Text(
                text = item.label,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        SmallFloatingActionButton(
            onClick = {
                item.onClick()
                onDismiss()
            },
            containerColor = fabColor,
            contentColor = fabContentColor,
            shape = CircleShape,
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
            )
        }
    }
}

@Preview(showBackground = true, name = "Collapsed")
@Composable
private fun BubbleMenuButtonCollapsedPreview() {
    RichUiDemoTheme {
        val previewItems = listOf(
            BubbleMenuItem(icon = Icons.Default.Share, label = "シェア", id = "share", onClick = {}),
            BubbleMenuItem(icon = Icons.Default.Edit, label = "編集", id = "edit", onClick = {}),
            BubbleMenuItem(
                icon = Icons.Default.Delete,
                label = "削除",
                id = "delete",
                containerColor = MaterialTheme.colorScheme.error,
                onClick = {},
            ),
        )
        Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.BottomEnd) {
            BubbleMenuButton(
                items = previewItems,
                modifier = Modifier.padding(16.dp),
                expanded = false,
                onExpandedChange = {},
                collapsedContentDescription = "メニューを開く",
                expandedContentDescription = "メニューを閉じる",
            )
        }
    }
}

@Preview(showBackground = true, name = "Expanded")
@Composable
private fun BubbleMenuButtonExpandedPreview() {
    RichUiDemoTheme {
        val previewItems = listOf(
            BubbleMenuItem(icon = Icons.Default.Share, label = "シェア", id = "share", onClick = {}),
            BubbleMenuItem(icon = Icons.Default.Edit, label = "編集", id = "edit", onClick = {}),
            BubbleMenuItem(
                icon = Icons.Default.Delete,
                label = "削除",
                id = "delete",
                containerColor = MaterialTheme.colorScheme.error,
                onClick = {},
            ),
        )
        Box(modifier = Modifier.size(300.dp), contentAlignment = Alignment.BottomEnd) {
            BubbleMenuButton(
                items = previewItems,
                modifier = Modifier.padding(16.dp),
                expanded = true,
                onExpandedChange = {},
                collapsedContentDescription = "メニューを開く",
                expandedContentDescription = "メニューを閉じる",
            )
        }
    }
}
