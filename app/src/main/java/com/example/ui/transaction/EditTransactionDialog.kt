package com.example.ui.transaction

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.example.ui.theme.CreditGreenContainer
import com.example.ui.theme.PaymentRed
import com.example.ui.theme.PaymentRedContainer
import com.example.ui.theme.SleekTeal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionDialog(
    transaction: TransactionEntity,
    customers: List<CustomerEntity>,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        type: TransactionType,
        amount: Long,
        quantity: Double?,
        unitPrice: Long?,
        discount: Long?,
        note: String,
        category: String,
        attachmentUri: String?,
        date: Long,
        newCustomerId: Long?
    ) -> Unit
) {
    val context = LocalContext.current
    val isBangla = language == AppLanguage.BANGLA

    var selectedType by remember {
        mutableStateOf(TransactionType.fromString(transaction.type))
    }
    var amountInput by remember {
        mutableStateOf(transaction.amount.toString())
    }
    var qtyInput by remember {
        mutableStateOf(transaction.quantity?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() } ?: "")
    }
    var unitPriceInput by remember {
        mutableStateOf(transaction.unitPrice?.toString() ?: "")
    }
    var discountInput by remember {
        mutableStateOf(transaction.discount?.toString() ?: "")
    }
    var noteInput by remember {
        mutableStateOf(transaction.note)
    }
    var selectedCategory by remember {
        mutableStateOf(transaction.category.ifEmpty {
            if (transaction.type == TransactionType.CREDIT.name) "বাকি বিক্রি" else "নগদ জমা"
        })
    }
    var attachmentUriStr by remember {
        mutableStateOf(transaction.attachmentUri)
    }

    // Date & Time state
    val calendar = remember {
        Calendar.getInstance().apply { timeInMillis = transaction.date }
    }
    var currentTimestamp by remember { mutableStateOf(transaction.date) }

    // Customer association state
    val initialCustomer = customers.find { it.id == transaction.customerId }
    var allowCustomerChange by remember { mutableStateOf(false) }
    var selectedCustomerId by remember { mutableStateOf(transaction.customerId) }
    var customerDropdownExpanded by remember { mutableStateOf(false) }

    // Safety Confirmation Dialog state
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            attachmentUriStr = uri.toString()
        }
    }

    val creditCategories = listOf("বাকি বিক্রি", "মালামাল", "কিস্তি পাওনা", "পূর্বের বাকি", "অন্যান্য")
    val paymentCategories = listOf("নগদ জমা", "বিকাশ/নগদ", "ব্যাংক চেক", "পরিশোধ", "অন্যান্য")
    val activeCategories = if (selectedType == TransactionType.CREDIT) creditCategories else paymentCategories

    val parsedAmount = amountInput.toLongOrNull() ?: 0L
    val isValidAmount = parsedAmount > 0L

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 20.dp)
                .testTag("edit_transaction_dialog"),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Dialog Title Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBangla) "লেনদেন এডিট করুন" else "Edit Transaction",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Original Transaction ID: #${transaction.id}",
                            fontSize = 11.sp,
                            color = SleekTeal,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_edit_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Safe Customer Association
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = SleekTeal,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBangla) "কাস্টমার হিসাব (সংরক্ষিত)" else "Customer Account (Locked)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val activeCustomer = customers.find { it.id == selectedCustomerId } ?: initialCustomer
                        Text(
                            text = activeCustomer?.name ?: "ID: $selectedCustomerId",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (activeCustomer?.phone?.isNotEmpty() == true) {
                            Text(
                                text = activeCustomer.phone,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { allowCustomerChange = !allowCustomerChange }
                        ) {
                            Checkbox(
                                checked = allowCustomerChange,
                                onCheckedChange = { allowCustomerChange = it },
                                modifier = Modifier.size(28.dp).testTag("checkbox_allow_customer_change")
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isBangla) "কাস্টমার পরিবর্তন করতে চান? (ভুল এন্ট্রির ক্ষেত্রে)" else "Change customer (for fixing entry errors)?",
                                fontSize = 11.sp,
                                color = if (allowCustomerChange) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (allowCustomerChange) {
                            Spacer(modifier = Modifier.height(8.dp))
                            ExposedDropdownMenuBox(
                                expanded = customerDropdownExpanded,
                                onExpandedChange = { customerDropdownExpanded = !customerDropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = activeCustomer?.name ?: "",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(if (isBangla) "অন্য কাস্টমার নির্বাচন করুন" else "Select New Customer") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                        .testTag("dropdown_edit_customer"),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = customerDropdownExpanded,
                                    onDismissRequest = { customerDropdownExpanded = false }
                                ) {
                                    customers.forEach { cust ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(cust.name, fontWeight = FontWeight.SemiBold)
                                                    Text(cust.phone, fontSize = 11.sp, color = Color.Gray)
                                                }
                                            },
                                            onClick = {
                                                selectedCustomerId = cust.id
                                                customerDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Transaction Type Switcher
                Text(
                    text = if (isBangla) "লেনদেনের ধরন" else "Transaction Type",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Credit Button
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedType = TransactionType.CREDIT
                                if (selectedCategory in paymentCategories) selectedCategory = "বাকি বিক্রি"
                            }
                            .testTag("edit_type_credit"),
                        color = if (selectedType == TransactionType.CREDIT) CreditGreenContainer else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (selectedType == TransactionType.CREDIT) BorderStroke(2.dp, CreditGreen) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (selectedType == TransactionType.CREDIT) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = CreditGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = if (isBangla) "+ পাওনা (Credit)" else "+ Credit",
                                fontWeight = FontWeight.Bold,
                                color = if (selectedType == TransactionType.CREDIT) CreditGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Payment Button
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedType = TransactionType.PAYMENT
                                if (selectedCategory in creditCategories) selectedCategory = "নগদ জমা"
                            }
                            .testTag("edit_type_payment"),
                        color = if (selectedType == TransactionType.PAYMENT) PaymentRedContainer else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (selectedType == TransactionType.PAYMENT) BorderStroke(2.dp, PaymentRed) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (selectedType == TransactionType.PAYMENT) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = PaymentRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = if (isBangla) "− জমা (Payment)" else "− Payment",
                                fontWeight = FontWeight.Bold,
                                color = if (selectedType == TransactionType.PAYMENT) PaymentRed else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Amount Field
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        amountInput = digits
                    },
                    label = { Text(if (isBangla) "টাকার পরিমাণ *" else "Amount *") },
                    leadingIcon = {
                        Text(
                            text = "৳",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedType == TransactionType.CREDIT) CreditGreen else PaymentRed,
                            modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_amount_input"),
                    singleLine = true
                )

                if (parsedAmount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "= " + BanglaUtils.formatTaka(parsedAmount, isBangla = isBangla, showSymbol = true) + if (isBangla) " টাকা" else "",
                        fontSize = 12.sp,
                        color = if (selectedType == TransactionType.CREDIT) CreditGreen else PaymentRed,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Itemized Calculation Fields (Quantity, Unit Price, Discount)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (isBangla) "পণ্য / হিসাবের বিস্তারিত (ঐচ্ছিক)" else "Itemized Details (Optional)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = qtyInput,
                                onValueChange = { qtyInput = it },
                                label = { Text(if (isBangla) "পরিমাণ" else "Qty", fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("edit_quantity_input"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = unitPriceInput,
                                onValueChange = { unitPriceInput = it.filter { ch -> ch.isDigit() } },
                                label = { Text(if (isBangla) "একক দর" else "Price", fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1.2f).testTag("edit_unit_price_input"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = discountInput,
                                onValueChange = { discountInput = it.filter { ch -> ch.isDigit() } },
                                label = { Text(if (isBangla) "ছাড়" else "Discount", fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).testTag("edit_discount_input"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                        }

                        // Auto-calculate button
                        val qVal = qtyInput.toDoubleOrNull()
                        val pVal = unitPriceInput.toLongOrNull()
                        val dVal = discountInput.toLongOrNull() ?: 0L

                        if (qVal != null && pVal != null) {
                            val calculatedTotal = ((qVal * pVal) - dVal).toLong().coerceAtLeast(0L)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${if (isBangla) "হিসাবকৃত মোট:" else "Calculated:"} ৳$calculatedTotal",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekTeal
                                )
                                TextButton(
                                    onClick = { amountInput = calculatedTotal.toString() },
                                    modifier = Modifier.testTag("btn_apply_calculated_amount")
                                ) {
                                    Text(
                                        text = if (isBangla) "মোট টাকায় বসান" else "Apply Total",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5. Date and Time Editing
                Text(
                    text = if (isBangla) "তারিখ ও সময়" else "Date & Time",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Date Button
                    OutlinedButton(
                        onClick = {
                            val dp = DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    calendar.set(Calendar.YEAR, year)
                                    calendar.set(Calendar.MONTH, month)
                                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                    currentTimestamp = calendar.timeInMillis
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            )
                            dp.show()
                        },
                        modifier = Modifier.weight(1f).testTag("edit_date_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = BanglaUtils.formatDate(currentTimestamp, isBangla = isBangla),
                            fontSize = 12.sp
                        )
                    }

                    // Time Button
                    OutlinedButton(
                        onClick = {
                            val tp = TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                    calendar.set(Calendar.MINUTE, minute)
                                    currentTimestamp = calendar.timeInMillis
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                false
                            )
                            tp.show()
                        },
                        modifier = Modifier.weight(0.9f).testTag("edit_time_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        val timeStr = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(currentTimestamp))
                        Text(
                            text = timeStr,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 6. Category Chips
                Text(
                    text = if (isBangla) "ক্যাটাগরি" else "Category",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activeCategories) { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { selectedCategory = cat }
                                .testTag("edit_cat_$cat")
                        ) {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 7. Note / Memo
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text(if (isBangla) "বিবরণ বা নোট" else "Note / Description") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_note_input"),
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 8. Attachment / Photo
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = SleekTeal)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (attachmentUriStr != null) (if (isBangla) "ছবি সংযুক্ত আছে" else "Photo Attached") else (if (isBangla) "রসিদ / পণ্যের ছবি" else "Receipt / Product Photo"),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (attachmentUriStr != null) (if (isBangla) "ছবি পরিবর্তন বা ডিলিট করতে পারেন" else "You can replace or remove photo") else (if (isBangla) "গ্যালারি থেকে ছবি যুক্ত করুন" else "Attach image from gallery"),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (attachmentUriStr != null) {
                                IconButton(
                                    onClick = { attachmentUriStr = null },
                                    modifier = Modifier.testTag("btn_remove_attachment")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove photo", tint = PaymentRed)
                                }
                            }
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("btn_pick_attachment")
                            ) {
                                Text(
                                    text = if (attachmentUriStr != null) (if (isBangla) "পরিবর্তন" else "Change") else (if (isBangla) "+ যুক্ত করুন" else "+ Add"),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save & Cancel Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).testTag("btn_cancel_edit"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isBangla) "বাতিল" else "Cancel")
                    }

                    Button(
                        onClick = {
                            if (isValidAmount) {
                                showConfirmDialog = true
                            }
                        },
                        enabled = isValidAmount,
                        modifier = Modifier.weight(1.4f).testTag("btn_save_edit"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SleekTeal)
                    ) {
                        Text(if (isBangla) "সংরক্ষণ করুন" else "Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Safety Confirmation Dialog as strictly required by prompt:
    // Prompt text: "আপনি কি এই লেনদেনের তথ্য পরিবর্তন করতে চান?"
    // Buttons: পরিবর্তন করুন | বাতিল
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = {
                Icon(
                    Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = if (isBangla) "নিশ্চিতকরণ" else "Confirm Edit",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isBangla) "আপনি কি এই লেনদেনের তথ্য পরিবর্তন করতে চান?" else "Are you sure you want to update this transaction?",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isBangla)
                            "সংশ্লিষ্ট কাস্টমারের হিসাবের জের (ব্যালেন্স) স্বয়ংক্রিয়ভাবে নির্ভুলভাবে পুনর্গণনা করা হবে।"
                        else
                            "The customer's balance will be automatically recalculated from the complete transaction history.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        val qParsed = qtyInput.toDoubleOrNull()
                        val uPriceParsed = unitPriceInput.toLongOrNull()
                        val discParsed = discountInput.toLongOrNull()
                        val finalCustId = if (allowCustomerChange && selectedCustomerId != transaction.customerId) selectedCustomerId else null

                        onSave(
                            transaction.id,
                            selectedType,
                            parsedAmount,
                            qParsed,
                            uPriceParsed,
                            discParsed,
                            noteInput.trim(),
                            selectedCategory,
                            attachmentUriStr,
                            currentTimestamp,
                            finalCustId
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekTeal),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_confirm_edit_transaction")
                ) {
                    Text(if (isBangla) "পরিবর্তন করুন" else "Confirm Change", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmDialog = false },
                    modifier = Modifier.testTag("btn_cancel_confirm_edit")
                ) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }
}
