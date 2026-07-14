package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {

    // --- Journal Entries ---
    @Query("SELECT * FROM journal_entries ORDER BY date DESC")
    fun getAllJournalEntriesFlow(): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries ORDER BY date DESC")
    suspend fun getAllJournalEntries(): List<JournalEntry>

    @Query("SELECT * FROM journal_entries WHERE date = :date LIMIT 1")
    fun getJournalEntryFlow(date: String): Flow<JournalEntry?>

    @Query("SELECT * FROM journal_entries WHERE date = :date LIMIT 1")
    suspend fun getJournalEntry(date: String): JournalEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: JournalEntry)

    @Query("DELETE FROM journal_entries")
    suspend fun deleteAllJournalEntries()

    // --- Today's Tasks ---
    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY id ASC")
    fun getTasksForDateFlow(date: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY id ASC")
    suspend fun getTasksForDate(date: String): List<Task>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Int)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    // --- Habits ---
    @Query("SELECT * FROM habits ORDER BY id ASC")
    fun getAllHabitsFlow(): Flow<List<Habit>>

    @Query("SELECT * FROM habits ORDER BY id ASC")
    suspend fun getAllHabits(): List<Habit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits()

    // --- Habit Logs ---
    @Query("SELECT * FROM habit_logs")
    fun getAllHabitLogsFlow(): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    fun getHabitLogsForDateFlow(date: String): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId")
    suspend fun getLogsForHabit(habitId: Int): List<HabitLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitLog(log: HabitLog)

    @Delete
    suspend fun deleteHabitLog(log: HabitLog)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND date = :date")
    suspend fun deleteHabitLog(habitId: Int, date: String)

    @Query("DELETE FROM habit_logs")
    suspend fun deleteAllHabitLogs()
}
