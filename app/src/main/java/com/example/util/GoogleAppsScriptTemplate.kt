package com.example.util

object GoogleAppsScriptTemplate {

    const val SHEET_COLUMNS_INFO = """
1. ID Pelanggan
2. Tanggal Input
3. Nama
4. Nomor HP
5. Alamat
6. Email
7. NIK
8. Foto KTP
9. Paket WiFi
10. Foto Meteran
11. Foto Rumah Depan
12. Foto Rumah Kanan
13. Foto Rumah Kiri
14. Foto Homepass
15. Kode Homepass
16. Alamat Homepass
17. Status Pengerjaan
18. Status Pembayaran
19. Catatan
20. Terakhir Diperbarui
"""

    val SCRIPT_CODE = """
/**
 * BACKEND API GOOGLE APPS SCRIPT - DATA PELANGGAN MYREPUBLIC
 * Hubungkan Spreadsheet dan Google Drive untuk pendataan pelanggan WiFi.
 * 
 * CARA PASANG:
 * 1. Buka spreadsheet baru di https://sheets.google.com
 * 2. Klik menu 'Ekstensi' (Extensions) -> 'Apps Script'
 * 3. Hapus kode bawaan, lalu tempel SELURUH kode ini.
 * 4. Klik tombol 'Terapkan' (Deploy) -> 'Penerapan Baru' (New Deployment).
 * 5. Pilih jenis 'Aplikasi Web' (Web App).
 * 6. Jalankan sebagai: 'Saya' (Me).
 * 7. Siapa yang memiliki akses: 'Siapa saja' (Anyone).
 * 8. Klik 'Terapkan', izinkan akses Google, lalu Salin URL Aplikasi Web yang dihasilkan.
 * 9. Masukkan URL tersebut ke menu Pengaturan di aplikasi DATA PELANGGAN.
 */

var SHEET_NAME = "Pelanggan";
var DRIVE_FOLDER_NAME = "DATA_PELANGGAN_MYREPUBLIC";

var HEADERS = [
  "ID Pelanggan", "Tanggal Input", "Nama", "Nomor HP", "Alamat",
  "Email", "NIK", "Foto KTP", "Paket WiFi", "Foto Meteran",
  "Foto Rumah Depan", "Foto Rumah Kanan", "Foto Rumah Kiri", "Foto Homepass",
  "Kode Homepass", "Alamat Homepass", "Status Pengerjaan", "Status Pembayaran",
  "Catatan", "Terakhir Diperbarui"
];

function getOrCreateSheet() {
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var sheet = ss.getSheetByName(SHEET_NAME);
  if (!sheet) {
    sheet = ss.insertSheet(SHEET_NAME);
    sheet.appendRow(HEADERS);
    sheet.getRange(1, 1, 1, HEADERS.length).setFontWeight("bold").setBackground("#C40510").setFontColor("#FFFFFF");
    sheet.setFrozenRows(1);
  }
  return sheet;
}

function getOrCreateFolder() {
  var folders = DriveApp.getFoldersByName(DRIVE_FOLDER_NAME);
  if (folders.hasNext()) {
    return folders.next();
  }
  return DriveApp.createFolder(DRIVE_FOLDER_NAME);
}

function doGet(e) {
  var action = (e && e.parameter && e.parameter.action) ? e.parameter.action : "ping";
  if (action === "ping") {
    return ContentService.createTextOutput(JSON.stringify({
      status: "success",
      message: "Koneksi Google Sheets & Drive MyRepublic Aktif",
      timestamp: new Date().toISOString()
    })).setMimeType(ContentService.MimeType.JSON);
  } else if (action === "getAll") {
    return handleGetAll();
  }
  return ContentService.createTextOutput(JSON.stringify({ status: "error", message: "Action tidak valid" })).setMimeType(ContentService.MimeType.JSON);
}

function doPost(e) {
  try {
    var rawData = e.postData.contents;
    var data = JSON.parse(rawData);
    var action = data.action;

    if (action === "ping") {
      return ContentService.createTextOutput(JSON.stringify({ status: "success", message: "Pong" })).setMimeType(ContentService.MimeType.JSON);
    } else if (action === "getAll") {
      return handleGetAll();
    } else if (action === "save") {
      return handleSaveCustomer(data.customer);
    } else if (action === "updateStatus") {
      return handleUpdateStatus(data.id, data.statusPengerjaan, data.statusPembayaran, data.updatedBy);
    } else if (action === "delete") {
      return handleDeleteCustomer(data.id);
    } else if (action === "uploadPhoto") {
      return handleUploadPhoto(data.customerId, data.photoType, data.base64, data.mimeType);
    }

    return ContentService.createTextOutput(JSON.stringify({ status: "error", message: "Action tidak dikenali" })).setMimeType(ContentService.MimeType.JSON);
  } catch (err) {
    return ContentService.createTextOutput(JSON.stringify({ status: "error", message: err.toString() })).setMimeType(ContentService.MimeType.JSON);
  }
}

function handleGetAll() {
  var sheet = getOrCreateSheet();
  var values = sheet.getDataRange().getValues();
  var result = [];

  if (values.length > 1) {
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
        fotoMeteran: String(row[9] || ""),
        fotoRumahDepan: String(row[10] || ""),
        fotoRumahKanan: String(row[11] || ""),
        fotoRumahKiri: String(row[12] || ""),
        fotoHomepass: String(row[13] || ""),
        kodeHomepass: String(row[14] || ""),
        alamatHomepass: String(row[15] || ""),
        statusPengerjaan: String(row[16] || "BAHAN"),
        statusPembayaran: String(row[17] || "UNPAID"),
        catatan: String(row[18] || ""),
        terakhirDiperbarui: String(row[19] || "")
      });
    }
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

  var sheet = getOrCreateSheet();
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
    cust.fotoMeteran || "",
    cust.fotoRumahDepan || "",
    cust.fotoRumahKanan || "",
    cust.fotoRumahKiri || "",
    cust.fotoHomepass || "",
    cust.kodeHomepass || "",
    cust.alamatHomepass || "",
    cust.statusPengerjaan || "BAHAN",
    cust.statusPembayaran || "UNPAID",
    cust.catatan || "",
    nowStr
  ];

  if (rowIndex > 0) {
    // Update baris yang sudah ada (mencegah duplikat!)
    sheet.getRange(rowIndex, 1, 1, rowData.length).setValues([rowData]);
  } else {
    // Tambah baris baru
    sheet.appendRow(rowData);
  }

  return ContentService.createTextOutput(JSON.stringify({
    status: "success",
    message: "Data pelanggan berhasil disimpan di Google Sheets",
    id: cust.id
  })).setMimeType(ContentService.MimeType.JSON);
}

function handleUpdateStatus(id, statusPengerjaan, statusPembayaran, updatedBy) {
  var sheet = getOrCreateSheet();
  var values = sheet.getDataRange().getValues();
  var rowIndex = -1;

  for (var i = 1; i < values.length; i++) {
    if (String(values[i][0]) === String(id)) {
      rowIndex = i + 1;
      break;
    }
  }

  if (rowIndex < 0) {
    return ContentService.createTextOutput(JSON.stringify({ status: "error", message: "Pelanggan tidak ditemukan" })).setMimeType(ContentService.MimeType.JSON);
  }

  var nowStr = Utilities.formatDate(new Date(), Session.getScriptTimeZone() || "GMT+7", "yyyy-MM-dd HH:mm:ss");
  if (statusPengerjaan) {
    sheet.getRange(rowIndex, 17).setValue(statusPengerjaan);
  }
  if (statusPembayaran) {
    sheet.getRange(rowIndex, 18).setValue(statusPembayaran);
  }
  sheet.getRange(rowIndex, 20).setValue(nowStr + (updatedBy ? " (" + updatedBy + ")" : ""));

  return ContentService.createTextOutput(JSON.stringify({
    status: "success",
    message: "Status berhasil diperbarui"
  })).setMimeType(ContentService.MimeType.JSON);
}

function handleDeleteCustomer(id) {
  var sheet = getOrCreateSheet();
  var values = sheet.getDataRange().getValues();
  for (var i = 1; i < values.length; i++) {
    if (String(values[i][0]) === String(id)) {
      sheet.deleteRow(i + 1);
      return ContentService.createTextOutput(JSON.stringify({ status: "success", message: "Data pelanggan berhasil dihapus" })).setMimeType(ContentService.MimeType.JSON);
    }
  }
  return ContentService.createTextOutput(JSON.stringify({ status: "error", message: "Data tidak ditemukan" })).setMimeType(ContentService.MimeType.JSON);
}

function handleUploadPhoto(customerId, photoType, base64Data, mimeType) {
  var folder = getOrCreateFolder();
  var decoded = Utilities.base64Decode(base64Data);
  var ext = (mimeType && mimeType.indexOf("png") >= 0) ? ".png" : ".jpg";
  var fileName = customerId + "_" + photoType + "_" + new Date().getTime() + ext;
  var blob = Utilities.newBlob(decoded, mimeType || "image/jpeg", fileName);
  var file = folder.createFile(blob);

  // Atur izin baca
  file.setSharing(DriveApp.Access.ANYONE_WITH_LINK, DriveApp.Permission.VIEW);
  var fileUrl = file.getUrl();

  return ContentService.createTextOutput(JSON.stringify({
    status: "success",
    fileId: file.getId(),
    fileUrl: fileUrl,
    photoType: photoType
  })).setMimeType(ContentService.MimeType.JSON);
}
""".trimIndent()
}
