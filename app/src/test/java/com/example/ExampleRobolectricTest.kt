package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("One STI", appName)
  }

  @Test
  fun `verify navigation destinations`() {
    assertEquals("portal", com.example.navigation.AppDestinations.PORTAL)
    assertEquals("shortcuts", com.example.navigation.AppDestinations.SHORTCUTS)
    assertEquals("about", com.example.navigation.AppDestinations.ABOUT)
  }

  @Test
  fun `verify no internet strings are localized and defined`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val noInternetTitle = context.getString(R.string.no_internet_title)
    val retryText = context.getString(R.string.retry_connection)
    val networkSettings = context.getString(R.string.network_settings)
    val viewCached = context.getString(R.string.view_cached_portal)
    val offlineBanner = context.getString(R.string.offline_banner)

    assertEquals("No Internet Connection", noInternetTitle)
    assertEquals("Retry Connection", retryText)
    assertEquals("Network Settings", networkSettings)
    assertEquals("View Cached Content", viewCached)
    assertEquals("Offline Mode • Displaying cached content", offlineBanner)
  }

  @Test
  fun `verify session manager remembers valid portal url and ignores logout url`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val defaultUrl = "https://one.sti.edu/"

    // Initially defaults to defaultUrl
    assertEquals(defaultUrl, SessionManager.getLastValidUrl(context, defaultUrl))

    // Saves valid portal URL
    val portalGradesUrl = "https://one.sti.edu/student/grades"
    SessionManager.saveLastValidUrl(context, portalGradesUrl)
    assertEquals(portalGradesUrl, SessionManager.getLastValidUrl(context, defaultUrl))

    // When logout URL is encountered, resets saved portal page to avoid loop
    val logoutUrl = "https://one.sti.edu/account/logout"
    SessionManager.saveLastValidUrl(context, logoutUrl)
    assertEquals(defaultUrl, SessionManager.getLastValidUrl(context, defaultUrl))
  }

  @Test
  fun `verify session persistence js is valid and contains storage synchronization`() {
    val js = SessionManager.SESSION_PERSISTENCE_JS
    assert(js.contains("__sti_persisted_session_"))
    assert(js.contains("sessionStorage"))
    assert(js.contains("localStorage"))
  }
}
