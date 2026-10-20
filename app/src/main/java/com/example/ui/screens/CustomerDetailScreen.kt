package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Customer
import com.example.ui.components.PembayaranBadge
import com.example.ui.components.PengerjaanBadge
import com.example.ui.components.PhotoPreviewDialog
import com.example.ui.theme.MyRepRedPrimary
import com.example.ui.viewmodel.CustomerViewModel
import java.io.File
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customer: Customer,
    viewModel: CustomerViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Customer) -> Unit
) {
    val context = LocalContext.current
    BackHandler { onNavigateBack() }

    var isNikRevealed by remember { mutableStateOf(false) }

    // Dialog state
    var showPengerjaanDialog by remember { mutableStateOf(false) }
    var showPembayaranDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPipelineDialog by remember { mutableStateOf(false) }
    var showWhatsAppDialog by remember { mutableStateOf(false) }

    // Preview photo dialog
    var previewPhotoPath by remember { mutableStateOf<String?>(null) }
    var previewPhotoTitle by remember { mutableStateOf("") }

    if (previewPhotoPath != null) {
        PhotoPreviewDialog(
            photoPath = previewPhotoPath!!,
            title = previewPhotoTitle,
            onDismiss = { previewPhotoPath = null }
        )
    }

    // Ubah Status Pengerjaan Dialog
    if (showPengerjaanDialog) {
        val nextPengerjaan = if (customer.statusPengerjaan == Customer.STATUS_DONE) Customer.STATUS_BAHAN else Customer.STATUS_DONE
        AlertDialog(
            onDismissRequest = { showPengerjaanDialog = false },
            title = { Text("Konfirmasi Ubah Status Pengerjaan") },
            text = {
                Text("Ubah status pengerjaan pelanggan ${customer.nama} dari ${customer.statusPengerjaan} menjadi $nextPengerjaan?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updatePengerjaanStatus(customer.id, nextPengerjaan) {
                            Toast.makeText(context, "Status pengerjaan diperbarui: $nextPengerjaan", Toast.LENGTH_SHORT).show()
                        }
                        showPengerjaanDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MyRepRedPrimary)
                ) {
                    Text("Ya, Ubah")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPengerjaanDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Ubah Status Pembayaran Dialog
    if (showPembayaranDialog) {
        val nextPembayaran = if (customer.statusPembayaran == Customer.STATUS_PAID) Customer.STATUS_UNPAID else Customer.STATUS_PAID
        AlertDialog(
            onDismissRequest = { showPembayaranDialog = false },
            title = { Text("Konfirmasi Status Pembayaran") },
            text = {
                Text("Ubah status pembayaran pelanggan ${customer.nama} dari ${customer.statusPembayaran} menjadi $nextPembayaran?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updatePembayaranStatus(customer.id, nextPembayaran) {
                            Toast.makeText(context, "Status pembayaran diperbarui: $nextPembayaran", Toast.LENGTH_SHORT).show()
                        }
                        showPembayaranDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MyRepRedPrimary)
                ) {
                    Text("Ya, Ubah")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPembayaranDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Ubah Pipeline Sales Dialog
    if (showPipelineDialog) {
        AlertDialog(
            onDismissRequest = { showPipelineDialog = false },
            title = { Text("Ubah Tahap Pipeline Sales") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Customer.ALL_PIPELINE_STAGES.forEach { stage ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    viewModel.updatePipelineStatus(customer.id, stage) {
                                        Toast.makeText(context, "Tahap pipeline diubah: $stage", Toast.LENGTH_SHORT).show()
                                    }
                                    showPipelineDialog = false
                                }
                                .padding(vertical = 8.dp, horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stage,
                                fontWeight = if (customer.pipelineSales == stage) FontWeight.Bold else FontWeight.Normal,
                                color = if (customer.pipelineSales == stage) MyRepRedPrimary else Color.DarkGray
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPipelineDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // WhatsApp Template Selector & Editor Dialog
    if (showWhatsAppDialog) {
        val (completeness, missing) = customer.getCompleteness()
        val missingStr = if (missing.isEmpty()) "Sudah Lengkap" else missing.joinToString(", ")

        val templates = listOf(
            "1. Konfirmasi Minat" to "Halo Bapak/Ibu ${customer.nama}, kami dari tim MyRepublic ingin mengonfirmasi permohonan pasang baru paket WiFi ${customer.paketWifi} (${customer.kecepatanSnapshot.ifBlank { "Broadband" }}). Apakah ada pertanyaan seputar promo atau paket?",
            "2. Pengingat Dokumen" to "Halo Bapak/Ibu ${customer.nama}, untuk memproses pemasangan WiFi MyRepublic, mohon bantuannya melengkapi berkas: $missingStr. Terima kasih!",
            "3. Konfirmasi Homepass" to "Halo Bapak/Ibu ${customer.nama}, teknisi MyRepublic sedang memeriksa jalur tiang FAT/Homepass terdekat di lokasi ${customer.alamat}. Mohon pastikan nomor ini aktif.",
            "4. Jadwal Pemasangan" to "Halo Bapak/Ibu ${customer.nama}, jadwal instalasi WiFi MyRepublic paket ${customer.paketWifi} telah diagendakan. Mohon dipastikan ada pihak keluarga di rumah."
        )

        var selectedTemplateIndex by remember { mutableStateOf(0) }
        var messageBody by remember { mutableStateOf(templates[0].second) }

        AlertDialog(
            onDismissRequest = { showWhatsAppDialog = false },
            title = { Text("Pilih & Edit Pesan WhatsApp") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pilih Template Pesan:", fontSize = 12.sp, color = Color.Gray)
                    templates.forEachIndexed { idx, (label, body) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selectedTemplateIndex == idx) MyRepRedPrimary.copy(alpha = 0.1f) else Color.Transparent)
                                .clickable {
                                    selectedTemplateIndex = idx
                                    messageBody = body
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTemplateIndex == idx) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTemplateIndex == idx) MyRepRedPrimary else Color.DarkGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Edit Pesan Sebelum Dikirim:", fontSize = 12.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = messageBody,
                        onValueChange = { messageBody = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        openWhatsAppWithText(context, customer.nomorHp, messageBody)
                        showWhatsAppDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kirim WhatsApp")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWhatsAppDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Hapus Pelanggan Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Pelanggan?") },
            text = {
                Text("Apakah Anda yakin ingin menghapus data pelanggan ${customer.nama} (${customer.id})? Tindakan ini tidak dapat dibatalkan.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCustomer(customer.id) {
                            Toast.makeText(context, "Data pelanggan berhasil dihapus", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detail Pelanggan",
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
                    IconButton(onClick = { shareCustomerReport(context, customer) }) {
                        Icon(Icons.Default.Share, contentDescription = "Bagikan", tint = Color.White)
                    }
                    IconButton(onClick = { onNavigateToEdit(customer) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MyRepRedPrimary)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val initial = customer.nama.firstOrNull()?.uppercaseChar()?.toString() ?: "P"
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(MyRepRedPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initial,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp,
                                    color = MyRepRedPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = customer.nama.ifBlank { "Tanpa Nama" },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = customer.id,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Pipeline Sales Stage
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Tahap Pipeline Sales:", fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFE0F2F1))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = customer.pipelineSales,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF00695C)
                                    )
                                }
                            }
                            OutlinedButton(
                                onClick = { showPipelineDialog = true },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ubah Pipeline", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Status pengerjaan toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Pengerjaan:", fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(2.dp))
                                PengerjaanBadge(status = customer.statusPengerjaan)
                            }
                            OutlinedButton(
                                onClick = { showPengerjaanDialog = true },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ubah Pengerjaan", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Status pembayaran toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Pembayaran:", fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(2.dp))
                                PembayaranBadge(status = customer.statusPembayaran)
                            }
                            OutlinedButton(
                                onClick = { showPembayaranDialog = true },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ubah Pembayaran", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Quick Actions: WhatsApp (dengan Template) & Google Maps
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showWhatsAppDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { openGoogleMaps(context, customer.alamat) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buka Maps", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Checklist Kelengkapan Dokumen & Survei
            item {
                val (score, missingList) = customer.getCompleteness()
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
                                text = "Checklist Kelengkapan Dokumen",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "$score%",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = if (score == 100) Color(0xFF2E7D32) else Color(0xFFF57C00)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { score / 100f },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                            color = if (score == 100) Color(0xFF2E7D32) else Color(0xFFF57C00),
                            trackColor = Color.LightGray.copy(alpha = 0.3f)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val checkItems = listOf(
                            "Nama & Nomor WhatsApp" to (customer.nama.isNotBlank() && customer.nomorHp.isNotBlank()),
                            "NIK 16 Digit" to (customer.nik.length == 16),
                            "Alamat Lengkap Pemasangan" to customer.alamat.isNotBlank(),
                            "Paket WiFi Pilihan" to customer.paketWifi.isNotBlank(),
                            "Foto KTP Pelanggan" to customer.fotoKtp.isNotBlank(),
                            "Foto Meteran Listrik" to customer.fotoMeteran.isNotBlank(),
                            "Foto Rumah Depan" to customer.fotoRumahDepan.isNotBlank(),
                            "Foto Sisi Kanan/Kiri" to (customer.fotoRumahKanan.isNotBlank() && customer.fotoRumahKiri.isNotBlank()),
                            "Data Titik Homepass / FAT" to (customer.fotoHomepass.isNotBlank() || customer.kodeHomepass.isNotBlank())
                        )

                        checkItems.forEach { (label, isOk) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isOk) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (isOk) Color(0xFF2E7D32) else Color(0xFFE57373),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    color = if (isOk) MaterialTheme.colorScheme.onSurface else Color.Gray,
                                    fontWeight = if (isOk) FontWeight.Medium else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Data Pelanggan Details Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Informasi Pemasangan & Paket",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        DetailRow(label = "Paket WiFi", value = customer.paketWifi.ifBlank { "Standar" })
                        if (customer.kecepatanSnapshot.isNotBlank()) {
                            DetailRow(label = "Kecepatan Paket", value = customer.kecepatanSnapshot)
                        }
                        if (customer.hargaSnapshot.isNotBlank()) {
                            DetailRow(label = "Harga Bulanan (Snapshot)", value = customer.hargaSnapshot)
                        }
                        if (customer.promoSnapshot.isNotBlank()) {
                            DetailRow(label = "Promo (Snapshot)", value = customer.promoSnapshot)
                        }

                        DetailRow(label = "Nomor HP / WA", value = customer.nomorHp)
                        DetailRow(label = "Email", value = customer.email.ifBlank { "-" })

                        // NIK with toggle reveal
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("NIK Pelanggan", fontSize = 12.sp, color = Color.Gray)
                                Text(
                                    text = if (isNikRevealed) customer.nik else Customer.maskNik(customer.nik),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                            IconButton(onClick = { isNikRevealed = !isNikRevealed }) {
                                Icon(
                                    imageVector = if (isNikRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Tampilkan NIK"
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                        DetailRow(label = "Alamat Pemasangan", value = customer.alamat)
                        DetailRow(label = "Kode Homepass", value = customer.kodeHomepass.ifBlank { "-" })
                        DetailRow(label = "Alamat Homepass", value = customer.alamatHomepass.ifBlank { customer.alamat })
                        DetailRow(label = "Jadwal Follow-Up", value = customer.jadwalFollowUp.ifBlank { "Belum Dijadwalkan" })
                        if (customer.catatanFollowUp.isNotBlank()) {
                            DetailRow(label = "Catatan Follow-Up", value = customer.catatanFollowUp)
                        }
                        DetailRow(label = "Tanggal Input", value = customer.tanggalInput)
                        DetailRow(label = "Terakhir Diperbarui", value = customer.terakhirDiperbarui)
                        DetailRow(label = "Catatan", value = customer.catatan.ifBlank { "-" })
                    }
                }
            }

            // Dokumentasi Foto Lapangan (Grid 6 Foto)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Dokumentasi Persyaratan Foto (6)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Ketuk foto mana saja untuk memperbesar (Full Preview).",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val photos = listOf(
                            "Foto KTP" to customer.fotoKtp,
                            "Meteran Listrik" to customer.fotoMeteran,
                            "Rumah Depan" to customer.fotoRumahDepan,
                            "Samping Kanan" to customer.fotoRumahKanan,
                            "Samping Kiri" to customer.fotoRumahKiri,
                            "Homepass (FAT)" to customer.fotoHomepass
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (i in photos.indices step 2) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val first = photos[i]
                                    Box(modifier = Modifier.weight(1f)) {
                                        PhotoThumbnailCard(
                                            title = first.first,
                                            path = first.second,
                                            onClick = {
                                                if (first.second.isNotBlank()) {
                                                    previewPhotoTitle = first.first
                                                    previewPhotoPath = first.second
                                                }
                                            }
                                        )
                                    }
                                    if (i + 1 < photos.size) {
                                        val second = photos[i + 1]
                                        Box(modifier = Modifier.weight(1f)) {
                                            PhotoThumbnailCard(
                                                title = second.first,
                                                path = second.second,
                                                onClick = {
                                                    if (second.second.isNotBlank()) {
                                                        previewPhotoTitle = second.first
                                                        previewPhotoPath = second.second
                                                    }
                                                }
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
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

@Composable
fun PhotoThumbnailCard(
    title: String,
    path: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable(enabled = path.isNotBlank()) { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                if (path.isNotBlank()) {
                    val model: Any = if (path.startsWith("http://") || path.startsWith("https://")) {
                        path
                    } else {
                        File(path)
                    }

                    AsyncImage(
                        model = model,
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray)
                        Text("Belum Ada Foto", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                maxLines = 1
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

fun openWhatsAppWithText(context: Context, rawPhone: String, message: String) {
    try {
        var cleanPhone = rawPhone.replace(Regex("[^0-9]"), "")
        if (cleanPhone.startsWith("0")) {
            cleanPhone = "62" + cleanPhone.substring(1)
        }
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${URLEncoder.encode(message, "UTF-8")}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Tidak dapat membuka WhatsApp", Toast.LENGTH_SHORT).show()
    }
}

fun openGoogleMaps(context: Context, address: String) {
    try {
        val gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(address))
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(address))
            context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal membuka peta: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}

fun shareCustomerReport(context: Context, c: Customer) {
    val (score, _) = c.getCompleteness()
    val report = """
=== DATA PELANGGAN MYREPUBLIC ===
ID Pelanggan: ${c.id}
Nama: ${c.nama}
No. HP: ${c.nomorHp}
Paket: ${c.paketWifi} (${c.kecepatanSnapshot})
Harga: ${c.hargaSnapshot}
Alamat: ${c.alamat}
Homepass: ${c.kodeHomepass} (${c.alamatHomepass})
Pipeline: ${c.pipelineSales}
Kelengkapan Berkas: $score%
Status Pengerjaan: ${c.statusPengerjaan}
Status Pembayaran: ${c.statusPembayaran}
Jadwal Follow-Up: ${c.jadwalFollowUp.ifBlank { "-" }}
Tanggal Input: ${c.tanggalInput}
Catatan: ${c.catatan.ifBlank { "-" }}
Terakhir Diperbarui: ${c.terakhirDiperbarui}
=================================
""".trimIndent()

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, report)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Bagikan Laporan Pelanggan"))
}
