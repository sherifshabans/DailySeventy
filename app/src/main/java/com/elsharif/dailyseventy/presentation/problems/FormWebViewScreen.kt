package com.elsharif.dailyseventy.presentation.problems

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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.elsharif.dailyseventy.presentation.components.DashboardScreenTopBar
import com.elsharif.dailyseventy.util.Screen

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun FormWebViewScreen(navController: NavController) {
    val formUrl =
        "https://docs.google.com/forms/d/e/1FAIpQLSe-GHN33J0Bw61tlxGQD3tY15fspUn2og16TEWchI4gkMhyFQ/viewform?usp=dialog"

    var isLoading by remember { mutableStateOf(true) }
    var webView: WebView? by remember { mutableStateOf(null) }

    Scaffold(
        topBar = {
            DashboardScreenTopBar(Screen.FeedbackScreen.titleRes, navController)

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
