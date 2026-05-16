package jp.developer.bbee.richuidemo.widget

import android.content.Context
import androidx.core.content.edit
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback

class StartPauseTimerAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val prefs = context.getSharedPreferences(
            CountdownTimerGlanceWidget.PREFS_NAME,
            Context.MODE_PRIVATE,
        )
        val isRunning = prefs.getBoolean(CountdownTimerGlanceWidget.KEY_IS_RUNNING, false)
        val remainingMs = CountdownTimerGlanceWidget.getRemainingMs(context)

        if (remainingMs <= 0L) return

        if (isRunning) {
            prefs.edit {
                putBoolean(CountdownTimerGlanceWidget.KEY_IS_RUNNING, false)
                    .putLong(CountdownTimerGlanceWidget.KEY_REMAINING_MS, remainingMs)
            }
            CountdownTimerGlanceWidgetReceiver.cancelTick(context)
        } else {
            val targetEndTime = System.currentTimeMillis() + remainingMs
            prefs.edit {
                putBoolean(CountdownTimerGlanceWidget.KEY_IS_RUNNING, true)
                    .putLong(CountdownTimerGlanceWidget.KEY_TARGET_END_TIME, targetEndTime)
            }
            CountdownTimerGlanceWidgetReceiver.scheduleTick(context)
        }

        val widget = CountdownTimerGlanceWidget()
        GlanceAppWidgetManager(context)
            .getGlanceIds(CountdownTimerGlanceWidget::class.java)
            .forEach { id -> widget.update(context, id) }
    }
}
