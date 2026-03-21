package com.example.todo

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showBottomSheet by remember { mutableStateOf(false) }

    // Dynamic Theming
    val isDark = isSystemInDarkTheme()
    val bgScreen = if (isDark) Color(0xFF000000) else Color(0xFFE5E5E5)
    val bgTerminal = if (isDark) Color(0xFF000000) else Color(0xFFFFFFFF)
    val bgHeader = if (isDark) Color(0xFF111111) else Color(0xFFF0F0F0)
    val borderColor = if (isDark) Color(0xFF333333) else Color(0xFFCCCCCC)
    val textMain = if (isDark) Color(0xFFFFFFFF) else Color(0xFF000000)
    val textGray = if (isDark) Color(0xFF888888) else Color(0xFF666666)

    val colorCrit = Color(0xFFFF5F56) // Red
    val colorWarn = Color(0xFFFFBD2E) // Yellow
    val colorInfo = Color(0xFF27CA3F) // Green

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgScreen)
            .padding(top = 50.dp, bottom = 20.dp, start = 16.dp, end = 16.dp)
    ) {
        // --- TERMINAL WINDOW ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(2.dp, borderColor, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(bgTerminal)
        ) {
            // HEADER BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgHeader)
                    .border(1.dp, borderColor)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(12.dp).background(colorCrit, CircleShape))
                    Box(modifier = Modifier.size(12.dp).background(colorWarn, CircleShape))
                    Box(modifier = Modifier.size(12.dp).background(colorInfo, CircleShape))
                }
                Text("todo.exe v1.0", color = textGray, fontFamily = FontFamily.Monospace, fontSize = 14.sp, modifier = Modifier.weight(1f).padding(end = 40.dp), textAlign = TextAlign.Center)
            }

            // TERMINAL BODY
            Column(modifier = Modifier.padding(20.dp).fillMaxSize()) {

                // Boot Sequence Text
                Text("SYSTEM BOOT ... OK", color = textGray, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                Text("LOADING DAEMONS ... OK", color = textGray, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(16.dp))

                // Action Bar (Grep & Add)
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("❯ grep: ", color = textMain, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    TextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("filter_tasks...", color = textGray, fontFamily = FontFamily.Monospace) },
                        textStyle = LocalTextStyle.current.copy(color = colorWarn, fontFamily = FontFamily.Monospace),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = textMain,
                            unfocusedIndicatorColor = borderColor
                        ),
                        singleLine = true
                    )
                }

                Text(
                    text = "[ + EXECUTE add_task.sh ]",
                    color = colorInfo,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .clickable { showBottomSheet = true }
                )

                HorizontalDivider(color = borderColor, thickness = 1.dp, modifier = Modifier.padding(bottom = 12.dp))

                // List
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(tasks, key = { it.id }) { task ->
                        SwipeToExecuteTask(
                            task = task,
                            onToggle = { viewModel.toggleTask(task) },
                            onDelete = { viewModel.deleteTask(task) },
                            textMain = textMain,
                            textGray = textGray,
                            colorCrit = colorCrit,
                            colorWarn = colorWarn,
                            colorInfo = colorInfo
                        )
                    }
                }
            }
        }

        // --- ASCII PROGRESS FOOTER ---
        val total = tasks.size
        val completed = tasks.count { it.isCompleted }
        val percentage = if (total == 0) 0f else (completed.toFloat() / total) * 100
        val bars = (percentage / 10).roundToInt()

        val progressString = "[" + "█".repeat(bars) + "░".repeat(10 - bars) + "] ${percentage.toInt()}%"

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .background(bgTerminal, RoundedCornerShape(4.dp))
                .border(1.dp, borderColor, RoundedCornerShape(4.dp))
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("PROCESS PROGRESS", color = textGray, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                Text(progressString, color = if (percentage == 100f) colorInfo else textMain, fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            containerColor = bgTerminal
        ) {
            AdvancedInputForm(textMain, textGray, colorCrit, colorWarn, colorInfo) { title, details, prio ->
                viewModel.addTask(title, details, prio)
                showBottomSheet = false
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToExecuteTask(task: Task, onToggle: () -> Unit, onDelete: () -> Unit, textMain: Color, textGray: Color, colorCrit: Color, colorWarn: Color, colorInfo: Color) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                Modifier.fillMaxSize().padding(vertical = 8.dp).background(Color(0x33FF5F56)).padding(end = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text("sudo rm -rf", color = colorCrit, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        },
        content = {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.Transparent).clickable { onToggle() }.padding(vertical = 12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = if (task.isCompleted) "◉ " else "◯ ",
                    color = if (task.isCompleted) textGray else textMain,
                    fontFamily = FontFamily.Monospace, fontSize = 16.sp, modifier = Modifier.padding(end = 12.dp, top = 2.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Priority Badge
                        val prioColor = when(task.priority) { 3 -> colorCrit; 2 -> colorWarn; 1 -> colorInfo; else -> textGray }
                        val prioText = when(task.priority) { 3 -> "[CRIT]"; 2 -> "[WARN]"; 1 -> "[INFO]"; else -> "" }

                        if (prioText.isNotEmpty()) {
                            Text("$prioText ", color = prioColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = task.text,
                            color = if (task.isCompleted) textGray else textMain,
                            fontFamily = FontFamily.Monospace, fontSize = 16.sp,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                        )
                    }
                    if (task.details.isNotBlank()) {
                        Text("↳ ${task.details}", color = textGray, fontFamily = FontFamily.Monospace, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    )
}

@Composable
fun AdvancedInputForm(textMain: Color, textGray: Color, colorCrit: Color, colorWarn: Color, colorInfo: Color, onSave: (String, String, Int) -> Unit) {
    var title by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableIntStateOf(1) } // Default INFO

    Column(modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 48.dp)) {
        TextField(
            value = title, onValueChange = { title = it },
            placeholder = { Text("Task argument...", fontFamily = FontFamily.Monospace) },
            modifier = Modifier.fillMaxWidth(), textStyle = LocalTextStyle.current.copy(color = textMain, fontFamily = FontFamily.Monospace),
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = textMain, cursorColor = textMain),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = details, onValueChange = { details = it },
            placeholder = { Text("--details=\"...\"", fontFamily = FontFamily.Monospace) },
            modifier = Modifier.fillMaxWidth(), textStyle = LocalTextStyle.current.copy(color = textMain, fontFamily = FontFamily.Monospace, fontSize = 14.sp),
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = textMain, cursorColor = textMain)
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text("SET --PRIORITY:", color = textGray, fontFamily = FontFamily.Monospace, fontSize = 12.sp)

        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PriorityButton("INFO", colorInfo, selectedPriority == 1) { selectedPriority = 1 }
            PriorityButton("WARN", colorWarn, selectedPriority == 2) { selectedPriority = 2 }
            PriorityButton("CRIT", colorCrit, selectedPriority == 3) { selectedPriority = 3 }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { if (title.isNotBlank()) onSave(title.trim(), details.trim(), selectedPriority) },
            enabled = title.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = colorInfo),
            modifier = Modifier.align(Alignment.End), shape = RoundedCornerShape(4.dp)
        ) {
            Text("COMMIT [ENTER]", color = Color.Black, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PriorityButton(label: String, color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .border(1.dp, if (isSelected) color else Color.DarkGray, RoundedCornerShape(4.dp))
            .background(if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(label, color = if (isSelected) color else Color.Gray, fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}