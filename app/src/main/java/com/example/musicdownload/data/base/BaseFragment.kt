package com.xuandq.radiofm.data.base

import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
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
import com.example.musicdownload.data.model.MusicLocal
import com.example.musicdownload.data.model.MusicPlaylist
import com.example.musicdownload.data.model.PlayList
import com.example.musicdownload.view.fragment.DownloadedFragment
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
import java.io.File

open class BaseFragment : Fragment() {
    private var listMusicOffline = ArrayList<Music>()
    var arrayMusicLocalBase = ArrayList<MusicLocal>()
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
        var musicPlaylistid: String = ""
        musicPlayListViewModel.readAllMusicData.observe(viewLifecycleOwner,
            Observer { musicplaylist ->
                for (item: Int in 0..musicplaylist.size - 1) {
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
                    0,
                    music.name,
                    music.artistName,
                    music.duration,
                    music.image,
                    music.audio
                )
                // check xem co trong playlist nao chưa
                musicPlayListViewModel.addMusicPlayList(musicPlaylist)
                favorite.setImageResource(R.drawable.ic_baseline_favorite_true_24)
            } else {
                musicPlayListViewModel.deleteMusicPlaylistWithId(musicPlaylistid.toString().trim())
                favorite.setImageResource(R.drawable.ic_baseline_favorite_24)
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
                FetchConfiguration.Builder(requireContext())
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
            bottomSheetDialogSong.dismiss()
        }

        viewRemoveDownloadSong?.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setPositiveButton("Yes"){_,_ ->
                for (i in 0..arrayMusicLocalBase.size - 1) {
                    if (music.name == arrayMusicLocalBase[i].title) {
                        val fdelete: File = File(arrayMusicLocalBase[i].data)
                        fdelete.delete()
                        MediaScannerConnection.scanFile(
                            requireContext(), arrayOf(arrayMusicLocalBase[i].data), null, null
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
                Toast.makeText(context, "Remove song Success",Toast.LENGTH_SHORT).show()
            }
            builder.setNegativeButton("No"){_,_ ->

            }
            builder.setTitle("Remove song from this phone?")
            builder.setMessage("Do you really want??")
            builder.create().show()
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

        val btnBack = bottomSheetDialogSong.findViewById<ImageView>(R.id.imgBackAddMusicToPlayList)
        btnBack?.setOnClickListener {
            bottomSheetDialogSong.dismiss()
        }
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
            val playList = PlayList(0,name, 0, "")
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
        var count: Int = 0
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
        musicPlayListViewModel.readAllMusicData.observe(this,
            Observer { musicplaylist ->
                for (item: Int in musicplaylist.indices) {
                    if (musicplaylist[item].idPlayList == playList.id && musicplaylist[item].name == music.name) {
                        count++
                    }
                }
                Log.e("count", count.toString())
                if (count > 0) {
                    Toast.makeText(
                        requireContext(),
                        "Song ${music.name} already in this Playlist",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    musicPlayListViewModel.addMusicPlayList(musicPlaylist)
                    Toast.makeText(
                        requireContext(),
                        "Add PlayList: ${music.name} Success",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
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
            val cursor = requireActivity().contentResolver.query(
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

                    val songLocal = MusicLocal(
                        id, title, data, duration, album, name
                    )

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
                    arrayMusicLocalBase.add(songLocal)
                    listMusicOffline.add(song)
                }
            }
        } catch (e : Exception){
            Log.e("getDataStoreEx",e.toString())
        }
    }
}
