package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import kotlinx.coroutines.launch
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.ui.components.PembayaranBadge
import com.example.ui.components.PengerjaanBadge
import com.example.ui.components.PhotoPickerField
import com.example.ui.theme.MyRepRedPrimary
import com.example.ui.viewmodel.CustomerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerFormScreen(
    viewModel: CustomerViewModel,
    existingCustomer: Customer? = null,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current
    val wifiPackages by viewModel.wifiPackages.collectAsState()
    val syncState by viewModel.syncState.collectAsState()

    BackHandler {
        onNavigateBack()
    }

    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    // 19 Fields state + Versi 3 Fields
    var customerId by remember { mutableStateOf(existingCustomer?.id ?: Customer.generateCustomerId()) }
    var tanggalInput by remember { mutableStateOf(existingCustomer?.tanggalInput ?: Customer.currentTimestampFormatted()) }
    var nama by remember { mutableStateOf(existingCustomer?.nama ?: "") }
    var nomorHp by remember { mutableStateOf(existingCustomer?.nomorHp ?: "") }
    var alamat by remember { mutableStateOf(existingCustomer?.alamat ?: "") }
    var email by remember { mutableStateOf(existingCustomer?.email ?: "") }
    var nik by remember { mutableStateOf(existingCustomer?.nik ?: "") }

    var paketWifi by remember {
        mutableStateOf(
            existingCustomer?.paketWifi ?: wifiPackages.firstOrNull { it.isActive }?.name ?: "Fast 50 Mbps"
        )
    }

    var fotoKtp by remember { mutableStateOf(existingCustomer?.fotoKtp ?: "") }
    var fotoMeteran by remember { mutableStateOf(existingCustomer?.fotoMeteran ?: "") }
    var fotoRumahDepan by remember { mutableStateOf(existingCustomer?.fotoRumahDepan ?: "") }
    var fotoRumahKanan by remember { mutableStateOf(existingCustomer?.fotoRumahKanan ?: "") }
    var fotoRumahKiri by remember { mutableStateOf(existingCustomer?.fotoRumahKiri ?: "") }
    var fotoHomepass by remember { mutableStateOf(existingCustomer?.fotoHomepass ?: "") }

    var kodeHomepass by remember { mutableStateOf(existingCustomer?.kodeHomepass ?: "") }
    var alamatHomepass by remember { mutableStateOf(existingCustomer?.alamatHomepass ?: "") }
    var isHomepassAlamatSama by remember {
        mutableStateOf(existingCustomer == null || (existingCustomer.alamatHomepass == existingCustomer.alamat))
    }

    var statusPengerjaan by remember { mutableStateOf(existingCustomer?.statusPengerjaan ?: Customer.STATUS_BAHAN) }
    var statusPembayaran by remember { mutableStateOf(existingCustomer?.statusPembayaran ?: Customer.STATUS_UNPAID) }
    var catatan by remember { mutableStateOf(existingCustomer?.catatan ?: "") }

    // Versi 3 Fields
    var pipelineSales by remember { mutableStateOf(existingCustomer?.pipelineSales ?: Customer.PIPELINE_PROSPEK) }
    var jadwalFollowUp by remember { mutableStateOf(existingCustomer?.jadwalFollowUp ?: "") }
    var catatanFollowUp by remember { mutableStateOf(existingCustomer?.catatanFollowUp ?: "") }

    // Duplicate Detection Dialog State
    var duplicateMatches by remember { mutableStateOf<List<Customer>>(emptyList()) }
    var showDuplicateDialog by remember { mutableStateOf(false) }

    // Validation Errors
    var namaError by remember { mutableStateOf(false) }
    var nomorHpError by remember { mutableStateOf(false) }
    var alamatError by remember { mutableStateOf(false) }
    var nikError by remember { mutableStateOf(false) }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("1. Identitas", "2. Paket & Alamat", "3. Dokumentasi", "4. Status & CRM")

    fun validateForm(): Boolean {
        var isValid = true
        namaError = nama.isBlank()
        if (namaError) isValid = false

        nomorHpError = nomorHp.isBlank()
        if (nomorHpError) isValid = false

        alamatError = alamat.isBlank()
        if (alamatError) isValid = false

        nikError = nik.length != 16 || !nik.all { it.isDigit() }
        if (nikError) isValid = false

        return isValid
    }

    fun doActualSave() {
        val finalAlamatHomepass = if (isHomepassAlamatSama) alamat else alamatHomepass
        val selectedPkgObj = wifiPackages.find { it.name.equals(paketWifi, ignoreCase = true) }

        val customerToSave = Customer(
            id = customerId,
            tanggalInput = tanggalInput,
            nama = nama.trim(),
            nomorHp = nomorHp.trim(),
            alamat = alamat.trim(),
            email = email.trim(),
            nik = nik.trim(),
            fotoKtp = fotoKtp,
            paketWifi = paketWifi,
            kecepatanSnapshot = selectedPkgObj?.speed ?: "",
            hargaSnapshot = selectedPkgObj?.priceEstimate ?: "",
            promoSnapshot = selectedPkgObj?.promo ?: "",
            fotoMeteran = fotoMeteran,
            fotoRumahDepan = fotoRumahDepan,
            fotoRumahKanan = fotoRumahKanan,
            fotoRumahKiri = fotoRumahKiri,
            fotoHomepass = fotoHomepass,
            kodeHomepass = kodeHomepass.trim(),
            alamatHomepass = finalAlamatHomepass.trim(),
            statusPengerjaan = statusPengerjaan,
            statusPembayaran = statusPembayaran,
            pipelineSales = pipelineSales,
            jadwalFollowUp = jadwalFollowUp.trim(),
            catatanFollowUp = catatanFollowUp.trim(),
            catatan = catatan.trim(),
            terakhirDiperbarui = Customer.currentTimestampFormatted(),
            updatedBy = viewModel.appSettings.petugasName
        )

        viewModel.saveCustomer(
            customer = customerToSave,
            onSuccess = {
                Toast.makeText(context, "Data pelanggan berhasil disimpan!", Toast.LENGTH_SHORT).show()
                onSaveSuccess()
            },
            onError = { errMsg ->
                Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show()
            }
        )
    }

    fun submitCustomer() {
        if (!validateForm()) {
            Toast.makeText(context, "Harap lengkapi semua kolom wajib dengan benar", Toast.LENGTH_SHORT).show()
            return
        }

        // Deteksi kemungkinan pelanggan duplikat (NIK, No HP, atau Kode Homepass)
        coroutineScope.launch {
            val dups = viewModel.checkDuplicates(nik, nomorHp, kodeHomepass, customerId)
            if (dups.isNotEmpty()) {
                duplicateMatches = dups
                showDuplicateDialog = true
            } else {
                doActualSave()
            }
        }
    }

    // Modal Konfirmasi Duplikasi
    if (showDuplicateDialog) {
        val dupNames = duplicateMatches.joinToString(", ") { "${it.nama} (${it.id})" }
        AlertDialog(
            onDismissRequest = { showDuplicateDialog = false },
            title = { Text("Peringatan: Potensi Duplikat Terdeteksi", color = Color(0xFFC62828)) },
            text = {
                Text("Ditemukan data pelanggan dengan NIK, Nomor HP, atau Kode Homepass serupa:\n\n• $dupNames\n\nApakah Anda yakin ingin tetap menyimpan data pelanggan ini?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDuplicateDialog = false
                        doActualSave()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MyRepRedPrimary)
                ) {
                    Text("Tetap Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDuplicateDialog = false }) {
                    Text("Periksa Kembali")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (existingCustomer == null) "Tambah Pelanggan Baru" else "Edit Data Pelanggan",
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
                actions = {
                    IconButton(
                        onClick = { submitCustomer() },
                        enabled = !syncState.isSyncing
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = "Simpan",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MyRepRedPrimary)
            )
        },
        bottomBar = {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (selectedTabIndex > 0) {
                        OutlinedButton(
                            onClick = { selectedTabIndex -= 1 },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Sebelumnya")
                        }
                    } else {
                        OutlinedButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Batal")
                        }
                    }

                    if (selectedTabIndex < tabTitles.size - 1) {
                        Button(
                            onClick = { selectedTabIndex += 1 },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MyRepRedPrimary)
                        ) {
                            Text("Lanjut")
                        }
                    } else {
                        Button(
                            onClick = { submitCustomer() },
                            modifier = Modifier.weight(1f),
                            enabled = !syncState.isSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = MyRepRedPrimary)
                        ) {
                            if (syncState.isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Simpan Data")
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Section Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 12.dp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Syncing Indicator Overlay
            if (syncState.isSyncing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MyRepRedPrimary.copy(alpha = 0.1f))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = syncState.progressMessage,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MyRepRedPrimary
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> {
                        // TAB 1: IDENTITAS PELANGGAN
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Informasi Sistem",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // 1. ID Pelanggan (Otomatis & Unik)
                                    OutlinedTextField(
                                        value = customerId,
                                        onValueChange = { },
                                        readOnly = true,
                                        label = { Text("ID Pelanggan (Otomatis & Unik)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // 2. Tanggal Input (Otomatis)
                                    OutlinedTextField(
                                        value = tanggalInput,
                                        onValueChange = { },
                                        readOnly = true,
                                        label = { Text("Tanggal & Waktu Input (Otomatis)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Data Pribadi Pelanggan",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // 3. Nama Lengkap
                                    OutlinedTextField(
                                        value = nama,
                                        onValueChange = {
                                            nama = it
                                            if (namaError) namaError = false
                                        },
                                        label = { Text("Nama Lengkap Pelanggan *") },
                                        isError = namaError,
                                        supportingText = { if (namaError) Text("Nama lengkap wajib diisi") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // 4. Nomor HP / WhatsApp
                                    OutlinedTextField(
                                        value = nomorHp,
                                        onValueChange = {
                                            nomorHp = it
                                            if (nomorHpError) nomorHpError = false
                                        },
                                        label = { Text("Nomor HP / WhatsApp *") },
                                        placeholder = { Text("Contoh: 081234567890") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        isError = nomorHpError,
                                        supportingText = { if (nomorHpError) Text("Nomor HP/WA wajib diisi") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // 6. Email Pelanggan
                                    OutlinedTextField(
                                        value = email,
                                        onValueChange = { email = it },
                                        label = { Text("Email Pelanggan (Opsional)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // 7. NIK Pelanggan (16 Digit)
                                    OutlinedTextField(
                                        value = nik,
                                        onValueChange = {
                                            if (it.length <= 16) {
                                                nik = it
                                                if (nikError) nikError = false
                                            }
                                        },
                                        label = { Text("NIK Pelanggan (16 Digit) *") },
                                        placeholder = { Text("320101...") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        isError = nikError,
                                        supportingText = {
                                            if (nikError) {
                                                Text("NIK harus tepat 16 digit angka (Sekarang: ${nik.length} digit)")
                                            } else {
                                                Text("Panjang: ${nik.length}/16 digit. NIK disamarkan di daftar umum.")
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        // TAB 2: PAKET WIFI & ALAMAT
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Paket WiFi MyRepublic",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // 9. Paket WiFi Dropdown
                                    var packageDropdownExpanded by remember { mutableStateOf(false) }
                                    ExposedDropdownMenuBox(
                                        expanded = packageDropdownExpanded,
                                        onExpandedChange = { packageDropdownExpanded = !packageDropdownExpanded }
                                    ) {
                                        OutlinedTextField(
                                            value = paketWifi,
                                            onValueChange = { },
                                            readOnly = true,
                                            label = { Text("Paket WiFi Pilihan *") },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = packageDropdownExpanded) },
                                            modifier = Modifier
                                                .menuAnchor()
                                                .fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp)
                                        )

                                        ExposedDropdownMenu(
                                            expanded = packageDropdownExpanded,
                                            onDismissRequest = { packageDropdownExpanded = false }
                                        ) {
                                            wifiPackages.filter { it.isActive }.forEach { pkg ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Column {
                                                            Text(pkg.name, fontWeight = FontWeight.Bold)
                                                            Text(
                                                                "${pkg.speed} • ${pkg.priceEstimate}",
                                                                fontSize = 12.sp,
                                                                color = Color.Gray
                                                            )
                                                            if (pkg.promo.isNotBlank()) {
                                                                Text(
                                                                    "Promo: ${pkg.promo}",
                                                                    fontSize = 11.sp,
                                                                    color = MyRepRedPrimary
                                                                )
                                                            }
                                                        }
                                                    },
                                                    onClick = {
                                                        paketWifi = pkg.name
                                                        packageDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // Snapshot info kartu paket terpilih
                                    val currentPkg = wifiPackages.find { it.name.equals(paketWifi, ignoreCase = true) }
                                    if (currentPkg != null) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .padding(10.dp)
                                        ) {
                                            Column {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = "Kecepatan: ${currentPkg.speed}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                    Text(
                                                        text = currentPkg.priceEstimate,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 13.sp,
                                                        color = MyRepRedPrimary
                                                    )
                                                }
                                                if (currentPkg.promo.isNotBlank()) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "Promo: ${currentPkg.promo}",
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF2E7D32),
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                                Text(
                                                    text = "*Harga data awal brosur, perlu konfirmasi saat verifikasi.",
                                                    fontSize = 10.sp,
                                                    color = Color.Gray
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
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Alamat Pemasangan",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // 5. Alamat Lengkap Pemasangan
                                    OutlinedTextField(
                                        value = alamat,
                                        onValueChange = {
                                            alamat = it
                                            if (alamatError) alamatError = false
                                        },
                                        label = { Text("Alamat Lengkap Pemasangan *") },
                                        placeholder = { Text("Nama Jalan, Nomor Rumah, RT/RW, Kelurahan, Kecamatan, Kota") },
                                        isError = alamatError,
                                        supportingText = { if (alamatError) Text("Alamat pemasangan wajib diisi") },
                                        minLines = 3,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Data Homepass (FAT / ODP)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // 15. Kode Homepass
                                    OutlinedTextField(
                                        value = kodeHomepass,
                                        onValueChange = { kodeHomepass = it },
                                        label = { Text("Nomor / Kode Homepass (FAT)") },
                                        placeholder = { Text("Contoh: HP-BDG-012 / FAT-84") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Checkbox alamat homepass sama dengan pemasangan
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        Checkbox(
                                            checked = isHomepassAlamatSama,
                                            onCheckedChange = { isHomepassAlamatSama = it }
                                        )
                                        Text(
                                            text = "Alamat Homepass sama dengan alamat pemasangan",
                                            fontSize = 13.sp
                                        )
                                    }

                                    if (!isHomepassAlamatSama) {
                                        // 16. Alamat Homepass berbeda
                                        OutlinedTextField(
                                            value = alamatHomepass,
                                            onValueChange = { alamatHomepass = it },
                                            label = { Text("Alamat Titik Homepass") },
                                            placeholder = { Text("Titik tiang atau lokasi tiang FAT") },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // TAB 3: DOKUMENTASI FOTO LENGKAP (6 Foto)
                        item {
                            Text(
                                text = "Persyaratan Foto Lapangan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Foto otomatis dikompresi agar tulisan KTP dan meteran tetap tajam namun ukuran file hemat.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // 8. Foto KTP
                        item {
                            PhotoPickerField(
                                title = "8. Foto KTP Pelanggan",
                                description = "Pastikan NIK dan nama terbaca jelas",
                                photoPath = fotoKtp,
                                targetType = "ktp",
                                isRequired = true,
                                onPhotoSelected = { uri ->
                                    val saved = viewModel.processPhoto(uri)
                                    if (saved != null) fotoKtp = saved
                                },
                                onPhotoCleared = { fotoKtp = "" }
                            )
                        }

                        // 10. Foto Meteran Listrik
                        item {
                            PhotoPickerField(
                                title = "10. Foto Meteran Listrik",
                                description = "Foto nomor ID PLN / kwh meter",
                                photoPath = fotoMeteran,
                                targetType = "meteran",
                                onPhotoSelected = { uri ->
                                    val saved = viewModel.processPhoto(uri)
                                    if (saved != null) fotoMeteran = saved
                                },
                                onPhotoCleared = { fotoMeteran = "" }
                            )
                        }

                        // 11. Foto Rumah Depan
                        item {
                            PhotoPickerField(
                                title = "11. Foto Rumah Tampak Depan",
                                description = "Tampak nomor rumah dan fasad depan",
                                photoPath = fotoRumahDepan,
                                targetType = "rumah",
                                onPhotoSelected = { uri ->
                                    val saved = viewModel.processPhoto(uri)
                                    if (saved != null) fotoRumahDepan = saved
                                },
                                onPhotoCleared = { fotoRumahDepan = "" }
                            )
                        }

                        // 12. Foto Rumah Samping Kanan
                        item {
                            PhotoPickerField(
                                title = "12. Foto Rumah Samping Kanan",
                                description = "Sudut pandang sisi kanan rumah",
                                photoPath = fotoRumahKanan,
                                targetType = "rumah",
                                onPhotoSelected = { uri ->
                                    val saved = viewModel.processPhoto(uri)
                                    if (saved != null) fotoRumahKanan = saved
                                },
                                onPhotoCleared = { fotoRumahKanan = "" }
                            )
                        }

                        // 13. Foto Rumah Samping Kiri
                        item {
                            PhotoPickerField(
                                title = "13. Foto Rumah Samping Kiri",
                                description = "Sudut pandang sisi kiri rumah",
                                photoPath = fotoRumahKiri,
                                targetType = "rumah",
                                onPhotoSelected = { uri ->
                                    val saved = viewModel.processPhoto(uri)
                                    if (saved != null) fotoRumahKiri = saved
                                },
                                onPhotoCleared = { fotoRumahKiri = "" }
                            )
                        }

                        // 14. Foto Homepass
                        item {
                            PhotoPickerField(
                                title = "14. Foto Titik Homepass (FAT / ODP)",
                                description = "Tiang FAT, kabel drop core, atau box homepass",
                                photoPath = fotoHomepass,
                                targetType = "homepass",
                                onPhotoSelected = { uri ->
                                    val saved = viewModel.processPhoto(uri)
                                    if (saved != null) fotoHomepass = saved
                                },
                                onPhotoCleared = { fotoHomepass = "" }
                            )
                        }
                    }

                    3 -> {
                        // TAB 4: STATUS PENGERJAAN, STATUS PEMBAYARAN & CATATAN
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "17. Status Pengerjaan",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        PengerjaanBadge(status = statusPengerjaan)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Pelanggan baru otomatis berstatus BAHAN. Ubah ke DONE jika pengerjaan instalasi teknisi sudah selesai.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            RadioButton(
                                                selected = statusPengerjaan == Customer.STATUS_BAHAN,
                                                onClick = { statusPengerjaan = Customer.STATUS_BAHAN }
                                            )
                                            Text("BAHAN", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            RadioButton(
                                                selected = statusPengerjaan == Customer.STATUS_DONE,
                                                onClick = { statusPengerjaan = Customer.STATUS_DONE }
                                            )
                                            Text("DONE", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "18. Status Pembayaran",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        PembayaranBadge(status = statusPembayaran)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Pelanggan baru otomatis berstatus UNPAID. Ubah ke PAID jika konfirmasi pembayaran tagihan pertama telah valid.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            RadioButton(
                                                selected = statusPembayaran == Customer.STATUS_UNPAID,
                                                onClick = { statusPembayaran = Customer.STATUS_UNPAID }
                                            )
                                            Text("UNPAID", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            RadioButton(
                                                selected = statusPembayaran == Customer.STATUS_PAID,
                                                onClick = { statusPembayaran = Customer.STATUS_PAID }
                                            )
                                            Text("PAID", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Tahap Pipeline Sales CRM",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    var pipelineExpanded by remember { mutableStateOf(false) }
                                    ExposedDropdownMenuBox(
                                        expanded = pipelineExpanded,
                                        onExpandedChange = { pipelineExpanded = !pipelineExpanded }
                                    ) {
                                        OutlinedTextField(
                                            value = pipelineSales,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Tahapan Prospek / Pipeline") },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pipelineExpanded) },
                                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp)
                                        )

                                        ExposedDropdownMenu(
                                            expanded = pipelineExpanded,
                                            onDismissRequest = { pipelineExpanded = false }
                                        ) {
                                            Customer.ALL_PIPELINE_STAGES.forEach { stage ->
                                                DropdownMenuItem(
                                                    text = { Text(stage, fontWeight = FontWeight.SemiBold) },
                                                    onClick = {
                                                        pipelineSales = stage
                                                        pipelineExpanded = false
                                                    }
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
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Pengingat Jadwal Follow-Up",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = jadwalFollowUp,
                                        onValueChange = { jadwalFollowUp = it },
                                        label = { Text("Tanggal Follow-Up (YYYY-MM-DD)") },
                                        placeholder = { Text("Contoh: 2026-10-22") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Quick date presets
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val now = java.util.Calendar.getInstance()
                                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())

                                        OutlinedButton(
                                            onClick = { jadwalFollowUp = sdf.format(now.time) },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Hari Ini", fontSize = 10.sp)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                val c = java.util.Calendar.getInstance()
                                                c.add(java.util.Calendar.DAY_OF_YEAR, 1)
                                                jadwalFollowUp = sdf.format(c.time)
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Besok", fontSize = 10.sp)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                val c = java.util.Calendar.getInstance()
                                                c.add(java.util.Calendar.DAY_OF_YEAR, 3)
                                                jadwalFollowUp = sdf.format(c.time)
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("+3 Hari", fontSize = 10.sp)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                val c = java.util.Calendar.getInstance()
                                                c.add(java.util.Calendar.DAY_OF_YEAR, 7)
                                                jadwalFollowUp = sdf.format(c.time)
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("+1 Mgg", fontSize = 10.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = catatanFollowUp,
                                        onValueChange = { catatanFollowUp = it },
                                        label = { Text("Catatan Rencana Follow-Up") },
                                        placeholder = { Text("Contoh: Telepon sore hari setelah jam kerja...") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Catatan Tambahan",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = catatan,
                                        onValueChange = { catatan = it },
                                        label = { Text("Catatan Teknisi / Sales") },
                                        placeholder = { Text("Contoh: Tarikan kabel butuh tiang tambahan 20 meter...") },
                                        minLines = 3,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
