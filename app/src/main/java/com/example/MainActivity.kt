package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
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
            AppTheme {
                val navController = rememberNavController()
                val viewModel: ExpenseViewModel = viewModel()
                
                NavHost(navController = navController, startDestination = "dashboard") {
                    composable("dashboard") {
                        DashboardScreen(navController = navController, viewModel = viewModel)
                    }
                    composable(
                        "add_expense?expenseId={expenseId}",
                        arguments = listOf(navArgument("expenseId") {
                            type = NavType.IntType
                            defaultValue = -1
                        })
                    ) { backStackEntry ->
                        val expenseId = backStackEntry.arguments?.getInt("expenseId") ?: -1
                        AddExpenseScreen(navController = navController, viewModel = viewModel, expenseId = expenseId)
                    }
                    composable("reports") {
                        ReportsScreen(navController = navController, viewModel = viewModel)
                    }
                }
            }
        }
    }
}
