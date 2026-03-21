package com.example.todo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val details: String = "", // Added for task descriptions
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)