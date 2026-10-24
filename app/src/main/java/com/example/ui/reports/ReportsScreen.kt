package com.example.ui.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.CustomerEntity
import com.example.data.entities.TransactionEntity
import com.example.domain.csv.CsvExportHelper
import com.example.domain.model.AppLanguage
import com.example.domain.model.ChartBarData
import com.example.domain.model.ReceiptData
import com.example.domain.model.ReportPeriod
import com.example.domain.model.TransactionType
import com.example.domain.pdf.ReceiptPdfGenerator
import com.example.domain.utils.BanglaUtils
import com.example.ui.common.AmountText
import com.example.ui.common.EmptyStateView
import com.example.ui.common.TallyHeader
import com.example.ui.theme.CreditGreen
import com.example.ui.theme.CreditGreenContainer
import com.example.ui.theme.PaymentRed
import com.example.ui.theme.PaymentRedContainer
import java.util.Calendar

@Composable
fun ReportsScreen(
    khataName: String,
    businessName: String,
    allTransactions: List<TransactionEntity>,
    customers: List<CustomerEntity>,
    language: AppLanguage,
    privacyMode: Boolean,
    onKhataClick: () -> Unit,
    onTogglePrivacy: () -> Unit,
    onToggleLanguage: () -> Unit,
    onTransactionClick: (TransactionEntity) -> Unit
) {
    val context = LocalContext.current
    val isBangla = language == AppLanguage.BANGLA
    var selectedPeriod by remember { mutableStateOf(ReportPeriod.THIS_MONTH) }
    var selectedAnalyticsIndex by remember { mutableStateOf<Int?>(null) }

    val customerMap = customers.associateBy { it.id }

    // Filter transactions based on period
    val now = Calendar.getInstance()
    val startTimestamp = when (selectedPeriod) {
        ReportPeriod.TODAY -> Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        ReportPeriod.THIS_WEEK -> Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        ReportPeriod.THIS_MONTH -> Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        ReportPeriod.THIS_YEAR -> Calendar.getInstance().apply {
            set(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        ReportPeriod.ALL_TIME -> 0L
    }

    val filteredTxns = allTransactions.filter { it.date >= startTimestamp }

    var periodCredit = 0L
    var periodPayment = 0L
    for (t in filteredTxns) {
        if (t.type == TransactionType.CREDIT.name) periodCredit += t.amount
        else periodPayment += t.amount
    }
    val netBalance = periodCredit - periodPayment

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        TallyHeader(
            khataName = khataName,
            businessName = businessName,
            language = language,
            privacyMode = privacyMode,
            onKhataClick = onKhataClick,
            onTogglePrivacy = onTogglePrivacy,
            onToggleLanguage = onToggleLanguage
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // Time Range Selector Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ReportPeriod.values()) { period ->
                        val isSelected = period == selectedPeriod
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable {
                                    selectedPeriod = period
                                    selectedAnalyticsIndex = null
                                }
                                .testTag("period_chip_${period.name}")
                        ) {
                            Text(
                                text = if (isBangla) period.banglaLabel else period.englishLabel,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Summary Metrics Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isBangla) "নির্বাচিত সময়ের আর্থিক রিপোর্ট" else "Financial Summary",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Total Credit
                            Column {
                                Text(
                                    text = if (isBangla) "মোট পাওনা (Credit)" else "Total Credit",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                AmountText(
                                    amount = periodCredit,
                                    isPrivacyMode = privacyMode,
                                    isBangla = isBangla,
                                    fontSize = 18,
                                    color = CreditGreen
                                )
                            }

                            // Total Payment
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (isBangla) "মোট জমা (Payment)" else "Total Payment",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                AmountText(
                                    amount = periodPayment,
                                    isPrivacyMode = privacyMode,
                                    isBangla = isBangla,
                                    fontSize = 18,
                                    color = PaymentRed
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Net Balance & Count
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isBangla) "নিট বাকি (পাওনা − জমা)" else "Net Balance",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                AmountText(
                                    amount = netBalance,
                                    isPrivacyMode = privacyMode,
                                    isBangla = isBangla,
                                    fontSize = 15,
                                    color = if (netBalance >= 0) CreditGreen else PaymentRed
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (isBangla) "মোট লেনদেন" else "Transactions",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (isBangla) "${BanglaUtils.toBanglaDigits(filteredTxns.size.toLong())} টি" else "${filteredTxns.size} Txns",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Transaction Analytics Graph Card
            item {
                val periodChartData = remember(selectedPeriod, filteredTxns, isBangla) {
                    generatePeriodChartData(selectedPeriod, filteredTxns, isBangla)
                }
                val maxChartVal = periodChartData.maxOfOrNull { maxOf(it.creditAmount, it.paymentAmount) } ?: 0L
                val selectedData = selectedAnalyticsIndex?.let { periodChartData.getOrNull(it) }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth().testTag("reports_analytics_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBangla) "লেনদেন বিশ্লেষণ গ্রাফ" else "Analytics Graph",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Legend
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CreditGreen))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = if (isBangla) "পাওনা" else "Credit", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(PaymentRed))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = if (isBangla) "জমা" else "Payment", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Credit vs Payment Ratio bar if there is money flow
                        val totalFlow = periodCredit + periodPayment
                        if (totalFlow > 0L) {
                            val creditPercent = ((periodCredit.toDouble() / totalFlow) * 100).toInt()
                            val paymentPercent = 100 - creditPercent

                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    if (creditPercent > 0) {
                                        Box(
                                            modifier = Modifier
                                                .weight(creditPercent.toFloat())
                                                .fillMaxSize()
                                                .background(CreditGreen)
                                        )
                                    }
                                    if (paymentPercent > 0) {
                                        Box(
                                            modifier = Modifier
                                                .weight(paymentPercent.toFloat())
                                                .fillMaxSize()
                                                .background(PaymentRed)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = (if (isBangla) "পাওনা: " else "Credit: ") +
                                                (if (isBangla) BanglaUtils.toBanglaDigits(creditPercent.toLong()) else creditPercent.toString()) + "%",
                                        fontSize = 10.sp,
                                        color = CreditGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = (if (isBangla) "জমা: " else "Payment: ") +
                                                (if (isBangla) BanglaUtils.toBanglaDigits(paymentPercent.toLong()) else paymentPercent.toString()) + "%",
                                        fontSize = 10.sp,
                                        color = PaymentRed,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Inspection Pill
                        if (selectedData != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${selectedData.label}:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = (if (isBangla) "পাওনা: " else "Credit: ") + BanglaUtils.formatTaka(selectedData.creditAmount, isBangla),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = CreditGreen
                                        )
                                        Text(
                                            text = (if (isBangla) "জমা: " else "Payment: ") + BanglaUtils.formatTaka(selectedData.paymentAmount, isBangla),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PaymentRed
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Bar Chart Display
                        if (maxChartVal <= 0L) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(70.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isBangla) "নির্বাচিত সময়ে কোনো লেনদেন বিশ্লেষণ নেই" else "No analytics data for this period",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(95.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                periodChartData.forEachIndexed { idx, data ->
                                    val isSelected = selectedAnalyticsIndex == idx
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else Color.Transparent)
                                            .clickable {
                                                selectedAnalyticsIndex = if (isSelected) null else idx
                                            }
                                            .padding(vertical = 4.dp)
                                            .testTag("report_chart_bar_$idx")
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth(),
                                            contentAlignment = Alignment.BottomCenter
                                        ) {
                                            if (data.creditAmount == 0L && data.paymentAmount == 0L) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(3.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                                                )
                                            } else {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                    verticalAlignment = Alignment.Bottom
                                                ) {
                                                    if (data.creditAmount > 0L) {
                                                        val fraction = (data.creditAmount.toFloat() / maxChartVal).coerceIn(0.10f, 1f)
                                                        Box(
                                                            modifier = Modifier
                                                                .width(7.dp)
                                                                .height((60 * fraction).dp)
                                                                .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                                                .background(CreditGreen)
                                                        )
                                                    }
                                                    if (data.paymentAmount > 0L) {
                                                        val fraction = (data.paymentAmount.toFloat() / maxChartVal).coerceIn(0.10f, 1f)
                                                        Box(
                                                            modifier = Modifier
                                                                .width(7.dp)
                                                                .height((60 * fraction).dp)
                                                                .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                                                .background(PaymentRed)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = data.label,
                                            fontSize = 9.sp,
                                            fontWeight = if (data.isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (data.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val csvFile = CsvExportHelper.generateTransactionReportCsv(
                                context = context,
                                khataName = khataName,
                                customersMap = customerMap,
                                transactions = filteredTxns
                            )
                            if (csvFile != null) {
                                CsvExportHelper.shareCsvFile(context, csvFile, "টালি খাতা রিপোর্ট ($khataName)")
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_csv_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBangla) "CSV ডাউনলোড" else "Download CSV")
                    }

                    OutlinedButton(
                        onClick = {
                            // PDF Summary generator using first customer or business slip
                            if (filteredTxns.isNotEmpty()) {
                                val dummyReceipt = ReceiptData(
                                    receiptNo = "REP-${System.currentTimeMillis().toString().takeLast(4)}",
                                    businessName = businessName,
                                    khataName = khataName,
                                    customerName = "সকল কাস্টমার রিপোর্ট (${selectedPeriod.banglaLabel})",
                                    customerPhone = "",
                                    dateString = BanglaUtils.formatDate(System.currentTimeMillis(), isBangla),
                                    timeString = BanglaUtils.formatTime(System.currentTimeMillis(), isBangla),
                                    previousBalance = periodPayment,
                                    transactionAmount = periodCredit,
                                    transactionType = TransactionType.CREDIT,
                                    currentBalance = netBalance,
                                    note = "মোট লেনদেন সংখ্যা: ${filteredTxns.size}"
                                )
                                val file = ReceiptPdfGenerator.generateReceiptPdf(context, dummyReceipt)
                                if (file != null) {
                                    ReceiptPdfGenerator.shareReceiptFile(context, file, businessName)
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_pdf_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBangla) "PDF রিপোর্ট" else "PDF Report")
                    }
                }
            }

            // Transactions breakdown list
            item {
                Text(
                    text = if (isBangla) "লেনদেনের তালিকা (${BanglaUtils.toBanglaDigits(filteredTxns.size.toLong())})" else "Transactions (${filteredTxns.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (filteredTxns.isEmpty()) {
                item {
                    EmptyStateView(
                        title = if (isBangla) "এই সময়ে কোনো লেনদেন নেই" else "No transactions in this period",
                        subtitle = if (isBangla) "অন্য কোনো সময়সীমা নির্বাচন করুন" else "Try selecting another time range"
                    )
                }
            } else {
                items(filteredTxns) { txn ->
                    val customer = customerMap[txn.customerId]
                    val isCredit = txn.type == TransactionType.CREDIT.name
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTransactionClick(txn) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = customer?.name ?: if (isBangla) "কাস্টমার" else "Customer",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${BanglaUtils.formatDate(txn.date, isBangla)} ${if (txn.note.isNotEmpty()) "• ${txn.note}" else ""}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                AmountText(
                                    amount = txn.amount,
                                    isPrivacyMode = privacyMode,
                                    isBangla = isBangla,
                                    fontSize = 15,
                                    color = if (isCredit) CreditGreen else PaymentRed
                                )
                                Text(
                                    text = if (isCredit) (if (isBangla) "পাওনা" else "Credit") else (if (isBangla) "জমা" else "Payment"),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCredit) CreditGreen else PaymentRed
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// Generate chart bar data for selected period in Reports
private fun generatePeriodChartData(
    period: ReportPeriod,
    transactions: List<TransactionEntity>,
    isBangla: Boolean
): List<ChartBarData> {
    val result = mutableListOf<ChartBarData>()

    when (period) {
        ReportPeriod.TODAY -> {
            val slots = listOf(
                Triple(if (isBangla) "সকাল" else "Morn", 6, 12),
                Triple(if (isBangla) "দুপুর" else "Noon", 12, 16),
                Triple(if (isBangla) "বিকাল" else "Eve", 16, 19),
                Triple(if (isBangla) "রাত" else "Night", 19, 24),
                Triple(if (isBangla) "ভোর" else "Dawn", 0, 6)
            )
            for (slot in slots) {
                var cred = 0L
                var pay = 0L
                for (t in transactions) {
                    val c = Calendar.getInstance().apply { timeInMillis = t.date }
                    val hour = c.get(Calendar.HOUR_OF_DAY)
                    if (hour in slot.second until slot.third) {
                        if (t.type == TransactionType.CREDIT.name) cred += t.amount else pay += t.amount
                    }
                }
                result.add(ChartBarData(label = slot.first, creditAmount = cred, paymentAmount = pay))
            }
        }
        ReportPeriod.THIS_WEEK -> {
            for (i in 6 downTo 0) {
                val startC = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -i)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val endC = (startC.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                val sMs = startC.timeInMillis
                val eMs = if (i == 0) Long.MAX_VALUE else endC.timeInMillis
                val dayOfWeek = startC.get(Calendar.DAY_OF_WEEK)
                val label = when {
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
                var cred = 0L
                var pay = 0L
                for (t in transactions) {
                    if (t.date in sMs..eMs) {
                        if (t.type == TransactionType.CREDIT.name) cred += t.amount else pay += t.amount
                    }
                }
                result.add(ChartBarData(label = label, creditAmount = cred, paymentAmount = pay, isToday = (i == 0)))
            }
        }
        ReportPeriod.THIS_MONTH -> {
            val weeks = listOf(
                Pair(if (isBangla) "সপ্তাহ ১" else "Wk 1", 1..7),
                Pair(if (isBangla) "সপ্তাহ ২" else "Wk 2", 8..14),
                Pair(if (isBangla) "সপ্তাহ ৩" else "Wk 3", 15..21),
                Pair(if (isBangla) "সপ্তাহ ৪" else "Wk 4", 22..28),
                Pair(if (isBangla) "সপ্তাহ ৫" else "Wk 5", 29..31)
            )
            for (w in weeks) {
                var cred = 0L
                var pay = 0L
                for (t in transactions) {
                    val c = Calendar.getInstance().apply { timeInMillis = t.date }
                    val day = c.get(Calendar.DAY_OF_MONTH)
                    if (day in w.second) {
                        if (t.type == TransactionType.CREDIT.name) cred += t.amount else pay += t.amount
                    }
                }
                result.add(ChartBarData(label = w.first, creditAmount = cred, paymentAmount = pay))
            }
        }
        ReportPeriod.THIS_YEAR, ReportPeriod.ALL_TIME -> {
            val monthLabelsBn = listOf("জানু", "ফেব", "মার", "এপ্রি", "মে", "জুন", "জুলা", "আগ", "সেপ্ট", "অক্টো", "নভে", "ডিসে")
            val monthLabelsEn = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
            val count = if (period == ReportPeriod.THIS_YEAR) 12 else 6
            for (m in 0 until count) {
                val targetMonth = if (period == ReportPeriod.THIS_YEAR) m else (currentMonth - (count - 1 - m) + 12) % 12
                val label = if (isBangla) monthLabelsBn[targetMonth] else monthLabelsEn[targetMonth]
                var cred = 0L
                var pay = 0L
                for (t in transactions) {
                    val c = Calendar.getInstance().apply { timeInMillis = t.date }
                    if (c.get(Calendar.MONTH) == targetMonth) {
                        if (t.type == TransactionType.CREDIT.name) cred += t.amount else pay += t.amount
                    }
                }
                result.add(ChartBarData(label = label, creditAmount = cred, paymentAmount = pay, isToday = (targetMonth == currentMonth)))
            }
        }
    }
    return result
}
