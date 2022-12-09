package com.example.musicdownload.view.fragment

import android.app.AlertDialog
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.musicdownload.R
import com.example.musicdownload.adapter.HomeTopListenedAdapter
import com.example.musicdownload.data.download.Data
import com.example.musicdownload.data.model.Music
import com.example.musicdownload.data.model.MusicLocal
import com.example.musicdownload.data.model.MusicPlaylist
import com.example.musicdownload.databinding.FragmentDownloadedBinding
import com.example.musicdownload.view.activity.PlayActivity
import com.example.musicdownload.viewmodel.MusicPlayListViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.xuandq.radiofm.data.base.BaseFragment
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*


class DownloadedFragment : BaseFragment() {
    private lateinit var binding: FragmentDownloadedBinding

    companion object {
        var arrayMusicModel = ArrayList<Music>()
    }

    var arrayMusicLocal = ArrayList<MusicLocal>()
    private val adapter = HomeTopListenedAdapter()
    lateinit var musicPlayListViewModel: MusicPlayListViewModel
    var isFavorite: Boolean = false
    var musicPlaylistid: String = ""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDownloadedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        musicPlayListViewModel =
            ViewModelProvider(this)[com.example.musicdownload.viewmodel.MusicPlayListViewModel::class.java]
        initUi()
        getDataStoreEx()
    }


    private fun initUi() {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.recDownloaded.layoutManager = linearLayoutManager
        adapter.setClickShowMusic { showBSAfterdownMusic(it) }
        adapter.setClickPlayMusic {
            HomeFragment.listMusicHome.clear()
            PlayActivity.isPlaying = false
            val intent = Intent(activity, PlayActivity::class.java)
            intent.putExtra("MainActivitySong", "DownloadedFragment")
            HomeFragment.listMusicHome = arrayMusicModel
            intent.putExtra("index", it)
            startActivity(intent)
        }
    }

    private fun showBSAfterdownMusic(music: Music) {
        val bottomSheetDialogSong = BottomSheetDialog(this.requireContext());
        bottomSheetDialogSong.setContentView(R.layout.bottom_sheet_download);
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
        musicPlayListViewModel.readAllMusicData.observe(viewLifecycleOwner,
            Observer { musicplaylist ->
                var count: Int = 0
                for (item: Int in 0..musicplaylist.size - 1) {
                    if (musicplaylist[item].name.equals(music.name.toString()) && musicplaylist[item].favorite) {
                        musicPlaylistid = musicplaylist[item].name
                        count++
                    }
                }
                if (count > 0) {
                    favorite!!.setImageResource(R.drawable.ic_baseline_favorite_true_24)
                    isFavorite = true
                } else {
                    favorite!!.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                    isFavorite = false
                }
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
                        musicPlayListViewModel.addMusicPlayList(musicPlaylist)
                        favorite!!.setImageResource(R.drawable.ic_baseline_favorite_true_24)
                        isFavorite = true
                    } else {
                        musicPlayListViewModel.deleteMusicPlaylistWithId(
                            musicPlaylistid.toString().trim()
                        )
                        favorite!!.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                        isFavorite = false
                    }
                }
                val viewSetRing: View = bottomSheetDialogSong.findViewById(R.id.viewSetRing)!!
                viewSetRing.setOnClickListener {
                    for (i in 0..arrayMusicLocal.size - 1) {
                        if (music.name == arrayMusicLocal[i].title) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                checkSystemWriteSettings(requireContext()) {
                                    if (it) {
                                        setAsRingtone(arrayMusicLocal[i])
                                        if (setAsRingtone(arrayMusicLocal[i])) {
                                            Toast.makeText(
                                                requireContext(),
                                                "Set ringtone success",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                requireContext(),
                                                "Set ringtone fail",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
                val viewRemoveDownloadSong: View =
                    bottomSheetDialogSong.findViewById(R.id.viewRemoveDownloadSong)!!
                viewRemoveDownloadSong.setOnClickListener {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setPositiveButton("Yes") { _, _ ->
                        for (i in 0..arrayMusicLocal.size - 1) {
                            if (music.name == arrayMusicLocal[i].title) {
                                val fdelete: File = File(arrayMusicLocal[i].data)
                                fdelete.delete()
                                arrayMusicModel.remove(arrayMusicModel[i])
                                MediaScannerConnection.scanFile(
                                    requireContext(), arrayOf(arrayMusicLocal[i].data), null, null
                                )
                                adapter.setMovieList(arrayMusicModel, requireContext())
                                bottomSheetDialogSong.dismiss()
                            }
                        }
                        Toast.makeText(requireContext(), "Remove song Success",Toast.LENGTH_SHORT).show()
                    }
                    builder.setNegativeButton("No") { _, _ ->

                    }
                    builder.setTitle("Remove song from this phone?")
                    builder.setMessage("Do you really want??")
                    builder.create().show()
                }
            })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun checkSystemWriteSettings(ctx: Context, onGranted: (Boolean) -> Unit) {
        if (!Settings.System.canWrite(ctx)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                Uri.parse("package:" + ctx.packageName)
            )
            ctx.startActivity(intent)
            onGranted(false)
        } else {
            onGranted(true)
        }
    }

    private fun setAsRingtone(musicDownloaded: MusicLocal): Boolean {
        val values = ContentValues()
        val file = File(musicDownloaded.data!!)
        values.put(MediaStore.MediaColumns.TITLE, musicDownloaded.title)
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg")
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val newUri: Uri? = requireContext().contentResolver
                .insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
            try {
                requireContext().contentResolver.openOutputStream(newUri!!).use { os ->
                    val size = file.length().toInt()
                    val bytes = ByteArray(size)
                    try {
                        val buf = BufferedInputStream(FileInputStream(file))
                        buf.read(bytes, 0, bytes.size)
                        buf.close()
                        os?.write(bytes)
                        os?.close()
                        os?.flush()
                    } catch (e: IOException) {
                        return false
                    }
                }
            } catch (ignored: Exception) {
                return false
            }
            RingtoneManager.setActualDefaultRingtoneUri(
                context,
                RingtoneManager.TYPE_RINGTONE,
                newUri
            )
        } else {
            values.put(MediaStore.MediaColumns.DATA, file.absolutePath)
            val uri = MediaStore.Audio.Media.getContentUriForPath(file.absolutePath)
            requireContext().contentResolver
                .delete(uri!!, MediaStore.MediaColumns.DATA + "=" + file.absolutePath + "", null)
            val newUri: Uri? = requireContext().contentResolver.insert(uri, values)
            RingtoneManager.setActualDefaultRingtoneUri(
                context,
                RingtoneManager.TYPE_RINGTONE,
                newUri
            )
            MediaStore.Audio.Media.getContentUriForPath(file.absolutePath)?.let {
                requireContext().contentResolver
                    .insert(it, values)
            }
        }
        return true
    }

    private fun getDataStoreEx() {
        arrayMusicLocal = ArrayList()
        arrayMusicModel = ArrayList()
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
        val cursor = requireContext().contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            null
        )

        try {
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
                    arrayMusicLocal.add(songLocal)
                    arrayMusicModel.add(song)
                    if (Data.listDownload.size > 0) {
                        for (i in 0..Data.listDownload.size - 1) {
                            if (Data.listDownload[i].name == song.name) {
                                arrayMusicModel.remove(song)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {

        }

        if (arrayMusicModel.size == 0) {
            binding.recDownloaded.visibility = View.GONE
        } else {
            binding.recDownloaded.visibility = View.VISIBLE
            // set to list view if has data
            binding.recDownloaded.adapter = adapter
            adapter.setMovieList(arrayMusicModel, requireContext())
        }
    }

    private fun getDDSD() {
        val file = Environment.getExternalStorageDirectory().getAbsolutePath();
        File(file).walk().toList().forEach {
            val filemp3 = it.name.toLowerCase(Locale.ROOT).contains(".mp3")
            if (filemp3) {
                Log.d("aaaaa", "file =${it}")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getDataStoreEx()
    }
}