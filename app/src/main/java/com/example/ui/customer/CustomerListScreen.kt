package com.example.ui.customer

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.CustomerEntity
import com.example.domain.model.AppLanguage
import com.example.domain.model.CustomerFilter
import com.example.domain.model.CustomerSort
import com.example.domain.utils.BanglaUtils
import com.example.ui.common.AmountText
import com.example.ui.common.EmptyStateView
import com.example.ui.common.TallyHeader
import com.example.ui.theme.CreditGreen
import com.example.ui.theme.CreditGreenContainer
import com.example.ui.theme.OnCreditGreenContainer
import com.example.ui.theme.OnPaymentRedContainer
import com.example.ui.theme.PaymentRed
import com.example.ui.theme.PaymentRedContainer
import com.example.ui.theme.SleekTeal

@Composable
fun CustomerListScreen(
    khataName: String,
    businessName: String,
    customers: List<CustomerEntity>,
    searchQuery: String,
    currentFilter: CustomerFilter,
    currentSort: CustomerSort,
    language: AppLanguage,
    privacyMode: Boolean,
    onKhataClick: () -> Unit,
    onTogglePrivacy: () -> Unit,
    onToggleLanguage: () -> Unit,
    onSearchChange: (String) -> Unit,
    onFilterChange: (CustomerFilter) -> Unit,
    onSortChange: (CustomerSort) -> Unit,
    onCustomerClick: (CustomerEntity) -> Unit,
    onAddCustomerClick: () -> Unit
) {
    val isBangla = language == AppLanguage.BANGLA
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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

            // Search Bar & Sort Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = {
                        Text(
                            if (isBangla) "নাম, মোবাইল বা প্রতিষ্ঠান খুঁজুন..." else "Search customer...",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF94A3B8))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF94A3B8))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = SleekTeal,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("customer_search_input")
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Sort Dropdown Button
                Box {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                            .clickable { sortMenuExpanded = true }
                            .testTag("customer_sort_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort",
                            tint = SleekTeal,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false }
                    ) {
                        CustomerSort.values().forEach { sort ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (isBangla) sort.banglaLabel else sort.englishLabel,
                                        fontWeight = if (sort == currentSort) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onSortChange(sort)
                                    sortMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Filter Chips Bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CustomerFilter.values()) { filter ->
                    val isSelected = filter == currentFilter
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) SleekTeal else MaterialTheme.colorScheme.surface,
                        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .clickable { onFilterChange(filter) }
                            .testTag("filter_chip_${filter.name}")
                    ) {
                        Text(
                            text = if (isBangla) filter.banglaLabel else filter.englishLabel,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Customer List
            if (customers.isEmpty()) {
                EmptyStateView(
                    title = if (isBangla) "কোনো কাস্টমার পাওয়া যায়নি" else "No customers found",
                    subtitle = if (isBangla) "নতুন কাস্টমার যোগ করতে নিচের বাটনে ট্যাপ করুন" else "Tap below to add a new customer",
                    buttonText = if (isBangla) "+ কাস্টমার যোগ করুন" else "+ Add Customer",
                    onButtonClick = onAddCustomerClick
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(customers) { customer ->
                        CustomerListItemCard(
                            customer = customer,
                            privacyMode = privacyMode,
                            isBangla = isBangla,
                            onClick = { onCustomerClick(customer) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // FAB to add Customer
        FloatingActionButton(
            onClick = onAddCustomerClick,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_customer_fab"),
            containerColor = SleekTeal,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Customer")
        }
    }
}

@Composable
fun CustomerListItemCard(
    customer: CustomerEntity,
    privacyMode: Boolean,
    isBangla: Boolean,
    onClick: () -> Unit
) {
    val isDue = customer.currentBalance > 0
    val isAdvance = customer.currentBalance < 0

    val statusColor = when {
        isDue -> Color(0xFF00796B)
        isAdvance -> Color(0xFFC62828)
        else -> Color(0xFF64748B)
    }

    val badgeBg = when {
        isDue -> Color(0xFFE0F2F1)
        isAdvance -> Color(0xFFFFEBEE)
        else -> Color(0xFFF1F5F9)
    }

    val avatarBg = when {
        isDue -> SleekTeal.copy(alpha = 0.12f)
        isAdvance -> PaymentRed.copy(alpha = 0.12f)
        else -> Color(0xFFF1F5F9)
    }

    val avatarTextColor = when {
        isDue -> SleekTeal
        isAdvance -> PaymentRed
        else -> Color(0xFF64748B)
    }

    val statusLabel = when {
        isDue -> if (isBangla) "পাওনা" else "DUE"
        isAdvance -> if (isBangla) "দেনা" else "ADVANCE"
        else -> if (isBangla) "পরিশোধিত" else "SETTLED"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("customer_card_${customer.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Avatar Circle
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(avatarBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = customer.name.take(1).uppercase(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = avatarTextColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = customer.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    if (customer.businessName.isNotEmpty()) {
                        Text(
                            text = customer.businessName,
                            fontSize = 11.sp,
                            color = SleekTeal,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else if (customer.phone.isNotEmpty()) {
                        Text(
                            text = customer.phone,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // Right side balance
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (customer.currentBalance >= 0) "" else "- "}${BanglaUtils.formatTaka(kotlin.math.abs(customer.currentBalance), isBangla = isBangla, showSymbol = true)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = statusColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeBg
                ) {
                    Text(
                        text = statusLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
