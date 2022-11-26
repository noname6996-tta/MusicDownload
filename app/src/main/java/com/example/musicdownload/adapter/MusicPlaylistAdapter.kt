package com.example.musicdownload.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicdownload.R
import com.example.musicdownload.data.model.Music
import com.example.musicdownload.data.model.MusicPlaylist
import com.example.musicdownload.databinding.ItemMusicPlaylistBinding
import com.example.musicdownload.databinding.ItemTopLisntenedBinding
import com.example.musicdownload.view.fragment.HomeFragment


class MusicPlaylistAdapter() :
    RecyclerView.Adapter<MusicPlaylistViewHolder>() {
    private var musicPlaylists: List<MusicPlaylist> = listOf()
    private lateinit var context: Context
    fun setMusicPlaylistList(movies: List<MusicPlaylist>, context: Context) {
        this.musicPlaylists = movies.toMutableList()
        this.context = context
        notifyDataSetChanged()
    }
    private var onClickMusic: ((MusicPlaylist) -> Unit)? = null
    fun setClickShowMusic(listener: ((MusicPlaylist) -> Unit)) {
        onClickMusic = listener
    }
    private var onClickPlayMusic: ((i:Int) -> Unit)? = null
    fun setClickPlayMusic(position: ((i:Int) -> Unit)) {
        onClickPlayMusic = position
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MusicPlaylistViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemMusicPlaylistBinding.inflate(inflater, parent, false)
        return MusicPlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MusicPlaylistViewHolder, position: Int) {
        val musicPlaylist = musicPlaylists[position]
        HomeFragment.positionMusic = position
        holder.binding.tvItemMusicPlaylistNameSong.text = musicPlaylist.name
        holder.binding.tvItemMusicPlaylistSingerSong.text = musicPlaylist.artists
        holder.binding.imgMoreItemMusicPlaylist.setOnClickListener {
            onClickMusic?.let{
                it(musicPlaylists[position])
            }
        }
        holder.binding.imgItemMusicPlaylist.setOnClickListener{
            onClickPlayMusic?.let{
                it(position)
            }
        }
        Glide.with(holder.itemView.context).load(musicPlaylist.image).error(R.drawable.demo_img_download)
            .into(holder.binding.imgItemMusicPlaylist)
    }

    override fun getItemCount(): Int {
        return musicPlaylists.size
    }
}

class MusicPlaylistViewHolder(val binding: ItemMusicPlaylistBinding) :
    RecyclerView.ViewHolder(binding.root) {
}