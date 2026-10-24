package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.entities.AuditLogEntity
import com.example.data.entities.BackupEntity
import com.example.data.entities.CustomerEntity
import com.example.data.entities.KhataEntity
import com.example.data.entities.ReminderEntity
import com.example.data.entities.TransactionEntity
import com.example.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TallyRepository(private val db: AppDatabase) {
    private val khataDao = db.khataDao()
    private val customerDao = db.customerDao()
    private val transactionDao = db.transactionDao()
    private val reminderDao = db.reminderDao()
    private val auditLogDao = db.auditLogDao()
    private val backupDao = db.backupDao()

    // Khatas
    fun getActiveKhatas(): Flow<List<KhataEntity>> = khataDao.getAllActiveKhatas()

    suspend fun getKhataById(id: Long): KhataEntity? = withContext(Dispatchers.IO) {
        khataDao.getKhataById(id)
    }

    suspend fun createKhata(name: String, description: String = "", businessName: String = "", isDefault: Boolean = false): Long = withContext(Dispatchers.IO) {
        if (isDefault) {
            khataDao.clearDefaultKhata()
        }
        val id = khataDao.insertKhata(
            KhataEntity(
                name = name,
                description = description,
                businessName = businessName,
                isDefault = isDefault
            )
        )
        auditLogDao.insertLog(
            AuditLogEntity(
                khataId = id,
                action = "KHATA_CREATED",
                entityType = "KHATA",
                entityId = id,
                description = "নতুন খাতা তৈরি: $name"
            )
        )
        id
    }

    suspend fun updateKhata(khata: KhataEntity) = withContext(Dispatchers.IO) {
        khataDao.updateKhata(khata)
        auditLogDao.insertLog(
            AuditLogEntity(
                khataId = khata.id,
                action = "KHATA_UPDATED",
                entityType = "KHATA",
                entityId = khata.id,
                description = "খাতার নাম পরিবর্তন: ${khata.name}"
            )
        )
    }

    suspend fun deleteKhata(id: Long) = withContext(Dispatchers.IO) {
        khataDao.softDeleteKhata(id)
        auditLogDao.insertLog(
            AuditLogEntity(
                khataId = id,
                action = "KHATA_DELETED",
                entityType = "KHATA",
                entityId = id,
                description = "খাতা মুছে ফেলা হয়েছে (ID: $id)"
            )
        )
    }

    suspend fun ensureDefaultKhata(defaultName: String = "দোকানের খাতা", businessName: String = "আমার দোকান"): Long = withContext(Dispatchers.IO) {
        val existing = khataDao.getDefaultKhata()
        if (existing != null) {
            existing.id
        } else {
            val count = khataDao.getActiveKhataCount()
            if (count > 0) {
                val list = khataDao.getAllKhatasSync().filter { !it.isDeleted }
                list.firstOrNull()?.id ?: createKhata(defaultName, "", businessName, true)
            } else {
                createKhata(defaultName, "", businessName, true)
            }
        }
    }

    // Customers
    fun getCustomers(khataId: Long): Flow<List<CustomerEntity>> = customerDao.getCustomersByKhata(khataId)
    fun searchCustomers(khataId: Long, query: String): Flow<List<CustomerEntity>> = customerDao.searchCustomers(khataId, query)
    fun getCustomerByIdFlow(id: Long): Flow<CustomerEntity?> = customerDao.getCustomerByIdFlow(id)

    suspend fun getCustomerById(id: Long): CustomerEntity? = withContext(Dispatchers.IO) {
        customerDao.getCustomerById(id)
    }

    suspend fun addCustomer(
        khataId: Long,
        name: String,
        phone: String,
        address: String = "",
        businessName: String = "",
        notes: String = "",
        initialBalance: Long = 0L,
        photoUri: String? = null
    ): Long = withContext(Dispatchers.IO) {
        val customer = CustomerEntity(
            khataId = khataId,
            name = name,
            phone = phone,
            address = address,
            businessName = businessName,
            notes = notes,
            photoUri = photoUri,
            currentBalance = 0L,
            totalCredit = 0L,
            totalPayment = 0L
        )
        val customerId = customerDao.insertCustomer(customer)
        auditLogDao.insertLog(
            AuditLogEntity(
                khataId = khataId,
                action = "CUSTOMER_CREATED",
                entityType = "CUSTOMER",
                entityId = customerId,
                description = "নতুন কাস্টমার যোগ: $name"
            )
        )

        // If there's an opening balance
        if (initialBalance != 0L) {
            val type = if (initialBalance > 0) TransactionType.CREDIT else TransactionType.PAYMENT
            val amount = kotlin.math.abs(initialBalance)
            addTransaction(
                khataId = khataId,
                customerId = customerId,
                type = type,
                amount = amount,
                note = "পূর্বের বকেয়া / প্রারম্ভিক জের",
                category = "প্রারম্ভিক জের"
            )
        }
        customerId
    }

    suspend fun updateCustomer(customer: CustomerEntity) = withContext(Dispatchers.IO) {
        customerDao.updateCustomer(customer.copy(updatedAt = System.currentTimeMillis()))
        auditLogDao.insertLog(
            AuditLogEntity(
                khataId = customer.khataId,
                action = "CUSTOMER_UPDATED",
                entityType = "CUSTOMER",
                entityId = customer.id,
                description = "কাস্টমার তথ্য আপডেট: ${customer.name}"
            )
        )
    }

    suspend fun softDeleteCustomer(id: Long) = withContext(Dispatchers.IO) {
        val customer = customerDao.getCustomerById(id)
        customerDao.softDeleteCustomer(id)
        auditLogDao.insertLog(
            AuditLogEntity(
                khataId = customer?.khataId,
                action = "CUSTOMER_DELETED",
                entityType = "CUSTOMER",
                entityId = id,
                description = "কাস্টমার ট্র্যাশে পাঠানো হয়েছে: ${customer?.name ?: "ID: $id"}"
            )
        )
    }

    suspend fun restoreCustomer(id: Long) = withContext(Dispatchers.IO) {
        customerDao.restoreCustomer(id)
        auditLogDao.insertLog(
            AuditLogEntity(
                action = "CUSTOMER_RESTORED",
                entityType = "CUSTOMER",
                entityId = id,
                description = "কাস্টমার পুনরুদ্ধার করা হয়েছে (ID: $id)"
            )
        )
    }

    suspend fun permanentlyDeleteCustomer(id: Long) = withContext(Dispatchers.IO) {
        customerDao.permanentlyDeleteCustomer(id)
    }

    // Transactions
    fun getTransactionsForKhata(khataId: Long): Flow<List<TransactionEntity>> = transactionDao.getTransactionsForKhata(khataId)
    fun getRecentTransactions(khataId: Long, limit: Int = 15): Flow<List<TransactionEntity>> = transactionDao.getRecentTransactions(khataId, limit)
    fun getTransactionsForCustomer(customerId: Long): Flow<List<TransactionEntity>> = transactionDao.getTransactionsForCustomer(customerId)
    fun getTransactionsInRange(khataId: Long, start: Long, end: Long): Flow<List<TransactionEntity>> = transactionDao.getTransactionsInRange(khataId, start, end)

    suspend fun getTransactionById(id: Long): TransactionEntity? = withContext(Dispatchers.IO) {
        transactionDao.getTransactionById(id)
    }

    suspend fun addTransaction(
        khataId: Long,
        customerId: Long,
        type: TransactionType,
        amount: Long,
        quantity: Double? = null,
        unitPrice: Long? = null,
        discount: Long? = null,
        note: String = "",
        category: String = "",
        attachmentUri: String? = null,
        date: Long = System.currentTimeMillis()
    ): Long = withContext(Dispatchers.IO) {
        val customer = customerDao.getCustomerById(customerId) ?: return@withContext -1L
        val prevBalance = customer.currentBalance

        // Deterministic balance calculation
        val newBalance = if (type == TransactionType.CREDIT) {
            prevBalance + amount
        } else {
            prevBalance - amount
        }

        val transaction = TransactionEntity(
            khataId = khataId,
            customerId = customerId,
            type = type.name,
            amount = amount,
            previousBalance = prevBalance,
            newBalance = newBalance,
            quantity = quantity,
            unitPrice = unitPrice,
            discount = discount,
            note = note,
            category = category,
            attachmentUri = attachmentUri,
            date = date
        )
        val txnId = transactionDao.insertTransaction(transaction)

        // Update customer balances deterministically
        val newTotalCredit = if (type == TransactionType.CREDIT) customer.totalCredit + amount else customer.totalCredit
        val newTotalPayment = if (type == TransactionType.PAYMENT) customer.totalPayment + amount else customer.totalPayment

        customerDao.updateCustomer(
            customer.copy(
                currentBalance = newBalance,
                totalCredit = newTotalCredit,
                totalPayment = newTotalPayment,
                totalTransactions = customer.totalTransactions + 1,
                lastTransactionDate = date,
                updatedAt = System.currentTimeMillis()
            )
        )

        val typeText = if (type == TransactionType.CREDIT) "পাওনা" else "জমা"
        auditLogDao.insertLog(
            AuditLogEntity(
                khataId = khataId,
                action = "TRANSACTION_ADDED",
                entityType = "TRANSACTION",
                entityId = txnId,
                description = "$typeText ৳$amount যুক্ত হয়েছে (${customer.name})"
            )
        )

        txnId
    }

    suspend fun softDeleteTransaction(id: Long) = withContext(Dispatchers.IO) {
        val txn = transactionDao.getTransactionById(id) ?: return@withContext
        transactionDao.softDeleteTransaction(id)
        recalculateCustomerMetrics(txn.customerId)
        auditLogDao.insertLog(
            AuditLogEntity(
                khataId = txn.khataId,
                action = "TRANSACTION_DELETED",
                entityType = "TRANSACTION",
                entityId = id,
                description = "লেনদেন ট্র্যাশে পাঠানো হয়েছে (ID: $id, ৳${txn.amount})"
            )
        )
    }

    suspend fun restoreTransaction(id: Long) = withContext(Dispatchers.IO) {
        val txn = transactionDao.getTransactionById(id) ?: return@withContext
        transactionDao.restoreTransaction(id)
        recalculateCustomerMetrics(txn.customerId)
        auditLogDao.insertLog(
            AuditLogEntity(
                khataId = txn.khataId,
                action = "TRANSACTION_RESTORED",
                entityType = "TRANSACTION",
                entityId = id,
                description = "লেনদেন পুনরুদ্ধার করা হয়েছে (ID: $id)"
            )
        )
    }

    suspend fun permanentlyDeleteTransaction(id: Long) = withContext(Dispatchers.IO) {
        val txn = transactionDao.getTransactionById(id)
        transactionDao.permanentlyDeleteTransaction(id)
        if (txn != null) {
            recalculateCustomerMetrics(txn.customerId)
        }
    }

    suspend fun updateTransaction(
        id: Long,
        amount: Long,
        type: TransactionType,
        quantity: Double? = null,
        unitPrice: Long? = null,
        discount: Long? = null,
        note: String = "",
        category: String = "",
        attachmentUri: String? = null,
        date: Long = System.currentTimeMillis(),
        newCustomerId: Long? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val oldTxn = transactionDao.getTransactionById(id) ?: return@withContext false
        val targetCustomerId = newCustomerId ?: oldTxn.customerId

        // Detailed Audit Record Generation
        val changes = mutableListOf<String>()
        if (oldTxn.type != type.name) {
            val oldTypeStr = if (oldTxn.type == TransactionType.CREDIT.name) "পাওনা" else "জমা"
            val newTypeStr = if (type == TransactionType.CREDIT) "পাওনা" else "জমা"
            changes.add("Type: $oldTypeStr → $newTypeStr")
        }
        if (oldTxn.amount != amount) {
            changes.add("Amount: ৳${oldTxn.amount} → ৳$amount")
        }
        if (oldTxn.quantity != quantity) {
            changes.add("Quantity: ${oldTxn.quantity ?: "-"} → ${quantity ?: "-"}")
        }
        if (oldTxn.unitPrice != unitPrice) {
            changes.add("Unit Price: ৳${oldTxn.unitPrice ?: 0} → ৳${unitPrice ?: 0}")
        }
        if (oldTxn.discount != discount) {
            changes.add("Discount: ৳${oldTxn.discount ?: 0} → ৳${discount ?: 0}")
        }
        if (oldTxn.note != note) {
            changes.add("Note: \"${oldTxn.note}\" → \"$note\"")
        }
        if (oldTxn.category != category) {
            changes.add("Category: ${oldTxn.category.ifEmpty { "-" }} → $category")
        }
        if (oldTxn.date != date) {
            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
            changes.add("Date: ${sdf.format(Date(oldTxn.date))} → ${sdf.format(Date(date))}")
        }
        if (oldTxn.attachmentUri != attachmentUri) {
            changes.add("Attachment: ${if (attachmentUri != null) "Updated" else "Removed"}")
        }
        if (oldTxn.customerId != targetCustomerId) {
            val oldCust = customerDao.getCustomerById(oldTxn.customerId)
            val newCust = customerDao.getCustomerById(targetCustomerId)
            changes.add("Customer: ${oldCust?.name ?: oldTxn.customerId} → ${newCust?.name ?: targetCustomerId}")
        }

        val formattedModifiedDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(Date(System.currentTimeMillis()))
        val changeBody = if (changes.isNotEmpty()) changes.joinToString("\n") else "No field values changed"
        val auditDescription = "Transaction edited\n$changeBody\nModified: $formattedModifiedDate"

        // Update preserving original transaction ID and createdAt
        val updated = oldTxn.copy(
            customerId = targetCustomerId,
            type = type.name,
            amount = amount,
            quantity = quantity,
            unitPrice = unitPrice,
            discount = discount,
            note = note,
            category = category,
            attachmentUri = attachmentUri,
            date = date,
            updatedAt = System.currentTimeMillis()
        )
        transactionDao.updateTransaction(updated)

        // Insert audit log
        auditLogDao.insertLog(
            AuditLogEntity(
                khataId = oldTxn.khataId,
                action = "TRANSACTION_EDITED",
                entityType = "TRANSACTION",
                entityId = id,
                description = auditDescription,
                timestamp = System.currentTimeMillis()
            )
        )

        // Recalculate customer metrics deterministically from complete transaction history
        if (targetCustomerId != oldTxn.customerId) {
            recalculateCustomerMetrics(oldTxn.customerId)
            recalculateCustomerMetrics(targetCustomerId)
        } else {
            recalculateCustomerMetrics(targetCustomerId)
        }

        true
    }

    /**
     * Recalculates customer balance, total credit, total payment, and transaction count
     * purely deterministically from active transactions.
     * Also updates each transaction's running previousBalance and newBalance in the ledger.
     */
    suspend fun recalculateCustomerMetrics(customerId: Long) = withContext(Dispatchers.IO) {
        val customer = customerDao.getCustomerById(customerId) ?: return@withContext
        val allTxns = transactionDao.getAllActiveTransactionsSync()
            .filter { it.customerId == customerId }
            .sortedWith(compareBy({ it.date }, { it.id }))

        var runningBalance = 0L
        var totalCredit = 0L
        var totalPayment = 0L
        var lastDate: Long? = null

        for (t in allTxns) {
            val prevBalance = runningBalance
            if (t.type == TransactionType.CREDIT.name) {
                totalCredit += t.amount
                runningBalance += t.amount
            } else {
                totalPayment += t.amount
                runningBalance -= t.amount
            }
            val newBal = runningBalance

            if (t.previousBalance != prevBalance || t.newBalance != newBal) {
                transactionDao.updateTransaction(
                    t.copy(
                        previousBalance = prevBalance,
                        newBalance = newBal
                    )
                )
            }
            lastDate = t.date
        }

        customerDao.updateCustomer(
            customer.copy(
                currentBalance = runningBalance,
                totalCredit = totalCredit,
                totalPayment = totalPayment,
                totalTransactions = allTxns.size,
                lastTransactionDate = lastDate,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    // Reminders
    fun getPendingReminders(khataId: Long): Flow<List<ReminderEntity>> = reminderDao.getPendingReminders(khataId)
    suspend fun addReminder(khataId: Long, customerId: Long, dateTime: Long, note: String): Long = withContext(Dispatchers.IO) {
        reminderDao.insertReminder(
            ReminderEntity(
                khataId = khataId,
                customerId = customerId,
                dateTime = dateTime,
                note = note
            )
        )
    }
    suspend fun markReminderCompleted(id: Long) = withContext(Dispatchers.IO) {
        reminderDao.markCompleted(id)
    }
    suspend fun deleteReminder(reminder: ReminderEntity) = withContext(Dispatchers.IO) {
        reminderDao.deleteReminder(reminder)
    }

    // Trash
    fun getTrashCustomers(): Flow<List<CustomerEntity>> = customerDao.getTrashCustomers()
    fun getTrashTransactions(): Flow<List<TransactionEntity>> = transactionDao.getTrashTransactions()
    suspend fun emptyTrash() = withContext(Dispatchers.IO) {
        customerDao.emptyTrash()
        transactionDao.emptyTrash()
        auditLogDao.insertLog(
            AuditLogEntity(
                action = "TRASH_EMPTIED",
                entityType = "TRASH",
                description = "রিসাইকেল বিন সম্পূর্ণ খালি করা হয়েছে"
            )
        )
    }

    // Audit logs
    fun getRecentAuditLogs(limit: Int = 100): Flow<List<AuditLogEntity>> = auditLogDao.getRecentLogs(limit)

    // Backups
    fun getAllBackups(): Flow<List<BackupEntity>> = backupDao.getAllBackups()
    suspend fun recordBackup(fileName: String, filePath: String, sizeBytes: Long, cCount: Int, tCount: Int, kCount: Int) = withContext(Dispatchers.IO) {
        backupDao.insertBackup(
            BackupEntity(
                fileName = fileName,
                filePath = filePath,
                sizeBytes = sizeBytes,
                customerCount = cCount,
                transactionCount = tCount,
                khataCount = kCount
            )
        )
    }
    suspend fun deleteBackup(backup: BackupEntity) = withContext(Dispatchers.IO) {
        backupDao.deleteBackup(backup)
    }

    suspend fun getAllBackupsSync(): List<BackupEntity> = withContext(Dispatchers.IO) {
        backupDao.getAllBackupsSync()
    }

    suspend fun getTotalCustomerCount(): Int = withContext(Dispatchers.IO) {
        customerDao.getTotalActiveCustomerCount()
    }

    suspend fun getTotalTransactionCount(): Int = withContext(Dispatchers.IO) {
        transactionDao.getTotalActiveTransactionCount()
    }

    suspend fun isDatabaseEmpty(): Boolean = withContext(Dispatchers.IO) {
        customerDao.getTotalActiveCustomerCount() == 0 && transactionDao.getTotalActiveTransactionCount() == 0
    }

    // Direct Access for Backup/Restore
    suspend fun getAllKhatasSync() = withContext(Dispatchers.IO) { khataDao.getAllKhatasSync() }
    suspend fun getAllCustomersSync() = withContext(Dispatchers.IO) { customerDao.getAllCustomersSync() }
    suspend fun getAllTransactionsSync() = withContext(Dispatchers.IO) { transactionDao.getAllTransactionsSync() }
    suspend fun getAllRemindersSync() = withContext(Dispatchers.IO) { reminderDao.getAllRemindersSync() }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        db.clearAllTables()
        auditLogDao.insertLog(
            AuditLogEntity(
                action = "DATA_CLEARED",
                entityType = "SYSTEM",
                description = "সকল ডেটা মুছে ফেলা হয়েছে"
            )
        )
    }

    suspend fun restoreData(
        khatas: List<KhataEntity>,
        customers: List<CustomerEntity>,
        transactions: List<TransactionEntity>,
        reminders: List<ReminderEntity>,
        merge: Boolean
    ) = withContext(Dispatchers.IO) {
        if (!merge) {
            db.clearAllTables()
        }
        for (k in khatas) {
            khataDao.insertKhata(k)
        }
        for (c in customers) {
            customerDao.insertCustomer(c)
        }
        for (t in transactions) {
            transactionDao.insertTransaction(t)
        }
        for (r in reminders) {
            reminderDao.insertReminder(r)
        }
        // Recalculate balances
        for (c in customers) {
            recalculateCustomerMetrics(c.id)
        }
        auditLogDao.insertLog(
            AuditLogEntity(
                action = if (merge) "RESTORE_MERGE" else "RESTORE_FULL",
                entityType = "BACKUP",
                description = "ব্যাকআপ রিস্টোর সম্পন্ন হয়েছে (${customers.size} কাস্টমার, ${transactions.size} লেনদেন)"
            )
        )
    }
}
