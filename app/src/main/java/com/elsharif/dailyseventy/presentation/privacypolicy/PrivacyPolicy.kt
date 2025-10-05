package com.elsharif.dailyseventy.presentation.privacypolicy

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.elsharif.dailyseventy.presentation.components.DashboardScreenTopBar
import com.elsharif.dailyseventy.util.Screen

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PrivacyPolicy(navController: NavController) {
    val formUrl =
        "https://www.freeprivacypolicy.com/live/113d7cd1-6c4a-4e80-97a3-c7dfc7fef64f"

    var isLoading by remember { mutableStateOf(true) }
    var webView: WebView? by remember { mutableStateOf(null) }

    Scaffold(
        topBar = {
            DashboardScreenTopBar(Screen.PrivacyPolicyScreen.titleRes, navController)

        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                            }
                        }
                        webChromeClient = WebChromeClient()

                        loadUrl(formUrl)
                        webView = this
                    }
                }
            )

            if (isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
    }
}
