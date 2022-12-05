package com.example.musicdownload.view.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
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
import com.example.musicdownload.databinding.FragmentHomeBinding
import com.example.musicdownload.network.RetrofitService
import com.example.musicdownload.viewmodel.HomeFragmentViewModel
import com.example.musicdownload.viewmodel.MyViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.xuandq.radiofm.data.base.BaseFragment
import kotlinx.coroutines.Runnable

class HomeFragment : BaseFragment() {
    companion object {
        lateinit var listMusicHome: ArrayList<Music>
        var positionMusic : Int = 0
        lateinit var binding: FragmentHomeBinding
    }


    lateinit var viewModel: HomeFragmentViewModel
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
        viewModel = ViewModelProvider(this, MyViewModelFactory(MusicRepository(retrofitService)))[HomeFragmentViewModel::class.java]
        listMusicHome = ArrayList()
        setCheckInternet()
        binding.btnReload.setOnClickListener {
            setCheckInternet()
        }
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
        binding.btnGoToDownloadedSong.setOnClickListener {
            Toast.makeText(requireContext(),"lala",Toast.LENGTH_SHORT).show()
            val action = HomeFragmentDirections.actionHomeFragmentToDownloadManagerFragment()
            findNavController().navigate(action)
        }
    }

    private fun addData() {
        setRecHomeDownload()
        setRecHomeRanking()
        setRecTopListened()
        setTopDownloadHome()
        setGenersloadHome()

    }

    private fun setRecTopListened() {
        val intent = Intent(activity, PlayActivity::class.java)

        // set adapter
        binding.recToplistened.adapter = topListenedAdapter

        // set recycleView
        val linearLayoutManager = LinearLayoutManager(requireContext() )
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.recToplistened.layoutManager = linearLayoutManager
        //

        viewModel.responseListenedToplistenedHome.observe(viewLifecycleOwner) {
            topListenedAdapter.setMovieList(it.take(5), requireContext())
        }
        viewModel.getToplistenedHome()
        viewModel.errorMessage.observe(viewLifecycleOwner) {}

        topListenedAdapter.setClickShowMusic {
            showBottomSheetMusic(it)
        }
        topListenedAdapter.setClickPlayMusic {
            listMusicHome.clear()
            PlayActivity.isPlaying = false
            intent.putExtra("index", it)
            intent.putExtra("MainActivitySong", "HomeFragment")
            viewModel.responseListenedToplistenedHome.observe(viewLifecycleOwner) {
                listMusicHome.addAll(it)
            }
            startActivity(intent)
        }
    }

    private fun setRecHomeDownload() {
        // set adapter
        val intent = Intent(activity, PlayActivity::class.java)
        binding.viewPager2.adapter = downloadAdapter
        binding.viewPager2.offscreenPageLimit = 2
        binding.viewPager2.clipToPadding = false
        binding.viewPager2.clipChildren = false
        binding.viewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        setUpTransformer()
        // set recycleView

        viewModel.responseListenedDownLoadHome.observe(viewLifecycleOwner) {
            downloadAdapter.setMovieList(it, requireContext())
            runnable = Runnable {
                if (binding.viewPager2.currentItem == it.size - 1) {
                    binding.viewPager2.currentItem = 0
                } else {
                    binding.viewPager2.currentItem = binding.viewPager2.currentItem + 1
                }
            }
        }
        viewModel.getDownloadHome()
        viewModel.errorMessage.observe(viewLifecycleOwner) {}
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
            viewModel.responseListenedDownLoadHome.observe(viewLifecycleOwner) { listMusic ->
                listMusicHome.addAll(listMusic)
            }
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
        viewModel.responseListenedRankingHome.observe(viewLifecycleOwner) {
            rankingAdapter.setMovieList(it.take(5), requireContext())
        }
        binding.recItemRanking.adapter = rankingAdapter
        // T
        viewModel.errorMessage.observe(viewLifecycleOwner) {}
        viewModel.getRankingHome()

        rankingAdapter.setClickPlayMusic {
            listMusicHome.clear()
            PlayActivity.isPlaying = false
            intent.putExtra("index", it)
            intent.putExtra("MainActivitySong", "HomeFragment")
            viewModel.responseListenedRankingHome.observe(viewLifecycleOwner) { listMusic ->
                listMusicHome.addAll(listMusic)

            }
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
        viewModel.responseListenedTopDownLoadHome.observe(viewLifecycleOwner) {
            topDownloadAdapter.setMovieList(it.take(10), requireContext())
        }
        viewModel.errorMessage.observe(viewLifecycleOwner) {}
        viewModel.getTopDownLoadHome()
        topDownloadAdapter.setClickShowMusic {
            showBottomSheetMusic(it)
        }
        topDownloadAdapter.setClickPlayMusic {
            listMusicHome.clear()
            PlayActivity.isPlaying = false
            intent.putExtra("index", it)
            intent.putExtra("MainActivitySong", "HomeFragment")
            viewModel.responseListenedTopDownLoadHome.observe(viewLifecycleOwner) { listMusic ->
                listMusicHome.addAll(listMusic)
            }
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
        viewModel.responseGenre.observe(viewLifecycleOwner, Observer {
            genresAdapter.setGenresList(it.take(5), requireContext())
            binding.shimmerLayout.visibility = GONE
            binding.scrollView2.visibility = VISIBLE
        })
        viewModel.errorMessage.observe(viewLifecycleOwner) {}
        viewModel.getGenresHome()
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