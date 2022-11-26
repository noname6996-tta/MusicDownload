package com.example.musicdownload.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.musicdownload.data.dao.MusicPlayListDao
import com.example.musicdownload.data.dao.PlayListDao
import com.example.musicdownload.data.dao.SearchDao
import com.example.musicdownload.data.model.MusicPlaylist
import com.example.musicdownload.data.model.PlayList
import com.example.musicdownload.data.model.Search

@Database(entities = [PlayList::class,MusicPlaylist::class,Search::class], version = 1, exportSchema = false)
abstract class PlayListDataBase:RoomDatabase() {

    abstract fun playDao():PlayListDao
    abstract fun musicPlayDao():MusicPlayListDao
    abstract fun searchDao():SearchDao
    companion object {
        @Volatile
        private var INSTANCE : PlayListDataBase? = null

        // chekc instance is not null
        fun getDatabase(context: Context):PlayListDataBase{
            val tempInstance = INSTANCE
            if (tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                // if no instance create one
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlayListDataBase::class.java,
                    "playlist_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}