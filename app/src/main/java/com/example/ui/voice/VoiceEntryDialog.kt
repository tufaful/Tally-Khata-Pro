package com.example.ui.voice

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.entities.CustomerEntity
import com.example.domain.model.AppLanguage
import com.example.domain.model.TransactionType
import com.example.domain.parser.ParsedVoiceTransaction
import com.example.domain.parser.VoiceTransactionParser
import com.example.domain.utils.BanglaUtils
import com.example.ui.theme.CreditGreen
import com.example.ui.theme.PaymentRed
import java.util.Locale

@Composable
fun VoiceEntryDialog(
    customers: List<CustomerEntity>,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSaveTransaction: (customerId: Long, type: TransactionType, amount: Long, note: String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var parsedResult by remember { mutableStateOf<ParsedVoiceTransaction?>(null) }
    var showConfirmation by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val isBangla = language == AppLanguage.BANGLA

    // Speech Recognizer launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                inputText = spoken
                val parsed = VoiceTransactionParser.parse(spoken, customers)
                parsedResult = parsed
                showConfirmation = true
            }
        }
    }

    val samplePhrases = listOf(
        "রহিমকে পাঁচ হাজার টাকা দিলাম",
        "করিম ৫০০ টাকা জমা দিল",
        "সুমন বাকিতে ২০০০ টাকার মাল নিল"
    )

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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBangla) "ভয়েস লেনদেন এন্ট্রি" else "Voice Entry",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!showConfirmation) {
                    // Big Voice Mic Button
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable {
                                try {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(
                                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                        )
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, "কথা বলুন: যেমন 'রহিম ৫০০ টাকা জমা দিল'")
                                    }
                                    speechLauncher.launch(intent)
                                } catch (e: Exception) {
                                    // Fallback to text input
                                }
                            }
                            .testTag("voice_mic_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Tap to speak",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isBangla) "মাইক্রোফোনে ট্যাপ করে কথা বলুন" else "Tap microphone to speak",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isBangla) "অথবা নিচের উদাহরণে ক্লিক করুন বা লিখুন" else "Or type/select a sentence below",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Text input alternative
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text(if (isBangla) "বাংলায় বলুন বা লিখুন..." else "Say or type sentence...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Sample Chips
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isBangla) "উদাহরণ বাক্যসমূহ:" else "Sample Sentences:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        samplePhrases.forEach { phrase ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        inputText = phrase
                                        val parsed = VoiceTransactionParser.parse(phrase, customers)
                                        parsedResult = parsed
                                        showConfirmation = true
                                    }
                            ) {
                                Text(
                                    text = "“$phrase”",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                val parsed = VoiceTransactionParser.parse(inputText, customers)
                                parsedResult = parsed
                                showConfirmation = true
                            }
                        },
                        enabled = inputText.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isBangla) "লেনদেন শনাক্ত করুন" else "Process Voice")
                    }
                } else {
                    // CONFIRMATION SCREEN (Requirement: Never save automatically!)
                    val parsed = parsedResult
                    if (parsed != null) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = if (isBangla) "এই লেনদেনটি সংরক্ষণ করবেন?" else "Save this transaction?",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "“${parsed.rawText}”",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Parsed Breakdown
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = if (isBangla) "কাস্টমার:" else "Customer:", fontSize = 13.sp)
                                    Text(
                                        text = parsed.detectedCustomerName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = if (isBangla) "ধরন:" else "Type:", fontSize = 13.sp)
                                    val isCredit = parsed.type == TransactionType.CREDIT
                                    Text(
                                        text = if (isCredit) (if (isBangla) "🟢 পাওনা (Credit)" else "Credit") else (if (isBangla) "🔴 জমা (Payment)" else "Payment"),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCredit) CreditGreen else PaymentRed
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = if (isBangla) "পরিমাণ:" else "Amount:", fontSize = 13.sp)
                                    Text(
                                        text = BanglaUtils.formatTaka(parsed.amount, isBangla = isBangla),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = parsed.confidenceNotes,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Confirmation Buttons: সংরক্ষণ (Save) / বাতিল (Cancel)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    showConfirmation = false
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(if (isBangla) "বাতিল" else "Cancel")
                            }

                            Button(
                                onClick = {
                                    val customerId = parsed.matchedCustomer?.id ?: customers.firstOrNull()?.id ?: 1L
                                    onSaveTransaction(
                                        customerId,
                                        parsed.type,
                                        parsed.amount,
                                        "ভয়েস এন্ট্রি: ${parsed.rawText}"
                                    )
                                },
                                modifier = Modifier
                                    .weight(1.3f)
                                    .testTag("confirm_voice_save_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (parsed.type == TransactionType.CREDIT) CreditGreen else PaymentRed
                                )
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isBangla) "সংরক্ষণ করুন" else "Save",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
