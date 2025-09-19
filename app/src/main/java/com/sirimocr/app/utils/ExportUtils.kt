package com.sirimocr.app.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import com.opencsv.CSVWriter
import com.sirimocr.app.data.database.entities.SirimRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportUtils(private val context: Context) {

    suspend fun exportToCsv(records: List<SirimRecord>, name: String): Uri? = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(context.getExternalFilesDir(null), "$name.csv")
            CSVWriter(FileWriter(file)).use { writer ->
                writer.writeNext(arrayOf(
                    "SIRIM Serial",
                    "Batch",
                    "Brand",
                    "Model",
                    "Type",
                    "Rating",
                    "Pack Size",
                    "Confidence",
                    "Created",
                    "Status"
                ))
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                records.forEach { record ->
                    writer.writeNext(arrayOf(
                        record.sirimSerialNo,
                        record.batchNo ?: "",
                        record.brandTrademark ?: "",
                        record.model ?: "",
                        record.type ?: "",
                        record.rating ?: "",
                        record.packSize ?: "",
                        record.confidenceScore.toString(),
                        dateFormat.format(Date(record.createdAt)),
                        record.validationStatus
                    ))
                }
            }
            Uri.fromFile(file)
        }.onFailure { Log.e("ExportUtils", "CSV export failed", it) }.getOrNull()
    }

    suspend fun exportToExcel(records: List<SirimRecord>, name: String): Uri? = withContext(Dispatchers.IO) {
        runCatching {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Records")
            val headerStyle = workbook.createCellStyle().apply {
                val font = workbook.createFont().apply { bold = true }
                setFont(font)
            }
            val headerRow = sheet.createRow(0)
            val headers = listOf(
                "SIRIM Serial",
                "Batch",
                "Brand",
                "Model",
                "Type",
                "Rating",
                "Pack Size",
                "Confidence",
                "Created",
                "Status"
            )
            headers.forEachIndexed { index, title ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(title)
                cell.cellStyle = headerStyle
            }
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            records.forEachIndexed { rowIndex, record ->
                val row = sheet.createRow(rowIndex + 1)
                row.createCell(0).setCellValue(record.sirimSerialNo)
                row.createCell(1).setCellValue(record.batchNo ?: "")
                row.createCell(2).setCellValue(record.brandTrademark ?: "")
                row.createCell(3).setCellValue(record.model ?: "")
                row.createCell(4).setCellValue(record.type ?: "")
                row.createCell(5).setCellValue(record.rating ?: "")
                row.createCell(6).setCellValue(record.packSize ?: "")
                row.createCell(7).setCellValue(record.confidenceScore.toDouble())
                row.createCell(8).setCellValue(dateFormat.format(Date(record.createdAt)))
                row.createCell(9).setCellValue(record.validationStatus)
            }
            headers.indices.forEach { index -> sheet.autoSizeColumn(index) }
            val file = File(context.getExternalFilesDir(null), "$name.xlsx")
            FileOutputStream(file).use { output -> workbook.write(output) }
            workbook.close()
            Uri.fromFile(file)
        }.onFailure { Log.e("ExportUtils", "Excel export failed", it) }.getOrNull()
    }

    suspend fun exportToPdf(records: List<SirimRecord>, name: String): Uri? = withContext(Dispatchers.IO) {
        runCatching {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = document.startPage(pageInfo)
            drawPdfContent(page.canvas, records)
            document.finishPage(page)
            val file = File(context.getExternalFilesDir(null), "$name.pdf")
            FileOutputStream(file).use { output -> document.writeTo(output) }
            document.close()
            Uri.fromFile(file)
        }.onFailure { Log.e("ExportUtils", "PDF export failed", it) }.getOrNull()
    }

    private fun drawPdfContent(canvas: Canvas, records: List<SirimRecord>) {
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
        }
        val bodyPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 12f
        }
        var y = 60f
        canvas.drawText("SIRIM OCR Export", 50f, y, titlePaint)
        y += 30f
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        canvas.drawText("Generated: ${dateFormat.format(Date())}", 50f, y, bodyPaint)
        y += 30f
        records.forEach { record ->
            if (y > 780f) return
            canvas.drawText("Serial: ${record.sirimSerialNo}", 50f, y, bodyPaint)
            y += 18f
            record.brandTrademark?.let {
                canvas.drawText("Brand: $it", 70f, y, bodyPaint)
                y += 18f
            }
            record.model?.let {
                canvas.drawText("Model: $it", 70f, y, bodyPaint)
                y += 18f
            }
            y += 12f
        }
    }
}
