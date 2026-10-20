package com.example.data.model

data class WifiPackage(
    val id: String,
    val name: String,
    val speed: String,
    val priceEstimate: String = "",
    val description: String = "",
    val promo: String = "",
    val isActive: Boolean = true,
    val sumberHarga: String = "Brosur Promosi (Perlu Konfirmasi)",
    val tanggalVerifikasi: String = "Belum Terverifikasi"
) {
    companion object {
        fun getDefaultPackages(): List<WifiPackage> = listOf(
            WifiPackage("pkg_01", "RING", "30 Mbps", "Rp116.000", "Paket hemat dasar", "Termasuk PPN 11%", true),
            WifiPackage("pkg_02", "PAS", "75 Mbps", "Rp139.000", "Kecepatan pas untuk keluarga", "Termasuk PPN 11%", true),
            WifiPackage("pkg_03", "ECO", "100 Mbps", "Rp166.500", "Streaming & browsing lancar", "Termasuk PPN 11%", true),
            WifiPackage("pkg_04", "NEO", "200 Mbps", "Rp233.100", "Kerja & belajar online", "Free speed upgrade & modem (Termasuk PPN 11%)", true),
            WifiPackage("pkg_05", "VELO", "300 Mbps", "Rp277.500", "Keluarga aktif multi-perangkat", "Free speed upgrade & modem (Termasuk PPN 11%)", true),
            WifiPackage("pkg_06", "NEXUS", "400 Mbps", "Rp333.000", "Kecepatan tinggi tanpa hambatan", "Free speed upgrade & modem (Termasuk PPN 11%)", true),
            WifiPackage("pkg_07", "SILVER", "350 Mbps", "Rp444.000", "Prioritas koneksi & IP Public", "IP Public Static + WiFi (Termasuk PPN 11%)", true),
            WifiPackage("pkg_08", "PRIME", "500 Mbps", "Rp555.000", "Hiburan maksimal & streaming 4K", "Free Vidio Platinum Lite (Termasuk PPN 11%)", true),
            WifiPackage("pkg_09", "GOLD", "500 Mbps", "Rp610.500", "Koneksi bisnis & IP Public", "IP Public Static + WiFi (Termasuk PPN 11%)", true),
            WifiPackage("pkg_10", "WONDER", "750 Mbps", "Rp721.000", "Super cepat untuk power user", "Free Vidio Platinum Lite (Termasuk PPN 11%)", true),
            WifiPackage("pkg_11", "ULTRA", "1 Gbps", "Rp943.500", "Kecepatan gigabit ultra", "Free Vidio Platinum Lite (Termasuk PPN 11%)", true),
            WifiPackage("pkg_12", "PLATINUM", "1 Gbps", "Rp999.000", "Paket tertinggi dengan IP Public", "IP Public Static + WiFi (Termasuk PPN 11%)", true)
        )
    }
}
