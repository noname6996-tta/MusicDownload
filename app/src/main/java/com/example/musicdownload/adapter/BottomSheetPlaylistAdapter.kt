package com.example.musicdownload.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicdownload.R
import com.example.musicdownload.data.model.Music
import com.example.musicdownload.data.model.PlayList
import com.example.musicdownload.databinding.ItemCreatePlaylistBinding


class BottomSheetPlaylistAdapter : RecyclerView.Adapter<BottomSheetPlaylistAdapterViewHolder>() {
    private var playLists: List<PlayList> = listOf()
    private lateinit var context: Context
    fun setPlayList(playLists: List<PlayList>, context: Context) {
        this.playLists = playLists.toMutableList()
        this.context = context
        notifyDataSetChanged()
    }
    private var onClickMusic: ((PlayList) -> Unit)? = null
    fun setAddMusicToPlaylist(listener: ((PlayList) -> Unit)) {
        onClickMusic = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BottomSheetPlaylistAdapterViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCreatePlaylistBinding.inflate(inflater, parent, false)
        return BottomSheetPlaylistAdapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BottomSheetPlaylistAdapterViewHolder, position: Int) {
        val playList = playLists[position]
        holder.binding.tvBottomSheetNamePlaylist.text = playList.name
        holder.binding.tvNumberSongPlaylist.text = "${playLists[position].number} songs"
        holder.binding.layoutItemPlaylist.setOnClickListener{
            onClickMusic?.let{
                it(playLists[position])
            }
        }
        Glide.with(holder.itemView.context).load(playList.image).error(R.drawable.demo_img_download)
            .into(holder.binding.imgBottomSheetSong)
    }

    override fun getItemCount(): Int {
        return playLists.size
    }
}

class BottomSheetPlaylistAdapterViewHolder(val binding: ItemCreatePlaylistBinding) :
    RecyclerView.ViewHolder(binding.root) {
}