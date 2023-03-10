package com.example.musicdownload.view.activity

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUI.navigateUp
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicdownload.R
import com.example.musicdownload.adapter.BottomSheetPlaylistAdapter
import com.example.musicdownload.adapter.FileAdapter
import com.example.musicdownload.data.model.Music
import com.example.musicdownload.data.model.MusicPlaylist
import com.example.musicdownload.data.model.PlayList
import com.example.musicdownload.databinding.ActivityMainBinding
import com.example.musicdownload.viewmodel.MusicPlayListViewModel
import com.example.musicdownload.viewmodel.PlayListViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationBarView
import com.karumi.dexter.Dexter
import com.karumi.dexter.DexterBuilder
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {
    lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var runnable: Runnable
    lateinit var musicPlayListViewModel: MusicPlayListViewModel
    var isFavorite: Boolean = false
    var musicPlaylistid: String = ""
    lateinit var dexter : DexterBuilder
    lateinit var navHostFragment : NavHostFragment
    companion object {
        lateinit var binding: ActivityMainBinding
        var listDownloading = ArrayList<Music>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermission()
        musicPlayListViewModel =
            ViewModelProvider(this)[com.example.musicdownload.viewmodel.MusicPlayListViewModel::class.java]
        binding = ActivityMainBinding.inflate(layoutInflater)
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.frameMain) as NavHostFragment
        navController = navHostFragment.findNavController()

        navController.addOnDestinationChangedListener(object :
            NavController.OnDestinationChangedListener {
            override fun onDestinationChanged(
                controller: NavController,
                destination: NavDestination,
                arguments: Bundle?
            ) {
                when (destination.id) {
                    R.id.homeFragment, R.id.downloadManagerFragment, R.id.playListFragment, R.id.settingFragment -> {
                        binding.bottomNavigation.visibility = View.VISIBLE
                    }
                    else -> {
                        binding.bottomNavigation.visibility = View.GONE
                    }
                }
            }
        })

        val mNavigationItemSelected = object : NavigationBarView.OnItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.homeFragment -> {
                        NavigationUI.onNavDestinationSelected(
                            item,
                            navController
                        )
                        return true
                    }
                    R.id.downloadManagerFragment -> {
                        NavigationUI.onNavDestinationSelected(
                            item,
                            navController
                        )
                        return true
                    }
                    R.id.playListFragment -> {
                        NavigationUI.onNavDestinationSelected(
                            item,
                            navController
                        )
                        return true
                    }
                    R.id.settingFragment -> {
                        NavigationUI.onNavDestinationSelected(
                            item,
                            navController
                        )
                        return true
                    }
                    else -> {
                        return false
                    }
                }
            }
        }

        binding.bottomNavigation.setupWithNavController(navController)
        binding.bottomNavigation.apply {
            setOnItemSelectedListener(mNavigationItemSelected)
        }
        setContentView(binding.root)
        binding.imgPlayBtnHome.setOnClickListener {
            if (PlayActivity.isPlaying) pauseMusic() else playMusic()
        }
        binding.layoutPlayHomeBottom.setOnClickListener {
            val intent = Intent(this, PlayActivity::class.java)
            intent.putExtra("index", PlayActivity.songPosition)
            intent.putExtra("MainActivitySong", "NowPlaying")
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return (navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp())
    }

    override fun onResume() {
        super.onResume()
        if (PlayActivity.musicService != null) {
            val music = PlayActivity.listMusicPlay[PlayActivity.songPosition]
            binding.layoutPlayHomeBottom.visibility = View.VISIBLE
            binding.tvNameSongPlayHome.text =
                PlayActivity.listMusicPlay[PlayActivity.songPosition].name
            binding.tvSingerSongPlayHome.text =
                PlayActivity.listMusicPlay[PlayActivity.songPosition].artistName
            //seekBarSetup()
            binding.seekBarHome.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        PlayActivity.musicService!!.mediaPlayer!!.seekTo(progress)
                        PlayActivity.musicService!!.showNotification(if (PlayActivity.isPlaying) R.drawable.ic_baseline_pause_24 else R.drawable.ic_baseline_play_arrow_24)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
                override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
            })
            checkFavorite(PlayActivity.listMusicPlay[PlayActivity.songPosition])
            binding.seekBarHome.progress = PlayActivity.musicService?.mediaPlayer!!.currentPosition
            binding.seekBarHome.max = PlayActivity.musicService?.mediaPlayer!!.duration
            seekBarSetup()
            binding.imgPlayBtnHome.setImageResource(R.drawable.ic_baseline_pause_24)
            if (PlayActivity.isPlaying) {
                binding.imgPlayBtnHome.setImageResource(R.drawable.ic_baseline_pause_24)
            } else {
                binding.imgPlayBtnHome.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            }


            binding.imgAddPlayListHome.setOnClickListener {
                showBottomSheetAddPlaylist(music)
            }

            binding.imgFavoriteHome.setOnClickListener {
                if (!isFavorite) {
                    val musicPlaylist = MusicPlaylist(
                        0,
                        false,
                        true,
                        0,
                        music.name,
                        music.artistName,
                        music.duration,
                        music.image,
                        music.audio
                    )
                    musicPlayListViewModel.addMusicPlayList(musicPlaylist)
                    binding.imgFavoriteHome.setImageResource(R.drawable.ic_baseline_favorite_true_24)
                    Toast.makeText(this,"Add" + music.name+ " to favorite success",Toast.LENGTH_SHORT).show()
                    isFavorite = true
                } else {
                    isFavorite = false
                    musicPlayListViewModel.deleteMusicPlaylistWithId(
                        musicPlaylistid.toString().trim()
                    )
                    binding.imgFavoriteHome.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                    Toast.makeText(this,"Remove" + music.name+ " to favorite success",Toast.LENGTH_SHORT).show()
                }
            }
        } else if (PlayActivity.musicService == null) {
            binding.layoutPlayHomeBottom.visibility = View.GONE
        }
