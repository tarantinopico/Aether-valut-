package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.repository.VaultRepository
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.CalendarViewModel
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.EditorViewModel
import com.example.ui.screens.GraphScreen
import com.example.ui.screens.GraphViewModel
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.NotesViewModel
import com.example.ui.screens.VaultSettingsScreen
import com.example.ui.screens.VaultSettingsViewModel
import com.example.ui.theme.AetherBorderGlass
import com.example.ui.theme.AetherSurfaceDeep
import com.example.ui.theme.AetherVoid
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val accentColor: Color = NeonIndigo
) {
    object Notes : Screen("notes", "Notes", Icons.Filled.Description, Icons.Outlined.Description, NeonIndigo)
    object Calendar : Screen("calendar", "Calendar", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, ElectricCyan)
    object Graph : Screen("graph", "Graph", Icons.Filled.Hub, Icons.Outlined.Hub, NeonViolet)
    object Settings : Screen("settings", "Vault", Icons.Filled.Settings, Icons.Outlined.Settings, NeonIndigo)
    object Editor : Screen("editor/{noteId}", "Editor", Icons.Filled.Description, Icons.Outlined.Description) {
        fun createRoute(noteId: String) = "editor/$noteId"
    }
}

val bottomNavScreens = listOf(
    Screen.Notes,
    Screen.Calendar,
    Screen.Graph,
    Screen.Settings
)

@Composable
fun AetherApp(
    vaultRepository: VaultRepository,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isEditorScreen = currentRoute?.startsWith("editor/") == true

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AetherVoid,
        bottomBar = {
            if (!isEditorScreen) {
                NavigationBar(
                    containerColor = AetherSurfaceDeep,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_navigation_bar")
                ) {
                    bottomNavScreens.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Notes.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = screen.accentColor,
                                selectedTextColor = screen.accentColor,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted,
                                indicatorColor = screen.accentColor.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("nav_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Notes.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Notes.route) {
                val notesViewModel = rememberViewModel { NotesViewModel(vaultRepository) }
                NotesScreen(
                    viewModel = notesViewModel,
                    onNoteSelected = { note ->
                        navController.navigate(Screen.Editor.createRoute(note.id))
                    }
                )
            }

            composable(Screen.Calendar.route) {
                val calendarViewModel = rememberViewModel { CalendarViewModel(vaultRepository) }
                CalendarScreen(
                    viewModel = calendarViewModel,
                    onNavigateToNote = { noteId ->
                        navController.navigate(Screen.Editor.createRoute(noteId))
                    }
                )
            }

            composable(Screen.Graph.route) {
                val graphViewModel = rememberViewModel { GraphViewModel(vaultRepository) }
                GraphScreen(
                    viewModel = graphViewModel,
                    onNavigateToNote = { noteId ->
                        navController.navigate(Screen.Editor.createRoute(noteId))
                    }
                )
            }

            composable(Screen.Settings.route) {
                val settingsViewModel = rememberViewModel { VaultSettingsViewModel(vaultRepository) }
                VaultSettingsScreen(viewModel = settingsViewModel)
            }

            composable(
                route = Screen.Editor.route,
                arguments = listOf(navArgument("noteId") { type = NavType.StringType })
            ) { backStackEntry ->
                val noteId = backStackEntry.arguments?.getString("noteId") ?: ""
                val editorViewModel = rememberViewModel(key = noteId) { EditorViewModel(noteId, vaultRepository) }
                EditorScreen(
                    viewModel = editorViewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToNote = { targetNoteId ->
                        navController.navigate(Screen.Editor.createRoute(targetNoteId))
                    }
                )
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
@Composable
inline fun <reified T : androidx.lifecycle.ViewModel> rememberViewModel(
    key: String? = null,
    crossinline factory: () -> T
): T {
    val viewModelFactory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <VM : androidx.lifecycle.ViewModel> create(modelClass: Class<VM>): VM {
            return factory() as VM
        }
    }
    return viewModel(key = key, factory = viewModelFactory)
}
