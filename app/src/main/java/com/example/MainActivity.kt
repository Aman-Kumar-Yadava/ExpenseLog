package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.ui.AddExpenseScreen
import com.example.ui.DashboardScreen
import com.example.ui.ExpenseViewModel
import com.example.ui.ReportsScreen
import com.example.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val prefs = getSharedPreferences("theme_prefs", android.content.Context.MODE_PRIVATE)
            var themeIndex by remember { mutableIntStateOf(prefs.getInt("theme_index", 0)) }
            val onThemeChange: (Int) -> Unit = { newIndex ->
                themeIndex = newIndex
                prefs.edit().putInt("theme_index", newIndex).apply()
            }

            AppTheme(themeIndex = themeIndex) {
                val navController = rememberNavController()
                val viewModel: ExpenseViewModel = viewModel()
                
                NavHost(navController = navController, startDestination = "dashboard") {
                    composable("dashboard") {
                        DashboardScreen(
                            navController = navController, 
                            viewModel = viewModel,
                            currentThemeIndex = themeIndex,
                            onThemeChange = onThemeChange
                        )
                    }
                    composable(
                        "add_expense?expenseId={expenseId}&folderId={folderId}",
                        arguments = listOf(
                            navArgument("expenseId") {
                                type = NavType.IntType
                                defaultValue = -1
                            },
                            navArgument("folderId") {
                                type = NavType.IntType
                                defaultValue = -1
                            }
                        )
                    ) { backStackEntry ->
                        val expenseId = backStackEntry.arguments?.getInt("expenseId") ?: -1
                        val folderId = backStackEntry.arguments?.getInt("folderId") ?: -1
                        AddExpenseScreen(navController = navController, viewModel = viewModel, expenseId = expenseId, folderId = folderId)
                    }
                    composable("select_folder") {
                        com.example.ui.SelectFolderScreen(navController = navController, viewModel = viewModel)
                    }
                    composable(
                        "folder_detail?folderId={folderId}",
                        arguments = listOf(
                            navArgument("folderId") {
                                type = NavType.IntType
                            }
                        )
                    ) { backStackEntry ->
                        val folderId = backStackEntry.arguments?.getInt("folderId") ?: -1
                        com.example.ui.FolderDetailScreen(navController = navController, viewModel = viewModel, folderId = folderId)
                    }
                    composable("create_folder") {
                        com.example.ui.CreateFolderScreen(navController = navController, viewModel = viewModel)
                    }
                    composable(
                        "edit_folder?folderId={folderId}",
                        arguments = listOf(navArgument("folderId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val folderId = backStackEntry.arguments?.getInt("folderId") ?: -1
                        com.example.ui.CreateFolderScreen(navController = navController, viewModel = viewModel, folderId = folderId)
                    }
                    composable("reports") {
                        ReportsScreen(navController = navController, viewModel = viewModel)
                    }
                }
            }
        }
    }
}
