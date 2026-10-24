package com.example.domain.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.domain.model.ReceiptData
import com.example.domain.model.TransactionType
import com.example.domain.utils.BanglaUtils
import java.io.File
import java.io.FileOutputStream

object ReceiptPdfGenerator {

    fun generateReceiptPdf(context: Context, receipt: ReceiptData): File? {
        return try {
            val pdfDocument = PdfDocument()
            // Standard receipt slip dimension: 400 pt width x 600 pt height
            val pageInfo = PdfDocument.PageInfo.Builder(400, 600, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            drawReceiptOnCanvas(canvas, receipt)

            pdfDocument.finishPage(page)

            val outputDir = File(context.cacheDir, "receipts").apply { mkdirs() }
            val outputFile = File(outputDir, "receipt_${receipt.receiptNo}_${System.currentTimeMillis()}.pdf")
            val fos = FileOutputStream(outputFile)
            pdfDocument.writeTo(fos)
            fos.flush()
            fos.close()
            pdfDocument.close()

            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun drawReceiptOnCanvas(canvas: Canvas, receipt: ReceiptData) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Background
        canvas.drawColor(Color.WHITE)

        // Header Sleek Teal banner
        paint.color = Color.parseColor("#006A60")
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, 400f, 90f, paint)

        // Shop / Business Title
        paint.color = Color.WHITE
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(receipt.businessName, 200f, 40f, paint)

        // Tagline / Khata
        paint.textSize = 12f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("টালি খাতা রসিদ — ${receipt.khataName}", 200f, 65f, paint)

        // Receipt Meta info box
        paint.color = Color.parseColor("#374151")
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 11f

        var y = 120f
        canvas.drawText("রসিদ নং: ${receipt.receiptNo}", 30f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("তারিখ: ${receipt.dateString}, ${receipt.timeString}", 370f, y, paint)

        // Divider
        y += 15f
        paint.color = Color.parseColor("#E5E7EB")
        paint.strokeWidth = 1.5f
        canvas.drawLine(30f, y, 370f, y, paint)

        // Customer Info
        y += 25f
        paint.color = Color.parseColor("#111827")
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("কাস্টমার: ${receipt.customerName}", 30f, y, paint)

        if (receipt.customerPhone.isNotEmpty()) {
            y += 20f
            paint.textSize = 12f
            paint.typeface = Typeface.DEFAULT
            paint.color = Color.parseColor("#4B5563")
            canvas.drawText("মোবাইল: ${receipt.customerPhone}", 30f, y, paint)
        }

        // Details Box
        y += 30f
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#F9FAFB")
        canvas.drawRoundRect(25f, y, 375f, y + 210f, 12f, 12f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#E5E7EB")
        canvas.drawRoundRect(25f, y, 375f, y + 210f, 12f, 12f, paint)

        paint.style = Paint.Style.FILL

        // Row: Previous Balance
        var rowY = y + 35f
        paint.color = Color.parseColor("#4B5563")
        paint.textSize = 12f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("পূর্বের বাকি:", 45f, rowY, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(BanglaUtils.formatTaka(receipt.previousBalance), 355f, rowY, paint)

        // Row: Transaction Type & Amount
        rowY += 40f
        paint.textAlign = Paint.Align.LEFT
        val isCredit = receipt.transactionType == TransactionType.CREDIT
        val typeTitle = if (isCredit) "পাওনা যোগ (বাকি বিক্রি):" else "জমা গ্রহণ (পরিশোধ):"
        canvas.drawText(typeTitle, 45f, rowY, paint)

        paint.textAlign = Paint.Align.RIGHT
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = if (isCredit) Color.parseColor("#00796B") else Color.parseColor("#C62828")
        canvas.drawText(BanglaUtils.formatTaka(receipt.transactionAmount), 355f, rowY, paint)

        // Line
        rowY += 25f
        paint.color = Color.parseColor("#E2E8F0")
        paint.strokeWidth = 1f
        canvas.drawLine(45f, rowY, 355f, rowY, paint)

        // Row: Current Balance
        rowY += 35f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 14f
        paint.color = Color.parseColor("#0F172A")
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("বর্তমান জের / বাকি:", 45f, rowY, paint)
        paint.textAlign = Paint.Align.RIGHT
        paint.textSize = 16f
        val balanceColor = if (receipt.currentBalance > 0) Color.parseColor("#00796B") else Color.parseColor("#0F172A")
        paint.color = balanceColor
        canvas.drawText(BanglaUtils.formatTaka(receipt.currentBalance), 355f, rowY, paint)

        if (receipt.note.isNotEmpty()) {
            rowY += 40f
            paint.typeface = Typeface.DEFAULT
            paint.textSize = 11f
            paint.color = Color.parseColor("#6B7280")
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("বিবরণ: ${receipt.note}", 45f, rowY, paint)
        }

        // Footer Thank you note
        val footerY = 530f
        paint.color = Color.parseColor("#9CA3AF")
        paint.textSize = 11f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("ধন্যবাদ! আবার আসবেন।", 200f, footerY, paint)
        paint.textSize = 9f
        canvas.drawText("জেনারেটেড বাই টালি খাতা প্রো (Tally Khata Pro)", 200f, footerY + 20f, paint)
    }

    fun shareReceiptFile(context: Context, file: File, customerName: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "টালি খাতা রসিদ — $customerName")
            putExtra(Intent.EXTRA_TEXT, "প্রিয় $customerName, আপনার টালি খাতা লেনদেনের ডিজিটাল রসিদ সংযুক্ত করা হলো।")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "রসিদ পাঠান")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
