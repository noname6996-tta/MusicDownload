package com.example.musicdownload.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.musicdownload.adapter.HomeTopListenedAdapter
import com.example.musicdownload.R
import com.example.musicdownload.adapter.HomeMoreGenresAdapter
import com.example.musicdownload.data.model.Music
import com.example.musicdownload.data.repository.MusicRepository
import com.example.musicdownload.databinding.FragmentMusicGenresBinding
import com.example.musicdownload.network.RetrofitService
import com.example.musicdownload.view.activity.PlayActivity
import com.example.musicdownload.viewmodel.HomeFragmentViewModel
import com.example.musicdownload.viewmodel.MyViewModelFactory
import com.xuandq.radiofm.data.base.DataFragment

class GenresMusicListFragment(): DataFragment() {
    lateinit var viewModel: HomeFragmentViewModel
    val topListenedAdapter = HomeTopListenedAdapter()
    val args : GenresMusicListFragmentArgs by navArgs()
    lateinit var listMusicGenres : List<Music>
    private lateinit var _binding : FragmentMusicGenresBinding
    private val retrofitService = RetrofitService.getInstance()
    private val binding get() = _binding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMusicGenresBinding.inflate(inflater, container, false)
        return binding.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            MyViewModelFactory(MusicRepository(retrofitService))
        )[HomeFragmentViewModel::class.java]
        setLayout()
        setRecMusicGenres()
    }

    private fun setLayout() {
        Glide.with(requireContext()).load(HomeMoreGenresAdapter.URL_GET_IMAGE+args.genresImage).error(R.drawable.demo_img_download)
            .into(binding.imgGenresMusic)
        binding.tvNameListMusic.text = args.genresName
        binding.imgBackMusicGenres.setOnClickListener{
            findNavController().popBackStack()
        }
        binding.btnPlayMusicGenres.setOnClickListener {
            val intent = Intent(activity, PlayActivity::class.java)
            HomeFragment.listMusicHome.clear()
            PlayActivity.isPlaying = false
            intent.putExtra("index", 0)
            intent.putExtra("MainActivitySong", "GenresMusicListFragment")
            HomeFragment.listMusicHome.addAll(listMusicGenres)
            startActivity(intent)
        }
    }

    private fun setRecMusicGenres() {
        // set adapter
        binding.recMusicGenres.adapter = topListenedAdapter
        topListenedAdapter.setClickShowMusic { showBottomSheetMusic(it) }
        // set recycleView
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.recMusicGenres.layoutManager = linearLayoutManager
        //
        viewModel.responseListenedMusicByGenres.observe(viewLifecycleOwner) {
            listMusicGenres = ArrayList<Music>()
            listMusicGenres = it
            topListenedAdapter.setMovieList(it, requireContext())
        }
        viewModel.errorMessage.observe(viewLifecycleOwner) {}
        viewModel.getMusicByGenres(args.genresKeySearch)

        topListenedAdapter.setClickPlayMusic {
            val intent = Intent(activity, PlayActivity::class.java)
            HomeFragment.listMusicHome.clear()
            PlayActivity.isPlaying = false
            intent.putExtra("index", it)
            intent.putExtra("MainActivitySong", "GenresMusicListFragment")
            HomeFragment.listMusicHome.addAll(listMusicGenres)
            startActivity(intent)
        }
    }
}