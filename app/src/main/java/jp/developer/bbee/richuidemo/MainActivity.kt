package jp.developer.bbee.richuidemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import jp.developer.bbee.richuidemo.component.AnimatedBorderCard
import jp.developer.bbee.richuidemo.component.AnimatedSurfaceCard
import jp.developer.bbee.richuidemo.ui.theme.RichUiDemoTheme
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RichUiDemoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    MainContent()
                }
            }
        }
    }
}

@Composable
fun MainContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        AnimatedBorderCard(
            shape = RoundedCornerShape(8.dp),
            gradient = Brush.sweepGradient(listOf(Color.Magenta, Color.Cyan, Color.Magenta)),
            borderWidth = 8.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    modifier = Modifier.padding(20.dp),
                    text = "Hello, Rich UI Demo!",
                    textAlign = TextAlign.Center,
                )
            }
        }

        AnimatedBorderCard(
            shape = RoundedCornerShape(12.dp),
            gradient = Brush.sweepGradient(
                listOf(Color.Magenta, Color.White, Color.Cyan, Color.White, Color.Magenta)
            ),
            borderWidth = 8.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    modifier = Modifier.padding(20.dp),
                    text = "Hello, Rich UI Demo!",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        AnimatedSurfaceCard(shape = RoundedCornerShape(12.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    modifier = Modifier.padding(20.dp),
                    text = "Hello, Rich UI Demo!",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        AnimatedBorderCard(
            shape = RoundedCornerShape(12.dp),
            gradient = Brush.sweepGradient(
                listOf(Color.Magenta, Color.White, Color.Cyan, Color.White, Color.Magenta)
            ),
            borderWidth = 8.dp,
        ) {
            AnimatedSurfaceCard(
                shape = RoundedCornerShape(12.dp),
                backGroundColor = MaterialTheme.colorScheme.surface,
                effectColor = MaterialTheme.colorScheme.secondary,
                animationInterval = 1000.milliseconds.toInt(DurationUnit.MILLISECONDS)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        modifier = Modifier.padding(20.dp),
                        text = "Hello, Rich UI Demo!",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}