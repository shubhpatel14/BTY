package com.example.data.repository

import com.example.data.database.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class JournalRepository(private val journalDao: JournalDao) {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // --- Journal Entries ---
    val allJournalEntriesFlow: Flow<List<JournalEntry>> = journalDao.getAllJournalEntriesFlow()

    suspend fun getAllJournalEntries(): List<JournalEntry> = journalDao.getAllJournalEntries()

    fun getJournalEntryFlow(date: String): Flow<JournalEntry?> = journalDao.getJournalEntryFlow(date)

    suspend fun getJournalEntry(date: String): JournalEntry? = journalDao.getJournalEntry(date)

    suspend fun saveJournalEntry(entry: JournalEntry) {
        journalDao.insertJournalEntry(entry)
    }

    suspend fun wipeAllData() {
        journalDao.deleteAllJournalEntries()
        journalDao.deleteAllTasks()
        journalDao.deleteAllHabitLogs()
        journalDao.deleteAllHabits()
    }

    // --- Tasks ---
    fun getTasksForDateFlow(date: String): Flow<List<Task>> = journalDao.getTasksForDateFlow(date)

    suspend fun saveTask(task: Task) {
        journalDao.insertTask(task)
    }

    suspend fun updateTask(task: Task) {
        journalDao.updateTask(task)
    }

    suspend fun deleteTask(task: Task) {
        journalDao.deleteTask(task)
    }

    suspend fun deleteTaskById(taskId: Int) {
        journalDao.deleteTaskById(taskId)
    }

    // --- Habits ---
    val allHabitsFlow: Flow<List<Habit>> = journalDao.getAllHabitsFlow()
    val allHabitLogsFlow: Flow<List<HabitLog>> = journalDao.getAllHabitLogsFlow()

    fun getHabitLogsForDateFlow(date: String): Flow<List<HabitLog>> = journalDao.getHabitLogsForDateFlow(date)

    suspend fun addHabit(name: String) {
        val habit = Habit(name = name)
        journalDao.insertHabit(habit)
    }

    suspend fun deleteHabit(habit: Habit) {
        journalDao.deleteHabit(habit)
    }

    suspend fun toggleHabit(habitId: Int, date: String, isCompleted: Boolean) {
        if (isCompleted) {
            journalDao.insertHabitLog(HabitLog(habitId = habitId, date = date, isCompleted = true))
        } else {
            journalDao.deleteHabitLog(habitId, date)
        }
        recalculateHabitStreaks(habitId)
    }

    suspend fun recalculateHabitStreaks(habitId: Int) {
        val habitsList = journalDao.getAllHabits()
        val habit = habitsList.find { it.id == habitId } ?: return

        val logs = journalDao.getLogsForHabit(habitId)
        if (logs.isEmpty()) {
            val updated = habit.copy(currentStreak = 0, longestStreak = 0)
            journalDao.updateHabit(updated)
            return
        }

        // Parse logs into sorted LocalDates
        val dates = logs.mapNotNull { log ->
            try {
                LocalDate.parse(log.date, dateFormatter)
            } catch (e: Exception) {
                null
            }
        }.distinct().sorted()

        if (dates.isEmpty()) {
            val updated = habit.copy(currentStreak = 0, longestStreak = 0)
            journalDao.updateHabit(updated)
            return
        }

        // Calculate Longest Streak
        var longest = 1
        var currentTempStreak = 1
        for (i in 1 until dates.size) {
            val diff = ChronoUnit.DAYS.between(dates[i - 1], dates[i])
            if (diff == 1L) {
                currentTempStreak++
            } else if (diff > 1L) {
                if (currentTempStreak > longest) {
                    longest = currentTempStreak
                }
                currentTempStreak = 1
            }
        }
        if (currentTempStreak > longest) {
            longest = currentTempStreak
        }

        // Calculate Current Streak
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val lastLoggedDate = dates.last()

        val current: Int
        if (lastLoggedDate == today || lastLoggedDate == yesterday) {
            var streakCount = 1
            var idx = dates.size - 1
            while (idx > 0) {
                val diff = ChronoUnit.DAYS.between(dates[idx - 1], dates[idx])
                if (diff == 1L) {
                    streakCount++
                    idx--
                } else {
                    break
                }
            }
            current = streakCount
        } else {
            current = 0
        }

        val updated = habit.copy(
            currentStreak = current,
            longestStreak = maxOf(longest, current)
        )
        journalDao.updateHabit(updated)
    }
}
