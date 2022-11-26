package com.example.musicdownload.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicdownload.R
import com.example.musicdownload.data.model.Genre
import com.example.musicdownload.databinding.ItemGenresHomeBinding


class HomeGenresAdapter(private val onClickGenre: (Genre) -> Unit) : RecyclerView.Adapter<HomeFragmentGenresViewHolder>() {
    private var genres: List<Genre> = listOf()
    private lateinit var context: Context
    fun setGenresList(genres: List<Genre>, context: Context) {
        this.genres = genres.toMutableList()
        this.context = context
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HomeFragmentGenresViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemGenresHomeBinding.inflate(inflater, parent, false)
        return HomeFragmentGenresViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeFragmentGenresViewHolder, position: Int) {
        val genre = genres[position]
        holder.binding.tvNameGenres.text = genre.name
        Glide.with(holder.itemView.context).load("http://marstechstudio.com/img-msd/"+genre.image).error(R.drawable.demo_img_download)
            .into(holder.binding.imgGeners)
        holder.binding.cardViewMoreGneres.setOnClickListener{
            onClickGenre(genres[position])
        }
    }

    override fun getItemCount(): Int {
        return genres.size
    }
}

class HomeFragmentGenresViewHolder(val binding: ItemGenresHomeBinding) :
    RecyclerView.ViewHolder(binding.root) {
}