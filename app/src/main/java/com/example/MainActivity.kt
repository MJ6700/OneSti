package com.example

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.RenderProcessGoneDetail
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.navigation.AppDestinations
import com.example.navigation.NavigationTransitions
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.ShortcutsScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StiBlue
import com.example.ui.theme.StiYellow

private const val ONE_STI_URL = "https://one.sti.edu/"

private const val SMOOTH_SCROLL_JS = """
  (function() {
    if (window.__sti_smooth_applied) return;
    window.__sti_smooth_applied = true;
    try {
      var s = document.createElement('style');
      s.id = '__sti_smooth_css';
      s.textContent = '* { -webkit-tap-highlight-color: transparent !important; } html, body { -webkit-overflow-scrolling: touch !important; overscroll-behavior-y: contain !important; } button, a, [role="button"], input, select { touch-action: manipulation !important; }';
      (document.head || document.documentElement).appendChild(s);
    } catch(e) {}
  })();
"""

@Composable
fun rememberNetworkConnectivity(): Boolean {
  val context = LocalContext.current
  val connectivityManager = remember(context) {
    context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
  }

  var isConnected by remember {
    val activeNetwork = connectivityManager?.activeNetwork
    val caps = connectivityManager?.getNetworkCapabilities(activeNetwork)
    mutableStateOf(caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true)
  }

  DisposableEffect(connectivityManager) {
    if (connectivityManager == null) return@DisposableEffect onDispose {}

    val request = NetworkRequest.Builder()
      .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
      .build()

    val callback = object : ConnectivityManager.NetworkCallback() {
      override fun onAvailable(network: Network) {
        isConnected = true
      }

      override fun onLost(network: Network) {
        val active = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(active)
        isConnected = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
      }

      override fun onCapabilitiesChanged(
        network: Network,
        networkCapabilities: NetworkCapabilities
      ) {
        isConnected = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
      }
    }

    try {
      connectivityManager.registerNetworkCallback(request, callback)
    } catch (_: Exception) {
      // Graceful fallback for test or constrained environments
    }

    onDispose {
      try {
        connectivityManager.unregisterNetworkCallback(callback)
      } catch (_: Exception) {}
    }
  }

  return isConnected
}

class MainActivity : ComponentActivity() {
  companion object {
    init {
      try {
        android.system.Os.setenv("MESA_LOG_LEVEL", "none", true)
        android.system.Os.setenv("EGL_LOG_LEVEL", "fatal", true)
      } catch (_: Throwable) {}
    }
  }

  private var activeWebView: WebView? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Hardware acceleration at Window level for 60/120fps smoothness
    window.setFlags(
      android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
      android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
    )

    // Enable high refresh rate (90Hz / 120Hz) on devices that support it
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      try {
        val display = display
        val maxMode = display?.supportedModes?.maxByOrNull { it.refreshRate }
        if (maxMode != null && maxMode.refreshRate > 60f) {
          window.attributes = window.attributes.apply {
            preferredDisplayModeId = maxMode.modeId
          }
        }
      } catch (_: Exception) {}
    }

    SessionManager.initCookieManager(this)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        OneStiNavGraph(onWebViewBound = { activeWebView = it })
      }
    }
  }

  override fun onPause() {
    super.onPause()
    activeWebView?.let { wv ->
      SessionManager.persistSession(this, wv.url)
    }
  }

  override fun onStop() {
    super.onStop()
    activeWebView?.let { wv ->
      SessionManager.persistSession(this, wv.url)
    }
  }

  override fun onDestroy() {
    activeWebView?.let { wv ->
      SessionManager.persistSession(this, wv.url)
    }
    super.onDestroy()
  }

  override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      val wv = activeWebView
      if (wv != null && wv.canGoBack()) {
        wv.goBack()
        return true
      }
    }
    return super.onKeyDown(keyCode, event)
  }
}

/**
 * Top-level Compose Navigation Graph with smooth slide & fade transitions between screens.
 */
