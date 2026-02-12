package com.example.epsviewer.ui.home

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.epsviewer.R
import com.example.epsviewer.databinding.ActivityHomeBinding
import com.example.epsviewer.ui.fragments.ConvertFragment
import com.example.epsviewer.ui.fragments.ProfileFragment
import com.example.epsviewer.ui.fragments.ViewerFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Home screen with bottom navigation bar for Convert, Viewer, and Profile tabs
 */
@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(ConvertFragment())
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_convert -> {
                    loadFragment(ConvertFragment())
                    true
                }
                R.id.nav_viewer -> {
                    loadFragment(ViewerFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}


