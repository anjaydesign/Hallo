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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

data class DashboardStats(
    val total: Int = 0,
    val totalBahan: Int = 0,
    val totalDone: Int = 0,
    val totalPaid: Int = 0,
    val totalUnpaid: Int = 0
)

data class SyncUiState(
    val isSyncing: Boolean = false,
    val progressMessage: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null
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

    // Filtered Customer List
    val filteredCustomers: StateFlow<List<Customer>> = combine(
        rawCustomers,
        _searchQuery,
        _pengerjaanFilter,
        _pembayaranFilter
    ) { list, query, pengerjaan, pembayaran ->
        list.filter { cust ->
            val matchQuery = query.isBlank() ||
                cust.nama.contains(query, ignoreCase = true) ||
                cust.nomorHp.contains(query, ignoreCase = true) ||
                cust.alamat.contains(query, ignoreCase = true) ||
                cust.kodeHomepass.contains(query, ignoreCase = true) ||
                cust.nik.contains(query, ignoreCase = true)

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

            matchQuery && matchPengerjaan && matchPembayaran
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard Statistics calculated from actual data
    val stats: StateFlow<DashboardStats> = rawCustomers.combine(_searchQuery) { list, _ ->
        DashboardStats(
            total = list.size,
            totalBahan = list.count { it.statusPengerjaan == Customer.STATUS_BAHAN },
            totalDone = list.count { it.statusPengerjaan == Customer.STATUS_DONE },
            totalPaid = list.count { it.statusPembayaran == Customer.STATUS_PAID },
            totalUnpaid = list.count { it.statusPembayaran == Customer.STATUS_UNPAID }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

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
        // Ping connection if URL is saved
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

    fun selectCustomer(customer: Customer?) {
        _selectedCustomer.value = customer
    }

    fun clearSyncMessage() {
        _syncState.value = _syncState.value.copy(errorMessage = null, successMessage = null)
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
                // Update selected customer state
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

    fun deleteCustomer(customerId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteCustomer(customerId)
            _selectedCustomer.value = null
            onSuccess()
        }
    }

    // Process photo from URI and return local saved file path
    fun processPhoto(uri: Uri): String? {
        val file = ImageCompressionHelper.processAndSaveImage(getApplication(), uri)
        return file?.absolutePath
    }

    // Package Management
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

    // Export CSV generator
    fun generateCsvExport(includeFullNik: Boolean = false): String {
        val list = filteredCustomers.value
        val sb = StringBuilder()
        sb.append("ID Pelanggan,Tanggal Input,Nama,Nomor HP,Alamat,Email,NIK,Paket WiFi,Kode Homepass,Alamat Homepass,Status Pengerjaan,Status Pembayaran,Catatan,Terakhir Diperbarui\n")

        for (c in list) {
            val nikVal = if (includeFullNik) c.nik else Customer.maskNik(c.nik)
            val escape = { str: String -> "\"${str.replace("\"", "\"\"")}\"" }
            sb.append(escape(c.id)).append(",")
            sb.append(escape(c.tanggalInput)).append(",")
            sb.append(escape(c.nama)).append(",")
            sb.append(escape(c.nomorHp)).append(",")
            sb.append(escape(c.alamat)).append(",")
            sb.append(escape(c.email)).append(",")
            sb.append(escape(nikVal)).append(",")
            sb.append(escape(c.paketWifi)).append(",")
            sb.append(escape(c.kodeHomepass)).append(",")
            sb.append(escape(c.alamatHomepass)).append(",")
            sb.append(escape(c.statusPengerjaan)).append(",")
            sb.append(escape(c.statusPembayaran)).append(",")
            sb.append(escape(c.catatan)).append(",")
            sb.append(escape(c.terakhirDiperbarui)).append("\n")
        }
        return sb.toString()
    }
}
