package jp.developer.bbee.richuidemo.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

private fun Color.asColorProvider(): ColorProvider = object : ColorProvider {
    override fun getColor(context: Context) = this@asColorProvider
}

class WeatherGlanceWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WeatherWidgetContent()
            }
        }
    }
}

@Composable
private fun WeatherWidgetContent() {
    val white = Color.White.asColorProvider()
    val whiteFaded = Color.White.copy(alpha = 0.75f).asColorProvider()
    val detailBg = Color.White.copy(alpha = 0.15f).asColorProvider()

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(Color(0xFF1565C0).asColorProvider())
            .cornerRadius(20.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.TopStart,
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            // Top row: city name + sun emoji
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically,
            ) {
                Text(
                    text = "Tokyo, Japan",
                    style = TextStyle(color = whiteFaded, fontSize = 13.sp),
                    modifier = GlanceModifier.defaultWeight(),
                )
                Text(text = "☀️", style = TextStyle(fontSize = 36.sp))
            }

            Spacer(GlanceModifier.height(4.dp))

            // Temperature + condition
            Row(verticalAlignment = Alignment.Vertical.Bottom) {
                Text(
                    text = "23°",
                    style = TextStyle(
                        color = white,
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Column(modifier = GlanceModifier.padding(start = 12.dp, bottom = 4.dp)) {
                    Text(text = "⛅", style = TextStyle(fontSize = 22.sp))
                    Spacer(GlanceModifier.height(2.dp))
                    Text(
                        text = "Partly Cloudy",
                        style = TextStyle(color = whiteFaded, fontSize = 13.sp),
                    )
                }
            }

            Spacer(GlanceModifier.height(10.dp))

            // Detail row
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(detailBg)
                    .cornerRadius(12.dp)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Vertical.CenterVertically,
            ) {
                Box(
                    modifier = GlanceModifier.defaultWeight(),
                    contentAlignment = Alignment.Center,
                ) { DetailCell(emoji = "💧", label = "Humidity", value = "68%") }
                Box(
                    modifier = GlanceModifier.defaultWeight(),
                    contentAlignment = Alignment.Center,
                ) { DetailCell(emoji = "💨", label = "Wind", value = "12 km/h") }
                Box(
                    modifier = GlanceModifier.defaultWeight(),
                    contentAlignment = Alignment.Center,
                ) { DetailCell(emoji = "👁", label = "Visibility", value = "10 km") }
            }
        }
    }
}

@Composable
private fun DetailCell(emoji: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.Horizontal.CenterHorizontally) {
        Text(text = emoji, style = TextStyle(fontSize = 16.sp))
        Spacer(GlanceModifier.height(2.dp))
        Text(
            text = value,
            style = TextStyle(
                color = Color.White.asColorProvider(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        Text(
            text = label,
            style = TextStyle(
                color = Color.White.copy(alpha = 0.65f).asColorProvider(),
                fontSize = 10.sp,
            ),
        )
    }
}
