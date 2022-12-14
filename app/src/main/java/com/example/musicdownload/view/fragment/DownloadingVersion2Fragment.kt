package com.example.musicdownload.view.fragment

import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicdownload.adapter.DownloadingAdapter
import com.example.musicdownload.data.model.Music
import com.example.musicdownload.databinding.FragmentDownloadingVer2Binding
import com.example.musicdownload.view.activity.MainActivity
import com.proxglobal.worlcupapp.base.BaseFragment

class DownloadingVersion2Fragment : BaseFragment<FragmentDownloadingVer2Binding>() {
    private val adapter = DownloadingAdapter()

    override fun getDataBinding(): FragmentDownloadingVer2Binding {
        return FragmentDownloadingVer2Binding.inflate(layoutInflater)
    }


    override fun initView() {
        super.initView()
        Log.d("listDownloading",MainActivity.listDownloading.toString())
        binding.recDownloading.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.recDownloading.layoutManager = linearLayoutManager
    }

    override fun initData() {
        super.initData()
        adapter.setDataDownloading(MainActivity.listDownloading, requireContext())
    }

    override fun onResume() {
        super.onResume()
        adapter.setDataDownloading(MainActivity.listDownloading, requireContext())

    }
}