package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.AppSettings
import com.example.data.model.Customer
import com.example.data.model.WifiPackage
import com.example.data.remote.GoogleSheetsSyncService
import com.example.data.remote.SyncResult
import com.example.data.repository.CustomerRepository
import com.example.util.ImageCompressionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import org.json.JSONArray
import org.json.JSONObject

data class DashboardStats(
    val total: Int = 0,
    val totalBahan: Int = 0,
    val totalDone: Int = 0,
    val totalPaid: Int = 0,
    val totalUnpaid: Int = 0
)

data class TargetProgress(
    val dailyCount: Int = 0,
    val dailyTarget: Int = 3,
    val weeklyCount: Int = 0,
    val weeklyTarget: Int = 15,
    val monthlyCount: Int = 0,
    val monthlyTarget: Int = 50
)

data class SyncUiState(
    val isSyncing: Boolean = false,
    val progressMessage: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null
)

data class SalesLeaderboardItem(
    val agentName: String,
    val totalCount: Int,
    val doneCount: Int,
    val paidCount: Int,
    val closingRate: Int
)

data class DuplicateAuditItem(
    val duplicateType: String, // "NIK", "Nomor HP", "Kode Homepass"
    val duplicateValue: String,
    val customers: List<Customer>
)

class CustomerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val syncService = GoogleSheetsSyncService()
    val appSettings = AppSettings(application)
    val repository = CustomerRepository(application, db.customerDao(), syncService, appSettings)

    // Raw Customers list from DB
    val rawCustomers: StateFlow<List<Customer>> = repository.getCustomersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search and Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _pengerjaanFilter = MutableStateFlow("SEMUA") // SEMUA, BAHAN, DONE
    val pengerjaanFilter: StateFlow<String> = _pengerjaanFilter.asStateFlow()

    private val _pembayaranFilter = MutableStateFlow("SEMUA") // SEMUA, UNPAID, PAID
    val pembayaranFilter: StateFlow<String> = _pembayaranFilter.asStateFlow()

    private val _pipelineFilter = MutableStateFlow("SEMUA") // SEMUA, PROSPEK, etc.
    val pipelineFilter: StateFlow<String> = _pipelineFilter.asStateFlow()

    private val _completenessFilter = MutableStateFlow("SEMUA") // SEMUA, LENGKAP, BELUM
    val completenessFilter: StateFlow<String> = _completenessFilter.asStateFlow()

    // Filtered Customer List
    val filteredCustomers: StateFlow<List<Customer>> = combine(
        rawCustomers,
        _searchQuery,
        _pengerjaanFilter,
        _pembayaranFilter,
        _pipelineFilter
    ) { list, query, pengerjaan, pembayaran, pipeline ->
        val completeFilter = _completenessFilter.value
        list.filter { cust ->
            val matchQuery = query.isBlank() ||
                cust.nama.contains(query, ignoreCase = true) ||
                cust.nomorHp.contains(query, ignoreCase = true) ||
                cust.alamat.contains(query, ignoreCase = true) ||
                cust.kodeHomepass.contains(query, ignoreCase = true) ||
                cust.nik.contains(query, ignoreCase = true) ||
                cust.paketWifi.contains(query, ignoreCase = true)

            val matchPengerjaan = when (pengerjaan) {
                Customer.STATUS_BAHAN -> cust.statusPengerjaan == Customer.STATUS_BAHAN
                Customer.STATUS_DONE -> cust.statusPengerjaan == Customer.STATUS_DONE
                else -> true
            }

            val matchPembayaran = when (pembayaran) {
                Customer.STATUS_PAID -> cust.statusPembayaran == Customer.STATUS_PAID
                Customer.STATUS_UNPAID -> cust.statusPembayaran == Customer.STATUS_UNPAID
                else -> true
            }

            val matchPipeline = if (pipeline == "SEMUA") true else cust.pipelineSales.equals(pipeline, ignoreCase = true)

            val matchCompleteness = when (completeFilter) {
                "LENGKAP" -> cust.isComplete()
                "BELUM" -> !cust.isComplete()
                else -> true
            }

            matchQuery && matchPengerjaan && matchPembayaran && matchPipeline && matchCompleteness
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard Statistics
    val stats: StateFlow<DashboardStats> = rawCustomers.map { list ->
        DashboardStats(
            total = list.size,
            totalBahan = list.count { it.statusPengerjaan == Customer.STATUS_BAHAN },
            totalDone = list.count { it.statusPengerjaan == Customer.STATUS_DONE },
            totalPaid = list.count { it.statusPembayaran == Customer.STATUS_PAID },
            totalUnpaid = list.count { it.statusPembayaran == Customer.STATUS_UNPAID }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    // Target Progress (Harian, Mingguan, Bulanan)
    val targetProgress: StateFlow<TargetProgress> = rawCustomers.map { list ->
        val now = Calendar.getInstance()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now.time)

        now.set(Calendar.DAY_OF_WEEK, now.firstDayOfWeek)
        val startOfWeek = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now.time)

        val thisMonthPrefix = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

        val daily = list.count { it.tanggalInput.startsWith(todayStr) }
        val weekly = list.count { it.tanggalInput.take(10) >= startOfWeek }
        val monthly = list.count { it.tanggalInput.startsWith(thisMonthPrefix) }

        TargetProgress(
            dailyCount = daily,
            dailyTarget = appSettings.dailySalesTarget,
            weeklyCount = weekly,
            weeklyTarget = appSettings.weeklySalesTarget,
            monthlyCount = monthly,
            monthlyTarget = appSettings.monthlySalesTarget
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TargetProgress())

    // Follow-Up Lists
    val followUpTodayList: StateFlow<List<Customer>> = rawCustomers.map { list ->
        list.filter { it.isFollowUpToday() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val followUpOverdueList: StateFlow<List<Customer>> = rawCustomers.map { list ->
        list.filter { it.isFollowUpOverdue() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Versi 3: Analitik Distribusi Paket WiFi
    val packageDistribution: StateFlow<Map<String, Int>> = rawCustomers.map { list ->
        list.groupBy { it.paketWifi.ifBlank { "Lainnya" } }
            .mapValues { it.value.size }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Versi 3: Analitik Distribusi Pipeline CRM
    val pipelineDistribution: StateFlow<Map<String, Int>> = rawCustomers.map { list ->
        Customer.ALL_PIPELINE_STAGES.associateWith { stage ->
            list.count { it.pipelineSales.equals(stage, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Versi 3: Peringkat Kinerja Sales / Leaderboard
    val salesLeaderboard: StateFlow<List<SalesLeaderboardItem>> = rawCustomers.map { list ->
        list.groupBy { it.updatedBy.ifBlank { "Petugas Lapangan" } }
            .map { (agent, custList) ->
                val total = custList.size
                val done = custList.count { it.statusPengerjaan == Customer.STATUS_DONE }
                val paid = custList.count { it.statusPembayaran == Customer.STATUS_PAID }
                val rate = if (total > 0) ((done.toFloat() / total) * 100).toInt() else 0
                SalesLeaderboardItem(
                    agentName = agent,
                    totalCount = total,
                    doneCount = done,
                    paidCount = paid,
                    closingRate = rate
                )
            }
            .sortedWith(compareByDescending<SalesLeaderboardItem> { it.doneCount }.thenByDescending { it.paidCount })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Versi 3: Audit Duplikasi Menyeluruh (NIK, No HP, Kode Homepass)
    val duplicateAudit: StateFlow<List<DuplicateAuditItem>> = rawCustomers.map { list ->
        val results = mutableListOf<DuplicateAuditItem>()

        // Duplikat NIK (jika 16 digit terisi)
        list.filter { it.nik.isNotBlank() && it.nik.length == 16 }
            .groupBy { it.nik }
            .filter { it.value.size > 1 }
            .forEach { (nik, dups) ->
                results.add(DuplicateAuditItem("NIK Ganda", nik, dups))
            }

        // Duplikat Nomor HP
        list.filter { it.nomorHp.isNotBlank() }
            .groupBy { it.nomorHp }
            .filter { it.value.size > 1 }
            .forEach { (hp, dups) ->
                results.add(DuplicateAuditItem("Nomor HP Ganda", hp, dups))
            }

        // Duplikat Kode Homepass
        list.filter { it.kodeHomepass.isNotBlank() }
            .groupBy { it.kodeHomepass }
            .filter { it.value.size > 1 }
            .forEach { (hp, dups) ->
                results.add(DuplicateAuditItem("Kode Homepass Ganda", hp, dups))
            }

        results
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sync State
    private val _syncState = MutableStateFlow(SyncUiState())
    val syncState: StateFlow<SyncUiState> = _syncState.asStateFlow()

    // Connection Ping State
    private val _connectionStatus = MutableStateFlow("Belum diuji")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    // Wifi Packages
    private val _wifiPackages = MutableStateFlow<List<WifiPackage>>(appSettings.getWifiPackages())
    val wifiPackages: StateFlow<List<WifiPackage>> = _wifiPackages.asStateFlow()

    // Selected customer for Detail or Edit
    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    init {
        if (appSettings.webAppUrl.isNotBlank()) {
            testConnection()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setPengerjaanFilter(filter: String) {
        _pengerjaanFilter.value = filter
    }

    fun setPembayaranFilter(filter: String) {
        _pembayaranFilter.value = filter
    }

    fun setPipelineFilter(filter: String) {
        _pipelineFilter.value = filter
    }

    fun setCompletenessFilter(filter: String) {
        _completenessFilter.value = filter
    }

    fun selectCustomer(customer: Customer?) {
        _selectedCustomer.value = customer
    }

    fun clearSyncMessage() {
        _syncState.value = _syncState.value.copy(errorMessage = null, successMessage = null)
    }

    fun updateSalesTargets(daily: Int, weekly: Int, monthly: Int) {
        appSettings.dailySalesTarget = daily
        appSettings.weeklySalesTarget = weekly
        appSettings.monthlySalesTarget = monthly
    }

    suspend fun checkDuplicates(nik: String, phone: String, homepass: String, excludeId: String): List<Customer> {
        return db.customerDao().findDuplicates(nik.trim(), phone.trim(), homepass.trim(), excludeId.trim())
    }

    fun testConnection() {
        val url = appSettings.webAppUrl
        if (url.isBlank()) {
            _connectionStatus.value = "URL Google Apps Script belum diisi"
            return
        }
        viewModelScope.launch {
            _connectionStatus.value = "Memeriksa koneksi..."
            when (val res = syncService.ping(url)) {
                is SyncResult.Success -> {
                    _connectionStatus.value = "Tersambung: ${res.data}"
                }
                is SyncResult.Error -> {
                    _connectionStatus.value = "Koneksi Gagal: ${res.message}"
                }
            }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _syncState.value = SyncUiState(isSyncing = true, progressMessage = "Menghubungkan ke Google Sheets...")
            when (val res = repository.syncAll()) {
                is SyncResult.Success -> {
                    _syncState.value = SyncUiState(
                        isSyncing = false,
                        successMessage = res.message.ifBlank { "Sinkronisasi berhasil!" }
                    )
                    _connectionStatus.value = "Tersambung & Sinkron"
                }
                is SyncResult.Error -> {
                    _syncState.value = SyncUiState(
                        isSyncing = false,
                        errorMessage = res.message
                    )
                }
            }
        }
    }

    fun saveCustomer(
        customer: Customer,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _syncState.value = SyncUiState(isSyncing = true, progressMessage = "Menyimpan data pelanggan...")
            val result = repository.saveCustomer(
                customer = customer,
                uploadPhotos = true,
                onProgress = { msg ->
                    _syncState.value = _syncState.value.copy(progressMessage = msg)
                }
            )
            result.onSuccess { saved ->
                _syncState.value = SyncUiState(isSyncing = false, successMessage = "Data pelanggan berhasil disimpan!")
                _selectedCustomer.value = saved
                onSuccess()
            }.onFailure { err ->
                _syncState.value = SyncUiState(isSyncing = false, errorMessage = err.message)
                onError(err.message ?: "Terjadi kesalahan saat menyimpan data")
            }
        }
    }

    fun updatePengerjaanStatus(customerId: String, newStatus: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val res = repository.updateStatus(customerId, statusPengerjaan = newStatus, statusPembayaran = null)
            res.onSuccess {
                _selectedCustomer.value = repository.getCustomerById(customerId)
                onSuccess()
            }
        }
    }

    fun updatePembayaranStatus(customerId: String, newStatus: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val res = repository.updateStatus(customerId, statusPengerjaan = null, statusPembayaran = newStatus)
            res.onSuccess {
                _selectedCustomer.value = repository.getCustomerById(customerId)
                onSuccess()
            }
        }
    }

    fun updatePipelineStatus(customerId: String, newPipeline: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val existing = repository.getCustomerById(customerId) ?: return@launch
            val updated = existing.copy(
                pipelineSales = newPipeline,
                terakhirDiperbarui = Customer.currentTimestampFormatted()
            )
            repository.saveCustomer(updated, uploadPhotos = false)
            _selectedCustomer.value = updated
            onSuccess()
        }
    }

    fun deleteCustomer(customerId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteCustomer(customerId)
            _selectedCustomer.value = null
            onSuccess()
        }
    }

    fun processPhoto(uri: Uri): String? {
        val file = ImageCompressionHelper.processAndSaveImage(getApplication(), uri)
        return file?.absolutePath
    }

    fun addWifiPackage(pkg: WifiPackage) {
        val updated = _wifiPackages.value + pkg
        _wifiPackages.value = updated
        appSettings.saveWifiPackages(updated)
    }

    fun toggleWifiPackage(id: String) {
        val updated = _wifiPackages.value.map {
            if (it.id == id) it.copy(isActive = !it.isActive) else it
        }
        _wifiPackages.value = updated
        appSettings.saveWifiPackages(updated)
    }

    fun generateCsvExport(includeFullNik: Boolean = false): String {
        val list = filteredCustomers.value
        val sb = StringBuilder()
        sb.append("ID Pelanggan,Tanggal Input,Nama,Nomor HP,Alamat,Email,NIK,Paket WiFi,Kecepatan,Harga,Promo,Kode Homepass,Alamat Homepass,Status Pengerjaan,Status Pembayaran,Pipeline Sales,Jadwal FollowUp,Kelengkapan,Catatan,Terakhir Diperbarui\n")

        for (c in list) {
            val nikVal = if (includeFullNik) c.nik else Customer.maskNik(c.nik)
            val escape = { str: String -> "\"${str.replace("\"", "\"\"")}\"" }
            val (pct, _) = c.getCompleteness()
            sb.append(escape(c.id)).append(",")
            sb.append(escape(c.tanggalInput)).append(",")
            sb.append(escape(c.nama)).append(",")
            sb.append(escape(c.nomorHp)).append(",")
            sb.append(escape(c.alamat)).append(",")
            sb.append(escape(c.email)).append(",")
            sb.append(escape(nikVal)).append(",")
            sb.append(escape(c.paketWifi)).append(",")
            sb.append(escape(c.kecepatanSnapshot)).append(",")
            sb.append(escape(c.hargaSnapshot)).append(",")
            sb.append(escape(c.promoSnapshot)).append(",")
            sb.append(escape(c.kodeHomepass)).append(",")
            sb.append(escape(c.alamatHomepass)).append(",")
            sb.append(escape(c.statusPengerjaan)).append(",")
            sb.append(escape(c.statusPembayaran)).append(",")
            sb.append(escape(c.pipelineSales)).append(",")
            sb.append(escape(c.jadwalFollowUp)).append(",")
            sb.append(escape("$pct%")).append(",")
            sb.append(escape(c.catatan)).append(",")
            sb.append(escape(c.terakhirDiperbarui)).append("\n")
        }
        return sb.toString()
    }

    fun movePipelineStage(customerId: String, forward: Boolean) {
        viewModelScope.launch {
            val existing = repository.getCustomerById(customerId) ?: return@launch
            val stages = Customer.ALL_PIPELINE_STAGES
            val currentIndex = stages.indexOfFirst { it.equals(existing.pipelineSales, ignoreCase = true) }
            val newIndex = if (forward) {
                (currentIndex + 1).coerceAtMost(stages.size - 1)
            } else {
                (currentIndex - 1).coerceAtLeast(0)
            }
            val newStage = stages[newIndex]
            updatePipelineStatus(customerId, newStage) {}
        }
    }

    fun exportAllCustomersAsJson(): String {
        val list = rawCustomers.value
        val arr = JSONArray()
        for (c in list) {
            val obj = JSONObject().apply {
                put("id", c.id)
                put("tanggalInput", c.tanggalInput)
                put("nama", c.nama)
                put("nomorHp", c.nomorHp)
                put("alamat", c.alamat)
                put("email", c.email)
                put("nik", c.nik)
                put("fotoKtp", c.fotoKtp)
                put("paketWifi", c.paketWifi)
                put("kecepatanSnapshot", c.kecepatanSnapshot)
                put("hargaSnapshot", c.hargaSnapshot)
                put("promoSnapshot", c.promoSnapshot)
                put("fotoMeteran", c.fotoMeteran)
                put("fotoRumahDepan", c.fotoRumahDepan)
                put("fotoRumahKanan", c.fotoRumahKanan)
                put("fotoRumahKiri", c.fotoRumahKiri)
                put("fotoHomepass", c.fotoHomepass)
                put("kodeHomepass", c.kodeHomepass)
                put("alamatHomepass", c.alamatHomepass)
                put("statusPengerjaan", c.statusPengerjaan)
                put("statusPembayaran", c.statusPembayaran)
                put("pipelineSales", c.pipelineSales)
                put("jadwalFollowUp", c.jadwalFollowUp)
                put("catatanFollowUp", c.catatanFollowUp)
                if (c.latitude != null) put("latitude", c.latitude)
                if (c.longitude != null) put("longitude", c.longitude)
                put("catatan", c.catatan)
                put("terakhirDiperbarui", c.terakhirDiperbarui)
                put("updatedBy", c.updatedBy)
            }
            arr.put(obj)
        }
        return arr.toString(2)
    }

    suspend fun importCustomersFromJson(jsonStr: String): Result<Int> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val arr = JSONArray(jsonStr)
            val customers = mutableListOf<Customer>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                customers.add(
                    Customer(
                        id = o.optString("id", Customer.generateCustomerId()),
                        tanggalInput = o.optString("tanggalInput", Customer.currentTimestampFormatted()),
                        nama = o.optString("nama"),
                        nomorHp = o.optString("nomorHp"),
                        alamat = o.optString("alamat"),
                        email = o.optString("email"),
                        nik = o.optString("nik"),
                        fotoKtp = o.optString("fotoKtp"),
                        paketWifi = o.optString("paketWifi"),
                        kecepatanSnapshot = o.optString("kecepatanSnapshot"),
                        hargaSnapshot = o.optString("hargaSnapshot"),
                        promoSnapshot = o.optString("promoSnapshot"),
                        fotoMeteran = o.optString("fotoMeteran"),
                        fotoRumahDepan = o.optString("fotoRumahDepan"),
                        fotoRumahKanan = o.optString("fotoRumahKanan"),
                        fotoRumahKiri = o.optString("fotoRumahKiri"),
                        fotoHomepass = o.optString("fotoHomepass"),
                        kodeHomepass = o.optString("kodeHomepass"),
                        alamatHomepass = o.optString("alamatHomepass"),
                        statusPengerjaan = o.optString("statusPengerjaan", Customer.STATUS_BAHAN),
                        statusPembayaran = o.optString("statusPembayaran", Customer.STATUS_UNPAID),
                        pipelineSales = o.optString("pipelineSales", Customer.PIPELINE_PROSPEK),
                        jadwalFollowUp = o.optString("jadwalFollowUp"),
                        catatanFollowUp = o.optString("catatanFollowUp"),
                        latitude = if (o.has("latitude") && !o.isNull("latitude")) o.optDouble("latitude") else null,
                        longitude = if (o.has("longitude") && !o.isNull("longitude")) o.optDouble("longitude") else null,
                        catatan = o.optString("catatan"),
                        terakhirDiperbarui = o.optString("terakhirDiperbarui", Customer.currentTimestampFormatted()),
                        updatedBy = o.optString("updatedBy", "Import Backup"),
                        syncPending = true
                    )
                )
            }
            if (customers.isNotEmpty()) {
                db.customerDao().insertAll(customers)
            }
            Result.success(customers.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun generatePrintableHtmlReport(includeFullNik: Boolean = false): String {
        val list = filteredCustomers.value
        val sb = StringBuilder()
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>")
        sb.append("<title>Laporan Data Pelanggan MyRepublic</title>")
        sb.append("<style>")
        sb.append("body { font-family: Arial, sans-serif; margin: 20px; color: #212121; }")
        sb.append(".header { border-bottom: 3px solid #C40510; padding-bottom: 12px; margin-bottom: 20px; }")
        sb.append(".title { color: #C40510; font-size: 24px; font-weight: bold; margin: 0; }")
        sb.append(".meta { font-size: 12px; color: #666; margin-top: 4px; }")
        sb.append("table { width: 100%; border-collapse: collapse; margin-top: 15px; font-size: 11px; }")
        sb.append("th { background-color: #C40510; color: white; padding: 8px 6px; text-align: left; }")
        sb.append("td { padding: 6px; border-bottom: 1px solid #ddd; vertical-align: top; }")
        sb.append(".badge-bahan { background: #FFF3E0; color: #E65100; padding: 2px 5px; border-radius: 4px; font-weight: bold; }")
        sb.append(".badge-done { background: #E8F5E9; color: #1B5E20; padding: 2px 5px; border-radius: 4px; font-weight: bold; }")
        sb.append(".badge-paid { background: #E8F5E9; color: #1B5E20; padding: 2px 5px; border-radius: 4px; font-weight: bold; }")
        sb.append(".badge-unpaid { background: #FFEBEE; color: #B71C1C; padding: 2px 5px; border-radius: 4px; font-weight: bold; }")
        sb.append("</style></head><body>")
        sb.append("<div class='header'><h1 class='title'>DATA PELANGGAN MYREPUBLIC</h1>")
        sb.append("<div class='meta'>Laporan Resmi Pemasangan & Penjualan WiFi • Total Pelanggan: ${list.size} • Dicetak: ${Customer.currentTimestampFormatted()}</div></div>")
        sb.append("<table><thead><tr><th>No</th><th>ID & Nama</th><th>Kontak & Alamat</th><th>NIK</th><th>Paket WiFi</th><th>Homepass</th><th>Pengerjaan</th><th>Pembayaran</th><th>Pipeline</th></tr></thead><tbody>")

        list.forEachIndexed { i, c ->
            val nikVal = if (includeFullNik) c.nik else Customer.maskNik(c.nik)
            val pStyle = if (c.statusPengerjaan == Customer.STATUS_DONE) "badge-done" else "badge-bahan"
            val bStyle = if (c.statusPembayaran == Customer.STATUS_PAID) "badge-paid" else "badge-unpaid"
            sb.append("<tr>")
            sb.append("<td>${i + 1}</td>")
            sb.append("<td><strong>${c.nama}</strong><br><small>${c.id}</small></td>")
            sb.append("<td>${c.nomorHp}<br><small>${c.alamat}</small></td>")
            sb.append("<td>${nikVal}</td>")
            sb.append("<td>${c.paketWifi}</td>")
            sb.append("<td>${c.kodeHomepass.ifBlank { "-" }}</td>")
            sb.append("<td><span class='$pStyle'>${c.statusPengerjaan}</span></td>")
            sb.append("<td><span class='$bStyle'>${c.statusPembayaran}</span></td>")
            sb.append("<td>${c.pipelineSales}</td>")
            sb.append("</tr>")
        }
        sb.append("</tbody></table></body></html>")
        return sb.toString()
    }
}
