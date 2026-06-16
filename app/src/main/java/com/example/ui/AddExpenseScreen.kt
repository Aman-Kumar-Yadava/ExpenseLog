package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.Expense
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(navController: NavController, viewModel: ExpenseViewModel, expenseId: Int = -1) {
    val expenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val isEditMode = expenseId != -1
    val existingExpense = remember(expenseId, expenses) {
        expenses.find { it.id == expenseId }
    }

    var reason by remember(existingExpense) { mutableStateOf(existingExpense?.reason ?: "") }
    var amount by remember(existingExpense) { mutableStateOf(existingExpense?.amount?.toString() ?: "") }
    var quantity by remember(existingExpense) { mutableStateOf(existingExpense?.quantity?.toString() ?: "") }
    var unit by remember(existingExpense) { mutableStateOf(existingExpense?.unit ?: "") }
    var notes by remember(existingExpense) { mutableStateOf(existingExpense?.notes ?: "") }
    
    var isSaving by remember { mutableStateOf(false) }
    var expandedUnit by remember { mutableStateOf(false) }
    val units = listOf("Kg", "Gram", "Litre", "Piece", "Packet", "Bag", "Box", "Meter", "Unit", "Other")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Expense" else "Add Expense") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Expense Reason *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (₹) *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity (Optional)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                
                ExposedDropdownMenuBox(
                    expanded = expandedUnit,
                    onExpandedChange = { expandedUnit = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = unit.ifEmpty { "Unit" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnit) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedUnit,
                        onDismissRequest = { expandedUnit = false }
                    ) {
                        units.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    unit = selectionOption
                                    expandedUnit = false
                                }
                            )
                        }
                    }
                }
            }
            
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (isSaving) return@Button
                    val amountValue = amount.toDoubleOrNull()
                    if (reason.isNotBlank() && amountValue != null) {
                        isSaving = true
                        val newExpense = Expense(
                            id = if (isEditMode) expenseId else 0,
                            date = existingExpense?.date ?: System.currentTimeMillis(),
                            reason = reason,
                            amount = amountValue,
                            quantity = quantity.toDoubleOrNull(),
                            unit = unit.takeIf { it.isNotBlank() },
                            notes = notes.takeIf { it.isNotBlank() }
                        )
                        if (isEditMode) {
                            viewModel.updateExpense(newExpense)
                        } else {
                            viewModel.addExpense(newExpense)
                        }
                        navController.navigateUp()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isSaving && reason.isNotBlank() && amount.toDoubleOrNull() != null
            ) {
                Text("Save Expense")
            }
        }
    }
}
