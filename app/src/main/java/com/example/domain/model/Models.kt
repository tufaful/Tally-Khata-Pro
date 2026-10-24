package com.example.domain.model

enum class TransactionType(val banglaLabel: String, val englishLabel: String) {
    CREDIT("পাওনা", "Credit"),
    PAYMENT("জমা", "Payment");

    companion object {
        fun fromString(value: String): TransactionType {
            return if (value.equals("PAYMENT", ignoreCase = true)) PAYMENT else CREDIT
        }
    }
}

enum class CustomerFilter(val banglaLabel: String, val englishLabel: String) {
    ALL("সকল", "All"),
    DUE("পাওনাদার", "Due / Credit"),
    ADVANCE("দেনা / জমা", "Advance / Paid"),
    SETTLED("পরিশোধিত", "Settled")
}

enum class CustomerSort(val banglaLabel: String, val englishLabel: String) {
    RECENT("সাম্প্রতিক লেনদেন", "Recent"),
    BALANCE_HIGH_TO_LOW("সর্বোচ্চ বাকি", "Highest Due"),
    NAME_AZ("নাম (অ - হ / A - Z)", "Name A-Z")
}

enum class ReportPeriod(val banglaLabel: String, val englishLabel: String) {
    TODAY("আজকে", "Today"),
    THIS_WEEK("এই সপ্তাহ", "This Week"),
    THIS_MONTH("এই মাস", "This Month"),
    THIS_YEAR("এই বছর", "This Year"),
    ALL_TIME("সর্বমোট", "All Time")
}

enum class AppLanguage(val code: String, val displayName: String) {
    BANGLA("bn", "বাংলা"),
    ENGLISH("en", "English")
}

enum class AppThemeMode(val banglaLabel: String, val englishLabel: String) {
    SYSTEM("সিস্টেম ডিফল্ট", "System Default"),
    LIGHT("লাইট থিম", "Light Theme"),
    DARK("ডার্ক থিম", "Dark Theme")
}

typealias ThemeMode = AppThemeMode

enum class AutoBackupSchedule(val banglaLabel: String, val englishLabel: String) {
    OFF("বন্ধ", "Off"),
    DAILY("প্রতিদিন", "Daily"),
    WEEKLY("সাপ্তাহিক", "Weekly"),
    MONTHLY("মাসিক", "Monthly")
}

data class DashboardSummary(
    val totalReceivable: Long = 0L, // মোট পাওনা
    val totalPayable: Long = 0L,    // মোট দেনা
    val todayCredit: Long = 0L,     // আজকের পাওনা
    val todayPayment: Long = 0L,    // আজকের জমা
    val totalCustomers: Int = 0,    // মোট কাস্টমার
    val todayTxnCount: Int = 0,     // আজকের লেনদেন সংখ্যা
    val monthTxnCount: Int = 0,     // এই মাসের লেনদেন সংখ্যা
    val monthCredit: Long = 0L,     // এই মাসের মোট পাওনা
    val monthPayment: Long = 0L     // এই মাসের মোট জমা
)

data class ReceiptData(
    val receiptNo: String,
    val businessName: String,
    val khataName: String,
    val customerName: String,
    val customerPhone: String,
    val dateString: String,
    val timeString: String,
    val previousBalance: Long,
    val transactionAmount: Long,
    val transactionType: TransactionType,
    val currentBalance: Long,
    val note: String,
    val category: String = ""
)

data class ChartBarData(
    val label: String,
    val creditAmount: Long,
    val paymentAmount: Long,
    val fullDateLabel: String = "",
    val timestamp: Long = 0L,
    val isToday: Boolean = false
)
