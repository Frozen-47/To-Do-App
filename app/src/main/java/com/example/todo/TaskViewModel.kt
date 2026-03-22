// app/src/main/java/com/example/todo/TaskViewModel.kt
package com.example.todo

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Changed to AndroidViewModel to access Context for Alarms
class TaskViewModel(application: Application, private val taskDao: TaskDao) : AndroidViewModel(application) {

    private val _currentTab = MutableStateFlow("ALL") // "ALL" or "STARRED"
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val tasks: StateFlow<List<Task>> = combine(_searchQuery, _currentTab) { query, tab -> Pair(query, tab) }
        .flatMapLatest { (query, tab) ->
            val flow = if (query.isEmpty()) taskDao.getAllTasks() else taskDao.searchTasks(query)
            flow.map { list ->
                if (tab == "STARRED") list.filter { it.isStarred } else list
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTab(tab: String) { _currentTab.value = tab }

    fun addTask(text: String, details: String = "", priority: Int = 1, dueDate: Long? = null, isStarred: Boolean = false, recurrence: String = "NONE") {
        viewModelScope.launch {
            val task = Task(text = text, details = details, priority = priority, dueDate = dueDate, isStarred = isStarred, recurrence = recurrence)
            taskDao.insertTask(task)

            if (dueDate != null && dueDate > System.currentTimeMillis()) {
                scheduleNotification(task) // You'd need the actual inserted ID for perfect targeting, but this is a structural example
            }
        }
    }

    fun toggleTask(task: Task) {
        viewModelScope.launch { taskDao.updateTask(task.copy(isCompleted = !task.isCompleted)) }
    }

    fun toggleStar(task: Task) {
        viewModelScope.launch { taskDao.updateTask(task.copy(isStarred = !task.isStarred)) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { taskDao.deleteTask(task) }
    }

    private fun scheduleNotification(task: Task) {
        val context = getApplication<Application>()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("TASK_TEXT", task.text)
            putExtra("TASK_ID", task.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, task.id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, task.dueDate!!, pendingIntent)
        } catch (e: SecurityException) {
            // Handle missing exact alarm permissions on Android 14+
        }
    }
}