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
import com.example.musicdownload.view.activity.PlayActivity


class HomeDownloadAdapter : RecyclerView.Adapter<HomeFragmentDownloadViewHolder>() {
    private var musics: List<Music> = listOf()
    private lateinit var context: Context
    fun setMovieList(movies: List<Music>, context: Context) {
        this.musics = movies.toMutableList()
        this.context = context
        notifyDataSetChanged()
    }

    private var onClickPlayMusic: ((i:Int) -> Unit)? = null
    fun setClickPlayMusic(position: ((i:Int) -> Unit)) {
        onClickPlayMusic = position
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HomeFragmentDownloadViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemDownloadHomeBinding.inflate(inflater, parent, false)
        return HomeFragmentDownloadViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeFragmentDownloadViewHolder, position: Int) {
        val music = musics[position]
        holder.binding.tvNameSongHomeDownload.text = music.name
        holder.binding.tvSingerSongHomeDownload.text = music.artistName
        holder.binding.layoutCardHomeDownload.setOnClickListener{
            val intent  = Intent(context, PlayActivity::class.java)
            intent.putExtra("index",position)
            intent.putExtra("object","Music Adapter")
            ContextCompat.startActivity(context,intent,null)
        }
        Glide.with(holder.itemView.context).load(music.image).error(R.drawable.demo_img_download)
            .into(holder.binding.imageViewHomeDownload)
        holder.binding.layoutCardHomeDownload.setOnClickListener{
            onClickPlayMusic?.let{
                it(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return musics.size
    }
}

class HomeFragmentDownloadViewHolder(val binding: ItemDownloadHomeBinding) :
    RecyclerView.ViewHolder(binding.root) {
}