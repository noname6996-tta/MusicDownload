package com.example.musicdownload.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicdownload.R
import com.example.musicdownload.data.model.Genre
import com.example.musicdownload.databinding.ItemGenresHomeBinding
import com.example.musicdownload.databinding.ItemMoreGenresBinding


class HomeMoreGenresAdapter(private val onClickGenre: (Genre) -> Unit) : RecyclerView.Adapter<HomeFragmentMoreGenresViewHolder>() {
    companion object {
        val URL_GET_IMAGE :String = "http://marstechstudio.com/img-msd/"
    }
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
    ): HomeFragmentMoreGenresViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemMoreGenresBinding.inflate(inflater, parent, false)
        return HomeFragmentMoreGenresViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeFragmentMoreGenresViewHolder, position: Int) {
        val genre = genres[position]
        holder.binding.tvMoreItems.text = genre.name
        Glide.with(holder.itemView.context).load(URL_GET_IMAGE+genre.image).error(R.drawable.demo_img_download)
            .into(holder.binding.imgMoreGeners)
        holder.binding.layoutItemsMore.setOnClickListener{
            onClickGenre(genres[position])
        }
    }

    override fun getItemCount(): Int {
        return genres.size
    }
}

class HomeFragmentMoreGenresViewHolder(val binding: ItemMoreGenresBinding) :
    RecyclerView.ViewHolder(binding.root) {
}