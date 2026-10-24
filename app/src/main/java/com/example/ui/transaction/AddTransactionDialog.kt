package com.example.ui.transaction

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.entities.CustomerEntity
import com.example.domain.calculator.CalculatorEngine
import com.example.domain.model.AppLanguage
import com.example.domain.model.TransactionType
import com.example.domain.utils.BanglaUtils
import com.example.ui.theme.CreditGreen
import com.example.ui.theme.CreditGreenContainer
import com.example.ui.theme.PaymentRed
import com.example.ui.theme.PaymentRedContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    initialType: TransactionType = TransactionType.CREDIT,
    initialCustomerId: Long? = null,
    customers: List<CustomerEntity>,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (
        customerId: Long,
        type: TransactionType,
        amount: Long,
        quantity: Double?,
        unitPrice: Long?,
        discount: Long?,
        note: String,
        category: String
    ) -> Unit
) {
    var selectedType by remember { mutableStateOf(initialType) }
    var selectedCustomerId by remember {
        mutableStateOf(initialCustomerId ?: customers.firstOrNull()?.id ?: 0L)
    }
    var amountInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }
    var selectedCategory by remember {
        mutableStateOf(if (initialType == TransactionType.CREDIT) "বাকি বিক্রি" else "নগদ জমা")
    }

    // Smart Calculator State
    var showCalculator by remember { mutableStateOf(false) }
    var calcModeItemized by remember { mutableStateOf(false) }
    var calcQtyInput by remember { mutableStateOf("1") }
    var calcPriceInput by remember { mutableStateOf("") }
    var calcDiscountInput by remember { mutableStateOf("0") }
    var calcExprInput by remember { mutableStateOf("") }

    // Customer Dropdown State
    var customerDropdownExpanded by remember { mutableStateOf(false) }
    val selectedCustomer = customers.find { it.id == selectedCustomerId }

    val isBangla = language == AppLanguage.BANGLA
    val categories = if (selectedType == TransactionType.CREDIT) {
        listOf("বাকি বিক্রি", "মালামাল", "কিস্তি পাওনা", "পূর্বের বাকি", "অন্যান্য")
    } else {
        listOf("নগদ জমা", "বিকাশ/নগদ", "ব্যাংক চেক", "পরিশোধ", "অন্যান্য")
    }

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
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBangla) "নতুন লেনদেন" else "New Transaction",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Type Toggle: 🟢 পাওনা / 🔴 জমা
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    // Credit Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedType == TransactionType.CREDIT) CreditGreen else Color.Transparent)
                            .clickable {
                                selectedType = TransactionType.CREDIT
                                selectedCategory = "বাকি বিক্রি"
                            }
                            .padding(vertical = 10.dp)
                            .testTag("type_credit_toggle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isBangla) "🟢 পাওনা (Credit)" else "🟢 Credit",
                            color = if (selectedType == TransactionType.CREDIT) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    // Payment Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedType == TransactionType.PAYMENT) PaymentRed else Color.Transparent)
                            .clickable {
                                selectedType = TransactionType.PAYMENT
                                selectedCategory = "নগদ জমা"
                            }
                            .padding(vertical = 10.dp)
                            .testTag("type_payment_toggle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isBangla) "🔴 জমা (Payment)" else "🔴 Payment",
                            color = if (selectedType == TransactionType.PAYMENT) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Customer Selection
                if (initialCustomerId == null) {
                    Text(
                        text = if (isBangla) "কাস্টমার নির্বাচন করুন *" else "Select Customer *",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    ExposedDropdownMenuBox(
                        expanded = customerDropdownExpanded,
                        onExpandedChange = { customerDropdownExpanded = !customerDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedCustomer?.name ?: if (isBangla) "কাস্টমার খুঁজুন" else "Select Customer",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("select_customer_dropdown"),
                            shape = RoundedCornerShape(12.dp)
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
                                            if (cust.phone.isNotEmpty()) {
                                                Text(cust.phone, fontSize = 12.sp, color = Color.Gray)
                                            }
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
                    Spacer(modifier = Modifier.height(14.dp))
                } else {
                    // Preselected customer badge
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBangla) "কাস্টমার: " else "Customer: ",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = selectedCustomer?.name ?: "",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Amount Field with Calculator icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isBangla) "টাকার পরিমাণ (৳) *" else "Amount (৳) *",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Calculator toggle button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                            .clickable { showCalculator = !showCalculator }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Calculate,
                            contentDescription = "Calculator",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showCalculator) (if (isBangla) "ক্যালকুলেটর বন্ধ" else "Hide Calc") else (if (isBangla) "ক্যালকুলেটর" else "Calculator"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    placeholder = { Text("0", fontSize = 20.sp) },
                    prefix = { Text("৳ ", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_amount_input"),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                // SMART CALCULATOR EXPANDED PANEL
                if (showCalculator) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Calc Mode Tabs
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = if (isBangla) "হিসাব (Expression)" else "Expression",
                                    fontSize = 12.sp,
                                    fontWeight = if (!calcModeItemized) FontWeight.Bold else FontWeight.Normal,
                                    color = if (!calcModeItemized) MaterialTheme.colorScheme.primary else Color.Gray,
                                    modifier = Modifier
                                        .clickable { calcModeItemized = false }
                                        .padding(4.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = if (isBangla) "পরিমাণ × দর (Qty × Price)" else "Qty × Price",
                                    fontSize = 12.sp,
                                    fontWeight = if (calcModeItemized) FontWeight.Bold else FontWeight.Normal,
                                    color = if (calcModeItemized) MaterialTheme.colorScheme.primary else Color.Gray,
                                    modifier = Modifier
                                        .clickable { calcModeItemized = true }
                                        .padding(4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (!calcModeItemized) {
                                // Expression Calc (e.g. 5 * 250 + 100)
                                OutlinedTextField(
                                    value = calcExprInput,
                                    onValueChange = {
                                        calcExprInput = it
                                        val result = CalculatorEngine.evaluateExpression(it)
                                        if (result != null) {
                                            amountInput = result.toString()
                                        }
                                    },
                                    placeholder = { Text(if (isBangla) "যেমন: ৫ * ২৫০ + ৫০০" else "e.g. 5 * 250 + 500") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )
                            } else {
                                // Itemized Qty x Price - Discount
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(
                                        value = calcQtyInput,
                                        onValueChange = {
                                            calcQtyInput = it
                                            updateItemizedTotal(calcQtyInput, calcPriceInput, calcDiscountInput) { tot ->
                                                amountInput = tot.toString()
                                            }
                                        },
                                        label = { Text(if (isBangla) "পরিমাণ" else "Qty", fontSize = 11.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    OutlinedTextField(
                                        value = calcPriceInput,
                                        onValueChange = {
                                            calcPriceInput = it
                                            updateItemizedTotal(calcQtyInput, calcPriceInput, calcDiscountInput) { tot ->
                                                amountInput = tot.toString()
                                            }
                                        },
                                        label = { Text(if (isBangla) "দর (৳)" else "Price", fontSize = 11.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    OutlinedTextField(
                                        value = calcDiscountInput,
                                        onValueChange = {
                                            calcDiscountInput = it
                                            updateItemizedTotal(calcQtyInput, calcPriceInput, calcDiscountInput) { tot ->
                                                amountInput = tot.toString()
                                            }
                                        },
                                        label = { Text(if (isBangla) "ছাড় (৳)" else "Disc", fontSize = 11.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Categories Chips
                Text(
                    text = if (isBangla) "ক্যাটাগরি" else "Category",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Note / Description Field
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text(if (isBangla) "বিবরণ বা পণ্যের নাম (ঐচ্ছিক)" else "Note / Description (Optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_note_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons: Cancel and Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (isBangla) "বাতিল" else "Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    val isValid = selectedCustomerId != 0L && (amountInput.toLongOrNull() ?: 0L) > 0L
                    Button(
                        onClick = {
                            val parsedAmount = BanglaUtils.toEnglishDigits(amountInput).trim().toLongOrNull() ?: 0L
                            if (parsedAmount > 0L && selectedCustomerId != 0L) {
                                val qty = calcQtyInput.toDoubleOrNull()
                                val unitPrice = calcPriceInput.toLongOrNull()
                                val discount = calcDiscountInput.toLongOrNull()

                                onSave(
                                    selectedCustomerId,
                                    selectedType,
                                    parsedAmount,
                                    if (showCalculator && calcModeItemized) qty else null,
                                    if (showCalculator && calcModeItemized) unitPrice else null,
                                    if (showCalculator && calcModeItemized) discount else null,
                                    noteInput.trim(),
                                    selectedCategory
                                )
                            }
                        },
                        enabled = isValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == TransactionType.CREDIT) CreditGreen else PaymentRed
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_transaction_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBangla) "সংরক্ষণ করুন" else "Save Transaction",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun updateItemizedTotal(qtyStr: String, priceStr: String, discStr: String, onResult: (Long) -> Unit) {
    val q = qtyStr.toDoubleOrNull() ?: 0.0
    val p = priceStr.toLongOrNull() ?: 0L
    val d = discStr.toLongOrNull() ?: 0L
    val tot = CalculatorEngine.calculateItemTotal(q, p, d)
    if (tot > 0L) {
        onResult(tot)
    }
}