//        if (!checkForInternet(this)){
//            navController.navigate(R.id.downloadManagerFragment,null, navOptions {
//                popUpTo(R.id.homeFragment) {
//                    inclusive = true
//                }
//            })
//        }
    }

    private fun playMusic() {
        PlayActivity.isPlaying = true
        PlayActivity.musicService!!.mediaPlayer!!.start()
        binding.imgPlayBtnHome.setImageResource(R.drawable.ic_baseline_pause_24)
        PlayActivity.musicService!!.showNotification(R.drawable.ic_baseline_pause_24)
    }

    private fun pauseMusic() {
        PlayActivity.isPlaying = false
        PlayActivity.musicService!!.mediaPlayer!!.pause()
        binding.imgPlayBtnHome.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        PlayActivity.musicService!!.showNotification(R.drawable.ic_baseline_play_arrow_24)
    }

    fun seekBarSetup() {
        runnable = Runnable {
            binding.seekBarHome.progress = PlayActivity.musicService?.mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        PlayActivity.musicService?.stopSelf()
        if(PlayActivity.musicService != null){
//        PlayActivity.musicService!!.audioManager.abandonAudioFocus(PlayActivity.musicService)
            PlayActivity.musicService!!.stopForeground(true)
            PlayActivity.musicService!!.mediaPlayer!!.release()
            PlayActivity.musicService = null}
        exitProcess(1)
    }

    private fun checkFavorite(music: Music) {
        musicPlayListViewModel.readAllMusicData.observe(this,
            Observer { musicplaylist ->
                for (item: Int in 0..musicplaylist.size - 1) {
                    if (musicplaylist[item].name.equals(music.name.toString()) && musicplaylist[item].favorite) {
                        binding.imgFavoriteHome!!.setImageResource(R.drawable.ic_baseline_favorite_true_24)
                        musicPlaylistid = musicplaylist[item].name
                        isFavorite = true
                    } else {
                        binding.imgFavoriteHome!!.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                        isFavorite = false
                    }

                }
            })
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
            showDialogAdd()
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

    open fun showDialogAdd() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.layout_add_playlist)
        var playListName = dialog.findViewById<EditText>(R.id.edtNamePlayList)
        dialog.findViewById<TextView>(R.id.tvAddPlayList).setOnClickListener {
            insertDataToDatabase(playListName.text.toString().trim(),dialog)

        }
        dialog.findViewById<TextView>(R.id.tvCancelAddPlayList).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun insertDataToDatabase(name: String,dialog: Dialog) {
        if (inputCheck(name)) {
            val playListViewModel = ViewModelProvider(this)[PlayListViewModel::class.java]
            // Create play list
            val playList = PlayList(0,name, 0, "")
            // add Data to database
            playListViewModel.addPlayList(playList)
            Toast.makeText(this, "Add PlayList: $name Success", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
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
            playList.id,
            music.name,
            music.artistName,
            music.duration,
            music.image,
            music.audio
        )
        var count: Int = 0
        val list : List<MusicPlaylist> = musicPlayListViewModel.readAllMusicData.value!!
        for (item: Int in 0..list.size-1) {
            if (list[item].idPlayList == playList.id && list[item].name == music.name) {
                count++
            }
        }
        if (count > 0) {
            Toast.makeText(this,music.name +"already in this Playlist",Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this,"Add "+music.name +"to this Playlist success",Toast.LENGTH_SHORT).show()
            musicPlayListViewModel.addMusicPlayList(musicPlaylist)
        }
    }

    private fun checkPermission(){
        dexter = Dexter.withContext(this)
            .withPermissions(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()){

                    }
                    else {
                        Toast.makeText(applicationContext,"Please give permission first",Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest?>?, token: PermissionToken?) {
                    token?.continuePermissionRequest()
                }
            }).withErrorListener{
                Toast.makeText(this, it.name, Toast.LENGTH_SHORT).show()
            }
        dexter.check()
    }

    override fun onBackPressed() {
        val backStackEntryCount = navHostFragment.childFragmentManager.backStackEntryCount
        if (backStackEntryCount > 0) {
            if (!navController.popBackStack()) {
                finish()
            }
            return
        } else {
            val builder = AlertDialog.Builder(this)
            builder.setCancelable(false)
            builder.setMessage("Do you want to Exit?")
            builder.setPositiveButton("Yes") { dialog, which -> //if user pressed "yes", then he is allowed to exit from application
                this.finishAffinity();
            }
            builder.setNegativeButton("No") { dialog, which -> //if user select "No", just cancel this dialog and continue with app
                dialog.cancel()
            }
            val alert = builder.create()
            alert.show()
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

}
