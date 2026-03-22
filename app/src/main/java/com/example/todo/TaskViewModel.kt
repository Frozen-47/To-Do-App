// app/src/main/java/com/example/todo/TaskViewModel.kt
package com.example.todo

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TaskViewModel(
    application: Application,
    private val taskDao: TaskDao,
) : AndroidViewModel(application) {

    private val _currentTab = MutableStateFlow("ALL")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val tasks: StateFlow<List<Task>> = combine(_searchQuery, _currentTab) { query, tab ->
        query to tab
    }.flatMapLatest { (query, tab) ->
        val source = if (query.isEmpty()) taskDao.getAllTasks() else taskDao.searchTasks(query)
        // Filter for starred tab; sort is handled in the UI layer (display concern)
        source.map { list -> if (tab == "STARRED") list.filter { it.isStarred } else list }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun setTab(tab: String) {
        _currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addTask(
        text: String,
        details: String = "",
        priority: Int = 1,
        dueDate: Long? = null,
        isStarred: Boolean = false,
        recurrence: String = "NONE",
    ) {
        viewModelScope.launch {
            val task = Task(
                text = text,
                details = details,
                priority = priority,
                dueDate = dueDate,
                isStarred = isStarred,
                recurrence = recurrence,
            )
            val insertedId = taskDao.insertTask(task).toInt()

            if (dueDate != null && dueDate > System.currentTimeMillis()) {
                AlarmScheduler.schedule(getApplication(), task.copy(id = insertedId))
            }
            refreshWidget()
        }
    }

    fun toggleTask(task: Task) {
        viewModelScope.launch {
            taskDao.updateTask(task.copy(isCompleted = !task.isCompleted))
            refreshWidget()
        }
    }

    fun toggleStar(task: Task) {
        viewModelScope.launch {
            taskDao.updateTask(task.copy(isStarred = !task.isStarred))
            refreshWidget()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskDao.deleteTask(task)
            refreshWidget()
        }
    }

    private fun refreshWidget() {
        viewModelScope.launch {
            TodoWidget().updateAll(getApplication())
        }
    }
}

// ── Alarm scheduling extracted from ViewModel ─────────────────────────────────

object AlarmScheduler {
    fun schedule(context: Context, task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("TASK_TEXT", task.text)
            putExtra("TASK_ID", task.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                task.dueDate!!,
                pendingIntent,
            )
        } catch (_: SecurityException) {
            // SCHEDULE_EXACT_ALARM permission not granted (Android 12+)
            // Graceful degradation: alarm not set; consider showing a warning to the user
        }
    }

    fun cancel(context: Context, task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        ) ?: return
        alarmManager.cancel(pendingIntent)
    }
}