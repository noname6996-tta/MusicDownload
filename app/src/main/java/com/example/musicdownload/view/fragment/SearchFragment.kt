package com.example.musicdownload.view.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
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
import com.example.musicdownload.view.activity.PlayActivity
import com.example.musicdownload.viewmodel.HomeFragmentViewModel
import com.example.musicdownload.viewmodel.MyViewModelFactory
import com.example.musicdownload.viewmodel.SearchViewModel
import com.xuandq.radiofm.data.base.DataFragment

class SearchFragment : DataFragment() {
    companion object {
        lateinit var binding: FragmentSearchBinding
    }

    private var list = ArrayList<Search>()
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
        viewModel.responseListenedSearchMusic.observe(viewLifecycleOwner) {
            topListenedAdapter.setMovieList(it, requireContext())
        }
        getMusicSearch(a)
        initUi()
    }

    private fun initUi() {
        binding.tvCancelSearch.setOnClickListener {
            insertSearchToDatabase(a)
            findNavController().popBackStack()

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
        //
        topListenedAdapter.setClickShowMusic {
            showBottomSheetMusic(it)
        }
        recentlySearchAdapter.setClickDeleteSearch {
            searchViewModel.deleteSearchlist(it)
        }
        recentlySearchAdapter.setClickSearch {
            binding.searchView.setQuery(it.name, true)
        }

//        addEvent()
        addTextListener()
    }

    fun Activity.hideKeyboard() {
        if (currentFocus != null) {
            val inputMethodManager = getSystemService(
                Context
                    .INPUT_METHOD_SERVICE
            ) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }

    //bugggg
    private fun insertSearchToDatabase(name: String) {
        if (inputCheck(name)) {
            // Create play list
            val search = Search(name)
            // add Data to database
            searchViewModel.addSearchList(search)
        }
    }

    fun addTextListener() {
        searchViewModel.readAllMusicData.observe(viewLifecycleOwner, Observer { search ->
            recentlySearchAdapter.setSearchList(search, requireContext())
            list.addAll(search)
            if (search.isEmpty()) {
                binding.textView2.visibility = View.GONE
                binding.recSearchRecently.visibility = View.GONE
            } else {
                binding.textView2.visibility = View.VISIBLE
                binding.recSearchRecently.visibility = View.VISIBLE
            }

        })
//        binding.edtSearch.setOnFocusChangeListener { _, hasFocus ->
//            isFocusedSearch = hasFocus
//        }
//        binding.edtSearch.setOnEditorActionListener { _, i, _ ->
//            if (i == EditorInfo.IME_ACTION_SEARCH) {
//                requireActivity().hideKeyboard()
//                return@setOnEditorActionListener true
//            }
//            return@setOnEditorActionListener false
//        }

//        binding.edtSearch.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
//
//            override fun afterTextChanged(s: Editable) {
//                if (isFocusedSearch) {
//                    binding.btnDeleteSearch.visibility =
//                        if (s.isEmpty()) View.GONE else View.VISIBLE
//                }
//                getMusicSearch(s)
//            }
//        })

//        binding.edtSearch.addTextChangedListener(object: TextWatcher{
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                // Do Nothing
//                if (isFocusedSearch) {
//                    binding.btnDeleteSearch.visibility =
//                        if (s.toString().isEmpty()) View.GONE else View.VISIBLE
//                }
//                getMusicSearch(s.toString())
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//
//            }
//
//        })

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                getMusicSearch(query.toString())
                a = query.trim().toString()
                return false
            }
            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })

    }

    private fun getMusicSearch(search: CharSequence) {
        requireActivity().hideKeyboard()
        viewModel.errorMessage.observe(viewLifecycleOwner) {}
        viewModel.searchByString(search.toString().trim().lowercase())
        topListenedAdapter.setClickPlayMusic {
            insertSearchToDatabase(a)
            val intent = Intent(activity, PlayActivity::class.java)
            HomeFragment.listMusicHome.clear()
            PlayActivity.isPlaying = false
            HomeFragment.listMusicHome.addAll(viewModel.responseListenedSearchMusic.value!!)
            intent.putExtra("MainActivitySong", "HomeFragment")
            intent.putExtra("index", it)
            startActivity(intent)
        }
    }
}