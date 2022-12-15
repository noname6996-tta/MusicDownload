package com.example.musicdownload.view.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.musicRanking.adapter.HomeRankingAdapter
import com.example.musicdownload.adapter.HomeTopListenedAdapter
import com.example.musicdownload.R
import com.example.musicdownload.adapter.HomeDownloadAdapter
import com.example.musicdownload.adapter.HomeGenresAdapter
import com.example.musicdownload.adapter.HomeTopDownloadAdapter
import com.example.musicdownload.data.model.Music
import com.example.musicdownload.data.repository.MusicRepository
import com.example.musicdownload.data.repository.Repostitory
import com.example.musicdownload.databinding.FragmentHomeBinding
import com.example.musicdownload.network.RetrofitService
import com.example.musicdownload.view.activity.PlayActivity
import com.example.musicdownload.viewmodel.HomeFragmentViewModel
import com.example.musicdownload.viewmodel.MainViewModel
import com.example.musicdownload.viewmodel.MainViewModelFactory
import com.example.musicdownload.viewmodel.MyViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.xuandq.radiofm.data.base.DataFragment
import kotlinx.coroutines.Runnable

class HomeFragment : DataFragment() {
    companion object {
        lateinit var listMusicHome: ArrayList<Music>
        var positionMusic : Int = 0
        lateinit var binding: FragmentHomeBinding
    }

    private lateinit var viewModelMain: MainViewModel
    private val retrofitService = RetrofitService.getInstance()
    private val downloadAdapter = HomeDownloadAdapter()
    private val rankingAdapter = HomeRankingAdapter()
    private val handler = Handler()
    private lateinit var runnable: Runnable
    private val topListenedAdapter = HomeTopListenedAdapter()
    private val topDownloadAdapter = HomeTopDownloadAdapter()
    private val genresAdapter = HomeGenresAdapter { genre ->
        val action = HomeFragmentDirections.actionHomeFragmentToGenresMusicListFragment(genre.name,genre.image,genre.keySearch)
        findNavController().navigate(action)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val repository = Repostitory()
        val viewModelFactory = MainViewModelFactory(repository)
        viewModelMain = ViewModelProvider(this,viewModelFactory)[MainViewModel::class.java]
        listMusicHome = ArrayList()
        setCheckInternet()
        binding.btnReload.setOnClickListener {
            setCheckInternet()
        }
        binding.btnGoToDownloadedSong.setOnClickListener {
            var action = HomeFragmentDirections.actionHomeFragmentToDownloadedHomeFragment()
            findNavController().navigate(action)
        }
    }

    override fun onResume() {
        super.onResume()
        setCheckInternet()
    }


    private fun setCheckInternet(){
        // check Internet
        if (checkForInternet(requireContext())) {
            binding.layoutNotConnect.visibility = GONE
            binding.shimmerLayout.startShimmerAnimation()
            addData()
            onClickLayout()
        } else {
            binding.shimmerLayout.visibility = GONE
            binding.layoutNotConnect.visibility = VISIBLE
        }
        val handler = Handler()
        handler.postDelayed({
            if (binding.shimmerLayout.visibility == VISIBLE) {
                binding.shimmerLayout.visibility = GONE
                binding.layoutNotConnect.visibility = VISIBLE
            }
        }, 5000)
    }


    private fun onClickLayout() {
        binding.tvMoreTopRanking.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToMoreFragment(1)
            findNavController().navigate(action)
        }

