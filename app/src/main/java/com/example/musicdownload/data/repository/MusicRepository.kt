package com.example.musicdownload.data.repository

import com.example.musicdownload.network.RetrofitService
import com.example.musicdownload.view.fragment.GenresMusicListFragment

class MusicRepository constructor(private val retrofitService: RetrofitService) {
    fun getDownloadHome() = retrofitService.getDownloadHome()
    fun getRankingHome() = retrofitService.getRanking()
    fun getToplistenedHome() = retrofitService.getToplistened()
    fun getTopDownloadHome() = retrofitService.getTopDownload()
    fun getGenres() = retrofitService.getGenres()
    fun getMusicByGenres(name:String) = retrofitService.getMusicByGenres(name)
}