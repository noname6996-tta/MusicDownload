package com.example.musicRanking.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicdownload.R
import com.example.musicdownload.data.model.Music
import com.example.musicdownload.databinding.ItemTopRankingBinding
import com.example.musicdownload.view.fragment.PlayActivity


class HomeRankingAdapter : RecyclerView.Adapter<HomeFragmentRankingViewHolder>() {
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
    ): HomeFragmentRankingViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTopRankingBinding.inflate(inflater, parent, false)
        return HomeFragmentRankingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeFragmentRankingViewHolder, position: Int) {
        val music = musics[position]
        holder.binding.tvTopRankingNameSong.text = music.name
        holder.binding.tvTopRankingArtisSong.text = music.artistName
        holder.binding.tvNumberRanking.text = (position+1).toString()
        when(position){
            0-> Glide.with(holder.itemView.context).load(R.drawable.topranking1).error(R.drawable.topranking1)
                .into(holder.binding.imgTagRanking)
            1-> Glide.with(holder.itemView.context).load(R.drawable.topranking2).error(R.drawable.topranking1)
                .into(holder.binding.imgTagRanking)
            2-> Glide.with(holder.itemView.context).load(R.drawable.topranking3).error(R.drawable.topranking1)
                .into(holder.binding.imgTagRanking)
            3-> Glide.with(holder.itemView.context).load(R.drawable.topranking4).error(R.drawable.topranking1)
                .into(holder.binding.imgTagRanking)
            4-> Glide.with(holder.itemView.context).load(R.drawable.topranking4).error(R.drawable.topranking1)
                .into(holder.binding.imgTagRanking)
        }
        Glide.with(holder.itemView.context).load(music.image).error(R.drawable.demo_img_download)
            .into(holder.binding.imgTopRanking)
        holder.binding.layoutTopRanking.setOnClickListener{
            onClickPlayMusic?.let{
                it(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return musics.size
    }
}

class HomeFragmentRankingViewHolder(val binding: ItemTopRankingBinding) :
    RecyclerView.ViewHolder(binding.root) {
}