@Composable
fun OneStiNavGraph(
  onWebViewBound: (WebView) -> Unit = {}
) {
  val navController = rememberNavController()
  var webViewRef by remember { mutableStateOf<WebView?>(null) }

  NavHost(
    navController = navController,
    startDestination = AppDestinations.PORTAL,
    enterTransition = NavigationTransitions.enterFromRight(),
    exitTransition = NavigationTransitions.exitToLeft(),
    popEnterTransition = NavigationTransitions.popEnterFromLeft(),
    popExitTransition = NavigationTransitions.popExitToRight()
  ) {
    composable(AppDestinations.PORTAL) {
      PortalScreen(
        onWebViewBound = {
          webViewRef = it
          onWebViewBound(it)
        },
        onNavigateToShortcuts = {
          navController.navigate(AppDestinations.SHORTCUTS)
        },
        onNavigateToAbout = {
          navController.navigate(AppDestinations.ABOUT)
        }
      )
    }

    composable(AppDestinations.SHORTCUTS) {
      ShortcutsScreen(
        onNavigateBack = {
          navController.popBackStack()
        },
        onOpenUrl = { url ->
          webViewRef?.loadUrl(url)
          navController.popBackStack()
        }
      )
    }

    composable(AppDestinations.ABOUT) {
      AboutScreen(
        onNavigateBack = {
          navController.popBackStack()
        }
      )
    }
  }
}

