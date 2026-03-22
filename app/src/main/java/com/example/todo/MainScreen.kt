package com.example.todo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }

    val isDark = isSystemInDarkTheme()

    // --- STRICT MONOCHROME PALETTE ---
    val bgColor = if (isDark) Color(0xFF000000) else Color(0xFFFFFFFF)
    val fgColor = if (isDark) Color(0xFFFFFFFF) else Color(0xFF000000)
    val mutedFgColor = if (isDark) Color(0xFF888888) else Color(0xFF666666)
    val borderColor = if (isDark) Color(0xFF222222) else Color(0xFFE0E0E0)

    // Sort logic: If ALL tab, starred items go to the top.
    val displayTasks = remember(tasks, currentTab) {
        if (currentTab == "ALL") {
            tasks.sortedWith(compareByDescending<Task> { it.isStarred }.thenByDescending { it.id })
        } else {
            tasks // Viewmodel already filters for "STARRED"
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        Scaffold(
            containerColor = bgColor,
            topBar = {
                Column(modifier = Modifier.background(bgColor)) {
                    TerminalDashboard(
                        tasks = tasks,
                        fgColor = fgColor,
                        mutedFgColor = mutedFgColor,
                        borderColor = borderColor
                    )

                    // Minimalist Tabs
                    TabRow(
                        selectedTabIndex = if (currentTab == "ALL") 0 else 1,
                        containerColor = bgColor,
                        contentColor = fgColor,
                        divider = { HorizontalDivider(color = borderColor) },
                        indicator = { tabPositions ->
                            if (tabPositions.isNotEmpty()) {
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[if (currentTab == "ALL") 0 else 1]),
                                    color = fgColor,
                                    height = 2.dp
                                )
                            }
                        }
                    ) {
                        Tab(
                            selected = currentTab == "ALL",
                            onClick = { viewModel.setTab("ALL") },
                            text = {
                                Text(
                                    text = "ALL TASKS",
                                    fontWeight = if (currentTab == "ALL") FontWeight.Bold else FontWeight.Normal,
                                    color = if (currentTab == "ALL") fgColor else mutedFgColor,
                                    letterSpacing = 1.sp
                                )
                            }
                        )
                        Tab(
                            selected = currentTab == "STARRED",
                            onClick = { viewModel.setTab("STARRED") },
                            text = {
                                Text(
                                    text = "STARRED",
                                    fontWeight = if (currentTab == "STARRED") FontWeight.Bold else FontWeight.Normal,
                                    color = if (currentTab == "STARRED") fgColor else mutedFgColor,
                                    letterSpacing = 1.sp
                                )
                            }
                        )
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddSheet = true },
                    containerColor = fgColor,
                    contentColor = bgColor,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayTasks, key = { it.id }) { task ->
                    AnimatedVisibility(
                        visible = true,
                        exit = fadeOut() + shrinkVertically(animationSpec = tween(300))
                    ) {
                        TerminalTaskCard(
                            task = task,
                            onToggle = { viewModel.toggleTask(task) },
                            onDelete = { viewModel.deleteTask(task) },
                            onToggleStar = { viewModel.toggleStar(task) },
                            fgColor = fgColor,
                            mutedFgColor = mutedFgColor,
                            borderColor = borderColor
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        if (showAddSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAddSheet = false },
                containerColor = bgColor,
                dragHandle = null,
                shape = RectangleShape
            ) {
                TerminalInputPrompt(
                    fgColor = fgColor,
                    mutedFgColor = mutedFgColor,
                    borderColor = borderColor,
                    bgColor = bgColor,
                    onSave = { title, details, priority, timestamp, recurrence ->
                        viewModel.addTask(title, details, priority, timestamp, recurrence = recurrence)
                        showAddSheet = false
                    },
                    onCancel = { showAddSheet = false }
                )
            }
        }
    }
}

@Composable
fun TerminalDashboard(
    tasks: List<Task>,
    fgColor: Color,
    mutedFgColor: Color,
    borderColor: Color
) {
    val completedCount = tasks.count { it.isCompleted }
    val totalCount = tasks.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(top = 32.dp)
            .background(Color.Transparent)
            .border(1.dp, borderColor, RectangleShape)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "OVERVIEW",
                color = mutedFgColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Task Progress",
                color = fgColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.W600,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "$completedCount of $totalCount completed",
                color = mutedFgColor,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(54.dp),
            color = fgColor,
            strokeWidth = 3.dp,
            trackColor = borderColor
        )
    }
}

