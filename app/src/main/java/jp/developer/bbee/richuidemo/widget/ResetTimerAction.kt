package jp.developer.bbee.richuidemo.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback

class ResetTimerAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val prefs = context.getSharedPreferences(
            CountdownTimerGlanceWidget.PREFS_NAME,
            Context.MODE_PRIVATE,
        )
        prefs.edit()
            .putLong(
                CountdownTimerGlanceWidget.KEY_REMAINING_MS,
                CountdownTimerGlanceWidget.DEFAULT_TOTAL_SECONDS * 1000L,
            )
            .putBoolean(CountdownTimerGlanceWidget.KEY_IS_RUNNING, false)
            .apply()

        CountdownTimerGlanceWidgetReceiver.cancelTick(context)
        val widget = CountdownTimerGlanceWidget()
        GlanceAppWidgetManager(context)
            .getGlanceIds(CountdownTimerGlanceWidget::class.java)
            .forEach { id -> widget.update(context, id) }
    }
}
