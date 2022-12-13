package com.example.musicdownload.data.repository

import com.example.musicdownload.data.model.ResponseListened
import com.example.musicdownload.network.RetrofitInstance

class Repostitory {
    suspend fun getToplistenedCrotines() : ResponseListened {
        return RetrofitInstance.api.getToplistenedCrotines()
    }
}