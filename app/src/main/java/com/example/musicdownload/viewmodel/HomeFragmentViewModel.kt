package com.example.musicdownload.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicdownload.data.model.Genre
import com.example.musicdownload.data.model.Music
import com.example.musicdownload.data.model.RespondGenre
import com.example.musicdownload.data.model.ResponseListened
import com.example.musicdownload.data.repository.MusicRepository
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragmentViewModel (private val musicRepository: MusicRepository) :
    ViewModel() {
    var responseListenedDownLoadHome = MutableLiveData<List<Music>>()
    var responseListenedRankingHome = MutableLiveData<List<Music>>()
    var responseListenedToplistenedHome = MutableLiveData<List<Music>>()
    var responseListenedTopDownLoadHome = MutableLiveData<List<Music>>()
    var responseListenedMusicByGenres = MutableLiveData<List<Music>>()
    var responseListenedSearchMusic = MutableLiveData<List<Music>>()
    var responseGenre = MutableLiveData<List<Genre>>()
    var errorMessage = MutableLiveData<String>()

    fun getDownloadHome() {
        val response = musicRepository.getDownloadHome()
        response.enqueue(object : Callback<ResponseListened> {
            override fun onResponse(
                call: Call<ResponseListened>,
                response: Response<ResponseListened>
            ) {
                // get all data
                responseListenedDownLoadHome.postValue(response.body()!!.data)
            }

            override fun onFailure(call: Call<ResponseListened>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    fun getRankingHome() {
        val response = musicRepository.getRankingHome()
        response.enqueue(object : Callback<ResponseListened> {
            override fun onResponse(
                call: Call<ResponseListened>,
                response: Response<ResponseListened>
            ) {
                responseListenedRankingHome.postValue(response.body()!!.data)
            }

            override fun onFailure(call: Call<ResponseListened>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    fun getToplistenedHome() {
        val response = musicRepository.getToplistenedHome()
        response.enqueue(object : Callback<ResponseListened> {
            override fun onResponse(
                call: Call<ResponseListened>,
                response: Response<ResponseListened>
            ) {
                responseListenedToplistenedHome.postValue(response.body()!!.data)
            }

            override fun onFailure(call: Call<ResponseListened>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    fun getTopDownLoadHome() {
        val response = musicRepository.getTopDownloadHome()
        response.enqueue(object : Callback<ResponseListened> {
            override fun onResponse(
                call: Call<ResponseListened>,
                response: Response<ResponseListened>
            ) {
                responseListenedTopDownLoadHome.postValue(response.body()!!.data)
            }

            override fun onFailure(call: Call<ResponseListened>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    fun getGenresHome() {
        val response = musicRepository.getGenres()
        response.enqueue(object : Callback<RespondGenre> {
            override fun onResponse(call: Call<RespondGenre>, response: Response<RespondGenre>) {
                responseGenre.postValue(response.body()!!.data)
            }

            override fun onFailure(call: Call<RespondGenre>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    fun getMusicByGenres(name: String) {
        val response = musicRepository.getMusicByGenres(name)
        response.enqueue(object : Callback<ResponseListened> {
            override fun onResponse(
                call: Call<ResponseListened>,
                response: Response<ResponseListened>
            ) {
                responseListenedMusicByGenres.postValue(response.body()!!.data)
            }

            override fun onFailure(call: Call<ResponseListened>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    fun searchByString(name: String) {
        val response = musicRepository.searchMusic(name)
        response.enqueue(object : Callback<ResponseListened> {
            override fun onResponse(
                call: Call<ResponseListened>,
                response: Response<ResponseListened>
            ) {
                responseListenedSearchMusic.postValue(response.body()!!.data)
            }

            override fun onFailure(call: Call<ResponseListened>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }
}