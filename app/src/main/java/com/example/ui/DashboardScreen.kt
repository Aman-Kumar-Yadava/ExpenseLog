package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.Expense
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(navController: NavController, viewModel: ExpenseViewModel) {
    val expenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val todayTotal by viewModel.todayTotal.collectAsStateWithLifecycle()
    val monthTotal by viewModel.monthTotal.collectAsStateWithLifecycle()
    val totalExpenses by viewModel.totalExpenses.collectAsStateWithLifecycle()
    
    var selectedExpenseIds by remember { mutableStateOf(setOf<Int>()) }
    var expenseToView by remember { mutableStateOf<Expense?>(null) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var expensesToExport by remember { mutableStateOf<List<Expense>?>(null) }
    val context = LocalContext.current

    if (showLanguageDialog && expensesToExport != null) {
        AlertDialog(
            onDismissRequest = { 
                showLanguageDialog = false 
                expensesToExport = null
            },
            title = { Text("Export to PDF") },
            text = { Text("Choose the language for the PDF report:") },
            confirmButton = {
                TextButton(onClick = {
                    exportToPdf(context, expensesToExport!!, "hi")
                    showLanguageDialog = false
                    selectedExpenseIds = emptySet()
                    expensesToExport = null
                }) {
                    Text("Hindi")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    exportToPdf(context, expensesToExport!!, "en")
                    showLanguageDialog = false
                    selectedExpenseIds = emptySet()
                    expensesToExport = null
                }) {
                    Text("English")
                }
            }
        )
    }

    if (expenseToView != null) {
        val expense = expenseToView!!
        AlertDialog(
            onDismissRequest = { expenseToView = null },
            title = { Text(text = "Expense Details") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Reason: ${expense.reason}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text("Amount: ${NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(expense.amount)}", style = MaterialTheme.typography.bodyMedium)
                    Text("Date: ${SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(expense.date))}", style = MaterialTheme.typography.bodyMedium)
                    if (expense.quantity != null) {
                        Text("Quantity: ${expense.quantity} ${expense.unit ?: ""}", style = MaterialTheme.typography.bodyMedium)
                    }
                    if (!expense.notes.isNullOrBlank()) {
                        Text("Notes: ${expense.notes}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    expenseToView = null
                    navController.navigate("add_expense?expenseId=${expense.id}")
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
                                navController.navigate("add_expense?expenseId=$id")
                                selectedExpenseIds = emptySet()
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                        }
                        IconButton(onClick = {
                            val selectedExpenses = expenses.filter { it.id in selectedExpenseIds }
                            val text = selectedExpenses.joinToString(separator = "\n\n") {
                                "Expense: ${it.reason}\nAmount: ₹${it.amount}\nDate: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it.date))}"
                            }
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, text)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Expense Logs"))
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
                    title = { 
                        Column {
                            Text("ExpenseLog", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                            Text("DASHBOARD", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 2.sp)
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("reports") }) {
                            Icon(Icons.Default.BarChart, contentDescription = "Reports")
                        }
                        IconButton(onClick = { 
                            expensesToExport = expenses
                            showLanguageDialog = true 
                        }) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("add_expense") },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Expense") },
                text = { Text("Add Expense", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                modifier = Modifier.padding(16.dp)
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
            item { Spacer(modifier = Modifier.height(4.dp)) }
            
            item {
                TodaySummaryCard(todayTotal)
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MiniSummaryCard(title = "This Month", amount = monthTotal, modifier = Modifier.weight(1f))
                    MiniSummaryCard(title = "Total All Time", amount = totalExpenses, modifier = Modifier.weight(1f))
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recent Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("View all", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                }
            }
            
            items(expenses.take(20)) { expense ->
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

@Composable
fun TodaySummaryCard(amount: Double) {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    Card(
        modifier = Modifier.fillMaxWidth().height(128.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Today's Total", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Text(text = format.format(amount), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom, modifier = Modifier.height(48.dp).padding(bottom = 4.dp)) {
                    Box(modifier = Modifier.width(8.dp).height(16.dp).background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f), androidx.compose.foundation.shape.RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.width(8.dp).height(32.dp).background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f), androidx.compose.foundation.shape.RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.width(8.dp).height(24.dp).background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f), androidx.compose.foundation.shape.RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.width(8.dp).height(40.dp).background(MaterialTheme.colorScheme.onSecondaryContainer, androidx.compose.foundation.shape.RoundedCornerShape(2.dp)))
                }
            }
        }
    }
}

@Composable
fun MiniSummaryCard(title: String, amount: Double, modifier: Modifier = Modifier) {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    format.maximumFractionDigits = 0
    
    Card(
        modifier = modifier.height(112.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(text = title.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
            Text(text = format.format(amount), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun SummaryCard(title: String, amount: Double, isCount: Boolean = false, modifier: Modifier = Modifier) {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            val displayText = if (isCount) amount.toInt().toString() else format.format(amount)
            Text(text = displayText, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpenseItem(
    expense: Expense,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val dateFormat = SimpleDateFormat("dd MMM • hh:mm a", Locale.getDefault())
    
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    
    Card(
        modifier = Modifier.fillMaxWidth(), 
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .padding(16.dp), 
            verticalAlignment = Alignment.CenterVertically, 
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val initial = expense.reason.firstOrNull()?.uppercase() ?: "E"
            val colors = listOf(
                Color(0xFFFFDAD6) to Color(0xFF410002), // Redish
                Color(0xFFD3E4FF) to Color(0xFF001D36), // Blueish
                Color(0xFFE8DEF8) to Color(0xFF1D192B)  // Purpleish
            )
            val colorPair = colors[expense.reason.length % colors.size]
            
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(colorPair.first, androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = initial, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colorPair.second)
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = expense.reason, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    Text(text = "-${format.format(expense.amount)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    val qtyText = if (expense.quantity != null && expense.unit != null) "${expense.quantity} ${expense.unit}" else "1 Item"
                    Text(text = qtyText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = dateFormat.format(Date(expense.date)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