        binding.imgSearchHome.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToSearchFragment()
            findNavController().navigate(action)
        }
        binding.tvMoreTopListened.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToMoreFragment(2)
            findNavController().navigate(action)
        }
        binding.tvMoreTopDownload.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToMoreFragment(3)
            findNavController().navigate(action)
        }
        binding.tvMoreGenres.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToMoreFragment(4)
            findNavController().navigate(action)
        }
        binding.viewChangeRegon.setOnClickListener {
            setBottomSheetRegion()
        }

    }

    private fun addData() {
        setRecHomeDownload()
        setRecHomeRanking()
        setRecTopListened()
        setTopDownloadHome()
        setGenersloadHome()
    }

    private fun setRecTopListened(){
        val intent = Intent(activity, PlayActivity::class.java)

        // set adapter
        binding.recToplistened.adapter = topListenedAdapter

        // set recycleView
        val linearLayoutManager = LinearLayoutManager(requireContext() )
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.recToplistened.layoutManager = linearLayoutManager
        //
        viewModelMain.getTopListenedHomeCrotines()
        viewModelMain.TopListenedHome.observe(viewLifecycleOwner, Observer {
            topListenedAdapter.setMovieList(it.take(5), requireContext())
        })
        topListenedAdapter.setClickShowMusic {
            showBottomSheetMusic(it)
        }
        topListenedAdapter.setClickPlayMusic {
            if (checkForInternet(requireContext())) {
                listMusicHome.clear()
                PlayActivity.isPlaying = false
                intent.putExtra("index", it)
                intent.putExtra("MainActivitySong", "HomeFragment")
                var listMusicHomeTopListened: List<Music> = viewModelMain.TopListenedHome.value!!
                listMusicHome.addAll(listMusicHomeTopListened)
                startActivity(intent)
            }
        }
    }

    private fun setRecHomeDownload() {
        val intent = Intent(activity, PlayActivity::class.java)
        binding.viewPager2.adapter = downloadAdapter
        binding.viewPager2.offscreenPageLimit = 2
        binding.viewPager2.clipToPadding = false
        binding.viewPager2.clipChildren = false
        binding.viewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        setUpTransformer()
        // set recycleView
        viewModelMain.getDownloadHome()
        viewModelMain.DownloadHome.observe(viewLifecycleOwner) {
            downloadAdapter.setMovieList(it, requireContext())
            runnable = Runnable {
                if (binding.viewPager2.currentItem == it.size - 1) {
                    binding.viewPager2.currentItem = 0
                } else {
                    binding.viewPager2.currentItem = binding.viewPager2.currentItem + 1
                }
            }
        }

        binding.viewPager2.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, 3000)
            }
        })
        downloadAdapter.setClickPlayMusic {
            listMusicHome.clear()
            PlayActivity.isPlaying = false
            intent.putExtra("index", it)
            intent.putExtra("MainActivitySong", "HomeFragment")
            var listMusicHomeDownload: List<Music> = viewModelMain.DownloadHome.value!!
            listMusicHome.addAll(listMusicHomeDownload)
            startActivity(intent)
        }
    }

    private fun setUpTransformer() {
        val transformer = CompositePageTransformer()
        transformer.addTransformer(MarginPageTransformer(40))
        transformer.addTransformer { page, position ->
            val r = 1 - kotlin.math.abs(position)
            page.scaleY = 0.85f + r * 0.14f
            page.scaleX = 0.85f + r * 0.14f
        }
        binding.viewPager2.setPageTransformer(transformer)
    }

    private fun setRecHomeRanking() {
        val intent = Intent(activity, PlayActivity::class.java)
        // set recycleView layout
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        binding.recItemRanking.layoutManager = linearLayoutManager
        // add data to recycle
        viewModelMain.RankingHome.observe(viewLifecycleOwner) {
            rankingAdapter.setMovieList(it.take(5), requireContext())
        }
        binding.recItemRanking.adapter = rankingAdapter
        // T
        viewModelMain.getRanking()

        rankingAdapter.setClickPlayMusic {
            listMusicHome.clear()
            PlayActivity.isPlaying = false
            intent.putExtra("index", it)
            intent.putExtra("MainActivitySong", "HomeFragment")
            var listMusicHomeDownload: List<Music> = viewModelMain.RankingHome.value!!
            listMusicHome.addAll(listMusicHomeDownload)
            startActivity(intent)
        }
    }

    private fun setTopDownloadHome() {
        val intent = Intent(activity, PlayActivity::class.java)
        // set adapter
        binding.recTopDownload.adapter = topDownloadAdapter
        // set recycleView layout
        binding.recTopDownload.layoutManager =
            GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)
        //
        viewModelMain.TopDownload.observe(viewLifecycleOwner) {
            topDownloadAdapter.setMovieList(it.take(10), requireContext())
        }
        viewModelMain.getTopDownload()
        topDownloadAdapter.setClickShowMusic {
            showBottomSheetMusic(it)
        }
        topDownloadAdapter.setClickPlayMusic {
            listMusicHome.clear()
            PlayActivity.isPlaying = false
            intent.putExtra("index", it)
            intent.putExtra("MainActivitySong", "HomeFragment")
            var listMusicHomeDownload: List<Music> = viewModelMain.TopDownload.value!!
            listMusicHome.addAll(listMusicHomeDownload)
            startActivity(intent)
        }
    }

    private fun setGenersloadHome() {
        // set adapter
        binding.recGenres.adapter = genresAdapter
        // set recycleView layout
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        binding.recGenres.layoutManager = linearLayoutManager
        //
        viewModelMain.GenresList.observe(viewLifecycleOwner, Observer {
            genresAdapter.setGenresList(it.take(5), requireContext())
            binding.shimmerLayout.visibility = GONE
            binding.scrollView2.visibility = VISIBLE
        })
        viewModelMain.getGenres()
    }

    private fun setBottomSheetRegion() {
        val bottomSheetDialog = BottomSheetDialog(this.requireContext());
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_change_region);
        bottomSheetDialog.show()
        val view7Region: View = bottomSheetDialog.findViewById<View>(R.id.view7Region)!!
        view7Region.setOnClickListener {
            binding.tvRegionHome.text = "VN"
            bottomSheetDialog.dismiss()
        }
        val view8Region: View = bottomSheetDialog.findViewById<View>(R.id.view8Region)!!
        view8Region.setOnClickListener {
            binding.tvRegionHome.text = "Eng"
            bottomSheetDialog.dismiss()
        }
    }
}