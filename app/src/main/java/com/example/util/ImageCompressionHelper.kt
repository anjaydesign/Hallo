package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageCompressionHelper {

    /**
     * Compresses image from Uri to internal storage file, ensuring max dimension <= 1280px
     * and maintaining crisp text readability for KTP/meteran while keeping size ~150-250KB.
     */
    fun processAndSaveImage(context: Context, sourceUri: Uri): File? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return null

            // Correct EXIF orientation if needed
            val rotatedBitmap = correctOrientation(context, sourceUri, originalBitmap)

            // Resize if greater than 1280px
            val maxDimension = 1280
            val width = rotatedBitmap.width
            val height = rotatedBitmap.height
            val scaledBitmap = if (width > maxDimension || height > maxDimension) {
                val ratio = minOf(maxDimension.toFloat() / width, maxDimension.toFloat() / height)
                val newWidth = (width * ratio).toInt()
                val newHeight = (height * ratio).toInt()
                Bitmap.createScaledBitmap(rotatedBitmap, newWidth, newHeight, true)
            } else {
                rotatedBitmap
            }

            // Save to internal storage directory
            val photosDir = File(context.filesDir, "customer_photos")
            if (!photosDir.exists()) {
                photosDir.mkdirs()
            }

            val targetFile = File(photosDir, "photo_${UUID.randomUUID()}.jpg")
            val outputStream = FileOutputStream(targetFile)
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 82, outputStream)
            outputStream.flush()
            outputStream.close()

            targetFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Converts a local File or Uri into Base64 string for uploading to Google Drive via Apps Script
     */
    fun fileToBase64(file: File): String? {
        return try {
            val bytes = file.readBytes()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun correctOrientation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return bitmap
            val exif = ExifInterface(inputStream)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            inputStream.close()

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                else -> return bitmap
            }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            bitmap
        }
    }

    fun getReadableFileSize(file: File): String {
        val bytes = file.length()
        return if (bytes < 1024) {
            "$bytes B"
        } else if (bytes < 1024 * 1024) {
            "${bytes / 1024} KB"
        } else {
            String.format("%.1f MB", bytes.toFloat() / (1024 * 1024))
        }
    }
}
