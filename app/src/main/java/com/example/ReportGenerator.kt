package com.example

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.data.TransactionEntity
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

object ReportGenerator {

    fun generateAndSavePdfReport(
        context: Context,
        transactions: List<TransactionEntity>,
        selectedPeriod: String,
        currency: String
    ): Uri? {
        val pdfDocument = PdfDocument()
        
        val headerPaint = Paint().apply {
            color = 0xFF0F172A.toInt() // Slate 900
            textSize = 21f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
        }
        
        val titlePaint = Paint().apply {
            color = 0xFF1E293B.toInt() // Slate 800
            textSize = 12f
            isAntiAlias = true
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
        }
        
        val bodyPaint = Paint().apply {
            color = 0xFF475569.toInt() // Slate 600
            textSize = 9f
            isAntiAlias = true
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        
        val bodyBoldPaint = Paint().apply {
            color = 0xFF0F172A.toInt() // Slate 900
            textSize = 9f
            isAntiAlias = true
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
        }

        val greenPaint = Paint().apply {
            color = 0xFF059669.toInt() // Emerald 600
            textSize = 9f
            isAntiAlias = true
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
        }

        val redPaint = Paint().apply {
            color = 0xFFDC2626.toInt() // Red 600
            textSize = 9f
            isAntiAlias = true
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
        }

        val dividerPaint = Paint().apply {
            color = 0xFFE2E8F0.toInt() // Slate 200
            strokeWidth = 1f
        }

        val footerPaint = Paint().apply {
            color = 0xFF94A3B8.toInt() // Slate 400
            textSize = 8f
            typeface = Typeface.create("sans-serif", Typeface.ITALIC)
        }

        val totalIncome = transactions.filter { it.isIncome }.sumOf { it.amount }
        val totalExpense = transactions.filter { !it.isIncome }.sumOf { it.amount }
        val netSavings = totalIncome - totalExpense

        // Formatting utilities
        val sdf = SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.US)
        val reportDate = sdf.format(Date())

        fun formatVal(amt: Double): String {
            return if (currency == "UGX" || currency == "TZS" || currency == "RWF") {
                String.format(Locale.US, "%,.0f", amt)
            } else {
                String.format(Locale.US, "%,.2f", amt)
            }
        }
        fun formatCurr(amt: Double): String = "$currency ${formatVal(amt)}"

        // Page dimensions
        val pageWidth = 595
        val pageHeight = 842
        var pageNumber = 1
        
        var currentPage = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var canvas = currentPage.canvas
        var yPosition = 50f

        fun drawHeaderAndMetadata() {
            // Apple style sleek top gradient bar marker
            val accentPaint = Paint().apply { color = 0xFF0284C7.toInt() } // Sky Blue 600
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 6f, accentPaint)
            
            yPosition = 45f
            canvas.drawText("FINSECURE PROTOCOLS", 40f, yPosition, headerPaint)
            yPosition += 16f
            
            canvas.drawText("OFFICIAL FINANCIAL LEDGER STATEMENT", 40f, yPosition, titlePaint)
            yPosition += 28f
            
            // Ledger Meta
            canvas.drawText("Generated: $reportDate", 40f, yPosition, bodyPaint)
            canvas.drawText("Scope Period: $selectedPeriod", 350f, yPosition, bodyBoldPaint)
            yPosition += 14f
            
            canvas.drawText("Hardware Mode: 100% Secure Local Encrypted Storage", 40f, yPosition, bodyPaint)
            canvas.drawText("Ledger Entries: ${transactions.size} records", 350f, yPosition, bodyPaint)
            
            yPosition += 20f
            canvas.drawLine(40f, yPosition, (pageWidth - 40).toFloat(), yPosition, dividerPaint)
            yPosition += 25f
        }

