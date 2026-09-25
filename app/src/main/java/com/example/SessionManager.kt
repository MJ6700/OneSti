package com.example

import android.content.Context
import android.content.SharedPreferences
import android.webkit.CookieManager
import android.webkit.WebView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Manages persistent login session for the ONE STI Portal.
 *
 * Ensures that session cookies, tokens, and browser state persist indefinitely
 * across app closures, background task kills, and device restarts so students
 * and faculty never get signed out when exiting the app.
 */
object SessionManager {

  private const val PREFS_NAME = "one_sti_session_store"
  private const val KEY_COOKIES_PREFIX = "cookie_"
  private const val KEY_LAST_URL = "last_valid_portal_url"
  private const val KEY_KEEP_SIGNED_IN = "keep_signed_in_active"

  // 10 years in seconds (315360000s)
  private const val TEN_YEARS_SECONDS = 315360000L

  val TRACKED_DOMAINS = listOf(
    "https://one.sti.edu",
    "https://sts.sti.edu",
    "https://login.microsoftonline.com",
    "https://login.live.com",
    "https://login.windows.net",
    "https://portal.office.com",
    "https://account.activedirectory.windowsazure.com",
    "https://mysignins.microsoft.com",
    "https://myapps.microsoft.com"
  )

  /**
   * JavaScript snippet injected into WebView pages.
   *
   * 1. Restores auth tokens stored in sessionStorage from persistent localStorage.
   * 2. Synchronizes any updates to sessionStorage into localStorage in real time.
   * 3. On Microsoft SSO "Stay signed in?" prompts, automatically keeps the persistent
   *    session option checked.
   */
  const val SESSION_PERSISTENCE_JS = """
    (function() {
      try {
        var PREFIX = '__sti_persisted_session_';

        // 1. Restore sessionStorage from localStorage
        for (var i = 0; i < localStorage.length; i++) {
          var k = localStorage.key(i);
          if (k && k.indexOf(PREFIX) === 0) {
            var originalKey = k.substring(PREFIX.length);
            var val = localStorage.getItem(k);
            if (val !== null && sessionStorage.getItem(originalKey) === null) {
              sessionStorage.setItem(originalKey, val);
            }
          }
        }

        // 2. Synchronize current sessionStorage into localStorage
        function syncSession() {
          try {
            for (var j = 0; j < sessionStorage.length; j++) {
              var sKey = sessionStorage.key(j);
              if (sKey) {
                var sVal = sessionStorage.getItem(sKey);
                if (sVal !== null) {
                  localStorage.setItem(PREFIX + sKey, sVal);
                }
              }
            }
          } catch (e) {}
        }
        syncSession();

        // 3. Intercept sessionStorage updates to keep persistent backup fresh
        if (!window.__sti_session_hooked) {
          window.__sti_session_hooked = true;
          var origSet = sessionStorage.setItem;
          sessionStorage.setItem = function(key, value) {
            origSet.apply(this, arguments);
            try {
              if (key) localStorage.setItem(PREFIX + key, value);
            } catch (e) {}
          };

          var origRemove = sessionStorage.removeItem;
          sessionStorage.removeItem = function(key) {
            origRemove.apply(this, arguments);
            try {
              if (key) localStorage.removeItem(PREFIX + key);
            } catch (e) {}
          };

          var origClear = sessionStorage.clear;
          sessionStorage.clear = function() {
            origClear.apply(this, arguments);
            try {
              for (var c = localStorage.length - 1; c >= 0; c--) {
                var cKey = localStorage.key(c);
                if (cKey && cKey.indexOf(PREFIX) === 0) {
                  localStorage.removeItem(cKey);
                }
              }
            } catch (e) {}
          };
        }

        // 4. Check "Don't show this again" / "Stay signed in" checkbox on Microsoft SSO if present
        var kmsiCheckbox = document.getElementById('KmsiCheckboxField') || 
                           document.querySelector('input[name="DontShowAgain"]');
        if (kmsiCheckbox && !kmsiCheckbox.checked) {
          kmsiCheckbox.checked = true;
          kmsiCheckbox.dispatchEvent(new Event('change', { bubbles: true }));
        }
      } catch (err) {}
    })();
  """

