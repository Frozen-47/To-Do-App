package com.example.todo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todo.Task
import com.example.todo.ui.theme.AppColors
import java.text.SimpleDateFormat
import java.util.*

// File-level singleton — not recreated on each recomposition
private val dueDateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

@Composable
fun TaskCard(
    task: Task,
    colors: AppColors,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onToggleStar: () -> Unit,
) {
    val priorityAlpha = when (task.priority) {
        3 -> 1.0f
        2 -> 0.5f
        else -> 0.15f
    }

    // Formatted only when dueDate changes, not on every recomposition
    val formattedDue = remember(task.dueDate) {
        task.dueDate?.let { dueDateFormatter.format(Date(it)) }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .border(1.dp, colors.border, RectangleShape)
            .clickable(onClick = onToggle)
            .padding(end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Priority indicator bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .heightIn(min = 60.dp)
                .background(
                    if (task.isCompleted) colors.border
                    else colors.fg.copy(alpha = priorityAlpha)
                ),
        )

        Spacer(Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 16.dp),
        ) {
            Text(
                text = task.text,
                fontSize = 16.sp,
                fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Medium,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                color = if (task.isCompleted) colors.mutedFg else colors.fg,
            )

            if (task.details.isNotBlank()) {
                Text(
                    text = task.details,
                    fontSize = 13.sp,
                    color = colors.mutedFg,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 1,
                )
            }

            if (formattedDue != null) {
                Text(
                    text = "TGT: $formattedDue",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = colors.mutedFg,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        IconButton(onClick = onToggleStar, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = if (task.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = if (task.isStarred) "Unstar" else "Star",
                tint = if (task.isStarred) colors.fg else colors.mutedFg.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp),
            )
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Delete",
                tint = colors.mutedFg.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}