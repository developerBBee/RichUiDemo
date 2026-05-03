package jp.developer.bbee.richuidemo.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import jp.developer.bbee.richuidemo.component.BackNavigationIcon
import jp.developer.bbee.richuidemo.component.DemoSectionLabel

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
fun PictureInPictureScreen(pipState: PipState, onBack: () -> Unit) {
    LaunchedEffect(Unit) {
        pipState.isVisible = true
    }

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
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                DemoSectionLabel(
                    title = "フローティング PiP ウィンドウ",
                    description = "戻るとホーム画面上にも表示 · X で閉じる · ヘッダードラッグで移動 · 右下でリサイズ",
                )
            }
            if (!pipState.isVisible) {
                item {
                    Button(
                        onClick = { pipState.isVisible = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = "  PiP を起動",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
            items(feedItems) { item ->
                FeedCard(item = item)
            }
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
