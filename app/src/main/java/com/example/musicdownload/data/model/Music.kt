package com.example.musicdownload.data.model

import com.example.musicdownload.view.activity.PlayActivity
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

data class Music(
    var id: String,
    var name: String,
    var duration: Int,
    var artistId: String,
    var artistName: String,
    var artistIdstr: String,
    var albumName: String,
    var albumId: String,
    var licenseCcurl: String,
    var position: Int,
    var releaseDate: String,
    var albumImage: String,
    var audio: String,
    var audioDownload: String,
    var prourl: String,
    var shorturl: String,
    var shareurl: String,
    var image: String,
    var audioDownloadAllowed: Boolean,
    var source: String,
)

fun formatDuration(duration: Long):String{
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) -
            minutes* TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
    return String.format("%02d:%02d", minutes, seconds)
}
fun setSongPosition(increment: Boolean){
    if(!PlayActivity.repeat){
        if(increment)
        {
            if(PlayActivity.listMusicPlay.size - 1 == PlayActivity.songPosition)
                PlayActivity.songPosition = 0
            else ++PlayActivity.songPosition
        }else{
            if(0 == PlayActivity.songPosition)
                PlayActivity.songPosition = PlayActivity.listMusicPlay.size-1
            else --PlayActivity.songPosition
        }
    }
}
fun exitApplication(){
    if(PlayActivity.musicService != null){
//        PlayActivity.musicService!!.audioManager.abandonAudioFocus(PlayActivity.musicService)
        PlayActivity.musicService!!.stopForeground(true)
        PlayActivity.musicService!!.mediaPlayer!!.release()
        PlayActivity.musicService = null}
    exitProcess(1)
}