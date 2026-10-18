package com.nothing.launcher

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

/**
 * Native RemoteViews widgets. TextClock and AnalogClock are system views, so
 * Android updates their displayed time without a periodic WorkManager/alarm loop.
 */
class NothingDigitalClockWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        manager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.widget_nothing_digital_clock)
            views.setOnClickPendingIntent(
                R.id.widget_time,
                clockPendingIntent(context, requestCode = id * 2)
            )
            views.setOnClickPendingIntent(
                R.id.widget_date,
                clockPendingIntent(context, requestCode = id * 2 + 1)
            )
            manager.updateAppWidget(id, views)
        }
    }
}

class NothingAnalogClockWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        manager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.widget_nothing_analog_clock)
            views.setOnClickPendingIntent(
                R.id.widget_analog_clock,
                clockPendingIntent(context, requestCode = id + 10000)
            )
            manager.updateAppWidget(id, views)
        }
    }
}

private fun clockPendingIntent(context: Context, requestCode: Int): PendingIntent {
    val intent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_APP_CLOCK)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    return PendingIntent.getActivity(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}
