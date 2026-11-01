package com.example.ui.util

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast

object PhotoDownloader {
    fun downloadPhoto(context: Context, photoUrl: String, title: String = "Pramanik_Bari_Photo") {
        if (photoUrl.isBlank()) {
            Toast.makeText(context, "ছবির কোনো লিংক পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            if (downloadManager == null) {
                Toast.makeText(context, "ডাউনলোড ম্যানেজার পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
                return
            }

            val uri = Uri.parse(photoUrl)
            val fileName = "PramanikBari_${System.currentTimeMillis()}.jpg"

            val request = DownloadManager.Request(uri).apply {
                setTitle(title)
                setDescription("প্রামাণিক বাড়ি পারিবারিক অ্যালবাম থেকে ডাউনলোড হচ্ছে...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "PramanikBari/$fileName")
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }

            downloadManager.enqueue(request)
            Toast.makeText(context, "ডাউনলোড শুরু হয়েছে! গ্যালারিতে সংরক্ষিত হবে।", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "ডাউনলোড করতে সমস্যা হয়েছে: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
