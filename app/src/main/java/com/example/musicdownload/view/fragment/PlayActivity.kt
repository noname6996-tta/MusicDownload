package com.example.musicdownload.view.fragment

import android.app.Dialog
import android.content.*
import android.media.MediaPlayer
import android.media.MediaScannerConnection
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicdownload.R
import com.example.musicdownload.adapter.BottomSheetPlaylistAdapter
import com.example.musicdownload.adapter.FileAdapter
import com.example.musicdownload.data.download.Data
import com.example.musicdownload.data.model.*
import com.example.musicdownload.data.repository.MusicRepository
import com.example.musicdownload.databinding.ActivityPlayBinding
import com.example.musicdownload.network.RetrofitService
import com.example.musicdownload.service.MusicService
import com.example.musicdownload.view.activity.MainActivity
import com.example.musicdownload.viewmodel.HomeFragmentViewModel
import com.example.musicdownload.viewmodel.MusicPlayListViewModel
import com.example.musicdownload.viewmodel.MyViewModelFactory
import com.example.musicdownload.viewmodel.PlayListViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration
import com.tonyodev.fetch2.Request
import com.tonyodev.fetch2core.Downloader
import com.tonyodev.fetch2okhttp.OkHttpDownloader
import java.io.File


class PlayActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {
    companion object {
        lateinit var listMusicPlay: ArrayList<Music>
        var songPosition: Int = 0
        lateinit var binding: ActivityPlayBinding
        var isPlaying: Boolean = false
        var musicService: MusicService? = null
        var repeat: Boolean = false
    }
    var arrayMusicLocalBase = ArrayList<MusicLocal>()
    var musicPlaylistid: String = "123456789"
    private var isFavorite: Boolean = false
    private var listMusicOffline = ArrayList<Music>()
    var sufferOption: Boolean = false
    lateinit var viewModel: HomeFragmentViewModel
    private val retrofitService = RetrofitService.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(
            this,
            MyViewModelFactory(MusicRepository(retrofitService))
        )[HomeFragmentViewModel::class.java]
        initializeLayout()
        setLayout()
        val intentService = Intent(this, MusicService::class.java)
        this.bindService(intentService, this, AppCompatActivity.BIND_AUTO_CREATE)
        this.startService(intentService)
        onClick()
    }

    private fun onClick() {
        binding.viewBntPlaying.setOnClickListener {
            if (isPlaying) pauseMusic()
            else playResume()
        }
        binding.imgNextSong.setOnClickListener {
            binding.imgNextSong.isEnabled = false
            nextSong(true)

        }
        binding.imgPreviousSong.setOnClickListener {
            binding.imgPreviousSong.isEnabled = true
            nextSong(false)
        }
        binding.imgPlayingSuffer.setOnClickListener {
            if (repeat) {

            } else {
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
        binding.imgPlayingRepeat.setOnClickListener {
            if (!repeat) {
                repeat = true
                binding.imgPlayingRepeat.setImageResource(R.drawable.repeatcolor)
            } else {
                repeat = false
                binding.imgPlayingRepeat.setImageResource(R.drawable.imgrepeat)
            }
        }
        binding.btnBackPlay.setOnClickListener {
            onBackPressed()
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
        binding.imgPlayFavorite.setOnClickListener {
            val musicPlayListViewModel = ViewModelProvider(this)[MusicPlayListViewModel::class.java]
            var music = listMusicPlay[songPosition]
            if (!isFavorite) {
                val musicPlaylist = MusicPlaylist(
                    0,
                    false,
                    true,
                    "favorite",
                    music.name,
                    music.artistName,
                    music.duration,
                    music.image,
                    music.audio
                )
                musicPlayListViewModel.addMusicPlayList(musicPlaylist)
                binding.imgPlayFavorite.setImageResource(R.drawable.ic_baseline_favorite_true_24)
            } else {
                musicPlayListViewModel.deleteMusicPlaylistWithId(musicPlaylistid.toString().trim())
                binding.imgPlayFavorite.setImageResource(R.drawable.ic_baseline_favorite_24)
            }
        }
    }

    private fun nextSong(a: Boolean) {
        if (a) {
            setSongPosition(true)
            setLayout()
            playMedia()
            binding.imgNextSong.isEnabled = true
            binding.imgPreviousSong.isEnabled = true
        } else {
            setSongPosition(false)
            setLayout()
            playMedia()
            binding.imgNextSong.isEnabled = true
            binding.imgPreviousSong.isEnabled = true
        }
    }


    private fun setLayout() {
        checkFavorite(listMusicPlay[songPosition])
        Glide.with(applicationContext).load(listMusicPlay[songPosition].image)
            .error(R.drawable.demo_img_download)
            .into(binding.imgPlaying)
        binding.tvSongNameNowPlaying.text = listMusicPlay[songPosition].name
        binding.tvSingerSongNowPlaying.text = listMusicPlay[songPosition].artistName
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
        songPosition = intent.getIntExtra("index", 0)
        when (intent.getStringExtra("MainActivitySong")) {
            "NowPlaying" -> {
                binding.tvTimeStart.text =
                    formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.tvTimeEnd.text =
                    formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seekBarNowPlaying.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.seekBarNowPlaying.max = musicService!!.mediaPlayer!!.duration
                if (isPlaying) binding.imgPlayOrPasuePlaying.setImageResource(R.drawable.ic_baseline_pause_24)
                else binding.imgPlayOrPasuePlaying.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                checkFavorite(listMusicPlay[songPosition])
                setLayout()
            }
            "HomeFragment" -> {
                if (checkForInternet(this)) {
                    listMusicPlay = HomeFragment.listMusicHome
                } else {
                    getDataStoreEx()
                    for (i in 0..HomeFragment.listMusicHome.size - 1) {
                        for (j in 0..listMusicOffline.size - 1) {
                            if (listMusicOffline[j].name.equals(HomeFragment.listMusicHome[i].name)) {
                                listMusicPlay.add(HomeFragment.listMusicHome[i])
                            } else {

                            }
                        }
                    }
                }
                checkFavorite(listMusicPlay[songPosition])
                if (musicService != null && !isPlaying) playMedia()
            }
            "DownloadedFragment" -> {
                getDataStoreEx()
                listMusicPlay = listMusicOffline
                checkFavorite(listMusicPlay[songPosition])
                if (musicService != null && !isPlaying) playMedia()
            }
            "PlaylistFragment" -> {
                if (checkForInternet(this)){
                    listMusicPlay = HomeFragment.listMusicHome
                    checkFavorite(listMusicPlay[songPosition])
                    if (musicService != null && !isPlaying) playMedia()
                } else {
                    getDataStoreEx()
                    listMusicPlay = HomeFragment.listMusicHome
                    for (i in 0 until listMusicOffline.size){
                        while (HomeFragment.listMusicHome[songPosition].name == listMusicOffline[i].name){
                                checkFavorite(listMusicPlay[songPosition])
                                if (musicService != null && !isPlaying) playMedia()
                                break
                            }
                        nextSong(true)
                    }
                }
            }
            "GenresMusicListFragment" -> {
                listMusicPlay = HomeFragment.listMusicHome
                checkFavorite(listMusicPlay[songPosition])
                if (musicService != null && !isPlaying) playMedia()
            }
        }
    }

    private fun getDataStoreEx() {
        try {
            arrayMusicLocalBase = ArrayList()
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ARTIST,
            )
            val dirfile: String = "DownloadList"
            val selection =
                MediaStore.Audio.Media.IS_MUSIC + "!= 0 AND " + "${MediaStore.Audio.Media.DATA} LIKE '%$dirfile%'"
            val cursor = this.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null
            )

            if (cursor != null) {
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                while (cursor.moveToNext()) {
                    var id: Long = cursor.getLong(idColumn)
                    val title: String = cursor.getString(titleColumn)
                    val data: String = cursor.getString(dataColumn)
                    val duration: String = cursor.getString(durationColumn)
                    val album: Long = cursor.getLong(albumColumn)
                    val name: String = cursor.getString(nameColumn)

                    val albumArtworkUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"), album
                    ).toString()

                    val song = Music(
                        "",
                        title,
                        duration.trim().toInt(),
                        "",
                        name,
                        "",
                        "",
                        "",
                        "",
                        0,
                        "",
                        "",
                        data,
                        "",
                        "",
                        "",
                        "",
                        albumArtworkUri,
                        true,
                        ""
                    )
                    listMusicOffline.add(song)
                }
            }
        } catch (e  : Exception){
            Log.e("getDataStoreEx",e.toString())
        }
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
        MainActivity.binding.tvNameSongPlayHome.text =
            listMusicPlay[songPosition].name
        MainActivity.binding.tvSingerSongPlayHome.text =
            listMusicPlay[songPosition].artistName
    }

    private fun checkFavorite(music: Music) {
        val musicPlayListViewModel = ViewModelProvider(this)[MusicPlayListViewModel::class.java]
        musicPlayListViewModel.readAllMusicData.observe(this,
            Observer { musicplaylist ->
                for (item: Int in 0..musicplaylist.size - 1) {
                    if (musicplaylist[item].name.equals(music.name.toString()) && musicplaylist[item].favorite == true) {
                        binding.imgPlayFavorite!!.setImageResource(R.drawable.ic_baseline_favorite_true_24)
                        musicPlaylistid = musicplaylist[item].name
                        isFavorite = true
                    } else {
                        binding.imgPlayFavorite!!.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                        isFavorite = false
                    }

                }
            })
    }

    override fun onResume() {
        super.onResume()
        if (isPlaying) {
            binding.imgPlayOrPasuePlaying.setImageResource(R.drawable.ic_baseline_pause_24)
        } else {
            binding.imgPlayOrPasuePlaying.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        }

    }

    private fun checkForInternet(context: Context): Boolean {
        // register activity with the connectivity manager service
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // if the android version is equal to M
        // or greater we need to use the
        // NetworkCapabilities to check what type of
        // network has the internet connection
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // Returns a Network object corresponding to
            // the currently active default data network.
            val network = connectivityManager.activeNetwork ?: return false

            // Representation of the capabilities of an active network.
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                // Indicates this network uses a Wi-Fi transport,
                // or WiFi has network connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

                // Indicates this network uses a Cellular transport. or
                // Cellular has network connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

                // else return false
                else -> false
            }
        } else {
            // if the android version is below M
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }

    private fun showBottomSheetMusic(music: Music) {
        val bottomSheetDialogSong = BottomSheetDialog(this);
        bottomSheetDialogSong.setContentView(R.layout.bottom_sheet_info_music);
        bottomSheetDialogSong.show()
        val img: ImageView = bottomSheetDialogSong.findViewById(R.id.imgBottomSheetSong)!!
        val song: TextView = bottomSheetDialogSong.findViewById(R.id.tvSongBtMusic)!!
        val singer: TextView = bottomSheetDialogSong.findViewById(R.id.tvSingerBtMusic)!!
        Glide.with(this).load(music.image).error(R.drawable.demo_img_download)
            .into(img)
        song.text = music.name
        singer.text = music.artistName
        val viewShare: View? = bottomSheetDialogSong.findViewById(R.id.layoutShare)
        if (viewShare == null) {

        } else {
            viewShare.setOnClickListener {
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.type = "plain/text"
                shareIntent.putExtra(Intent.EXTRA_TEXT, music.audio)
                startActivity(Intent.createChooser(shareIntent, "Sharing Music File!!"))
            }
        }
        val viewAddToPlaylist = bottomSheetDialogSong.findViewById<View>(R.id.viewAddToPlaylist)
        viewAddToPlaylist!!.setOnClickListener {
            bottomSheetDialogSong.dismiss()
            showBottomSheetAddPlaylist(music)
        }

        val favorite = bottomSheetDialogSong.findViewById<ImageView>(R.id.imgFavoriteMusic)
        val musicPlayListViewModel = ViewModelProvider(this)[MusicPlayListViewModel::class.java]
        var isFavorite: Boolean = false
        var musicPlaylistid: String = "123456789"
        musicPlayListViewModel.readAllMusicData.observe(this,
            Observer { musicplaylist ->
                for (item: Int in musicplaylist.indices) {
                    if (musicplaylist[item].name.equals(music.name.toString()) && musicplaylist[item].favorite) {
                        favorite!!.setImageResource(R.drawable.ic_baseline_favorite_true_24)
                        musicPlaylistid = musicplaylist[item].name
                        isFavorite = true
                    } else {
                        favorite!!.setImageResource(R.drawable.ic_baseline_favorite_24)
                        isFavorite = false
                    }

                }
            })


        favorite?.setOnClickListener {
            if (!isFavorite) {
                val musicPlaylist = MusicPlaylist(
                    0,
                    false,
                    true,
                    "favorite",
                    music.name,
                    music.artistName,
                    music.duration,
                    music.image,
                    music.audio
                )
                musicPlayListViewModel.addMusicPlayList(musicPlaylist)
                favorite.setImageResource(R.drawable.ic_baseline_favorite_true_24)
            } else {
                musicPlayListViewModel.deleteMusicPlaylistWithId(musicPlaylistid.toString().trim())
                favorite.setImageResource(R.drawable.ic_baseline_favorite_border_24)
            }
        }
        getDataStoreEx()
        var viewDownloadSong = bottomSheetDialogSong.findViewById<View>(R.id.viewDownloadSong)
        var tvDownload = bottomSheetDialogSong.findViewById<TextView>(R.id.tvDownload)
        var imageviewDownloadSong = bottomSheetDialogSong.findViewById<ImageView>(R.id.imageviewDownloadSong)
        var viewRemoveDownloadSong = bottomSheetDialogSong.findViewById<View>(R.id.viewRemoveDownloadSong)
        var removeSong = bottomSheetDialogSong.findViewById<TextView>(R.id.removeSong)
        var imageviewRemoveDownloadSong = bottomSheetDialogSong.findViewById<ImageView>(R.id.imageviewRemoveDownloadSong)
        var count : Int =0
        for (i in 0..listMusicOffline.size-1){
            if (music.name == listMusicOffline[i].name){
                count ++
            }
        }
        if (count>0){
            viewDownloadSong!!.visibility = View.GONE
            tvDownload!!.visibility = View.GONE
            imageviewDownloadSong!!.visibility = View.GONE
            viewRemoveDownloadSong!!.visibility = View.VISIBLE
            removeSong!!.visibility = View.VISIBLE
            imageviewRemoveDownloadSong!!.visibility = View.VISIBLE
        }
        else {
            viewDownloadSong!!.visibility = View.VISIBLE
            tvDownload!!.visibility = View.VISIBLE
            imageviewDownloadSong!!.visibility = View.VISIBLE
            viewRemoveDownloadSong!!.visibility = View.GONE
            removeSong!!.visibility = View.GONE
            imageviewRemoveDownloadSong!!.visibility = View.GONE
        }
        viewDownloadSong?.setOnClickListener {
            Data.listDownload.add(music)
            FileAdapter.list.add(music)
            var fetch: Fetch
            val fetchConfiguration: FetchConfiguration =
                FetchConfiguration.Builder(this)
                    .setDownloadConcurrentLimit(999999)
                    .enableLogging(true)
                    .setHttpDownloader(OkHttpDownloader(Downloader.FileDownloaderType.PARALLEL))
                    .setNamespace(DownloadingFragment.FETCH_NAMESPACE)
                    .build()
            fetch = Fetch.Impl.getInstance(fetchConfiguration)

            val requests: List<Request> = Data.getFetchRequestWithGroupId(
                DownloadingFragment.GROUP_ID,
                this
            )
            fetch.enqueue(requests) { updatedRequests: List<Pair<Request?, Error?>?>? -> }
            Toast.makeText(this, "Downloading", Toast.LENGTH_LONG).show()
            bottomSheetDialogSong.dismiss()
        }

        viewRemoveDownloadSong?.setOnClickListener {
            for (i in 0..arrayMusicLocalBase.size - 1) {
                if (music.name == arrayMusicLocalBase[i].title) {
                    val fdelete: File = File(arrayMusicLocalBase[i].data)
                    fdelete.delete()
                    MediaScannerConnection.scanFile(
                        this, arrayOf(arrayMusicLocalBase[i].data), null, null
                    )
                    getDataStoreEx()
                    var count2 : Int =0
                    for (i in 0..listMusicOffline.size-1){
                        if (music.name == listMusicOffline[i].name){
                            count2 ++
                        }
                    }
                    if (count2>0){
                        viewDownloadSong!!.visibility = View.GONE
                        tvDownload!!.visibility = View.GONE
                        imageviewDownloadSong!!.visibility = View.GONE
                        viewRemoveDownloadSong!!.visibility = View.VISIBLE
                        removeSong!!.visibility = View.VISIBLE
                        imageviewRemoveDownloadSong!!.visibility = View.VISIBLE
                    }
                    else {
                        viewDownloadSong!!.visibility = View.VISIBLE
                        tvDownload!!.visibility = View.VISIBLE
                        imageviewDownloadSong!!.visibility = View.VISIBLE
                        viewRemoveDownloadSong!!.visibility = View.GONE
                        removeSong!!.visibility = View.GONE
                        imageviewRemoveDownloadSong!!.visibility = View.GONE
                    }
                    bottomSheetDialogSong.dismiss()
                }
            }
        }
    }

    private fun showBottomSheetAddPlaylist(music: Music) {
        val bottomSheetDialogSong = BottomSheetDialog(this);
        bottomSheetDialogSong.setContentView(R.layout.bottom_sheet_add_music);
        bottomSheetDialogSong.show()

        val musicPlaylist = bottomSheetDialogSong.findViewById<RecyclerView>(R.id.recBottomAddMusic)

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        musicPlaylist?.layoutManager = linearLayoutManager

        val bottomSheetPlaylistAdapter = BottomSheetPlaylistAdapter()
        musicPlaylist?.adapter = bottomSheetPlaylistAdapter

        val btnCreate = bottomSheetDialogSong.findViewById<ImageView>(R.id.imgCreateNewPlaylist)

        btnCreate?.setOnClickListener {
            showDialogAdd(this)
        }

        bottomSheetPlaylistAdapter.setAddMusicToPlaylist {
            insertMusicToPlaylist(music, it)
            bottomSheetDialogSong.dismiss()
        }

        val playListViewModel = ViewModelProvider(this)[PlayListViewModel::class.java]
        playListViewModel.readAllData.observe(this, Observer { playlist ->
            bottomSheetPlaylistAdapter.setPlayList(playlist, this)
        })

        val btnBack = bottomSheetDialogSong.findViewById<ImageView>(R.id.imgBackAddMusicToPlayList)
        btnBack?.setOnClickListener {
            bottomSheetDialogSong.dismiss()
        }
    }

    private fun showDialogAdd(context: Context) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.layout_add_playlist)
        var playListName = dialog.findViewById<EditText>(R.id.edtNamePlayList)
        dialog.findViewById<TextView>(R.id.tvAddPlayList).setOnClickListener {
            insertDataToDatabase(playListName.text.toString().trim())
            dialog.dismiss()
        }
        dialog.findViewById<TextView>(R.id.tvCancelAddPlayList).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun insertDataToDatabase(name: String) {
        if (inputCheck(name)) {
            val playListViewModel = ViewModelProvider(this)[PlayListViewModel::class.java]
            // Create play list
            val playList = PlayList( name, 0, "")
            // add Data to database
            playListViewModel.addPlayList(playList)
            Toast.makeText(this, "Add PlayList: $name Success", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Add PlayList: $name fail", Toast.LENGTH_SHORT).show()
        }
    }

    fun inputCheck(a: String): Boolean {
        return !(TextUtils.isEmpty(a))
    }

    private fun insertMusicToPlaylist(music: Music, playList: PlayList) {
        val musicPlayListViewModel = ViewModelProvider(this)[MusicPlayListViewModel::class.java]
        //
        val musicPlaylist = MusicPlaylist(
            0,
            false,
            false,
            playList.name,
            music.name,
            music.artistName,
            music.duration,
            music.image,
            music.audio
        )
        musicPlayListViewModel.readAllMusicData.observe(this,
            Observer { musicplaylist ->
                for (item: Int in musicplaylist.indices) {
                    if (musicplaylist[item].namePlayList == playList.name) {
                        Toast.makeText(this, "Song ${music.name} already in this Playlist", Toast.LENGTH_SHORT).show()
                    } else {
                        musicPlayListViewModel.addMusicPlayList(musicPlaylist)
                        Toast.makeText(this, "Add PlayList: ${music.name} Success", Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }
}