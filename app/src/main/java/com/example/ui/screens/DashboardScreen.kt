package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.ui.components.PembayaranBadge
import com.example.ui.components.PengerjaanBadge
import com.example.ui.theme.MyRepRedPrimary
import com.example.ui.viewmodel.CustomerViewModel
import com.example.ui.viewmodel.TargetProgress
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: CustomerViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToDetail: (Customer) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val customers by viewModel.filteredCustomers.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val targets by viewModel.targetProgress.collectAsState()
    val followUpToday by viewModel.followUpTodayList.collectAsState()
    val followUpOverdue by viewModel.followUpOverdueList.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val pengerjaanFilter by viewModel.pengerjaanFilter.collectAsState()
    val pembayaranFilter by viewModel.pembayaranFilter.collectAsState()
    val pipelineFilter by viewModel.pipelineFilter.collectAsState()
    val completenessFilter by viewModel.completenessFilter.collectAsState()

    val syncState by viewModel.syncState.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(syncState.errorMessage) {
        syncState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSyncMessage()
        }
    }

    LaunchedEffect(syncState.successMessage) {
        syncState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSyncMessage()
        }
    }

    // Target Edit Dialog
    var showTargetDialog by remember { mutableStateOf(false) }
    var inputDailyTarget by remember(targets) { mutableStateOf(targets.dailyTarget.toString()) }
    var inputWeeklyTarget by remember(targets) { mutableStateOf(targets.weeklyTarget.toString()) }
    var inputMonthlyTarget by remember(targets) { mutableStateOf(targets.monthlyTarget.toString()) }

    if (showTargetDialog) {
        AlertDialog(
            onDismissRequest = { showTargetDialog = false },
            title = { Text("Atur Target Penjualan", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Tentukan kuota target penjualan pelanggan baru:", fontSize = 12.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = inputDailyTarget,
                        onValueChange = { inputDailyTarget = it },
                        label = { Text("Target Harian") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = inputWeeklyTarget,
                        onValueChange = { inputWeeklyTarget = it },
                        label = { Text("Target Mingguan") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = inputMonthlyTarget,
                        onValueChange = { inputMonthlyTarget = it },
                        label = { Text("Target Bulanan") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val d = inputDailyTarget.toIntOrNull() ?: targets.dailyTarget
                        val w = inputWeeklyTarget.toIntOrNull() ?: targets.weeklyTarget
                        val m = inputMonthlyTarget.toIntOrNull() ?: targets.monthlyTarget
                        viewModel.updateSalesTargets(d, w, m)
                        showTargetDialog = false
                        Toast.makeText(context, "Target penjualan berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MyRepRedPrimary)
                ) {
                    Text("Simpan Target")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTargetDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "DATA PELANGGAN",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "V3 Pro",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "MyRepublic Pipeline, Target & Follow-Up",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.syncNow() }) {
                        if (syncState.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Sinkronkan",
                                tint = Color.White
                            )
                        }
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Pengaturan",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MyRepRedPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = MyRepRedPrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tambah", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            // Connection & Sync Status Banner
            item {
                ConnectionBanner(
                    connectionStatus = connectionStatus,
                    isSyncing = syncState.isSyncing,
                    progressMessage = syncState.progressMessage,
                    lastSync = viewModel.appSettings.lastSyncTime,
                    onSyncClick = { viewModel.syncNow() }
                )
            }

            // MODUL 1: TARGET PENJUALAN (Harian, Mingguan, Bulanan)
            item {
                SalesTargetSection(
                    targetProgress = targets,
                    onEditClick = { showTargetDialog = true }
                )
            }

            // MODUL 3: PENGINGAT FOLLOW-UP TERJADWAL
            if (followUpToday.isNotEmpty() || followUpOverdue.isNotEmpty()) {
                item {
                    FollowUpAlertSection(
                        todayList = followUpToday,
                        overdueList = followUpOverdue,
                        onCustomerClick = onNavigateToDetail,
                        onWhatsAppClick = { cust ->
                            openWhatsAppDirect(context, cust.nomorHp, cust.nama, "Halo ${cust.nama}, kami dari tim MyRepublic ingin menindaklanjuti permohonan pemasangan WiFi Anda.")
                        }
                    )
                }
            }

            // Statistics Grid (5 Metrics: Total, BAHAN, DONE, PAID, UNPAID)
            item {
                StatisticsSection(stats = stats)
            }

            // MODUL 2: PENCARIAN CEPAT & FILTER LANJUTAN
            item {
                SearchAndAdvancedFilterSection(
                    searchQuery = searchQuery,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    pengerjaanFilter = pengerjaanFilter,
                    onPengerjaanFilterChange = { viewModel.setPengerjaanFilter(it) },
                    pembayaranFilter = pembayaranFilter,
                    onPembayaranFilterChange = { viewModel.setPembayaranFilter(it) },
                    pipelineFilter = pipelineFilter,
                    onPipelineFilterChange = { viewModel.setPipelineFilter(it) },
                    completenessFilter = completenessFilter,
                    onCompletenessFilterChange = { viewModel.setCompletenessFilter(it) }
                )
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Pelanggan (${customers.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Customer List
            if (customers.isEmpty()) {
                item {
                    EmptyCustomerView(
                        isFiltered = searchQuery.isNotBlank() || pengerjaanFilter != "SEMUA" || pembayaranFilter != "SEMUA" || pipelineFilter != "SEMUA" || completenessFilter != "SEMUA",
                        onAddClick = onNavigateToAdd,
                        onResetFilter = {
                            viewModel.setSearchQuery("")
                            viewModel.setPengerjaanFilter("SEMUA")
                            viewModel.setPembayaranFilter("SEMUA")
                            viewModel.setPipelineFilter("SEMUA")
                            viewModel.setCompletenessFilter("SEMUA")
                        }
                    )
                }
            } else {
                items(customers, key = { it.id }) { customer ->
                    CustomerCardItem(
                        customer = customer,
                        onClick = { onNavigateToDetail(customer) }
                    )
                }
            }
        }
    }
}

@Composable
fun SalesTargetSection(targetProgress: TargetProgress, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = MyRepRedPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Target Penjualan Sales",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    IconButton(onClick = onEditClick, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Ubah Target",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = "Bulan Ini: ${targetProgress.monthlyCount}/${targetProgress.monthlyTarget}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MyRepRedPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TargetMetricItem(
                    label = "Harian",
                    count = targetProgress.dailyCount,
                    target = targetProgress.dailyTarget,
                    modifier = Modifier.weight(1f)
                )
                TargetMetricItem(
                    label = "Mingguan",
                    count = targetProgress.weeklyCount,
                    target = targetProgress.weeklyTarget,
                    modifier = Modifier.weight(1f)
                )
                TargetMetricItem(
                    label = "Bulanan",
                    count = targetProgress.monthlyCount,
                    target = targetProgress.monthlyTarget,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun TargetMetricItem(label: String, count: Int, target: Int, modifier: Modifier = Modifier) {
    val progress = (count.toFloat() / target.coerceAtLeast(1)).coerceIn(0f, 1f)
    val percentage = (progress * 100).toInt()

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
            Text(text = "$percentage%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MyRepRedPrimary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "$count / $target", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(CircleShape),
            color = if (progress >= 1f) Color(0xFF2E7D32) else MyRepRedPrimary,
            trackColor = Color.LightGray.copy(alpha = 0.4f)
        )
    }
}

@Composable
fun FollowUpAlertSection(
    todayList: List<Customer>,
    overdueList: List<Customer>,
    onCustomerClick: (Customer) -> Unit,
    onWhatsAppClick: (Customer) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = Color(0xFFF57F17),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pengingat Follow-Up (${todayList.size + overdueList.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF5D4037)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val allReminders = (overdueList.map { it to true } + todayList.map { it to false }).take(3)
            allReminders.forEach { (cust, isOverdue) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onCustomerClick(cust) }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = cust.nama,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isOverdue) Color(0xFFFFCDD2) else Color(0xFFC8E6C9))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = if (isOverdue) "TERLAMBAT" else "HARI INI",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOverdue) Color(0xFFC62828) else Color(0xFF2E7D32)
                                )
                            }
                        }
                        Text(
                            text = "${cust.paketWifi} • ${cust.jadwalFollowUp}",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }

                    IconButton(
                        onClick = { onWhatsAppClick(cust) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = "WhatsApp",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectionBanner(
    connectionStatus: String,
    isSyncing: Boolean,
    progressMessage: String,
    lastSync: String,
    onSyncClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (connectionStatus.contains("Gagal")) Icons.Default.CloudOff else Icons.Default.CloudSync,
                contentDescription = null,
                tint = if (connectionStatus.contains("Gagal")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isSyncing) progressMessage else connectionStatus,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Sinkron terakhir: $lastSync",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = onSyncClick,
                enabled = !isSyncing,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Sinkronkan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StatisticsSection(stats: com.example.ui.viewmodel.DashboardStats) {
    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            item {
                StatCard(
                    title = "Total Pelanggan",
                    count = stats.total,
                    icon = Icons.Default.People,
                    cardColor = Color(0xFF263238),
                    textColor = Color.White
                )
            }
            item {
                StatCard(
                    title = "Status BAHAN",
                    count = stats.totalBahan,
                    icon = Icons.Default.HourglassEmpty,
                    cardColor = Color(0xFFFFF3E0),
                    textColor = Color(0xFFE65100)
                )
            }
            item {
                StatCard(
                    title = "Status DONE",
                    count = stats.totalDone,
                    icon = Icons.Default.CheckCircle,
                    cardColor = Color(0xFFE8F5E9),
                    textColor = Color(0xFF1B5E20)
                )
            }
            item {
                StatCard(
                    title = "Status PAID",
                    count = stats.totalPaid,
                    icon = Icons.Default.Payments,
                    cardColor = Color(0xFFE8F5E9),
                    textColor = Color(0xFF1B5E20)
                )
            }
            item {
                StatCard(
                    title = "Status UNPAID",
                    count = stats.totalUnpaid,
                    icon = Icons.Default.Payments,
                    cardColor = Color(0xFFFFEBEE),
                    textColor = Color(0xFFB71C1C)
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    count: Int,
    icon: ImageVector,
    cardColor: Color,
    textColor: Color
) {
    Card(
        modifier = Modifier
            .width(135.dp)
            .height(86.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor.copy(alpha = 0.85f),
                    maxLines = 1
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = "$count",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchAndAdvancedFilterSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    pengerjaanFilter: String,
    onPengerjaanFilterChange: (String) -> Unit,
    pembayaranFilter: String,
    onPembayaranFilterChange: (String) -> Unit,
    pipelineFilter: String,
    onPipelineFilterChange: (String) -> Unit,
    completenessFilter: String,
    onCompletenessFilterChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Cari nama, NIK, No HP, alamat, paket, homepass...", fontSize = 13.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Cari", tint = Color.Gray)
            },
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Pipeline CRM Filters
        Text(
            text = "Pipeline Sales CRM:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val pipelines = listOf("SEMUA") + Customer.ALL_PIPELINE_STAGES.take(5)
            pipelines.forEach { filter ->
                FilterChip(
                    selected = pipelineFilter == filter,
                    onClick = { onPipelineFilterChange(filter) },
                    label = { Text(if (filter == "SEMUA") "Semua Pipeline" else filter, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MyRepRedPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Checklist Kelengkapan Dokumen Filters
        Text(
            text = "Kelengkapan Dokumen & Survei:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("SEMUA" to "Semua Dokumen", "LENGKAP" to "100% Lengkap", "BELUM" to "Belum Lengkap").forEach { (code, label) ->
                FilterChip(
                    selected = completenessFilter == code,
                    onClick = { onCompletenessFilterChange(code) },
                    label = { Text(label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MyRepRedPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Status Pengerjaan & Pembayaran Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Pengerjaan:", fontSize = 11.sp, color = Color.Gray)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("SEMUA", Customer.STATUS_BAHAN, Customer.STATUS_DONE).forEach { f ->
                        FilterChip(
                            selected = pengerjaanFilter == f,
                            onClick = { onPengerjaanFilterChange(f) },
                            label = { Text(if (f == "SEMUA") "Semua" else f, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MyRepRedPrimary, selectedLabelColor = Color.White)
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text("Pembayaran:", fontSize = 11.sp, color = Color.Gray)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("SEMUA", Customer.STATUS_UNPAID, Customer.STATUS_PAID).forEach { f ->
                        FilterChip(
                            selected = pembayaranFilter == f,
                            onClick = { onPembayaranFilterChange(f) },
                            label = { Text(if (f == "SEMUA") "Semua" else f, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MyRepRedPrimary, selectedLabelColor = Color.White)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerCardItem(
    customer: Customer,
    onClick: () -> Unit
) {
    val (completenessScore, missingItems) = customer.getCompleteness()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 5.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Initial Circle
                val initial = customer.nama.firstOrNull()?.uppercaseChar()?.toString() ?: "P"
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MyRepRedPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MyRepRedPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = customer.nama.ifBlank { "Tanpa Nama" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Pipeline Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE0F2F1))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = customer.pipelineSales,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00695C)
                            )
                        }
                    }

                    Text(
                        text = "WA: ${customer.nomorHp} • NIK: ${Customer.maskNik(customer.nik)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Cloud Sync Status Icon
                if (customer.syncPending) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = "Pending Sync",
                        tint = Color(0xFFF57C00),
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Tersinkron",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Package & Address
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Wifi,
                    contentDescription = null,
                    tint = MyRepRedPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = customer.paketWifi.ifBlank { "Paket Standar" },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (customer.kodeHomepass.isNotBlank()) {
                    Text(
                        text = " • HP: ${customer.kodeHomepass}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = customer.alamat.ifBlank { "Alamat belum diisi" },
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Checklist Dokumen & Follow-up Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dokumen Completeness Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (completenessScore == 100) Color(0xFFE8F5E9) else Color(0xFFFFF3E0))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (completenessScore == 100) "✓ Dokumen Lengkap (100%)" else "Berkas $completenessScore% (Kurang ${missingItems.size})",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (completenessScore == 100) Color(0xFF1B5E20) else Color(0xFFE65100)
                    )
                }

                if (customer.jadwalFollowUp.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = if (customer.isFollowUpOverdue()) Color.Red else Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = customer.jadwalFollowUp,
                            fontSize = 10.sp,
                            fontWeight = if (customer.isFollowUpOverdue()) FontWeight.Bold else FontWeight.Normal,
                            color = if (customer.isFollowUpOverdue()) Color.Red else Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PengerjaanBadge(status = customer.statusPengerjaan)
                    PembayaranBadge(status = customer.statusPembayaran)
                }
                Text(
                    text = customer.tanggalInput.take(16),
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun EmptyCustomerView(
    isFiltered: Boolean,
    onAddClick: () -> Unit,
    onResetFilter: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.People,
                contentDescription = null,
                modifier = Modifier.size(54.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (isFiltered) "Tidak ada pelanggan yang cocok" else "Belum Ada Data Pelanggan",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isFiltered)
                    "Coba ubah kata kunci pencarian atau bersihkan filter."
                else
                    "Mulai catat calon pemasangan pelanggan WiFi MyRepublic baru dengan foto persyaratan lengkap.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (isFiltered) {
                Button(onClick = onResetFilter) {
                    Text("Reset Filter")
                }
            } else {
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MyRepRedPrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tambah Pelanggan Baru")
                }
            }
        }
    }
}

fun openWhatsAppDirect(context: Context, rawPhone: String, customerName: String, messageText: String) {
    try {
        var cleanPhone = rawPhone.replace(Regex("[^0-9]"), "")
        if (cleanPhone.startsWith("0")) {
            cleanPhone = "62" + cleanPhone.substring(1)
        }
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${URLEncoder.encode(messageText, "UTF-8")}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Tidak dapat membuka WhatsApp", Toast.LENGTH_SHORT).show()
    }
}
