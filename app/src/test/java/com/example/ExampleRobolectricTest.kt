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

  @Test
  fun `verify grade notification channel creation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.notifications.GradeNotificationManager.createNotificationChannel(context)
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
    val channel = notificationManager.getNotificationChannel(com.example.notifications.GradeNotificationManager.CHANNEL_ID)
    org.junit.Assert.assertNotNull(channel)
    assertEquals("Grade Updates", channel.name)
    assertEquals(android.app.NotificationManager.IMPORTANCE_HIGH, channel.importance)
  }

  @Test
  fun `verify grade tracker parses json and detects newly posted grades`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.data.GradeTracker.clearGradeCache(context)

    val jsonPayload = """
      [
        {
          "courseCode": "CS101",
          "courseDescription": "Introduction to Computing",
          "grade": "1.25",
          "term": "Midterm"
        },
        {
          "courseCode": "MATH101",
          "courseDescription": "College Algebra",
          "grade": "1.50",
          "term": "Midterm"
        }
      ]
    """.trimIndent()

    // 1. Initial snapshot should establish baseline without false positives
    val initialNew = com.example.data.GradeTracker.processJsonGrades(context, jsonPayload)
    assertEquals(0, initialNew.size)

    // 2. Subsequent check with new grade posted should detect the newly posted subject
    val updatedPayload = """
      [
        {
          "courseCode": "CS101",
          "courseDescription": "Introduction to Computing",
          "grade": "1.25",
          "term": "Midterm"
        },
        {
          "courseCode": "MATH101",
          "courseDescription": "College Algebra",
          "grade": "1.50",
          "term": "Midterm"
        },
        {
          "courseCode": "ENG101",
          "courseDescription": "Purposive Communication",
          "grade": "1.00",
          "term": "Midterm"
        }
      ]
    """.trimIndent()

    val secondPassNew = com.example.data.GradeTracker.processJsonGrades(context, updatedPayload)
    assertEquals(1, secondPassNew.size)
    assertEquals("ENG101", secondPassNew[0].courseCode)
    assertEquals("1.00", secondPassNew[0].grade)

    // 3. Repeated pass with same data produces no duplicate notifications
    val repeatedPass = com.example.data.GradeTracker.processJsonGrades(context, updatedPayload)
    assertEquals(0, repeatedPass.size)
  }

  @Test
  fun `verify main activity launches and sets up hardware acceleration and window flags`() {
    val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java).setup()
    val activity = controller.get()
    org.junit.Assert.assertNotNull(activity)
    val flags = activity.window.attributes.flags
    val isHardwareAccelerated = (flags and android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED) != 0
    org.junit.Assert.assertTrue(isHardwareAccelerated)
  }

  @Test
  fun `verify sti session bridge persists and retrieves web tokens`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val bridge = com.example.bridge.StiSessionBridge(context)
    val testOrigin = "https://one.sti.edu"
    val testTokens = """{"id_token":"mock_jwt_12345","user_id":"02000123456"}"""
    bridge.saveWebTokens(testOrigin, testTokens)

    val retrieved = bridge.getWebTokens(testOrigin)
    assertEquals(testTokens, retrieved)
  }

  @Test
  fun `verify native student data manager loads profile and schedule`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val profile = com.example.data.StudentDataManager.getProfile(context)
    org.junit.Assert.assertNotNull(profile)
    org.junit.Assert.assertTrue(profile.name.isNotBlank())
    org.junit.Assert.assertTrue(profile.studentNumber.isNotBlank())

    val schedule = com.example.data.StudentDataManager.getDefaultSchedule()
    org.junit.Assert.assertTrue(schedule.isNotEmpty())

    val grades = com.example.data.StudentDataManager.getDefaultGrades()
    org.junit.Assert.assertTrue(grades.isNotEmpty())
  }

  @Test
  fun `verify send test notification runs without error`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.notifications.GradeNotificationManager.sendTestNotification(context)
  }
}
