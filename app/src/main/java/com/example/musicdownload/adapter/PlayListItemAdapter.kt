package com.example.musicRanking.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicdownload.R
import com.example.musicdownload.data.model.Music
import com.example.musicdownload.data.model.PlayList
import com.example.musicdownload.databinding.ItemPlaylistBinding

class PlayListItemAdapter : RecyclerView.Adapter<PlayListViewHolder>() {
    private var playLists: List<PlayList> = listOf()
    private lateinit var context: Context
    fun setMovieList(playLists: List<PlayList>, context: Context) {
        this.playLists = playLists.toMutableList()
        this.context = context
        notifyDataSetChanged()
    }
    private var onClickPlayList: ((PlayList) -> Unit)? = null
    fun setClickShowPlayList(listener: ((PlayList) -> Unit)) {
        onClickPlayList = listener
    }
    private var onClickSendPlayList: ((PlayList) -> Unit)? = null
    fun setClickSendInfo(listener: ((PlayList) -> Unit)) {
        onClickSendPlayList = listener
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlayListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPlaylistBinding.inflate(inflater, parent, false)
        return PlayListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlayListViewHolder, position: Int) {
        val playList = playLists[position]
        holder.binding.tvNamePlaylist.text = playList.name
        Glide.with(holder.itemView.context).load(playList.image).error(R.drawable.demo_img_download)
            .into(holder.binding.imgItemPlaylist)
        holder.binding.imgMorePlayList.setOnClickListener {
            onClickPlayList?.let{
                it(playLists[position])
            }
        }
        holder.binding.viewImgPlaylist.setOnClickListener{
            onClickSendPlayList?.let{
                it(playLists[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return playLists.size
    }
}

class PlayListViewHolder(val binding: ItemPlaylistBinding) :
    RecyclerView.ViewHolder(binding.root) {
}