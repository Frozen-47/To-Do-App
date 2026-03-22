package com.example.todo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

    // --- HIGH-TECH COLOR PALETTE & GRADIENTS ---
    val accentColor = Color(0xFF00FFCC) // Cyberpunk Neon Cyan
    val secondaryAccent = Color(0xFFB300FF) // Neon Purple

    // Deep tech gradient background
    val backgroundBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF0B0F19), Color(0xFF1A1A2E), Color(0xFF16213E))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFE0EAFC), Color(0xFFCFDEF3))
        )
    }

    val textColor = if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B)
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    val glassColor = if (isDark) Color(0xFFFFFFFF).copy(alpha = 0.05f) else Color(0xFFFFFFFF).copy(alpha = 0.6f)
    val glassBorder = if (isDark) Color(0xFFFFFFFF).copy(alpha = 0.1f) else Color(0xFFFFFFFF).copy(alpha = 0.4f)

    Box(modifier = Modifier.fillMaxSize().background(backgroundBrush)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column {
                    DashboardHeader(
                        tasks = tasks,
                        textColor = textColor,
                        accentColor = accentColor,
                        glassColor = glassColor,
                        glassBorder = glassBorder
                    )

                    // High-Tech Tab Row
                    TabRow(
                        selectedTabIndex = if (currentTab == "ALL") 0 else 1,
                        containerColor = Color.Transparent,
                        contentColor = accentColor,
                        indicator = { tabPositions ->
                            if (tabPositions.isNotEmpty()) {
                                TabRowDefaults.Indicator(
                                    Modifier.tabIndicatorOffset(tabPositions[if (currentTab == "ALL") 0 else 1]),
                                    color = accentColor
                                )
                            }
                        }
                    ) {
                        Tab(
                            selected = currentTab == "ALL",
                            onClick = { viewModel.setTab("ALL") },
                            text = { Text("ALL TASKS", fontWeight = FontWeight.Bold, color = if (currentTab == "ALL") accentColor else subTextColor) }
                        )
                        Tab(
                            selected = currentTab == "STARRED",
                            onClick = { viewModel.setTab("STARRED") },
                            text = { Text("STARRED", fontWeight = FontWeight.Bold, color = if (currentTab == "STARRED") accentColor else subTextColor) }
                        )
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddSheet = true },
                    containerColor = accentColor,
                    contentColor = Color(0xFF0B0F19),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    AnimatedVisibility(
                        visible = true,
                        exit = fadeOut() + shrinkVertically(animationSpec = tween(500))
                    ) {
                        GlassTaskCard(
                            task = task,
                            onToggle = { viewModel.toggleTask(task) },
                            onDelete = { viewModel.deleteTask(task) },
                            onToggleStar = { viewModel.toggleStar(task) },
                            accentColor = accentColor,
                            textColor = textColor,
                            subTextColor = subTextColor,
                            glassColor = glassColor,
                            glassBorder = glassBorder
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        if (showAddSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAddSheet = false },
                containerColor = if (isDark) Color(0xFF1A1A2E) else Color(0xFFF1F5F9),
                dragHandle = { BottomSheetDefaults.DragHandle(color = subTextColor) }
            ) {
                TechInputPrompt(
                    textColor = textColor,
                    subTextColor = subTextColor,
                    accentColor = accentColor,
                    glassColor = glassColor,
                    onSave = { title, details, priority, timestamp, recurrence ->
                        viewModel.addTask(
                            text = title,
                            details = details,
                            priority = priority,
                            dueDate = timestamp,
                            recurrence = recurrence
                        )
                        showAddSheet = false
                    }
                )
            }
        }
    }
}

