package com.example.todo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val details: String = "",
    val priority: Int = 1, // 0=None, 1=Info(Green), 2=Warn(Yellow), 3=Crit(Red)
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)