package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import kotlinx.coroutines.launch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WifiPackage
import com.example.ui.theme.MyRepRedPrimary
import com.example.ui.viewmodel.CustomerViewModel
import com.example.util.GoogleAppsScriptTemplate
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAndGuideScreen(
    viewModel: CustomerViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    BackHandler { onNavigateBack() }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Integrasi Sheets", "Panduan Lengkap", "Kelola Paket WiFi", "Ekspor & Keamanan")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pengaturan & Panduan",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MyRepRedPrimary)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 12.dp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> IntegrationTab(context, viewModel)
                1 -> GuideTab(context)
                2 -> WifiPackagesTab(viewModel)
                3 -> ExportAndSecurityTab(context, viewModel)
            }
        }
    }
}

@Composable
fun IntegrationTab(context: Context, viewModel: CustomerViewModel) {
    var webAppUrlInput by remember { mutableStateOf(viewModel.appSettings.webAppUrl) }
    var operatorNameInput by remember { mutableStateOf(viewModel.appSettings.petugasName) }
    val connectionStatus by viewModel.connectionStatus.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = MyRepRedPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Koneksi Google Sheets & Drive",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Masukkan Web App URL yang didapatkan setelah menerapkan Google Apps Script di Spreadsheet Anda.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = webAppUrlInput,
                        onValueChange = { webAppUrlInput = it },
                        label = { Text("URL Google Apps Script Web App") },
                        placeholder = { Text("https://script.google.com/macros/s/.../exec") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.appSettings.webAppUrl = webAppUrlInput
                                Toast.makeText(context, "URL berhasil disimpan!", Toast.LENGTH_SHORT).show()
                                viewModel.testConnection()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MyRepRedPrimary)
                        ) {
                            Text("Simpan URL")
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.appSettings.webAppUrl = webAppUrlInput
                                viewModel.testConnection()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Uji Koneksi")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Status Result Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (connectionStatus.contains("Tersambung")) Color(0xFFE8F5E9)
                                else if (connectionStatus.contains("Gagal")) Color(0xFFFFEBEE)
                                else Color(0xFFECEFF1)
                            )
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Status: $connectionStatus",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (connectionStatus.contains("Tersambung")) Color(0xFF1B5E20)
                            else if (connectionStatus.contains("Gagal")) Color(0xFFB71C1C)
                            else Color(0xFF37474F)
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MyRepRedPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Identitas Petugas / Operator",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Nama ini dicatat sebagai riwayat pembaruan status pengerjaan dan pembayaran di Google Sheets.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = operatorNameInput,
                        onValueChange = {
                            operatorNameInput = it
                            viewModel.appSettings.petugasName = it
                        },
                        label = { Text("Nama Petugas / Teknisi") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GuideTab(context: Context) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kode Google Apps Script",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MyRepRedPrimary
                        )
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("AppsScriptMyRepublic", GoogleAppsScriptTemplate.SCRIPT_CODE)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Kode Apps Script berhasil disalin ke Clipboard!", Toast.LENGTH_LONG).show()
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MyRepRedPrimary)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Salin Kode", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Kode ini menangani penyimpanan database Spreadsheet dan pengunggahan foto otomatis ke folder Google Drive 'DATA_PELANGGAN_MYREPUBLIC'.",
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF212121))
                            .padding(10.dp)
                    ) {
                        LazyColumn {
                            item {
                                Text(
                                    text = GoogleAppsScriptTemplate.SCRIPT_CODE,
                                    color = Color(0xFF81C784),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Langkah-Langkah Pemasangan (5 Menit)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MyRepRedPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val steps = listOf(
                        "1. Buat Spreadsheet Baru di Google Drive akun Anda (misal beri judul 'DATABASE PELANGGAN MYREPUBLIC').",
                        "2. Klik menu 'Ekstensi' (Extensions) di atas, lalu pilih 'Apps Script'.",
                        "3. Hapus semua teks bawaan, lalu tekan tombol 'Salin Kode' di atas dan tempel seluruh kode ke editor Apps Script.",
                        "4. Klik tombol 'Terapkan' (Deploy) di pojok kanan atas -> pilih 'Penerapan Baru' (New Deployment).",
                        "5. Klik ikon gerigi jenis penerapan -> pilih 'Aplikasi Web' (Web App).",
                        "6. Atur 'Jalankan sebagai' = 'Saya' (Me), dan 'Siapa yang memiliki akses' = 'Siapa saja' (Anyone).",
                        "7. Klik 'Terapkan', izinkan otorisasi akses Google Drive & Sheets akun Anda.",
                        "8. Salin URL Aplikasi Web yang berakhiran '/exec', lalu tempel di tab 'Integrasi Sheets' aplikasi ini."
                    )

                    steps.forEach { step ->
                        Text(
                            text = step,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 3.dp),
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Struktur 20 Kolom Spreadsheet",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MyRepRedPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Kolom berikut otomatis dibuat pada sheet 'Pelanggan':",
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = GoogleAppsScriptTemplate.SHEET_COLUMNS_INFO.trim(),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun WifiPackagesTab(viewModel: CustomerViewModel) {
    val packages by viewModel.wifiPackages.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    var newPkgName by remember { mutableStateOf("") }
    var newPkgSpeed by remember { mutableStateOf("") }
    var newPkgPrice by remember { mutableStateOf("") }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Tambah Paket WiFi Baru") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newPkgName,
                        onValueChange = { newPkgName = it },
                        label = { Text("Nama Paket (contoh: Dynamic 150)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPkgSpeed,
                        onValueChange = { newPkgSpeed = it },
                        label = { Text("Kecepatan (contoh: 150 Mbps)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPkgPrice,
                        onValueChange = { newPkgPrice = it },
                        label = { Text("Estimasi Harga (contoh: Rp 399.000/bln)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPkgName.isNotBlank() && newPkgSpeed.isNotBlank()) {
                            viewModel.addWifiPackage(
                                WifiPackage(
                                    id = "pkg_${UUID.randomUUID().toString().take(6)}",
                                    name = newPkgName.trim(),
                                    speed = newPkgSpeed.trim(),
                                    priceEstimate = newPkgPrice.trim(),
                                    isActive = true
                                )
                            )
                            newPkgName = ""
                            newPkgSpeed = ""
                            newPkgPrice = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MyRepRedPrimary)
                ) {
                    Text("Tambah")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Kelola Paket WiFi Resmi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Aktifkan atau nonaktifkan paket yang ditawarkan di formulir.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                Button(
                    onClick = { showAddDialog = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MyRepRedPrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah Paket", fontSize = 11.sp)
                }
            }
        }

        items(packages.size) { idx ->
            val pkg = packages[idx]
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Wifi,
                        contentDescription = null,
                        tint = if (pkg.isActive) MyRepRedPrimary else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = pkg.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${pkg.speed} • ${pkg.priceEstimate}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = pkg.isActive,
                        onCheckedChange = { viewModel.toggleWifiPackage(pkg.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ExportAndSecurityTab(context: Context, viewModel: CustomerViewModel) {
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var includeFullNik by remember { mutableStateOf(false) }

    var userRole by remember { mutableStateOf(viewModel.appSettings.userRole) }
    val roles = listOf("ADMIN" to "Administrator (Penuh)", "SALES" to "Sales Lapangan", "TEKNISI" to "Teknisi Lapangan")

    var showRestoreDialog by remember { mutableStateOf(false) }
    var jsonRestoreInput by remember { mutableStateOf("") }

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Pulihkan Data dari JSON", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tempelkan data JSON cadangan (backup) yang pernah Anda ekspor:", fontSize = 12.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = jsonRestoreInput,
                        onValueChange = { jsonRestoreInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        placeholder = { Text("[{\"id\": \"MYREP-...\", ...}]", fontSize = 11.sp) }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (jsonRestoreInput.isNotBlank()) {
                            coroutineScope.launch {
                                val result = viewModel.importCustomersFromJson(jsonRestoreInput.trim())
                                result.onSuccess { count ->
                                    Toast.makeText(context, "Berhasil memulihkan $count data pelanggan!", Toast.LENGTH_LONG).show()
                                    showRestoreDialog = false
                                    jsonRestoreInput = ""
                                }.onFailure { e ->
                                    Toast.makeText(context, "Gagal memulihkan: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MyRepRedPrimary)
                ) {
                    Text("Pulihkan Sekarang")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hak Akses & Peran Pengguna (Modul 11)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Badge,
                            contentDescription = null,
                            tint = MyRepRedPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hak Akses & Peran Pengguna",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tentukan peran akun Anda untuk mengelola batas wewenang aplikasi.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    roles.forEach { (code, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (userRole == code) MyRepRedPrimary.copy(alpha = 0.1f) else Color.Transparent)
                                .clickable {
                                    userRole = code
                                    viewModel.appSettings.userRole = code
                                    Toast.makeText(context, "Peran diubah menjadi: $label", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (userRole == code) FontWeight.Bold else FontWeight.Normal,
                                color = if (userRole == code) MyRepRedPrimary else Color.DarkGray
                            )
                        }
                    }
                }
            }
        }

        // Ekspor Laporan CSV & HTML/PDF (Modul 7 & 15)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = null,
                            tint = MyRepRedPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ekspor Laporan (CSV & Cetak PDF)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Ekspor daftar pelanggan ke file CSV untuk Excel, atau buat laporan HTML siap cetak/simpan PDF.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = includeFullNik,
                            onCheckedChange = { includeFullNik = it }
                        )
                        Text(
                            text = "Sertakan NIK Lengkap (Bukan Sensor)",
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val csvContent = viewModel.generateCsvExport(includeFullNik)
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, csvContent)
                                    putExtra(Intent.EXTRA_SUBJECT, "DATA_PELANGGAN_MYREPUBLIC.csv")
                                    type = "text/csv"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Ekspor CSV Pelanggan"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MyRepRedPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ekspor CSV", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val htmlReport = viewModel.generatePrintableHtmlReport(includeFullNik)
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, htmlReport)
                                    putExtra(Intent.EXTRA_SUBJECT, "LAPORAN_RESMI_MYREPUBLIC.html")
                                    type = "text/html"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Cetak Laporan / PDF"))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Laporan PDF", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Backup & Restore Database (Modul 10)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Backup,
                            contentDescription = null,
                            tint = MyRepRedPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cadangkan & Pulihkan Data (JSON)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Amankan seluruh database pelanggan lokal ke file JSON yang dapat disimpan di Google Drive atau dibagikan.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val jsonBackup = viewModel.exportAllCustomersAsJson()
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, jsonBackup)
                                    putExtra(Intent.EXTRA_SUBJECT, "BACKUP_DATA_PELANGGAN_MYREPUBLIC.json")
                                    type = "application/json"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Simpan File Backup JSON"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF263238)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cadangkan Data", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { showRestoreDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pulihkan Data", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Prosedur Backup Spreadsheet
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Prosedur Pemulihan & Backup Google Sheets",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MyRepRedPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1. Buka spreadsheet database di browser HP.\n2. Pilih menu 'File' -> 'Buat Salinan' (Make a copy) setiap akhir pekan.\n3. Jika spreadsheet tidak sengaja terhapus, Anda dapat memulihkannya dari menu 'Riwayat Versi' (Version history) Google Sheets atau mengimpor file backup JSON di atas.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
