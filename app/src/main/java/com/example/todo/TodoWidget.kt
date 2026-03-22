// app/src/main/java/com/example/todo/TodoWidget.kt
package com.example.todo

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

class TodoWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // 1. Fetch live data from Room
        val database = AppDatabase.getDatabase(context)
        val tasks = database.taskDao().getWidgetTasks()

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color.Black) // Terminal minimal black
                    .padding(16.dp)
            ) {
                Text(
                    text = "SYSTEM TASKS",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = GlanceModifier.padding(bottom = 12.dp)
                )

                if (tasks.isEmpty()) {
                    Text(
                        text = "No pending directives.",
                        style = TextStyle(color = ColorProvider(Color.Gray), fontSize = 14.sp)
                    )
                } else {
                    tasks.forEach { task ->
                        Row(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            // Minimalist Terminal Checkbox
                            Text(
                                text = if (task.isCompleted) "[X] " else "[ ] ",
                                style = TextStyle(color = ColorProvider(if (task.isCompleted) Color.Gray else Color.White))
                            )
                            Text(
                                text = task.text,
                                style = TextStyle(color = ColorProvider(if (task.isCompleted) Color.Gray else Color.White))
                            )
                        }
                    }
                }
            }
        }
    }
}

class TodoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodoWidget()
}