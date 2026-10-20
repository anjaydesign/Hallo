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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsNotFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerMapScreen(
    viewModel: CustomerViewModel,
    onCustomerClick: (Customer) -> Unit
) {
    val context = LocalContext.current
    val rawCustomers by viewModel.rawCustomers.collectAsState()

    var mapFilter by remember { mutableStateOf("SEMUA") } // SEMUA, GPS, BELUM
    var searchQuery by remember { mutableStateOf("") }

    val totalMapped = remember(rawCustomers) {
        rawCustomers.count { it.latitude != null && it.longitude != null }
    }

    val filteredList = remember(rawCustomers, mapFilter, searchQuery) {
        rawCustomers.filter { cust ->
            val hasGps = cust.latitude != null && cust.longitude != null
            val matchGps = when (mapFilter) {
                "GPS" -> hasGps
                "BELUM" -> !hasGps
                else -> true
            }
            val matchQuery = searchQuery.isBlank() ||
                cust.nama.contains(searchQuery, ignoreCase = true) ||
                cust.alamat.contains(searchQuery, ignoreCase = true) ||
                cust.kodeHomepass.contains(searchQuery, ignoreCase = true) ||
                cust.paketWifi.contains(searchQuery, ignoreCase = true)

            matchGps && matchQuery
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Peta & Sebaran Pelanggan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Koordinat Terverifikasi & Navigasi Google Maps",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
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
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Summary Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MyRepRedPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Status Verifikasi Titik GPS",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Text(
                                text = "$totalMapped / ${rawCustomers.size} Terpetakan",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MyRepRedPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Teknisi dan sales dapat langsung membuka navigasi rute Google Maps ke lokasi rumah atau tiang Homepass pelanggan.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Search Bar & Filter Chips
            item {
                Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Cari lokasi, alamat, atau nama...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("SEMUA" to "Semua (${rawCustomers.size})", "GPS" to "Ada GPS ($totalMapped)", "BELUM" to "Belum Ada (${rawCustomers.size - totalMapped})").forEach { (code, label) ->
                            FilterChip(
                                selected = mapFilter == code,
                                onClick = { mapFilter = code },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MyRepRedPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Customer Geo Items
            items(filteredList, key = { it.id }) { customer ->
                MapCustomerItem(
                    customer = customer,
                    onCustomerClick = { onCustomerClick(customer) },
                    onOpenMaps = {
                        openMapsForCustomer(context, customer)
                    },
                    onWhatsAppClick = {
                        val msg = "Halo ${customer.nama}, teknisi MyRepublic sedang menuju ke lokasi pemasangan Anda di ${customer.alamat}."
                        val url = "https://api.whatsapp.com/send?phone=${customer.nomorHp}&text=${URLEncoder.encode(msg, "UTF-8")}"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Gagal membuka WhatsApp", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MapCustomerItem(
    customer: Customer,
    onCustomerClick: () -> Unit,
    onOpenMaps: () -> Unit,
    onWhatsAppClick: () -> Unit
) {
    val hasCoords = customer.latitude != null && customer.longitude != null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 5.dp)
            .clickable { onCustomerClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = customer.nama,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (hasCoords) Color(0xFFE8F5E9) else Color(0xFFFFF3E0))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (hasCoords) Icons.Default.GpsFixed else Icons.Default.GpsNotFixed,
                            contentDescription = null,
                            tint = if (hasCoords) Color(0xFF2E7D32) else Color(0xFFE65100),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (hasCoords) "Koordinat Terverifikasi" else "Belum Titik GPS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (hasCoords) Color(0xFF2E7D32) else Color(0xFFE65100)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${customer.paketWifi} • Homepass: ${customer.kodeHomepass.ifBlank { "Belum ada FAT" }}",
                fontSize = 12.sp,
                color = MyRepRedPrimary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = customer.alamat,
                fontSize = 11.sp,
                color = Color.DarkGray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (hasCoords) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "GPS: ${customer.latitude}, ${customer.longitude}",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PengerjaanBadge(status = customer.statusPengerjaan)
                    PembayaranBadge(status = customer.statusPembayaran)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onWhatsAppClick,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = "WhatsApp",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Button(
                        onClick = onOpenMaps,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                    ) {
                        Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (hasCoords) "Rute Maps" else "Cari di Maps", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

fun openMapsForCustomer(context: Context, customer: Customer) {
    try {
        val uri = if (customer.latitude != null && customer.longitude != null) {
            Uri.parse("geo:${customer.latitude},${customer.longitude}?q=${customer.latitude},${customer.longitude}(${URLEncoder.encode(customer.nama, "UTF-8")})")
        } else {
            Uri.parse("geo:0,0?q=${URLEncoder.encode(customer.alamat, "UTF-8")}")
        }
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Fallback to browser Google Maps
            val webUri = if (customer.latitude != null && customer.longitude != null) {
                Uri.parse("https://www.google.com/maps/search/?api=1&query=${customer.latitude},${customer.longitude}")
            } else {
                Uri.parse("https://www.google.com/maps/search/?api=1&query=${URLEncoder.encode(customer.alamat, "UTF-8")}")
            }
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Tidak dapat membuka aplikasi Google Maps", Toast.LENGTH_SHORT).show()
    }
}
