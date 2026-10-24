package com.example.domain.backup

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import com.example.data.entities.BackupEntity
import com.example.data.entities.CustomerEntity
import com.example.data.entities.KhataEntity
import com.example.data.entities.ReminderEntity
import com.example.data.entities.TransactionEntity
import com.example.data.repository.TallyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupPreviewInfo(
    val file: File,
    val fileName: String,
    val dateString: String,
    val khataCount: Int,
    val customerCount: Int,
    val transactionCount: Int,
    val sizeBytes: Long
)

data class BackupPayload(
    val khatas: List<KhataEntity>,
    val customers: List<CustomerEntity>,
    val transactions: List<TransactionEntity>,
    val reminders: List<ReminderEntity>
)

object LocalBackupManager {

    private const val DIRECTORY_NAME = "TallyKhata"
    private const val FILE_PREFIX = "tally_backup_"
    private const val FILE_EXTENSION = ".json"

    /**
     * Dedicated public backup directory on external storage:
     * Primary: /storage/emulated/0/Documents/TallyKhata/
     * Secondary fallback: /storage/emulated/0/Download/TallyKhata/
     */
    fun getPublicBackupDirectory(context: Context): File {
        try {
            val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val tallyDocsDir = File(documentsDir, DIRECTORY_NAME)
            if (tallyDocsDir.exists() || tallyDocsDir.mkdirs()) {
                return tallyDocsDir
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val tallyDownloadDir = File(downloadsDir, DIRECTORY_NAME)
            if (tallyDownloadDir.exists() || tallyDownloadDir.mkdirs()) {
                return tallyDownloadDir
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val externalFiles = context.getExternalFilesDir(DIRECTORY_NAME)
        if (externalFiles != null && (externalFiles.exists() || externalFiles.mkdirs())) {
            return externalFiles
        }

        return File(context.filesDir, "backups").apply { mkdirs() }
    }

    /**
     * Migrates any legacy backups from internal app-private storage (context.filesDir)
     * to the public Documents/TallyKhata/ folder so they are visible and persist after uninstall.
     */
    fun migrateLegacyBackups(context: Context) {
        try {
            val legacyDir = File(context.filesDir, "backups")
            if (legacyDir.exists() && legacyDir.isDirectory) {
                val publicDir = getPublicBackupDirectory(context)
                legacyDir.listFiles()?.forEach { file ->
                    if (file.isFile && file.name.startsWith(FILE_PREFIX) && file.name.endsWith(FILE_EXTENSION)) {
                        val destFile = File(publicDir, file.name)
                        if (!destFile.exists()) {
                            file.copyTo(destFile, overwrite = true)
                            MediaScannerConnection.scanFile(
                                context,
                                arrayOf(destFile.absolutePath),
                                arrayOf("application/json"),
                                null
                            )
                        }
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Returns all physical backup files from public external storage (Documents/TallyKhata/ and Download/TallyKhata/),
     * sorted with the newest first.
     */
    fun getBackupFiles(context: Context): List<File> {
        val publicDir = getPublicBackupDirectory(context)
        val files = mutableListOf<File>()

        // 1. Check primary Documents/TallyKhata
        if (publicDir.exists() && publicDir.isDirectory) {
            publicDir.listFiles()?.filter {
                it.isFile && it.name.startsWith(FILE_PREFIX) && it.name.endsWith(FILE_EXTENSION)
            }?.let { files.addAll(it) }
        }

        // 2. Check secondary Download/TallyKhata
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val downloadTallyDir = File(downloadsDir, DIRECTORY_NAME)
            if (downloadTallyDir.exists() && downloadTallyDir.isDirectory && downloadTallyDir.canonicalPath != publicDir.canonicalPath) {
                downloadTallyDir.listFiles()?.filter {
                    it.isFile && it.name.startsWith(FILE_PREFIX) && it.name.endsWith(FILE_EXTENSION)
                }?.forEach { f ->
                    if (files.none { it.name == f.name }) {
                        files.add(f)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return files.sortedByDescending { it.lastModified() }
    }

    /**
     * Inspects the latest available backup file in public storage for auto-restore prompts.
     */
    suspend fun getLatestBackupPreview(context: Context): BackupPreviewInfo? = withContext(Dispatchers.IO) {
        migrateLegacyBackups(context)
        val files = getBackupFiles(context)
        for (file in files) {
            val info = inspectBackupFile(file)
            if (info != null && (info.customerCount > 0 || info.transactionCount > 0 || info.khataCount > 0)) {
                return@withContext info
            }
        }
        null
    }

    /**
     * Synchronizes the Room database `backup_records` table with the actual files
     * in the public Documents/TallyKhata/ directory.
     */
    suspend fun syncBackups(context: Context, repository: TallyRepository): List<BackupEntity> = withContext(Dispatchers.IO) {
        try {
            migrateLegacyBackups(context)
            val filesOnDisk = getBackupFiles(context)
            val existingInDb = repository.getAllBackupsSync()

            // 1. Remove DB entries whose physical file was deleted from disk
            for (dbEntry in existingInDb) {
                val f = File(dbEntry.filePath)
                if (!f.exists()) {
                    repository.deleteBackup(dbEntry)
                }
            }

            // 2. Add files on disk that are not yet recorded in DB
            for (file in filesOnDisk) {
                val alreadyTracked = existingInDb.any { it.fileName == file.name || it.filePath == file.absolutePath }
                if (!alreadyTracked) {
                    val preview = inspectBackupFile(file)
                    if (preview != null) {
                        repository.recordBackup(
                            fileName = file.name,
                            filePath = file.absolutePath,
                            sizeBytes = file.length(),
                            cCount = preview.customerCount,
                            tCount = preview.transactionCount,
                            kCount = preview.khataCount
                        )
                    }
                }
            }

            // Return updated list
            repository.getAllBackupsSync()
        } catch (e: Exception) {
            e.printStackTrace()
            repository.getAllBackupsSync()
        }
    }

    suspend fun createBackup(context: Context, repository: TallyRepository): BackupEntity? = withContext(Dispatchers.IO) {
        try {
            val khatas = repository.getAllKhatasSync()
            val customers = repository.getAllCustomersSync()
            val transactions = repository.getAllTransactionsSync()
            val reminders = repository.getAllRemindersSync()

            val rootJson = JSONObject()
            rootJson.put("version", 1)
            rootJson.put("app", "TallyKhataPro")
            rootJson.put("timestamp", System.currentTimeMillis())

            // Khatas
            val kArray = JSONArray()
            for (k in khatas) {
                val obj = JSONObject()
                obj.put("id", k.id)
                obj.put("name", k.name)
                obj.put("description", k.description)
                obj.put("businessName", k.businessName)
                obj.put("isDefault", k.isDefault)
                obj.put("isDeleted", k.isDeleted)
                obj.put("createdAt", k.createdAt)
                obj.put("updatedAt", k.updatedAt)
                kArray.put(obj)
            }
            rootJson.put("khatas", kArray)

            // Customers
            val cArray = JSONArray()
            for (c in customers) {
                val obj = JSONObject()
                obj.put("id", c.id)
                obj.put("khataId", c.khataId)
                obj.put("name", c.name)
                obj.put("phone", c.phone)
                obj.put("address", c.address)
                obj.put("businessName", c.businessName)
                obj.put("notes", c.notes)
                obj.put("photoUri", c.photoUri ?: "")
                obj.put("currentBalance", c.currentBalance)
                obj.put("totalCredit", c.totalCredit)
                obj.put("totalPayment", c.totalPayment)
                obj.put("totalTransactions", c.totalTransactions)
                obj.put("lastTransactionDate", c.lastTransactionDate ?: 0L)
                obj.put("isDeleted", c.isDeleted)
                obj.put("createdAt", c.createdAt)
                obj.put("updatedAt", c.updatedAt)
                cArray.put(obj)
            }
            rootJson.put("customers", cArray)

            // Transactions
            val tArray = JSONArray()
            for (t in transactions) {
                val obj = JSONObject()
                obj.put("id", t.id)
                obj.put("khataId", t.khataId)
                obj.put("customerId", t.customerId)
                obj.put("type", t.type)
                obj.put("amount", t.amount)
                obj.put("previousBalance", t.previousBalance)
                obj.put("newBalance", t.newBalance)
                obj.put("note", t.note)
                obj.put("category", t.category)
                obj.put("attachmentUri", t.attachmentUri ?: "")
                obj.put("date", t.date)
                obj.put("isDeleted", t.isDeleted)
                obj.put("createdAt", t.createdAt)
                obj.put("updatedAt", t.updatedAt)
                tArray.put(obj)
            }
            rootJson.put("transactions", tArray)

            // Reminders
            val rArray = JSONArray()
            for (r in reminders) {
                val obj = JSONObject()
                obj.put("id", r.id)
                obj.put("khataId", r.khataId)
                obj.put("customerId", r.customerId)
                obj.put("dateTime", r.dateTime)
                obj.put("note", r.note)
                obj.put("completed", r.completed)
                obj.put("createdAt", r.createdAt)
                rArray.put(obj)
            }
            rootJson.put("reminders", rArray)

            // Save to Public Documents/TallyKhata directory
            val backupDir = getPublicBackupDirectory(context)
            val timeStampStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "${FILE_PREFIX}${timeStampStr}${FILE_EXTENSION}"
            val file = File(backupDir, fileName)

            FileOutputStream(file).use { fos ->
                fos.write(rootJson.toString(2).toByteArray(Charsets.UTF_8))
                fos.flush()
            }

            // Trigger MediaScanner so the file immediately shows up in Files / PC
            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.absolutePath),
                arrayOf("application/json")
            ) { _, _ -> }

            val sizeBytes = file.length()
            val insertedId = repository.recordBackup(
                fileName = fileName,
                filePath = file.absolutePath,
                sizeBytes = sizeBytes,
                cCount = customers.size,
                tCount = transactions.size,
                kCount = khatas.size
            )

            BackupEntity(
                id = insertedId,
                fileName = fileName,
                filePath = file.absolutePath,
                sizeBytes = sizeBytes,
                customerCount = customers.size,
                transactionCount = transactions.size,
                khataCount = khatas.size
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun deleteBackupFile(file: File): Boolean = withContext(Dispatchers.IO) {
        try {
            if (file.exists()) {
                file.delete()
            } else {
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun inspectBackupFile(file: File): BackupPreviewInfo? = withContext(Dispatchers.IO) {
        try {
            if (!file.exists()) return@withContext null
            val fis = FileInputStream(file)
            val jsonStr = fis.bufferedReader().use { it.readText() }
            val root = JSONObject(jsonStr)

            val timestamp = root.optLong("timestamp", file.lastModified())
            val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date(timestamp))
            val kCount = root.optJSONArray("khatas")?.length() ?: 0
            val cCount = root.optJSONArray("customers")?.length() ?: 0
            val tCount = root.optJSONArray("transactions")?.length() ?: 0

            BackupPreviewInfo(
                file = file,
                fileName = file.name,
                dateString = dateStr,
                khataCount = kCount,
                customerCount = cCount,
                transactionCount = tCount,
                sizeBytes = file.length()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun parseBackupFile(file: File): BackupPayload? = withContext(Dispatchers.IO) {
        try {
            if (!file.exists()) return@withContext null
            val fis = FileInputStream(file)
            val jsonStr = fis.bufferedReader().use { it.readText() }
            val root = JSONObject(jsonStr)

            val khatas = mutableListOf<KhataEntity>()
            val kArray = root.optJSONArray("khatas") ?: JSONArray()
            for (i in 0 until kArray.length()) {
                val o = kArray.getJSONObject(i)
                khatas.add(
                    KhataEntity(
                        id = o.optLong("id"),
                        name = o.optString("name", "খাতা"),
                        description = o.optString("description", ""),
                        businessName = o.optString("businessName", ""),
                        isDefault = o.optBoolean("isDefault", false),
                        isDeleted = o.optBoolean("isDeleted", false),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }

            val customers = mutableListOf<CustomerEntity>()
            val cArray = root.optJSONArray("customers") ?: JSONArray()
            for (i in 0 until cArray.length()) {
                val o = cArray.getJSONObject(i)
                customers.add(
                    CustomerEntity(
                        id = o.optLong("id"),
                        khataId = o.optLong("khataId", 1L),
                        name = o.optString("name", "কাস্টমার"),
                        phone = o.optString("phone", ""),
                        address = o.optString("address", ""),
                        businessName = o.optString("businessName", ""),
                        notes = o.optString("notes", ""),
                        photoUri = if (o.optString("photoUri").isEmpty()) null else o.optString("photoUri"),
                        currentBalance = o.optLong("currentBalance", 0L),
                        totalCredit = o.optLong("totalCredit", 0L),
                        totalPayment = o.optLong("totalPayment", 0L),
                        totalTransactions = o.optInt("totalTransactions", 0),
                        lastTransactionDate = if (o.optLong("lastTransactionDate") == 0L) null else o.optLong("lastTransactionDate"),
                        isDeleted = o.optBoolean("isDeleted", false),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }

            val transactions = mutableListOf<TransactionEntity>()
            val tArray = root.optJSONArray("transactions") ?: JSONArray()
            for (i in 0 until tArray.length()) {
                val o = tArray.getJSONObject(i)
                transactions.add(
                    TransactionEntity(
                        id = o.optLong("id"),
                        khataId = o.optLong("khataId", 1L),
                        customerId = o.optLong("customerId"),
                        type = o.optString("type", "CREDIT"),
                        amount = o.optLong("amount", 0L),
                        previousBalance = o.optLong("previousBalance", 0L),
                        newBalance = o.optLong("newBalance", 0L),
                        note = o.optString("note", ""),
                        category = o.optString("category", ""),
                        attachmentUri = if (o.optString("attachmentUri").isEmpty()) null else o.optString("attachmentUri"),
                        date = o.optLong("date", System.currentTimeMillis()),
                        isDeleted = o.optBoolean("isDeleted", false),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }

            val reminders = mutableListOf<ReminderEntity>()
            val rArray = root.optJSONArray("reminders") ?: JSONArray()
            for (i in 0 until rArray.length()) {
                val o = rArray.getJSONObject(i)
                reminders.add(
                    ReminderEntity(
                        id = o.optLong("id"),
                        khataId = o.optLong("khataId", 1L),
                        customerId = o.optLong("customerId"),
                        dateTime = o.optLong("dateTime"),
                        note = o.optString("note", ""),
                        completed = o.optBoolean("completed", false),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            BackupPayload(khatas, customers, transactions, reminders)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun restoreFromFile(file: File, repository: TallyRepository, merge: Boolean): BackupPayload? = withContext(Dispatchers.IO) {
        val payload = parseBackupFile(file) ?: return@withContext null
        repository.restoreData(payload.khatas, payload.customers, payload.transactions, payload.reminders, merge)
        payload
    }
}
