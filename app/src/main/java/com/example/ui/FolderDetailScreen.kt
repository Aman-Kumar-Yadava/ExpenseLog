// Ensure import is added or edit the file
package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.Expense
import com.example.data.Folder
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderDetailScreen(navController: NavController, viewModel: ExpenseViewModel, folderId: Int) {
    val folderFlow = remember { viewModel.getFolderByIdExt(folderId) }
    val folder by folderFlow.collectAsStateWithLifecycle(initialValue = null)
    
    val folderExpensesFlow = remember { viewModel.getExpensesByFolder(folderId) }
    val expenses by folderExpensesFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredExpenses = expenses.filter {
        it.reason.contains(searchQuery, ignoreCase = true) ||
        (it.notes?.contains(searchQuery, ignoreCase = true) == true) ||
        it.amount.toString().contains(searchQuery)
    }

    val totalSpent = expenses.sumOf { it.amount }
    val highestExpense = expenses.maxOfOrNull { it.amount } ?: 0.0
    val averageExpense = if (expenses.isNotEmpty()) totalSpent / expenses.size else 0.0

    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    
    var showLanguageDialog by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var selectedExpenseIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var expensesToExport by remember { mutableStateOf<List<Expense>?>(null) }
    
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { 
                if (!isExporting) {
                    showLanguageDialog = false 
                    expensesToExport = null
                }
            },
            title = { Text("Export to PDF") },
            text = { 
                if (isExporting) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Text("Translating & Generating PDF...")
                    }
                } else {
                    Text("Choose the language for the PDF report:") 
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (!isExporting) {
                            isExporting = true
                            scope.launch {
                                com.example.ui.exportToPdf(context, expensesToExport ?: expenses, "hi", folderTitleArg = folder?.name)
                                isExporting = false
                                showLanguageDialog = false
                                selectedExpenseIds = emptySet()
                                expensesToExport = null
                            }
                        }
                    },
                    enabled = !isExporting
                ) {
                    Text("Hindi")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (!isExporting) {
                            isExporting = true
                            scope.launch {
                                com.example.ui.exportToPdf(context, expensesToExport ?: expenses, "en", folderTitleArg = folder?.name)
                                isExporting = false
                                showLanguageDialog = false
                                selectedExpenseIds = emptySet()
                                expensesToExport = null
                            }
                        }
                    },
                    enabled = !isExporting
                ) {
                    Text("English")
                }
            }
        )
    }

    var expenseToView by remember { mutableStateOf<Expense?>(null) }
    
    if (expenseToView != null) {
        val expense = expenseToView!!
        AlertDialog(
            onDismissRequest = { expenseToView = null },
            title = { Text(text = "Expense Details") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
                ) {
                    Text("Reason: ${expense.reason}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text("Amount: ${format.format(expense.amount)}", style = MaterialTheme.typography.bodyMedium)
                    Text("Date: ${SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(expense.date))}", style = MaterialTheme.typography.bodyMedium)
                    if (expense.quantity != null) {
                        Text("Quantity: ${expense.quantity} ${expense.unit ?: ""}", style = MaterialTheme.typography.bodyMedium)
                    }
                    if (!expense.notes.isNullOrBlank()) {
                        Text("Notes:\n${expense.notes}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val id = expense.id
                    expenseToView = null
                    navController.navigate("add_expense?expenseId=$id&folderId=$folderId")
                }) {
                    Text("Edit")
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToView = null }) {
                    Text("Close")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            if (selectedExpenseIds.isNotEmpty()) {
                TopAppBar(
                    title = { Text("${selectedExpenseIds.size} Selected") },
                    navigationIcon = {
                        IconButton(onClick = { selectedExpenseIds = emptySet() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close selection")
                        }
                    },
                    actions = {
                        if (selectedExpenseIds.size == 1) {
                            IconButton(onClick = {
                                val id = selectedExpenseIds.first()
                                navController.navigate("add_expense?expenseId=$id&folderId=$folderId")
                                selectedExpenseIds = emptySet()
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                        }
                        IconButton(onClick = {
                            val selectedExpenses = expenses.filter { it.id in selectedExpenseIds }
                            val text = selectedExpenses.joinToString(separator = "\n\n") {
                                "Expense: ${it.reason}\nAmount: ${format.format(it.amount)}\nDate: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it.date))}" +
                                (if (!it.notes.isNullOrBlank()) "\nNotes: ${it.notes}" else "")
                            }
                            val sendIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(android.content.Intent.EXTRA_TEXT, text)
                                type = "text/plain"
                            }
                            context.startActivity(android.content.Intent.createChooser(sendIntent, "Share Expense Logs"))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share text")
                        }
                        IconButton(onClick = {
                            expensesToExport = expenses.filter { it.id in selectedExpenseIds }
                            showLanguageDialog = true
                        }) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "Share PDF")
                        }
                        IconButton(onClick = {
                            selectedExpenseIds.forEach { viewModel.deleteExpenseById(it) }
                            selectedExpenseIds = emptySet()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            } else {
                TopAppBar(
                    title = { Text(folder?.name ?: "Loading...") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showLanguageDialog = true }) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "Export Folder PDF")
                        }
                        var expandedMenu by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { expandedMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Options")
                            }
                            DropdownMenu(
                                expanded = expandedMenu,
                                onDismissRequest = { expandedMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit Folder") },
                                    onClick = {
                                        expandedMenu = false
                                        navController.navigate("edit_folder?folderId=$folderId")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete Folder") },
                                    onClick = {
                                        expandedMenu = false
                                        folder?.let { 
                                            viewModel.deleteFolder(it)
                                            navController.navigateUp()
                                        }
                                    }
                                )
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("add_expense?folderId=$folderId") },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Expense") },
                text = { Text("Add to Folder") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search by name, notes, amount") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                )
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Folder Statistics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Spent:", style = MaterialTheme.typography.bodyMedium)
                            Text(format.format(totalSpent), fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Number of Expenses:", style = MaterialTheme.typography.bodyMedium)
                            Text("${expenses.size}", fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Average Expense:", style = MaterialTheme.typography.bodyMedium)
                            Text(format.format(averageExpense), fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Highest Expense:", style = MaterialTheme.typography.bodyMedium)
                            Text(format.format(highestExpense), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            item {
                Text("Folder Analytics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                com.example.ui.BarChart(expenses = expenses.take(10).reversed())
            }
            
            item {
                Text("Recent Expenses", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            
            items(filteredExpenses) { expense ->
                val isSelected = selectedExpenseIds.contains(expense.id)
                ExpenseItem(
                    expense = expense,
                    isSelected = isSelected,
                    onClick = {
                        if (selectedExpenseIds.isNotEmpty()) {
                            if (isSelected) {
                                selectedExpenseIds = selectedExpenseIds - expense.id
                            } else {
                                selectedExpenseIds = selectedExpenseIds + expense.id
                            }
                        } else {
                            expenseToView = expense
                        }
                    },
                    onLongClick = {
                        if (!isSelected) {
                            selectedExpenseIds = selectedExpenseIds + expense.id
                        }
                    }
                )
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
