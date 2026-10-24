package com.example.ui.receipt

import android.content.Intent
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.model.AppLanguage
import com.example.domain.model.ReceiptData
import com.example.domain.model.TransactionType
import com.example.domain.pdf.ReceiptPdfGenerator
import com.example.domain.utils.BanglaUtils
import com.example.ui.theme.CreditGreen
import com.example.ui.theme.PaymentRed

@Composable
fun ReceiptDialog(
    receipt: ReceiptData,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isBangla = language == AppLanguage.BANGLA
    val isCredit = receipt.transactionType == TransactionType.CREDIT

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBangla) "ডিজিটাল রসিদ" else "Digital Receipt",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Receipt Slip Card Container
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Crimson Header
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(vertical = 16.dp, horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = receipt.businessName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "টালি খাতা রসিদ (${receipt.khataName})",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        // Meta details
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "রসিদ নং: ${receipt.receiptNo}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "${receipt.dateString}, ${receipt.timeString}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFFF3F4F6))
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "কাস্টমার: ${receipt.customerName}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                            if (receipt.customerPhone.isNotEmpty()) {
                                Text(
                                    text = "মোবাইল: ${receipt.customerPhone}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF4B5563)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Breakdown Table Card
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF9FAFB),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    // Row 1: Previous Balance
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "পূর্বের বাকি:", fontSize = 13.sp, color = Color(0xFF4B5563))
                                        Text(
                                            text = BanglaUtils.formatTaka(receipt.previousBalance, isBangla = isBangla),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF111827)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Row 2: Transaction Amount
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        val typeText = if (isCredit) "পাওনা যোগ (বাকি):" else "জমা গ্রহণ (পরিশোধ):"
                                        Text(text = typeText, fontSize = 13.sp, color = Color(0xFF4B5563))
                                        Text(
                                            text = BanglaUtils.formatTaka(receipt.transactionAmount, isBangla = isBangla),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCredit) CreditGreen else PaymentRed
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = Color(0xFFE5E7EB))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Row 3: Current Balance
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "বর্তমান বাকি / জের:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                                        Text(
                                            text = BanglaUtils.formatTaka(receipt.currentBalance, isBangla = isBangla),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (receipt.currentBalance > 0) CreditGreen else Color(0xFF111827)
                                        )
                                    }

                                    if (receipt.note.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "বিবরণ: ${receipt.note}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF6B7280)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "ধন্যবাদ! আবার আসবেন।",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // PDF Share
                    Button(
                        onClick = {
                            val pdfFile = ReceiptPdfGenerator.generateReceiptPdf(context, receipt)
                            if (pdfFile != null) {
                                ReceiptPdfGenerator.shareReceiptFile(context, pdfFile, receipt.customerName)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_pdf_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isBangla) "PDF রসিদ" else "PDF Slip", fontSize = 13.sp)
                    }

                    // SMS Share
                    OutlinedButton(
                        onClick = {
                            val smsText = "প্রিয় ${receipt.customerName}, আপনার ${receipt.businessName}-এ ${if (isCredit) "বাকি যোগ" else "জমা"} হয়েছে ৳${receipt.transactionAmount}। বর্তমান বাকি: ৳${receipt.currentBalance}। ধন্যবাদ।"
                            val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("smsto:${receipt.customerPhone}")
                                putExtra("sms_body", smsText)
                            }
                            try {
                                context.startActivity(smsIntent)
                            } catch (e: Exception) {
                                // Fallback general share
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, smsText)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "শেয়ার করুন"))
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sms_share_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isBangla) "এসএমএস" else "SMS", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