@Composable
fun DashboardHeader(
    tasks: List<Task>,
    textColor: Color,
    accentColor: Color,
    glassColor: Color,
    glassBorder: Color
) {
    val completedCount = tasks.count { it.isCompleted }
    val totalCount = tasks.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(top = 32.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(glassColor)
            .border(1.dp, glassBorder, RoundedCornerShape(24.dp))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "System Status",
                color = accentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Task Execution",
                color = textColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.W800
            )
            Text(
                text = "$completedCount of $totalCount completed",
                color = textColor.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(64.dp),
            color = accentColor,
            strokeWidth = 6.dp,
            trackColor = textColor.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun GlassTaskCard(
    task: Task,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onToggleStar: () -> Unit,
    accentColor: Color,
    textColor: Color,
    subTextColor: Color,
    glassColor: Color,
    glassBorder: Color
) {
    val priorityColor = when (task.priority) {
        3 -> Color(0xFFFF3366) // High
        2 -> Color(0xFFFFBB00) // Medium
        else -> Color(0xFF00FFCC) // Low
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(glassColor)
            .border(1.dp, glassBorder, RoundedCornerShape(16.dp))
            .clickable { onToggle() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Priority Indicator Line
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(40.dp)
                .clip(CircleShape)
                .background(if (task.isCompleted) subTextColor.copy(alpha = 0.3f) else priorityColor)
        )

        Spacer(modifier = Modifier.width(12.dp))

        IconButton(
            onClick = onToggle,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = if (task.isCompleted) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = "Complete",
                tint = if (task.isCompleted) subTextColor else accentColor
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.text,
                fontSize = 16.sp,
                fontWeight = FontWeight.W600,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                color = if (task.isCompleted) subTextColor else textColor
            )

            if (task.details.isNotBlank()) {
                Text(
                    text = task.details,
                    fontSize = 13.sp,
                    color = subTextColor,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (task.dueDate != null) {
                // Formatting Date and Time together
                val formattedDate = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(task.dueDate))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = subTextColor, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = formattedDate, fontSize = 12.sp, color = subTextColor)
                }
            }

            if (task.recurrence != "NONE") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(Icons.Default.Repeat, contentDescription = null, tint = accentColor, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = task.recurrence, fontSize = 10.sp, color = accentColor, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Star Toggle
        IconButton(
            onClick = onToggleStar,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = if (task.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = "Star",
                tint = if (task.isStarred) Color(0xFFFFBB00) else subTextColor.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = subTextColor.copy(alpha = 0.5f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechInputPrompt(
    textColor: Color,
    subTextColor: Color,
    accentColor: Color,
    glassColor: Color,
    onSave: (String, String, Int, Long?, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var priority by remember { mutableIntStateOf(1) }
    var recurrence by remember { mutableStateOf("NONE") }

    val isDark = isSystemInDarkTheme()

    // Time & Date States
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

    Column(modifier = Modifier.fillMaxWidth().padding(20.dp).padding(bottom = 24.dp)) {

        // Priority Selector
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            PriorityButton("Low", 1, priority, Color(0xFF00FFCC)) { priority = 1 }
            PriorityButton("Med", 2, priority, Color(0xFFFFBB00)) { priority = 2 }
            PriorityButton("High", 3, priority, Color(0xFFFF3366)) { priority = 3 }
        }

        // Recurrence Selector
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            PriorityButton("Once", "NONE", recurrence, subTextColor) { recurrence = "NONE" }
            PriorityButton("Daily", "DAILY", recurrence, accentColor) { recurrence = "DAILY" }
            PriorityButton("Weekly", "WEEKLY", recurrence, accentColor) { recurrence = "WEEKLY" }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(glassColor)
                .border(1.dp, subTextColor.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .padding(8.dp)
        ) {
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Initialize new task...", color = subTextColor) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = accentColor,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                )
            )

            TextField(
                value = details,
                onValueChange = { details = it },
                placeholder = { Text("Append parameters...", color = subTextColor.copy(alpha = 0.5f), fontSize = 14.sp) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = accentColor,
                    focusedTextColor = subTextColor,
                    unfocusedTextColor = subTextColor
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Add Date",
                            tint = if (selectedDateMillis != null) accentColor else subTextColor
                        )
                    }
                    IconButton(onClick = { showTimePicker = true }) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = "Add Time",
                            tint = if (timePickerState.hour != 0 || timePickerState.minute != 0) accentColor else subTextColor
                        )
                    }
                }

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onSave(title.trim(), details.trim(), priority, getCombinedTimestamp(), recurrence)
                        }
                    },
                    enabled = title.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        disabledContainerColor = subTextColor.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Save Task",
                        tint = Color(0xFF0B0F19),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("EXECUTE", color = Color(0xFF0B0F19), fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK", color = accentColor) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = textColor) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog Custom Wrapper
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("OK", color = accentColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = textColor)
                }
            },
            text = {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = glassColor,
                        selectorColor = accentColor,
                        containerColor = Color.Transparent,
                        periodSelectorSelectedContainerColor = accentColor.copy(alpha = 0.2f),
                        periodSelectorSelectedContentColor = accentColor,
                        timeSelectorSelectedContainerColor = accentColor.copy(alpha = 0.2f),
                        timeSelectorSelectedContentColor = accentColor
                    )
                )
            },
            containerColor = if (isDark) Color(0xFF1A1A2E) else Color(0xFFF1F5F9)
        )
    }
}

// Overloaded generic PriorityButton for Int priorities (High/Med/Low)
@Composable
fun PriorityButton(text: String, level: Int, currentLevel: Int, color: Color, onClick: () -> Unit) {
    val isSelected = level == currentLevel
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent,
            contentColor = color
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) color else color.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text)
    }
}

// Overloaded generic PriorityButton for String recurrence (Once/Daily/Weekly)
@Composable
fun PriorityButton(text: String, level: String, currentLevel: String, color: Color, onClick: () -> Unit) {
    val isSelected = level == currentLevel
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent,
            contentColor = color
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) color else color.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text)
    }
}