// app/src/main/java/com/example/todo/TodoWidget.kt
package com.example.todo

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

class TodoWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF1A1A2E)) // High-tech dark background
                    .padding(16.dp)
            ) {
                Text(
                    text = "System Tasks",
                    style = TextStyle(color = ColorProvider(Color(0xFF00FFCC)))
                )
                // Note: In a production app, you would load real tasks from the DB here
                // using a Glance state definition or a flow.
                Text(
                    text = "Widget active. Open app to sync.",
                    style = TextStyle(color = ColorProvider(Color.White)),
                    modifier = GlanceModifier.padding(top = 8.dp)
                )
            }
        }
    }
}

class TodoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodoWidget()
}