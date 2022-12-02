package com.example.musicRanking.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicdownload.R
import com.example.musicdownload.data.model.Music
import com.example.musicdownload.databinding.ItemMoreBinding


class HomeMoreAdapter(private val onClickMusic: (Music)->Unit) : RecyclerView.Adapter<HomeMoreViewHolder>(){
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
    ): HomeMoreViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemMoreBinding.inflate(inflater, parent, false)
        return HomeMoreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeMoreViewHolder, position: Int) {
        val music = musics[position]
        holder.binding.tvMoreSingerSong.text = music.name
        holder.binding.tvMoreNameSong.text = music.artistName
        holder.binding.imgMoreTopListened.setOnClickListener{
            onClickMusic(musics[position])
        }
        Glide.with(holder.itemView.context).load(music.image).error(R.drawable.demo_img_download)
            .into(holder.binding.imgMore)
        holder.binding.layoutItemMorePlayMusic.setOnClickListener {
            onClickPlayMusic?.let{
                it(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return musics.size
    }
}

class HomeMoreViewHolder(val binding: ItemMoreBinding) :
    RecyclerView.ViewHolder(binding.root) {
}