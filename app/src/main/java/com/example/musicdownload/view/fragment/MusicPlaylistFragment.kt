package com.example.musicdownload.view.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.musicdownload.R
import com.example.musicdownload.adapter.MusicPlaylistAdapter
import com.example.musicdownload.data.model.Music
import com.example.musicdownload.data.model.MusicPlaylist
import com.example.musicdownload.data.model.PlayList
import com.example.musicdownload.databinding.ListPlaylistLayoutBinding
import com.example.musicdownload.view.activity.PlayActivity
import com.example.musicdownload.viewmodel.MusicPlayListViewModel
import com.example.musicdownload.viewmodel.PlayListViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.xuandq.radiofm.data.base.BaseFragment

class MusicPlaylistFragment : BaseFragment() {
    private var listMusicOffline = ArrayList<Music>()
    private lateinit var binding: ListPlaylistLayoutBinding
    private val musicAdapter = MusicPlaylistAdapter()
    private lateinit var musicPlayListViewModel: MusicPlayListViewModel
    private lateinit var playListViewModel: PlayListViewModel
    private val args: MusicPlaylistFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ListPlaylistLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        musicPlayListViewModel = ViewModelProvider(this)[MusicPlayListViewModel::class.java]
        playListViewModel = ViewModelProvider(this)[PlayListViewModel::class.java]
        initUi()

    }

    @SuppressLint("SuspiciousIndentation")
    private fun initUi() {
        val playList = args.playlist
        if (args.favorite) {
            binding.imgMoreMusicPlaylist.visibility = View.GONE
            binding.imgPlayList.visibility = View.GONE
            binding.imgPlayListFa.visibility = View.VISIBLE
        }
        binding.recMusicPlaylist.adapter = musicAdapter

        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.recMusicPlaylist.layoutManager = linearLayoutManager
        Glide.with(requireContext()).load(playList.image).error(R.drawable.playlistmusic)
            .into(binding.imgPlayList)
        binding.tvPlaylistName.text = playList.name
        binding.imageView9.setOnClickListener {
            findNavController().popBackStack()
        }

        val id = playList.id
        var listMusicPlaylist = ArrayList<MusicPlaylist>()
        val listMusic = ArrayList<Music>()
        musicPlayListViewModel.readAllMusicData.observe(viewLifecycleOwner,
            Observer { musicplaylist ->
                for (item: Int in musicplaylist.indices) {
                    if (args.favorite){
                        if (musicplaylist[item].favorite) {
                            listMusicPlaylist.add(musicplaylist[item])
                            val music = Music(
                                "",
                                musicplaylist[item].name,
                                musicplaylist[item].duration,
                                "",
                                musicplaylist[item].artists,
                                "",
                                "",
                                "",
                                "",
                                0,
                                "",
                                "",
                                musicplaylist[item].audio,
                                "",
                                "",
                                "",
                                "",
                                musicplaylist[item].image,
                                true,
                                ""
                            )
                            listMusic.add(music)
                            val hashSet = HashSet<MusicPlaylist>()
                            hashSet.addAll(listMusicPlaylist)
                            listMusicPlaylist.clear()
                            listMusicPlaylist.addAll(hashSet)

                        }
                        musicAdapter.setMusicPlaylistList(listMusicPlaylist, requireContext())
                        binding.tvListSize.text = listMusicPlaylist.size.toString() + " songs"
                        musicAdapter.notifyDataSetChanged()
                        if  (listMusicPlaylist.size==0){
                            binding.tvListSize.setText("No Song in the Playlist")
                            binding.imgPlayListMusic.visibility = View.GONE}
                        else {
                            binding.tvListSize.text = listMusicPlaylist.size.toString() + " songs"
                            binding.imgPlayListMusic.visibility = View.VISIBLE
                        }
                    }
                    else {
                        if (musicplaylist[item].idPlayList == id) {
                            listMusicPlaylist.add(musicplaylist[item])
                            // Create play list
                            val updatePlayList = PlayList(
                                playList.id,
                                playList.name,
                                listMusicPlaylist.size,
                                listMusicPlaylist[0].image
                            )
                            // update Data to database
                            playListViewModel.updatePlaylist(updatePlayList)
                            val music = Music(
                                "",
                                musicplaylist[item].name,
                                musicplaylist[item].duration,
                                "",
                                musicplaylist[item].artists,
                                "",
                                "",
                                "",
                                "",
                                0,
                                "",
                                "",
                                musicplaylist[item].audio,
                                "",
                                "",
                                "",
                                "",
                                musicplaylist[item].image,
                                true,
                                ""
                            )
                            listMusic.add(music)
                        }
                        musicAdapter.setMusicPlaylistList(listMusicPlaylist, requireContext())
                        if  (listMusicPlaylist.size==0){
                            binding.tvListSize.setText("No Song in the Playlist")
                            binding.imgPlayListMusic.visibility = View.GONE
                        } else {
                            binding.tvListSize.text = listMusicPlaylist.size.toString() + " songs"
                            binding.imgPlayListMusic.visibility = View.VISIBLE
                        }
                        musicAdapter.notifyDataSetChanged()
                    }
                }
            })

        musicAdapter.setClickShowMusic {
            showBottomSheetDeleteSongPlaylist(it, requireContext())
        }

        binding.imgMoreMusicPlaylist.setOnClickListener {
            showBottomSheetUpdatePlaylist(requireContext(), playList)
        }
        musicAdapter.setClickPlayMusic {
            HomeFragment.listMusicHome.clear()
            PlayActivity.isPlaying = false
            val intent = Intent(activity, PlayActivity::class.java)
            intent.putExtra("MainActivitySong", "PlaylistFragment")
            if (checkForInternet(requireContext())) {
                HomeFragment.listMusicHome = listMusic
                intent.putExtra("index", it)
                startActivity(intent)
            } else {
                getDataStoreEx()
                var count = 0
                for (j in 0..listMusic.size - 1) {
                    for (i in 0..listMusicOffline.size - 1) {
                        if (listMusic[j].name.equals(listMusicOffline[i].name)) {
                            HomeFragment.listMusicHome.add(listMusicOffline[i])
                            count ++
                        } else {
                        }
                    }
                }
                if (count>0){
                    intent.putExtra("index", 0)
                    startActivity(intent)
                }
            }
        }
        binding.imgPlayListMusic.setOnClickListener {
            HomeFragment.listMusicHome.clear()
            PlayActivity.isPlaying = false
            val intent = Intent(activity, PlayActivity::class.java)
            intent.putExtra("MainActivitySong", "PlaylistFragment")
            if (checkForInternet(requireContext())) {
                HomeFragment.listMusicHome = listMusic
                intent.putExtra("index", 0)
                startActivity(intent)
            } else {
                getDataStoreEx()
                var count = 0
                for (j in 0..listMusic.size - 1) {
                    for (i in 0..listMusicOffline.size - 1) {
                        if (listMusic[j].name.equals(listMusicOffline[i].name)) {
                            HomeFragment.listMusicHome.add(listMusicOffline[i])
                            count ++
                        } else {
                        }
                    }
                }
                if (count>0){
                    intent.putExtra("index", 0)
                    startActivity(intent)
                }

            }
        }
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
        val selection =
            MediaStore.Audio.Media.IS_MUSIC + "!= 0 AND " + "${MediaStore.Audio.Media.DATA} LIKE '%$dirfile%'"
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
                listMusicOffline.add(song)
            }
        }
    }

    private fun showBottomSheetUpdatePlaylist(context: Context, playList: PlayList) {
        val bottomSheetDialogSong = BottomSheetDialog(context);
        bottomSheetDialogSong.setContentView(R.layout.bottom_sheet_edit_list_music);
        bottomSheetDialogSong.show()
        val viewBottomUpdate = bottomSheetDialogSong.findViewById<View>(R.id.view12)
        viewBottomUpdate!!.setOnClickListener {
            showDialogUpdate(context, playList)
            bottomSheetDialogSong.dismiss()
        }
        val viewBottomDelete = bottomSheetDialogSong.findViewById<View>(R.id.view13)
        viewBottomDelete!!.setOnClickListener {
            deletePlaylist(playList, context)
            bottomSheetDialogSong.dismiss()
        }
    }

    private fun showBottomSheetDeleteSongPlaylist(musicPlaylist: MusicPlaylist, context: Context) {
        val bottomSheetDialogSong = BottomSheetDialog(context);
        bottomSheetDialogSong.setContentView(R.layout.bottom_sheet_remove_list_music);
        bottomSheetDialogSong.show()
        val viewBottomUpdate = bottomSheetDialogSong.findViewById<View>(R.id.view12)
        viewBottomUpdate!!.setOnClickListener {
            deleteMusicPlaylist(musicPlaylist, requireContext())
            bottomSheetDialogSong.dismiss()
        }
    }

    private fun showDialogUpdate(context: Context, playList: PlayList) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.layout_update_playlist)
        var playListName = dialog.findViewById<EditText>(R.id.edtUpdateNamePlayList)
        playListName.setText(playList.name)
        dialog.findViewById<TextView>(R.id.tvUpdatePlayList).setOnClickListener {
            updatePlayList(playList, playListName.text.toString().trim())
            binding.tvPlaylistName.text = playListName.text.toString().trim()
            dialog.dismiss()
        }
        dialog.findViewById<TextView>(R.id.tvCancelUpdatePlayList).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun updatePlayList(playList: PlayList, name: String) {
        if (inputCheck(name)) {
            // Create play list
            val updatePlayList = PlayList(playList.id,name, playList.number, playList.image)
            // update Data to database
            playListViewModel.updatePlaylist(updatePlayList)
            Toast.makeText(context, "Change name playList: $name Success", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(context, "Change name playList: $name fail", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deletePlaylist(playList: PlayList, context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setPositiveButton("Yes") { _, _ ->
            playListViewModel.deletePlaylist(playList)
            Toast.makeText(
                context,
                "Playlist deleted",
                Toast.LENGTH_SHORT
            ).show()
            findNavController().popBackStack()
        }
        builder.setNegativeButton("No") { _, _ ->

        }
        builder.setTitle("Remove ${playList.name} playlist ?")
        builder.setMessage("Do you really want delete this?")
        builder.create().show()
    }

    private fun deleteMusicPlaylist(musicPlaylist: MusicPlaylist, context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setPositiveButton("Yes") { _, _ ->
            musicPlayListViewModel.deleteMusicPlaylist(musicPlaylist)
            Toast.makeText(
                context,
                "Remove music : ${musicPlaylist.name} Success",
                Toast.LENGTH_SHORT
            ).show()
            findNavController().popBackStack()
        }
        builder.setNegativeButton("No") { _, _ ->

        }
        builder.setTitle("Remove ${musicPlaylist.name} playlist ?")
        builder.setMessage("Do you really want remove this?")
        builder.create().show()
    }

}