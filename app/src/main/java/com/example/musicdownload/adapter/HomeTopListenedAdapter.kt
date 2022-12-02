package com.example.musicdownload.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicdownload.R
import com.example.musicdownload.data.model.Music
import com.example.musicdownload.databinding.ItemTopLisntenedBinding
import com.example.musicdownload.view.fragment.HomeFragment
import com.example.musicdownload.view.fragment.SearchFragment
import java.util.*


class HomeTopListenedAdapter() :
    RecyclerView.Adapter<HomeFragmentTopListenedViewHolder>() {
    private var musics: List<Music> = listOf()
    private var musicsFilter: List<Music> = listOf()
    private lateinit var context: Context
    fun setMovieList(movies: List<Music>, context: Context) {
        this.musics = movies.toMutableList()
        this.musicsFilter = musics
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
    ): HomeFragmentTopListenedViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTopLisntenedBinding.inflate(inflater, parent, false)
        return HomeFragmentTopListenedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeFragmentTopListenedViewHolder, position: Int) {
        val music = musics[position]
        HomeFragment.positionMusic = position
        holder.binding.tvTopListenedSingerSong.text = music.artistName
        holder.binding.tvTopListenedNameSong.text = music.name
        holder.binding.imgMoreTopListened.setOnClickListener {
            onClickMusic?.let{
                it(musics[position])
            }
        }
        holder.binding.layoutTopListened.setOnClickListener{
            onClickPlayMusic?.let{
                it(position)
            }
        }
        Glide.with(holder.itemView.context).load(music.image).error(R.drawable.demo_img_download)
            .into(holder.binding.imgItemListened)
    }

    override fun getItemCount(): Int {
        return musics.size
    }

    fun getFilter(): Filter? {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val srcSearch = charSequence.toString()
                if (srcSearch.isEmpty()) {
                    musics = musicsFilter
                } else {
                    val list: MutableList<Music> = ArrayList<Music>()
                    for (store in musicsFilter) {
                        if (store.name.toLowerCase()
                                .contains(srcSearch.lowercase(Locale.getDefault()))
                        ) {
                            list.add(store)
                        }
                    }
                    musics = list
                }
                val filterResults = FilterResults()
                filterResults.values = musics
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                musics = filterResults.values as List<Music>
                if (musics.isEmpty()){
                    SearchFragment.binding.imgNomatchSearch.visibility = View.VISIBLE
                }
                else {
                    SearchFragment.binding.imgNomatchSearch.visibility = View.GONE
                }
                notifyDataSetChanged()
            }
        }
    }
}

class HomeFragmentTopListenedViewHolder(val binding: ItemTopLisntenedBinding) :
    RecyclerView.ViewHolder(binding.root) {
}