package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.example.data.database.JournalDatabase
import com.example.data.repository.JournalRepository
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.HabitsScreen
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.JournalScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.BetterThanYesterdayTheme
import com.example.ui.viewmodel.JournalViewModel
import com.example.ui.viewmodel.JournalViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Core room persistence instantiations
        val database = JournalDatabase.getDatabase(applicationContext)
        val repository = JournalRepository(database.journalDao())

        // ViewModel instantiation via factory
        val viewModel: JournalViewModel by viewModels {
            JournalViewModelFactory(application, repository)
        }

        setContent {
            val darkModeEnabled by viewModel.darkModeEnabled.collectAsState()

            BetterThanYesterdayTheme(darkTheme = darkModeEnabled) {
                MainLayout(viewModel)
            }
        }
    }
}

sealed class NavigationScreen(
    val route: String,
    val title: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector
) {
    object Calendar : NavigationScreen("calendar", "Calendar", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth)
    object Reflection : NavigationScreen("reflection", "Journal", Icons.Filled.EditNote, Icons.Outlined.EditNote)
    object Habits : NavigationScreen("habits", "Habits", Icons.Filled.TaskAlt, Icons.Outlined.TaskAlt)
    object Insights : NavigationScreen("insights", "Insights", Icons.Filled.BarChart, Icons.Outlined.BarChart)
    object Settings : NavigationScreen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainLayout(viewModel: JournalViewModel) {
    var currentScreen by remember { mutableStateOf<NavigationScreen>(NavigationScreen.Calendar) }

    val screens = listOf(
        NavigationScreen.Calendar,
        NavigationScreen.Reflection,
        NavigationScreen.Habits,
        NavigationScreen.Insights,
        NavigationScreen.Settings
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("bottom_nav_bar")
            ) {
                screens.forEach { screen ->
                    val isSelected = currentScreen.route == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        label = { Text(text = screen.title) },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.filledIcon else screen.outlinedIcon,
                                contentDescription = screen.title
                            )
                        },
                        modifier = Modifier.testTag("nav_item_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) with fadeOut(animationSpec = tween(220))
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                NavigationScreen.Calendar -> CalendarScreen(
                    viewModel = viewModel,
                    onNavigateToJournal = { currentScreen = NavigationScreen.Reflection }
                )
                NavigationScreen.Reflection -> JournalScreen(
                    viewModel = viewModel
                )
                NavigationScreen.Habits -> HabitsScreen(
                    viewModel = viewModel
                )
                NavigationScreen.Insights -> InsightsScreen(
                    viewModel = viewModel
                )
                NavigationScreen.Settings -> SettingsScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}
