package jp.developer.bbee.richuidemo.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyScaffold(
    modifier: Modifier = Modifier,
    title: String = "",
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(title) }) },
        content = content,
    )
}

@Preview(showSystemUi = true)
@Composable
private fun Warning() {
    MyScaffold(title = "Warning") {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.LightGray),
        ) {

        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun OK() {
    MyScaffold(title = "OK") { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(color = Color.LightGray),
        ) {

        }
    }
}
