package com.example.musicdownload.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.musicdownload.data.model.MusicPlaylist
import com.example.musicdownload.data.model.PlayList
import com.example.musicdownload.data.model.Search

@Dao
interface PlayListDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addPlayList(playList: PlayList)

    @Update
    suspend fun updatePlaylist(playList: PlayList)

    @Delete
    suspend fun deletePlaylist(playList: PlayList)

    @Query("DELETE FROM playlist_table")
    suspend fun deleteAllPlaylist()

    @Query("SELECT * FROM playlist_table ORDER BY id ASC")
    fun readAllData(): LiveData<List<PlayList>>
}

@Dao
interface MusicPlayListDao{

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addMusicPlayList(musicPlaylist: MusicPlaylist)

    @Update
    suspend fun updateMusicPlaylist(musicPlaylist: MusicPlaylist)

    @Delete
    suspend fun deleteMusicPlaylist(musicPlaylist: MusicPlaylist)

    @Query("DELETE FROM music_playlist_table WHERE id = :id")
    suspend fun deleteMusicPlaylistWithId(id: Long)

    @Query("DELETE FROM music_playlist_table")
    suspend fun deleteAllMusicPlaylist()

    @Query("SELECT * FROM music_playlist_table ORDER BY id ASC")
    fun readAllMusicData(): LiveData<List<MusicPlaylist>>
}

@Dao
interface SearchDao{
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSearch(search: Search)

    @Delete
    suspend fun deleteSearch(search: Search)

    @Query("DELETE FROM search_table")
    suspend fun deleteAllSearch()

    @Query("SELECT * FROM search_table ORDER BY id ASC")
    fun readAllSearchData(): LiveData<List<Search>>
}
