package com.example.epsviewer.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.epsviewer.databinding.FragmentProfileBinding
import com.example.epsviewer.ui.help.HelpActivity
import com.example.epsviewer.ui.premium.PremiumActivity
import com.example.epsviewer.ui.settings.SettingsActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        binding.apply {
            goPremiumButton.setOnClickListener {
                startActivity(Intent(requireContext(), PremiumActivity::class.java))
            }

            settingsButton.setOnClickListener {
                startActivity(Intent(requireContext(), SettingsActivity::class.java))
            }

            helpButton.setOnClickListener {
                startActivity(Intent(requireContext(), HelpActivity::class.java))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

