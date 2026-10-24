package com.example.ui.backup

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.BackupEntity
import com.example.domain.backup.BackupPreviewInfo
import com.example.domain.backup.LocalBackupManager
import com.example.domain.model.AppLanguage
import com.example.domain.model.AutoBackupSchedule
import com.example.domain.utils.BanglaUtils
import com.example.ui.common.EmptyStateView
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    backups: List<BackupEntity>,
    autoBackupSchedule: AutoBackupSchedule,
    language: AppLanguage,
    onBackClick: () -> Unit,
    onCreateBackup: () -> Unit,
    onRestoreBackup: (file: File, merge: Boolean) -> Unit,
    onDeleteBackup: (BackupEntity) -> Unit,
    onSetAutoBackup: (AutoBackupSchedule) -> Unit,
    onRefreshBackups: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isBangla = language == AppLanguage.BANGLA

    androidx.compose.runtime.LaunchedEffect(Unit) {
        onRefreshBackups()
    }

    var previewBackupInfo by remember { mutableStateOf<BackupPreviewInfo?>(null) }
    var backupToDelete by remember { mutableStateOf<BackupEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isBangla) "ব্যাকআপ ও রিস্টোর" else "Backup & Restore", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // Create Backup Hero Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isBangla) "নিরাপদ অফলাইন ব্যাকআপ" else "Safe Offline Backup",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isBangla) "আপনার সমস্ত খাতা, কাস্টমার এবং লেনদেনের পূর্ণাঙ্গ কপি ডিভাইসের 'Documents/TallyKhata' ফোল্ডারে সংরক্ষিত থাকবে। অ্যাপ আনইনস্টল করলেও ব্যাকআপ সুরক্ষিত থাকবে।" else "Store a full snapshot of your ledgers and transactions safely in 'Documents/TallyKhata'. Backups persist even after app uninstall.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onCreateBackup,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("create_backup_button")
                        ) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBangla) "এখনই ব্যাকআপ নিন" else "Create Backup Now",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Auto Backup Schedule Selector
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (isBangla) "অটো ব্যাকআপ সময়সূচী" else "Auto Backup Schedule",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AutoBackupSchedule.values().forEach { sched ->
                                val isSelected = sched == autoBackupSchedule
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onSetAutoBackup(sched) }
                                ) {
                                    Text(
                                        text = if (isBangla) sched.banglaLabel else sched.englishLabel,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // History Section Header
            item {
                Text(
                    text = if (isBangla) "সংরক্ষিত ব্যাকআপ ফাইলসমূহ (${BanglaUtils.toBanglaDigits(backups.size.toLong())})" else "Backup Records (${backups.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (backups.isEmpty()) {
                item {
                    EmptyStateView(
                        title = if (isBangla) "কোনো ব্যাকআপ ফাইল নেই" else "No backup files yet",
                        subtitle = if (isBangla) "উপরে 'এখনই ব্যাকআপ নিন' বাটনে ট্যাপ করে ব্যাকআপ তৈরি করুন" else "Tap 'Create Backup Now' above to generate a backup"
                    )
                }
            } else {
                items(backups) { record ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = record.fileName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "তারিখ: ${BanglaUtils.formatDateTime(record.createdAt, isBangla)} • সাইজ: ${record.sizeBytes / 1024} KB",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${record.customerCount} কাস্টমার • ${record.transactionCount} লেনদেন",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Restore Button
                                OutlinedButton(
                                    onClick = {
                                        val file = File(record.filePath)
                                        scope.launch {
                                            val preview = LocalBackupManager.inspectBackupFile(file)
                                            previewBackupInfo = preview
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("restore_btn_${record.id}")
                                ) {
                                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isBangla) "রিস্টোর" else "Restore", fontSize = 11.sp)
                                }

                                IconButton(
                                    onClick = { backupToDelete = record },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
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

    // Delete Confirmation Dialog
    backupToDelete?.let { record ->
        AlertDialog(
            onDismissRequest = { backupToDelete = null },
            title = { Text(if (isBangla) "ব্যাকআপ ফাইল ডিলিট করবেন?" else "Delete Backup File?") },
            text = {
                Text(
                    text = if (isBangla)
                        "ফাইল: ${record.fileName}\n\nএই ব্যাকআপ ফাইলটি আপনার ডিভাইসের 'Documents/TallyKhata' ফোল্ডার থেকে স্থায়ীভাবে মুছে ফেলা হবে।"
                    else
                        "File: ${record.fileName}\n\nThis backup file will be permanently removed from your device's 'Documents/TallyKhata' folder."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteBackup(record)
                        backupToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isBangla) "মুছে ফেলুন" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { backupToDelete = null }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Restore Confirmation Dialog with Preview
    previewBackupInfo?.let { preview ->
        AlertDialog(
            onDismissRequest = { previewBackupInfo = null },
            title = { Text(if (isBangla) "ব্যাকআপ রিস্টোর অপশন" else "Restore Backup Options") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isBangla) "ব্যাকআপ ফাইলের বিবরণ:" else "Backup File Information:",
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "• তারিখ: ${preview.dateString}", fontSize = 13.sp)
                    Text(text = "• মোট খাতা: ${preview.khataCount}", fontSize = 13.sp)
                    Text(text = "• মোট কাস্টমার: ${preview.customerCount}", fontSize = 13.sp)
                    Text(text = "• মোট লেনদেন: ${preview.transactionCount}", fontSize = 13.sp)

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isBangla)
                            "সম্পূর্ণ রিস্টোর (বর্তমান ডেটা মুছে যাবে) নাকি মার্জ রিস্টোর (বর্তমান ডেটার সাথে যোগ হবে) করতে চান?"
                        else
                            "Choose Full Restore (replace current data) or Merge Restore (combine with current data).",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Merge Restore
                    OutlinedButton(
                        onClick = {
                            onRestoreBackup(preview.file, true)
                            previewBackupInfo = null
                        }
                    ) {
                        Text(if (isBangla) "মার্জ রিস্টোর" else "Merge")
                    }

                    // Full Restore
                    Button(
                        onClick = {
                            onRestoreBackup(preview.file, false)
                            previewBackupInfo = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(if (isBangla) "সম্পূর্ণ রিস্টোর" else "Full Restore")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { previewBackupInfo = null }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }
}
