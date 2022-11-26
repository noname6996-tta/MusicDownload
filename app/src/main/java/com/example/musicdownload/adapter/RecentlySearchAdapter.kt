package com.example.musicdownload.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicdownload.R
import com.example.musicdownload.data.model.Music
import com.example.musicdownload.data.model.PlayList
import com.example.musicdownload.data.model.Search
import com.example.musicdownload.databinding.ItemCreatePlaylistBinding
import com.example.musicdownload.databinding.ItemRecentlySearchBinding


class RecentlySearchAdapter : RecyclerView.Adapter<RecentlySearchAdapterViewHolder>() {
    private var searchs: List<Search> = listOf()
    private lateinit var context: Context
    fun setSearchList(searchs: List<Search>, context: Context) {
        this.searchs = searchs.toMutableList()
        this.context = context
        notifyDataSetChanged()
    }
    private var onClickDeleteSearch: ((Search) -> Unit)? = null
    fun setClickDeleteSearch(listener: ((Search) -> Unit)) {
        onClickDeleteSearch = listener
    }
    private var onClickSearch: ((Search) -> Unit)? = null
    fun setClickSearch(listener: ((Search) -> Unit)) {
        onClickSearch = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecentlySearchAdapterViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemRecentlySearchBinding.inflate(inflater, parent, false)
        return RecentlySearchAdapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentlySearchAdapterViewHolder, position: Int) {
        val search = searchs[position]
        holder.binding.tvHistorySearch.text = search.name
        holder.binding.imgDeleteHistory.setOnClickListener {
            onClickDeleteSearch?.let{
                it(searchs[position])
            }
        }
        holder.binding.tvHistorySearch.setOnClickListener{
            onClickSearch?.let{
                it(searchs[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return searchs.size
    }
}

class RecentlySearchAdapterViewHolder(val binding: ItemRecentlySearchBinding) :
    RecyclerView.ViewHolder(binding.root) {
}