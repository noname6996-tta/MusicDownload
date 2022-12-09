package com.example.musicdownload.adapter

import android.content.res.Resources
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.musicdownload.view.fragment.DownloadedFragment
import com.example.musicdownload.view.fragment.DownloadingFragment

class DownloadPagerAdapter (fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity){
    override fun getItemCount()=2

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> DownloadedFragment()
            1 -> DownloadingFragment()
            // tam thoi
            else -> {
                throw Resources.NotFoundException("Not Found")
            }
        }
    }
}