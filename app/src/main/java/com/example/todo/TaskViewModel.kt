package com.example.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(private val dao: TaskDao) : ViewModel() {

    // The search query state ("grep" filter)
    val searchQuery = MutableStateFlow("")

    // Automatically updates the list whenever the database OR the search query changes
    @OptIn(ExperimentalCoroutinesApi::class)
    val tasks: StateFlow<List<Task>> = searchQuery
        .flatMapLatest { query: String -> dao.searchTasks(query) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<Task>())

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun addTask(text: String, details: String = "", priority: Int = 1) {
        viewModelScope.launch {
            dao.insertTask(Task(text = text, details = details, priority = priority))
        }
    }

    fun toggleTask(task: Task) {
        viewModelScope.launch {
            dao.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            dao.deleteTask(task)
        }
    }
}