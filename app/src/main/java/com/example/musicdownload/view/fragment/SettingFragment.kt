package com.example.musicdownload.view.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
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
            Toast.makeText(requireContext(), "Comming soon", Toast.LENGTH_SHORT).show()
        }
        binding.viewRattingApp.setOnClickListener {
            Toast.makeText(requireContext(), "Comming soon", Toast.LENGTH_SHORT).show()
        }
        binding.viewFeedback.setOnClickListener {
            composeEmail()
        }
        binding.viewPrivacyTerm.setOnClickListener {
            Toast.makeText(requireContext(), "Comming soon", Toast.LENGTH_SHORT).show()
        }
        binding.viewShareWithF.setOnClickListener {
            Toast.makeText(requireContext(), "Comming soon", Toast.LENGTH_SHORT).show()
        }
    }

    fun composeEmail() {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:") // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, "theanh682001@gmail.com")
        intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback MusicDownload")
        startActivity(intent)

    }
}