package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey val date: String, // format "yyyy-MM-dd"
    val mood: String,             // "GOOD", "DIFFICULT", "NONE"
    val whatWentBetter: String = "",
    val mistakesMade: String = "",
    val lessonsLearned: String = "",
    val gratitude: String = "",
    val tomorrowFocus: String = "",
    val rating: Int = 5          // 1 to 10
)

@Entity(
    tableName = "tasks",
    indices = [Index(value = ["date"])]
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,             // format "yyyy-MM-dd"
    val text: String,
    val isCompleted: Boolean = false
)

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0
)

@Entity(
    tableName = "habit_logs",
    primaryKeys = ["habitId", "date"],
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["habitId"])]
)
data class HabitLog(
    val habitId: Int,
    val date: String,             // format "yyyy-MM-dd"
    val isCompleted: Boolean = true
)
