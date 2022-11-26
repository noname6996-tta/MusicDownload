package com.example.musicdownload.view.fragment

import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.media.MediaScannerConnection
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.compose.ui.text.toLowerCase
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.musicdownload.R
import com.example.musicdownload.adapter.HomeTopListenedAdapter
import com.example.musicdownload.data.download.Data
import com.example.musicdownload.data.model.Music
import com.example.musicdownload.data.model.MusicPlaylist
import com.example.musicdownload.databinding.FragmentDownloadedBinding
import com.example.musicdownload.viewmodel.MusicPlayListViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.xuandq.radiofm.data.base.BaseFragment
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.log


class DownloadedFragment : BaseFragment() {
    private lateinit var binding: FragmentDownloadedBinding
    private var arrayMusicModel = ArrayList<Music>()
    private val adapter = HomeTopListenedAdapter()
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
        initUi()
        getDataStoreEx()
        addEvent()
    }

    private fun addEvent() {
        adapter.setClickPlayMusic {
            HomeFragment.listMusicHome.clear()
            HomeFragment.listMusicHome = arrayMusicModel
            val action = DownloadedFragmentDirections.actionDownloadedFragmentToPlayActivity(it)
            findNavController().navigate(action)
        }
    }

    private fun initUi() {
//        if (Data.path.isEmpty()){
//
//        } else {
//            MediaScannerConnection.scanFile(
//                requireContext(), listOf(Data.path).toTypedArray(), null, null)
//        }
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.recDownloaded.layoutManager = linearLayoutManager

        adapter.setClickShowMusic { showBSAfterdownMusic(it) }
        adapter.setClickPlayMusic {
        }
    }

    override fun onResume() {
        super.onResume()


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
        val musicPlayListViewModel = ViewModelProvider(this)[MusicPlayListViewModel::class.java]
        var isFavorite: Boolean = false
        var musicPlaylistid: Int = 0
        musicPlayListViewModel.readAllMusicData.observe(viewLifecycleOwner,
            Observer { musicplaylist ->
                for (item: Int in musicplaylist.indices) {
                    if (musicplaylist[item].name.equals(music.name.toString()) && musicplaylist[item].favorite) {
                        favorite!!.setImageResource(R.drawable.ic_baseline_favorite_true_24)
                        musicPlaylistid = musicplaylist[item].id
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
                musicPlayListViewModel.addMusicPlayList(musicPlaylist)
                favorite.setImageResource(R.drawable.ic_baseline_favorite_true_24)
            } else {
                musicPlayListViewModel.deleteMusicPlaylistWithId(musicPlaylistid.toLong())
                favorite.setImageResource(R.drawable.ic_baseline_favorite_24)
            }
        }

        val viewSetRing: View = bottomSheetDialogSong.findViewById(R.id.viewSetRing)!!
        viewSetRing.setOnClickListener {

        }

    }

    private fun setRing() {
        val k = File("path", "mysong.mp3") // path is a file to /sdcard/media/ringtone


        val values = ContentValues()
        values.put(MediaStore.MediaColumns.DATA, k.getAbsolutePath())
        values.put(MediaStore.MediaColumns.TITLE, "My Song title")
        values.put(MediaStore.MediaColumns.SIZE, 215454)
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3")
        values.put(MediaStore.Audio.Media.ARTIST, "Madonna")
        values.put(MediaStore.Audio.Media.DURATION, 230)
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true)
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false)
        values.put(MediaStore.Audio.Media.IS_ALARM, false)
        values.put(MediaStore.Audio.Media.IS_MUSIC, false)

//Insert it into the database

//Insert it into the database
        val uri = MediaStore.Audio.Media.getContentUriForPath(k.getAbsolutePath())
        val newUri: Uri = requireActivity().getContentResolver().insert(uri!!, values)!!

        RingtoneManager.setActualDefaultRingtoneUri(
            requireActivity(),
            RingtoneManager.TYPE_RINGTONE,
            newUri
        )
    }

    private fun getDataStoreEx() {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST,
        )
        val dirfile: String = "DownloadList"
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0 AND " +"${MediaStore.Audio.Media.DATA} LIKE '%$dirfile%'"
        val cursor = requireContext().contentResolver.query(
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
                arrayMusicModel.add(song)
            }

        }
        if (arrayMusicModel.size == 0) {
            binding.tvHaveNull.visibility = View.VISIBLE
            binding.recDownloaded.visibility = View.GONE
        } else {
            binding.recDownloaded.visibility = View.VISIBLE
            binding.tvHaveNull.visibility = View.GONE
            // set to list view if has data
//            list_Music.adapter = SongAdapter(this, arrayAudioModel)
            binding.recDownloaded.adapter = adapter
            adapter.setMovieList(arrayMusicModel, requireContext())
        }
    }

    private fun getDDSD(){
        val file = Environment.getExternalStorageDirectory().getAbsolutePath();
        File(file).walk().toList().forEach {
            val filemp3 = it.name.toLowerCase(Locale.ROOT).contains(".mp3")
            if (filemp3) {
                Log.d("aaaaa", "file =${it.name}")
            }
        }
    }
}