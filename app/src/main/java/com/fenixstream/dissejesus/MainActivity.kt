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
                    
                    // Bloqueia navegação para fora do domínio do YouTube
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        val newUrl = request?.url.toString()
                        return !newUrl.contains("youtube.com") && !newUrl.contains("youtu.be")
                    }

                    // Injeção de Motor Anti-Ad e Restrição Visual
                    override fun onPageFinished(view: WebView?, url: String?) {
                        val jsScript = """
                            (function() {
                                // 1. INJEÇÃO DE CSS: Oculta anúncios e os vídeos recomendados (Força a ficar no canal)
                                var style = document.createElement('style');
                                style.innerHTML = `
                                    /* Esconde Banners Promocionais */
                                    ytm-promoted-video-renderer,
                                    ytm-companion-ad-renderer,
                                    .ad-container,
                                    .ytp-ad-overlay-container,
                                    /* Esconde "Vídeos Relacionados" para evitar fuga do canal */
                                    ytm-item-section-renderer[section-identifier="related-items"],
                                    .ytm-pivot-bar-renderer {
                                        display: none !important;
                                    }
                                `;
                                document.head.appendChild(style);

                                // 2. LÓGICA DE VÍDEO: Pula o anúncio em vez de deixar a tela preta
                                setInterval(function() {
                                    // Tenta clicar no botão "Pular Anúncio" se ele existir
                                    var skipBtn = document.querySelector('.ytp-ad-skip-button, .ytp-skip-ad-button, .ytp-ad-skip-button-modern');
                                    if (skipBtn) { 
                                        skipBtn.click(); 
                                    }
                                    
                                    // Se o anúncio não for pulável, força o vídeo do anúncio a ir para o final imediatamente
                                    var adIsShowing = document.querySelector('.ad-showing');
                                    var video = document.querySelector('video');
                                    if (adIsShowing && video) {
                                        if (video.duration && video.currentTime < video.duration - 1) {
                                            video.currentTime = video.duration - 0.5; // Avança para meio segundo antes do fim
                                        }
                                    }
                                }, 500); // Roda a cada meio segundo
                            })();
                        """.trimIndent()
                        view?.evaluateJavascript(jsScript, null)
                    }
                }
                
                webChromeClient = WebChromeClient()
                loadUrl(url)
            }
        }, 
        update = { it.loadUrl(url) }
    )
}
