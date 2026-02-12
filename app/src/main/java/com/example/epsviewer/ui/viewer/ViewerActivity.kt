package com.example.epsviewer.ui.viewer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.epsviewer.R
import com.example.epsviewer.databinding.ActivityViewerBinding
import com.example.epsviewer.domain.model.ExportFormat
import com.example.epsviewer.domain.model.PremiumTier
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Viewer activity for displaying and interacting with EPS files
 */
@AndroidEntryPoint
class ViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewerBinding
    private val viewModel: ViewerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "EPS Viewer"

        setupUI()
        observeState()

        // Load file from intent
        intent.getStringExtra(EXTRA_FILE_URI)?.let { uriString ->
            val uri = Uri.parse(uriString)
            viewModel.loadFile(uri)
        }
    }

    private fun setupUI() {
        binding.apply {
            zoomInButton.setOnClickListener {
                val currentZoom = viewModel.uiState.value.zoomLevel
                viewModel.setZoomLevel(currentZoom * 1.2f)
            }
            zoomOutButton.setOnClickListener {
                val currentZoom = viewModel.uiState.value.zoomLevel
                viewModel.setZoomLevel(currentZoom / 1.2f)
            }
            fitButton.setOnClickListener { viewModel.fitToScreen() }
            resetButton.setOnClickListener { viewModel.resetZoom() }
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.apply {
                        // Show loading with message
                        if (state.isLoading) {
                            loadingContainer.visibility = android.view.View.VISIBLE
                            loadingText.text = when {
                                state.loadingMessage != null -> state.loadingMessage
                                else -> getString(R.string.loading_eps)
                            }
                        } else {
                            loadingContainer.visibility = android.view.View.GONE
                        }

                        // Show preview
                        state.preview?.let { previewImage.setImageBitmap(it) }

                        // Handle errors
                        state.error?.let {
                            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                            viewModel.clearError()
                        }

                        // Show export progress
                        if (state.isExporting) {
                            exportProgressBar.visibility = android.view.View.VISIBLE
                            exportProgressBar.progress = state.exportProgress
                        } else {
                            exportProgressBar.visibility = android.view.View.GONE
                        }
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_viewer, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        R.id.action_export -> {
            showExportDialog()
            true
        }
        R.id.action_file_info -> {
            showFileInfo()
            true
        }
        R.id.action_open_in -> {
            openInOtherApp()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun showExportDialog() {
        val state = viewModel.uiState.value
        val tier = state.premiumTier

        val formats = if (tier == PremiumTier.FREE) {
            arrayOf("PNG (Standard)", "JPG (Standard)")
        } else {
            arrayOf("PNG (High-Res)", "JPG (High-Res)", "PDF (High-Res)")
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Export EPS")
            .setItems(formats) { _, which ->
                val format = when (which) {
                    0 -> ExportFormat.PNG
                    1 -> ExportFormat.JPG
                    2 -> ExportFormat.PDF
                    else -> ExportFormat.PNG
                }
                val dpi = if (tier == PremiumTier.FREE) 150 else 300
                launchExport(format, dpi)
            }
            .show()
    }

    private fun launchExport(format: ExportFormat, dpi: Int) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = format.mimeType
            putExtra(Intent.EXTRA_TITLE, "exported_eps.${format.extension}")
        }
        startActivityForResult(intent, REQUEST_CODE_EXPORT)
    }

    private fun showFileInfo() {
        val metadata = viewModel.uiState.value.metadata
        if (metadata != null) {
            val info = """
                File: ${metadata.fileName}
                Size: ${metadata.fileSize / 1024} KB
                Dimensions: ${metadata.width} x ${metadata.height} px
            """.trimIndent()

            MaterialAlertDialogBuilder(this)
                .setTitle("File Information")
                .setMessage(info)
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun openInOtherApp() {
        val uri = viewModel.uiState.value.fileUri
        if (uri != null) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/postscript")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Snackbar.make(binding.root, "No app available", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_EXPORT && resultCode == RESULT_OK) {
            data?.data?.let { outputUri ->
                viewModel.export(outputUri, ExportFormat.PNG, 150)
            }
        }
    }

    companion object {
        const val EXTRA_FILE_URI = "file_uri"
        private const val REQUEST_CODE_EXPORT = 2001
    }
}

