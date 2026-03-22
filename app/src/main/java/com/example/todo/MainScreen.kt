// app/src/main/java/com/example/todo/MainScreen.kt
package com.example.todo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todo.ui.components.TaskCard
import com.example.todo.ui.sheet.AddTaskSheet
import com.example.todo.ui.theme.AppColors
import com.example.todo.ui.theme.rememberAppColors

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun MainScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    val colors = rememberAppColors()

    val displayTasks = remember(tasks, currentTab) {
        if (currentTab == "ALL") {
            tasks.sortedWith(compareByDescending<Task> { it.isStarred }.thenByDescending { it.id })
        } else {
            tasks // ViewModel already filters to starred only
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        Scaffold(
            containerColor = colors.bg,
            topBar = {
                // Top tabs isolated for a clean header
                TaskTabs(currentTab = currentTab, onTabSelected = viewModel::setTab, colors = colors)
            },
            bottomBar = {
                // Overview card moved to the bottom bar
                OverviewCard(tasks = tasks, colors = colors)
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddSheet = true },
                    containerColor = colors.fg,
                    contentColor = colors.bg,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add task")
                }
            },
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(displayTasks, key = { it.id }) { task ->
                    AnimatedVisibility(
                        visible = true,
                        exit = fadeOut() + shrinkVertically(animationSpec = tween(300)),
                    ) {
                        TaskCard(
                            task = task,
                            colors = colors,
                            onToggle = { viewModel.toggleTask(task) },
                            onDelete = { viewModel.deleteTask(task) },
                            onToggleStar = { viewModel.toggleStar(task) },
                        )
                    }
                }
            }
        }

        if (showAddSheet) {
            AddTaskSheet(
                colors = colors,
                onSave = { title, details, priority, timestamp, recurrence ->
                    viewModel.addTask(title, details, priority, timestamp, recurrence = recurrence)
                    showAddSheet = false
                },
                onDismiss = { showAddSheet = false },
            )
        }
    }
}

// ── Overview card ─────────────────────────────────────────────────────────────

@Composable
private fun OverviewCard(tasks: List<Task>, colors: AppColors) {
    val completed = tasks.count { it.isCompleted }
    val total = tasks.size
    val progress = if (total > 0) completed.toFloat() / total else 0f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bg) // Block tasks from showing behind the bottom bar
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .navigationBarsPadding() // Adapts to system gestures/bars properly
                .border(1.dp, colors.border, RectangleShape)
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "OVERVIEW",
                    color = colors.mutedFg,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Monospace,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Task Progress",
                    color = colors.fg,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.W600,
                    letterSpacing = (-0.5).sp,
                )
                Text(
                    text = "$completed of $total completed",
                    color = colors.mutedFg,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(54.dp),
                color = colors.fg,
                strokeWidth = 3.dp,
                trackColor = colors.border,
            )
        }
    }
}

// ── Tab row ───────────────────────────────────────────────────────────────────

private val TABS = listOf("ALL" to "ALL TASKS", "STARRED" to "STARRED")

@Composable
private fun TaskTabs(
    currentTab: String,
    onTabSelected: (String) -> Unit,
    colors: AppColors,
) {
    val selectedIndex = TABS.indexOfFirst { it.first == currentTab }.coerceAtLeast(0)

    TabRow(
        selectedTabIndex = selectedIndex,
        containerColor = colors.bg,
        contentColor = colors.fg,
        divider = { HorizontalDivider(color = colors.border) },
        indicator = { positions ->
            if (positions.isNotEmpty()) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(positions[selectedIndex]),
                    color = colors.fg,
                    height = 2.dp,
                )
            }
        },
    ) {
        TABS.forEach { (key, label) ->
            val selected = currentTab == key
            Tab(
                selected = selected,
                onClick = { onTabSelected(key) },
                text = {
                    Text(
                        text = label,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) colors.fg else colors.mutedFg,
                        letterSpacing = 1.sp,
                    )
                },
            )
        }
    }
}