package com.example.musicRanking.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicdownload.R
import com.example.musicdownload.data.model.Music
import com.example.musicdownload.databinding.ItemTopLisntenedBinding
import java.util.*


class SearchListenedAdapter(private val onClickMusic: (Music) -> Unit) :
    RecyclerView.Adapter<SearchListenedViewHolder>() {
    private var musics: List<Music> = listOf()
    private var musicsold: List<Music> = listOf()
    private lateinit var context: Context
    fun setMovieList(movies: List<Music>, context: Context) {
        this.musics = movies.toMutableList()
        this.context = context
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchListenedViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTopLisntenedBinding.inflate(inflater, parent, false)
        return SearchListenedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchListenedViewHolder, position: Int) {
        val music = musics[position]
        holder.binding.tvTopListenedSingerSong.text = music.artistName
        holder.binding.tvTopListenedNameSong.text = music.name
        holder.binding.imgMoreTopListened.setOnClickListener {
            onClickMusic(musics[position])
        }
        Glide.with(holder.itemView.context).load(music.image).error(R.drawable.demo_img_download)
            .into(holder.binding.imgItemListened)
    }

    override fun getItemCount(): Int {
        return musics.size
    }
//    fun getFilter(): Filter? {
//        return object : Filter() {
//            override fun performFiltering(charSequence: CharSequence): FilterResults {
//                val srcSearch = charSequence.toString()
//                if (srcSearch.isEmpty()) {
//                    musics = musicsold
//                } else {
//                    val list: MutableList<Music> = ArrayList<Music>()
//                    for (store in musicsold) {
//                        if (store.name.toLowerCase()
//                                .contains(srcSearch.lowercase(Locale.getDefault()))
//                        ) {
//                            list.add(store)
//                        }
//                    }
//                    musics = list
//                }
//                val filterResults = FilterResults()
//                filterResults.values = musics
//                return filterResults
//            }
//
//            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
//                musics = filterResults.values as List<Music?>
//                notifyDataSetChanged()
//            }
//        }
//    }
}

class SearchListenedViewHolder(val binding: ItemTopLisntenedBinding) :
    RecyclerView.ViewHolder(binding.root) {
}

