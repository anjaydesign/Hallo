package com.example.data.remote

import com.example.data.model.Customer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class SyncResult<out T> {
    data class Success<T>(val data: T, val message: String = "") : SyncResult<T>()
    data class Error(val message: String, val exception: Throwable? = null) : SyncResult<Nothing>()
}

class GoogleSheetsSyncService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .writeTimeout(35, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun ping(webAppUrl: String): SyncResult<String> = withContext(Dispatchers.IO) {
        if (webAppUrl.isBlank()) {
            return@withContext SyncResult.Error("URL Google Apps Script belum dikonfigurasi.")
        }
        try {
            val url = if (webAppUrl.contains("?")) "$webAppUrl&action=ping" else "$webAppUrl?action=ping"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext SyncResult.Error("Server merespons kode HTTP: ${response.code}")
            }

            val json = JSONObject(body)
            if (json.optString("status") == "success") {
                SyncResult.Success(json.optString("message", "Terhubung ke Google Sheets & Drive"))
            } else {
                SyncResult.Error(json.optString("message", "Gagal menghubungkan ke Google Sheets"))
            }
        } catch (e: Exception) {
            SyncResult.Error("Gagal menghubungi server: ${e.localizedMessage ?: "Timeout / Masalah Jaringan"}", e)
        }
    }

    suspend fun fetchAllCustomers(webAppUrl: String): SyncResult<List<Customer>> = withContext(Dispatchers.IO) {
        if (webAppUrl.isBlank()) {
            return@withContext SyncResult.Error("URL Google Apps Script belum diisi.")
        }
        try {
            val url = if (webAppUrl.contains("?")) "$webAppUrl&action=getAll" else "$webAppUrl?action=getAll"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext SyncResult.Error("HTTP error: ${response.code}")
            }

            val json = JSONObject(body)
            if (json.optString("status") == "success") {
                val dataArray = json.optJSONArray("data") ?: JSONArray()
                val list = mutableListOf<Customer>()
                for (i in 0 until dataArray.length()) {
                    val item = dataArray.getJSONObject(i)
                    list.add(
                        Customer(
                            id = item.optString("id"),
                            tanggalInput = item.optString("tanggalInput"),
                            nama = item.optString("nama"),
                            nomorHp = item.optString("nomorHp"),
                            alamat = item.optString("alamat"),
                            email = item.optString("email"),
                            nik = item.optString("nik"),
                            fotoKtp = item.optString("fotoKtp"),
                            paketWifi = item.optString("paketWifi"),
                            fotoMeteran = item.optString("fotoMeteran"),
                            fotoRumahDepan = item.optString("fotoRumahDepan"),
                            fotoRumahKanan = item.optString("fotoRumahKanan"),
                            fotoRumahKiri = item.optString("fotoRumahKiri"),
                            fotoHomepass = item.optString("fotoHomepass"),
                            kodeHomepass = item.optString("kodeHomepass"),
                            alamatHomepass = item.optString("alamatHomepass"),
                            statusPengerjaan = item.optString("statusPengerjaan", Customer.STATUS_BAHAN),
                            statusPembayaran = item.optString("statusPembayaran", Customer.STATUS_UNPAID),
                            catatan = item.optString("catatan"),
                            terakhirDiperbarui = item.optString("terakhirDiperbarui"),
                            syncPending = false
                        )
                    )
                }
                SyncResult.Success(list, "Berhasil memuat ${list.size} pelanggan dari Google Sheets")
            } else {
                SyncResult.Error(json.optString("message", "Gagal memuat data dari Spreadsheet"))
            }
        } catch (e: Exception) {
            SyncResult.Error("Gagal mengambil data dari Google Sheets: ${e.localizedMessage}", e)
        }
    }

    suspend fun saveCustomer(webAppUrl: String, customer: Customer): SyncResult<String> = withContext(Dispatchers.IO) {
        if (webAppUrl.isBlank()) {
            return@withContext SyncResult.Error("URL Google Apps Script belum diisi.")
        }
        try {
            val payload = JSONObject().apply {
                put("action", "save")
                val custObj = JSONObject().apply {
                    put("id", customer.id)
                    put("tanggalInput", customer.tanggalInput)
                    put("nama", customer.nama)
                    put("nomorHp", customer.nomorHp)
                    put("alamat", customer.alamat)
                    put("email", customer.email)
                    put("nik", customer.nik)
                    put("fotoKtp", customer.fotoKtp)
                    put("paketWifi", customer.paketWifi)
                    put("fotoMeteran", customer.fotoMeteran)
                    put("fotoRumahDepan", customer.fotoRumahDepan)
                    put("fotoRumahKanan", customer.fotoRumahKanan)
                    put("fotoRumahKiri", customer.fotoRumahKiri)
                    put("fotoHomepass", customer.fotoHomepass)
                    put("kodeHomepass", customer.kodeHomepass)
                    put("alamatHomepass", customer.alamatHomepass)
                    put("statusPengerjaan", customer.statusPengerjaan)
                    put("statusPembayaran", customer.statusPembayaran)
                    put("catatan", customer.catatan)
                }
                put("customer", custObj)
            }

            val requestBody = payload.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder().url(webAppUrl).post(requestBody).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext SyncResult.Error("Gagal menyimpan ke Google Sheets (HTTP ${response.code})")
            }

            val json = JSONObject(body)
            if (json.optString("status") == "success") {
                SyncResult.Success(json.optString("id", customer.id), json.optString("message", "Berhasil disimpan di Google Sheets"))
            } else {
                SyncResult.Error(json.optString("message", "Gagal menyimpan di Google Sheets"))
            }
        } catch (e: Exception) {
            SyncResult.Error("Koneksi gagal saat menyimpan ke Google Sheets: ${e.localizedMessage}", e)
        }
    }

    suspend fun updateStatus(
        webAppUrl: String,
        id: String,
        statusPengerjaan: String?,
        statusPembayaran: String?,
        updatedBy: String
    ): SyncResult<String> = withContext(Dispatchers.IO) {
        if (webAppUrl.isBlank()) {
            return@withContext SyncResult.Error("URL Google Apps Script belum diisi.")
        }
        try {
            val payload = JSONObject().apply {
                put("action", "updateStatus")
                put("id", id)
                if (statusPengerjaan != null) put("statusPengerjaan", statusPengerjaan)
                if (statusPembayaran != null) put("statusPembayaran", statusPembayaran)
                put("updatedBy", updatedBy)
            }

            val request = Request.Builder()
                .url(webAppUrl)
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            val json = JSONObject(body)
            if (json.optString("status") == "success") {
                SyncResult.Success(id, json.optString("message", "Status berhasil diperbarui"))
            } else {
                SyncResult.Error(json.optString("message", "Gagal memperbarui status di Google Sheets"))
            }
        } catch (e: Exception) {
            SyncResult.Error("Koneksi gagal saat memperbarui status: ${e.localizedMessage}", e)
        }
    }

    suspend fun deleteCustomer(webAppUrl: String, id: String): SyncResult<Boolean> = withContext(Dispatchers.IO) {
        if (webAppUrl.isBlank()) {
            return@withContext SyncResult.Error("URL Google Apps Script belum diisi.")
        }
        try {
            val payload = JSONObject().apply {
                put("action", "delete")
                put("id", id)
            }
            val request = Request.Builder()
                .url(webAppUrl)
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            val json = JSONObject(body)
            if (json.optString("status") == "success") {
                SyncResult.Success(true, "Data berhasil dihapus dari Google Sheets")
            } else {
                SyncResult.Error(json.optString("message", "Gagal menghapus data dari Google Sheets"))
            }
        } catch (e: Exception) {
            SyncResult.Error("Koneksi gagal saat menghapus data: ${e.localizedMessage}", e)
        }
    }

    suspend fun uploadPhotoToDrive(
        webAppUrl: String,
        customerId: String,
        photoType: String,
        base64Data: String,
        mimeType: String = "image/jpeg"
    ): SyncResult<String> = withContext(Dispatchers.IO) {
        if (webAppUrl.isBlank()) {
            return@withContext SyncResult.Error("URL Google Apps Script belum diisi.")
        }
        try {
            val payload = JSONObject().apply {
                put("action", "uploadPhoto")
                put("customerId", customerId)
                put("photoType", photoType)
                put("base64", base64Data)
                put("mimeType", mimeType)
            }

            val request = Request.Builder()
                .url(webAppUrl)
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            val json = JSONObject(body)
            if (json.optString("status") == "success") {
                val fileUrl = json.optString("fileUrl")
                SyncResult.Success(fileUrl, "Foto berhasil diunggah ke Google Drive")
            } else {
                SyncResult.Error(json.optString("message", "Gagal mengunggah foto ke Google Drive"))
            }
        } catch (e: Exception) {
            SyncResult.Error("Gagal mengunggah foto ke Google Drive: ${e.localizedMessage}", e)
        }
    }
}
