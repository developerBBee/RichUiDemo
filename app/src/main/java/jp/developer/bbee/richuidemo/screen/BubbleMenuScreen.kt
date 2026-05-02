package jp.developer.bbee.richuidemo.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.developer.bbee.richuidemo.component.BackNavigationIcon
import jp.developer.bbee.richuidemo.component.BubbleMenuButton
import jp.developer.bbee.richuidemo.component.BubbleMenuItem
import jp.developer.bbee.richuidemo.ui.theme.RichUiDemoTheme
import kotlinx.coroutines.launch

private data class AppListEntry(val title: String, val category: String, val color: Color)

private val sampleItems = listOf(
    AppListEntry("写真編集アプリ", "ポートフォリオ", Color(0xFF6650A4)),
    AppListEntry("音楽プレイヤー", "メディア", Color(0xFF0061A4)),
    AppListEntry("タスク管理", "生産性", Color(0xFF006E1C)),
    AppListEntry("天気予報", "ツール", Color(0xFF984061)),
    AppListEntry("料理レシピ", "ライフスタイル", Color(0xFF6B5C00)),
    AppListEntry("フィットネス", "健康", Color(0xFF00696E)),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BubbleMenuScreen(onBack: () -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }
    BackHandler(enabled = menuExpanded) { menuExpanded = false }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val favoriteContainerColor = MaterialTheme.colorScheme.tertiaryContainer
    val errorColor = MaterialTheme.colorScheme.error

    val bubbleItems = remember(scope, snackbarHostState, favoriteContainerColor, errorColor) {
        listOf(
            BubbleMenuItem(
                icon = Icons.Default.Favorite,
                label = "お気に入り",
                id = "favorite",
                containerColor = favoriteContainerColor,
                onClick = {
                    scope.launch { snackbarHostState.showSnackbar("お気に入りに追加しました") }
                },
            ),
            BubbleMenuItem(
                icon = Icons.Default.Share,
                label = "シェア",
                id = "share",
                onClick = {
                    scope.launch { snackbarHostState.showSnackbar("シェアしました") }
                },
            ),
            BubbleMenuItem(
                icon = Icons.Default.Edit,
                label = "編集",
                id = "edit",
                onClick = {
                    scope.launch { snackbarHostState.showSnackbar("編集モードを開始します") }
                },
            ),
            BubbleMenuItem(
                icon = Icons.Default.Delete,
                label = "削除",
                id = "delete",
                containerColor = errorColor,
                onClick = {
                    scope.launch { snackbarHostState.showSnackbar("削除しました") }
                },
            ),
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bubble Menu Button") },
                navigationIcon = { BackNavigationIcon(onClick = onBack) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            BubbleMenuButton(
                items = bubbleItems,
                expanded = menuExpanded,
                onExpandedChange = { menuExpanded = it },
                collapsedContentDescription = "メニューを開く",
                expandedContentDescription = "メニューを閉じる",
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .then(if (menuExpanded) Modifier.clearAndSetSemantics {} else Modifier),
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Text(
                            text = "右下の + ボタンをタップしてメニューを開く",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
                    items(sampleItems) { entry ->
                        AppListItem(entry = entry)
                    }
                }
            }

            AnimatedVisibility(
                visible = menuExpanded,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics { contentDescription = "メニューを閉じる" }
                        .background(Color.Black.copy(alpha = 0.35f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClickLabel = "メニューを閉じる",
                            role = Role.Button,
                        ) { menuExpanded = false },
                )
            }
        }
    }
}

@Composable
private fun AppListItem(entry: AppListEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = entry.color.copy(alpha = 0.18f),
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = entry.color,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = entry.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BubbleMenuScreenPreview() {
    RichUiDemoTheme {
        BubbleMenuScreen(onBack = {})
    }
}
