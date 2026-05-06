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
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(icon = { Text("📻") }, label = { Text("Rádio") }, selected = selectedTab == 0, onClick = { selectedTab = 0 })
                NavigationBarItem(icon = { Text("📺") }, label = { Text("YouTube") }, selected = selectedTab == 1, onClick = { selectedTab = 1 })
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            Crossfade(targetState = selectedTab, label = "Tab") { tab ->
                when (tab) {
                    0 -> Text("Painel Rádio (Em breve)", modifier = Modifier.fillMaxSize())
                    1 -> YouTubeScreen(url = "https://m.youtube.com/@dissejesus/streams")
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
            settings.apply { javaScriptEnabled = true; domStorageEnabled = true; mediaPlaybackRequiresUserGesture = false }
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            loadUrl(url)
        }
    }, update = { it.loadUrl(url) })
}
