package com.example.musicdownload.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.musicdownload.data.database.PlayListDataBase
import com.example.musicdownload.data.model.MusicPlaylist
import com.example.musicdownload.data.model.PlayList
import com.example.musicdownload.data.model.Search
import com.example.musicdownload.data.repository.MusicPlaylistRepository
import com.example.musicdownload.data.repository.PlayListRepository
import com.example.musicdownload.data.repository.SearchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlayListViewModel(application: Application) : AndroidViewModel(application) {

    val readAllData: LiveData<List<PlayList>>
    val readAllMusicData: LiveData<List<MusicPlaylist>>
    private val repositoryPlaylist: PlayListRepository
    private val repositoryMusicPlaylist: MusicPlaylistRepository

    init {
        val playListDao = PlayListDataBase.getDatabase(application).playDao()
        repositoryPlaylist = PlayListRepository(playListDao)
        readAllData = repositoryPlaylist.readAllData

        val musicPlayListDao = PlayListDataBase.getDatabase(application).musicPlayDao()
        repositoryMusicPlaylist = MusicPlaylistRepository(musicPlayListDao)
        readAllMusicData = repositoryMusicPlaylist.readAllDataMusic
    }

    fun addPlayList(playList: PlayList) {
        viewModelScope.launch(Dispatchers.IO) {
            repositoryPlaylist.addPlayList(playList)
        }
    }

    fun updatePlaylist(playList: PlayList) {
        viewModelScope.launch(Dispatchers.IO) {
            repositoryPlaylist.updatePlaylist(playList)
        }
    }

    fun deletePlaylist(playList: PlayList) {
        viewModelScope.launch(Dispatchers.IO) {
            repositoryPlaylist.deletePlaylist(playList)
        }
    }

    fun deleteAllPlaylist() {
        viewModelScope.launch(Dispatchers.IO) {
            repositoryPlaylist.deleteAllPlaylist()
        }
    }
}
class MusicPlayListViewModel(application: Application) : AndroidViewModel(application) {

    val readAllMusicData: LiveData<List<MusicPlaylist>>
    private val repositoryMusicPlaylist: MusicPlaylistRepository

    init {

        val musicPlayListDao = PlayListDataBase.getDatabase(application).musicPlayDao()
        repositoryMusicPlaylist = MusicPlaylistRepository(musicPlayListDao)
        readAllMusicData = repositoryMusicPlaylist.readAllDataMusic
    }

    fun addMusicPlayList(musicPlaylist: MusicPlaylist) {
        viewModelScope.launch(Dispatchers.IO) {
            repositoryMusicPlaylist.addMusicPlayList(musicPlaylist)
        }
    }

    fun updateMusicPlaylist(musicPlaylist: MusicPlaylist) {
        viewModelScope.launch(Dispatchers.IO) {
            repositoryMusicPlaylist.updateMusicPlaylist(musicPlaylist)
        }
    }

    fun deleteMusicPlaylist(musicPlaylist: MusicPlaylist) {
        viewModelScope.launch(Dispatchers.IO) {
            repositoryMusicPlaylist.deleteMusicPlaylist(musicPlaylist)
        }
    }

    fun deleteMusicPlaylistWithId(id:Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repositoryMusicPlaylist.deleteMusicPlaylistWithid(id)
        }
    }

    fun deleteAllMusicPlaylist() {
        viewModelScope.launch(Dispatchers.IO) {
            repositoryMusicPlaylist.deleteAllMusicPlaylist()
        }
    }
}
class SearchViewModel(application: Application) : AndroidViewModel(application) {

    val readAllMusicData: LiveData<List<Search>>
    private val repositoryMusicPlaylist: SearchRepository

    init {

        val searchDao = PlayListDataBase.getDatabase(application).searchDao()
        repositoryMusicPlaylist = SearchRepository(searchDao)
        readAllMusicData = repositoryMusicPlaylist.readAllDataMusic
    }

    fun addSearchList(search: Search) {
        viewModelScope.launch(Dispatchers.IO) {
            repositoryMusicPlaylist.addMusicPlayList(search)
        }
    }

    fun deleteSearchlist(search: Search) {
        viewModelScope.launch(Dispatchers.IO) {
            repositoryMusicPlaylist.deleteMusicPlaylist(search)
        }
    }

    fun deleteAllSearchlist() {
        viewModelScope.launch(Dispatchers.IO) {
            repositoryMusicPlaylist.deleteAllMusicPlaylist()
        }
    }
}