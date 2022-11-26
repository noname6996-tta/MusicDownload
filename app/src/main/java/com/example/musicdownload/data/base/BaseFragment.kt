package com.xuandq.radiofm.data.base

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicdownload.R
import com.example.musicdownload.adapter.BottomSheetPlaylistAdapter
import com.example.musicdownload.adapter.FileAdapter
import com.example.musicdownload.data.download.Data
import com.example.musicdownload.data.model.Music
import com.example.musicdownload.data.model.MusicPlaylist
import com.example.musicdownload.data.model.PlayList
import com.example.musicdownload.view.fragment.DownloadingFragment
import com.example.musicdownload.viewmodel.MusicPlayListViewModel
import com.example.musicdownload.viewmodel.PlayListViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration
import com.tonyodev.fetch2.Request
import com.tonyodev.fetch2core.Downloader
import com.tonyodev.fetch2okhttp.OkHttpDownloader

open class BaseFragment : Fragment() {
    open fun checkForInternet(context: Context): Boolean {
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

    open fun showBottomSheetMusic(music: Music) {
        val bottomSheetDialogSong = BottomSheetDialog(this.requireContext());
        bottomSheetDialogSong.setContentView(R.layout.bottom_sheet_info_music);
        bottomSheetDialogSong.show()
        val img: ImageView = bottomSheetDialogSong.findViewById(R.id.imgBottomSheetSong)!!
        val song: TextView = bottomSheetDialogSong.findViewById(R.id.tvSongBtMusic)!!
        val singer: TextView = bottomSheetDialogSong.findViewById(R.id.tvSingerBtMusic)!!
        Glide.with(requireContext()).load(music.image).error(R.drawable.demo_img_download)
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
        var musicPlaylistid : Int = 0
        musicPlayListViewModel.readAllMusicData.observe(viewLifecycleOwner,
            Observer { musicplaylist ->
                for (item: Int in musicplaylist.indices) {
                    if (musicplaylist[item].name.equals(music.name.toString())&& musicplaylist[item].favorite){
                        favorite!!.setImageResource(R.drawable.ic_baseline_favorite_true_24)
                        musicPlaylistid = musicplaylist[item].id
                        isFavorite = true
                    }
                    else {
                        favorite!!.setImageResource(R.drawable.ic_baseline_favorite_24)
                        isFavorite = false
                    }

                }
            })


        favorite?.setOnClickListener {
            if (!isFavorite){
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
                favorite.setImageResource(R.drawable.ic_baseline_favorite_true_24)
            }
            else {
                musicPlayListViewModel.deleteMusicPlaylistWithId(musicPlaylistid.toLong())
                favorite.setImageResource(R.drawable.ic_baseline_favorite_24)
            }
        }
        var viewDownloadSong = bottomSheetDialogSong.findViewById<View>(R.id.viewDownloadSong)
        viewDownloadSong?.setOnClickListener{
            Data.listDownload.add(music)
            FileAdapter.list.add(music)
            var fetch: Fetch
            val fetchConfiguration: FetchConfiguration = FetchConfiguration.Builder(requireContext())
                .setDownloadConcurrentLimit(999999)
                .enableLogging(true)
                .setHttpDownloader(OkHttpDownloader(Downloader.FileDownloaderType.PARALLEL))
                .setNamespace(DownloadingFragment.FETCH_NAMESPACE)
                .build()
            fetch = Fetch.Impl.getInstance(fetchConfiguration)

            val requests: List<Request> = Data.getFetchRequestWithGroupId(
                DownloadingFragment.GROUP_ID,
                requireContext()
            )
            fetch.enqueue(requests) { updatedRequests: List<Pair<Request?, Error?>?>? -> }
            Toast.makeText(context, "Downloading", Toast.LENGTH_LONG).show()
        }
    }

    open fun showBottomSheetAddPlaylist(music: Music) {
        val bottomSheetDialogSong = BottomSheetDialog(this.requireContext());
        bottomSheetDialogSong.setContentView(R.layout.bottom_sheet_add_music);
        bottomSheetDialogSong.show()

        val musicPlaylist = bottomSheetDialogSong.findViewById<RecyclerView>(R.id.recBottomAddMusic)

        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        musicPlaylist?.layoutManager = linearLayoutManager

        val bottomSheetPlaylistAdapter = BottomSheetPlaylistAdapter()
        musicPlaylist?.adapter = bottomSheetPlaylistAdapter

        val btnCreate = bottomSheetDialogSong.findViewById<ImageView>(R.id.imgCreateNewPlaylist)

        btnCreate?.setOnClickListener {
            showDialogAdd(this.requireContext())
        }

        bottomSheetPlaylistAdapter.setAddMusicToPlaylist {
            insertMusicToPlaylist(music, it)
            bottomSheetDialogSong.dismiss()
        }

        val playListViewModel = ViewModelProvider(this)[PlayListViewModel::class.java]
        playListViewModel.readAllData.observe(viewLifecycleOwner, Observer { playlist ->
            bottomSheetPlaylistAdapter.setPlayList(playlist, requireContext())
        })
    }

    open fun showDialogAdd(context: Context) {
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
            val playList = PlayList(0, name, 0, "")
            // add Data to database
            playListViewModel.addPlayList(playList)
            Toast.makeText(context, "Add PlayList: $name Success", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Add PlayList: $name fail", Toast.LENGTH_SHORT).show()
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
        //
        musicPlayListViewModel.addMusicPlayList(musicPlaylist)
        Toast.makeText(context, "Add PlayList: ${music.name} Success", Toast.LENGTH_SHORT).show()
    }
}

