package com.example.musicdownload.view.fragment

import android.app.ProgressDialog
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.ImageLoader
import com.bumptech.glide.Glide
import com.example.musicdownload.R
import com.example.musicdownload.data.model.Music
import com.example.musicdownload.data.model.formatDuration
import com.example.musicdownload.data.model.setSongPosition
import com.example.musicdownload.data.repository.MusicRepository
import com.example.musicdownload.databinding.ActivityPlayBinding
import com.example.musicdownload.network.RetrofitService
import com.example.musicdownload.service.MusicService
import com.example.musicdownload.view.activity.MainActivity
import com.example.musicdownload.viewmodel.HomeFragmentViewModel
import com.example.musicdownload.viewmodel.MyViewModelFactory
import com.xuandq.radiofm.data.base.BaseFragment


class PlayActivity : BaseFragment(), ServiceConnection, MediaPlayer.OnCompletionListener {
    companion object {
        lateinit var listMusicPlay: ArrayList<Music>
        var songPosition: Int = 0
        lateinit var binding: ActivityPlayBinding
        var isPlaying: Boolean = false
        var musicService: MusicService? = null
        var repeat: Boolean = false
    }


    var sufferOption: Boolean = false
    lateinit var viewModel: HomeFragmentViewModel
    private val retrofitService = RetrofitService.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = ActivityPlayBinding.inflate(inflater, container, false)
        return binding.root

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewloadingPlaying.visibility = View.VISIBLE
        viewModel = ViewModelProvider(
            requireActivity(), MyViewModelFactory(MusicRepository(retrofitService))
        )[HomeFragmentViewModel::class.java]
        MainActivity.binding.bottomNavigation.visibility = View.GONE
        MainActivity.binding.layoutPlayHomeBottom.visibility = View.GONE
        initializeLayout()
        setLayout()
        val intentService = Intent(requireActivity(), MusicService::class.java)
        requireActivity().bindService(intentService, this, AppCompatActivity.BIND_AUTO_CREATE)
        requireActivity().startService(intentService)
        onClick()
    }

    private fun onClick() {
        binding.viewBntPlaying.setOnClickListener {
            if (isPlaying) pauseMusic()
            else playResume()
        }
        binding.imgNextSong.setOnClickListener {
            nextSong(true)
        }
        binding.imgPreviousSong.setOnClickListener {
            nextSong(false)
        }
        binding.imgPlayingSuffer.setOnClickListener {
            if (repeat){

            }
            else {
                if (!sufferOption) {
                    sufferOption = true
                    binding.imgPlayingSuffer.setImageResource(R.drawable.suffertrue)
                    var listMusicPlayShuffer: ArrayList<Music> = listMusicPlay
                    listMusicPlayShuffer.shuffle()
                } else {
                    sufferOption = false
                    binding.imgPlayingSuffer.setImageResource(R.drawable.randmusic)
                }
            }

        }
        binding.imgPlayingRepeat.setOnClickListener{
            if (!repeat){
                repeat = true
                binding.imgPlayingRepeat.setImageResource(R.drawable.repeatcolor)
            } else {
                repeat = false
                binding.imgPlayingRepeat.setImageResource(R.drawable.imgrepeat)
            }
        }
        binding.btnBackPlay.setOnClickListener {
            findNavController().popBackStack()
            MainActivity.binding.bottomNavigation.visibility = View.VISIBLE
            MainActivity.binding.layoutPlayHomeBottom.visibility = View.VISIBLE
        }
        binding.seekBarNowPlaying.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    musicService!!.mediaPlayer!!.seekTo(progress)
                    musicService!!.showNotification(if (isPlaying) R.drawable.ic_baseline_pause_24 else R.drawable.ic_baseline_play_arrow_24)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
        MainActivity.binding.seekBarHome.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    musicService!!.mediaPlayer!!.seekTo(progress)
                    musicService!!.showNotification(if (isPlaying) R.drawable.ic_baseline_pause_24 else R.drawable.ic_baseline_play_arrow_24)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
        binding.imgShowBottomPlaying.setOnClickListener {
            showBottomSheetMusic(listMusicPlay[songPosition])
        }
    }

    private fun nextSong(a: Boolean) {
        if (a) {
            setSongPosition(true)
            setLayout()
            playMedia()
        } else {
            setSongPosition(false)
            setLayout()
            playMedia()
        }
    }


    private fun setLayout() {
        Glide.with(this).load(listMusicPlay[songPosition].image).error(R.drawable.demo_img_download)
            .into(binding.imgPlaying)
        binding.tvSongNameNowPlaying.text = listMusicPlay[songPosition].name
        binding.tvSingerSongNowPlaying.text = listMusicPlay[songPosition].artistName
        binding.tvTimeEnd.text =
            formatDuration(listMusicPlay[songPosition].duration.toString().trim().toLong())
        if (repeat) binding.imgPlayingRepeat.setImageResource(R.drawable.repeatcolor)
    }

    private fun playMedia() {
        try {
            isPlaying = true
            if (musicService!!.mediaPlayer == null) musicService!!.mediaPlayer = MediaPlayer()
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(listMusicPlay[songPosition].audio)
            musicService!!.mediaPlayer!!.prepare()
            musicService!!.mediaPlayer!!.start()
            binding.imgPlayOrPasuePlaying.setImageResource(R.drawable.ic_baseline_pause_24)
            binding.tvTimeStart.text =
                formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.tvTimeEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            binding.seekBarNowPlaying.progress = musicService!!.mediaPlayer!!.currentPosition
            binding.seekBarNowPlaying.max = musicService!!.mediaPlayer!!.duration
            musicService!!.showNotification(R.drawable.ic_baseline_pause_24)
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
        } catch (e: Exception) {
            Log.e("playMedia", e.printStackTrace().toString())
        }
    }

    private fun initializeLayout() {
        listMusicPlay = ArrayList()
        val args: PlayActivityArgs by navArgs()
        songPosition = args.positionMusic
        listMusicPlay = HomeFragment.listMusicHome
        if (musicService != null && !isPlaying) playMedia()
    }

    private fun playResume() {
        isPlaying = true
        musicService!!.mediaPlayer!!.start()
        binding.imgPlayOrPasuePlaying.setImageResource(R.drawable.ic_baseline_pause_24)
        musicService!!.showNotification(R.drawable.ic_baseline_pause_24)

    }

    private fun pauseMusic() {
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()
        binding.imgPlayOrPasuePlaying.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        musicService!!.showNotification(R.drawable.ic_baseline_play_arrow_24)

    }

    override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
        if (musicService == null) {
            val binder = service as MusicService.MyBinder
            musicService = binder.currentService()
            musicService!!.seekBarSetup()
            playMedia()
        }
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        musicService = null
    }

    override fun onCompletion(mp: MediaPlayer?) {
        setSongPosition(increment = true)
        playMedia()
        setLayout()
    }

}