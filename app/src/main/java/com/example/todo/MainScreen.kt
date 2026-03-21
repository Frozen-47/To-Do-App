package com.example.todo

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    var showAddSheet by remember { mutableStateOf(false) }

    val isDark = isSystemInDarkTheme()

    // --- SLEEK MINIMALIST COLOR PALETTE ---
    val accentColor = Color(0xFF10A37F) // Keeping the nice accent for checkboxes/buttons

    val bgColor = if (isDark) Color(0xFF212121) else Color(0xFFFFFFFF)
    val surfaceColor = if (isDark) Color(0xFF2F2F2F) else Color(0xFFF7F7F8)
    val textColor = if (isDark) Color(0xFFECECF1) else Color(0xFF353740)
    val subTextColor = if (isDark) Color(0xFFC5C5D2) else Color(0xFF8E8EA0)
    val dividerColor = if (isDark) Color(0xFF4D4D4F) else Color(0xFFE5E5E5)

    // Dynamic FAB Colors (White in Dark Theme, Black in Light Theme)
    val fabBgColor = if (isDark) Color.White else Color.Black
    val fabIconColor = if (isDark) Color.Black else Color.White

    Scaffold(
        containerColor = bgColor,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "Tasks",
                            fontWeight = FontWeight.W600,
                            color = textColor,
                            fontSize = 18.sp
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
                )
                HorizontalDivider(color = dividerColor, thickness = 1.dp)
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = fabBgColor,
                contentColor = fabIconColor,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(tasks, key = { it.id }) { task ->
                ModernTaskRow(
                    task = task,
                    onToggle = { viewModel.toggleTask(task) },
                    onDelete = { viewModel.deleteTask(task) },
                    accentColor = accentColor,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    surfaceColor = surfaceColor
                )
                HorizontalDivider(color = dividerColor, thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp, end = 16.dp))
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            containerColor = surfaceColor,
            dragHandle = { BottomSheetDefaults.DragHandle(color = subTextColor) }
        ) {
            ModernInputPrompt(
                textColor = textColor,
                subTextColor = subTextColor,
                accentColor = accentColor,
                bgColor = bgColor,
                onSave = { title, details, date ->
                    viewModel.addTask(title, details, 1, date)
                    showAddSheet = false
                }
            )
        }
    }
}

@Composable
fun ModernTaskRow(
    task: Task,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    accentColor: Color,
    textColor: Color,
    subTextColor: Color,
    surfaceColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        IconButton(
            onClick = onToggle,
            modifier = Modifier.size(24.dp).padding(top = 2.dp)
        ) {
            Icon(
                imageVector = if (task.isCompleted) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = "Complete",
                tint = if (task.isCompleted) accentColor else subTextColor
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.text,
                fontSize = 16.sp,
                fontWeight = FontWeight.W400,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                color = if (task.isCompleted) subTextColor else textColor
            )

            if (task.details.isNotBlank()) {
                Text(
                    text = task.details,
                    fontSize = 14.sp,
                    color = subTextColor,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (task.dueDate != null) {
                val formattedDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(task.dueDate))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(surfaceColor)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = subTextColor, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = formattedDate, fontSize = 12.sp, color = subTextColor)
                }
            }
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(24.dp).padding(top = 2.dp)
        ) {
            Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = subTextColor.copy(alpha = 0.5f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernInputPrompt(
    textColor: Color,
    subTextColor: Color,
    accentColor: Color,
    bgColor: Color,
    onSave: (String, String, Long?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 24.dp)) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(bgColor)
                .border(1.dp, subTextColor.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .padding(8.dp)
        ) {
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("What needs to be done?", color = subTextColor) },
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
                placeholder = { Text("Add context or details...", color = subTextColor.copy(alpha = 0.5f), fontSize = 14.sp) },
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Add Date",
                        tint = if (selectedDateMillis != null) accentColor else subTextColor
                    )
                }

                IconButton(
                    onClick = { if (title.isNotBlank()) onSave(title.trim(), details.trim(), selectedDateMillis) },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (title.isNotBlank()) accentColor else subTextColor.copy(alpha = 0.2f))
                        .size(36.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Save Task",
                        tint = if (title.isNotBlank()) Color.White else subTextColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

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
            },
            colors = DatePickerDefaults.colors(containerColor = bgColor)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    titleContentColor = textColor,
                    headlineContentColor = textColor,
                    weekdayContentColor = subTextColor,
                    dayContentColor = textColor,
                    selectedDayContainerColor = accentColor,
                    selectedDayContentColor = Color.White
                )
            )
        }
    }
}