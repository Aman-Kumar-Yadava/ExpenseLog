package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.Folder

val availableColors = listOf(
    0xFF4CAF50, 0xFFFF9800, 0xFF2196F3, 0xFF9C27B0, 
    0xFFF44336, 0xFFFFEB3B, 0xFF607D8B, 0xFFE91E63,
    0xFF00BCD4, 0xFF8BC34A, 0xFFFF5722, 0xFF795548
)

val availableIcons = listOf(
    "ShoppingCart", "Home", "DirectionsCar", "Flight", 
    "LocalHospital", "School", "Business", "Person",
    "Folder"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFolderScreen(navController: NavController, viewModel: ExpenseViewModel, folderId: Int = -1) {
    val folders by viewModel.allFolders.collectAsStateWithLifecycle(initialValue = emptyList())
    val isEditMode = folderId != -1
    val existingFolder = remember(folderId, folders) { folders.find { it.id == folderId } }

    var folderName by remember(existingFolder) { mutableStateOf(existingFolder?.name ?: "") }
    var selectedColor by remember(existingFolder) { mutableStateOf(existingFolder?.color ?: availableColors[0]) }
    var selectedIcon by remember(existingFolder) { mutableStateOf(existingFolder?.iconName ?: availableIcons[8]) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Folder" else "Create Folder") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = folderName,
                onValueChange = { folderName = it },
                label = { Text("Folder Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Text("Select Color", style = MaterialTheme.typography.titleMedium)
            LazyVerticalGrid(
                columns = GridCells.Adaptive(48.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(120.dp)
            ) {
                items(availableColors) { colorValue ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(colorValue), shape = CircleShape)
                            .clickable { selectedColor = colorValue },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedColor == colorValue) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }

            Text("Select Icon", style = MaterialTheme.typography.titleMedium)
            LazyVerticalGrid(
                columns = GridCells.Adaptive(48.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(availableIcons) { iconName ->
                    val isSelected = selectedIcon == iconName
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape)
                            .clickable { selectedIcon = iconName },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(getIconByName(iconName), contentDescription = null, tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Button(
                onClick = {
                    if (folderName.isNotBlank()) {
                        val folder = Folder(
                            id = if (isEditMode) folderId else 0,
                            name = folderName,
                            color = selectedColor,
                            iconName = selectedIcon,
                            createdAt = existingFolder?.createdAt ?: System.currentTimeMillis()
                        )
                        if (isEditMode) {
                            viewModel.updateFolder(folder)
                        } else {
                            viewModel.addFolder(folder)
                        }
                        navController.navigateUp()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = folderName.isNotBlank()
            ) {
                Text("Save Folder")
            }
        }
    }
}
