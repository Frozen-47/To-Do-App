// app/src/main/java/com/example/todo/Task.kt
package com.example.todo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val details: String = "",
    val priority: Int = 1,
    val dueDate: Long? = null,
    val isCompleted: Boolean = false,
    val isStarred: Boolean = false, // NEW
    val recurrence: String = "NONE", // NEW: NONE, DAILY, WEEKLY
    val timestamp: Long = System.currentTimeMillis()
)