  private fun getPrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }

  private fun getFarFutureExpiryDate(): String {
    val sdf = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("GMT")
    val futureTime = System.currentTimeMillis() + (TEN_YEARS_SECONDS * 1000L)
    return sdf.format(Date(futureTime))
  }

  /**
   * Initializes CookieManager settings to enable cross-session persistence.
   */
  fun initCookieManager(context: Context, webView: WebView? = null) {
    try {
      val cookieManager = CookieManager.getInstance()
      cookieManager.setAcceptCookie(true)
      if (webView != null) {
        cookieManager.setAcceptThirdPartyCookies(webView, true)
      }
      restoreCookies(context)
      cookieManager.flush()
    } catch (_: Exception) {}
  }

  /**
   * Reads existing cookies, gives them a 10-year expiration date so Chromium's
   * engine marks them as persistent SQLite records instead of in-memory session cookies,
   * saves a backup in SharedPreferences, and flushes to disk immediately.
   * Runs in the background to ensure butter-smooth 60/120fps UI scrolling.
   */
  fun persistSession(context: Context, currentUrl: String?) {
    Thread {
      try {
        val cookieManager = CookieManager.getInstance()
        val prefs = getPrefs(context)
        val editor = prefs.edit()
        val expiry = getFarFutureExpiryDate()

        val domainsToScan = mutableListOf<String>()
        if (!currentUrl.isNullOrBlank()) {
          domainsToScan.add(currentUrl)
        }
        domainsToScan.addAll(TRACKED_DOMAINS)

        for (domain in domainsToScan.distinct()) {
          val cookieHeader = cookieManager.getCookie(domain)
          if (!cookieHeader.isNullOrBlank()) {
            // Backup raw header
            editor.putString(KEY_COOKIES_PREFIX + domain, cookieHeader)

            // Re-inject each cookie with a 10-year Max-Age and Expires date so Android
            // Chromium's SQLite database retains it across app termination and process death
            val cookies = cookieHeader.split(";")
            for (cookie in cookies) {
              val trimmed = cookie.trim()
              if (trimmed.isNotEmpty()) {
                val nameValue = trimmed.split("=", limit = 2)
                if (nameValue.isNotEmpty()) {
                  val name = nameValue[0].trim()
                  val value = if (nameValue.size > 1) nameValue[1].trim() else ""
                  val persistentCookie = "$name=$value; Expires=$expiry; Max-Age=$TEN_YEARS_SECONDS; Path=/; SameSite=Lax; Secure"
                  cookieManager.setCookie(domain, persistentCookie)
                }
              }
            }
          }
        }

        editor.putBoolean(KEY_KEEP_SIGNED_IN, true)
        editor.apply()
        cookieManager.flush()
      } catch (_: Exception) {}
    }.start()
  }

  /**
   * Restores backed-up cookies from SharedPreferences into CookieManager
   * on app startup before the initial webpage is fetched.
   */
  fun restoreCookies(context: Context) {
    try {
      val cookieManager = CookieManager.getInstance()
      val prefs = getPrefs(context)
      val allEntries = prefs.all
      val expiry = getFarFutureExpiryDate()

      for ((key, value) in allEntries) {
        if (key.startsWith(KEY_COOKIES_PREFIX) && value is String && value.isNotBlank()) {
          val domain = key.substring(KEY_COOKIES_PREFIX.length)
          val cookies = value.split(";")
          for (cookie in cookies) {
            val trimmed = cookie.trim()
            if (trimmed.isNotEmpty()) {
              val nameValue = trimmed.split("=", limit = 2)
              if (nameValue.isNotEmpty()) {
                val name = nameValue[0].trim()
                val cookieVal = if (nameValue.size > 1) nameValue[1].trim() else ""
                val persistentCookie = "$name=$cookieVal; Expires=$expiry; Max-Age=$TEN_YEARS_SECONDS; Path=/; SameSite=Lax; Secure"
                cookieManager.setCookie(domain, persistentCookie)
              }
            }
          }
        }
      }
      cookieManager.flush()
    } catch (_: Exception) {}
  }

  /**
   * Saves the last valid portal page so the user returns right back to their active
   * screen after exiting, rather than being dumped at the root splash or login page.
   */
  fun saveLastValidUrl(context: Context, url: String?) {
    if (url.isNullOrBlank()) return
    val lower = url.lowercase()

    // If user explicitly performed a signout or hit an error page, reset saved page
    if (lower.contains("logout") || lower.contains("signout") || lower.contains("logoff")) {
      getPrefs(context).edit().remove(KEY_LAST_URL).apply()
      return
    }

    // Only save legitimate STI portal pages
    if (lower.contains("sti.edu")) {
      getPrefs(context).edit().putString(KEY_LAST_URL, url).apply()
    }
  }

  /**
   * Returns the last valid portal URL the user visited, or the default portal URL.
   */
  fun getLastValidUrl(context: Context, defaultUrl: String): String {
    val prefs = getPrefs(context)
    val saved = prefs.getString(KEY_LAST_URL, null)
    return if (!saved.isNullOrBlank() && saved.contains("sti.edu")) {
      saved
    } else {
      defaultUrl
    }
  }

  /**
   * Clears saved session state if user deliberately logs out.
   */
  fun handleExplicitLogout(context: Context) {
    try {
      getPrefs(context).edit().clear().apply()
      val cookieManager = CookieManager.getInstance()
      cookieManager.removeAllCookies(null)
      cookieManager.flush()
    } catch (_: Exception) {}
  }
}
