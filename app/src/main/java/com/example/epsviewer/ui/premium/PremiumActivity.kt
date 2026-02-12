package com.example.epsviewer.ui.premium

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.epsviewer.R
import com.example.epsviewer.databinding.ActivityPremiumBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Premium subscription plans activity
 */
@AndroidEntryPoint
class PremiumActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPremiumBinding
    private val viewModel: PremiumViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPremiumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Go Premium"

        setupUI()
        observeState()
    }

    private fun setupUI() {
        binding.apply {
            monthlyButton.setOnClickListener { viewModel.purchaseMonthly() }
            annualButton.setOnClickListener { viewModel.purchaseAnnual() }
            lifetimeButton.setOnClickListener { viewModel.purchaseLifetime() }
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.apply {
                        progressBar.visibility = if (state.isLoading) android.view.View.VISIBLE else android.view.View.GONE

                        state.error?.let {
                            Snackbar.make(root, it, Snackbar.LENGTH_LONG).show()
                            viewModel.clearError()
                        }

                        if (state.purchaseSuccess) {
                            Snackbar.make(root, "Purchase successful!", Snackbar.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

