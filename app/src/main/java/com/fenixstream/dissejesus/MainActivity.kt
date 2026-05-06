package com.fenixstream.dissejesus

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { MainScreen() } }
    }
}

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableIntStateOf(1) }
    // URL fornecida pelo usuário
    val targetUrl = "Https://youtube.com/@dissejesusoficial"

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Text("📻") }, 
                    label = { Text("Rádio") }, 
                    selected = selectedTab == 0, 
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Text("📺") }, 
                    label = { Text("Vídeos") }, 
                    selected = selectedTab == 1, 
                    onClick = { selectedTab = 1 }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            Crossfade(targetState = selectedTab, label = "Tab") { tab ->
                when (tab) {
                    0 -> Box(Modifier.fillMaxSize()) { Text("Painel Rádio em desenvolvimento", modifier = Modifier.padding(16.dp)) }
                    1 -> YouTubeScreen(url = targetUrl)
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeScreen(url: String) {
    AndroidView(factory = { context ->
        WebView(context).apply {
            layoutParams = android.view.ViewGroup.LayoutParams(-1, -1)
            
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                mediaPlaybackRequiresUserGesture = false
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            }

            webViewClient = object : WebViewClient() {
                // Bloqueio de anúncios via Injeção de JS
                override fun onPageFinished(view: WebView?, url: String?) {
                    val script = """
                        (function() {
                            var ads = document.querySelectorAll('.ad-container, .ad-interrupting, .ytp-ad-overlay-container, .promoted-sparkles-text-search-root');
                            for (var i = 0; i < ads.length; i++) { ads[i].remove(); }
                            var video = document.querySelector('video');
                            if (video && document.querySelector('.ytp-ad-skip-button')) {
                                video.currentTime = video.duration;
                                document.querySelector('.ytp-ad-skip-button').click();
                            }
                        })();
                    """.trimIndent()
                    view?.evaluateJavascript(script, null)
                }

                // Restrição de Navegação (Somente permite o canal/URL base)
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val newUrl = request?.url.toString()
                    return if (newUrl.contains("youtube.com") && !newUrl.contains("googleusercontent.com/youtube.com/3")) {
                        true
                    } else {
                        false
                    }
                }
            }
            
            webChromeClient = WebChromeClient()
            loadUrl(url)
        }
    }, update = { it.loadUrl(url) })
}
