package com.example.ui

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.Expense
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(navController: NavController, viewModel: ExpenseViewModel) {
    val expenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    var showLanguageDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val context = LocalContext.current

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Export to PDF") },
            text = { Text("Choose the language for the PDF report:") },
            confirmButton = {
                TextButton(onClick = {
                    exportToPdf(context, expenses, "hi")
                    showLanguageDialog = false
                }) {
                    Text("Hindi")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    exportToPdf(context, expenses, "en")
                    showLanguageDialog = false
                }) {
                    Text("English")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showLanguageDialog = true }) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF")
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            val total = expenses.sumOf { it.amount }
            val avg = if (expenses.isNotEmpty()) total / expenses.size else 0.0
            val highest = expenses.maxOfOrNull { it.amount } ?: 0.0
            
            val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryCard(title = "Total", amount = total, modifier = Modifier.weight(1f))
                SummaryCard(title = "Average", amount = avg, modifier = Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryCard(title = "Records", amount = expenses.size.toDouble(),  isCount = true, modifier = Modifier.weight(1f))
                SummaryCard(title = "Highest", amount = highest, modifier = Modifier.weight(1f))
            }

            Text("Expense Amount History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            BarChart(expenses = expenses.take(10).reversed())

            Text("Recent Spending Trend", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            LineChart(expenses = expenses.take(15).reversed())
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun BarChart(expenses: List<Expense>) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        if (expenses.isEmpty()) return@Canvas
        val maxAmount = expenses.maxOf { it.amount }.toFloat().coerceAtLeast(1f)
        val barWidth = size.width / (expenses.size * 2)
        
        expenses.forEachIndexed { index, expense ->
            val barHeight = (expense.amount.toFloat() / maxAmount) * size.height
            val x = index * (barWidth * 2) + barWidth / 2
            val y = size.height - barHeight
            
            drawRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight)
            )
        }
    }
}

@Composable
fun LineChart(expenses: List<Expense>) {
    val color = MaterialTheme.colorScheme.secondary
    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        if (expenses.isEmpty()) return@Canvas
        val maxAmount = expenses.maxOf { it.amount }.toFloat().coerceAtLeast(1f)
        val stepX = size.width / expenses.size.coerceAtLeast(2).minus(1)
        
        val path = Path()
        expenses.forEachIndexed { index, expense ->
            val x = index * stepX
            val y = size.height - ((expense.amount.toFloat() / maxAmount) * size.height)
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

fun exportToPdf(context: Context, expenses: List<Expense>, language: String = "en") {
    try {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()
        
        val isHindi = language == "hi"
        val rsPrefix = if (isHindi) "रु " else "Rs. "

        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText(if (isHindi) "व्यय रिपोर्ट (ExpenseLog)" else "ExpenseLog Report", 40f, 60f, paint)
        
        paint.textSize = 14f
        paint.isFakeBoldText = false
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy hh:mm a", java.util.Locale.getDefault())
        val generatedOnText = if (isHindi) "बनाने की तिथि: " else "Generated on: "
        canvas.drawText("${generatedOnText}${dateFormat.format(Date())}", 40f, 90f, paint)
        
        paint.textSize = 16f
        var yPos = 140f
        
        canvas.drawText(if (isHindi) "तारीख" else "Date", 40f, yPos, paint)
        canvas.drawText(if (isHindi) "कारण" else "Reason", 150f, yPos, paint)
        canvas.drawText(if (isHindi) "मात्रा" else "Qty", 350f, yPos, paint)
        canvas.drawText(if (isHindi) "रकम" else "Amount", 450f, yPos, paint)
        
        yPos += 20f
        paint.strokeWidth = 1f
        canvas.drawLine(40f, yPos, 550f, yPos, paint)
        yPos += 30f
        
        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        val itemDateFormat = java.text.SimpleDateFormat("dd/MM/yy", java.util.Locale.getDefault())
        val itemTimeFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        
        paint.textSize = 14f
        expenses.forEach { expense ->
            if (yPos > 730f) {
                // If it exceeds the page limit, we can stop for simplicity or start a new page.
                // Stopping here for simplicity, but we still want room for the total.
                return@forEach
            }
            
            paint.color = android.graphics.Color.BLACK
            paint.textSize = 14f
            
            val dateStr = itemDateFormat.format(Date(expense.date))
            canvas.drawText(dateStr, 40f, yPos, paint)
            
            val reasonStr = if (expense.reason.length > 25) expense.reason.take(22) + "..." else expense.reason
            canvas.drawText(reasonStr, 150f, yPos, paint)
            
            val qtyUnit = if (expense.quantity != null) "${expense.quantity} ${expense.unit ?: ""}" else "-"
            canvas.drawText(qtyUnit, 350f, yPos, paint)
            
            val amountStr = format.format(expense.amount).replace("₹", rsPrefix)
            canvas.drawText(amountStr, 450f, yPos, paint)
            
            yPos += 15f
            
            val timeStr = itemTimeFormat.format(Date(expense.date))
            paint.color = android.graphics.Color.DKGRAY
            paint.textSize = 9f
            canvas.drawText(timeStr, 40f, yPos, paint)
            
            if (!expense.notes.isNullOrBlank()) {
                val notesStr = if (isHindi) "नोट्स: ${expense.notes}" else "Notes: ${expense.notes}"
                val notesStrTruncated = if (notesStr.length > 60) notesStr.take(57) + "..." else notesStr
                
                canvas.drawText(notesStrTruncated, 150f, yPos, paint)
            }
            
            // Draw a subtle separator line between expenses
            yPos += 15f
            paint.color = android.graphics.Color.LTGRAY
            paint.strokeWidth = 0.5f
            canvas.drawLine(40f, yPos, 550f, yPos, paint)
            
            // Add space before next expense
            yPos += 25f
        }
        
        yPos += 10f
        paint.color = android.graphics.Color.BLACK
        paint.strokeWidth = 1f
        canvas.drawLine(40f, yPos - 20f, 550f, yPos - 20f, paint)
        
        // Print Total Expenditure Amount at the bottom
        val totalAmount = expenses.sumOf { it.amount }
        paint.textSize = 16f
        paint.isFakeBoldText = true
        canvas.drawText(if (isHindi) "कुल व्यय:" else "Total Expenditure:", 300f, yPos, paint)
        
        val totalStr = format.format(totalAmount).replace("₹", rsPrefix)
        canvas.drawText(totalStr, 450f, yPos, paint)
        paint.isFakeBoldText = false
        
        document.finishPage(page)
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, "ExpenseLog_Report_${System.currentTimeMillis()}.pdf")
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    document.writeTo(outputStream)
                }
            }
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val file = File(downloadsDir, "ExpenseLog_Report_${System.currentTimeMillis()}.pdf")
            FileOutputStream(file).use { outputStream ->
                document.writeTo(outputStream)
            }
        }
        
        document.close()
        
        Toast.makeText(context, "PDF saved to Downloads folder!", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to export PDF", Toast.LENGTH_SHORT).show()
    }
}
