package com.example.domain.csv

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.entities.CustomerEntity
import com.example.data.entities.TransactionEntity
import com.example.domain.model.TransactionType
import com.example.domain.utils.BanglaUtils
import java.io.File
import java.io.FileOutputStream

object CsvExportHelper {

    fun generateTransactionReportCsv(
        context: Context,
        khataName: String,
        customersMap: Map<Long, CustomerEntity>,
        transactions: List<TransactionEntity>
    ): File? {
        return try {
            val outputDir = File(context.cacheDir, "reports").apply { mkdirs() }
            val outputFile = File(outputDir, "tally_report_${System.currentTimeMillis()}.csv")
            val fos = FileOutputStream(outputFile)

            // UTF-8 BOM so Excel opens Bangla text without mojibake
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))

            val header = "তারিখ,সময়,কাস্টমার,মোবাইল,লেনদেনের ধরন,পরিমাণ (৳),পূর্বের বাকি (৳),বর্তমান বাকি (৳),নোট\n"
            fos.write(header.toByteArray(Charsets.UTF_8))

            for (tx in transactions) {
                val customer = customersMap[tx.customerId]
                val customerName = customer?.name ?: "অজানা"
                val customerPhone = customer?.phone ?: ""
                val dateStr = BanglaUtils.formatDate(tx.date, isBangla = false)
                val timeStr = BanglaUtils.formatTime(tx.date, isBangla = false)
                val typeStr = if (tx.type == TransactionType.CREDIT.name) "পাওনা (Credit)" else "জমা (Payment)"
                val cleanNote = tx.note.replace(",", " ").replace("\n", " ")

                val line = "$dateStr,$timeStr,\"$customerName\",\"$customerPhone\",\"$typeStr\",${tx.amount},${tx.previousBalance},${tx.newBalance},\"$cleanNote\"\n"
                fos.write(line.toByteArray(Charsets.UTF_8))
            }

            fos.flush()
            fos.close()
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareCsvFile(context: Context, file: File, title: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "$title - টালি খাতা প্রো")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "রিপোর্ট শেয়ার করুন")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
