package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Expense
import com.example.data.ExpenseRepository
import com.example.data.Folder
import com.example.data.FolderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val expenseRepository: ExpenseRepository
    private val folderRepository: FolderRepository
    val allExpenses: StateFlow<List<Expense>>
    val allFolders: StateFlow<List<Folder>>

    init {
        val db = AppDatabase.getDatabase(application)
        expenseRepository = ExpenseRepository(db.expenseDao())
        folderRepository = FolderRepository(db.folderDao())
        
        allExpenses = expenseRepository.allExpenses.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
        allFolders = folderRepository.allActiveFolders.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
        viewModelScope.launch {
            seedDefaultFolders()
        }
    }
    
    private suspend fun seedDefaultFolders() {
        // No predefined folders. Users will add their own.
    }

    // --- Folder Operations ---
    fun addFolder(folder: Folder) = viewModelScope.launch {
        folderRepository.insertFolder(folder)
    }

    fun updateFolder(folder: Folder) = viewModelScope.launch {
        folderRepository.updateFolder(folder)
    }

    fun deleteFolder(folder: Folder) = viewModelScope.launch {
        folderRepository.deleteFolderById(folder.id)
    }

    fun getFolderByIdExt(id: Int): Flow<Folder?> = folderRepository.getFolderByIdFlow(id)

    fun getExpensesByFolder(folderId: Int): Flow<List<Expense>> {
        return expenseRepository.getExpensesByFolder(folderId)
    }

    // --- Expense Operations ---
    fun addExpense(expense: Expense) = viewModelScope.launch {
        expenseRepository.insertExpense(expense)
    }

    fun updateExpense(expense: Expense) = viewModelScope.launch {
        expenseRepository.updateExpense(expense)
    }

    fun deleteExpense(expense: Expense) = viewModelScope.launch {
        expenseRepository.deleteExpenseById(expense.id)
    }

    fun deleteExpenseById(id: Int) = viewModelScope.launch {
        expenseRepository.deleteExpenseById(id)
    }

    val todayTotal: StateFlow<Double> = expenseRepository.allExpenses.map { expenses ->
        val today = Calendar.getInstance()
        expenses.filter {
            val date = Calendar.getInstance().apply { timeInMillis = it.date }
            date.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    date.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthTotal: StateFlow<Double> = expenseRepository.allExpenses.map { expenses ->
        val today = Calendar.getInstance()
        expenses.filter {
            val date = Calendar.getInstance().apply { timeInMillis = it.date }
            date.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    date.get(Calendar.MONTH) == today.get(Calendar.MONTH)
        }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpenses: StateFlow<Double> = expenseRepository.allExpenses.map { expenses ->
        expenses.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
}
