package com.example

import com.example.R
import com.example.databinding.ActivityMainBinding
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
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.navigation.AppDestinations
import com.example.navigation.NavigationTransitions
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.ShortcutsScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

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

/**
 * Native XML Activity for One STI.
 *
 * Implements Android XML layout (R.layout.activity_main via ViewBinding) with
 * SwipeRefreshLayout, full edge-to-edge hardware acceleration, 60/120Hz display modes,
 * permanent session persistence, anti-inactivity keep-alive, and integrated download management.
 */
class MainActivity : ComponentActivity() {

  companion object {
    init {
      try {
        android.system.Os.setenv("MESA_LOG_LEVEL", "none", true)
        android.system.Os.setenv("EGL_LOG_LEVEL", "fatal", true)
      } catch (_: Throwable) {}
    }
  }

  private lateinit var binding: ActivityMainBinding

  private var backPressedTime: Long = 0L
  private var filePathCallback: ValueCallback<Array<Uri>>? = null
  private val sessionScope = CoroutineScope(Dispatchers.Main + Job())

  private var connectivityManager: ConnectivityManager? = null
  private var networkCallback: ConnectivityManager.NetworkCallback? = null

  private val fileChooserLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
  ) { result ->
    val callback = filePathCallback
    if (callback != null) {
      if (result.resultCode == Activity.RESULT_OK && result.data != null) {
        val parsedResults = WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
        callback.onReceiveValue(parsedResults)
      } else {
        callback.onReceiveValue(null)
      }
      filePathCallback = null
    }
  }

  @SuppressLint("SetJavaScriptEnabled")
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

    // Inflate Android XML Layout with ViewBinding
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setupWebView()
    setupSwipeRefresh()
    setupErrorControls()
    setupNetworkMonitoring()
    startPeriodicSessionPersistence()

    // Restore last valid portal URL or default home
    val startUrl = SessionManager.getLastValidUrl(this, ONE_STI_URL)
    binding.webView.loadUrl(startUrl)
  }

  @SuppressLint("SetJavaScriptEnabled")
  private fun setupWebView() {
    val wv = binding.webView
    SessionManager.initCookieManager(this, wv)
    CookieManager.getInstance().setAcceptThirdPartyCookies(wv, true)

    // Direct GPU compositing
    wv.setLayerType(View.LAYER_TYPE_NONE, null)

    // Smooth touch scrolling handled by Chromium compositor thread
    wv.isNestedScrollingEnabled = false
    wv.isScrollContainer = true
    wv.isVerticalScrollBarEnabled = false
    wv.isHorizontalScrollBarEnabled = false
    wv.overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS

    wv.settings.apply {
      javaScriptEnabled = true
      domStorageEnabled = true
      databaseEnabled = true
      saveFormData = true
      cacheMode = WebSettings.LOAD_DEFAULT
      allowFileAccess = false
      allowContentAccess = true
      setSupportZoom(true)
      builtInZoomControls = true
      displayZoomControls = false
      useWideViewPort = true
      loadWithOverviewMode = true
      mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
      mediaPlaybackRequiresUserGesture = false

      // Performance & zero-flicker scrolling
      offscreenPreRaster = true
      setRenderPriority(WebSettings.RenderPriority.HIGH)

      // Persistent browser User-Agent
      val defaultUa = userAgentString
      userAgentString = defaultUa
        .replace("; wv", "")
        .replace(Regex("Version/\\d+\\.\\d+\\s*"), "")
    }

    wv.webChromeClient = object : WebChromeClient() {
      override fun onProgressChanged(view: WebView?, newProgress: Int) {
        binding.progressBar.progress = newProgress
        if (newProgress < 100) {
          binding.progressBar.visibility = View.VISIBLE
        } else {
          binding.progressBar.visibility = View.GONE
          binding.swipeRefreshLayout.isRefreshing = false
        }
      }

      override fun onShowFileChooser(
        view: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
      ): Boolean {
        this@MainActivity.filePathCallback?.onReceiveValue(null)
        this@MainActivity.filePathCallback = filePathCallback
        return try {
          val intent = fileChooserParams?.createIntent()
          if (intent != null) {
            fileChooserLauncher.launch(intent)
            true
          } else {
            this@MainActivity.filePathCallback = null
            false
          }
        } catch (_: Exception) {
          this@MainActivity.filePathCallback = null
          false
        }
      }
    }

    wv.webViewClient = object : WebViewClient() {
      override fun onPageCommitVisible(view: WebView?, url: String?) {
        super.onPageCommitVisible(view, url)
        view?.evaluateJavascript(SMOOTH_SCROLL_JS, null)
      }

      override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        binding.progressBar.visibility = View.VISIBLE

        view?.evaluateJavascript(SMOOTH_SCROLL_JS, null)
        view?.evaluateJavascript(SessionManager.SESSION_PERSISTENCE_JS, null)
        SessionManager.saveLastValidUrl(this@MainActivity, url)
      }

      override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        binding.progressBar.visibility = View.GONE
        binding.swipeRefreshLayout.isRefreshing = false

        view?.evaluateJavascript(SMOOTH_SCROLL_JS, null)
        view?.evaluateJavascript(SessionManager.SESSION_PERSISTENCE_JS, null)
        SessionManager.persistSession(this@MainActivity, url)
        SessionManager.saveLastValidUrl(this@MainActivity, url)
      }

      override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
        super.doUpdateVisitedHistory(view, url, isReload)
        SessionManager.saveLastValidUrl(this@MainActivity, url)
      }

      override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val targetUri = request?.url ?: return false
        val scheme = targetUri.scheme?.lowercase() ?: ""

        if (scheme != "http" && scheme != "https") {
          return try {
            val intent = Intent(Intent.ACTION_VIEW, targetUri)
            startActivity(intent)
            true
          } catch (_: Exception) {
            true
          }
        }
        return false
      }

      override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
        super.onReceivedError(view, request, error)
        if (request?.isForMainFrame == true) {
          val desc = error?.description?.toString() ?: getString(R.string.no_internet_desc)
          showErrorState(desc)
        }
      }

      override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
        showErrorState("Web renderer restarted. Tap Retry to reload.")
        return true
      }
    }

    // Support file downloads (Syllabus, schedule, study guides, PDF assessments)
    wv.setDownloadListener(DownloadListener { downloadUrl, userAgent, contentDisposition, mimetype, _ ->
      try {
        val guessedName = URLUtil.guessFileName(downloadUrl, contentDisposition, mimetype)
        val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
          setMimeType(mimetype)
          val cookies = CookieManager.getInstance().getCookie(downloadUrl)
          addRequestHeader("cookie", cookies)
          addRequestHeader("User-Agent", userAgent)
          setDescription("Downloading: $guessedName • Made by MJ")
          setTitle("$guessedName (Made by MJ)")
          setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
          setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, guessedName)
        }
        val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
        Toast.makeText(this, "Download started: $guessedName\nMade by MJ", Toast.LENGTH_LONG).show()
      } catch (_: Exception) {
        try {
          val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
          startActivity(fallbackIntent)
        } catch (_: Exception) {
          Toast.makeText(this, "Unable to start download", Toast.LENGTH_SHORT).show()
        }
      }
    })
  }

  private fun setupSwipeRefresh() {
    binding.swipeRefreshLayout.setColorSchemeColors(
      android.graphics.Color.parseColor("#005596"),
      android.graphics.Color.parseColor("#FFD100")
    )
    binding.swipeRefreshLayout.setProgressBackgroundColorSchemeColor(android.graphics.Color.WHITE)
    binding.swipeRefreshLayout.setDistanceToTriggerSync(320)

    binding.swipeRefreshLayout.setOnChildScrollUpCallback { _, _ ->
      binding.webView.canScrollVertically(-1)
    }

    binding.swipeRefreshLayout.setOnRefreshListener {
      binding.errorContainer.visibility = View.GONE
      binding.webView.settings.cacheMode = WebSettings.LOAD_DEFAULT
      binding.webView.reload()
    }
  }

  private fun setupErrorControls() {
    binding.btnRetry.setOnClickListener {
      binding.errorContainer.visibility = View.GONE
      binding.webView.settings.cacheMode = WebSettings.LOAD_DEFAULT
      binding.webView.reload()
    }

    binding.btnSettings.setOnClickListener {
      try {
        startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
      } catch (_: Exception) {
        try {
          startActivity(Intent(Settings.ACTION_SETTINGS))
        } catch (_: Exception) {
          Toast.makeText(this, "Unable to open system settings", Toast.LENGTH_SHORT).show()
        }
      }
    }

    binding.btnCached.setOnClickListener {
      binding.errorContainer.visibility = View.GONE
      binding.webView.settings.cacheMode = WebSettings.LOAD_CACHE_ONLY
      binding.webView.loadUrl(ONE_STI_URL)
    }
  }

  private fun showErrorState(message: String) {
    binding.swipeRefreshLayout.isRefreshing = false
    binding.progressBar.visibility = View.GONE
    binding.tvErrorMessage.text = message
    binding.errorContainer.visibility = View.VISIBLE
  }

  private fun setupNetworkMonitoring() {
    connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
    val request = NetworkRequest.Builder()
      .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
      .build()

    networkCallback = object : ConnectivityManager.NetworkCallback() {
      override fun onAvailable(network: Network) {
        runOnUiThread {
          binding.offlineBanner.visibility = View.GONE
          if (binding.errorContainer.visibility == View.VISIBLE) {
            binding.errorContainer.visibility = View.GONE
            Toast.makeText(this@MainActivity, "Internet connection restored. Reloading One STI...", Toast.LENGTH_SHORT).show()
            binding.webView.settings.cacheMode = WebSettings.LOAD_DEFAULT
            binding.webView.reload()
          }
        }
      }

      override fun onLost(network: Network) {
        runOnUiThread {
          if (binding.errorContainer.visibility != View.VISIBLE) {
            binding.offlineBanner.visibility = View.VISIBLE
          }
        }
      }
    }

    try {
      connectivityManager?.registerNetworkCallback(request, networkCallback!!)
    } catch (_: Exception) {}
  }

  private fun startPeriodicSessionPersistence() {
    sessionScope.launch {
      while (isActive) {
        delay(30_000L)
        SessionManager.persistSession(this@MainActivity, binding.webView.url)
      }
    }
  }

  override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
    if (keyCode == KeyEvent.KEYCODE_BACK && event?.action == KeyEvent.ACTION_DOWN) {
      if (binding.errorContainer.visibility == View.VISIBLE) {
        binding.errorContainer.visibility = View.GONE
        binding.webView.reload()
        return true
      }
      if (binding.webView.canGoBack()) {
        binding.webView.goBack()
        return true
      }
      val now = System.currentTimeMillis()
      if (now - backPressedTime < 2000L) {
        SessionManager.persistSession(this, binding.webView.url)
        finish()
      } else {
        backPressedTime = now
        Toast.makeText(this, "Press back again to exit One STI • Made by MJ", Toast.LENGTH_SHORT).show()
      }
      return true
    }
    return super.onKeyDown(keyCode, event)
  }

  override fun onPause() {
    super.onPause()
    SessionManager.persistSession(this, binding.webView.url)
  }

  override fun onStop() {
    super.onStop()
    SessionManager.persistSession(this, binding.webView.url)
  }

  override fun onDestroy() {
    sessionScope.cancel()
    SessionManager.persistSession(this, binding.webView.url)
    try {
      networkCallback?.let { connectivityManager?.unregisterNetworkCallback(it) }
    } catch (_: Exception) {}
    super.onDestroy()
  }
}

/**
 * Compose Navigation definitions retained for architectural completeness and tests.
 */
@Composable
fun OneStiNavGraph(
  onWebViewBound: (WebView) -> Unit = {}
) {
  val navController = rememberNavController()

  NavHost(
    navController = navController,
    startDestination = AppDestinations.PORTAL,
    enterTransition = NavigationTransitions.enterFromRight(),
    exitTransition = NavigationTransitions.exitToLeft(),
    popEnterTransition = NavigationTransitions.popEnterFromLeft(),
    popExitTransition = NavigationTransitions.popExitToRight()
  ) {
    composable(AppDestinations.PORTAL) {}
    composable(AppDestinations.SHORTCUTS) {
      ShortcutsScreen(
        onNavigateBack = { navController.popBackStack() },
        onOpenUrl = { navController.popBackStack() }
      )
    }
    composable(AppDestinations.ABOUT) {
      AboutScreen(onNavigateBack = { navController.popBackStack() })
    }
  }
}

@Composable
fun OneStiApp(onWebViewBound: (WebView) -> Unit = {}) {
  OneStiNavGraph(onWebViewBound = onWebViewBound)
}

