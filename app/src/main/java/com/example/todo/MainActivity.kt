package com.example.todo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {

    private val factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val database = AppDatabase.getDatabase(applicationContext)
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(database.taskDao()) as T
        }
    }

    private val taskViewModel: TaskViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TerminalTodoApp(viewModel = taskViewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalTodoApp(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    var showBottomSheet by remember { mutableStateOf(false) }

    // DYNAMIC TERMINAL COLORS (Light & Dark Mode Support)
    val isDark = isSystemInDarkTheme()
    val bgScreen = if (isDark) Color(0xFF000000) else Color(0xFFE5E5E5)
    val bgTerminal = if (isDark) Color(0xFF000000) else Color(0xFFFFFFFF)
    val bgHeader = if (isDark) Color(0xFF111111) else Color(0xFFF0F0F0)
    val borderColor = if (isDark) Color(0xFF333333) else Color(0xFFCCCCCC)
    val textMain = if (isDark) Color(0xFFFFFFFF) else Color(0xFF000000)
    val textGray = if (isDark) Color(0xFF888888) else Color(0xFF666666)
    val textCompleted = if (isDark) Color(0xFF555555) else Color(0xFFAAAAAA)

    // Header Dots
    val dotRed = Color(0xFFFF5F56)
    val dotYellow = Color(0xFFFFBD2E)
    val dotGreen = Color(0xFF27CA3F)

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
            // Terminal Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgHeader)
                    .border(1.dp, borderColor)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(12.dp).background(dotRed, CircleShape))
                    Box(modifier = Modifier.size(12.dp).background(dotYellow, CircleShape))
                    Box(modifier = Modifier.size(12.dp).background(dotGreen, CircleShape))
                }
                Text(
                    text = "todo.exe",
                    color = textGray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f).padding(end = 40.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Terminal Body
            Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 24.dp)) {
                    Text("❯ ", color = textMain, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("TASK MANAGER v4.8", color = textMain, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }

                // Command to open the Advanced Add Menu
                Text(
                    text = "[ Click here to execute add_task.sh ]",
                    color = dotGreen,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showBottomSheet = true }
                        .padding(bottom = 24.dp)
                )

                Text(
                    text = "ACTIVE TASKS",
                    color = textGray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                HorizontalDivider(color = borderColor, thickness = 1.dp, modifier = Modifier.padding(bottom = 16.dp))

                if (tasks.isEmpty()) {
                    Text(
                        text = "// no tasks found",
                        color = textGray,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(tasks, key = { it.id }) { task ->
                            TaskRow(
                                task = task,
                                onToggle = { viewModel.toggleTask(task) },
                                onDelete = { viewModel.deleteTask(task) },
                                textMain = textMain,
                                textGray = textGray,
                                textCompleted = textCompleted
                            )
                        }
                    }
                }
            }
        }

        // Stats Footer
        val completed = tasks.count { it.isCompleted }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .background(bgTerminal, RoundedCornerShape(4.dp))
                .border(1.dp, borderColor, RoundedCornerShape(4.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "Total: ${tasks.size} | Completed: $completed | Pending: ${tasks.size - completed}",
                color = textMain,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Google Tasks-style Bottom Sheet, but styled like a Terminal
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            containerColor = bgTerminal,
            dragHandle = { Box(modifier = Modifier.padding(12.dp).size(40.dp, 4.dp).background(borderColor, RoundedCornerShape(2.dp))) }
        ) {
            TerminalInputForm(
                textMain = textMain,
                textGray = textGray,
                onSave = { title, details ->
                    viewModel.addTask(title, details)
                    showBottomSheet = false
                }
            )
        }
    }
}

@Composable
fun TaskRow(
    task: Task,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    textMain: Color,
    textGray: Color,
    textCompleted: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = if (task.isCompleted) "◉ " else "◯ ",
            color = if (task.isCompleted) textMain else textGray,
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp,
            modifier = Modifier.padding(end = 12.dp, top = 2.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.text,
                color = if (task.isCompleted) textCompleted else textMain,
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
            )
            // Advanced Details (Google Tasks feature)
            if (task.details.isNotBlank()) {
                Text(
                    text = "↳ ${task.details}",
                    color = textGray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Text(
            text = "×",
            color = textGray,
            fontSize = 26.sp,
            modifier = Modifier
                .clickable { onDelete() }
                .padding(start = 16.dp, end = 4.dp)
        )
    }
}

@Composable
fun TerminalInputForm(textMain: Color, textGray: Color, onSave: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 48.dp)
    ) {
        Text("➤ new_task:", color = textGray, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(bottom = 8.dp))

        TextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("Title...", fontFamily = FontFamily.Monospace) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(color = textMain, fontFamily = FontFamily.Monospace),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = textMain,
                unfocusedIndicatorColor = textGray,
                cursorColor = textMain
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("➤ details (optional):", color = textGray, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(bottom = 8.dp))

        TextField(
            value = details,
            onValueChange = { details = it },
            placeholder = { Text("Description...", fontFamily = FontFamily.Monospace) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(color = textMain, fontFamily = FontFamily.Monospace, fontSize = 14.sp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = textMain,
                unfocusedIndicatorColor = textGray,
                cursorColor = textMain
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { if (title.isNotBlank()) onSave(title.trim(), details.trim()) },
            enabled = title.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27CA3F)),
            modifier = Modifier.align(Alignment.End),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("COMMIT [ENTER]", color = Color.Black, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
    }
}
