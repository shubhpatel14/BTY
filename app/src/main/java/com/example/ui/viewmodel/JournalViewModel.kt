package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.JournalDatabase
import com.example.data.database.JournalEntry
import com.example.data.database.Task
import com.example.data.database.Habit
import com.example.data.database.HabitLog
import com.example.data.repository.JournalRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class JournalViewModel(
    application: Application,
    private val repository: JournalRepository
) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // --- Active States ---
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _currentMonth = MutableStateFlow(LocalDate.now().withDayOfMonth(1))
    val currentMonth: StateFlow<LocalDate> = _currentMonth.asStateFlow()

    // --- Journal Entry Form State ---
    private val _mood = MutableStateFlow("NONE") // "GOOD", "DIFFICULT", "NONE"
    val mood: StateFlow<String> = _mood.asStateFlow()

    private val _whatWentBetter = MutableStateFlow("")
    val whatWentBetter: StateFlow<String> = _whatWentBetter.asStateFlow()

    private val _mistakesMade = MutableStateFlow("")
    val mistakesMade: StateFlow<String> = _mistakesMade.asStateFlow()

    private val _lessonsLearned = MutableStateFlow("")
    val lessonsLearned: StateFlow<String> = _lessonsLearned.asStateFlow()

    private val _gratitude = MutableStateFlow("")
    val gratitude: StateFlow<String> = _gratitude.asStateFlow()

    private val _tomorrowFocus = MutableStateFlow("")
    val tomorrowFocus: StateFlow<String> = _tomorrowFocus.asStateFlow()

    private val _rating = MutableStateFlow(5) // 1 to 10
    val rating: StateFlow<Int> = _rating.asStateFlow()

    // --- Database Flows ---
    val allJournalEntries: StateFlow<List<JournalEntry>> = repository.allJournalEntriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasksForSelectedDate: StateFlow<List<Task>> = _selectedDate
        .flatMapLatest { date ->
            repository.getTasksForDateFlow(date.format(dateFormatter))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHabits: StateFlow<List<Habit>> = repository.allHabitsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHabitLogs: StateFlow<List<HabitLog>> = repository.allHabitLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Settings States ---
    private val _darkModeEnabled = MutableStateFlow(sharedPrefs.getBoolean("dark_mode", false))
    val darkModeEnabled: StateFlow<Boolean> = _darkModeEnabled.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(sharedPrefs.getBoolean("notifications", true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    init {
        // Load the initial journal entry whenever the selected date changes
        viewModelScope.launch {
            _selectedDate.collect { date ->
                loadJournalEntryForDate(date)
            }
        }
    }

    // --- Selected Date Management ---
    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun nextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
    }

    fun prevMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
    }

    // --- Form Handlers ---
    fun updateMood(newMood: String) {
        _mood.value = newMood
    }

    fun updateWhatWentBetter(text: String) {
        _whatWentBetter.value = text
    }

    fun updateMistakesMade(text: String) {
        _mistakesMade.value = text
    }

    fun updateLessonsLearned(text: String) {
        _lessonsLearned.value = text
    }

    fun updateGratitude(text: String) {
        _gratitude.value = text
    }

    fun updateTomorrowFocus(text: String) {
        _tomorrowFocus.value = text
    }

    fun updateRating(newRating: Int) {
        _rating.value = newRating
    }

    private suspend fun loadJournalEntryForDate(date: LocalDate) {
        val entry = repository.getJournalEntry(date.format(dateFormatter))
        if (entry != null) {
            _mood.value = entry.mood
            _whatWentBetter.value = entry.whatWentBetter
            _mistakesMade.value = entry.mistakesMade
            _lessonsLearned.value = entry.lessonsLearned
            _gratitude.value = entry.gratitude
            _tomorrowFocus.value = entry.tomorrowFocus
            _rating.value = entry.rating
        } else {
            // Reset to empty for new entry
            _mood.value = "NONE"
            _whatWentBetter.value = ""
            _mistakesMade.value = ""
            _lessonsLearned.value = ""
            _gratitude.value = ""
            _tomorrowFocus.value = ""
            _rating.value = 5
        }
    }

    // --- Save Journal Entry ---
    fun saveCurrentJournalEntry() {
        viewModelScope.launch {
            val dateStr = _selectedDate.value.format(dateFormatter)
            val entry = JournalEntry(
                date = dateStr,
                mood = _mood.value,
                whatWentBetter = _whatWentBetter.value,
                mistakesMade = _mistakesMade.value,
                lessonsLearned = _lessonsLearned.value,
                gratitude = _gratitude.value,
                tomorrowFocus = _tomorrowFocus.value,
                rating = _rating.value
            )
            repository.saveJournalEntry(entry)
        }
    }

    // --- Task Management ---
    fun addTask(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val dateStr = _selectedDate.value.format(dateFormatter)
            val task = Task(date = dateStr, text = text)
            repository.saveTask(task)
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // --- Habit Management ---
    fun createHabit(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addHabit(name)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    fun toggleHabitCompletion(habitId: Int, date: LocalDate, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleHabit(habitId, date.format(dateFormatter), isCompleted)
        }
    }

    // --- Settings Actions ---
    fun toggleDarkMode() {
        val next = !_darkModeEnabled.value
        _darkModeEnabled.value = next
        sharedPrefs.edit().putBoolean("dark_mode", next).apply()
    }

    fun toggleNotifications() {
        val next = !_notificationsEnabled.value
        _notificationsEnabled.value = next
        sharedPrefs.edit().putBoolean("notifications", next).apply()
    }

    fun wipeAllUserData() {
        viewModelScope.launch {
            repository.wipeAllData()
            loadJournalEntryForDate(_selectedDate.value)
        }
    }

    fun getExportText(): String {
        val entries = allJournalEntries.value
        val habits = allHabits.value
        val logs = allHabitLogs.value

        val sb = StringBuilder()
        sb.append("=== BETTER THAN YESTERDAY BACKUP ===\n\n")

        sb.append("--- JOURNAL ENTRIES ---\n")
        sb.append("Date | Mood | Rating | What went better | Mistakes | Lessons | Gratitude | Tomorrow focus\n")
        entries.forEach { e ->
            sb.append("${e.date} | ${e.mood} | ${e.rating}/10 | ${e.whatWentBetter} | ${e.mistakesMade} | ${e.lessonsLearned} | ${e.gratitude} | ${e.tomorrowFocus}\n")
        }

        sb.append("\n--- HABITS ---\n")
        sb.append("ID | Name | Current Streak | Longest Streak\n")
        habits.forEach { h ->
            sb.append("${h.id} | ${h.name} | ${h.currentStreak} | ${h.longestStreak}\n")
        }

        sb.append("\n--- HABIT COMPLETION LOGS ---\n")
        sb.append("Habit ID | Date\n")
        logs.forEach { l ->
            sb.append("${l.habitId} | ${l.date}\n")
        }

        return sb.toString()
    }

    // --- Dynamic Insights State ---
    val insightsState: StateFlow<InsightsState> = combine(
        allJournalEntries,
        allHabitLogs,
        allHabits
    ) { entries, logs, habits ->
        calculateInsights(entries, logs, habits)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InsightsState())

    private fun calculateInsights(
        entries: List<JournalEntry>,
        logs: List<HabitLog>,
        habits: List<Habit>
    ): InsightsState {
        val totalEntries = entries.size
        val greenDays = entries.count { it.mood == "GOOD" }
        val difficultDays = entries.count { it.mood == "DIFFICULT" }

        // Journal Streak Calculations
        val sortedDates = entries.mapNotNull { e ->
            try {
                LocalDate.parse(e.date, dateFormatter)
            } catch (ex: Exception) {
                null
            }
        }.distinct().sorted()

        var longestJournalStreak = 0
        var currentJournalStreak = 0

        if (sortedDates.isNotEmpty()) {
            // Longest Journal Streak
            var maxStreak = 1
            var runningStreak = 1
            for (i in 1 until sortedDates.size) {
                val diff = ChronoUnit.DAYS.between(sortedDates[i - 1], sortedDates[i])
                if (diff == 1L) {
                    runningStreak++
                } else if (diff > 1L) {
                    if (runningStreak > maxStreak) {
                        maxStreak = runningStreak
                    }
                    runningStreak = 1
                }
            }
            longestJournalStreak = maxOf(maxStreak, runningStreak)

            // Current Journal Streak
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)
            val lastEntryDate = sortedDates.last()

            if (lastEntryDate == today || lastEntryDate == yesterday) {
                var count = 1
                var idx = sortedDates.size - 1
                while (idx > 0) {
                    val diff = ChronoUnit.DAYS.between(sortedDates[idx - 1], sortedDates[idx])
                    if (diff == 1L) {
                        count++
                        idx--
                    } else {
                        break
                    }
                }
                currentJournalStreak = count
            } else {
                currentJournalStreak = 0
            }
        }

        // Monthly completion percentage (current month)
        val today = LocalDate.now()
        val daysInCurrentMonth = today.lengthOfMonth()
        val currentMonthPrefix = today.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val entriesThisMonth = entries.count { it.date.startsWith(currentMonthPrefix) }
        val monthlyCompletionRate = if (daysInCurrentMonth > 0) {
            (entriesThisMonth.toFloat() / daysInCurrentMonth.toFloat()) * 100f
        } else {
            0f
        }

        // Habit completion rate
        // We evaluate for last 30 days to give a realistic habit completion percentage
        val totalHabitsCount = habits.size
        val habitCompletionRate = if (totalHabitsCount > 0) {
            val last30Days = (0..29).map { today.minusDays(it.toLong()).format(dateFormatter) }
            val possibleCompletions = totalHabitsCount * 30
            val actualCompletions = logs.count { last30Days.contains(it.date) }
            (actualCompletions.toFloat() / possibleCompletions.toFloat()) * 100f
        } else {
            0f
        }

        return InsightsState(
            greenDays = greenDays,
            difficultDays = difficultDays,
            totalEntries = totalEntries,
            currentJournalStreak = currentJournalStreak,
            longestJournalStreak = longestJournalStreak,
            monthlyCompletionRate = monthlyCompletionRate,
            habitCompletionRate = habitCompletionRate
        )
    }
}

data class InsightsState(
    val greenDays: Int = 0,
    val difficultDays: Int = 0,
    val totalEntries: Int = 0,
    val currentJournalStreak: Int = 0,
    val longestJournalStreak: Int = 0,
    val monthlyCompletionRate: Float = 0f,
    val habitCompletionRate: Float = 0f
)

class JournalViewModelFactory(
    private val application: Application,
    private val repository: JournalRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JournalViewModel::class.java)) {
            return JournalViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
