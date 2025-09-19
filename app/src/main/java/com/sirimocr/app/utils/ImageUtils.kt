package com.sirimocr.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Environment
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImageUtils(private val context: Context) {

    private val ioDispatcher = Dispatchers.IO

    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: File(context.filesDir, "images")
        val storageDir = File(baseDir, "SirimOcr")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        return File(storageDir, "SIRIM_${'$'}timeStamp.jpg")
    }

    suspend fun prepareImage(file: File): String = withContext(ioDispatcher) {
        require(file.exists()) { "Image file missing" }
        val path = file.absolutePath
        val bitmap = decodeScaledBitmap(path)
        val rotated = applyExifRotation(path, bitmap)
        FileOutputStream(file).use { out ->
            rotated.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        if (rotated !== bitmap) {
            bitmap.recycle()
        }
        rotated.recycle()
        path
    }

    private fun decodeScaledBitmap(path: String): Bitmap {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, options)
        options.inSampleSize = calculateInSampleSize(options, 1920, 1920)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(path, options)
            ?: throw IOException("Unable to decode image at $path")
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            var halfHeight = height / 2
            var halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun applyExifRotation(path: String, bitmap: Bitmap): Bitmap {
        return try {
            val exif = ExifInterface(path)
            val rotation = when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
            if (rotation == 0f) {
                bitmap
            } else {
                val matrix = Matrix().apply { postRotate(rotation) }
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }
        } catch (_: IOException) {
            bitmap
        }
    }
}
