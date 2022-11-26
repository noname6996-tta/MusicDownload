package com.example.musicdownload.network

import com.example.musicdownload.data.model.RespondGenre
import com.example.musicdownload.data.model.ResponseListened
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL = "http://marstechstudio.com/"

interface RetrofitService {

    @GET("msd/music/listened?offset=0")
    fun getToplistened(): Call<ResponseListened>

    @GET("msd/scmusic/download?offset=0&country=za")
    fun getDownloadHome(): Call<ResponseListened>

    @GET("msd/music/popular?offset=0")
    fun getRanking() :Call<ResponseListened>

    @GET("msd/music/download?offset=0")
    fun getTopDownload() : Call<ResponseListened>

    @GET("msd/scmusic/genre")
    fun getGenres() : Call<RespondGenre>

    @GET("http://marstechstudio.com/msd/scmusic/bygenre?offset=0&country=za&")
    fun getMusicByGenres(@Query("query") genresName: String):Call<ResponseListened>

    companion object {
        var retrofitService: RetrofitService? = null

        fun getInstance(): RetrofitService {
            if (retrofitService == null) {
                val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                retrofitService = retrofit.create(RetrofitService::class.java)
            }
            return retrofitService!!
        }
    }
}