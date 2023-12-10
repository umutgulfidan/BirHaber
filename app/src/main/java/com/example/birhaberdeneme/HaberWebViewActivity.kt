package com.example.birhaberdeneme

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.birhaberdeneme.databinding.ActivityHaberWebViewBinding
import com.example.birhaberdeneme.databinding.FragmentNewsResultBinding

class HaberWebViewActivity : AppCompatActivity() {
    lateinit var binding: ActivityHaberWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHaberWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val newsUrl = intent.getStringExtra("newsUrl")

        if(newsUrl != null){
            binding.webView.webViewClient = WebViewClient()
            binding.webView.loadUrl(newsUrl)
        }

    }
}