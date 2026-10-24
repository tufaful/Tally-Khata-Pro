package com.example.ui.transaction

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.entities.CustomerEntity
import com.example.data.entities.TransactionEntity
import com.example.domain.model.AppLanguage
import com.example.domain.model.TransactionType
import com.example.domain.utils.BanglaUtils
import com.example.ui.theme.CreditGreen
import com.example.ui.theme.PaymentRed
import com.example.ui.theme.SleekTeal

@Composable
fun TransactionOptionsDialog(
    transaction: TransactionEntity,
    customer: CustomerEntity?,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onViewReceiptClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isBangla = language == AppLanguage.BANGLA
    val isCredit = transaction.type == TransactionType.CREDIT.name
    val primaryColor = if (isCredit) CreditGreen else PaymentRed
    val badgeBg = if (isCredit) Color(0xFFE0F2F1) else Color(0xFFFFEBEE)
    val badgeText = if (isCredit) {
        if (isBangla) "পাওনা (বাকি বিক্রি)" else "CREDIT"
    } else {
        if (isBangla) "জমা (নগদ গ্রহণ)" else "PAYMENT"
    }
    val customerName = customer?.name ?: if (isBangla) "কাস্টমার" else "Customer"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("transaction_options_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBangla) "লেনদেন বিবরণ ও অপশন" else "Transaction Options",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "ID: #${transaction.id} • ${BanglaUtils.formatDate(transaction.date, isBangla = isBangla)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_options_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = customerName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (customer?.phone?.isNotEmpty() == true) {
                                    Text(
                                        text = customer.phone,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = badgeBg
                            ) {
                                Text(
                                    text = badgeText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = if (isBangla) "লেনদেনের পরিমাণ" else "Amount",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${if (isCredit) "+ " else "- "}${BanglaUtils.formatTaka(transaction.amount, isBangla = isBangla, showSymbol = true)}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = primaryColor
                                )
                            }

                            if (transaction.category.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Text(
                                        text = transaction.category,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        if (transaction.note.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "বিবরণ: ${transaction.note}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Options
                Text(
                    text = if (isBangla) "করণীয় নির্বাচন করুন" else "Select Action",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Option 1: ✏️ Edit
                OptionActionRow(
                    icon = Icons.Default.Edit,
                    iconTint = SleekTeal,
                    title = if (isBangla) "✏️ লেনদেন তথ্য পরিবর্তন (Edit)" else "✏️ Edit Transaction",
                    subtitle = if (isBangla) "ধরন, পরিমাণ, তারিখ, দর ও বিবরণ পরিবর্তন করুন" else "Edit amount, type, date, note, etc.",
                    testTag = "btn_transaction_option_edit",
                    onClick = {
                        onDismiss()
                        onEditClick()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Option 2: 📄 View Receipt
                OptionActionRow(
                    icon = Icons.Default.ReceiptLong,
                    iconTint = Color(0xFF1E88E5),
                    title = if (isBangla) "📄 রসিদ দেখুন / শেয়ার (Receipt)" else "📄 View / Share Receipt",
                    subtitle = if (isBangla) "ডিজিটাল রসিদ দেখুন, PDF প্রিন্ট ও শেয়ার করুন" else "View digital receipt, download PDF, share",
                    testTag = "btn_transaction_option_receipt",
                    onClick = {
                        onDismiss()
                        onViewReceiptClick()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Option 3: 🗑️ Delete
                OptionActionRow(
                    icon = Icons.Default.DeleteOutline,
                    iconTint = PaymentRed,
                    title = if (isBangla) "🗑️ লেনদেন মুছে ফেলুন (Delete)" else "🗑️ Delete Transaction",
                    subtitle = if (isBangla) "রিসাইকেল বিনে স্থানান্তর হবে এবং ব্যালেন্স সমন্বয় হবে" else "Move to Recycle Bin and adjust customer balance",
                    testTag = "btn_transaction_option_delete",
                    onClick = {
                        onDismiss()
                        onDeleteClick()
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_close_transaction_options"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        }
    }
}

@Composable
private fun OptionActionRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
