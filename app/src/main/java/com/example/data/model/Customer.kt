package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey
    val id: String = generateCustomerId(),
    val tanggalInput: String = currentTimestampFormatted(),
    val nama: String = "",
    val nomorHp: String = "",
    val alamat: String = "",
    val email: String = "",
    val nik: String = "",
    val fotoKtp: String = "",
    val paketWifi: String = "",
    val kecepatanSnapshot: String = "",
    val hargaSnapshot: String = "",
    val promoSnapshot: String = "",
    val fotoMeteran: String = "",
    val fotoRumahDepan: String = "",
    val fotoRumahKanan: String = "",
    val fotoRumahKiri: String = "",
    val fotoHomepass: String = "",
    val kodeHomepass: String = "",
    val alamatHomepass: String = "",
    val statusPengerjaan: String = STATUS_BAHAN,
    val statusPembayaran: String = STATUS_UNPAID,
    val pipelineSales: String = PIPELINE_PROSPEK,
    val jadwalFollowUp: String = "",
    val catatanFollowUp: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val catatan: String = "",
    val terakhirDiperbarui: String = currentTimestampFormatted(),
    val updatedBy: String = "Petugas Lapangan",
    val syncPending: Boolean = false
) {
    /**
     * Menghitung persentase kelengkapan berkas & survei (0-100%) dan daftar dokumen yang kurang
     */
    fun getCompleteness(): Pair<Int, List<String>> {
        val missing = mutableListOf<String>()
        var points = 0
        val totalPoints = 9

        if (nama.isNotBlank() && nomorHp.isNotBlank()) points += 1 else missing.add("Kontak Dasar")
        if (nik.length == 16) points += 1 else missing.add("NIK 16 Digit")
        if (alamat.isNotBlank()) points += 1 else missing.add("Alamat Pemasangan")
        if (paketWifi.isNotBlank()) points += 1 else missing.add("Pilihan Paket")
        if (fotoKtp.isNotBlank()) points += 1 else missing.add("Foto KTP")
        if (fotoMeteran.isNotBlank()) points += 1 else missing.add("Foto Meteran Listrik")
        if (fotoRumahDepan.isNotBlank()) points += 1 else missing.add("Foto Rumah Depan")
        if (fotoRumahKanan.isNotBlank() && fotoRumahKiri.isNotBlank()) points += 1 else missing.add("Foto Sisi Rumah (Kanan/Kiri)")
        if (fotoHomepass.isNotBlank() || kodeHomepass.isNotBlank()) points += 1 else missing.add("Data Homepass (FAT)")

        val percentage = ((points.toFloat() / totalPoints) * 100).toInt()
        return Pair(percentage, missing)
    }

    fun isComplete(): Boolean {
        return getCompleteness().first == 100
    }

    /**
     * Memeriksa apakah ada jadwal follow-up hari ini
     */
    fun isFollowUpToday(): Boolean {
        if (jadwalFollowUp.isBlank()) return false
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return jadwalFollowUp.startsWith(todayStr)
    }

    /**
     * Memeriksa apakah jadwal follow-up sudah terlewat / terlambat
     */
    fun isFollowUpOverdue(): Boolean {
        if (jadwalFollowUp.isBlank()) return false
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dueDate = sdf.parse(jadwalFollowUp.take(10)) ?: return false
            val today = sdf.parse(sdf.format(Date())) ?: return false
            dueDate.before(today) && pipelineSales != PIPELINE_SELESAI && pipelineSales != PIPELINE_BATAL
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        const val STATUS_BAHAN = "BAHAN"
        const val STATUS_DONE = "DONE"

        const val STATUS_UNPAID = "UNPAID"
        const val STATUS_PAID = "PAID"

        // CRM Pipeline Stages
        const val PIPELINE_PROSPEK = "PROSPEK"
        const val PIPELINE_DIHUBUNGI = "DIHUBUNGI"
        const val PIPELINE_BERMINAT = "BERMINAT"
        const val PIPELINE_DATA_LENGKAP = "DATA LENGKAP"
        const val PIPELINE_PENGAJUAN = "PENGAJUAN"
        const val PIPELINE_PEMASANGAN = "PEMASANGAN"
        const val PIPELINE_SELESAI = "SELESAI"
        const val PIPELINE_BATAL = "BATAL"

        val ALL_PIPELINE_STAGES = listOf(
            PIPELINE_PROSPEK,
            PIPELINE_DIHUBUNGI,
            PIPELINE_BERMINAT,
            PIPELINE_DATA_LENGKAP,
            PIPELINE_PENGAJUAN,
            PIPELINE_PEMASANGAN,
            PIPELINE_SELESAI,
            PIPELINE_BATAL
        )

        fun generateCustomerId(): String {
            val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            val randomSuffix = UUID.randomUUID().toString().substring(0, 4).uppercase(Locale.ROOT)
            return "MYREP-$dateStr-$randomSuffix"
        }

        fun currentTimestampFormatted(): String {
            return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        }

        fun maskNik(nik: String): String {
            if (nik.length < 8) return nik
            val prefix = nik.take(6)
            val suffix = nik.takeLast(4)
            val maskedPart = "*".repeat((nik.length - 10).coerceAtLeast(4))
            return "$prefix$maskedPart$suffix"
        }
    }
}