        fun drawFinancialSummary() {
            canvas.drawText("FINANCIAL STATEMENT STATEMENT SUMMARY", 40f, yPosition, titlePaint)
            yPosition += 16f

            // Sleek card elements using light Apple style backgrounds
            val cardBgPaint = Paint().apply { color = 0xFFF8FAFC.toInt() } // Slate 50
            val cardBorderPaint = Paint().apply { 
                color = 0xFFF1F5F9.toInt() // Slate 100
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }

            val colWidth = 160f
            val cardHeight = 52f
            
            // Total Earnings
            canvas.drawRoundRect(40f, yPosition, 40f + colWidth, yPosition + cardHeight, 10f, 10f, cardBgPaint)
            canvas.drawRoundRect(40f, yPosition, 40f + colWidth, yPosition + cardHeight, 10f, 10f, cardBorderPaint)
            canvas.drawText("TOTAL INFLOW", 50f, yPosition + 18f, bodyPaint)
            canvas.drawText(formatCurr(totalIncome), 50f, yPosition + 36f, greenPaint)

            // Total Charges
            canvas.drawRoundRect(218f, yPosition, 218f + colWidth, yPosition + cardHeight, 10f, 10f, cardBgPaint)
            canvas.drawRoundRect(218f, yPosition, 218f + colWidth, yPosition + cardHeight, 10f, 10f, cardBorderPaint)
            canvas.drawText("TOTAL OUTFLOW", 228f, yPosition + 18f, bodyPaint)
            canvas.drawText(formatCurr(totalExpense), 228f, yPosition + 36f, redPaint)

            // Net Savings
            canvas.drawRoundRect(395f, yPosition, 395f + colWidth, yPosition + cardHeight, 10f, 10f, cardBgPaint)
            canvas.drawRoundRect(395f, yPosition, 395f + colWidth, yPosition + cardHeight, 10f, 10f, cardBorderPaint)
            canvas.drawText("NET CASH CASHFLOW", 405f, yPosition + 18f, bodyPaint)
            val netPaint = if (netSavings >= 0) greenPaint else redPaint
            canvas.drawText(formatCurr(netSavings), 405f, yPosition + 36f, netPaint)

            yPosition += cardHeight + 30f
        }

        fun drawFooter() {
            canvas.drawLine(40f, (pageHeight - 45).toFloat(), (pageWidth - 40).toFloat(), (pageHeight - 45).toFloat(), dividerPaint)
            canvas.drawText("FinSecure Portfolio Summary Statement. Confidential Local Report.", 40f, (pageHeight - 32).toFloat(), footerPaint)
            canvas.drawText("Page $pageNumber", (pageWidth - 75).toFloat(), (pageHeight - 32).toFloat(), footerPaint)
        }

        fun drawCategoryBreakdown() {
            val categorized = transactions.filter { !it.isIncome }
                .groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }

