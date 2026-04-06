package com.padhleyrr.mppsc.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.padhleyrr.mppsc.ui.theme.gkkColors

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MapScreen() {
    val c = gkkColors
    var loading by remember { mutableStateOf(true) }
    var webView: WebView? by remember { mutableStateOf(null) }

    BackHandler(enabled = webView?.canGoBack() == true) {
        webView?.goBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.apply {
                        javaScriptEnabled      = true
                        domStorageEnabled      = true
                        loadWithOverviewMode   = true
                        useWideViewPort        = true
                        setSupportZoom(true)
                        builtInZoomControls    = true
                        displayZoomControls    = false
                        mixedContentMode       = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        cacheMode              = WebSettings.LOAD_DEFAULT
                        allowFileAccessFromFileURLs = true
                        allowUniversalAccessFromFileURLs = true
                    }
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            loading = false
                        }
                    }
                    webChromeClient = WebChromeClient()
                    loadUrl("file:///android_asset/maps.html")
                    webView = this
                }
            }
        )

        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color    = c.saff
            )
        }
    }
}
