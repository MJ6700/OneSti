package com.example

import android.annotation.SuppressLint
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.bridge.StiGradeBridge
import com.example.notifications.GradeNotificationManager
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
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import com.example.ui.theme.StiDarkBlue
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.navigation.AppDestinations
import com.example.navigation.NavigationTransitions
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GradesScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.screens.LedgerScreen
import com.example.ui.screens.ProfileScreen
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
      s.textContent = '* { -webkit-tap-highlight-color: transparent !important; } ' +
        'html, body { -webkit-overflow-scrolling: touch !important; overscroll-behavior-y: contain !important; scroll-behavior: smooth !important; } ' +
        'button, a, [role="button"], input, select { touch-action: manipulation !important; } ' +
        '.page-content, main, .main-content, #content, .container, body { transform: translateZ(0); backface-visibility: hidden; will-change: scroll-position; }';
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

    // Enable high refresh rate (144Hz / 120Hz / 90Hz) on devices that support it for ultra-smooth experience
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val display = display
        val modes = display?.supportedModes ?: emptyArray()
        val currentWidth = display?.mode?.physicalWidth ?: 0
        val currentHeight = display?.mode?.physicalHeight ?: 0

        // Prioritize highest refresh rate mode (supports 90Hz, 120Hz, 144Hz, 165Hz)
        val bestMode = modes
          .filter { it.physicalWidth == currentWidth && it.physicalHeight == currentHeight }
          .maxByOrNull { it.refreshRate }
          ?: modes.maxByOrNull { it.refreshRate }

        if (bestMode != null && bestMode.refreshRate > 60f) {
          val attrs = window.attributes
          attrs.preferredDisplayModeId = bestMode.modeId
          attrs.preferredRefreshRate = bestMode.refreshRate
          window.attributes = attrs
        }
        window.setPreferMinimalPostProcessing(true)
      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        @Suppress("DEPRECATION")
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as? android.view.WindowManager
        @Suppress("DEPRECATION")
        val defaultDisplay = windowManager?.defaultDisplay
        val modes = defaultDisplay?.supportedModes ?: emptyArray()
        val maxMode = modes.maxByOrNull { it.refreshRate }
        if (maxMode != null && maxMode.refreshRate > 60f) {
          val attrs = window.attributes
          attrs.preferredDisplayModeId = maxMode.modeId
          attrs.preferredRefreshRate = maxMode.refreshRate
          window.attributes = attrs
        }
      }
    } catch (_: Throwable) {}

    SessionManager.initCookieManager(this)
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(android.graphics.Color.BLACK)
    )
    try {
      WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
    } catch (_: Exception) {}
    setContent {
      MyApplicationTheme {
        OneStiNavGraph(
          onWebViewBound = { activeWebView = it }
        )
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    val targetUrl = intent.getStringExtra(GradeNotificationManager.EXTRA_TARGET_URL)
    if (!targetUrl.isNullOrBlank()) {
      activeWebView?.loadUrl(targetUrl)
    }
  }

  override fun onResume() {
    super.onResume()
    SessionManager.restoreCookies(this)
    try {
      android.webkit.CookieManager.getInstance().flush()
    } catch (_: Exception) {}
  }

  override fun onPause() {
    super.onPause()
    activeWebView?.let { wv ->
      SessionManager.persistSession(this, wv.url)
    }
    try {
      android.webkit.CookieManager.getInstance().flush()
    } catch (_: Exception) {}
  }

  override fun onStop() {
    super.onStop()
    activeWebView?.let { wv ->
      SessionManager.persistSession(this, wv.url)
    }
    try {
      android.webkit.CookieManager.getInstance().flush()
    } catch (_: Exception) {}
  }

  override fun onDestroy() {
    activeWebView?.let { wv ->
      SessionManager.persistSession(this, wv.url)
    }
    try {
      android.webkit.CookieManager.getInstance().flush()
    } catch (_: Exception) {}
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
 * Top-level Compose Navigation Graph with smooth slide & fade transitions between screens
 * and unified persistent Bottom Navigation Bar.
 */
@Composable
fun OneStiNavGraph(
  onWebViewBound: (WebView) -> Unit = {}
) {
  val navController = rememberNavController()
  var webViewRef by remember { mutableStateOf<WebView?>(null) }
  var pendingPortalUrl by remember { mutableStateOf<String?>(null) }

  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route ?: AppDestinations.DASHBOARD

  val showBottomBar = currentRoute in listOf(
    AppDestinations.DASHBOARD,
    AppDestinations.GRADES,
    AppDestinations.SCHEDULE,
    AppDestinations.LEDGER,
    AppDestinations.PORTAL
  )

  Scaffold(
    containerColor = Color.Black,
    bottomBar = {
      if (showBottomBar) {
        NavigationBar(
          containerColor = Color.Black,
          contentColor = Color.White,
          tonalElevation = 8.dp,
          modifier = Modifier.testTag("app_bottom_nav_bar")
        ) {
          NavigationBarItem(
            selected = currentRoute == AppDestinations.DASHBOARD,
            onClick = {
              if (currentRoute != AppDestinations.DASHBOARD) {
                navController.navigate(AppDestinations.DASHBOARD) {
                  popUpTo(AppDestinations.DASHBOARD) { saveState = true }
                  launchSingleTop = true
                  restoreState = true
                }
              }
            },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = StiDarkBlue,
              selectedTextColor = StiYellow,
              indicatorColor = StiYellow,
              unselectedIconColor = Color.White.copy(alpha = 0.6f),
              unselectedTextColor = Color.White.copy(alpha = 0.6f)
            ),
            modifier = Modifier.testTag("nav_item_dashboard")
          )

          NavigationBarItem(
            selected = currentRoute == AppDestinations.GRADES,
            onClick = {
              if (currentRoute != AppDestinations.GRADES) {
                navController.navigate(AppDestinations.GRADES) {
                  popUpTo(AppDestinations.DASHBOARD) { saveState = true }
                  launchSingleTop = true
                  restoreState = true
                }
              }
            },
            icon = { Icon(Icons.Default.Assessment, contentDescription = "Grades") },
            label = { Text("Grades") },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = StiDarkBlue,
              selectedTextColor = StiYellow,
              indicatorColor = StiYellow,
              unselectedIconColor = Color.White.copy(alpha = 0.6f),
              unselectedTextColor = Color.White.copy(alpha = 0.6f)
            ),
            modifier = Modifier.testTag("nav_item_grades")
          )

          NavigationBarItem(
            selected = currentRoute == AppDestinations.SCHEDULE,
            onClick = {
              if (currentRoute != AppDestinations.SCHEDULE) {
                navController.navigate(AppDestinations.SCHEDULE) {
                  popUpTo(AppDestinations.DASHBOARD) { saveState = true }
                  launchSingleTop = true
                  restoreState = true
                }
              }
            },
            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Schedule") },
            label = { Text("Schedule") },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = StiDarkBlue,
              selectedTextColor = StiYellow,
              indicatorColor = StiYellow,
              unselectedIconColor = Color.White.copy(alpha = 0.6f),
              unselectedTextColor = Color.White.copy(alpha = 0.6f)
            ),
            modifier = Modifier.testTag("nav_item_schedule")
          )

          NavigationBarItem(
            selected = currentRoute == AppDestinations.LEDGER,
            onClick = {
              if (currentRoute != AppDestinations.LEDGER) {
                navController.navigate(AppDestinations.LEDGER) {
                  popUpTo(AppDestinations.DASHBOARD) { saveState = true }
                  launchSingleTop = true
                  restoreState = true
                }
              }
            },
            icon = { Icon(Icons.Default.AccountBalance, contentDescription = "Ledger") },
            label = { Text("Ledger") },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = StiDarkBlue,
              selectedTextColor = StiYellow,
              indicatorColor = StiYellow,
              unselectedIconColor = Color.White.copy(alpha = 0.6f),
              unselectedTextColor = Color.White.copy(alpha = 0.6f)
            ),
            modifier = Modifier.testTag("nav_item_ledger")
          )

          NavigationBarItem(
            selected = currentRoute == AppDestinations.PORTAL,
            onClick = {
              if (currentRoute != AppDestinations.PORTAL) {
                navController.navigate(AppDestinations.PORTAL) {
                  popUpTo(AppDestinations.DASHBOARD) { saveState = true }
                  launchSingleTop = true
                  restoreState = true
                }
              }
            },
            icon = { Icon(Icons.Default.Language, contentDescription = "Live Portal") },
            label = { Text("Portal") },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = StiDarkBlue,
              selectedTextColor = StiYellow,
              indicatorColor = StiYellow,
              unselectedIconColor = Color.White.copy(alpha = 0.6f),
              unselectedTextColor = Color.White.copy(alpha = 0.6f)
            ),
            modifier = Modifier.testTag("nav_item_portal")
          )
        }
      }
    }
  ) { innerPadding ->
    NavHost(
      navController = navController,
      startDestination = AppDestinations.DASHBOARD,
      modifier = Modifier.padding(innerPadding),
      enterTransition = NavigationTransitions.enterFromRight(),
      exitTransition = NavigationTransitions.exitToLeft(),
      popEnterTransition = NavigationTransitions.popEnterFromLeft(),
      popExitTransition = NavigationTransitions.popExitToRight()
    ) {
      composable(AppDestinations.DASHBOARD) {
        DashboardScreen(
          onNavigateToGrades = { navController.navigate(AppDestinations.GRADES) },
          onNavigateToSchedule = { navController.navigate(AppDestinations.SCHEDULE) },
          onNavigateToLedger = { navController.navigate(AppDestinations.LEDGER) },
          onNavigateToPortal = { url ->
            pendingPortalUrl = url
            navController.navigate(AppDestinations.PORTAL)
          },
          onNavigateToProfile = { navController.navigate(AppDestinations.PROFILE) }
        )
      }

      composable(AppDestinations.GRADES) {
        GradesScreen(
          onNavigateBack = { navController.popBackStack() },
          onOpenLivePortal = {
            pendingPortalUrl = "https://one.sti.edu/Student/Grades"
            navController.navigate(AppDestinations.PORTAL)
          }
        )
      }

      composable(AppDestinations.SCHEDULE) {
        ScheduleScreen(
          onNavigateBack = { navController.popBackStack() },
          onOpenLivePortal = {
            pendingPortalUrl = "https://one.sti.edu/Student/ClassSchedule"
            navController.navigate(AppDestinations.PORTAL)
          }
        )
      }

      composable(AppDestinations.LEDGER) {
        LedgerScreen(
          onNavigateBack = { navController.popBackStack() },
          onOpenLivePortal = {
            pendingPortalUrl = "https://one.sti.edu/Student/Ledger"
            navController.navigate(AppDestinations.PORTAL)
          }
        )
      }

      composable(AppDestinations.PORTAL) {
        PortalScreen(
          initialUrlOverride = pendingPortalUrl,
          onWebViewBound = {
            webViewRef = it
            onWebViewBound(it)
          },
          onNavigateToAbout = {
            navController.navigate(AppDestinations.ABOUT)
          }
        )
      }

      composable(AppDestinations.PROFILE) {
        ProfileScreen(
          onNavigateBack = { navController.popBackStack() },
          onNavigateToAbout = { navController.navigate(AppDestinations.ABOUT) }
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
  initialUrlOverride: String? = null,
  onWebViewBound: (WebView) -> Unit = {},
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

  // Request POST_NOTIFICATIONS permission on Android 13+ (API 33+)
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
      if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
    }
  }

  // Periodic background session flush every 15 seconds to guarantee all accounts stay logged in permanently
  LaunchedEffect(Unit) {
    while (true) {
      kotlinx.coroutines.delay(15_000L)
      webViewInstance?.let { wv ->
        SessionManager.persistSession(context, wv.url)
        wv.evaluateJavascript(SessionManager.SESSION_PERSISTENCE_JS, null)
      }
    }
  }

  // Periodic grade evaluation check while app is active
  LaunchedEffect(Unit) {
    while (true) {
      kotlinx.coroutines.delay(45_000L)
      webViewInstance?.evaluateJavascript(StiGradeBridge.GRADE_MONITOR_JS, null)
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

  val activity = context as? Activity
  val deepLinkUrl = remember { activity?.intent?.getStringExtra(GradeNotificationManager.EXTRA_TARGET_URL) }
  val initialUrl = remember(initialUrlOverride) {
    initialUrlOverride ?: deepLinkUrl ?: SessionManager.getLastValidUrl(context, ONE_STI_URL)
  }

  LaunchedEffect(initialUrlOverride) {
    if (!initialUrlOverride.isNullOrBlank()) {
      webViewInstance?.loadUrl(initialUrlOverride)
    }
  }

  val animatedProgress by animateFloatAsState(
    targetValue = if (isLoading) loadProgress.coerceIn(0.08f, 1f) else 1f,
    animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
    label = "load_progress_anim"
  )

  Scaffold(
    containerColor = Color.Black,
    contentWindowInsets = WindowInsets(0, 0, 0, 0)
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // Sleek solid Black Top Bar matching status bar height
      Spacer(
        modifier = Modifier
          .fillMaxWidth()
          .windowInsetsTopHeight(WindowInsets.statusBars)
          .background(Color.Black)
      )

      // Live Portal Header & Quick Links
      Surface(
        color = Color.Black,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "ONE STI LIVE PORTAL",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                letterSpacing = 0.5.sp
              )
              Spacer(modifier = Modifier.width(8.dp))
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = StiYellow
              ) {
                Text(
                  text = "LIVE",
                  color = StiDarkBlue,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Black,
                  modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
              }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(
                onClick = { webViewInstance?.reload() },
                modifier = Modifier.size(36.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Refresh,
                  contentDescription = "Reload",
                  tint = Color.White,
                  modifier = Modifier.size(20.dp)
                )
              }
              IconButton(
                onClick = onNavigateToAbout,
                modifier = Modifier.size(36.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Info,
                  contentDescription = "About",
                  tint = StiYellow,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }

          // Quick Chips for STI Portals
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            val currentWebUrl = webViewInstance?.url.orEmpty()
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (currentWebUrl.contains("one.sti") || currentWebUrl.isEmpty()) StiBlue else Color(0xFF1E293B),
              modifier = Modifier
                .weight(1f)
                .clickable { webViewInstance?.loadUrl("https://one.sti.edu") }
            ) {
              Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                Text("One STI", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (currentWebUrl.contains("elms")) StiBlue else Color(0xFF1E293B),
              modifier = Modifier
                .weight(1f)
                .clickable { webViewInstance?.loadUrl("https://elms.sti.edu") }
            ) {
              Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                Text("ELMS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (currentWebUrl.contains("office") || currentWebUrl.contains("microsoft")) StiBlue else Color(0xFF1E293B),
              modifier = Modifier
                .weight(1f)
                .clickable { webViewInstance?.loadUrl("https://portal.office.com") }
            ) {
              Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                Text("M365", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (currentWebUrl.contains("sts.sti")) StiBlue else Color(0xFF1E293B),
              modifier = Modifier
                .weight(1f)
                .clickable { webViewInstance?.loadUrl("https://sts.sti.edu") }
            ) {
              Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                Text("STS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
        }
      }

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .windowInsetsPadding(WindowInsets.navigationBars)
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
      androidx.compose.animation.AnimatedVisibility(
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
      androidx.compose.animation.AnimatedVisibility(
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
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
              contentAlignment = Alignment.Center
            ) {
              Box(
                modifier = Modifier
                  .size(68.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.WifiOff,
                  contentDescription = stringResource(R.string.no_internet_title),
                  tint = MaterialTheme.colorScheme.primary,
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
              color = MaterialTheme.colorScheme.primary,
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
                  color = MaterialTheme.colorScheme.primary
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
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("error_retry_button")
            ) {
              if (isRetrying && isLoading) {
                CircularProgressIndicator(
                  color = MaterialTheme.colorScheme.onPrimary,
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
              Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.size(8.dp))
              Text(stringResource(R.string.network_settings), color = MaterialTheme.colorScheme.primary)
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
              Icon(imageVector = Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.size(6.dp))
              Text(stringResource(R.string.view_cached_portal), color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = "One STI v1.1 • Made by MJ",
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
  var webViewRef by remember { mutableStateOf<WebView?>(null) }

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
        webViewRef = this
        layoutParams = ViewGroup.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT
        )

        // Setup CookieManager and restore saved session state
        SessionManager.initCookieManager(ctx, this)
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptThirdPartyCookies(this, true)

        // Inject StiGradeBridge to monitor and detect newly posted student grades
        addJavascriptInterface(StiGradeBridge(ctx), StiGradeBridge.BRIDGE_NAME)

        // Inject StiSessionBridge to vault Web Storage tokens natively and ensure no account logs out
        addJavascriptInterface(com.example.bridge.StiSessionBridge(ctx), com.example.bridge.StiSessionBridge.BRIDGE_NAME)

        // Clean white background for standard One STI portal
        setBackgroundColor(android.graphics.Color.WHITE)

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
          mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
          setSupportMultipleWindows(true)
          javaScriptCanOpenWindowsAutomatically = true

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

        // WebChromeClient for progress, title, multiple windows, and file upload support
        webChromeClient = object : WebChromeClient() {
          override fun onCreateWindow(
            view: WebView?,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: android.os.Message?
          ): Boolean {
            val windowCtx = view?.context ?: return false
            val tempWebView = WebView(windowCtx)
            tempWebView.webViewClient = object : WebViewClient() {
              override fun shouldOverrideUrlLoading(v: WebView?, request: WebResourceRequest?): Boolean {
                val targetUrl = request?.url?.toString()
                if (!targetUrl.isNullOrBlank()) {
                  view?.loadUrl(targetUrl)
                }
                return true
              }
            }
            val transport = resultMsg?.obj as? WebView.WebViewTransport
            if (transport != null) {
              transport.webView = tempWebView
              resultMsg.sendToTarget()
              return true
            }
            return false
          }

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
            view?.evaluateJavascript(StiGradeBridge.GRADE_MONITOR_JS, null)
          }

          override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            onLoadingChanged(true)
            onCanGoBackChanged(view?.canGoBack() == true)
            onCanGoForwardChanged(view?.canGoForward() == true)

            // Inject smooth scrolling rules, persistent storage sync, and cookies
            SessionManager.trackDomain(ctx, url)
            view?.evaluateJavascript(SMOOTH_SCROLL_JS, null)
            view?.evaluateJavascript(SessionManager.SESSION_PERSISTENCE_JS, null)
            SessionManager.persistSession(ctx, url)
            SessionManager.saveLastValidUrl(ctx, url)
          }

          override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            onLoadingChanged(false)
            onCanGoBackChanged(view?.canGoBack() == true)
            onCanGoForwardChanged(view?.canGoForward() == true)

            // Ensure smooth scrolling rules, tokens mirrored, and cookies flushed
            SessionManager.trackDomain(ctx, url)
            view?.evaluateJavascript(SMOOTH_SCROLL_JS, null)
            view?.evaluateJavascript(SessionManager.SESSION_PERSISTENCE_JS, null)
            view?.evaluateJavascript(StiGradeBridge.GRADE_MONITOR_JS, null)
            SessionManager.persistSession(ctx, url)
            SessionManager.saveLastValidUrl(ctx, url)
          }

          override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
            super.doUpdateVisitedHistory(view, url, isReload)
            onCanGoBackChanged(view?.canGoBack() == true)
            onCanGoForwardChanged(view?.canGoForward() == true)
            SessionManager.trackDomain(ctx, url)
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

            // Protect against automatic inactivity/session timeout forced logouts
            val urlStr = targetUri.toString()
            val lower = urlStr.lowercase()
            if (lower.contains("timeout") || lower.contains("sessionexpired") ||
                lower.contains("session-expired") || lower.contains("sessionended") ||
                lower.contains("inactivity")) {
              SessionManager.restoreCookies(ctx)
              val resumeUrl = SessionManager.getLastValidUrl(ctx, ONE_STI_URL)
              view?.loadUrl(resumeUrl)
              return true
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
        val primaryCol = android.graphics.Color.parseColor("#005596")
        val secondaryCol = android.graphics.Color.parseColor("#FFD100")
        val bgCol = android.graphics.Color.WHITE
        setColorSchemeColors(primaryCol, secondaryCol)
        setProgressBackgroundColorSchemeColor(bgCol)
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
      val primaryCol = android.graphics.Color.parseColor("#005596")
      val secondaryCol = android.graphics.Color.parseColor("#FFD100")
      val bgCol = android.graphics.Color.WHITE
      swipeRefresh.setColorSchemeColors(primaryCol, secondaryCol)
      swipeRefresh.setProgressBackgroundColorSchemeColor(bgCol)

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
