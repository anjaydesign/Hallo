package com.example.data.model

data class WifiPackage(
    val id: String,
    val name: String,
    val speed: String,
    val priceEstimate: String = "",
    val description: String = "",
    val isActive: Boolean = true
) {
    companion object {
        fun getDefaultPackages(): List<WifiPackage> = listOf(
            WifiPackage("pkg_1", "Value 30 Mbps", "30 Mbps", "Rp 210.000/bln", "Cocok untuk 1-3 perangkat santai", true),
            WifiPackage("pkg_2", "Fast 50 Mbps", "50 Mbps", "Rp 275.000/bln", "Streaming HD & browsing keluarga", true),
            WifiPackage("pkg_3", "Nova 100 Mbps", "100 Mbps", "Rp 349.000/bln", "Download super cepat & WFH produktif", true),
            WifiPackage("pkg_4", "MyGamer 250 Mbps", "250 Mbps", "Rp 449.000/bln", "Ping rendah, prioritas routing gamer", true),
            WifiPackage("pkg_5", "Prime 500 Mbps", "500 Mbps", "Rp 649.000/bln", "Kecepatan maksimal untuk kantor & streamer", true)
        )
    }
}