@Composable
fun OneStiApp(
  onWebViewBound: (WebView) -> Unit = {}
) {
  OneStiNavGraph(onWebViewBound = onWebViewBound)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortalScreen(
  onWebViewBound: (WebView) -> Unit = {},
  onNavigateToShortcuts: () -> Unit = {},
  onNavigateToAbout: () -> Unit = {}
) {
  val context = LocalContext.current
  val isOnline = rememberNetworkConnectivity()
  var webViewInstance by remember { mutableStateOf<WebView?>(null) }
  var canGoBack by remember { mutableStateOf(false) }
  var canGoForward by remember { mutableStateOf(false) }
  var isLoading by remember { mutableStateOf(true) }
  var isPullRefreshing by remember { mutableStateOf(false) }
  var isRetrying by remember { mutableStateOf(false) }
  var loadProgress by remember { mutableFloatStateOf(0f) }
  var pageTitle by remember { mutableStateOf("One STI") }
  var hasError by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf("") }
  var backPressedTime by remember { mutableLongStateOf(0L) }

  // Auto-reconnect when network connectivity is re-established
  LaunchedEffect(isOnline) {
    if (isOnline && hasError) {
      hasError = false
      isRetrying = false
      Toast.makeText(context, "Internet connection restored. Reloading One STI...", Toast.LENGTH_SHORT).show()
      webViewInstance?.settings?.cacheMode = WebSettings.LOAD_DEFAULT
      webViewInstance?.reload()
    }
  }

  // Periodic background session flush every 30 seconds to guarantee no session data is ever lost
  LaunchedEffect(Unit) {
    while (true) {
      kotlinx.coroutines.delay(30_000L)
      webViewInstance?.let { wv ->
        SessionManager.persistSession(context, wv.url)
      }
    }
  }

  // File upload callback handling for WebChromeClient
  var filePathCallbackRef by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }

  val fileChooserLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) { result ->
    val callback = filePathCallbackRef
    if (callback != null) {
      if (result.resultCode == Activity.RESULT_OK && result.data != null) {
        val parsedResults = WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
        callback.onReceiveValue(parsedResults)
      } else {
        callback.onReceiveValue(null)
      }
      filePathCallbackRef = null
    }
  }

  // Hardware and system back button navigation:
  // Traverses WebView browser history first; shows confirmation if on root page to prevent accidental exit
  BackHandler(enabled = true) {
    val wv = webViewInstance
    when {
      hasError -> {
        hasError = false
        wv?.reload()
      }
      wv != null && wv.canGoBack() -> {
        wv.goBack()
        canGoBack = wv.canGoBack()
        canGoForward = wv.canGoForward()
      }
      else -> {
        val now = System.currentTimeMillis()
        if (now - backPressedTime < 2000L) {
          SessionManager.persistSession(context, wv?.url)
          (context as? Activity)?.finish()
        } else {
          backPressedTime = now
          Toast.makeText(context, "Press back again to exit One STI • Made by MJ", Toast.LENGTH_SHORT).show()
        }
      }
    }
  }

  val initialUrl = remember { SessionManager.getLastValidUrl(context, ONE_STI_URL) }

  val animatedProgress by animateFloatAsState(
    targetValue = if (isLoading) loadProgress.coerceIn(0.08f, 1f) else 1f,
    animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
    label = "load_progress_anim"
  )

  Scaffold(
    contentWindowInsets = WindowInsets.safeDrawing
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      OneStiWebViewContainer(
        url = initialUrl,
        isRefreshing = isPullRefreshing,
        isOnline = isOnline,
        onRefresh = {
          isPullRefreshing = true
          hasError = false
          webViewInstance?.reload()
        },
        onWebViewCreated = { wv ->
          webViewInstance = wv
          onWebViewBound(wv)
        },
        onCanGoBackChanged = { canGoBack = it },
        onCanGoForwardChanged = { canGoForward = it },
        onLoadingChanged = { loading ->
          isLoading = loading
          if (!loading) {
            isPullRefreshing = false
            isRetrying = false
          }
        },
        onProgressChanged = { loadProgress = it },
        onTitleChanged = { title ->
          if (!title.isNullOrBlank() && !title.startsWith("http")) {
            pageTitle = title
          }
        },
        onError = { err ->
          hasError = true
          errorMessage = err
          isPullRefreshing = false
          isRetrying = false
        },
        onOpenFileChooser = { callback, params ->
          filePathCallbackRef?.onReceiveValue(null)
          filePathCallbackRef = callback
          try {
            val intent = params.createIntent()
            fileChooserLauncher.launch(intent)
            true
          } catch (_: Exception) {
            filePathCallbackRef = null
            false
          }
        },
        modifier = Modifier
          .fillMaxSize()
          .testTag("one_sti_webview")
      )

      // Thin loading indicator at the very top of the page
      AnimatedVisibility(
        visible = isLoading && !hasError,
        enter = fadeIn(animationSpec = tween(150)),
        exit = fadeOut(animationSpec = tween(250)),
        modifier = Modifier.align(Alignment.TopCenter)
      ) {
        LinearProgressIndicator(
          progress = { animatedProgress },
          modifier = Modifier
            .fillMaxWidth()
            .height(3.dp),
          color = StiYellow,
          trackColor = Color.Transparent
        )
      }

      // Subtle offline mode banner when viewing cached content without error
      AnimatedVisibility(
        visible = !isOnline && !hasError,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier
          .align(Alignment.TopCenter)
          .padding(top = 10.dp)
      ) {
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.94f),
          tonalElevation = 6.dp,
          shadowElevation = 4.dp,
          modifier = Modifier.testTag("offline_cached_banner")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(StiYellow)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
              text = stringResource(R.string.offline_banner),
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      // Custom No Internet Connection & Error Screen
      if (hasError) {
        Surface(
          modifier = Modifier
            .fillMaxSize()
            .testTag("error_container"),
          color = MaterialTheme.colorScheme.background
        ) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 28.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            // STI Branded Visual Header
            Box(
              modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(StiBlue.copy(alpha = 0.12f)),
              contentAlignment = Alignment.Center
            ) {
              Box(
                modifier = Modifier
                  .size(68.dp)
                  .clip(CircleShape)
                  .background(StiBlue.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.WifiOff,
                  contentDescription = stringResource(R.string.no_internet_title),
                  tint = StiBlue,
                  modifier = Modifier.size(38.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Offline Status Chip
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
              modifier = Modifier.testTag("offline_status_pill")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                  text = "Offline",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onErrorContainer
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = stringResource(R.string.no_internet_title),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = StiBlue,
              textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = if (errorMessage.isNotBlank()) errorMessage else stringResource(R.string.no_internet_desc),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Troubleshooting Checklist Card
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
              )
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Text(
                  text = "Troubleshooting Tips:",
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.SemiBold,
                  color = StiBlue
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "• Verify Wi-Fi or mobile data is enabled\n• Toggle Airplane Mode off and on\n• Check your campus Wi-Fi sign-in portal",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  lineHeight = 18.sp
                )
              }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary: Retry Connection Button
            Button(
              onClick = {
                isRetrying = true
                hasError = false
                webViewInstance?.settings?.cacheMode = if (isOnline) WebSettings.LOAD_DEFAULT else WebSettings.LOAD_CACHE_ELSE_NETWORK
                webViewInstance?.reload()
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = StiBlue,
                contentColor = Color.White
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("error_retry_button")
            ) {
              if (isRetrying && isLoading) {
                CircularProgressIndicator(
                  color = Color.White,
                  modifier = Modifier.size(18.dp),
                  strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.size(10.dp))
                Text("Connecting...")
              } else {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text(stringResource(R.string.retry_connection))
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary: Network Settings Button
            OutlinedButton(
              onClick = {
                try {
                  val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                  context.startActivity(intent)
                } catch (_: Exception) {
                  try {
                    val fallbackIntent = Intent(Settings.ACTION_SETTINGS)
                    context.startActivity(fallbackIntent)
                  } catch (_: Exception) {
                    Toast.makeText(context, "Unable to open system settings", Toast.LENGTH_SHORT).show()
                  }
                }
              },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("network_settings_button")
            ) {
              Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = StiBlue)
              Spacer(modifier = Modifier.size(8.dp))
              Text(stringResource(R.string.network_settings), color = StiBlue)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Tertiary: View Cached Portal Content Button
            TextButton(
              onClick = {
                hasError = false
                webViewInstance?.settings?.cacheMode = WebSettings.LOAD_CACHE_ONLY
                webViewInstance?.loadUrl(ONE_STI_URL)
              },
              modifier = Modifier.testTag("view_cached_button")
            ) {
              Icon(imageVector = Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(18.dp), tint = StiBlue)
              Spacer(modifier = Modifier.size(6.dp))
              Text(stringResource(R.string.view_cached_portal), color = StiBlue)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = "One STI • Made by MJ",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
              textAlign = TextAlign.Center
            )
          }
        }
      }
    }
  }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun OneStiWebViewContainer(
  url: String,
  isRefreshing: Boolean,
  isOnline: Boolean,
  onRefresh: () -> Unit,
  onWebViewCreated: (WebView) -> Unit,
  onCanGoBackChanged: (Boolean) -> Unit,
  onCanGoForwardChanged: (Boolean) -> Unit,
  onLoadingChanged: (Boolean) -> Unit,
  onProgressChanged: (Float) -> Unit,
  onTitleChanged: (String?) -> Unit,
  onError: (String) -> Unit,
  onOpenFileChooser: (ValueCallback<Array<Uri>>, WebChromeClient.FileChooserParams) -> Boolean,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  DisposableEffect(Unit) {
    onDispose {
      SessionManager.persistSession(context, null)
      CookieManager.getInstance().flush()
    }
  }

  AndroidView(
    modifier = modifier,
    factory = { ctx ->
      val webView = WebView(ctx).apply {
        layoutParams = ViewGroup.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT
        )

        // Setup CookieManager and restore saved session state
        SessionManager.initCookieManager(ctx, this)
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptThirdPartyCookies(this, true)

        // Disable intermediate offscreen layer to enable direct hardware GPU compositing
        setLayerType(View.LAYER_TYPE_NONE, null)

        // Disable nested scrolling on WebView so Chromium's internal compositor thread
        // handles touch drags, flings, and smooth momentum scrolling at full 60/120Hz
        isNestedScrollingEnabled = false
        isScrollContainer = true
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS

        // WebSettings optimizations for fluid student portal browsing
        settings.apply {
          javaScriptEnabled = true
          domStorageEnabled = true
          databaseEnabled = true
          saveFormData = true
          cacheMode = if (isOnline) WebSettings.LOAD_DEFAULT else WebSettings.LOAD_CACHE_ELSE_NETWORK
          allowFileAccess = false
          allowContentAccess = true
          setSupportZoom(true)
          builtInZoomControls = true
          displayZoomControls = false
          useWideViewPort = true
          loadWithOverviewMode = true
          mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

          // Optimize layout and rendering pipeline for responsive student portal browsing
          mediaPlaybackRequiresUserGesture = false
          offscreenPreRaster = true // Pre-renders out-of-viewport tiles for smooth scrolling
          setRenderPriority(WebSettings.RenderPriority.HIGH)

          // Optimize user agent string so Google/Microsoft OAuth does not reject with disallowed_useragent
          // and treats the session as a persistent browser rather than a transient in-app webview
          val defaultUa = userAgentString
          userAgentString = defaultUa
            .replace("; wv", "")
            .replace(Regex("Version/\\d+\\.\\d+\\s*"), "")
        }

        // WebChromeClient for progress, title, and file upload support
        webChromeClient = object : WebChromeClient() {
          override fun onProgressChanged(view: WebView?, newProgress: Int) {
            onProgressChanged(newProgress / 100f)
            if (newProgress >= 100) {
              onLoadingChanged(false)
            }
          }

          override fun onReceivedTitle(view: WebView?, title: String?) {
            onTitleChanged(title)
          }

          override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
          ): Boolean {
            if (filePathCallback != null && fileChooserParams != null) {
              return onOpenFileChooser(filePathCallback, fileChooserParams)
            }
            return false
          }
        }

        // WebViewClient to handle navigation, SSO logins, and errors
        webViewClient = object : WebViewClient() {
          override fun onPageCommitVisible(view: WebView?, url: String?) {
            super.onPageCommitVisible(view, url)
            onLoadingChanged(false)
            view?.evaluateJavascript(SMOOTH_SCROLL_JS, null)
          }

          override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            onLoadingChanged(true)
            onCanGoBackChanged(view?.canGoBack() == true)
            onCanGoForwardChanged(view?.canGoForward() == true)

            // Inject smooth scrolling rules, persistent storage sync, and cookies
            view?.evaluateJavascript(SMOOTH_SCROLL_JS, null)
            view?.evaluateJavascript(SessionManager.SESSION_PERSISTENCE_JS, null)
            SessionManager.saveLastValidUrl(ctx, url)
          }

          override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            onLoadingChanged(false)
            onCanGoBackChanged(view?.canGoBack() == true)
            onCanGoForwardChanged(view?.canGoForward() == true)

            // Ensure smooth scrolling rules, tokens mirrored, and cookies flushed
            view?.evaluateJavascript(SMOOTH_SCROLL_JS, null)
            view?.evaluateJavascript(SessionManager.SESSION_PERSISTENCE_JS, null)
            SessionManager.persistSession(ctx, url)
            SessionManager.saveLastValidUrl(ctx, url)
          }

          override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
            super.doUpdateVisitedHistory(view, url, isReload)
            onCanGoBackChanged(view?.canGoBack() == true)
            onCanGoForwardChanged(view?.canGoForward() == true)
            SessionManager.saveLastValidUrl(ctx, url)
          }

          override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val targetUri = request?.url ?: return false
            val scheme = targetUri.scheme?.lowercase() ?: ""

            // Handle non-web URI schemes (tel, mailto, sms, intent, etc.)
            if (scheme != "http" && scheme != "https") {
              return try {
                val intent = Intent(Intent.ACTION_VIEW, targetUri)
                ctx.startActivity(intent)
                true
              } catch (_: Exception) {
                true
              }
            }

            // Normal web links, SSO login pages, and student portal stay within the app's WebView
            return false
          }

          override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
          ) {
            super.onReceivedError(view, request, error)
            // Only report errors for the main page load
            if (request?.isForMainFrame == true) {
              val desc = error?.description?.toString() ?: "Connection error"
              onError(desc)
            }
          }

          override fun onRenderProcessGone(
            view: WebView?,
            detail: RenderProcessGoneDetail?
          ): Boolean {
            if (view != null) {
              (view.parent as? ViewGroup)?.removeView(view)
              view.destroy()
            }
            onError("Web renderer was restarted. Tap Try Again to reload.")
            return true
          }
        }

        // Support file downloads (e.g. syllabus, study guides, PDF registration, grades)
        setDownloadListener(DownloadListener { downloadUrl, userAgent, contentDisposition, mimetype, _ ->
          try {
            val guessedName = URLUtil.guessFileName(downloadUrl, contentDisposition, mimetype)
            val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
              setMimeType(mimetype)
              val cookies = cookieManager.getCookie(downloadUrl)
              addRequestHeader("cookie", cookies)
              addRequestHeader("User-Agent", userAgent)
              setDescription("Downloading: $guessedName • Made by MJ")
              setTitle("$guessedName (Made by MJ)")
              setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
              setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, guessedName)
            }

            val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
            Toast.makeText(ctx, "Download started: $guessedName\nMade by MJ", Toast.LENGTH_LONG).show()
          } catch (e: Exception) {
            try {
              val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
              ctx.startActivity(fallbackIntent)
              Toast.makeText(ctx, "Downloading in browser • Made by MJ", Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {
              Toast.makeText(ctx, "Unable to start download", Toast.LENGTH_SHORT).show()
            }
          }
        })

        setOnKeyListener { _, keyCode, event ->
          if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            if (canGoBack()) {
              goBack()
              onCanGoBackChanged(canGoBack())
              onCanGoForwardChanged(canGoForward())
              return@setOnKeyListener true
            }
          }
          false
        }

        loadUrl(url)
        onWebViewCreated(this)
      }

      val swipeRefresh = SwipeRefreshLayout(ctx).apply {
        layoutParams = ViewGroup.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT
        )
        // STI Blue (#005596) and STI Yellow (#FFD100)
        setColorSchemeColors(
          android.graphics.Color.parseColor("#005596"),
          android.graphics.Color.parseColor("#FFD100")
        )
        setProgressBackgroundColorSchemeColor(android.graphics.Color.WHITE)
        setDistanceToTriggerSync(320)

        // Only allow pull-to-refresh if the WebView is at the top of the content without scroll conflicts
        setOnChildScrollUpCallback { _, _ ->
          webView.canScrollVertically(-1)
        }

        setOnRefreshListener {
          onRefresh()
        }

        addView(webView)
      }

      swipeRefresh
    },
    update = { swipeRefresh ->
      swipeRefresh.isRefreshing = isRefreshing
      val webView = swipeRefresh.getChildAt(0) as? WebView
      val targetCacheMode = if (isOnline) {
        WebSettings.LOAD_DEFAULT
      } else {
        WebSettings.LOAD_CACHE_ELSE_NETWORK
      }
      if (webView?.settings?.cacheMode != targetCacheMode) {
        webView?.settings?.cacheMode = targetCacheMode
      }
    }
  )
}
