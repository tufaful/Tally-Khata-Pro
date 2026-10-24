package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entities.AuditLogEntity
import com.example.data.entities.BackupEntity
import com.example.data.entities.CustomerEntity
import com.example.data.entities.KhataEntity
import com.example.data.entities.ReminderEntity
import com.example.data.entities.TransactionEntity
import com.example.data.preferences.UserPreferences
import com.example.data.repository.TallyRepository
import com.example.domain.backup.BackupPreviewInfo
import com.example.domain.backup.LocalBackupManager
import com.example.domain.model.AppLanguage
import com.example.domain.model.AppThemeMode
import com.example.domain.model.AutoBackupSchedule
import com.example.domain.model.ChartBarData
import com.example.domain.model.CustomerFilter
import com.example.domain.model.CustomerSort
import com.example.domain.model.DashboardSummary
import com.example.domain.model.ReceiptData
import com.example.domain.model.TransactionType
import com.example.domain.utils.BanglaUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {
    val repository: TallyRepository
    val preferences: UserPreferences

    init {
        val db = AppDatabase.getInstance(application)
        repository = TallyRepository(db)
        preferences = UserPreferences(application)

        // Ensure default Khata exists on launch and check for backups
        viewModelScope.launch {
            repository.ensureDefaultKhata()
            syncBackups()
            checkAutoRestoreOnLaunch()
        }
    }

    // Preferences Flows
    val language: StateFlow<AppLanguage?> = preferences.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val themeMode: StateFlow<AppThemeMode?> = preferences.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val privacyMode: StateFlow<Boolean> = preferences.privacyMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val pinEnabled: StateFlow<Boolean?> = preferences.pinEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val pinCode: StateFlow<String> = preferences.pinCode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val isOnboardingDone: StateFlow<Boolean?> = preferences.isOnboardingDone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)


    val businessName: StateFlow<String> = preferences.businessName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "আমার দোকান")

    val ownerName: StateFlow<String> = preferences.ownerName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val phone: StateFlow<String> = preferences.phone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val address: StateFlow<String> = preferences.address
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val autoBackupSchedule: StateFlow<AutoBackupSchedule> = preferences.autoBackupSchedule
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AutoBackupSchedule.OFF)

    val onboardingDone: StateFlow<Boolean?> get() = isOnboardingDone

    val activeKhataId: StateFlow<Long> = preferences.activeKhataId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1L)

    val allKhatas: StateFlow<List<KhataEntity>> = repository.getActiveKhatas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Khata object
    val activeKhata: StateFlow<KhataEntity?> = combine(activeKhataId, allKhatas) { id, list ->
        list.find { it.id == id } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Customer filters and search
    private val _customerSearchQuery = MutableStateFlow("")
    val customerSearchQuery = _customerSearchQuery.asStateFlow()
    val searchQuery: StateFlow<String> get() = customerSearchQuery

    private val _customerFilter = MutableStateFlow(CustomerFilter.ALL)
    val customerFilter = _customerFilter.asStateFlow()

    private val _customerSort = MutableStateFlow(CustomerSort.RECENT)
    val customerSort = _customerSort.asStateFlow()

    // Raw Customers for Active Khata
    val rawCustomers: StateFlow<List<CustomerEntity>> = activeKhataId.flatMapLatest { khataId ->
        repository.getCustomers(khataId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered & Sorted Customers
    val filteredCustomers: StateFlow<List<CustomerEntity>> = combine(
        rawCustomers,
        _customerSearchQuery,
        _customerFilter,
        _customerSort
    ) { list, query, filter, sort ->
        var result = list

        // 1. Search Query
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            result = result.filter {
                it.name.lowercase().contains(q) ||
                it.phone.contains(q) ||
                it.businessName.lowercase().contains(q) ||
                it.notes.lowercase().contains(q)
            }
        }

        // 2. Filter
        result = when (filter) {
            CustomerFilter.ALL -> result
            CustomerFilter.DUE -> result.filter { it.currentBalance > 0 }
            CustomerFilter.ADVANCE -> result.filter { it.currentBalance < 0 }
            CustomerFilter.SETTLED -> result.filter { it.currentBalance == 0L }
        }

        // 3. Sort
        when (sort) {
            CustomerSort.RECENT -> result.sortedByDescending { it.lastTransactionDate ?: it.createdAt }
            CustomerSort.BALANCE_HIGH_TO_LOW -> result.sortedByDescending { it.currentBalance }
            CustomerSort.NAME_AZ -> result.sortedBy { it.name.lowercase() }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Transactions for Active Khata
    val activeTransactions: StateFlow<List<TransactionEntity>> = activeKhataId.flatMapLatest { khataId ->
        repository.getTransactionsForKhata(khataId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeKhataTransactions: StateFlow<List<TransactionEntity>> get() = activeTransactions

    val recentTransactions: StateFlow<List<TransactionEntity>> = activeKhataId.flatMapLatest { khataId ->
        repository.getRecentTransactions(khataId, 20)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard Summary Metrics
    val dashboardSummary: StateFlow<DashboardSummary> = combine(
        rawCustomers,
        activeTransactions
    ) { customers, transactions ->
        var totalReceivable = 0L
        var totalPayable = 0L

        for (c in customers) {
            if (c.currentBalance > 0) {
                totalReceivable += c.currentBalance
            } else if (c.currentBalance < 0) {
                totalPayable += kotlin.math.abs(c.currentBalance)
            }
        }

        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val startOfMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        var todayCredit = 0L
        var todayPayment = 0L
        var todayCount = 0

        var monthCredit = 0L
        var monthPayment = 0L
        var monthCount = 0

        for (t in transactions) {
            if (t.date >= startOfToday) {
                todayCount++
                if (t.type == TransactionType.CREDIT.name) todayCredit += t.amount
                else todayPayment += t.amount
            }
            if (t.date >= startOfMonth) {
                monthCount++
                if (t.type == TransactionType.CREDIT.name) monthCredit += t.amount
                else monthPayment += t.amount
            }
        }

        DashboardSummary(
            totalReceivable = totalReceivable,
            totalPayable = totalPayable,
            todayCredit = todayCredit,
            todayPayment = todayPayment,
            totalCustomers = customers.size,
            todayTxnCount = todayCount,
            monthTxnCount = monthCount,
            monthCredit = monthCredit,
            monthPayment = monthPayment
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

    // Chart Data for Home Dashboard
    val chartData: StateFlow<List<ChartBarData>> = combine(
        activeTransactions,
        language
    ) { txns, lang ->
        val isBangla = lang == AppLanguage.BANGLA
        val result = mutableListOf<ChartBarData>()

        // Generate 7 days in chronological order from 6 days ago up to today
        for (i in 6 downTo 0) {
            val startCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val endCal = Calendar.getInstance().apply {
                timeInMillis = startCal.timeInMillis
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }

            val startMs = startCal.timeInMillis
            // For today (i == 0), include any transactions recorded for today or future timestamps of today
            val endMs = if (i == 0) Long.MAX_VALUE else endCal.timeInMillis

            val dayOfWeek = startCal.get(Calendar.DAY_OF_WEEK)
            val dayLabel = when {
                i == 0 -> if (isBangla) "আজ" else "Today"
                i == 1 -> if (isBangla) "গতকাল" else "Y'day"
                else -> when (dayOfWeek) {
                    Calendar.SATURDAY -> if (isBangla) "শনি" else "Sat"
                    Calendar.SUNDAY -> if (isBangla) "রবি" else "Sun"
                    Calendar.MONDAY -> if (isBangla) "সোম" else "Mon"
                    Calendar.TUESDAY -> if (isBangla) "মঙ্গল" else "Tue"
                    Calendar.WEDNESDAY -> if (isBangla) "বুধ" else "Wed"
                    Calendar.THURSDAY -> if (isBangla) "বৃহ" else "Thu"
                    Calendar.FRIDAY -> if (isBangla) "শুক্র" else "Fri"
                    else -> ""
                }
            }

            val fullDateStr = BanglaUtils.formatDate(startMs, isBangla)

            var dayCredit = 0L
            var dayPayment = 0L

            for (t in txns) {
                if (t.date in startMs..endMs) {
                    if (t.type == TransactionType.CREDIT.name) {
                        dayCredit += t.amount
                    } else {
                        dayPayment += t.amount
                    }
                }
            }

            result.add(
                ChartBarData(
                    label = dayLabel,
                    creditAmount = dayCredit,
                    paymentAmount = dayPayment,
                    fullDateLabel = fullDateStr,
                    timestamp = startMs,
                    isToday = (i == 0)
                )
            )
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reminders
    val pendingReminders: StateFlow<List<ReminderEntity>> = activeKhataId.flatMapLatest { khataId ->
        repository.getPendingReminders(khataId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Trash
    val trashCustomers: StateFlow<List<CustomerEntity>> = repository.getTrashCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trashTransactions: StateFlow<List<TransactionEntity>> = repository.getTrashTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Audit Logs
    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.getRecentAuditLogs(100)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Backups
    val backups: StateFlow<List<BackupEntity>> = repository.getAllBackups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val backupRecords: StateFlow<List<BackupEntity>> get() = backups

    private val _detectedBackupForAutoRestore = MutableStateFlow<BackupPreviewInfo?>(null)
    val detectedBackupForAutoRestore: StateFlow<BackupPreviewInfo?> = _detectedBackupForAutoRestore.asStateFlow()

    private val _isRestoringBackup = MutableStateFlow(false)
    val isRestoringBackup: StateFlow<Boolean> = _isRestoringBackup.asStateFlow()

    // App Lock State
    private val _isAppUnlocked = MutableStateFlow(false)
    val isAppUnlocked = _isAppUnlocked.asStateFlow()

    fun unlockApp() {
        _isAppUnlocked.value = true
    }

    fun lockApp() {
        _isAppUnlocked.value = false
    }

    // Search and Filters
    fun setSearchQuery(query: String) {
        _customerSearchQuery.value = query
    }

    fun setCustomerFilter(filter: CustomerFilter) {
        _customerFilter.value = filter
    }

    fun setCustomerSort(sort: CustomerSort) {
        _customerSort.value = sort
    }

    // Preferences Actions
    fun togglePrivacyMode() {
        viewModelScope.launch {
            preferences.setPrivacyMode(!privacyMode.value)
        }
    }

    fun toggleLanguage() {
        viewModelScope.launch {
            val next = if (language.value == AppLanguage.BANGLA) AppLanguage.ENGLISH else AppLanguage.BANGLA
            preferences.setLanguage(next)
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            preferences.setThemeMode(mode)
        }
    }

    fun setPin(pin: String, enabled: Boolean) {
        viewModelScope.launch {
            preferences.setPin(pin, enabled)
            if (!enabled) _isAppUnlocked.value = true
        }
    }

    fun setAutoBackup(schedule: AutoBackupSchedule) {
        viewModelScope.launch {
            preferences.setAutoBackup(schedule)
        }
    }

    fun updateBusinessDetails(name: String, owner: String, phone: String, address: String) {
        viewModelScope.launch {
            preferences.setBusinessDetails(name, owner, phone, address)
        }
    }

    // Khata Actions
    fun switchKhata(id: Long) {
        viewModelScope.launch {
            preferences.setActiveKhataId(id)
        }
    }

    fun createKhata(name: String, description: String = "", businessName: String = "", onCreated: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = repository.createKhata(name, description, businessName)
            preferences.setActiveKhataId(id)
            onCreated?.invoke(id)
        }
    }

    fun updateKhata(khata: KhataEntity) {
        viewModelScope.launch {
            repository.updateKhata(khata)
        }
    }

    fun deleteKhata(id: Long) {
        viewModelScope.launch {
            repository.deleteKhata(id)
            val remaining = allKhatas.value.filter { it.id != id }
            if (remaining.isNotEmpty()) {
                preferences.setActiveKhataId(remaining.first().id)
            }
        }
    }

    // Customer Actions
    fun addCustomer(
        name: String,
        phone: String,
        address: String = "",
        businessName: String = "",
        notes: String = "",
        initialBalance: Long = 0L,
        onSuccess: ((Long) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val id = repository.addCustomer(
                khataId = activeKhataId.value,
                name = name,
                phone = phone,
                address = address,
                businessName = businessName,
                notes = notes,
                initialBalance = initialBalance
            )
            onSuccess?.invoke(id)
        }
    }

    fun updateCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
        }
    }

    fun deleteCustomer(id: Long) {
        viewModelScope.launch {
            repository.softDeleteCustomer(id)
        }
    }

    fun restoreCustomer(id: Long) {
        viewModelScope.launch {
            repository.restoreCustomer(id)
        }
    }

    fun permanentlyDeleteCustomer(id: Long) {
        viewModelScope.launch {
            repository.permanentlyDeleteCustomer(id)
        }
    }

    // Transaction Actions
    fun addTransaction(
        customerId: Long,
        type: TransactionType,
        amount: Long,
        quantity: Double? = null,
        unitPrice: Long? = null,
        discount: Long? = null,
        note: String = "",
        category: String = "",
        attachmentUri: String? = null,
        date: Long = System.currentTimeMillis(),
        onSuccess: ((Long) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val txnId = repository.addTransaction(
                khataId = activeKhataId.value,
                customerId = customerId,
                type = type,
                amount = amount,
                quantity = quantity,
                unitPrice = unitPrice,
                discount = discount,
                note = note,
                category = category,
                attachmentUri = attachmentUri,
                date = date
            )
            onSuccess?.invoke(txnId)
        }
    }

    fun updateTransaction(
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
        newCustomerId: Long? = null,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val updated = repository.updateTransaction(
                id = id,
                amount = amount,
                type = type,
                quantity = quantity,
                unitPrice = unitPrice,
                discount = discount,
                note = note,
                category = category,
                attachmentUri = attachmentUri,
                date = date,
                newCustomerId = newCustomerId
            )
            if (updated) {
                onSuccess?.invoke()
            }
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.softDeleteTransaction(id)
        }
    }

    fun restoreTransaction(id: Long) {
        viewModelScope.launch {
            repository.restoreTransaction(id)
        }
    }

    fun permanentlyDeleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.permanentlyDeleteTransaction(id)
        }
    }

    // Reminders
    fun addReminder(customerId: Long, dateTime: Long, note: String) {
        viewModelScope.launch {
            repository.addReminder(activeKhataId.value, customerId, dateTime, note)
        }
    }

    fun markReminderCompleted(id: Long) {
        viewModelScope.launch {
            repository.markReminderCompleted(id)
        }
    }

    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
        }
    }

    // Trash
    fun emptyTrash() {
        viewModelScope.launch {
            repository.emptyTrash()
        }
    }

    // Onboarding finish
    fun finishOnboarding(
        businessName: String,
        ownerName: String,
        phone: String,
        language: AppLanguage
    ) {
        viewModelScope.launch {
            preferences.setBusinessDetails(businessName, ownerName, phone, "")
            preferences.setLanguage(language)
            preferences.setOnboardingDone(true)
            val current = activeKhata.value
            if (current != null) {
                repository.updateKhata(current.copy(name = "মূল খাতা", businessName = businessName))
            }
            _isAppUnlocked.value = true
        }
    }

    fun finishOnboarding(businessName: String, khataName: String, pin: String) {
        viewModelScope.launch {
            preferences.setBusinessDetails(businessName, "", "", "")
            val defaultKhata = repository.getActiveKhatas()
            val current = activeKhata.value
            if (current != null) {
                repository.updateKhata(current.copy(name = khataName, businessName = businessName))
            } else {
                val newId = repository.createKhata(khataName, businessName = businessName, isDefault = true)
                preferences.setActiveKhataId(newId)
            }
            if (pin.isNotEmpty()) {
                preferences.setPin(pin, true)
            }
            preferences.setOnboardingDone(true)
            _isAppUnlocked.value = true
        }
    }

    fun updateProfile(business: String, owner: String, phoneNumber: String, addr: String) {
        updateBusinessDetails(business, owner, phoneNumber, addr)
    }

    fun renameKhata(khata: KhataEntity) {
        updateKhata(khata)
    }

    fun createCustomer(
        name: String,
        phone: String,
        address: String = "",
        businessName: String = "",
        notes: String = "",
        initialBalance: Long = 0L,
        onSuccess: ((Long) -> Unit)? = null
    ) {
        addCustomer(name, phone, address, businessName, notes, initialBalance, onSuccess)
    }

    fun createTransaction(
        customerId: Long,
        type: TransactionType,
        amount: Long,
        note: String = "",
        date: Long = System.currentTimeMillis(),
        quantity: Double? = null,
        unitPrice: Long? = null,
        discount: Long? = null,
        category: String = "",
        onSuccess: ((TransactionEntity) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val txnId = repository.addTransaction(
                khataId = activeKhataId.value,
                customerId = customerId,
                type = type,
                amount = amount,
                quantity = quantity,
                unitPrice = unitPrice,
                discount = discount,
                note = note,
                category = category,
                date = date
            )
            val txn = repository.getTransactionById(txnId)
            if (txn != null) {
                onSuccess?.invoke(txn)
            }
        }
    }

    fun createReminder(customerId: Long, dateTime: Long, note: String) {
        addReminder(customerId, dateTime, note)
    }

    fun checkAutoRestoreOnLaunch() {
        viewModelScope.launch {
            try {
                if (repository.isDatabaseEmpty()) {
                    val latest = LocalBackupManager.getLatestBackupPreview(getApplication())
                    if (latest != null) {
                        _detectedBackupForAutoRestore.value = latest
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun dismissAutoRestore() {
        _detectedBackupForAutoRestore.value = null
    }

    fun restoreFromAutoDetectedBackup(file: File, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _isRestoringBackup.value = true
            val payload = LocalBackupManager.restoreFromFile(file, repository, merge = false)
            if (payload != null) {
                val primaryKhata = payload.khatas.firstOrNull { it.isDefault } ?: payload.khatas.firstOrNull()
                if (primaryKhata != null) {
                    preferences.setActiveKhataId(primaryKhata.id)
                    if (primaryKhata.businessName.isNotEmpty()) {
                        preferences.setBusinessDetails(primaryKhata.businessName, "", "", "")
                    }
                }
                preferences.setOnboardingDone(true)
                _isAppUnlocked.value = true
                _detectedBackupForAutoRestore.value = null
                syncBackups()
                onSuccess?.invoke()
            }
            _isRestoringBackup.value = false
        }
    }

    fun syncBackups() {
        viewModelScope.launch {
            LocalBackupManager.syncBackups(getApplication(), repository)
        }
    }

    fun createBackup(onSuccess: ((BackupEntity) -> Unit)? = null) {
        viewModelScope.launch {
            val result = LocalBackupManager.createBackup(getApplication(), repository)
            syncBackups()
            if (result != null) {
                onSuccess?.invoke(result)
            }
        }
    }

    fun restoreBackup(file: File, merge: Boolean, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _isRestoringBackup.value = true
            val payload = LocalBackupManager.restoreFromFile(file, repository, merge)
            if (payload != null) {
                val primaryKhata = payload.khatas.firstOrNull { it.isDefault } ?: payload.khatas.firstOrNull()
                if (primaryKhata != null) {
                    preferences.setActiveKhataId(primaryKhata.id)
                }
                syncBackups()
                onSuccess?.invoke()
            }
            _isRestoringBackup.value = false
        }
    }

    fun deleteBackup(record: BackupEntity) {
        viewModelScope.launch {
            LocalBackupManager.deleteBackupFile(File(record.filePath))
            repository.deleteBackup(record)
            syncBackups()
        }
    }

    fun permanentDeleteCustomer(id: Long) {
        permanentlyDeleteCustomer(id)
    }

    fun permanentDeleteTransaction(id: Long) {
        permanentlyDeleteTransaction(id)
    }


    // Generate receipt data for a transaction
    fun getReceiptData(transaction: TransactionEntity): ReceiptData {
        val customer = rawCustomers.value.find { it.id == transaction.customerId }
        val bizName = businessName.value.ifEmpty { "টালি খাতা প্রো" }
        val kName = activeKhata.value?.name ?: "খাতা"

        return ReceiptData(
            receiptNo = "TK-${transaction.id}",
            businessName = bizName,
            khataName = kName,
            customerName = customer?.name ?: "কাস্টমার",
            customerPhone = customer?.phone ?: "",
            dateString = BanglaUtils.formatDate(transaction.date, isBangla = language.value == AppLanguage.BANGLA),
            timeString = BanglaUtils.formatTime(transaction.date, isBangla = language.value == AppLanguage.BANGLA),
            previousBalance = transaction.previousBalance,
            transactionAmount = transaction.amount,
            transactionType = TransactionType.fromString(transaction.type),
            currentBalance = transaction.newBalance,
            note = transaction.note,
            category = transaction.category
        )
    }
}
