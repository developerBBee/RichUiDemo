package jp.developer.bbee.richuidemo.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.content.edit

class CountdownTimerGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CountdownTimerGlanceWidget()

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("CountdownTimerWidget", "Received intent: ${intent.action}")
        super.onReceive(context, intent)
        if (intent.action == TICK_ACTION) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    handleTick(context)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    private suspend fun handleTick(context: Context) {
        val prefs = context.getSharedPreferences(
            CountdownTimerGlanceWidget.PREFS_NAME,
            Context.MODE_PRIVATE,
        )
        val isRunning = prefs.getBoolean(CountdownTimerGlanceWidget.KEY_IS_RUNNING, false)
        if (!isRunning) return

        val targetEnd = prefs.getLong(CountdownTimerGlanceWidget.KEY_TARGET_END_TIME, 0L)
        val remainingMs = targetEnd - System.currentTimeMillis()

        if (remainingMs <= 0L) {
            prefs.edit {
                putLong(CountdownTimerGlanceWidget.KEY_REMAINING_MS, 0L)
                    .putBoolean(CountdownTimerGlanceWidget.KEY_IS_RUNNING, false)
            }
        } else {
            scheduleTick(context)
        }

        // Update all widget instances
        val ids = GlanceAppWidgetManager(context)
            .getGlanceIds(CountdownTimerGlanceWidget::class.java)
        Log.d("CountdownTimerWidget", "Tick: remainingMs=$remainingMs, updating ${ids.size} widgets")
        ids.forEach { id -> glanceAppWidget.update(context, id) }
        Log.d("CountdownTimerWidget", "Tick handling complete")
    }

    companion object {
        const val TICK_ACTION = "jp.developer.bbee.richuidemo.widget.TIMER_TICK"
        private const val TICK_REQUEST_CODE = 9001
        // Android 12+ throttles background exact alarms to ~5s; match that to avoid silent deferral
        const val TICK_INTERVAL_MS = 5_000L

        fun scheduleTick(context: Context) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val triggerAt = System.currentTimeMillis() + TICK_INTERVAL_MS
            val pi = buildTickIntent(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                // Exact alarm permission not granted; use inexact alarm as fallback
                Log.d("CountdownTimerWidget", "Scheduling inexact alarm for tick at $triggerAt")
                am.set(AlarmManager.RTC, triggerAt, pi)
            } else {
                Log.d("CountdownTimerWidget", "Scheduling exact alarm for tick at $triggerAt")
                am.setExact(AlarmManager.RTC, triggerAt, pi)
            }
        }

        fun cancelTick(context: Context) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.cancel(buildTickIntent(context))
        }

        private fun buildTickIntent(context: Context): PendingIntent {
            val intent = Intent(context, CountdownTimerGlanceWidgetReceiver::class.java).apply {
                action = TICK_ACTION
            }
            return PendingIntent.getBroadcast(
                context,
                TICK_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
}
