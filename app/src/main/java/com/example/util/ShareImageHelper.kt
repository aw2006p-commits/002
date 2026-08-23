package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import java.io.OutputStream

object ShareImageHelper {
    fun shareBitmap(context: Context, bitmap: Bitmap) {
        val uri = saveBitmapToMediaStore(context, bitmap)
        if (uri != null) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "مشاركة البطاقة"))
        }
    }

    private fun saveBitmapToMediaStore(context: Context, bitmap: Bitmap): Uri? {
        val filename = "quote_${System.currentTimeMillis()}.png"
        var fos: OutputStream? = null
        var imageUri: Uri? = null

        try {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/SheikhSamir")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            
            if (imageUri != null) {
                fos = resolver.openOutputStream(imageUri)
                if (fos != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(imageUri, contentValues, null, null)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            fos?.close()
        }
        
        return imageUri
    }
}
