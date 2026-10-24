package com.example.ui.reminders

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.entities.ReminderEntity
import com.example.domain.model.AppLanguage
import com.example.domain.utils.BanglaUtils
import com.example.ui.common.AmountText
import com.example.ui.common.EmptyStateView
import com.example.ui.common.TallyHeader
import com.example.ui.theme.AmberLight
import com.example.ui.theme.CreditGreen
import com.example.ui.theme.SleekTeal

@Composable
fun RemindersScreen(
    khataName: String,
    businessName: String,
    reminders: List<ReminderEntity>,
    customers: List<CustomerEntity>,
    language: AppLanguage,
    privacyMode: Boolean,
    onKhataClick: () -> Unit,
    onTogglePrivacy: () -> Unit,
    onToggleLanguage: () -> Unit,
    onMarkCompleted: (Long) -> Unit,
    onDeleteReminder: (ReminderEntity) -> Unit
) {
    val context = LocalContext.current
    val isBangla = language == AppLanguage.BANGLA
    val customerMap = customers.associateBy { it.id }

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBangla) "বকেয়া তাগাদার তালিকা (${BanglaUtils.toBanglaDigits(reminders.size.toLong())})" else "Reminders (${reminders.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (reminders.isEmpty()) {
                item {
                    EmptyStateView(
                        title = if (isBangla) "কোনো তাগাদা রিমাইন্ডার নেই" else "No pending reminders",
                        subtitle = if (isBangla) "কাস্টমার প্রোফাইল থেকে সহজেই তাগাদার সময় নির্ধারণ করতে পারেন" else "You can set payment reminders directly from customer profile"
                    )
                }
            } else {
                items(reminders) { reminder ->
                    val customer = customerMap[reminder.customerId]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reminder_item_${reminder.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFEF3C7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Alarm, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = customer?.name ?: if (isBangla) "কাস্টমার" else "Customer",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "তারিখ: ${BanglaUtils.formatDate(reminder.dateTime, isBangla)}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (customer != null) {
                                    AmountText(
                                        amount = customer.currentBalance,
                                        isPrivacyMode = privacyMode,
                                        isBangla = isBangla,
                                        fontSize = 16,
                                        color = Color(0xFF00796B)
                                    )
                                }
                            }

                            if (reminder.note.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "নোট: ${reminder.note}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Contact Actions Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (customer != null && customer.phone.isNotEmpty()) {
                                    OutlinedButton(
                                        onClick = {
                                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))
                                            context.startActivity(dialIntent)
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = MaterialTheme.colorScheme.surface,
                                            contentColor = MaterialTheme.colorScheme.onSurface
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp), tint = SleekTeal)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isBangla) "কল" else "Call",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val smsText = "প্রিয় ${customer.name}, ${businessName}-এ আপনার বকেয়া ৳${customer.currentBalance} পরিশোধের তাগাদা। ধন্যবাদ।"
                                            val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${customer.phone}")).apply {
                                                putExtra("sms_body", smsText)
                                            }
                                            context.startActivity(smsIntent)
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = MaterialTheme.colorScheme.surface,
                                            contentColor = MaterialTheme.colorScheme.onSurface
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(14.dp), tint = SleekTeal)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isBangla) "এসএমএস" else "SMS",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { onMarkCompleted(reminder.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Done", tint = Color(0xFF00796B))
                                }

                                IconButton(
                                    onClick = { onDeleteReminder(reminder) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFF94A3B8))
                                }
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
