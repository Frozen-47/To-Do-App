package com.example.todo.ui.sheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todo.ui.components.SegmentButton
import com.example.todo.ui.theme.AppColors
import java.text.SimpleDateFormat
import java.util.*

private val shortDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

// ── Public entry point ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskSheet(
    colors: AppColors,
    onSave: (title: String, details: String, priority: Int, timestamp: Long?, recurrence: String) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.bg,
        dragHandle = null,
        shape = RectangleShape,
    ) {
        AddTaskForm(colors = colors, onSave = onSave, onCancel = onDismiss)
    }
}

// ── Form ──────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTaskForm(
    colors: AppColors,
    onSave: (String, String, Int, Long?, String) -> Unit,
    onCancel: () -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var priority by remember { mutableIntStateOf(1) }

    // Schedule state — kept here so combinedTimestamp() can read both together
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    val timePickerState = rememberTimePickerState()

    fun combinedTimestamp(): Long? {
        val base = selectedDateMillis ?: return null
        return Calendar.getInstance().apply {
            timeInMillis = base
            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
            set(Calendar.MINUTE, timePickerState.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding(),
    ) {
        SheetLabel("INITIALIZE TASK", colors)

        Spacer(Modifier.height(16.dp))

        MonoInput(
            value = title,
            onValueChange = { title = it },
            placeholder = "Task directive...",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            underlineColor = colors.fg,
            colors = colors,
        )

        Spacer(Modifier.height(20.dp))

        MonoInput(
            value = details,
            onValueChange = { details = it },
            placeholder = "Append parameters (optional)...",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            underlineColor = colors.border,
            colors = colors,
        )

        Spacer(Modifier.height(32.dp))

        SheetLabel("SCHEDULE (OPTIONAL)", colors)
        Spacer(Modifier.height(12.dp))
        SchedulePicker(
            selectedDateMillis = selectedDateMillis,
            timePickerState = timePickerState,
            onDateSelected = { selectedDateMillis = it },
            colors = colors,
        )

        Spacer(Modifier.height(24.dp))

        SheetLabel("PRIORITY LEVEL", colors)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1 to "LOW", 2 to "MED", 3 to "HIGH").forEach { (level, label) ->
                SegmentButton(
                    text = label,
                    isSelected = priority == level,
                    fgColor = colors.fg,
                    borderColor = colors.border,
                    modifier = Modifier.weight(1f),
                    onClick = { priority = level },
                )
            }
        }

        Spacer(Modifier.height(40.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(
                onClick = onCancel,
                border = BorderStroke(1.dp, colors.border),
                shape = RectangleShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.fg),
            ) {
                Text("CANCEL", letterSpacing = 1.sp, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            }

            Spacer(Modifier.width(12.dp))

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title.trim(), details.trim(), priority, combinedTimestamp(), "NONE")
                    }
                },
                enabled = title.isNotBlank(),
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.fg,
                    contentColor = colors.bg,
                    disabledContainerColor = colors.border,
                ),
            ) {
                Text("SAVE", letterSpacing = 1.sp, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            }
        }
    }
}

// ── Schedule picker ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SchedulePicker(
    selectedDateMillis: Long?,
    timePickerState: TimePickerState,
    onDateSelected: (Long?) -> Unit,
    colors: AppColors,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    // datePickerState lives here, not in AddTaskForm — its UI concern alone
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)

    val dateLabel = selectedDateMillis?.let { shortDateFormatter.format(Date(it)) } ?: "YYYY-MM-DD"
    val timeHasValue = timePickerState.hour != 0 || timePickerState.minute != 0
    val timeLabel = if (timeHasValue) {
        String.format(Locale.getDefault(), "%02d:%02d", timePickerState.hour, timePickerState.minute)
    } else "HH:MM"

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SegmentButton(
            text = dateLabel,
            isSelected = selectedDateMillis != null,
            fgColor = colors.fg,
            borderColor = colors.border,
            modifier = Modifier.weight(1f),
            onClick = { showDatePicker = true },
        )
        SegmentButton(
            text = timeLabel,
            isSelected = timeHasValue,
            fgColor = colors.fg,
            borderColor = colors.border,
            modifier = Modifier.weight(1f),
            onClick = { showTimePicker = true },
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                    showDatePicker = false
                }) { Text("CONFIRM", color = colors.fg, fontFamily = FontFamily.Monospace) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("CANCEL", color = colors.mutedFg, fontFamily = FontFamily.Monospace)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = colors.bg),
            shape = RectangleShape,
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("CONFIRM", color = colors.fg, fontFamily = FontFamily.Monospace)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("CANCEL", color = colors.mutedFg, fontFamily = FontFamily.Monospace)
                }
            },
            text = { TimePicker(state = timePickerState) },
            containerColor = colors.bg,
            shape = RectangleShape,
        )
    }
}

// ── Private helpers ───────────────────────────────────────────────────────────

@Composable
private fun SheetLabel(text: String, colors: AppColors) {
    Text(
        text = text,
        color = colors.mutedFg,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
        fontFamily = FontFamily.Monospace,
    )
}

@Composable
private fun MonoInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontWeight: FontWeight,
    underlineColor: androidx.compose.ui.graphics.Color,
    colors: AppColors,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(color = colors.fg, fontSize = fontSize, fontWeight = fontWeight),
        cursorBrush = SolidColor(colors.fg),
        decorationBox = { innerTextField ->
            Column(Modifier.fillMaxWidth()) {
                if (value.isEmpty()) {
                    Text(placeholder, color = colors.mutedFg, fontSize = fontSize)
                } else {
                    innerTextField()
                }
                HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = underlineColor)
            }
        },
    )
}