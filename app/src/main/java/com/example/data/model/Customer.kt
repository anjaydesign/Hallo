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
    val fotoMeteran: String = "",
    val fotoRumahDepan: String = "",
    val fotoRumahKanan: String = "",
    val fotoRumahKiri: String = "",
    val fotoHomepass: String = "",
    val kodeHomepass: String = "",
    val alamatHomepass: String = "",
    val statusPengerjaan: String = STATUS_BAHAN,
    val statusPembayaran: String = STATUS_UNPAID,
    val catatan: String = "",
    val terakhirDiperbarui: String = currentTimestampFormatted(),
    val updatedBy: String = "Petugas Lapangan",
    val syncPending: Boolean = false
) {
    companion object {
        const val STATUS_BAHAN = "BAHAN"
        const val STATUS_DONE = "DONE"

        const val STATUS_UNPAID = "UNPAID"
        const val STATUS_PAID = "PAID"

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
