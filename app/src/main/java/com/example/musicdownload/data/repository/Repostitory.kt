package com.example.musicdownload.data.repository

import com.example.musicdownload.data.model.RespondGenre
import com.example.musicdownload.data.model.ResponseListened
import com.example.musicdownload.network.RetrofitInstance

class Repostitory {
    suspend fun getToplistenedCrotines() : ResponseListened {
        return RetrofitInstance.api.getToplistenedCrotines()
    }

    suspend fun getDownloadHome() : ResponseListened {
        return RetrofitInstance.api.getDownloadHome()
    }

    suspend fun getRanking() : ResponseListened {
        return RetrofitInstance.api.getRanking()
    }

    suspend fun getTopDownload() : ResponseListened {
        return RetrofitInstance.api.getTopDownload()
    }

    suspend fun getGenres() : RespondGenre {
        return RetrofitInstance.api.getGenres()
    }

    suspend fun getMusicByGenres(query : String) : ResponseListened {
        return RetrofitInstance.api.getMusicByGenres(query)
    }

    suspend fun searchByString(query : String) : ResponseListened {
        return RetrofitInstance.api.searchByString(query)
    }
}