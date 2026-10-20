package com.example.data.repository

import android.content.Context
import com.example.data.local.AppSettings
import com.example.data.local.CustomerDao
import com.example.data.model.Customer
import com.example.data.remote.GoogleSheetsSyncService
import com.example.data.remote.SyncResult
import com.example.util.ImageCompressionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class CustomerRepository(
    private val context: Context,
    private val customerDao: CustomerDao,
    private val syncService: GoogleSheetsSyncService,
    private val appSettings: AppSettings
) {

    fun getCustomersFlow(): Flow<List<Customer>> = customerDao.getAllCustomers()

    suspend fun getCustomerById(id: String): Customer? = customerDao.getCustomerById(id)

    suspend fun saveCustomer(
        customer: Customer,
        uploadPhotos: Boolean = true,
        onProgress: ((String) -> Unit)? = null
    ): Result<Customer> = withContext(Dispatchers.IO) {
        try {
            var updatedCustomer = customer
            val webAppUrl = appSettings.webAppUrl

            if (uploadPhotos && webAppUrl.isNotBlank()) {
                onProgress?.invoke("Mengunggah dokumentasi foto ke Google Drive...")
                // Upload photos if they are local files
                updatedCustomer = uploadCustomerPhotosToDrive(updatedCustomer, webAppUrl)
            }

            // Save to Room first (offline-first durability)
            val isRemoteReady = webAppUrl.isNotBlank()
            customerDao.insertCustomer(updatedCustomer.copy(syncPending = !isRemoteReady))

            if (isRemoteReady) {
                onProgress?.invoke("Menyimpan ke Google Sheets...")
                val remoteResult = syncService.saveCustomer(webAppUrl, updatedCustomer)
                when (remoteResult) {
                    is SyncResult.Success -> {
                        customerDao.insertCustomer(updatedCustomer.copy(syncPending = false))
                        appSettings.lastSyncTime = Customer.currentTimestampFormatted()
                        Result.success(updatedCustomer)
                    }
                    is SyncResult.Error -> {
                        // Mark as pending sync in Room
                        customerDao.insertCustomer(updatedCustomer.copy(syncPending = true))
                        Result.failure(Exception("Tersimpan di perangkat lokal, namun gagal sync ke Google Sheets: ${remoteResult.message}"))
                    }
                }
            } else {
                Result.success(updatedCustomer)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun uploadCustomerPhotosToDrive(cust: Customer, webAppUrl: String): Customer {
        var c = cust
        val photoFields = listOf(
            Triple("ktp", c.fotoKtp) { url: String -> c = c.copy(fotoKtp = url) },
            Triple("meteran", c.fotoMeteran) { url: String -> c = c.copy(fotoMeteran = url) },
            Triple("depan", c.fotoRumahDepan) { url: String -> c = c.copy(fotoRumahDepan = url) },
            Triple("kanan", c.fotoRumahKanan) { url: String -> c = c.copy(fotoRumahKanan = url) },
            Triple("kiri", c.fotoRumahKiri) { url: String -> c = c.copy(fotoRumahKiri = url) },
            Triple("homepass", c.fotoHomepass) { url: String -> c = c.copy(fotoHomepass = url) }
        )

        for (field in photoFields) {
            val type = field.first
            val path = field.second
            val updateAction = field.third

            if (path.isNotBlank() && !path.startsWith("http://") && !path.startsWith("https://")) {
                val file = File(path)
                if (file.exists()) {
                    val base64 = ImageCompressionHelper.fileToBase64(file)
                    if (base64 != null) {
                        val res = syncService.uploadPhotoToDrive(webAppUrl, c.id, type, base64)
                        if (res is SyncResult.Success) {
                            updateAction(res.data)
                        }
                    }
                }
            }
        }
        return c
    }

    suspend fun updateStatus(
        id: String,
        statusPengerjaan: String?,
        statusPembayaran: String?,
        updatedBy: String = appSettings.petugasName
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val existing = customerDao.getCustomerById(id) ?: return@withContext Result.failure(Exception("Pelanggan tidak ditemukan"))
            val nowStr = Customer.currentTimestampFormatted()
            val updated = existing.copy(
                statusPengerjaan = statusPengerjaan ?: existing.statusPengerjaan,
                statusPembayaran = statusPembayaran ?: existing.statusPembayaran,
                terakhirDiperbarui = "$nowStr ($updatedBy)",
                updatedBy = updatedBy,
                syncPending = true
            )
            customerDao.updateCustomer(updated)

            val webAppUrl = appSettings.webAppUrl
            if (webAppUrl.isNotBlank()) {
                val res = syncService.updateStatus(webAppUrl, id, statusPengerjaan, statusPembayaran, updatedBy)
                if (res is SyncResult.Success) {
                    customerDao.updateCustomer(updated.copy(syncPending = false))
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCustomer(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            customerDao.deleteById(id)
            val webAppUrl = appSettings.webAppUrl
            if (webAppUrl.isNotBlank()) {
                syncService.deleteCustomer(webAppUrl, id)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncAll(): SyncResult<String> = withContext(Dispatchers.IO) {
        val webAppUrl = appSettings.webAppUrl
        if (webAppUrl.isBlank()) {
            return@withContext SyncResult.Error("URL Google Apps Script belum diisi. Buka Pengaturan untuk mengisi.")
        }

        // 1. Push pending local items
        val pendingList = customerDao.getPendingSyncCustomers()
        for (item in pendingList) {
            val uploaded = uploadCustomerPhotosToDrive(item, webAppUrl)
            val res = syncService.saveCustomer(webAppUrl, uploaded)
            if (res is SyncResult.Success) {
                customerDao.insertCustomer(uploaded.copy(syncPending = false))
            }
        }

        // 2. Pull remote items from Google Sheets
        val pullRes = syncService.fetchAllCustomers(webAppUrl)
        if (pullRes is SyncResult.Success) {
            val remoteList = pullRes.data
            for (remoteCust in remoteList) {
                val localCust = customerDao.getCustomerById(remoteCust.id)
                if (localCust == null) {
                    customerDao.insertCustomer(remoteCust.copy(syncPending = false))
                } else if (!localCust.syncPending) {
                    // Overwrite with remote version if not pending local edits
                    customerDao.insertCustomer(remoteCust.copy(syncPending = false))
                }
            }
            appSettings.lastSyncTime = Customer.currentTimestampFormatted()
            SyncResult.Success("Sinkronisasi Berhasil! ${remoteList.size} data pelanggan diperbarui.", "OK")
        } else {
            SyncResult.Error((pullRes as SyncResult.Error).message)
        }
    }
}
