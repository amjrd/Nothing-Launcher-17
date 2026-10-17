package com.example.ui.components

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * 1. ودجت ساعة Nothing OS الرقمية بنمط النقاط (Dot Matrix)
 */
class NothingDigitalClockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateDigitalClock(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateDigitalClock(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val packageName = context.packageName
            val layoutId = context.resources.getIdentifier("widget_nothing_container", "layout", packageName)
            val imageId = context.resources.getIdentifier("widget_image_view", "id", packageName)

            if (layoutId == 0 || imageId == 0) return

            val views = RemoteViews(packageName, layoutId)

            // توليد صورة عالية الدقة للودجت بنمط Nothing OS
            val bitmap = renderDigitalClockBitmap(context)
            views.setImageViewBitmap(imageId, bitmap)

            // فتح تطبيق الساعة عند النقر على الودجت
            val clockIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CLOCK)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, clockIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(imageId, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun renderDigitalClockBitmap(context: Context): Bitmap {
            val width = 600
            val height = 300
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // خلفية دائرية شفافة داكنة بنمط Nothing
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#E6121212")
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(RectF(10f, 10f, width - 10f, height - 10f), 48f, 48f, bgPaint)

            // إطار خارجي رمادي خفيف
            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#33FFFFFF")
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
            canvas.drawRoundRect(RectF(10f, 10f, width - 10f, height - 10f), 48f, 48f, strokePaint)

            // رسم الوقت بنمط النقاط الأبيض/الأحمر
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val timeStr = timeFormat.format(Date())

            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 96f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(timeStr, width / 2f, height / 2f + 10f, textPaint)

            // رسم التاريخ بنمط أحمر وأبيض
            val dateFormat = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
            val dateStr = dateFormat.format(Date()).uppercase()

            val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#D71921") // Nothing Red
                textSize = 28f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(dateStr, width / 2f, height / 2f + 75f, datePaint)

            return bitmap
        }
    }
}

/**
 * 2. ودجت ساعة Nothing OS التناظرية (Analog Clock)
 */
class NothingAnalogClockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAnalogClock(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAnalogClock(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val packageName = context.packageName
            val layoutId = context.resources.getIdentifier("widget_nothing_container", "layout", packageName)
            val imageId = context.resources.getIdentifier("widget_image_view", "id", packageName)

            if (layoutId == 0 || imageId == 0) return

            val views = RemoteViews(packageName, layoutId)
            val bitmap = renderAnalogClockBitmap()
            views.setImageViewBitmap(imageId, bitmap)

            val clockIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CLOCK)
            val pendingIntent = PendingIntent.getActivity(
                context, 1, clockIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(imageId, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun renderAnalogClockBitmap(): Bitmap {
            val size = 400
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val center = size / 2f
            val radius = size / 2f - 20f

            // قرص الساعة الأسود
            val dialPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#F2000000")
                style = Paint.Style.FILL
            }
            canvas.drawCircle(center, center, radius, dialPaint)

            // إطار النقاط الدائري
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#40FFFFFF")
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
            canvas.drawCircle(center, center, radius, borderPaint)

            // نقاط مؤشرات الساعات الـ 12
            val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
            }
            for (i in 0 until 12) {
                val angle = Math.toRadians((i * 30).toDouble())
                val x = center + (radius - 25f) * sin(angle).toFloat()
                val y = center - (radius - 25f) * cos(angle).toFloat()
                val r = if (i % 3 == 0) 5f else 2.5f
                if (i == 0) dotPaint.color = Color.parseColor("#D71921") else dotPaint.color = Color.WHITE
                canvas.drawCircle(x, y, r, dotPaint)
            }

            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR)
            val minute = calendar.get(Calendar.MINUTE)

            // عقرب الساعات
            val hourAngle = Math.toRadians(((hour + minute / 60f) * 30).toDouble())
            val hourPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                strokeWidth = 10f
                strokeCap = Paint.Cap.ROUND
            }
            canvas.drawLine(
                center, center,
                center + (radius * 0.5f) * sin(hourAngle).toFloat(),
                center - (radius * 0.5f) * cos(hourAngle).toFloat(),
                hourPaint
            )

            // عقرب الدقائق (أحمر بنمط Nothing)
            val minAngle = Math.toRadians((minute * 6).toDouble())
            val minPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#D71921")
                strokeWidth = 6f
                strokeCap = Paint.Cap.ROUND
            }
            canvas.drawLine(
                center, center,
                center + (radius * 0.75f) * sin(minAngle).toFloat(),
                center - (radius * 0.75f) * cos(minAngle).toFloat(),
                minPaint
            )

            // نقطة المنتصف المركزية
            val centerDot = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
            canvas.drawCircle(center, center, 8f, centerDot)

            return bitmap
        }
    }
}
