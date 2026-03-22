package com.example.todo

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

// ── Action parameter keys ─────────────────────────────────────────────────────

private val taskIdKey = ActionParameters.Key<Int>("task_id")

// ── Colors ────────────────────────────────────────────────────────────────────

private object WidgetColors {
    val background    = ColorProvider(Color(0xFF0A0A0A))
    val surface       = ColorProvider(Color(0xFF141414))
    val border        = ColorProvider(Color(0xFF222222))
    val fg            = ColorProvider(Color(0xFFFFFFFF))
    val mutedFg       = ColorProvider(Color(0xFF666666))
    val dimFg         = ColorProvider(Color(0xFF333333))
    val accent        = ColorProvider(Color(0xFFFFFFFF))
    val priorityHigh  = ColorProvider(Color(0xFFFFFFFF))   // full opacity
    val priorityMed   = ColorProvider(Color(0xFF888888))   // 50%
    val priorityLow   = ColorProvider(Color(0xFF282828))   // 15%
    val completed     = ColorProvider(Color(0xFF222222))
}

private val dueDateFormatter = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())

// ── Widget ────────────────────────────────────────────────────────────────────

class TodoWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Fetch off the main thread
        val tasks = withContext(Dispatchers.IO) {
            AppDatabase.getDatabase(context).taskDao().getWidgetTasks()
        }

        val pending   = tasks.count { !it.isCompleted }
        val completed = tasks.count { it.isCompleted }
        val total     = tasks.size
        val progress  = if (total > 0) completed.toFloat() / total else 0f

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(WidgetColors.background)
                    .padding(0.dp),
            ) {
                // ── Header ────────────────────────────────────────────────────
                WidgetHeader(
                    pending = pending,
                    completed = completed,
                    total = total,
                    progress = progress,
                    context = context,
                )

                // ── Divider ───────────────────────────────────────────────────
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(WidgetColors.border),
                ) {}

                // ── Task list ─────────────────────────────────────────────────
                if (tasks.isEmpty()) {
                    EmptyState()
                } else {
                    Column(modifier = GlanceModifier.fillMaxSize().padding(8.dp)) {
                        tasks.forEach { task ->
                            TaskRow(task = task)
                            // Row separator
                            Box(
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(WidgetColors.border),
                            ) {}
                        }
                    }
                }
            }
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun WidgetHeader(
    pending: Int,
    completed: Int,
    total: Int,
    progress: Float,
    context: Context,
) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = "TASKS",
                style = TextStyle(
                    color = WidgetColors.mutedFg,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(GlanceModifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$pending",
                    style = TextStyle(
                        color = WidgetColors.fg,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Spacer(GlanceModifier.width(4.dp))
                Text(
                    text = "pending",
                    style = TextStyle(
                        color = WidgetColors.mutedFg,
                        fontSize = 11.sp,
                    ),
                )
            }
        }

        // Progress ring approximated as stacked text — Glance has no Canvas
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(progress * 100).toInt()}%",
                style = TextStyle(
                    color = WidgetColors.fg,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Text(
                text = "$completed/$total done",
                style = TextStyle(
                    color = WidgetColors.mutedFg,
                    fontSize = 10.sp,
                ),
            )
        }
    }
}

// ── Task row ──────────────────────────────────────────────────────────────────

@Composable
private fun TaskRow(task: Task) {
    val textColor   = if (task.isCompleted) WidgetColors.mutedFg else WidgetColors.fg
    val barColor    = when {
        task.isCompleted -> WidgetColors.completed
        task.priority == 3 -> WidgetColors.priorityHigh
        task.priority == 2 -> WidgetColors.priorityMed
        else -> WidgetColors.priorityLow
    }

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 7.dp)
            .clickable(
                actionRunCallback<ToggleTaskAction>(
                    actionParametersOf(taskIdKey to task.id)
                )
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Priority bar
        Box(
            modifier = GlanceModifier
                .width(3.dp)
                .height(36.dp)
                .background(barColor),
        ) {}

        Spacer(GlanceModifier.width(10.dp))

        // Checkbox glyph
        Text(
            text = if (task.isCompleted) "■" else "□",
            style = TextStyle(
                color = if (task.isCompleted) WidgetColors.mutedFg else WidgetColors.dimFg,
                fontSize = 13.sp,
            ),
        )

        Spacer(GlanceModifier.width(8.dp))

        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = task.text,
                style = TextStyle(
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Medium,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                ),
                maxLines = 1,
            )

            // Due date chip — only for incomplete tasks with a due date
            if (!task.isCompleted && task.dueDate != null) {
                val isOverdue = task.dueDate < System.currentTimeMillis()
                val label = dueDateFormatter.format(Date(task.dueDate))
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    text = if (isOverdue) "!! $label" else "→ $label",
                    style = TextStyle(
                        color = if (isOverdue) ColorProvider(Color(0xFFFF4444)) else WidgetColors.mutedFg,
                        fontSize = 10.sp,
                        fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal,
                    ),
                )
            }

            // Details line
            if (task.details.isNotBlank() && !task.isCompleted) {
                Text(
                    text = task.details,
                    style = TextStyle(color = WidgetColors.mutedFg, fontSize = 10.sp),
                    maxLines = 1,
                )
            }
        }

        // Star indicator
        if (task.isStarred && !task.isCompleted) {
            Text(
                text = "★",
                style = TextStyle(color = WidgetColors.fg, fontSize = 11.sp),
            )
            Spacer(GlanceModifier.width(6.dp))
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyState() {
    Column(
        modifier = GlanceModifier.fillMaxSize().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "[ ]",
            style = TextStyle(color = WidgetColors.dimFg, fontSize = 24.sp, fontWeight = FontWeight.Bold),
        )
        Spacer(GlanceModifier.height(8.dp))
        Text(
            text = "NO PENDING DIRECTIVES",
            style = TextStyle(color = WidgetColors.mutedFg, fontSize = 10.sp, fontWeight = FontWeight.Bold),
        )
    }
}

// ── Tap-to-toggle action ──────────────────────────────────────────────────────

class ToggleTaskAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val taskId = parameters[taskIdKey] ?: return
        withContext(Dispatchers.IO) {
            val dao = AppDatabase.getDatabase(context).taskDao()
            val task = dao.getTaskById(taskId) ?: return@withContext
            dao.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
        // Re-render all instances of this widget
        TodoWidget().updateAll(context)
    }
}

// ── Receiver ──────────────────────────────────────────────────────────────────

class TodoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodoWidget()
}
