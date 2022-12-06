package com.example.musicdownload.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "playlist_table")
data class PlayList(
    @PrimaryKey(autoGenerate = true)
    val id : Int,
    val name: String,
    val number : Int,
    val image : String
) :Parcelable

@Parcelize
@Entity(tableName = "music_playlist_table")
data class MusicPlaylist(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var getDownloaded: Boolean,
    var favorite: Boolean,
    var idPlayList: Int,
    var name: String,
    var artists: String,
    var duration: Int,
    var image: String,
    var audio: String
): Parcelable

@Parcelize
@Entity(tableName = "search_table")
data class Search(
    @PrimaryKey
    val name: String,
) :Parcelable
