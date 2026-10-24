package com.example.ui.trash

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.CustomerEntity
import com.example.data.entities.TransactionEntity
import com.example.domain.model.AppLanguage
import com.example.domain.model.TransactionType
import com.example.domain.utils.BanglaUtils
import com.example.ui.common.EmptyStateView
import com.example.ui.theme.CreditGreen
import com.example.ui.theme.PaymentRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    trashCustomers: List<CustomerEntity>,
    trashTransactions: List<TransactionEntity>,
    language: AppLanguage,
    onBackClick: () -> Unit,
    onRestoreCustomer: (Long) -> Unit,
    onPermanentDeleteCustomer: (Long) -> Unit,
    onRestoreTransaction: (Long) -> Unit,
    onPermanentDeleteTransaction: (Long) -> Unit,
    onEmptyTrash: () -> Unit
) {
    val isBangla = language == AppLanguage.BANGLA
    var selectedTab by remember { mutableStateOf(0) }
    var showEmptyConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isBangla) "রিসাইকেল বিন (Trash)" else "Recycle Bin", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (trashCustomers.isNotEmpty() || trashTransactions.isNotEmpty()) {
                        IconButton(onClick = { showEmptyConfirm = true }) {
                            Icon(Icons.Default.DeleteForever, contentDescription = "Empty Trash", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            if (isBangla) "কাস্টমার (${BanglaUtils.toBanglaDigits(trashCustomers.size.toLong())})"
                            else "Customers (${trashCustomers.size})"
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            if (isBangla) "লেনদেন (${BanglaUtils.toBanglaDigits(trashTransactions.size.toLong())})"
                            else "Transactions (${trashTransactions.size})"
                        )
                    }
                )
            }

            if (selectedTab == 0) {
                // Customers Tab
                if (trashCustomers.isEmpty()) {
                    EmptyStateView(
                        title = if (isBangla) "রিসাইকেল বিনে কোনো কাস্টমার নেই" else "No deleted customers",
                        subtitle = if (isBangla) "মুছে ফেলা কাস্টমার এখানে জমা হবে" else "Deleted customers will appear here"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(trashCustomers) { customer ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = customer.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (customer.phone.isNotEmpty()) {
                                            Text(
                                                text = customer.phone,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = "বাকি: ৳${customer.currentBalance}",
                                            fontSize = 12.sp,
                                            color = if (customer.currentBalance > 0) CreditGreen else PaymentRed
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedButton(
                                            onClick = { onRestoreCustomer(customer.id) },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("restore_customer_${customer.id}")
                                        ) {
                                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isBangla) "পুনরুদ্ধার" else "Restore", fontSize = 11.sp)
                                        }

                                        IconButton(onClick = { onPermanentDeleteCustomer(customer.id) }) {
                                            Icon(Icons.Default.DeleteForever, contentDescription = "Delete Forever", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Transactions Tab
                if (trashTransactions.isEmpty()) {
                    EmptyStateView(
                        title = if (isBangla) "রিসাইকেল বিনে কোনো লেনদেন নেই" else "No deleted transactions",
                        subtitle = if (isBangla) "মুছে ফেলা লেনদেন এখানে জমা হবে" else "Deleted transactions will appear here"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(trashTransactions) { txn ->
                            val isCredit = txn.type == TransactionType.CREDIT.name
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isCredit) (if (isBangla) "পাওনা: ৳${txn.amount}" else "Credit: ৳${txn.amount}") else (if (isBangla) "জমা: ৳${txn.amount}" else "Payment: ৳${txn.amount}"),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCredit) CreditGreen else PaymentRed
                                        )
                                        Text(
                                            text = "${BanglaUtils.formatDate(txn.date, isBangla)} ${if (txn.note.isNotEmpty()) "• ${txn.note}" else ""}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedButton(
                                            onClick = { onRestoreTransaction(txn.id) },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("restore_txn_${txn.id}")
                                        ) {
                                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isBangla) "পুনরুদ্ধার" else "Restore", fontSize = 11.sp)
                                        }

                                        IconButton(onClick = { onPermanentDeleteTransaction(txn.id) }) {
                                            Icon(Icons.Default.DeleteForever, contentDescription = "Delete Forever", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Empty Trash confirmation
    if (showEmptyConfirm) {
        AlertDialog(
            onDismissRequest = { showEmptyConfirm = false },
            title = { Text(if (isBangla) "রিসাইকেল বিন খালি করবেন?" else "Empty Recycle Bin?") },
            text = {
                Text(
                    if (isBangla)
                        "রিসাইকেল বিনের সমস্ত কাস্টমার এবং লেনদেন চিরতরে মুছে ফেলা হবে। এটি আর ফিরিয়ে আনা যাবে না।"
                    else
                        "All deleted items will be permanently erased. This cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onEmptyTrash()
                        showEmptyConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isBangla) "চিরতরে মুছুন" else "Empty Trash")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyConfirm = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }
}
