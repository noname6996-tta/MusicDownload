package com.example.musicdownload.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicRanking.adapter.HomeMoreAdapter
import com.example.musicdownload.adapter.HomeMoreGenresAdapter
import com.example.musicdownload.data.model.Music
import com.example.musicdownload.data.repository.MusicRepository
import com.example.musicdownload.databinding.FragmentMoreBinding
import com.example.musicdownload.network.RetrofitService
import com.example.musicdownload.view.activity.PlayActivity
import com.example.musicdownload.viewmodel.HomeFragmentViewModel
import com.example.musicdownload.viewmodel.MyViewModelFactory
import com.xuandq.radiofm.data.base.BaseFragment

class MoreFragment(): BaseFragment() {
    lateinit var listMusicMore: ArrayList<Music>
    private lateinit var _binding: FragmentMoreBinding
    lateinit var viewModel: HomeFragmentViewModel
    private val retrofitService = RetrofitService.getInstance()
    private val binding get() = _binding
    var i:Int? = null

    val args:MoreFragmentArgs by navArgs()

    val homeMoreAdapter = HomeMoreAdapter{
            music ->
        showBottomSheetMusic(music)
    }
    val moreAdapter = HomeMoreGenresAdapter { genre ->
        //go to genre GenreFragment(genre)
        val action = MoreFragmentDirections.actionMoreFragmentToGenresMusicListFragment(genre.name,genre.image,genre.keySearch)
        findNavController().navigate(action)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoreBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        i = args.indexOF
        viewModel = ViewModelProvider(requireActivity(), MyViewModelFactory(MusicRepository(retrofitService)))[HomeFragmentViewModel::class.java]
        addData()
        binding.imgBackMoreLayout.setOnClickListener{
              findNavController().popBackStack()
        }
    }


    private fun addDataForGenres() {
        binding.recMore.adapter = moreAdapter
        binding.recMore.layoutManager =
            GridLayoutManager(requireContext(), 3, GridLayoutManager.VERTICAL, false)
        viewModel.responseGenre.observe(viewLifecycleOwner) {
            moreAdapter.setGenresList(it, requireContext())
        }
        viewModel.getGenresHome()
        binding.tvTitleMore.text = "More Genres"
    }

    private fun addData() {
        listMusicMore = ArrayList()
        binding.recMore.adapter = homeMoreAdapter
        // set recycleView layout
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.recMore.layoutManager = linearLayoutManager
        // add data to recycle

        when (i){
            1 -> {
                viewModel.responseListenedRankingHome.observe(viewLifecycleOwner) {
                    homeMoreAdapter.setMovieList(it, requireContext())
                    listMusicMore.addAll(it)
                }
                viewModel.errorMessage.observe(viewLifecycleOwner){}
                viewModel.getRankingHome()
                binding.tvTitleMore.text = "Top Ranking"
            }
            2 -> {
                viewModel.responseListenedToplistenedHome.observe(viewLifecycleOwner) {
                    homeMoreAdapter.setMovieList(it, requireContext())
                    listMusicMore.addAll(it)
                }
                viewModel.errorMessage.observe(viewLifecycleOwner){}
                viewModel.getToplistenedHome()
                binding.tvTitleMore.text = "Top listened"
            }
            3 -> {
                viewModel.responseListenedTopDownLoadHome.observe(viewLifecycleOwner) {
                    homeMoreAdapter.setMovieList(it, requireContext())
                    listMusicMore.addAll(it)
                }
                viewModel.errorMessage.observe(viewLifecycleOwner){}
                viewModel.getTopDownLoadHome()
                binding.tvTitleMore.text = "Top Download"
            }
            4 ->{
                addDataForGenres()
            }
        }

        homeMoreAdapter.setClickPlayMusic {
            HomeFragment.listMusicHome.clear()
            PlayActivity.isPlaying = false
            val intent = Intent(activity, PlayActivity::class.java)
            intent.putExtra("MainActivitySong", "HomeFragment")
            intent.putExtra("index", it)
            HomeFragment.listMusicHome.addAll(listMusicMore)
            startActivity(intent)
        }
    }
}
