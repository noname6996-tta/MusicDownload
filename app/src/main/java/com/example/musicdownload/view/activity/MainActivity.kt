package com.example.musicdownload.view.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.example.musicdownload.R
import com.example.musicdownload.databinding.ActivityMainBinding
import com.example.musicdownload.view.fragment.PlayActivity
import com.google.android.material.navigation.NavigationBarView


class MainActivity : AppCompatActivity() {
    lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    companion object {
        lateinit var binding: ActivityMainBinding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.frameMain) as NavHostFragment
        navController = navHostFragment.findNavController()

        navController.addOnDestinationChangedListener(object : NavController.OnDestinationChangedListener{
            override fun onDestinationChanged(
                controller: NavController,
                destination: NavDestination,
                arguments: Bundle?
            ) {
                when(destination.id){
                    R.id.homeFragment, R.id.downloadManagerFragment,R.id.playListFragment,R.id.settingFragment->{
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
            setOnItemSelectedListener (mNavigationItemSelected )
        }
        setContentView(binding.root)
        binding.imgPlayBtnHome.setOnClickListener{
            if(PlayActivity.isPlaying) pauseMusic() else playMusic()
        }

    }
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }



    override fun onResume() {
        // dang co loi
        super.onResume()

        if (PlayActivity.musicService!= null){
            Toast.makeText(this,"onResume Home",Toast.LENGTH_SHORT).show()
            binding.layoutPlayHomeBottom.visibility = View.VISIBLE
            binding.tvNameSongPlayHome.text = PlayActivity.listMusicPlay[PlayActivity.songPosition].name
            binding.tvSingerSongPlayHome.text = PlayActivity.listMusicPlay[PlayActivity.songPosition].artistName
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
            binding.imgPlayBtnHome.setImageResource(R.drawable.ic_baseline_pause_24)
            if(PlayActivity.isPlaying){
                binding.imgPlayBtnHome.setImageResource(R.drawable.ic_baseline_pause_24)
            }
            else binding.imgPlayBtnHome.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        }
    }
    private fun playMusic(){
        PlayActivity.isPlaying = true
        PlayActivity.musicService!!.mediaPlayer!!.start()
        binding.imgPlayBtnHome.setImageResource(R.drawable.ic_baseline_pause_24)
        PlayActivity.musicService!!.showNotification(R.drawable.ic_baseline_pause_24)
    }
    private fun pauseMusic(){
        PlayActivity.isPlaying = false
        PlayActivity.musicService!!.mediaPlayer!!.pause()
        binding.imgPlayBtnHome.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        PlayActivity.musicService!!.showNotification(R.drawable.ic_baseline_play_arrow_24)
    }
}
