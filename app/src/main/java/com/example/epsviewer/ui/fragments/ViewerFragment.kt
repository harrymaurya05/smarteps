package com.example.epsviewer.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.epsviewer.databinding.FragmentViewerBinding
import com.example.epsviewer.ui.home.HomeViewModel
import com.example.epsviewer.ui.viewer.ViewerActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ViewerFragment : Fragment() {

    private var _binding: FragmentViewerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        loadRecentFiles()
    }

    private fun setupUI() {
        // Refresh button
        binding.refreshButton.setOnClickListener {
            loadRecentFiles()
        }
    }

    private fun loadRecentFiles() {
        val recentFiles = viewModel.getRecentFiles()

        if (recentFiles.isEmpty()) {
            binding.emptyView.visibility = View.VISIBLE
            binding.recentFilesList.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.GONE
            binding.recentFilesList.visibility = View.VISIBLE

            // Display recent files
            val fileText = recentFiles.joinToString("\n\n") { file ->
                "${file.fileName}\n${formatFileSize(file.fileSize)}"
            }
            binding.recentFilesText.text = fileText

            // Set up click listeners
            binding.recentFilesList.setOnClickListener {
                showRecentFilesDialog(recentFiles)
            }
        }
    }

    private fun showRecentFilesDialog(recentFiles: List<com.example.epsviewer.data.model.RecentFile>) {
        val fileNames = recentFiles.map { "${it.fileName} (${formatFileSize(it.fileSize)})" }.toTypedArray()

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Select File")
            .setItems(fileNames) { _, which ->
                val selectedFile = recentFiles[which]
                // Open the selected file
                val intent = Intent(requireContext(), ViewerActivity::class.java).apply {
                    putExtra(ViewerActivity.EXTRA_FILE_URI, selectedFile.uri)
                }
                startActivity(intent)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> "${size / (1024 * 1024)} MB"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

