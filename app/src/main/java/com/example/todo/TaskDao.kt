package com.example.todo

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY timestamp DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE text LIKE '%' || :query || '%' OR details LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchTasks(query: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks ORDER BY isStarred DESC, id DESC LIMIT 5")
    suspend fun getWidgetTasks(): List<Task>

    // Used by ToggleTaskAction in the widget
    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Int): Task?

    @Insert
    suspend fun insertTask(task: Task): Long

    @Delete
    suspend fun deleteTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)
}