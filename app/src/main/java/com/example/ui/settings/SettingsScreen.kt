package com.example.ui.settings

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AppLanguage
import com.example.domain.model.AppThemeMode
import com.example.ui.common.TallyHeader

@Composable
fun SettingsScreen(
    khataName: String,
    businessName: String,
    ownerName: String,
    phone: String,
    address: String,
    language: AppLanguage,
    themeMode: AppThemeMode,
    pinLockEnabled: Boolean,
    privacyMode: Boolean,
    onKhataClick: () -> Unit,
    onTogglePrivacy: () -> Unit,
    onToggleLanguage: () -> Unit,
    onSetThemeMode: (AppThemeMode) -> Unit,
    onUpdateProfile: (businessName: String, ownerName: String, phone: String, address: String) -> Unit,
    onSetPinLock: (enabled: Boolean, pin: String) -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToTrash: () -> Unit,
    onNavigateToAuditLogs: () -> Unit
) {
    val isBangla = language == AppLanguage.BANGLA
    val context = LocalContext.current

    var showProfileDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // Business Profile Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showProfileDialog = true }
                        .testTag("settings_profile_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Business, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = businessName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${if (ownerName.isNotEmpty()) "$ownerName • " else ""}$phone",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                    }
                }
            }

            // General Settings Group
            item {
                Text(
                    text = if (isBangla) "সাধারণ সেটিংস" else "General Settings",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column {
                        // Language
                        SettingsItemRow(
                            icon = Icons.Default.Language,
                            title = if (isBangla) "ভাষা (Language)" else "Language",
                            subtitle = if (isBangla) "বাংলা" else "English",
                            onClick = onToggleLanguage
                        )

                        // Theme Mode
                        SettingsItemRow(
                            icon = Icons.Default.DarkMode,
                            title = if (isBangla) "থিম মোড" else "Theme Mode",
                            subtitle = if (isBangla) themeMode.banglaLabel else themeMode.englishLabel,
                            onClick = {
                                val nextTheme = when (themeMode) {
                                    AppThemeMode.SYSTEM -> AppThemeMode.LIGHT
                                    AppThemeMode.LIGHT -> AppThemeMode.DARK
                                    AppThemeMode.DARK -> AppThemeMode.SYSTEM
                                }
                                onSetThemeMode(nextTheme)
                            }
                        )

                        // Privacy Mode Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Visibility, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (isBangla) "গোপনীয়তা মোড (ব্যালেন্স লুকান)" else "Privacy Mode",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = if (isBangla) "টাকার অংক •••••• দিয়ে ঢাকুন" else "Mask balances in public",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = privacyMode,
                                onCheckedChange = { onTogglePrivacy() },
                                modifier = Modifier.testTag("privacy_mode_switch")
                            )
                        }

                        // PIN Lock Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (isBangla) "অ্যাপ সিকিউরিটি পিন লক" else "App PIN Lock",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = if (pinLockEnabled) (if (isBangla) "সক্রিয় (৪ ডিজিট)" else "Enabled") else (if (isBangla) "নিষ্ক্রিয়" else "Disabled"),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = pinLockEnabled,
                                onCheckedChange = { isChecked ->
                                    if (isChecked) {
                                        showPinDialog = true
                                    } else {
                                        onSetPinLock(false, "")
                                    }
                                },
                                modifier = Modifier.testTag("pin_lock_switch")
                            )
                        }
                    }
                }
            }

            // Data & Tools Group
            item {
                Text(
                    text = if (isBangla) "ডেটা ও ব্যাকআপ টুলস" else "Data & Backup",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column {
                        SettingsItemRow(
                            icon = Icons.Default.Backup,
                            title = if (isBangla) "ব্যাকআপ ও রিস্টোর কেন্দ্র" else "Backup & Restore",
                            subtitle = if (isBangla) "অফলাইন ফাইল ও রিস্টোর" else "Manage offline snapshots",
                            onClick = onNavigateToBackup,
                            testTag = "btn_nav_backup"
                        )

                        SettingsItemRow(
                            icon = Icons.Default.Delete,
                            title = if (isBangla) "রিসাইকেল বিন (মুছে ফেলা ডেটা)" else "Recycle Bin (Trash)",
                            subtitle = if (isBangla) "মুছে ফেলা কাস্টমার ও লেনদেন পুনরুদ্ধার" else "Restore deleted items",
                            onClick = onNavigateToTrash,
                            testTag = "btn_nav_trash"
                        )

                        SettingsItemRow(
                            icon = Icons.Default.History,
                            title = if (isBangla) "অডিট ও অ্যাক্টিভিটি লগ" else "Audit Activity Logs",
                            subtitle = if (isBangla) "লেনদেন ও পরিবর্তনের ইতিহাস" else "Track app operations",
                            onClick = onNavigateToAuditLogs
                        )
                    }
                }
            }

            // App Info Section
            item {
                Text(
                    text = if (isBangla) "অ্যাপ তথ্য" else "App Info",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    SettingsItemRow(
                        icon = Icons.Default.Info,
                        title = if (isBangla) "টালি খাতা প্রো সম্পর্কে" else "About Tally Khata Pro",
                        subtitle = "Version 1.0.0 • ambia.inbox@gmail.com",
                        onClick = { showAboutDialog = true }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Profile Edit Dialog
    if (showProfileDialog) {
        var bName by remember { mutableStateOf(businessName) }
        var oName by remember { mutableStateOf(ownerName) }
        var pNum by remember { mutableStateOf(phone) }
        var addr by remember { mutableStateOf(address) }

        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text(if (isBangla) "ব্যবসা প্রোফাইল পরিবর্তন" else "Edit Business Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = bName,
                        onValueChange = { bName = it },
                        label = { Text(if (isBangla) "দোকানের নাম" else "Business Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = oName,
                        onValueChange = { oName = it },
                        label = { Text(if (isBangla) "মালিকের নাম" else "Owner Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pNum,
                        onValueChange = { pNum = it },
                        label = { Text(if (isBangla) "মোবাইল" else "Phone") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = addr,
                        onValueChange = { addr = it },
                        label = { Text(if (isBangla) "ঠিকানা" else "Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (bName.isNotBlank()) {
                            onUpdateProfile(bName.trim(), oName.trim(), pNum.trim(), addr.trim())
                            showProfileDialog = false
                        }
                    }
                ) {
                    Text(if (isBangla) "সংরক্ষণ" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // PIN Setup Dialog
    if (showPinDialog) {
        var newPin by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text(if (isBangla) "৪ ডিজিটের পিন কোড সেট করুন" else "Set 4-Digit PIN") },
            text = {
                Column {
                    Text(if (isBangla) "অ্যাপ খোলার সময় এই পিন প্রয়োজন হবে:" else "This PIN will be required to open the app:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) newPin = it },
                        placeholder = { Text("1234") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPin.length == 4) {
                            onSetPinLock(true, newPin)
                            showPinDialog = false
                        }
                    },
                    enabled = newPin.length == 4
                ) {
                    Text(if (isBangla) "পিন চালু করুন" else "Enable PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }


    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("Tally Khata Pro — টালি খাতা") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "সংস্করণ: 1.0.0 (Production Ready)", fontWeight = FontWeight.SemiBold)
                    Text(text = "• ১০০% অফলাইন ও ব্যক্তিগত ডেটা সুরক্ষা")
                    Text(text = "• কোনো বহিরাগত সার্ভার সংযোগের প্রয়োজন নেই")
                    Text(text = "• আধুনিক জেটপ্যাক কম্পোজ ও রুম ডাটাবেজ দ্বারা নির্মিত")
                    Text(text = "• সম্পূর্ণ বাংলাদেশি ব্যবসায়ের জন্য অপ্টিমাইজড")

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:ambia.inbox@gmail.com")
                                        putExtra(Intent.EXTRA_SUBJECT, "Tally Khata Pro Feedback / Support")
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isBangla) "ডেভলপার ইমেইল:" else "Developer Email:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "ambia.inbox@gmail.com",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }) {
                    Text(if (isBangla) "ঠিক আছে" else "OK")
                }
            }
        )
    }
}

@Composable
fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(14.dp)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}
