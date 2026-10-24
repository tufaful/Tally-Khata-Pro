package com.example.ui.customer

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.CustomerEntity
import com.example.data.entities.TransactionEntity
import com.example.domain.model.AppLanguage
import com.example.domain.utils.BanglaUtils
import com.example.ui.common.AmountText
import com.example.ui.common.EmptyStateView
import com.example.ui.common.TransactionRow
import com.example.ui.theme.CreditGreen
import com.example.ui.theme.CreditGreenBorder
import com.example.ui.theme.CreditGreenContainer
import com.example.ui.theme.PaymentRed
import com.example.ui.theme.PaymentRedBorder
import com.example.ui.theme.PaymentRedContainer
import com.example.ui.theme.SleekTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customer: CustomerEntity,
    transactions: List<TransactionEntity>,
    businessName: String,
    language: AppLanguage,
    privacyMode: Boolean,
    onBackClick: () -> Unit,
    onAddCreditClick: () -> Unit,
    onAddPaymentClick: () -> Unit,
    onTransactionClick: (TransactionEntity) -> Unit,
    onUpdateCustomer: (CustomerEntity) -> Unit,
    onDeleteCustomer: (Long) -> Unit,
    onAddReminder: (customerId: Long, dateTime: Long, note: String) -> Unit
) {
    val context = LocalContext.current
    val isBangla = language == AppLanguage.BANGLA

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }

    val isDue = customer.currentBalance > 0
    val statusColor = if (isDue) CreditGreen else if (customer.currentBalance < 0) PaymentRed else Color.Gray

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = customer.name,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val subtitleText = when {
                            customer.businessName.isNotEmpty() && customer.phone.isNotEmpty() -> "${customer.businessName} • ${customer.phone}"
                            customer.businessName.isNotEmpty() -> customer.businessName
                            customer.phone.isNotEmpty() -> customer.phone
                            else -> null
                        }
                        if (subtitleText != null) {
                            Text(
                                text = subtitleText,
                                fontSize = 12.sp,
                                color = SleekTeal,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Customer", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Customer", tint = PaymentRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        bottomBar = {
            // Sticky Bottom Action Bar: + পাওনা / − জমা
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)),
                tonalElevation = 0.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onAddCreditClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("detail_add_credit_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
                    ) {
                        Text(
                            text = if (isBangla) "+ পাওনা (বাকি)" else "+ Credit",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = onAddPaymentClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("detail_add_payment_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                    ) {
                        Text(
                            text = if (isBangla) "− জমা (পরিশোধ)" else "− Payment",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // Big Balance Hero Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDue) CreditGreenContainer else PaymentRedContainer),
                    border = BorderStroke(1.dp, if (isDue) CreditGreenBorder else PaymentRedBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isDue) (if (isBangla) "বর্তমান মোট পাওনা (আপনি পাবেন)" else "Current Due (You'll Get)") else (if (isBangla) "বর্তমান মোট দেনা (অগ্রিম জমা)" else "Current Advance (You Owe)"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        AmountText(
                            amount = customer.currentBalance,
                            isPrivacyMode = privacyMode,
                            isBangla = isBangla,
                            fontSize = 32,
                            color = statusColor
                        )
                    }
                }
            }

            // Quick Contact / Communication Buttons
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CustomerActionTile(
                            icon = Icons.Default.Call,
                            label = if (isBangla) "কল" else "Call",
                            iconTint = Color(0xFF00796B),
                            containerColor = Color(0xFFE0F2F1),
                            testTag = "btn_call_customer",
                            onClick = {
                                if (customer.phone.isNotEmpty()) {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))
                                    context.startActivity(dialIntent)
                                } else {
                                    Toast.makeText(
                                        context,
                                        if (isBangla) "মোবাইল নম্বর দেওয়া নেই" else "No phone number available",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )

                        CustomerActionTile(
                            icon = Icons.Default.Sms,
                            label = if (isBangla) "এসএমএস" else "SMS",
                            iconTint = Color(0xFF1D4ED8),
                            containerColor = Color(0xFFDBEAFE),
                            testTag = "btn_sms_customer",
                            onClick = {
                                if (customer.phone.isNotEmpty()) {
                                    val smsBody = "প্রিয় ${customer.name}, ${businessName}-এ আপনার বর্তমান বাকি ৳${customer.currentBalance}। অনুগ্রহ করে পরিশোধ করুন। ধন্যবাদ।"
                                    val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${customer.phone}")).apply {
                                        putExtra("sms_body", smsBody)
                                    }
                                    try {
                                        context.startActivity(smsIntent)
                                    } catch (e: Exception) {
                                        // Fallback general share
                                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, smsBody)
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "শেয়ার করুন"))
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        if (isBangla) "মোবাইল নম্বর দেওয়া নেই" else "No phone number available",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )

                        CustomerActionTile(
                            icon = Icons.Default.Alarm,
                            label = if (isBangla) "তাগাদা" else "Alert",
                            iconTint = Color(0xFFD97706),
                            containerColor = Color(0xFFFEF3C7),
                            testTag = "btn_reminder_customer",
                            onClick = { showReminderDialog = true },
                            modifier = Modifier.weight(1f)
                        )

                        CustomerActionTile(
                            icon = Icons.Default.Share,
                            label = if (isBangla) "শেয়ার" else "Share",
                            iconTint = Color(0xFF7C3AED),
                            containerColor = Color(0xFFEDE9FE),
                            testTag = "btn_share_customer",
                            onClick = {
                                val shareText = "হিসাবের বিবরণ: ${customer.name}\n${businessName}\nবর্তমান বাকি: ৳${customer.currentBalance}\nমোট পাওনা: ৳${customer.totalCredit}\nমোট জমা: ৳${customer.totalPayment}"
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "শেয়ার করুন"))
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Statistics Strip: Total Credit, Total Payment, Total Txns
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isBangla) "মোট পাওনা" else "Total Credit",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            AmountText(
                                amount = customer.totalCredit,
                                isPrivacyMode = privacyMode,
                                isBangla = isBangla,
                                fontSize = 14,
                                fontWeight = FontWeight.Bold,
                                color = CreditGreen
                            )
                        }

                        Box(
                            modifier = Modifier
                                .height(28.dp)
                                .width(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isBangla) "মোট জমা" else "Total Payment",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            AmountText(
                                amount = customer.totalPayment,
                                isPrivacyMode = privacyMode,
                                isBangla = isBangla,
                                fontSize = 14,
                                fontWeight = FontWeight.Bold,
                                color = PaymentRed
                            )
                        }

                        Box(
                            modifier = Modifier
                                .height(28.dp)
                                .width(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isBangla) "মোট লেনদেন" else "Total Txns",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (isBangla) "${BanglaUtils.toBanglaDigits(customer.totalTransactions.toLong())} টি" else "${customer.totalTransactions} Txns",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Transaction History Section Header
            item {
                Text(
                    text = if (isBangla) "লেনদেনের ইতিহাস (${BanglaUtils.toBanglaDigits(transactions.size.toLong())})" else "Transaction History (${transactions.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Transactions
            if (transactions.isEmpty()) {
                item {
                    EmptyStateView(
                        title = if (isBangla) "কোনো লেনদেন পাওয়া যায়নি" else "No transactions yet",
                        subtitle = if (isBangla) "নিচের বাটন দিয়ে পাওনা বা জমা এন্ট্রি করুন" else "Add credit or payment using buttons below"
                    )
                }
            } else {
                items(transactions) { txn ->
                    TransactionRow(
                        transaction = txn,
                        customerName = customer.name,
                        isPrivacyMode = privacyMode,
                        isBangla = isBangla,
                        onClick = { onTransactionClick(txn) },
                        showCustomerName = false
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Edit Customer Dialog
    if (showEditDialog) {
        var editName by remember { mutableStateOf(customer.name) }
        var editPhone by remember { mutableStateOf(customer.phone) }
        var editAddress by remember { mutableStateOf(customer.address) }
        var editBusiness by remember { mutableStateOf(customer.businessName) }
        var editNotes by remember { mutableStateOf(customer.notes) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(if (isBangla) "কাস্টমার তথ্য পরিবর্তন" else "Edit Customer") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text(if (isBangla) "নাম" else "Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text(if (isBangla) "মোবাইল" else "Phone") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editBusiness,
                        onValueChange = { editBusiness = it },
                        label = { Text(if (isBangla) "দোকান / প্রতিষ্ঠান" else "Business") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editAddress,
                        onValueChange = { editAddress = it },
                        label = { Text(if (isBangla) "ঠিকানা" else "Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isNotBlank()) {
                            onUpdateCustomer(
                                customer.copy(
                                    name = editName.trim(),
                                    phone = editPhone.trim(),
                                    businessName = editBusiness.trim(),
                                    address = editAddress.trim(),
                                    notes = editNotes.trim()
                                )
                            )
                            showEditDialog = false
                        }
                    },
                    enabled = editName.isNotBlank()
                ) {
                    Text(if (isBangla) "সংরক্ষণ" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Delete Confirmation
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(if (isBangla) "কাস্টমার মুছে ফেলবেন?" else "Delete Customer?") },
            text = {
                Text(
                    if (isBangla)
                        "কাস্টমারকে রিসাইকেল বিনে (Trash) পাঠানো হবে। পরবর্তীতে পুনরুদ্ধার করতে পারবেন।"
                    else
                        "The customer will be moved to the Recycle Bin."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCustomer(customer.id)
                        showDeleteConfirm = false
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isBangla) "মুছে ফেলুন" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Add Reminder Dialog
    if (showReminderDialog) {
        var reminderNote by remember { mutableStateOf("বাকি টাকার তাগাদা") }
        var reminderDays by remember { mutableStateOf(3) }

        AlertDialog(
            onDismissRequest = { showReminderDialog = false },
            title = { Text(if (isBangla) "তাগাদা রিমাইন্ডার সেট করুন" else "Set Reminder") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "কাস্টমার: ${customer.name}", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = reminderNote,
                        onValueChange = { reminderNote = it },
                        label = { Text(if (isBangla) "মন্তব্য" else "Note") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1, 3, 7, 15).forEach { d ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (reminderDays == d) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { reminderDays = d }
                            ) {
                                Text(
                                    text = if (isBangla) "$d দিন পর" else "$d Days",
                                    fontSize = 12.sp,
                                    color = if (reminderDays == d) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val futureTime = System.currentTimeMillis() + (reminderDays * 86400000L)
                        onAddReminder(customer.id, futureTime, reminderNote.trim())
                        showReminderDialog = false
                    }
                ) {
                    Text(if (isBangla) "সেট করুন" else "Set Reminder")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReminderDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun CustomerActionTile(
    icon: ImageVector,
    label: String,
    iconTint: Color,
    containerColor: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag(testTag)
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(containerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

