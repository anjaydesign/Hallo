package com.example

import com.example.data.model.Customer
import com.example.data.model.WifiPackage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testWifiPackagesCatalog() {
        val defaultPackages = WifiPackage.getDefaultPackages()
        // Pastikan ada 12 paket awal MyRepublic
        assertEquals(12, defaultPackages.size)
        assertEquals("RING", defaultPackages[0].name)
        assertEquals("30 Mbps", defaultPackages[0].speed)
        assertEquals("Rp116.000", defaultPackages[0].priceEstimate)
        assertEquals("PLATINUM", defaultPackages[11].name)
        assertEquals("1 Gbps", defaultPackages[11].speed)
    }

    @Test
    fun testNikMasking() {
        val sampleNik = "3201011234560001"
        val masked = Customer.maskNik(sampleNik)
        // Prefix 6 digit ("320101"), suffix 4 digit ("0001"), tengah bintang
        assertTrue(masked.startsWith("320101"))
        assertTrue(masked.endsWith("0001"))
        assertTrue(masked.contains("******"))
        assertEquals(16, masked.length)
    }

    @Test
    fun testDocumentCompleteness() {
        val emptyCustomer = Customer(id = "CUST-01", nama = "")
        val (scoreEmpty, missingEmpty) = emptyCustomer.getCompleteness()
        assertEquals(0, scoreEmpty)
        assertTrue(missingEmpty.isNotEmpty())

        val fullCustomer = Customer(
            id = "CUST-02",
            nama = "Budi Santoso",
            nomorHp = "081234567890",
            nik = "3201011234560001",
            alamat = "Jl. Merdeka No. 10",
            paketWifi = "Fast 50 Mbps",
            fotoKtp = "/storage/ktp.jpg",
            fotoMeteran = "/storage/meteran.jpg",
            fotoRumahDepan = "/storage/depan.jpg",
            fotoRumahKanan = "/storage/kanan.jpg",
            fotoRumahKiri = "/storage/kiri.jpg",
            fotoHomepass = "/storage/homepass.jpg",
            kodeHomepass = "FAT-JKT-001"
        )
        val (scoreFull, missingFull) = fullCustomer.getCompleteness()
        assertEquals(100, scoreFull)
        assertTrue(missingFull.isEmpty())
        assertTrue(fullCustomer.isComplete())
    }

    @Test
    fun testStatusSeparation() {
        // Memastikan status pengerjaan dan status pembayaran terpisah secara ketat
        val customer = Customer(
            id = "CUST-03",
            nama = "Ani Wijaya",
            statusPengerjaan = Customer.STATUS_BAHAN,
            statusPembayaran = Customer.STATUS_UNPAID
        )
        assertEquals(Customer.STATUS_BAHAN, customer.statusPengerjaan)
        assertEquals(Customer.STATUS_UNPAID, customer.statusPembayaran)

        val updated = customer.copy(statusPengerjaan = Customer.STATUS_DONE)
        assertEquals(Customer.STATUS_DONE, updated.statusPengerjaan)
        assertEquals(Customer.STATUS_UNPAID, updated.statusPembayaran)
    }
}
