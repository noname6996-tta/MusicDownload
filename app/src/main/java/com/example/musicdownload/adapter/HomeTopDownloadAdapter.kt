package com.example.musicdownload.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicdownload.R
import com.example.musicdownload.data.model.Music
import com.example.musicdownload.databinding.ItemDownloadHomeBinding
import com.example.musicdownload.databinding.ItemTopDownloadBinding
import com.example.musicdownload.view.fragment.PlayActivity


class HomeTopDownloadAdapter() : RecyclerView.Adapter<HomeFragmentTopDownloadViewHolder>() {
    private var musics: List<Music> = listOf()
    private lateinit var context: Context
    fun setMovieList(movies: List<Music>, context: Context) {
        this.musics = movies.toMutableList()
        this.context = context
        notifyDataSetChanged()
    }
    private var onClickMusic: ((Music) -> Unit)? = null
    fun setClickShowMusic(listener: ((Music) -> Unit)) {
        onClickMusic = listener
    }
    private var onClickPlayMusic: ((i:Int) -> Unit)? = null
    fun setClickPlayMusic(position: ((i:Int) -> Unit)) {
        onClickPlayMusic = position
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HomeFragmentTopDownloadViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTopDownloadBinding.inflate(inflater, parent, false)
        return HomeFragmentTopDownloadViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeFragmentTopDownloadViewHolder, position: Int) {
        val music = musics[position]
        holder.binding.tvTopDownloadNameSong.text = music.name
        holder.binding.tvTopDownloadSingerSong.text = music.artistName
        Glide.with(holder.itemView.context).load(music.image).error(R.drawable.demo_img_download)
            .into(holder.binding.imgItemTopDownload)
        holder.binding.imgMoreTopDownload.setOnClickListener{
            onClickMusic?.let{
                it(musics[position])
            }
        }
        holder.binding.imgItemTopDownload.setOnClickListener{
            onClickPlayMusic?.let{
                it(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return musics.size
    }
}

class HomeFragmentTopDownloadViewHolder(val binding: ItemTopDownloadBinding) :
    RecyclerView.ViewHolder(binding.root) {
}