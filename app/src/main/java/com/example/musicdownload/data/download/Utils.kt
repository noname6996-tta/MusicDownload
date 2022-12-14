package com.example.musicdownload.data.download

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.example.musicdownload.R
import java.text.DecimalFormat
import java.util.*

object Utils {
    fun getMimeType(context: Context, uri: Uri): String {
        val cR = context.contentResolver
        val mime = MimeTypeMap.getSingleton()
        var type = mime.getExtensionFromMimeType(cR.getType(uri))
        if (type == null) {
            type = "*/*"
        }
        return type
    }

    @SuppressLint("StringFormatInvalid")
    fun getETAString(context: Context, etaInMilliSeconds: Long): String {
        if (etaInMilliSeconds < 0) {
            return ""
        }
        var seconds = (etaInMilliSeconds / 1000).toInt()
        val hours = (seconds / 3600).toLong()
        seconds -= (hours * 3600).toInt()
        val minutes = (seconds / 60).toLong()
        seconds -= (minutes * 60).toInt()
        return if (hours > 0) {
            context.getString(R.string.download_eta_hrs, hours, minutes, seconds)
        } else if (minutes > 0) {
            context.getString(R.string.download_eta_min, minutes, seconds)
        } else {
            context.getString(R.string.download_eta_sec, seconds)
        }
    }

    @SuppressLint("StringFormatInvalid")
    fun getDownloadSpeedString(context: Context, downloadedBytesPerSecond: Long): String {
        if (downloadedBytesPerSecond < 0) {
            return ""
        }
        val kb = downloadedBytesPerSecond.toDouble() / 1000.0
        val mb = kb / 1000.0
        val decimalFormat = DecimalFormat(".##")
        return if (mb >= 1) {
            context.getString(R.string.download_speed_mb, decimalFormat.format(mb))
        } else if (kb >= 1) {
            context.getString(R.string.download_speed_kb, decimalFormat.format(kb))
        } else {
            context.getString(R.string.download_speed_bytes, downloadedBytesPerSecond)
        }
    }
    fun getProgressDisplayLine(currentBytes: Long, totalBytes: Long): String? {
        return getBytesToMBString(currentBytes) + "/" + getBytesToMBString(totalBytes)
    }

    private fun getBytesToMBString(bytes: Long): String {
        return java.lang.String.format(Locale.ENGLISH, "%.2fMb", bytes / (1024.00 * 1024.00))
    }
}