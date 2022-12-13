package com.example.musicdownload.network

import com.example.musicdownload.data.model.ResponseListened
import retrofit2.http.GET

interface Api {
    @GET("msd/music/listened?offset=0")
    suspend fun getToplistenedCrotines(): ResponseListened
}