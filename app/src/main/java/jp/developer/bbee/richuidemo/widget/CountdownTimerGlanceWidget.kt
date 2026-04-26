package jp.developer.bbee.richuidemo.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.action.actionRunCallback
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
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

private fun Color.asColorProvider(): ColorProvider = object : ColorProvider {
    override fun getColor(context: Context): Color = this@asColorProvider
}

class CountdownTimerGlanceWidget : GlanceAppWidget() {

    companion object {
        const val DEFAULT_TOTAL_SECONDS = 60
        const val PREFS_NAME = "countdown_timer_widget_prefs"
        const val KEY_REMAINING_MS = "remaining_ms"
        const val KEY_IS_RUNNING = "is_running"
        const val KEY_TARGET_END_TIME = "target_end_time_ms"

        fun getRemainingMs(context: Context): Long {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val isRunning = prefs.getBoolean(KEY_IS_RUNNING, false)
            return if (isRunning) {
                val targetEnd = prefs.getLong(KEY_TARGET_END_TIME, 0L)
                (targetEnd - System.currentTimeMillis()).coerceAtLeast(0L)
            } else {
                prefs.getLong(KEY_REMAINING_MS, DEFAULT_TOTAL_SECONDS * 1000L)
            }
        }

        fun isRunning(context: Context): Boolean =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_IS_RUNNING, false)
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val remainingMs = getRemainingMs(context)
        val running = isRunning(context)

        provideContent {
            GlanceTheme {
                CountdownTimerWidgetContent(
                    remainingMs = remainingMs,
                    isRunning = running,
                    totalSeconds = DEFAULT_TOTAL_SECONDS,
                )
            }
        }
    }
}

@Composable
private fun CountdownTimerWidgetContent(
    remainingMs: Long,
    isRunning: Boolean,
    totalSeconds: Int,
) {
    val remainingSeconds = (remainingMs / 1000L).toInt()
    val isDone = remainingSeconds == 0
    val progress = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds else 0f
    val percent = (progress * 100).toInt()

    val timerColor = when {
        progress > 0.5f -> Color(0xFF4CAF50)
        progress > 0.25f -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }
    val timerColorProvider = timerColor.asColorProvider()
    val bgColor = Color(0xFF1A1A2E).asColorProvider()
    val surfaceColor = Color(0xFF16213E).asColorProvider()
    val white = Color.White.asColorProvider()
    val whiteFaded = Color.White.copy(alpha = 0.6f).asColorProvider()

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(bgColor)
            .cornerRadius(20.dp)
            .padding(16.dp),
        contentAlignment = Alignment.TopStart,
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        ) {
            Text(
                text = "Countdown Timer",
                style = TextStyle(
                    color = white,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                ),
                modifier = GlanceModifier.fillMaxWidth(),
            )

            Spacer(GlanceModifier.height(8.dp))

            // Time display
            Text(
                text = "%d:%02d".format(remainingSeconds / 60, remainingSeconds % 60),
                style = TextStyle(
                    color = timerColorProvider,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
                modifier = GlanceModifier.fillMaxWidth(),
            )

            Spacer(GlanceModifier.height(2.dp))

            // Status + percentage
            Text(
                text = when {
                    isDone -> "Done! 🎉"
                    isRunning -> "▶  running  •  $percent%"
                    else -> "⏸  paused  •  $percent%"
                },
                style = TextStyle(
                    color = whiteFaded,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                ),
                modifier = GlanceModifier.fillMaxWidth(),
            )

            Spacer(GlanceModifier.height(12.dp))

            // Buttons
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically,
            ) {
                Button(
                    text = if (isRunning) "Pause" else "Start",
                    onClick = actionRunCallback<StartPauseTimerAction>(),
                    enabled = !isDone,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = timerColorProvider,
                        contentColor = white,
                    ),
                    modifier = GlanceModifier.defaultWeight(),
                )
                Spacer(GlanceModifier.width(8.dp))
                Button(
                    text = "Reset",
                    onClick = actionRunCallback<ResetTimerAction>(),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = surfaceColor,
                        contentColor = white,
                    ),
                    modifier = GlanceModifier.defaultWeight(),
                )
            }
        }
    }
}
