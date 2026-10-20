package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.WifiPackage
import org.json.JSONArray
import org.json.JSONObject

class AppSettings(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings_myrep", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_WEB_APP_URL = "web_app_url"
        private const val KEY_PETUGAS_NAME = "petugas_name"
        private const val KEY_PASSCODE = "app_passcode"
        private const val KEY_PASSCODE_ENABLED = "passcode_enabled"
        private const val KEY_WIFI_PACKAGES = "wifi_packages_json"
        private const val KEY_LAST_SYNC_TIME = "last_sync_time"
        private const val KEY_TARGET_DAILY = "target_daily"
        private const val KEY_TARGET_WEEKLY = "target_weekly"
        private const val KEY_TARGET_MONTHLY = "target_monthly"
        private const val KEY_USER_ROLE = "user_role"
    }

    var dailySalesTarget: Int
        get() = prefs.getInt(KEY_TARGET_DAILY, 3)
        set(value) = prefs.edit().putInt(KEY_TARGET_DAILY, value.coerceAtLeast(1)).apply()

    var weeklySalesTarget: Int
        get() = prefs.getInt(KEY_TARGET_WEEKLY, 15)
        set(value) = prefs.edit().putInt(KEY_TARGET_WEEKLY, value.coerceAtLeast(1)).apply()

    var monthlySalesTarget: Int
        get() = prefs.getInt(KEY_TARGET_MONTHLY, 50)
        set(value) = prefs.edit().putInt(KEY_TARGET_MONTHLY, value.coerceAtLeast(1)).apply()

    var userRole: String
        get() = prefs.getString(KEY_USER_ROLE, "ADMIN") ?: "ADMIN"
        set(value) = prefs.edit().putString(KEY_USER_ROLE, value).apply()

    var webAppUrl: String
        get() = prefs.getString(KEY_WEB_APP_URL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_WEB_APP_URL, value.trim()).apply()

    var petugasName: String
        get() = prefs.getString(KEY_PETUGAS_NAME, "Petugas MyRepublic") ?: "Petugas MyRepublic"
        set(value) = prefs.edit().putString(KEY_PETUGAS_NAME, value.trim()).apply()

    var appPasscode: String
        get() = prefs.getString(KEY_PASSCODE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PASSCODE, value).apply()

    var isPasscodeEnabled: Boolean
        get() = prefs.getBoolean(KEY_PASSCODE_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_PASSCODE_ENABLED, value).apply()

    var lastSyncTime: String
        get() = prefs.getString(KEY_LAST_SYNC_TIME, "-") ?: "-"
        set(value) = prefs.edit().putString(KEY_LAST_SYNC_TIME, value).apply()

    fun getWifiPackages(): List<WifiPackage> {
        val jsonStr = prefs.getString(KEY_WIFI_PACKAGES, null)
        if (jsonStr.isNullOrBlank()) {
            return WifiPackage.getDefaultPackages()
        }
        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<WifiPackage>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    WifiPackage(
                        id = obj.optString("id"),
                        name = obj.optString("name"),
                        speed = obj.optString("speed"),
                        priceEstimate = obj.optString("priceEstimate"),
                        description = obj.optString("description"),
                        isActive = obj.optBoolean("isActive", true)
                    )
                )
            }
            if (list.isEmpty()) WifiPackage.getDefaultPackages() else list
        } catch (e: Exception) {
            WifiPackage.getDefaultPackages()
        }
    }

    fun saveWifiPackages(packages: List<WifiPackage>) {
        try {
            val arr = JSONArray()
            packages.forEach { pkg ->
                val obj = JSONObject().apply {
                    put("id", pkg.id)
                    put("name", pkg.name)
                    put("speed", pkg.speed)
                    put("priceEstimate", pkg.priceEstimate)
                    put("description", pkg.description)
                    put("isActive", pkg.isActive)
                }
                arr.put(obj)
            }
            prefs.edit().putString(KEY_WIFI_PACKAGES, arr.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
