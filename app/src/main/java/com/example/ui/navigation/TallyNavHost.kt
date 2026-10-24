package com.example.ui.navigation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.domain.model.AppLanguage
import com.example.domain.model.ReceiptData
import com.example.domain.model.TransactionType
import com.example.domain.utils.BanglaUtils
import com.example.ui.MainViewModel
import com.example.ui.audit.AuditLogScreen
import com.example.ui.backup.AutoRestoreBackupDialog
import com.example.ui.backup.BackupScreen
import com.example.ui.customer.AddCustomerDialog
import com.example.ui.customer.CustomerDetailScreen
import com.example.ui.customer.CustomerListScreen
import com.example.ui.home.HomeScreen
import com.example.ui.khata.KhataManagementDialog
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.receipt.ReceiptDialog
import com.example.ui.reminders.RemindersScreen
import com.example.ui.reports.ReportsScreen
import com.example.ui.security.PinLockScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.SleekTeal
import com.example.ui.transaction.AddTransactionDialog
import com.example.ui.transaction.EditTransactionDialog
import com.example.ui.transaction.TransactionOptionsDialog
import com.example.ui.trash.TrashScreen
import com.example.ui.voice.VoiceEntryDialog
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val banglaTitle: String, val englishTitle: String, val icon: ImageVector) {
    object Home : Screen("home", "হোম", "Home", Icons.Default.Home)
    object Customers : Screen("customers", "কাস্টমার", "Customers", Icons.Default.People)
    object Reports : Screen("reports", "রিপোর্ট", "Reports", Icons.Default.Assessment)
    object Reminders : Screen("reminders", "তাগাদা", "Reminders", Icons.Default.Alarm)
    object Settings : Screen("settings", "সেটিংস", "Settings", Icons.Default.Settings)
    object CustomerDetail : Screen("customer_detail/{customerId}", "বিবরণ", "Detail", Icons.Default.People) {
        fun createRoute(customerId: Long) = "customer_detail/$customerId"
    }
    object Backup : Screen("backup", "ব্যাকআপ", "Backup", Icons.Default.Settings)
    object Trash : Screen("trash", "রিসাইকেল বিন", "Trash", Icons.Default.Settings)
    object Audit : Screen("audit", "অডিট লগ", "Audit", Icons.Default.Settings)
}

val bottomNavScreens = listOf(
    Screen.Home,
    Screen.Customers,
    Screen.Reports,
    Screen.Reminders,
    Screen.Settings
)

