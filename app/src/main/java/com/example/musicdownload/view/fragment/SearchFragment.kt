package com.example.musicdownload.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicdownload.adapter.HomeTopListenedAdapter
import com.example.musicdownload.adapter.RecentlySearchAdapter
import com.example.musicdownload.data.model.Search
import com.example.musicdownload.data.repository.MusicRepository
import com.example.musicdownload.databinding.FragmentSearchBinding
import com.example.musicdownload.network.RetrofitService
import com.example.musicdownload.viewmodel.HomeFragmentViewModel
import com.example.musicdownload.viewmodel.MyViewModelFactory
import com.example.musicdownload.viewmodel.SearchViewModel
import com.xuandq.radiofm.data.base.BaseFragment

class SearchFragment : BaseFragment() {
    companion object {
        lateinit var binding: FragmentSearchBinding
    }

    private var a: String = ""
    private lateinit var searchViewModel: SearchViewModel
    lateinit var viewModel: HomeFragmentViewModel
    private val topListenedAdapter = HomeTopListenedAdapter()
    private val recentlySearchAdapter = RecentlySearchAdapter()
    private val retrofitService = RetrofitService.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            MyViewModelFactory(MusicRepository(retrofitService))
        )[HomeFragmentViewModel::class.java]
        searchViewModel = ViewModelProvider(this)[SearchViewModel::class.java]
        initUi()

    }

    private fun initUi() {

        binding.tvCancelSearch.setOnClickListener {
            findNavController().popBackStack()
            insertSearchToDatabase(a)
        }
        // set adapter
        binding.recListSearch.adapter = topListenedAdapter
        binding.recSearchRecently.adapter = recentlySearchAdapter
        // set recycleView
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.recListSearch.layoutManager = linearLayoutManager
        //
        val linearLayoutManager2 = LinearLayoutManager(requireContext())
        linearLayoutManager2.orientation = LinearLayoutManager.HORIZONTAL
        binding.recSearchRecently.layoutManager = linearLayoutManager2
        //
        viewModel.responseListenedToplistenedHome.observe(viewLifecycleOwner) {
            topListenedAdapter.setMovieList(it, requireContext())
        }
        viewModel.getToplistenedHome()
        //
        searchViewModel.readAllMusicData.observe(viewLifecycleOwner, Observer { search ->
            recentlySearchAdapter.setSearchList(search, requireContext())
            if (search.isEmpty()) {
                binding.textView2.visibility = View.GONE
                binding.recSearchRecently.visibility = View.GONE
            } else {
                binding.textView2.visibility = View.VISIBLE
                binding.recSearchRecently.visibility = View.VISIBLE
            }

        })
        //
        binding.edtSearchKey.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                topListenedAdapter.getFilter()?.filter(query)
                a = query.toString()
                return true
            }
        })
        topListenedAdapter.setClickShowMusic {
            showBottomSheetMusic(it)
            insertSearchToDatabase(a)
        }
        topListenedAdapter.setClickPlayMusic {
            val action = SearchFragmentDirections.actionSearchFragmentToPlayActivity(it)
            viewModel.responseListenedToplistenedHome.observe(viewLifecycleOwner) { listMusic ->
                HomeFragment.listMusicHome.addAll(listMusic)
                findNavController().navigate(action)
                insertSearchToDatabase(a)
            }
        }
        recentlySearchAdapter.setClickDeleteSearch {
            searchViewModel.deleteSearchlist(it)
        }
        recentlySearchAdapter.setClickSearch {
            binding.edtSearchKey.setQuery(it.name,true)
        }
    }

    private fun insertSearchToDatabase(name: String) {
        if (inputCheck(name)) {
            // Create play list
            val search = Search(0, name)
            // add Data to database
            searchViewModel.addSearchList(search)
        }
    }
}