            if (categorized.isNotEmpty()) {
                canvas.drawText("SPENDING BREAKDOWN BY SEGMENT", 40f, yPosition, titlePaint)
                yPosition += 16f

                categorized.entries.forEach { (category, amount) ->
                    val ratio = if (totalExpense > 0) (amount / totalExpense).toFloat() else 0f
                    
                    canvas.drawText(category, 40f, yPosition + 8f, bodyBoldPaint)
                    canvas.drawText("${formatCurr(amount)} (${String.format("%.0f", ratio * 100)}%)", 360f, yPosition + 8f, bodyPaint)
                    
                    // Styled progress bar
                    val barWidthMax = 160f
                    val filledBarWidth = barWidthMax * ratio
                    
                    val bgBarPaint = Paint().apply { color = 0xFFF1F5F9.toInt() }
                    val fgBarPaint = Paint().apply { color = 0xFF0284C7.toInt() } // Sky Blue 600
                    
                    canvas.drawRect(185f, yPosition + 1f, 185f + barWidthMax, yPosition + 7f, bgBarPaint)
                    canvas.drawRect(185f, yPosition + 1f, 185f + filledBarWidth, yPosition + 7f, fgBarPaint)
                    
                    yPosition += 18f
                    
                    if (yPosition > pageHeight - 90) {
                        drawFooter()
                        pdfDocument.finishPage(currentPage)
                        pageNumber++
                        currentPage = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                        canvas = currentPage.canvas
                        yPosition = 50f
                    }
                }
                yPosition += 20f
            }
        }

        // Generate Page 1 content
        drawHeaderAndMetadata()
        drawFinancialSummary()
        drawCategoryBreakdown()

        if (yPosition > pageHeight - 120) {
            drawFooter()
            pdfDocument.finishPage(currentPage)
            pageNumber++
            currentPage = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            canvas = currentPage.canvas
            yPosition = 50f
        }

        canvas.drawText("DETAILED TRANSACTION REGISTRY", 40f, yPosition, titlePaint)
        yPosition += 20f

        // Table headers
        canvas.drawText("Date", 40f, yPosition, bodyBoldPaint)
        canvas.drawText("Title (Notes)", 110f, yPosition, bodyBoldPaint)
        canvas.drawText("Category", 290f, yPosition, bodyBoldPaint)
        canvas.drawText("Type", 395f, yPosition, bodyBoldPaint)
        canvas.drawText("Amount", 480f, yPosition, bodyBoldPaint)

        yPosition += 6f
        canvas.drawLine(40f, yPosition, (pageWidth - 40).toFloat(), yPosition, dividerPaint)
        yPosition += 14f

        val itemSdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        
        for (tx in transactions) {
            val dateStr = itemSdf.format(Date(tx.timestamp))
            val detailsLabel = if (tx.notes.isNotBlank()) "${tx.title} (${tx.notes})" else tx.title
            val titleStr = if (detailsLabel.length > 32) detailsLabel.substring(0, 29) + "..." else detailsLabel
            val categoryStr = tx.category
            val typeStr = if (tx.isIncome) "INFLOW" else "OUTFLOW"
            val typeColor = if (tx.isIncome) greenPaint else redPaint
            val amtStr = formatCurr(tx.amount)

            if (yPosition > pageHeight - 65) {
                drawFooter()
                pdfDocument.finishPage(currentPage)
                pageNumber++
                currentPage = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                canvas = currentPage.canvas
                
                // Redraw table headers on new page
                yPosition = 50f
                canvas.drawText("Date", 40f, yPosition, bodyBoldPaint)
                canvas.drawText("Title (Notes)", 110f, yPosition, bodyBoldPaint)
                canvas.drawText("Category", 290f, yPosition, bodyBoldPaint)
                canvas.drawText("Type", 395f, yPosition, bodyBoldPaint)
                canvas.drawText("Amount", 480f, yPosition, bodyBoldPaint)
                yPosition += 6f
                canvas.drawLine(40f, yPosition, (pageWidth - 40).toFloat(), yPosition, dividerPaint)
                yPosition += 14f
            }

            canvas.drawText(dateStr, 40f, yPosition, bodyPaint)
            canvas.drawText(titleStr, 110f, yPosition, bodyBoldPaint)
            canvas.drawText(categoryStr, 290f, yPosition, bodyPaint)
            canvas.drawText(typeStr, 395f, yPosition, typeColor)
            canvas.drawText(amtStr, 480f, yPosition, typeColor)

            yPosition += 18f
        }

        drawFooter()
        pdfDocument.finishPage(currentPage)

        val cleanPeriodName = selectedPeriod.replace(" ", "_")
        val fileName = "FinSecure_Statement_${cleanPeriodName}_${System.currentTimeMillis() / 1000}.pdf"
        
        return saveFileToDownloads(context, fileName, "application/pdf") { outStream ->
            pdfDocument.writeTo(outStream)
            pdfDocument.close()
        }
    }

    private fun saveFileToDownloads(
        context: Context,
        fileName: String,
        mimeType: String,
        writeBlock: (OutputStream) -> Unit
    ): Uri? {
        val resolver = context.contentResolver
        
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
        }

        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = java.io.File(downloadsDir, fileName)
            Uri.fromFile(file)
        }

        if (uri != null) {
            try {
                resolver.openOutputStream(uri)?.use { outStream ->
                    writeBlock(outStream)
                }
                return uri
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }
}
