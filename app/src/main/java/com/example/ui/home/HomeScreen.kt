package com.example.ui.home

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.CustomerEntity
import com.example.data.entities.ReminderEntity
import com.example.data.entities.TransactionEntity
import com.example.domain.model.AppLanguage
import com.example.domain.model.ChartBarData
import com.example.domain.model.DashboardSummary
import com.example.domain.utils.BanglaUtils
import com.example.ui.common.AmountText
import com.example.ui.common.EmptyStateView
import com.example.ui.common.QuickActionButton
import com.example.ui.common.SummaryCard
import com.example.ui.common.TallyHeader
import com.example.ui.common.TransactionRow
import com.example.ui.theme.AmberLight
import com.example.ui.theme.CreditGreen
import com.example.ui.theme.CreditGreenContainer
import com.example.ui.theme.OnCreditGreenContainer
import com.example.ui.theme.OnPaymentRedContainer
import com.example.ui.theme.PaymentRed
import com.example.ui.theme.PaymentRedContainer
import com.example.ui.theme.SleekIndigo
import com.example.ui.theme.SleekPurple
import com.example.ui.theme.SleekTeal

@Composable
fun HomeScreen(
    khataName: String,
    businessName: String,
    summary: DashboardSummary,
    chartData: List<ChartBarData>,
    recentTransactions: List<TransactionEntity>,
    customers: List<CustomerEntity>,
    pendingReminders: List<ReminderEntity>,
    language: AppLanguage,
    privacyMode: Boolean,
    onKhataClick: () -> Unit,
    onTogglePrivacy: () -> Unit,
    onToggleLanguage: () -> Unit,
    onAddCreditClick: () -> Unit,
    onAddPaymentClick: () -> Unit,
    onAddCustomerClick: () -> Unit,
    onVoiceEntryClick: () -> Unit,
    onTransactionClick: (TransactionEntity) -> Unit,
    onViewAllTransactions: () -> Unit,
    onReminderClick: () -> Unit
) {
    val isBangla = language == AppLanguage.BANGLA
    val customerMap = customers.associateBy { it.id }
    var selectedChartDayIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Header
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))

                // Hero Cards: Total Receivable & Total Payable
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        title = if (isBangla) "মোট পাওনা (Receivable)" else "Total Receivable",
                        amount = summary.totalReceivable,
                        isCredit = true,
                        isPrivacyMode = privacyMode,
                        isBangla = isBangla,
                        subtitle = if (isBangla) "${BanglaUtils.toBanglaDigits(summary.totalCustomers.toLong())} জন কাস্টমার" else "${summary.totalCustomers} Customers",
                        modifier = Modifier
                            .weight(1f)
                            .testTag("summary_receivable_card")
                    )
                    SummaryCard(
                        title = if (isBangla) "মোট দেনা (Payable)" else "Total Payable",
                        amount = summary.totalPayable,
                        isCredit = false,
                        isPrivacyMode = privacyMode,
                        isBangla = isBangla,
                        subtitle = if (isBangla) "অগ্রিম জমা" else "Advance Paid",
                        modifier = Modifier
                            .weight(1f)
                            .testTag("summary_payable_card")
                    )
                }
            }

            // Quick Actions 4-Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionButton(
                        label = if (isBangla) "+ পাওনা" else "+ Credit",
                        icon = Icons.Default.Add,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = SleekTeal,
                        onClick = onAddCreditClick,
                        modifier = Modifier.weight(1f),
                        testTag = "quick_add_credit"
                    )
                    QuickActionButton(
                        label = if (isBangla) "− জমা" else "− Payment",
                        icon = Icons.Default.Remove,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = PaymentRed,
                        onClick = onAddPaymentClick,
                        modifier = Modifier.weight(1f),
                        testTag = "quick_add_payment"
                    )
                    QuickActionButton(
                        label = if (isBangla) "কাস্টমার" else "Customer",
                        icon = Icons.Default.PersonAdd,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = SleekIndigo,
                        onClick = onAddCustomerClick,
                        modifier = Modifier.weight(1f),
                        testTag = "quick_add_customer"
                    )
                    QuickActionButton(
                        label = if (isBangla) "ভয়েস" else "Voice",
                        icon = Icons.Default.Mic,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = SleekPurple,
                        onClick = onVoiceEntryClick,
                        modifier = Modifier.weight(1f),
                        testTag = "quick_voice_entry"
                    )
                }
            }

            // Pending Reminders Alert Banner
            if (pendingReminders.isNotEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onReminderClick() }
                            .testTag("home_reminder_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isBangla) "বকেয়া তাগাদার রিমাইন্ডার রয়েছে" else "Pending Payment Reminders",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF78350F)
                                )
                                Text(
                                    text = if (isBangla) "${BanglaUtils.toBanglaDigits(pendingReminders.size.toLong())} টি তাগাদা বাকি আছে" else "${pendingReminders.size} reminders pending",
                                    fontSize = 11.sp,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }
                    }
                }
            }

            // Today's Activity Strip
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 4.dp, height = 16.dp)
                                        .clip(CircleShape)
                                        .background(SleekTeal)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBangla) "আজকের সারসংক্ষেপ" else "Today's Activity",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SleekTeal.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = if (isBangla) "${BanglaUtils.toBanglaDigits(summary.todayTxnCount.toLong())} টি লেনদেন" else "${summary.todayTxnCount} Txns",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekTeal,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = if (isBangla) "আজকের পাওনা" else "Today Credit",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                AmountText(
                                    amount = summary.todayCredit,
                                    isPrivacyMode = privacyMode,
                                    isBangla = isBangla,
                                    fontSize = 16,
                                    color = CreditGreen
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (isBangla) "আজকের জমা" else "Today Payment",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                AmountText(
                                    amount = summary.todayPayment,
                                    isPrivacyMode = privacyMode,
                                    isBangla = isBangla,
                                    fontSize = 16,
                                    color = PaymentRed
                                )
                            }
                        }
                    }
                }
            }

            // Visual 7-Day Trend Chart
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                    modifier = Modifier.fillMaxWidth().testTag("home_analytics_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 4.dp, height = 16.dp)
                                        .clip(CircleShape)
                                        .background(SleekTeal)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBangla) "৭ দিনের লেনদেন গ্রাফ" else "7-Day Analytics",
                                    fontSize = 14.sp,
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

                        val maxAmount = chartData.maxOfOrNull { maxOf(it.creditAmount, it.paymentAmount) } ?: 0L
                        val total7DayCredit = chartData.sumOf { it.creditAmount }
                        val total7DayPayment = chartData.sumOf { it.paymentAmount }
                        val selectedData = selectedChartDayIndex?.let { chartData.getOrNull(it) }

                        // Inspection and Summary Pill
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (selectedData != null) {
                                    Text(
                                        text = "${selectedData.label}:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
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
                                } else {
                                    Text(
                                        text = if (isBangla) "গত ৭ দিনে মোট" else "7-Day Total",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = (if (isBangla) "পাওনা: " else "Credit: ") + BanglaUtils.formatTaka(total7DayCredit, isBangla),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = CreditGreen
                                        )
                                        Text(
                                            text = (if (isBangla) "জমা: " else "Payment: ") + BanglaUtils.formatTaka(total7DayPayment, isBangla),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PaymentRed
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Custom Bar Chart
                        if (maxAmount <= 0L) {
                            // Clean Zero-state layout showing day labels & prompt
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isBangla) "গত ৭ দিনে কোনো লেনদেন নেই" else "No transactions in the last 7 days",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    chartData.forEach { data ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.outlineVariant)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = data.label,
                                                fontSize = 10.sp,
                                                fontWeight = if (data.isToday) FontWeight.Bold else FontWeight.Normal,
                                                color = if (data.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(95.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                chartData.forEachIndexed { index, data ->
                                    val isSelected = selectedChartDayIndex == index
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else Color.Transparent)
                                            .clickable {
                                                selectedChartDayIndex = if (isSelected) null else index
                                            }
                                            .padding(vertical = 4.dp)
                                            .testTag("chart_bar_$index")
                                    ) {
                                        // Bars Area
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth(),
                                            contentAlignment = Alignment.BottomCenter
                                        ) {
                                            if (data.creditAmount == 0L && data.paymentAmount == 0L) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
                                                )
                                            } else {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                                    verticalAlignment = Alignment.Bottom
                                                ) {
                                                    // Credit Bar
                                                    if (data.creditAmount > 0L) {
                                                        val fraction = (data.creditAmount.toFloat() / maxAmount).coerceIn(0.10f, 1f)
                                                        Box(
                                                            modifier = Modifier
                                                                .width(9.dp)
                                                                .height((65 * fraction).dp)
                                                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                                                .background(CreditGreen)
                                                        )
                                                    }

                                                    // Payment Bar
                                                    if (data.paymentAmount > 0L) {
                                                        val fraction = (data.paymentAmount.toFloat() / maxAmount).coerceIn(0.10f, 1f)
                                                        Box(
                                                            modifier = Modifier
                                                                .width(9.dp)
                                                                .height((65 * fraction).dp)
                                                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                                                .background(PaymentRed)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = data.label,
                                            fontSize = 10.sp,
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

            // Recent Transactions Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(width = 4.dp, height = 16.dp)
                                .clip(CircleShape)
                                .background(SleekTeal)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBangla) "সাম্প্রতিক লেনদেন" else "Recent Transactions",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    TextButton(onClick = onViewAllTransactions) {
                        Text(
                            text = if (isBangla) "সব দেখুন" else "View All",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekTeal
                        )
                    }
                }
            }

            // Transactions List
            if (recentTransactions.isEmpty()) {
                item {
                    EmptyStateView(
                        title = if (isBangla) "কোনো লেনদেন পাওয়া যায়নি" else "No transactions yet",
                        subtitle = if (isBangla) "+ পাওনা বা − জমা বাটনে ট্যাপ করে নতুন লেনদেন শুরু করুন" else "Tap + Credit or − Payment to add your first transaction",
                        buttonText = if (isBangla) "+ লেনদেন যোগ করুন" else "+ Add Transaction",
                        onButtonClick = onAddCreditClick
                    )
                }
            } else {
                items(recentTransactions) { txn ->
                    val customer = customerMap[txn.customerId]
                    val customerName = customer?.name ?: if (isBangla) "কাস্টমার" else "Customer"
                    TransactionRow(
                        transaction = txn,
                        customerName = customerName,
                        isPrivacyMode = privacyMode,
                        isBangla = isBangla,
                        onClick = { onTransactionClick(txn) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
