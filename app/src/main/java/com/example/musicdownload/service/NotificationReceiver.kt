package com.example.musicdownload.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicdownload.R
import com.example.musicdownload.data.model.exitApplication
import com.example.musicdownload.data.model.setSongPosition
import com.example.musicdownload.view.activity.MainActivity
import com.example.musicdownload.view.fragment.PlayActivity
import com.example.musicdownload.view.fragment.HomeFragment

class NotificationReceiver:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            ApplicationClass.PREVIOUS -> prevNextSong(increment = false, context = context!!)
            ApplicationClass.PLAY -> if(PlayActivity.isPlaying) pauseMusic() else playMusic()
            ApplicationClass.NEXT -> prevNextSong(increment = true, context = context!!)
            ApplicationClass.EXIT ->{
                stopService()
            }
        }
    }
    private fun playMusic(){
        PlayActivity.isPlaying = true
        PlayActivity.musicService!!.mediaPlayer!!.start()
        PlayActivity.musicService!!.showNotification(R.drawable.ic_baseline_pause_24)
        PlayActivity.binding.imgPlayOrPasuePlaying.setImageResource(R.drawable.ic_baseline_pause_24)
        MainActivity.binding.imgPlayBtnHome.setImageResource(R.drawable.ic_baseline_pause_24)
    }

    private fun pauseMusic(){
        PlayActivity.isPlaying = false
        PlayActivity.musicService!!.mediaPlayer!!.pause()
        PlayActivity.musicService!!.showNotification(R.drawable.ic_baseline_play_arrow_24)
        PlayActivity.binding.imgPlayOrPasuePlaying.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        MainActivity.binding.imgPlayBtnHome.setImageResource(R.drawable.ic_baseline_play_arrow_24)
    }

    private fun prevNextSong(increment: Boolean, context: Context){
        setSongPosition(increment = increment)
        PlayActivity.musicService!!.createMediaPlayer()
        PlayActivity.musicService!!.showNotification(R.drawable.ic_baseline_pause_24)
        Glide.with(context)
            .load(PlayActivity.listMusicPlay[PlayActivity.songPosition].image)
            .apply(RequestOptions().placeholder(R.drawable.demo_img_download).centerCrop())
            .into(PlayActivity.binding.imgPlaying)
        PlayActivity.binding.tvSongNameNowPlaying.text = PlayActivity.listMusicPlay[PlayActivity.songPosition].name
        PlayActivity.binding.tvSingerSongNowPlaying.text = PlayActivity.listMusicPlay[PlayActivity.songPosition].artistName
        MainActivity.binding.tvNameSongPlayHome.text = PlayActivity.listMusicPlay[PlayActivity.songPosition].name
        MainActivity.binding.tvSingerSongPlayHome.text = PlayActivity.listMusicPlay[PlayActivity.songPosition].artistName
        playMusic()
    }
    private fun stopService(){
        val service= MusicService()
        service.stopSelf()
        exitApplication()
    }
}