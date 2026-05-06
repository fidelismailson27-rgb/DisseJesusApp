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
    // A URL oficial correta informada
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
    AndroidView(
        // CORREÇÃO CRÍTICA: Força a WebView a ocupar toda a tela disponível
        modifier = Modifier.fillMaxSize(), 
        factory = { context ->
            WebView(context).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
                
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                }

                webViewClient = object : WebViewClient() {
                    // Restrição de Navegação Segura
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        val newUrl = request?.url.toString()
                        // Permite apenas navegação dentro do domínio do YouTube
                        return if (newUrl.contains("youtube.com") || newUrl.contains("youtu.be")) {
                            false // Permite o carregamento
                        } else {
                            true // Bloqueia saídas para sites externos (proteção)
                        }
                    }

                    // Injeção de JS para remover containers de anúncios
                    override fun onPageFinished(view: WebView?, url: String?) {
                        val script = """
                            (function() {
                                var removeAds = function() {
                                    var ads = document.querySelectorAll('.ad-container, .ad-interrupting, .ytp-ad-overlay-container, .promoted-sparkles-text-search-root, ytm-promoted-video-renderer');
                                    for (var i = 0; i < ads.length; i++) { 
                                        ads[i].style.display = 'none'; 
                                    }
                                    var skipBtn = document.querySelector('.ytp-ad-skip-button');
                                    if (skipBtn) { skipBtn.click(); }
                                };
                                removeAds();
                                // Observa mudanças na DOM para remover ads carregados dinamicamente
                                var observer = new MutationObserver(removeAds);
                                observer.observe(document.body, { childList: true, subtree: true });
                            })();
                        """.trimIndent()
                        view?.evaluateJavascript(script, null)
                    }
                }
                
                webChromeClient = WebChromeClient()
                loadUrl(url)
            }
        }, 
        update = { it.loadUrl(url) }
    )
}
