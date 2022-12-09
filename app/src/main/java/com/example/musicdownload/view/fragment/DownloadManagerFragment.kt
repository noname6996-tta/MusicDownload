package com.example.musicdownload.view.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.musicdownload.R
import com.example.musicdownload.adapter.DownloadPagerAdapter
import com.example.musicdownload.databinding.FragmentDownloadManagerBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class DownloadManagerFragment : Fragment() {
    private lateinit var binding: FragmentDownloadManagerBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentDownloadManagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI(){
//        val fragmentTransaction: FragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction()
//        fragmentTransaction.add(R.id.viewPagerDownloadFragment, DownloadedFragment())
//        fragmentTransaction.commit()
//        binding.btnDemo1.setTextColor(requireContext().getColor(R.color.color_btnPlay))
//        binding.btnDemo1.setOnClickListener{
//            val fragmentTransaction: FragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction()
//            fragmentTransaction.replace(R.id.viewPagerDownloadFragment, DownloadedFragment())
//            fragmentTransaction.commit()
//            binding.btnDemo1.setTextColor(requireContext().getColor(R.color.color_btnPlay))
//            binding.btnDemo2.setTextColor(requireContext().getColor(R.color.white))
//            binding.viewSlideBtn1.visibility = View.VISIBLE
//            binding.viewSlideBtn2.visibility = View.GONE
//        }
//        binding.btnDemo2.setOnClickListener {
//            val fragmentTransaction: FragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction()
//            fragmentTransaction.replace(R.id.viewPagerDownloadFragment, DownloadingFragment())
//            fragmentTransaction.commit()
//            binding.btnDemo2.setTextColor(requireContext().getColor(R.color.color_btnPlay))
//            binding.btnDemo1.setTextColor(requireContext().getColor(R.color.white))
//            binding.viewSlideBtn2.visibility = View.VISIBLE
//            binding.viewSlideBtn1.visibility = View.GONE
//        }
        binding.viewPagerHomeGroup.adapter = DownloadPagerAdapter(requireActivity())
        TabLayoutMediator(binding.tabLayoutHomeGroup, binding.viewPagerHomeGroup,object : TabLayoutMediator.TabConfigurationStrategy{
            override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
                when(position){
                    0-> tab.text = "Downloaded"
                    1-> tab.text = "Downloading"
                }
            }

        }).attach()

    }
}