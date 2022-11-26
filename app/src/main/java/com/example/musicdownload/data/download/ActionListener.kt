package com.example.musicdownload.data.download

interface ActionListener {
    fun onPauseDownload(id: Int)

    fun onResumeDownload(id: Int)

    fun onRemoveDownload(id: Int)

    fun onRetryDownload(id: Int)
}