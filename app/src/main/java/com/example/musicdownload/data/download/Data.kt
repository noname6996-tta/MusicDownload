package com.example.musicdownload.data.download

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.example.musicdownload.data.model.Music
import com.tonyodev.fetch2.Request

object Data {
    var path = ArrayList<String>()
    var listDownload = ArrayList<Music>()

    fun getFetchRequests(context: Context): List<Request> {
        val requests = ArrayList<Request>()
        for (i in 0.. listDownload.size-1) {
            val request = Request(listDownload[i].audioDownload, getFilePath(listDownload[i].audioDownload, context,listDownload[i].name))
            requests.add(request)
        }
        return requests
    }

    fun getFetchRequestWithGroupId(groupId: Int, context: Context): List<Request> {
        val requests : List<Request> = getFetchRequests(context)
        for (request in requests) {
            request.groupId = groupId
        }
        return requests
    }

    fun getFilePath(url: String, context: Context,name:String): String {
        val uri = Uri.parse(url)
        val fileName = name
        val dir = getSaveDir(context)
        var urlPath = "$dir/DownloadList/$fileName.mp3"
        path.add(urlPath)
        Log.e("path_song", urlPath)
        return "$dir/DownloadList/$fileName.mp3"
    }


    fun getSaveDir(context: Context?): String {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .toString() + ""
    }
}