@Composable
fun TallyApp(
    viewModel: MainViewModel,
    navController: NavHostController = rememberNavController()
) {
    // State observation
    val onboardingDone by viewModel.onboardingDone.collectAsState()
    val isAppUnlocked by viewModel.isAppUnlocked.collectAsState()
    val pinEnabled by viewModel.pinEnabled.collectAsState()
    val pinCode by viewModel.pinCode.collectAsState()

    val language by viewModel.language.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val privacyMode by viewModel.privacyMode.collectAsState()

    val activeKhata by viewModel.activeKhata.collectAsState()
    val allKhatas by viewModel.allKhatas.collectAsState()
    val businessName by viewModel.businessName.collectAsState()
    val ownerName by viewModel.ownerName.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val address by viewModel.address.collectAsState()

    val dashboardSummary by viewModel.dashboardSummary.collectAsState()
    val chartData by viewModel.chartData.collectAsState()
    val recentTransactions by viewModel.recentTransactions.collectAsState()
    val rawCustomers by viewModel.rawCustomers.collectAsState()
    val filteredCustomers by viewModel.filteredCustomers.collectAsState()
    val allTransactions by viewModel.activeKhataTransactions.collectAsState()
    val pendingReminders by viewModel.pendingReminders.collectAsState()
    val customerFilter by viewModel.customerFilter.collectAsState()
    val customerSort by viewModel.customerSort.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val backupRecords by viewModel.backupRecords.collectAsState()
    val autoBackupSchedule by viewModel.autoBackupSchedule.collectAsState()
    val detectedBackupForAutoRestore by viewModel.detectedBackupForAutoRestore.collectAsState()
    val isRestoringBackup by viewModel.isRestoringBackup.collectAsState()
    val trashCustomers by viewModel.trashCustomers.collectAsState()
    val trashTransactions by viewModel.trashTransactions.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    // Dialog state holders
    var showKhataDialog by remember { mutableStateOf(false) }
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var showVoiceDialog by remember { mutableStateOf(false) }
    var transactionDialogState by remember {
        mutableStateOf<Triple<Boolean, TransactionType, Long?>?>(null)
    }
    var activeReceipt by remember { mutableStateOf<ReceiptData?>(null) }
    var selectedTransactionForOptions by remember { mutableStateOf<com.example.data.entities.TransactionEntity?>(null) }
    var editingTransaction by remember { mutableStateOf<com.example.data.entities.TransactionEntity?>(null) }
    var transactionToDelete by remember { mutableStateOf<com.example.data.entities.TransactionEntity?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Initial Loading state
    if (onboardingDone == null || pinEnabled == null || language == null || themeMode == null) {
        return
    }

    val safeLanguage = language!!
    val safeThemeMode = themeMode!!

    val isBangla = safeLanguage == AppLanguage.BANGLA
    val currentKhataName = activeKhata?.name ?: (if (isBangla) "মূল খাতা" else "Main Ledger")

    // Check Onboarding
    if (onboardingDone == false) {
        Box(modifier = Modifier.fillMaxSize()) {
            OnboardingScreen(
                onFinishOnboarding = { bName, oName, pNum, lang ->
                    viewModel.finishOnboarding(bName, oName, pNum, lang)
                }
            )

            // Auto-Detection Restore Dialog if previous backup is found upon app launch/reinstall
            detectedBackupForAutoRestore?.let { preview ->
                AutoRestoreBackupDialog(
                    preview = preview,
                    isBangla = isBangla,
                    isRestoring = isRestoringBackup,
                    onRestore = {
                        viewModel.restoreFromAutoDetectedBackup(preview.file) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    if (isBangla) "✅ পূর্ববর্তী ব্যাকআপ সফলভাবে রিস্টোর করা হয়েছে"
                                    else "✅ Previous backup successfully restored"
                                )
                            }
                        }
                    },
                    onSkip = {
                        viewModel.dismissAutoRestore()
                    }
                )
            }
        }
        return
    }

    // Check PIN Lock
    if (pinEnabled == true && !isAppUnlocked) {
        PinLockScreen(
            correctPin = pinCode,
            language = safeLanguage,
            onSuccess = { viewModel.unlockApp() }
        )
        return
    }

    val context = LocalContext.current

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isBottomBarVisible = bottomNavScreens.any { it.route == currentRoute }

    // Exit confirmation dialog state
    var showExitConfirmDialog by remember { mutableStateOf(false) }

    // Back button handling:
    // 1. From secondary main tabs (Settings, Customers, Reports, Reminders), pressing Back goes directly to Home.
    val isSecondaryTab = currentRoute in listOf(
        Screen.Customers.route,
        Screen.Reports.route,
        Screen.Reminders.route,
        Screen.Settings.route
    )
    BackHandler(enabled = isSecondaryTab) {
        navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Home.route) {
                inclusive = false
            }
            launchSingleTop = true
        }
    }

    // 2. From Home, pressing Back asks for confirmation before exiting the app.
    BackHandler(enabled = currentRoute == Screen.Home.route) {
        showExitConfirmDialog = true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (isBottomBarVisible) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    modifier = Modifier.border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant))
                ) {
                    bottomNavScreens.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                if (screen == Screen.Reminders && pendingReminders.isNotEmpty()) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = SleekTeal,
                                                contentColor = Color.White
                                            ) {
                                                Text(
                                                    if (isBangla) BanglaUtils.toBanglaDigits(pendingReminders.size.toLong())
                                                    else pendingReminders.size.toString(),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    ) {
                                        Icon(screen.icon, contentDescription = screen.englishTitle)
                                    }
                                } else {
                                    Icon(screen.icon, contentDescription = screen.englishTitle)
                                }
                            },
                            label = {
                                Text(
                                    text = if (isBangla) screen.banglaTitle else screen.englishTitle,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            selected = selected,
                            onClick = {
                                if (screen == Screen.Home) {
                                    if (currentRoute != Screen.Home.route) {
                                        navController.navigate(Screen.Home.route) {
                                            popUpTo(Screen.Home.route) {
                                                inclusive = false
                                            }
                                            launchSingleTop = true
                                        }
                                    }
                                } else if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Home.route) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SleekTeal,
                                selectedTextColor = SleekTeal,
                                indicatorColor = SleekTeal.copy(alpha = 0.12f),
                                unselectedIconColor = Color(0xFF94A3B8),
                                unselectedTextColor = Color(0xFF94A3B8)
                            ),
                            modifier = Modifier.testTag("nav_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. Home Dashboard
            composable(Screen.Home.route) {
                HomeScreen(
                    khataName = currentKhataName,
                    businessName = businessName,
                    summary = dashboardSummary,
                    chartData = chartData,
                    recentTransactions = recentTransactions,
                    customers = rawCustomers,
                    pendingReminders = pendingReminders,
                    language = safeLanguage,
                    privacyMode = privacyMode,
                    onKhataClick = { showKhataDialog = true },
                    onTogglePrivacy = { viewModel.togglePrivacyMode() },
                    onToggleLanguage = { viewModel.toggleLanguage() },
                    onAddCreditClick = {
                        transactionDialogState = Triple(true, TransactionType.CREDIT, null)
                    },
                    onAddPaymentClick = {
                        transactionDialogState = Triple(true, TransactionType.PAYMENT, null)
                    },
                    onAddCustomerClick = { showAddCustomerDialog = true },
                    onVoiceEntryClick = { showVoiceDialog = true },
                    onTransactionClick = { txn ->
                        selectedTransactionForOptions = txn
                    },
                    onViewAllTransactions = {
                        navController.navigate(Screen.Reports.route) {
                            popUpTo(Screen.Home.route) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    },
                    onReminderClick = {
                        navController.navigate(Screen.Reminders.route) {
                            popUpTo(Screen.Home.route) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // 2. Customers List
            composable(Screen.Customers.route) {
                CustomerListScreen(
                    khataName = currentKhataName,
                    businessName = businessName,
                    customers = filteredCustomers,
                    searchQuery = searchQuery,
                    currentFilter = customerFilter,
                    currentSort = customerSort,
                    language = safeLanguage,
                    privacyMode = privacyMode,
                    onKhataClick = { showKhataDialog = true },
                    onTogglePrivacy = { viewModel.togglePrivacyMode() },
                    onToggleLanguage = { viewModel.toggleLanguage() },
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onFilterChange = { viewModel.setCustomerFilter(it) },
                    onSortChange = { viewModel.setCustomerSort(it) },
                    onCustomerClick = { customer ->
                        navController.navigate(Screen.CustomerDetail.createRoute(customer.id))
                    },
                    onAddCustomerClick = { showAddCustomerDialog = true }
                )
            }

            // 3. Customer Detail
            composable(
                route = Screen.CustomerDetail.route,
                arguments = listOf(navArgument("customerId") { type = NavType.LongType })
            ) { backStackEntry ->
                val customerId = backStackEntry.arguments?.getLong("customerId") ?: 0L
                val customer = rawCustomers.find { it.id == customerId }
                val customerTxns = allTransactions.filter { it.customerId == customerId }

                if (customer != null) {
                    CustomerDetailScreen(
                        customer = customer,
                        transactions = customerTxns,
                        businessName = businessName,
                        language = safeLanguage,
                        privacyMode = privacyMode,
                        onBackClick = { navController.popBackStack() },
                        onAddCreditClick = {
                            transactionDialogState = Triple(true, TransactionType.CREDIT, customerId)
                        },
                        onAddPaymentClick = {
                            transactionDialogState = Triple(true, TransactionType.PAYMENT, customerId)
                        },
                        onTransactionClick = { txn ->
                            selectedTransactionForOptions = txn
                        },
                        onUpdateCustomer = { updated -> viewModel.updateCustomer(updated) },
                        onDeleteCustomer = { id -> viewModel.deleteCustomer(id) },
                        onAddReminder = { cId, time, note -> viewModel.createReminder(cId, time, note) }
                    )
                }
            }

            // 4. Reports Screen
            composable(Screen.Reports.route) {
                ReportsScreen(
                    khataName = currentKhataName,
                    businessName = businessName,
                    allTransactions = allTransactions,
                    customers = rawCustomers,
                    language = safeLanguage,
                    privacyMode = privacyMode,
                    onKhataClick = { showKhataDialog = true },
                    onTogglePrivacy = { viewModel.togglePrivacyMode() },
                    onToggleLanguage = { viewModel.toggleLanguage() },
                    onTransactionClick = { txn ->
                        selectedTransactionForOptions = txn
                    }
                )
            }

            // 5. Reminders Screen
            composable(Screen.Reminders.route) {
                RemindersScreen(
                    khataName = currentKhataName,
                    businessName = businessName,
                    reminders = pendingReminders,
                    customers = rawCustomers,
                    language = safeLanguage,
                    privacyMode = privacyMode,
                    onKhataClick = { showKhataDialog = true },
                    onTogglePrivacy = { viewModel.togglePrivacyMode() },
                    onToggleLanguage = { viewModel.toggleLanguage() },
                    onMarkCompleted = { id -> viewModel.markReminderCompleted(id) },
                    onDeleteReminder = { reminder -> viewModel.deleteReminder(reminder) }
                )
            }

            // 6. Settings Screen
            composable(Screen.Settings.route) {
                SettingsScreen(
                    khataName = currentKhataName,
                    businessName = businessName,
                    ownerName = ownerName,
                    phone = phone,
                    address = address,
                    language = safeLanguage,
                    themeMode = safeThemeMode,
                    pinLockEnabled = pinEnabled ?: false,
                    privacyMode = privacyMode,
                    onKhataClick = { showKhataDialog = true },
                    onTogglePrivacy = { viewModel.togglePrivacyMode() },
                    onToggleLanguage = { viewModel.toggleLanguage() },
                    onSetThemeMode = { viewModel.setThemeMode(it) },
                    onUpdateProfile = { b, o, p, a -> viewModel.updateProfile(b, o, p, a) },
                    onSetPinLock = { enabled, pin -> viewModel.setPin(pin, enabled) },
                    onNavigateToBackup = { navController.navigate(Screen.Backup.route) },
                    onNavigateToTrash = { navController.navigate(Screen.Trash.route) },
                    onNavigateToAuditLogs = { navController.navigate(Screen.Audit.route) }
                )
            }

            // 7. Backup Screen
            composable(Screen.Backup.route) {
                BackupScreen(
                    backups = backupRecords,
                    autoBackupSchedule = autoBackupSchedule,
                    language = safeLanguage,
                    onBackClick = { navController.popBackStack() },
                    onCreateBackup = {
                        viewModel.createBackup {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    if (isBangla) "✅ নতুন ব্যাকআপ 'Documents/TallyKhata' তে সংরক্ষিত হয়েছে"
                                    else "✅ New backup saved in 'Documents/TallyKhata'"
                                )
                            }
                        }
                    },
                    onRestoreBackup = { file, merge ->
                        viewModel.restoreBackup(file, merge) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    if (isBangla) "✅ ব্যাকআপ সফলভাবে রিস্টোর করা হয়েছে"
                                    else "✅ Backup restored successfully"
                                )
                            }
                        }
                    },
                    onDeleteBackup = { record ->
                        viewModel.deleteBackup(record)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                if (isBangla) "🗑️ ব্যাকআপ ফাইল মুছে ফেলা হয়েছে"
                                else "🗑️ Backup file deleted"
                            )
                        }
                    },
                    onSetAutoBackup = { schedule -> viewModel.setAutoBackup(schedule) },
                    onRefreshBackups = { viewModel.syncBackups() }
                )
            }

            // 8. Trash Screen
            composable(Screen.Trash.route) {
                TrashScreen(
                    trashCustomers = trashCustomers,
                    trashTransactions = trashTransactions,
                    language = safeLanguage,
                    onBackClick = { navController.popBackStack() },
                    onRestoreCustomer = { id -> viewModel.restoreCustomer(id) },
                    onPermanentDeleteCustomer = { id -> viewModel.permanentDeleteCustomer(id) },
                    onRestoreTransaction = { id -> viewModel.restoreTransaction(id) },
                    onPermanentDeleteTransaction = { id -> viewModel.permanentDeleteTransaction(id) },
                    onEmptyTrash = { viewModel.emptyTrash() }
                )
            }

            // 9. Audit Log Screen
            composable(Screen.Audit.route) {
                AuditLogScreen(
                    logs = auditLogs,
                    language = safeLanguage,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }

    // Modal: Add / Switch Khata
    if (showKhataDialog) {
        KhataManagementDialog(
            currentKhataId = activeKhata?.id ?: 1L,
            khatas = allKhatas,
            language = safeLanguage,
            onDismiss = { showKhataDialog = false },
            onSelectKhata = { id -> viewModel.switchKhata(id) },
            onCreateKhata = { name, desc, biz -> viewModel.createKhata(name, desc, biz) },
            onRenameKhata = { khata -> viewModel.renameKhata(khata) },
            onDeleteKhata = { id -> viewModel.deleteKhata(id) }
        )
    }

    // Modal: Add Customer
    if (showAddCustomerDialog) {
        AddCustomerDialog(
            language = safeLanguage,
            onDismiss = { showAddCustomerDialog = false },
            onSave = { name, custPhone, custAddress, custBiz, notes, initialBalance ->
                viewModel.createCustomer(
                    name = name,
                    phone = custPhone,
                    address = custAddress,
                    businessName = custBiz,
                    notes = notes,
                    initialBalance = initialBalance
                )
                showAddCustomerDialog = false
            }
        )
    }

    // Modal: Add Transaction (+ Credit / - Payment)
    transactionDialogState?.let { (isOpen, type, preselectedCustomerId) ->
        if (isOpen) {
            AddTransactionDialog(
                initialType = type,
                initialCustomerId = preselectedCustomerId,
                customers = rawCustomers,
                language = safeLanguage,
                onDismiss = { transactionDialogState = null },
                onSave = { custId, txnType, amount, quantity, unitPrice, discount, note, category ->
                    viewModel.createTransaction(
                        customerId = custId,
                        type = txnType,
                        amount = amount,
                        quantity = quantity,
                        unitPrice = unitPrice,
                        discount = discount,
                        note = note,
                        category = category,
                        onSuccess = { txn ->
                            transactionDialogState = null
                            activeReceipt = viewModel.getReceiptData(txn)
                        }
                    )
                }
            )
        }
    }

    // Modal: Voice Entry
    if (showVoiceDialog) {
        VoiceEntryDialog(
            customers = rawCustomers,
            language = safeLanguage,
            onDismiss = { showVoiceDialog = false },
            onSaveTransaction = { custId, txnType, amount, note ->
                viewModel.createTransaction(
                    customerId = custId,
                    type = txnType,
                    amount = amount,
                    note = note,
                    onSuccess = { txn ->
                        showVoiceDialog = false
                        activeReceipt = viewModel.getReceiptData(txn)
                    }
                )
            }
        )
    }

    // Modal: Digital Receipt Preview & Actions
    activeReceipt?.let { receipt ->
        ReceiptDialog(
            receipt = receipt,
            language = safeLanguage,
            onDismiss = { activeReceipt = null }
        )
    }

    // Modal: Transaction Options (Edit, Delete, View Receipt)
    selectedTransactionForOptions?.let { txn ->
        val customer = rawCustomers.find { it.id == txn.customerId }
        TransactionOptionsDialog(
            transaction = txn,
            customer = customer,
            language = safeLanguage,
            onDismiss = { selectedTransactionForOptions = null },
            onEditClick = {
                editingTransaction = txn
            },
            onViewReceiptClick = {
                activeReceipt = viewModel.getReceiptData(txn)
            },
            onDeleteClick = {
                transactionToDelete = txn
            }
        )
    }

    // Modal: Delete Transaction Confirmation Dialog
    transactionToDelete?.let { txn ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text(if (isBangla) "লেনদেন মুছে ফেলতে চান?" else "Delete Transaction?") },
            text = {
                Text(
                    if (isBangla)
                        "এই লেনদেনটি রিসাইকেল বিনে (Trash) পাঠানো হবে এবং কাস্টমারের ব্যালেন্স স্বয়ংক্রিয়ভাবে সমন্বয় হবে।"
                    else
                        "This transaction will be moved to Recycle Bin and the customer's balance will be recalculated."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = txn.id
                        transactionToDelete = null
                        viewModel.deleteTransaction(id)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                if (isBangla) "লেনদেন ট্র্যাশে পাঠানো হয়েছে" else "Transaction moved to trash"
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isBangla) "মুছে ফেলুন" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Modal: Edit Transaction
    editingTransaction?.let { txn ->
        EditTransactionDialog(
            transaction = txn,
            customers = rawCustomers,
            language = safeLanguage,
            onDismiss = { editingTransaction = null },
            onSave = { id, type, amount, quantity, unitPrice, discount, note, category, attachmentUri, date, newCustomerId ->
                viewModel.updateTransaction(
                    id = id,
                    amount = amount,
                    type = type,
                    quantity = quantity,
                    unitPrice = unitPrice,
                    discount = discount,
                    note = note,
                    category = category,
                    attachmentUri = attachmentUri,
                    date = date,
                    newCustomerId = newCustomerId,
                    onSuccess = {
                        editingTransaction = null
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("✅ লেনদেন সফলভাবে আপডেট হয়েছে")
                        }
                    }
                )
            }
        )
    }

    // Modal: App Exit Confirmation Dialog
    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    tint = SleekTeal,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = if (isBangla) "অ্যাপ থেকে বের হতে চান?" else "Exit App?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = if (isBangla)
                        "আপনি কি নিশ্চিতভাবে টালি খাতা প্রো বন্ধ করতে চান?"
                    else
                        "Are you sure you want to exit Tally Khata Pro?",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmDialog = false
                        (context as? Activity)?.finish()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SleekTeal
                    )
                ) {
                    Text(if (isBangla) "হ্যাঁ, বের হন" else "Yes, Exit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmDialog = false }) {
                    Text(
                        text = if (isBangla) "না, ফিরে যান" else "Cancel",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }

    // Modal: Auto-Detection Restore Dialog on App Launch/Reinstall
    detectedBackupForAutoRestore?.let { preview ->
        AutoRestoreBackupDialog(
            preview = preview,
            isBangla = isBangla,
            isRestoring = isRestoringBackup,
            onRestore = {
                viewModel.restoreFromAutoDetectedBackup(preview.file) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            if (isBangla) "✅ পূর্ববর্তী ব্যাকআপ সফলভাবে রিস্টোর করা হয়েছে"
                            else "✅ Previous backup successfully restored"
                        )
                    }
                }
            },
            onSkip = {
                viewModel.dismissAutoRestore()
            }
        )
    }
}
