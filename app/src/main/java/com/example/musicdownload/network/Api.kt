package com.example.musicdownload.network

import com.example.musicdownload.data.model.RespondGenre
import com.example.musicdownload.data.model.ResponseListened
import retrofit2.http.GET
import retrofit2.http.Query

interface Api {
    @GET("msd/music/listened?offset=0")
    suspend fun getToplistenedCrotines(): ResponseListened

    @GET("msd/scmusic/download?offset=0&country=za")
    suspend fun getDownloadHome(): ResponseListened

    @GET("msd/music/popular?offset=0")
    suspend fun getRanking(): ResponseListened

    @GET("msd/music/download?offset=0")
    suspend fun getTopDownload(): ResponseListened

    @GET("msd/scmusic/genre")
    suspend fun getGenres(): RespondGenre

    @GET("http://marstechstudio.com/msd/scmusic/bygenre?offset=0&country=za&")
    suspend fun getMusicByGenres(@Query("query") genresName: String): ResponseListened

    @GET("http://marstechstudio.com/msd/music/search?")
    suspend fun searchByString(@Query("query") genresName: String): ResponseListened
}