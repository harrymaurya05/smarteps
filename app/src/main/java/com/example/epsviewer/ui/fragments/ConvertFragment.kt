package com.example.epsviewer.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.epsviewer.databinding.FragmentConvertBinding
import com.example.epsviewer.ui.home.HomeViewModel
import com.example.epsviewer.ui.viewer.ViewerActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class ConvertFragment : Fragment() {

    private var _binding: FragmentConvertBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConvertBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        binding.selectFileCard.setOnClickListener {
            openFilePicker()
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/postscript", "image/eps"))
        }
        startActivityForResult(intent, REQUEST_CODE_OPEN_FILE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_OPEN_FILE && resultCode == android.app.Activity.RESULT_OK) {
            data?.data?.let { uri ->
                // Save to recent files
                lifecycleScope.launch {
                    try {
                        val fileName = getFileName(uri)
                        val fileSize = getFileSize(uri)
                        val recentFile = com.example.epsviewer.data.model.RecentFile(
                            uri = uri.toString(),
                            fileName = fileName,
                            filePath = uri.toString(),
                            fileSize = fileSize,
                            lastOpenedTime = System.currentTimeMillis()
                        )
                        viewModel.addRecentFile(recentFile)
                    } catch (e: Exception) {
                        Timber.e(e, "Error saving recent file")
                    }
                }

                // Open viewer
                val intent = Intent(requireContext(), ViewerActivity::class.java).apply {
                    putExtra(ViewerActivity.EXTRA_FILE_URI, uri.toString())
                }
                startActivity(intent)
            }
        }
    }

    private fun getFileName(uri: android.net.Uri): String {
        var name = "Unknown"
        requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    name = cursor.getString(nameIndex)
                }
            }
        }
        return name
    }

    private fun getFileSize(uri: android.net.Uri): Long {
        var size = 0L
        requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                if (sizeIndex >= 0) {
                    size = cursor.getLong(sizeIndex)
                }
            }
        }
        return size
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val REQUEST_CODE_OPEN_FILE = 1001
    }
}

