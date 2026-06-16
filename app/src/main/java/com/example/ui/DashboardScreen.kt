package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

fun getIconByName(name: String) = when(name) {
    "ShoppingCart" -> Icons.Default.ShoppingCart
    "Home" -> Icons.Default.Home
    "DirectionsCar" -> Icons.Default.DirectionsCar
    "Flight" -> Icons.Default.Flight
    "LocalHospital" -> Icons.Default.LocalHospital
    "School" -> Icons.Default.School
    "Business" -> Icons.Default.Business
    "Person" -> Icons.Default.Person
    else -> Icons.Default.Folder
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    navController: NavController, 
    viewModel: ExpenseViewModel,
    currentThemeIndex: Int = 0,
    onThemeChange: (Int) -> Unit = {}
) {
    val folders by viewModel.allFolders.collectAsStateWithLifecycle()
    val expenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val todayTotal by viewModel.todayTotal.collectAsStateWithLifecycle()
    val monthTotal by viewModel.monthTotal.collectAsStateWithLifecycle()
    val totalExpenses by viewModel.totalExpenses.collectAsStateWithLifecycle()
    
    var selectedExpenseIds by remember { mutableStateOf(setOf<Int>()) }
    var expenseToView by remember { mutableStateOf<com.example.data.Expense?>(null) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var expensesToExport by remember { mutableStateOf<List<com.example.data.Expense>?>(null) }
    var selectedFolderId by remember { mutableStateOf<Int?>(null) }
    var isExporting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val context = LocalContext.current

    if (showLanguageDialog && expensesToExport != null) {
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
                                exportToPdf(context, expensesToExport!!, "hi")
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
                                exportToPdf(context, expensesToExport!!, "en")
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
                            val sendIntent: android.content.Intent = android.content.Intent().apply {
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
                    title = { 
                        Column {
                            Text("ExpenseLog", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                            Text("DASHBOARD", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 2.sp)
                        }
                    },
                actions = {
                    var isThemeMenuExpanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { isThemeMenuExpanded = true }) {
                            Icon(Icons.Default.Palette, contentDescription = "Themes")
                        }
                        DropdownMenu(
                            expanded = isThemeMenuExpanded,
                            onDismissRequest = { isThemeMenuExpanded = false }
                        ) {
                            val themeNames = listOf("Purple (Default)", "Green", "Blue", "Orange", "Rose")
                            themeNames.forEachIndexed { index, name ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = { 
                                        onThemeChange(index)
                                        isThemeMenuExpanded = false
                                    },
                                    trailingIcon = if (currentThemeIndex == index) {
                                        { Icon(Icons.Default.Check, contentDescription = "Selected") }
                                    } else null
                                )
                            }
                        }
                    }
                    IconButton(onClick = { navController.navigate("reports") }) {
                        Icon(Icons.Default.BarChart, contentDescription = "Reports")
                    }
                    IconButton(onClick = { navController.navigate("create_folder") }) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = "Create Folder")
                    }
                }
            )
            }
        }
    ) { padding ->
        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
            columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(minSize = 150.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) { Spacer(modifier = Modifier.height(4.dp)) }
            
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("DIRECTORIES & DRAWERS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                }
            }
            
            if (folders.isEmpty()) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Folder,
                            contentDescription = "No folders",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No folders yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Create a custom folder to organize your expenses.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(folders, key = { it.id }) { folder ->
                    val folderExpenses = expenses.filter { it.folderId == folder.id }
                    val totalAmount = folderExpenses.sumOf { it.amount }
                    val count = folderExpenses.size
                    
                    FolderNotesAppCard(
                        folder = folder,
                        expenseCount = count,
                        totalAmount = totalAmount,
                        onClick = { navController.navigate("folder_detail?folderId=${folder.id}") }
                    )
                }
            }

            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun FolderNotesAppCard(
    folder: com.example.data.Folder,
    expenseCount: Int,
    totalAmount: Double,
    onClick: () -> Unit
) {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    format.maximumFractionDigits = 0

    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(1.2f).clickable { onClick() },
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = getIconByName(folder.iconName),
                    contentDescription = folder.name,
                    tint = Color(folder.color),
                    modifier = Modifier.size(36.dp)
                )
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Options",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = folder.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                 Text(
                    text = "$expenseCount items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                 Text(
                    text = format.format(totalAmount),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
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

