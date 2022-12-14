package com.example.musicdownload.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicdownload.data.model.Music
import com.example.musicdownload.data.repository.Repostitory
import kotlinx.coroutines.launch

class MainViewModel(private val repostitory: Repostitory): ViewModel() {

    val TopListenedHome = MutableLiveData<List<Music>>()

    fun getTopListenedHomeCrotines(){
        viewModelScope.launch {
            val response = repostitory.getToplistenedCrotines()
            TopListenedHome.value = response.data
        }
    }
}