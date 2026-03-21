package com.example.todo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels // <-- ADD THIS IMPORT
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    // BULLETPROOF WAY to initialize the ViewModel
    private val taskViewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Pass the initialized ViewModel into your UI
            TerminalTodoApp(viewModel = taskViewModel)
        }
    }
}

@Composable
fun TerminalTodoApp(viewModel: TaskViewModel) { // <-- REMOVE the "= viewModel()" here
    val tasks by viewModel.tasks.collectAsState()
    var inputText by remember { mutableStateOf("") }

    // ... THE REST OF YOUR CODE STAYS EXACTLY THE SAME ...

    // Terminal Colors matching your CSS
    val bgDark = Color(0xFF000000)
    val textLight = Color(0xFFFFFFFF)
    val textGray = Color(0xFF666666)
    val deleteRed = Color(0xFFFF6B6B)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgDark)
            .padding(top = 48.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
    ) {
        // Header
        Text(
            text = "TASK MANAGER v4.7",
            color = textLight,
            fontFamily = FontFamily.Monospace,
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Input Area
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Text("➤ add: ", color = textGray, fontFamily = FontFamily.Monospace)

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(
                    color = textLight,
                    fontFamily = FontFamily.Monospace
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = textLight,
                    unfocusedIndicatorColor = textGray,
                    cursorColor = textLight
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (inputText.isNotBlank()) {
                            viewModel.addTask(inputText.trim())
                            inputText = ""
                        }
                    }
                )
            )
        }

        // Section Title
        Text(
            text = "ACTIVE TASKS",
            color = textGray,
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Divider(color = Color(0xFF333333), thickness = 1.dp, modifier = Modifier.padding(bottom = 16.dp))

        // Task List
        if (tasks.isEmpty()) {
            Text(
                text = "// no tasks found\n// add your first task above",
                color = textGray,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 20.dp)
            )
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(tasks) { task ->
                    TaskRow(
                        task = task,
                        onToggle = { viewModel.toggleTask(task) },
                        onDelete = { viewModel.deleteTask(task) },
                        textLight = textLight,
                        textGray = textGray,
                        deleteRed = deleteRed
                    )
                }
            }
        }

        // Stats Footer
        val completed = tasks.count { it.isCompleted }
        Text(
            text = "Total: ${tasks.size} | Completed: $completed | Pending: ${tasks.size - completed}",
            color = textLight,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
fun TaskRow(
    task: Task,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    textLight: Color,
    textGray: Color,
    deleteRed: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = if (task.isCompleted) "◉ " else "◯ ",
            color = if (task.isCompleted) textLight else textGray,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(end = 8.dp)
        )

        Text(
            text = task.text,
            color = if (task.isCompleted) textGray else textLight,
            fontFamily = FontFamily.Monospace,
            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "×",
            color = textGray,
            fontSize = 24.sp,
            modifier = Modifier
                .clickable { onDelete() }
                .padding(start = 16.dp, end = 8.dp)

        )
    }
}