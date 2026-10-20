package com.example.util

object GoogleAppsScriptTemplate {

    const val SHEET_COLUMNS_INFO = """
1. Sheet "Pelanggan" (26 Kolom): ID, Tanggal, Nama, HP, Alamat, Email, NIK, Foto KTP, Paket, Kecepatan, Harga, Promo, Foto Meteran, Foto Depan, Kanan, Kiri, Homepass, Kode HP, Alamat HP, Status Pengerjaan, Pembayaran, Pipeline, FollowUp, Catatan, Petugas, Update
2. Sheet "Paket" (9 Kolom): 12 Paket Awal MyRepublic (RING s/d PLATINUM), Harga, Promo, Status Aktif
3. Sheet "Aktivitas" (7 Kolom): Audit trail log perubahan
4. Sheet "FollowUp" (8 Kolom): Jadwal & hasil CRM
5. Sheet "Pembayaran" (9 Kolom): Transaksi keuangan & verifikasi
6. Sheet "Pengaturan" (3 Kolom): Parameter sistem
7. Sheet "Pengguna" (5 Kolom): Petugas & Role
"""

    val SCRIPT_CODE = """
/**
 * BACKEND API GOOGLE APPS SCRIPT — DATA PELANGGAN MYREPUBLIC (FULL VERSION)
 * FASE 1: Database Setup, Struktur 7 Sheet & 12 Paket Awal MyRepublic
 * 
 * CARA SETUP DATABASE DARI HP ANDROID:
 * 1. Buka spreadsheet baru di https://sheets.google.com melalui browser Chrome di HP.
 * 2. Beri nama file: "DATABASE PELANGGAN MYREPUBLIC"
 * 3. Buka menu 'Ekstensi' (Extensions) -> 'Apps Script'
 * 4. Hapus seluruh kode bawaan, lalu tempel SELURUH kode ini.
 * 5. Pada baris fungsi di editor atas, pilih fungsi "setupDatabase".
 * 6. Tekan tombol 'Jalankan' (Run).
 * 7. Berikan otorisasi izin akses akun Google pertama kali.
 * 8. Periksa tab-tab Spreadsheet: 7 sheet & 12 paket awal terisi rapi!
 */

var DRIVE_FOLDER_NAME = "DATA_PELANGGAN_MYREPUBLIC";

// DAFTAR 12 PAKET AWAL MYREPUBLIC
var INITIAL_PACKAGES = [
  { id: "PKG-01", nama: "RING", kecepatan: "30 Mbps", harga: "Rp116.000", promo: "Termasuk PPN 11%", aktif: true, sumber: "Brosur Promosi (Perlu Konfirmasi)", verifikasi: "Belum Diverifikasi", catatan: "Data awal" },
  { id: "PKG-02", nama: "PAS", kecepatan: "75 Mbps", harga: "Rp139.000", promo: "Termasuk PPN 11%", aktif: true, sumber: "Brosur Promosi (Perlu Konfirmasi)", verifikasi: "Belum Diverifikasi", catatan: "Data awal" },
  { id: "PKG-03", nama: "ECO", kecepatan: "100 Mbps", harga: "Rp166.500", promo: "Termasuk PPN 11%", aktif: true, sumber: "Brosur Promosi (Perlu Konfirmasi)", verifikasi: "Belum Diverifikasi", catatan: "Data awal" },
  { id: "PKG-04", nama: "NEO", kecepatan: "200 Mbps", harga: "Rp233.100", promo: "Free speed upgrade & modem (Termasuk PPN 11%)", aktif: true, sumber: "Brosur Promosi (Perlu Konfirmasi)", verifikasi: "Belum Diverifikasi", catatan: "Data awal" },
  { id: "PKG-05", nama: "VELO", kecepatan: "300 Mbps", harga: "Rp277.500", promo: "Free speed upgrade & modem (Termasuk PPN 11%)", aktif: true, sumber: "Brosur Promosi (Perlu Konfirmasi)", verifikasi: "Belum Diverifikasi", catatan: "Data awal" },
  { id: "PKG-06", nama: "NEXUS", kecepatan: "400 Mbps", harga: "Rp333.000", promo: "Free speed upgrade & modem (Termasuk PPN 11%)", aktif: true, sumber: "Brosur Promosi (Perlu Konfirmasi)", verifikasi: "Belum Diverifikasi", catatan: "Data awal" },
  { id: "PKG-07", nama: "SILVER", kecepatan: "350 Mbps", harga: "Rp444.000", promo: "IP Public Static + WiFi (Termasuk PPN 11%)", aktif: true, sumber: "Brosur Promosi (Perlu Konfirmasi)", verifikasi: "Belum Diverifikasi", catatan: "Data awal" },
  { id: "PKG-08", nama: "PRIME", kecepatan: "500 Mbps", harga: "Rp555.000", promo: "Free Vidio Platinum Lite (Termasuk PPN 11%)", aktif: true, sumber: "Brosur Promosi (Perlu Konfirmasi)", verifikasi: "Belum Diverifikasi", catatan: "Data awal" },
  { id: "PKG-09", nama: "GOLD", kecepatan: "500 Mbps", harga: "Rp610.500", promo: "IP Public Static + WiFi (Termasuk PPN 11%)", aktif: true, sumber: "Brosur Promosi (Perlu Konfirmasi)", verifikasi: "Belum Diverifikasi", catatan: "Data awal" },
  { id: "PKG-10", nama: "WONDER", kecepatan: "750 Mbps", harga: "Rp721.000", promo: "Free Vidio Platinum Lite (Termasuk PPN 11%)", aktif: true, sumber: "Brosur Promosi (Perlu Konfirmasi)", verifikasi: "Belum Diverifikasi", catatan: "Data awal" },
  { id: "PKG-11", nama: "ULTRA", kecepatan: "1 Gbps", harga: "Rp943.500", promo: "Free Vidio Platinum Lite (Termasuk PPN 11%)", aktif: true, sumber: "Brosur Promosi (Perlu Konfirmasi)", verifikasi: "Belum Diverifikasi", catatan: "Data awal" },
  { id: "PKG-12", nama: "PLATINUM", kecepatan: "1 Gbps", harga: "Rp999.000", promo: "IP Public Static + WiFi (Termasuk PPN 11%)", aktif: true, sumber: "Brosur Promosi (Perlu Konfirmasi)", verifikasi: "Belum Diverifikasi", catatan: "Data awal" }
];

/**
 * FASE 1: Menyiapkan 7 Sheet dan menginisialisasi 12 Paket Awal secara aman
 */
function setupDatabase() {
  var lock = LockService.getScriptLock();
  try {
    lock.waitLock(30000); // Kunci 30 detik untuk mencegah benturan (LockService)
  } catch (e) {
    Logger.log("Sistem sibuk, silakan coba lagi.");
    return "Error: LockService timeout";
  }

  try {
    var ss = SpreadsheetApp.getActiveSpreadsheet();

    // 1. Sheet "Pelanggan"
    setupSheet(ss, "Pelanggan", [
      "ID Pelanggan", "Tanggal Input", "Nama", "Nomor HP", "Alamat",
      "Email", "NIK", "Foto KTP", "Paket WiFi", "Kecepatan Snapshot", "Harga Snapshot", "Promo Snapshot",
      "Foto Meteran", "Foto Rumah Depan", "Foto Rumah Kanan", "Foto Rumah Kiri", "Foto Homepass",
      "Kode Homepass", "Alamat Homepass", "Status Pengerjaan", "Status Pembayaran",
      "Pipeline Sales", "Jadwal FollowUp", "Catatan", "Petugas", "Terakhir Diperbarui"
    ], "#C40510");

    // 2. Sheet "Paket" (12 Paket Awal)
    var paketSheet = setupSheet(ss, "Paket", [
      "ID Paket", "Nama Paket", "Kecepatan", "Harga Bulanan", "Keterangan Promo",
      "Status Aktif", "Sumber Harga", "Tanggal Verifikasi", "Catatan Admin"
    ], "#C40510");

    initPackagesNoDuplicate(paketSheet);

    // 3. Sheet "Aktivitas"
    setupSheet(ss, "Aktivitas", [
      "ID Aktivitas", "Timestamp", "ID Pelanggan", "Nama Pelanggan", "Pengguna / Petugas", "Jenis Aktivitas", "Ringkasan Perubahan"
    ], "#263238");

    // 4. Sheet "FollowUp"
    setupSheet(ss, "FollowUp", [
      "ID FollowUp", "ID Pelanggan", "Tanggal Jadwal", "Jenis Komunikasi", "Catatan", "Hasil", "Tanggal Tindak Lanjut Berikutnya", "Petugas"
    ], "#263238");

    // 5. Sheet "Pembayaran"
    setupSheet(ss, "Pembayaran", [
      "ID Pembayaran", "ID Pelanggan", "Tanggal Pembayaran", "Nominal", "Metode Pembayaran", "Nomor Referensi", "Petugas Pencatat", "Status Verifikasi", "Catatan"
    ], "#263238");

    // 6. Sheet "Pengaturan"
    setupSheet(ss, "Pengaturan", [
      "Kunci", "Nilai", "Keterangan Terakhir Diperbarui"
    ], "#37474F");

    // 7. Sheet "Pengguna"
    setupSheet(ss, "Pengguna", [
      "Email / ID Petugas", "Nama Lengkap", "Role (ADMIN/SALES/TEKNISI/FINANCE)", "Status Aktif", "Tanggal Terdaftar"
    ], "#37474F");

    // Pastikan folder Google Drive khusus foto ada
    getOrCreateDriveFolder();

    // Hapus sheet default "Sheet1" jika ada
    var defaultSheet = ss.getSheetByName("Sheet1");
    if (defaultSheet && ss.getSheets().length > 1) {
      try { ss.deleteSheet(defaultSheet); } catch (e) {}
    }

    Logger.log("SETUP DATABASE BERHASIL: 7 Sheet & 12 Paket MyRepublic siap digunakan!");
    return "SUCCESS: 7 Sheet dan 12 Paket Awal MyRepublic berhasil dibuat tanpa duplikasi.";
  } finally {
    lock.releaseLock();
  }
}

function setupSheet(ss, sheetName, headers, headerColor) {
  var sheet = ss.getSheetByName(sheetName);
  if (!sheet) {
    sheet = ss.insertSheet(sheetName);
  }

  // Cek apakah header sudah ada
  if (sheet.getLastRow() === 0) {
    sheet.appendRow(headers);
    sheet.getRange(1, 1, 1, headers.length)
      .setFontWeight("bold")
      .setBackground(headerColor)
      .setFontColor("#FFFFFF");
    sheet.setFrozenRows(1);
  }
  return sheet;
}

/**
 * Inisialisasi 12 Paket Awal dengan PENCEGAHAN DUPLIKASI
 */
function initPackagesNoDuplicate(paketSheet) {
  var values = paketSheet.getDataRange().getValues();
  var existingIds = {};

  // Baca ID Paket yang sudah ada
  for (var i = 1; i < values.length; i++) {
    var id = String(values[i][0]).trim();
    if (id) {
      existingIds[id] = true;
    }
  }

  var rowsToAppend = [];
  for (var j = 0; j < INITIAL_PACKAGES.length; j++) {
    var p = INITIAL_PACKAGES[j];
    if (!existingIds[p.id]) {
      rowsToAppend.push([
        p.id,
        p.nama,
        p.kecepatan,
        p.harga,
        p.promo,
        p.aktif ? "AKTIF" : "NONAKTIF",
        p.sumber,
        p.verifikasi,
        p.catatan
      ]);
    }
  }

  if (rowsToAppend.length > 0) {
    paketSheet.getRange(paketSheet.getLastRow() + 1, 1, rowsToAppend.length, 9).setValues(rowsToAppend);
  }
}

function getOrCreateDriveFolder() {
  var folders = DriveApp.getFoldersByName(DRIVE_FOLDER_NAME);
  if (folders.hasNext()) {
    return folders.next();
  }
  return DriveApp.createFolder(DRIVE_FOLDER_NAME);
}

// REST API Web App Gateway
function doGet(e) {
  var action = (e && e.parameter && e.parameter.action) ? e.parameter.action : "ping";
  if (action === "ping") {
    return ContentService.createTextOutput(JSON.stringify({
      status: "success",
      message: "API Google Sheets MyRepublic Siap",
      database: "7 Sheet Siap (Pelanggan, Paket, Aktivitas, FollowUp, Pembayaran, Pengaturan, Pengguna)",
      timestamp: new Date().toISOString()
    })).setMimeType(ContentService.MimeType.JSON);
  } else if (action === "getPackages") {
    return handleGetPackages();
  } else if (action === "getAll") {
    return handleGetAll();
  }
  return ContentService.createTextOutput(JSON.stringify({ status: "error", message: "Action tidak dikenal" })).setMimeType(ContentService.MimeType.JSON);
}

function doPost(e) {
  try {
    var rawData = e.postData.contents;
    var data = JSON.parse(rawData);
    var action = data.action;

    if (action === "ping") {
      return ContentService.createTextOutput(JSON.stringify({ status: "success", message: "Pong" })).setMimeType(ContentService.MimeType.JSON);
    } else if (action === "setup") {
      var res = setupDatabase();
      return ContentService.createTextOutput(JSON.stringify({ status: "success", message: res })).setMimeType(ContentService.MimeType.JSON);
    } else if (action === "getPackages") {
      return handleGetPackages();
    } else if (action === "getAll") {
      return handleGetAll();
    } else if (action === "save") {
      return handleSaveCustomer(data.customer);
    } else if (action === "uploadPhoto") {
      return handleUploadPhoto(data.customerId, data.photoType, data.base64, data.mimeType);
    }

    return ContentService.createTextOutput(JSON.stringify({ status: "error", message: "Action tidak dikenali" })).setMimeType(ContentService.MimeType.JSON);
  } catch (err) {
    return ContentService.createTextOutput(JSON.stringify({ status: "error", message: err.toString() })).setMimeType(ContentService.MimeType.JSON);
  }
}

function handleGetPackages() {
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var sheet = ss.getSheetByName("Paket");
  if (!sheet) {
    setupDatabase();
    sheet = ss.getSheetByName("Paket");
  }
  var values = sheet.getDataRange().getValues();
  var list = [];
  for (var i = 1; i < values.length; i++) {
    var r = values[i];
    if (!r[0]) continue;
    list.push({
      id: String(r[0]),
      name: String(r[1]),
      speed: String(r[2]),
      priceEstimate: String(r[3]),
      promo: String(r[4]),
      isActive: String(r[5]).toUpperCase() === "AKTIF",
      sumberHarga: String(r[6]),
      tanggalVerifikasi: String(r[7])
    });
  }
  return ContentService.createTextOutput(JSON.stringify({ status: "success", packages: list })).setMimeType(ContentService.MimeType.JSON);
}

function handleGetAll() {
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var sheet = ss.getSheetByName("Pelanggan");
  if (!sheet) {
    setupDatabase();
    sheet = ss.getSheetByName("Pelanggan");
  }
  var values = sheet.getDataRange().getValues();
  var result = [];

  for (var i = 1; i < values.length; i++) {
    var row = values[i];
    if (!row[0]) continue;
    result.push({
      id: String(row[0] || ""),
      tanggalInput: String(row[1] || ""),
      nama: String(row[2] || ""),
      nomorHp: String(row[3] || ""),
      alamat: String(row[4] || ""),
      email: String(row[5] || ""),
      nik: String(row[6] || ""),
      fotoKtp: String(row[7] || ""),
      paketWifi: String(row[8] || ""),
      fotoMeteran: String(row[12] || ""),
      fotoRumahDepan: String(row[13] || ""),
      fotoRumahKanan: String(row[14] || ""),
      fotoRumahKiri: String(row[15] || ""),
      fotoHomepass: String(row[16] || ""),
      kodeHomepass: String(row[17] || ""),
      alamatHomepass: String(row[18] || ""),
      statusPengerjaan: String(row[19] || "BAHAN"),
      statusPembayaran: String(row[20] || "UNPAID"),
      catatan: String(row[23] || ""),
      petugas: String(row[24] || ""),
      terakhirDiperbarui: String(row[25] || "")
    });
  }

  return ContentService.createTextOutput(JSON.stringify({
    status: "success",
    count: result.length,
    data: result
  })).setMimeType(ContentService.MimeType.JSON);
}

function handleSaveCustomer(cust) {
  if (!cust || !cust.id) {
    return ContentService.createTextOutput(JSON.stringify({ status: "error", message: "ID Pelanggan wajib diisi" })).setMimeType(ContentService.MimeType.JSON);
  }

  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var sheet = ss.getSheetByName("Pelanggan");
  if (!sheet) {
    setupDatabase();
    sheet = ss.getSheetByName("Pelanggan");
  }

  var values = sheet.getDataRange().getValues();
  var rowIndex = -1;

  for (var i = 1; i < values.length; i++) {
    if (String(values[i][0]) === String(cust.id)) {
      rowIndex = i + 1;
      break;
    }
  }

  var nowStr = Utilities.formatDate(new Date(), Session.getScriptTimeZone() || "GMT+7", "yyyy-MM-dd HH:mm:ss");
  var rowData = [
    cust.id,
    cust.tanggalInput || nowStr,
    cust.nama || "",
    cust.nomorHp || "",
    cust.alamat || "",
    cust.email || "",
    cust.nik || "",
    cust.fotoKtp || "",
    cust.paketWifi || "",
    cust.kecepatan || "",
    cust.hargaSnapshot || "",
    cust.promoSnapshot || "",
    cust.fotoMeteran || "",
    cust.fotoRumahDepan || "",
    cust.fotoRumahKanan || "",
    cust.fotoRumahKiri || "",
    cust.fotoHomepass || "",
    cust.kodeHomepass || "",
    cust.alamatHomepass || "",
    cust.statusPengerjaan || "BAHAN",
    cust.statusPembayaran || "UNPAID",
    cust.pipelineSales || "PROSPEK",
    cust.jadwalFollowUp || "",
    cust.catatan || "",
    cust.updatedBy || "Petugas",
    nowStr
  ];

  if (rowIndex > 0) {
    sheet.getRange(rowIndex, 1, 1, rowData.length).setValues([rowData]);
  } else {
    sheet.appendRow(rowData);
  }

  return ContentService.createTextOutput(JSON.stringify({
    status: "success",
    message: "Data pelanggan berhasil disimpan di Google Sheets",
    id: cust.id
  })).setMimeType(ContentService.MimeType.JSON);
}

function handleUploadPhoto(customerId, photoType, base64Data, mimeType) {
  var folder = getOrCreateDriveFolder();
  var decoded = Utilities.base64Decode(base64Data);
  var ext = (mimeType && mimeType.indexOf("png") >= 0) ? ".png" : ".jpg";
  var fileName = customerId + "_" + photoType + "_" + new Date().getTime() + ext;
  var blob = Utilities.newBlob(decoded, mimeType || "image/jpeg", fileName);
  var file = folder.createFile(blob);

  // Akses terbatas (hanya via tautan berwenang)
  file.setSharing(DriveApp.Access.ANYONE_WITH_LINK, DriveApp.Permission.VIEW);
  return ContentService.createTextOutput(JSON.stringify({
    status: "success",
    fileId: file.getId(),
    fileUrl: file.getUrl(),
    photoType: photoType
  })).setMimeType(ContentService.MimeType.JSON);
}
""".trimIndent()
}
