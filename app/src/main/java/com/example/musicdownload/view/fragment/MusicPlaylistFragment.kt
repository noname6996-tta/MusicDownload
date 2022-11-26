package com.example.musicdownload.view.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
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
import com.example.musicdownload.viewmodel.MusicPlayListViewModel
import com.example.musicdownload.viewmodel.PlayListViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.xuandq.radiofm.data.base.BaseFragment

class MusicPlaylistFragment : BaseFragment() {
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

    private fun initUi() {
        val playList = args.playlist
        binding.recMusicPlaylist.adapter = musicAdapter

        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.recMusicPlaylist.layoutManager = linearLayoutManager

        binding.tvPlaylistName.text = playList.name
        binding.imageView9.setOnClickListener {
            findNavController().popBackStack()
        }

        val id = playList.id
        val listMusicPlaylist = ArrayList<MusicPlaylist>()
        val listMusic = ArrayList<Music>()
        musicPlayListViewModel.readAllMusicData.observe(viewLifecycleOwner,
            Observer { musicplaylist ->
                for (item: Int in musicplaylist.indices) {
                    if (musicplaylist[item].idPlaylist == id) {
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
                        Glide.with(requireContext()).load(listMusicPlaylist[0].image)
                            .error(R.drawable.demo_img_download)
                            .into(binding.imgPlayList)
                        //
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
                        if (checkForInternet(requireContext()) || checkForInternet(requireContext())&& musicplaylist[item].getDownloaded == true){
                            listMusic.add(music)
                        }

                    }
                }
                musicAdapter.setMusicPlaylistList(listMusicPlaylist, requireContext())
                binding.tvListSize.text = listMusicPlaylist.size.toString() + " songs"
            })

        musicAdapter.setClickShowMusic {
            showBottomSheetDeleteSongPlaylist(it, requireContext())
        }

        binding.imgMoreMusicPlaylist.setOnClickListener {
            showBottomSheetUpdatePlaylist(requireContext(), playList)
        }
        musicAdapter.setClickPlayMusic {
            val action = MusicPlaylistFragmentDirections.actionMusicPlaylistFragmentToPlayActivity(it)
            HomeFragment.listMusicHome.addAll(listMusic)
            findNavController().navigate(action)

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
            val updatePlayList = PlayList(playList.id, name, playList.number, playList.image)
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
                "Change name playList: ${playList.name} Success",
                Toast.LENGTH_SHORT
            ).show()
            findNavController().popBackStack()
        }
        builder.setNegativeButton("No") { _, _ ->

        }
        builder.setTitle("Delete ${playList.name} playlist ?")
        builder.setMessage("Chắc ko bro ??")
        builder.create().show()
    }

    private fun deleteMusicPlaylist(musicPlaylist: MusicPlaylist, context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setPositiveButton("Yes") { _, _ ->
            musicPlayListViewModel.deleteMusicPlaylist(musicPlaylist)
            Toast.makeText(
                context,
                "Delete music : ${musicPlaylist.name} Success",
                Toast.LENGTH_SHORT
            ).show()
            findNavController().popBackStack()
        }
        builder.setNegativeButton("No") { _, _ ->

        }
        builder.setTitle("Delete ${musicPlaylist.name} playlist ?")
        builder.setMessage("Chắc ko bro ??")
        builder.create().show()
    }

}