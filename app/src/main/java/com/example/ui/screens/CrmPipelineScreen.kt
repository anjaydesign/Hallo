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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
fun CrmPipelineScreen(
    viewModel: CustomerViewModel,
    onCustomerClick: (Customer) -> Unit
) {
    val context = LocalContext.current
    val rawCustomers by viewModel.rawCustomers.collectAsState()
    var selectedStageIndex by remember { mutableIntStateOf(0) }
    var pipelineSearchQuery by remember { mutableStateOf("") }

    val stages = Customer.ALL_PIPELINE_STAGES
    val currentStage = stages.getOrElse(selectedStageIndex) { Customer.PIPELINE_PROSPEK }

    // Hitung pelanggan per tahapan
    val stageCounts = remember(rawCustomers) {
        stages.associateWith { stage ->
            rawCustomers.count { it.pipelineSales.equals(stage, ignoreCase = true) }
        }
    }

    // Filter pelanggan sesuai tahapan aktif dan pencarian
    val customersInStage = remember(rawCustomers, currentStage, pipelineSearchQuery) {
        rawCustomers.filter { cust ->
            cust.pipelineSales.equals(currentStage, ignoreCase = true) &&
                (pipelineSearchQuery.isBlank() ||
                    cust.nama.contains(pipelineSearchQuery, ignoreCase = true) ||
                    cust.nomorHp.contains(pipelineSearchQuery, ignoreCase = true) ||
                    cust.alamat.contains(pipelineSearchQuery, ignoreCase = true) ||
                    cust.paketWifi.contains(pipelineSearchQuery, ignoreCase = true))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Pipeline CRM Sales",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Pemantauan Alur Pelanggan dari Prospek hingga Selesai",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
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
            // Scrollable Stage Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedStageIndex,
                edgePadding = 12.dp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                stages.forEachIndexed { idx, stage ->
                    val count = stageCounts[stage] ?: 0
                    Tab(
                        selected = selectedStageIndex == idx,
                        onClick = { selectedStageIndex = idx },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = stage,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedStageIndex == idx) FontWeight.Bold else FontWeight.Normal
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(
                                            if (selectedStageIndex == idx) MyRepRedPrimary else Color.Gray.copy(alpha = 0.4f)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "$count",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    )
                }
            }

            // Search Bar in Pipeline
            OutlinedTextField(
                value = pipelineSearchQuery,
                onValueChange = { pipelineSearchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                placeholder = { Text("Cari prospek di tahap $currentStage...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Stage Info Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFECEFF1))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tahap $currentStage (${customersInStage.size} Pelanggan)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF37474F)
                    )
                    Text(
                        text = "Gunakan tombol panah untuk alihkan status",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }

            // Customer List in Pipeline Stage
            if (customersInStage.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Tidak ada pelanggan di tahap $currentStage",
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(customersInStage, key = { it.id }) { customer ->
                        PipelineCustomerCard(
                            customer = customer,
                            onCardClick = { onCustomerClick(customer) },
                            onMovePrev = {
                                viewModel.movePipelineStage(customer.id, forward = false)
                                Toast.makeText(context, "Tahap dimundurkan", Toast.LENGTH_SHORT).show()
                            },
                            onMoveNext = {
                                viewModel.movePipelineStage(customer.id, forward = true)
                                Toast.makeText(context, "Tahap dimajukan", Toast.LENGTH_SHORT).show()
                            },
                            onWhatsAppClick = {
                                val msg = "Halo ${customer.nama}, kami dari MyRepublic ingin menindaklanjuti status pemasangan WiFi Anda."
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
    }
}

@Composable
fun PipelineCustomerCard(
    customer: Customer,
    onCardClick: () -> Unit,
    onMovePrev: () -> Unit,
    onMoveNext: () -> Unit,
    onWhatsAppClick: () -> Unit
) {
    val (score, _) = customer.getCompleteness()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
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
                        .background(if (score == 100) Color(0xFFE8F5E9) else Color(0xFFFFF3E0))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Dokumen $score%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (score == 100) Color(0xFF2E7D32) else Color(0xFFE65100)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${customer.paketWifi} • ${customer.nomorHp}",
                fontSize = 12.sp,
                color = MyRepRedPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = customer.alamat,
                fontSize = 11.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PengerjaanBadge(status = customer.statusPengerjaan)
                    PembayaranBadge(status = customer.statusPembayaran)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onWhatsAppClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = "WhatsApp",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = onMovePrev,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Mundur",
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Button(
                        onClick = onMoveNext,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MyRepRedPrimary)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Maju",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