@Composable
fun TerminalTaskCard(
    task: Task,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onToggleStar: () -> Unit,
    fgColor: Color,
    mutedFgColor: Color,
    borderColor: Color
) {
    val priorityAlpha = when (task.priority) {
        3 -> 1.0f
        2 -> 0.5f
        else -> 0.15f
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .border(1.dp, borderColor, RectangleShape)
            .clickable { onToggle() }
            .padding(end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Priority Indicator Bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .heightIn(min = 60.dp)
                .background(if (task.isCompleted) borderColor else fgColor.copy(alpha = priorityAlpha))
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f).padding(vertical = 16.dp)) {
            Text(
                text = task.text,
                fontSize = 16.sp,
                fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Medium,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                color = if (task.isCompleted) mutedFgColor else fgColor
            )

            if (task.details.isNotBlank()) {
                Text(
                    text = task.details,
                    fontSize = 13.sp,
                    color = mutedFgColor,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 1
                )
            }

            // --- NEW: DISPLAY TARGET DATE/TIME ---
            if (task.dueDate != null) {
                val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(task.dueDate))
                Text(
                    text = "TGT: $dateStr",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = mutedFgColor,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Action Icons
        IconButton(onClick = onToggleStar, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = if (task.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = "Star",
                tint = if (task.isStarred) fgColor else mutedFgColor.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Outlined.Close,
                contentDescription = "Delete",
                tint = mutedFgColor.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalInputPrompt(
    fgColor: Color,
    mutedFgColor: Color,
    borderColor: Color,
    bgColor: Color,
    onSave: (String, String, Int, Long?, String) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var priority by remember { mutableIntStateOf(1) }

    // --- NEW: TIME & DATE STATES ---
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState()

    // Logic to combine Date and Time
    fun getCombinedTimestamp(): Long? {
        if (selectedDateMillis == null) return null
        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis!!
            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
            set(Calendar.MINUTE, timePickerState.minute)
            set(Calendar.SECOND, 0)
        }
        return calendar.timeInMillis
    }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp)
        .padding(bottom = 24.dp)
    ) {
        Text(
            text = "INITIALIZE TASK",
            color = mutedFgColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Title Input
        BasicTextField(
            value = title,
            onValueChange = { title = it },
            textStyle = TextStyle(
                color = fgColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            ),
            cursorBrush = SolidColor(fgColor),
            decorationBox = { innerTextField ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (title.isEmpty()) {
                        Text("Task directive...", color = mutedFgColor, fontSize = 20.sp)
                    } else {
                        innerTextField()
                    }
                    HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = fgColor)
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Details Input
        BasicTextField(
            value = details,
            onValueChange = { details = it },
            textStyle = TextStyle(
                color = fgColor,
                fontSize = 14.sp
            ),
            cursorBrush = SolidColor(fgColor),
            decorationBox = { innerTextField ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (details.isEmpty()) {
                        Text("Append parameters (optional)...", color = mutedFgColor, fontSize = 14.sp)
                    } else {
                        innerTextField()
                    }
                    HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = borderColor)
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- NEW: SCHEDULE SELECTOR ---
        Text(
            text = "SCHEDULE (OPTIONAL)",
            color = mutedFgColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val dateLabel = if (selectedDateMillis != null) {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(selectedDateMillis!!))
            } else "YYYY-MM-DD"

            val timeLabel = if (timePickerState.hour != 0 || timePickerState.minute != 0) {
                String.format(Locale.getDefault(), "%02d:%02d", timePickerState.hour, timePickerState.minute)
            } else "HH:MM"

            SegmentButton(dateLabel, selectedDateMillis != null, fgColor, borderColor) { showDatePicker = true }
            SegmentButton(timeLabel, timePickerState.hour != 0 || timePickerState.minute != 0, fgColor, borderColor) { showTimePicker = true }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "PRIORITY LEVEL",
            color = mutedFgColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SegmentButton("LOW", priority == 1, fgColor, borderColor) { priority = 1 }
            SegmentButton("MED", priority == 2, fgColor, borderColor) { priority = 2 }
            SegmentButton("HIGH", priority == 3, fgColor, borderColor) { priority = 3 }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Action Buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(
                onClick = onCancel,
                border = BorderStroke(1.dp, borderColor),
                shape = RectangleShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = fgColor)
            ) {
                Text("CANCEL", letterSpacing = 1.sp, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title.trim(), details.trim(), priority, getCombinedTimestamp(), "NONE")
                    }
                },
                enabled = title.isNotBlank(),
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = fgColor,
                    contentColor = bgColor,
                    disabledContainerColor = borderColor
                )
            ) {
                Text("SAVE", letterSpacing = 1.sp, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            }
        }
    }

    // --- NEW: PICKER DIALOGS ---
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("CONFIRM", color = fgColor, fontFamily = FontFamily.Monospace) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("CANCEL", color = mutedFgColor, fontFamily = FontFamily.Monospace) }
            },
            colors = DatePickerDefaults.colors(containerColor = bgColor),
            shape = RectangleShape
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("CONFIRM", color = fgColor, fontFamily = FontFamily.Monospace)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("CANCEL", color = mutedFgColor, fontFamily = FontFamily.Monospace)
                }
            },
            text = { TimePicker(state = timePickerState) },
            containerColor = bgColor,
            shape = RectangleShape
        )
    }
}

@Composable
fun RowScope.SegmentButton(text: String, isSelected: Boolean, fgColor: Color, borderColor: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f)
            .clickable { onClick() }
            .border(1.dp, if (isSelected) fgColor else borderColor, RectangleShape)
            .background(if (isSelected) fgColor else Color.Transparent)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) (if (fgColor == Color.White) Color.Black else Color.White) else fgColor,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
    }
}