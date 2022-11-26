package com.example.musicdownload.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.musicdownload.R
import com.example.musicdownload.databinding.FragmentHomeBinding
import com.example.musicdownload.databinding.FragmentSettingBinding

class SettingFragment : Fragment() {
    private lateinit var binding: FragmentSettingBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnUpdateNow.setOnClickListener {
            Toast.makeText(requireContext(),"Not now bro",Toast.LENGTH_SHORT).show()
        }
        binding.viewRattingApp.setOnClickListener {
            Toast.makeText(requireContext(),"Not now bro",Toast.LENGTH_SHORT).show()
        }
        binding.viewFeedback.setOnClickListener {
            Toast.makeText(requireContext(),"Not now bro",Toast.LENGTH_SHORT).show()
        }
        binding.viewPrivacyTerm.setOnClickListener {
            Toast.makeText(requireContext(),"Not now bro",Toast.LENGTH_SHORT).show()
        }
        binding.viewShareWithF.setOnClickListener {
            Toast.makeText(requireContext(),"Not now bro",Toast.LENGTH_SHORT).show()
        }
    }
}