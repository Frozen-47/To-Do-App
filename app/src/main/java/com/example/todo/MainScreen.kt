// app/src/main/java/com/example/todo/MainScreen.kt
package com.example.todo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
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

// ── Overview card (Minimal Design) ───────────────────────────────────────────

@Composable
private fun OverviewCard(tasks: List<Task>, colors: AppColors) {
    val completed = tasks.count { it.isCompleted }
    val total = tasks.size
    val progress = if (total > 0) completed.toFloat() / total else 0f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bg) // Completely seamless with the app background
            .navigationBarsPadding() // Adapts to system gestures/bars properly
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Daily Progress",
                    color = colors.fg,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "$completed / $total",
                    color = colors.mutedFg,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Ultra-thin, sleek linear progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)), // Softly rounded edges on the line
                color = colors.fg,
                trackColor = colors.border.copy(alpha = 0.3f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

// ── Tab row ───────────────────────────────────────────────────────────────────

private val TABS = listOf("STARRED" to "★", "ALL" to "ALL TASKS")

@Composable
private fun TaskTabs(
    currentTab: String,
    onTabSelected: (String) -> Unit,
    colors: AppColors,
) {
    val selectedIndex = TABS.indexOfFirst { it.first == currentTab }.coerceAtLeast(0)

    // Calculate the custom widths
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val starTabWidth = 72.dp // Narrow width just for the star
    val allTasksTabWidth = screenWidth - starTabWidth // The rest of the screen goes to ALL TASKS

    ScrollableTabRow(
        modifier = Modifier.fillMaxWidth().statusBarsPadding(),
        selectedTabIndex = selectedIndex,
        containerColor = colors.bg,
        contentColor = colors.fg,
        edgePadding = 0.dp,
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
                modifier = Modifier.width(if (key == "STARRED") starTabWidth else allTasksTabWidth),
                selected = selected,
                onClick = { onTabSelected(key) },
                text = {
                    Text(
                        text = label,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) colors.fg else colors.mutedFg,
                        letterSpacing = 1.sp,
                        fontSize = if (key == "STARRED") 16.sp else 18.sp
                    )
                },
            )
        }
    }
}