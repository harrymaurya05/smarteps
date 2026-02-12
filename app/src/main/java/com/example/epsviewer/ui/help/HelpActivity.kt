package com.example.epsviewer.ui.help

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.example.epsviewer.R

/**
 * Help activity showing usage instructions
 */
class HelpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Help"

        val webView = findViewById<WebView>(R.id.help_webview)
        webView.loadUrl("file:///android_asset/help.html")
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

