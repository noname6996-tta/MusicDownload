package com.example.musicdownload.view.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicRanking.adapter.PlayListItemAdapter
import com.example.musicdownload.R
import com.example.musicdownload.data.model.MusicPlaylist
import com.example.musicdownload.data.model.PlayList
import com.example.musicdownload.databinding.FragmentPlaylistBinding
import com.example.musicdownload.viewmodel.MusicPlayListViewModel
import com.example.musicdownload.viewmodel.PlayListViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.xuandq.radiofm.data.base.BaseFragment

class PlayListFragment : BaseFragment() {
    private lateinit var binding: FragmentPlaylistBinding
    lateinit var playListViewModel: PlayListViewModel
    private val adapter = PlayListItemAdapter()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playListViewModel = ViewModelProvider(this)[PlayListViewModel::class.java]
        initUi()
        binding.floatingActionButton.setOnClickListener{
            showDialogAdd(requireActivity())
        }
    }

    private fun initUi() {
        binding.recPlayList.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.recPlayList.layoutManager = linearLayoutManager

        playListViewModel.readAllData.observe(viewLifecycleOwner, Observer {playlist ->
            adapter.setMovieList(playlist,requireContext())
        })
        adapter.setClickShowPlayList {
            showBottomSheetUpdatePlaylist(requireContext(),it)
        }
        adapter.setClickSendInfo {
            val action = PlayListFragmentDirections.actionPlayListFragmentToMusicPlaylistFragment(it)
            findNavController().navigate(action)
        }
        // favorite
        val playlistFa = PlayList(0,"Favorite",0,"")
        binding.viewFavorite.setOnClickListener{
            val action = PlayListFragmentDirections.actionPlayListFragmentToMusicPlaylistFragment(playlistFa)
            findNavController().navigate(action)
        }
    }
    private fun showBottomSheetUpdatePlaylist(context: Context,playList: PlayList) {
        var bottomSheetDialogSong = BottomSheetDialog(context);
        bottomSheetDialogSong.setContentView(R.layout.bottom_sheet_edit_list_music);
        bottomSheetDialogSong.show()
        val viewBottomUpdate = bottomSheetDialogSong.findViewById<View>(R.id.view12)
        viewBottomUpdate!!.setOnClickListener{
            showDialogUpdate(context,playList)
            bottomSheetDialogSong.dismiss()
        }
        val viewBottomDelete = bottomSheetDialogSong.findViewById<View>(R.id.view13)
        viewBottomDelete!!.setOnClickListener{
            deletePlaylist(playList,context)
            bottomSheetDialogSong.dismiss()
        }
    }
    private fun showDialogUpdate(context: Context,playList: PlayList) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.layout_update_playlist)
        var playListName = dialog.findViewById<EditText>(R.id.edtUpdateNamePlayList)
        playListName.setText(playList.name)
        dialog.findViewById<TextView>(R.id.tvUpdatePlayList).setOnClickListener {
            updatePlayList(playList, playListName.text.toString().trim())
            binding.tvNamePlaylist.text = playListName.text.toString().trim()
            dialog.dismiss()
        }
        dialog.findViewById<TextView>(R.id.tvCancelUpdatePlayList).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun updatePlayList(playList: PlayList,name: String){
        if (inputCheck(name)){
            // Create play list
            val updatePlayList = PlayList (playList.id,name,playList.number,playList.image)
            // update Data to database
            playListViewModel.updatePlaylist(updatePlayList)
            Toast.makeText(context, "Change name playList: $name Success",Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Change name playList: $name fail",Toast.LENGTH_SHORT).show()
        }
    }
    private fun deletePlaylist(playList: PlayList,context: Context){
        val builder = AlertDialog.Builder(context)
        builder.setPositiveButton("Yes"){_,_ ->
            playListViewModel.deletePlaylist(playList)
            Toast.makeText(context, "Change name playList: ${playList.name} Success",Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("No"){_,_ ->

        }
        builder.setTitle("Delete ${playList.name} playlist ?")
        builder.setMessage("Chắc ko bro ??")
        builder.create().show()
    }

}