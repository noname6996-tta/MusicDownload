package com.example.musicdownload.data.repository

import androidx.lifecycle.LiveData
import com.example.musicdownload.data.dao.MusicPlayListDao
import com.example.musicdownload.data.dao.PlayListDao
import com.example.musicdownload.data.dao.SearchDao
import com.example.musicdownload.data.model.MusicPlaylist
import com.example.musicdownload.data.model.PlayList
import com.example.musicdownload.data.model.Search

class PlayListRepository(private val playListDao: PlayListDao) {

    val readAllData : LiveData<List<PlayList>> = playListDao.readAllData()

    suspend fun addPlayList(playList: PlayList){
        playListDao.addPlayList(playList)
    }

    suspend fun updatePlaylist(playList: PlayList){
        playListDao.updatePlaylist(playList)
    }

    suspend fun deletePlaylist(playList: PlayList){
        playListDao.deletePlaylist(playList)
    }

    suspend fun deleteAllPlaylist(){
        playListDao.deleteAllPlaylist()
    }
}
class MusicPlaylistRepository(private val musicPlaylistDao: MusicPlayListDao){
    val readAllDataMusic : LiveData<List<MusicPlaylist>> = musicPlaylistDao.readAllMusicData()

    suspend fun addMusicPlayList(musicPlaylist: MusicPlaylist){
        musicPlaylistDao.addMusicPlayList(musicPlaylist)
    }

    suspend fun updateMusicPlaylist(musicPlaylist: MusicPlaylist){
        musicPlaylistDao.updateMusicPlaylist(musicPlaylist)
    }

    suspend fun deleteMusicPlaylist(musicPlaylist: MusicPlaylist){
        musicPlaylistDao.deleteMusicPlaylist(musicPlaylist)
    }

    suspend fun deleteMusicPlaylistWithid(id : Long){
        musicPlaylistDao.deleteMusicPlaylistWithId(id)
    }

    suspend fun deleteAllMusicPlaylist(){
        musicPlaylistDao.deleteAllMusicPlaylist()
    }
}
class SearchRepository(private val searchDao: SearchDao){
    val readAllDataMusic : LiveData<List<Search>> = searchDao.readAllSearchData()

    suspend fun addMusicPlayList(search: Search){
        searchDao.addSearch(search)
    }


    suspend fun deleteMusicPlaylist(search: Search){
        searchDao.deleteSearch(search)
    }

    suspend fun deleteAllMusicPlaylist(){
        searchDao.deleteAllSearch()
    }
}