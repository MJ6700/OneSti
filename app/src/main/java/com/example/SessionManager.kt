package com.example

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Manages persistent login session for the ONE STI Portal and all associated accounts.
 *
 * Ensures that session cookies, tokens, and browser state for ALL student, faculty,
 * and Microsoft 365 / STI SSO accounts persist indefinitely across app closures,
 * background task kills, and device restarts so users never get signed out.
 */
object SessionManager {

  private const val PREFS_NAME = "one_sti_session_store"
  private const val KEY_COOKIES_PREFIX = "cookie_"
  private const val KEY_LAST_URL = "last_valid_portal_url"
  private const val KEY_KEEP_SIGNED_IN = "keep_signed_in_active"
  private const val KEY_KNOWN_DOMAINS = "tracked_visited_domains"

  // 10 years in seconds (315360000s)
  private const val TEN_YEARS_SECONDS = 315360000L

  val TRACKED_DOMAINS = listOf(
    // STI Portals (.edu and .edu.ph)
    "https://one.sti.edu",
    "https://sti.edu",
    "https://sts.sti.edu",
    "https://elms.sti.edu",
    "https://enrollment.sti.edu",
    "https://portal.sti.edu",
    "https://sis.sti.edu",
    "https://one.sti.edu.ph",
    "https://sti.edu.ph",
    "https://sts.sti.edu.ph",
    "https://elms.sti.edu.ph",
    "https://portal.sti.edu.ph",
    "https://enrollment.sti.edu.ph",
    "https://sti.instructure.com",

    // Microsoft Identity, Office 365, & Azure AD Single Sign-On
    "https://login.microsoftonline.com",
    "https://login.live.com",
    "https://login.microsoft.com",
    "https://login.windows.net",
    "https://portal.office.com",
    "https://account.activedirectory.windowsazure.com",
    "https://account.live.com",
    "https://mysignins.microsoft.com",
    "https://myapps.microsoft.com",
    "https://myprofile.microsoft.com",
    "https://aadcdn.msauth.net",
    "https://aadcdn.msftauth.net",
    "https://autologon.microsoftazuread-sso.com",
    "https://sts.windows.net",
    "https://msft.sts.microsoft.com",
    "https://teams.microsoft.com",
    "https://outlook.office.com",
    "https://outlook.office365.com"
  )

  /**
   * JavaScript snippet injected into WebView pages.
   *
   * 1. Restores auth tokens stored in sessionStorage from persistent localStorage across all accounts.
   * 2. Synchronizes any updates to sessionStorage into localStorage in real time.
   * 3. Protects multi-account OAuth/MSAL/ADAL tokens from being flushed on page transitions.
   * 4. On Microsoft SSO & STI STS prompts, automatically selects "Stay signed in" / "Remember me"
   *    and confirms "Yes" so persistent refresh tokens (PRTs) are issued for all accounts.
   * 5. Pings the portal every 2 minutes and resets idle timers so university sessions never expire.
   */
  const val SESSION_PERSISTENCE_JS = """
    (function() {
      try {
        var PREFIX = '__sti_persisted_session_';

        // 1. Restore sessionStorage from persistent localStorage
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

        // 2. Synchronize current sessionStorage into localStorage across all accounts
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

          window.addEventListener('beforeunload', syncSession);
          window.addEventListener('pagehide', syncSession);
          document.addEventListener('visibilitychange', function() {
            if (document.visibilityState === 'hidden') syncSession();
          });
        }

        // 4. Auto-check "Stay signed in", "Remember me", and "Don't show this again" on all login screens
        var checkSelectors = [
          '#KmsiCheckboxField',
          'input[name="DontShowAgain"]',
          'input[name="RememberMe"]',
          'input[id*="RememberMe"]',
          'input[id*="Kmsi"]',
          'input[id*="remember"]',
          'input[name*="remember"]'
        ];
        for (var s = 0; s < checkSelectors.length; s++) {
          var cb = document.querySelector(checkSelectors[s]);
          if (cb && !cb.checked) {
            cb.checked = true;
            cb.dispatchEvent(new Event('change', { bubbles: true }));
          }
        }

        // 5. On Microsoft "Stay signed in?" prompt, confirm with "Yes" so persistent refresh tokens are created
        var kmsiPromptTitle = document.getElementById('kmsiTitle') || 
                              document.querySelector('[data-test-id="kmsiText"]') ||
                              document.querySelector('.kmsi-title');
        var submitBtn = document.getElementById('idSIButton9');
        if (kmsiPromptTitle && submitBtn && !window.__sti_kmsi_submitted) {
          window.__sti_kmsi_submitted = true;
          setTimeout(function() {
            try {
              var kmsiCb = document.getElementById('KmsiCheckboxField') || document.querySelector('input[name="DontShowAgain"]');
              if (kmsiCb && !kmsiCb.checked) kmsiCb.checked = true;
              submitBtn.click();
            } catch(e) {}
          }, 300);
        }

        // 6. Anti-Inactivity Keep-Alive: Prevent all STI & Microsoft Portal sessions from timing out
        if (!window.__sti_keepalive_active) {
          window.__sti_keepalive_active = true;
          // Ping origin every 2 minutes to refresh ASP.NET and OAuth session cookies
          setInterval(function() {
            try {
              if (location.origin && (location.origin.indexOf('sti.edu') !== -1 || location.origin.indexOf('microsoft') !== -1 || location.origin.indexOf('office') !== -1)) {
                var xhr = new XMLHttpRequest();
                xhr.open('GET', location.origin + '/?_keepalive=' + Date.now(), true);
                xhr.withCredentials = true;
                xhr.send();
              }
            } catch (e) {}
          }, 2 * 60 * 1000);

          // Reset client-side idle timers if portal has inactivity checks
          setInterval(function() {
            try {
              if (typeof window.resetSessionTimer === 'function') window.resetSessionTimer();
              if (typeof window.keepAlive === 'function') window.keepAlive();
              document.dispatchEvent(new Event('mousemove'));
            } catch (e) {}
          }, 60 * 1000);
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
   * Dynamically tracks any domain visited by the user so that cookies for any campus
   * portal, OAuth redirect, or auxiliary service are permanently preserved.
   */
  fun trackDomain(context: Context, url: String?) {
    if (url.isNullOrBlank()) return
    try {
      val uri = Uri.parse(url)
      val scheme = uri.scheme?.lowercase() ?: "https"
      val host = uri.host?.lowercase() ?: return
      if (scheme == "http" || scheme == "https") {
        val domainOrigin = "$scheme://$host"
        val prefs = getPrefs(context)
        val currentSet = prefs.getStringSet(KEY_KNOWN_DOMAINS, emptySet())?.toMutableSet() ?: mutableSetOf()
        if (currentSet.add(domainOrigin)) {
          prefs.edit().putStringSet(KEY_KNOWN_DOMAINS, currentSet).apply()
        }
      }
    } catch (_: Exception) {}
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

        // Track current domain dynamically
        trackDomain(context, currentUrl)

        val domainsToScan = mutableListOf<String>()
        if (!currentUrl.isNullOrBlank()) {
          domainsToScan.add(currentUrl)
          try {
            val uri = Uri.parse(currentUrl)
            val host = uri.host
            if (!host.isNullOrBlank()) {
              domainsToScan.add("${uri.scheme ?: "https"}://$host")
            }
          } catch (_: Exception) {}
        }
        domainsToScan.addAll(TRACKED_DOMAINS)
        val dynamicDomains = prefs.getStringSet(KEY_KNOWN_DOMAINS, emptySet()) ?: emptySet()
        domainsToScan.addAll(dynamicDomains)

        val pathsToCheck = listOf("", "/", "/Student", "/Account", "/Home", "/adfs", "/common", "/organizations")

        for (domain in domainsToScan.distinct()) {
          for (subPath in pathsToCheck) {
            val scanUrl = if (domain.endsWith("/")) domain.removeSuffix("/") + subPath else domain + subPath
            val cookieHeader = cookieManager.getCookie(scanUrl)
            if (!cookieHeader.isNullOrBlank()) {
              // Backup raw header
              editor.putString(KEY_COOKIES_PREFIX + domain, cookieHeader)

              // Re-inject each cookie with a 10-year Max-Age and Expires date so Android
              // Chromium's SQLite database retains it permanently across all accounts
              val cookies = cookieHeader.split(";")
              val isHttps = domain.startsWith("https://")
              val secureFlag = if (isHttps) "; Secure" else ""

              for (cookie in cookies) {
                val trimmed = cookie.trim()
                if (trimmed.isNotEmpty()) {
                  val nameValue = trimmed.split("=", limit = 2)
                  if (nameValue.isNotEmpty()) {
                    val name = nameValue[0].trim()
                    val value = if (nameValue.size > 1) nameValue[1].trim() else ""

                    // 1. Host-specific cookie
                    val persistentCookieHost = "$name=$value; Expires=$expiry; Max-Age=$TEN_YEARS_SECONDS; Path=/; SameSite=Lax$secureFlag"
                    cookieManager.setCookie(domain, persistentCookieHost)

                    // 2. Wildcard domain cookies so subdomains share login state seamlessly
                    val lowerDomain = domain.lowercase()
                    val rootDomains = mutableListOf<String>()
                    if (lowerDomain.contains("sti.edu.ph")) rootDomains.add(".sti.edu.ph")
                    if (lowerDomain.contains("sti.edu")) rootDomains.add(".sti.edu")
                    if (lowerDomain.contains("microsoftonline.com")) rootDomains.add(".microsoftonline.com")
                    if (lowerDomain.contains("microsoft.com")) rootDomains.add(".microsoft.com")
                    if (lowerDomain.contains("live.com")) rootDomains.add(".live.com")
                    if (lowerDomain.contains("windows.net")) rootDomains.add(".windows.net")
                    if (lowerDomain.contains("office.com")) rootDomains.add(".office.com")
                    if (lowerDomain.contains("office365.com")) rootDomains.add(".office365.com")
                    if (lowerDomain.contains("instructure.com")) rootDomains.add(".instructure.com")

                    for (rootDomain in rootDomains.distinct()) {
                      val persistentCookieDomain = "$name=$value; Domain=$rootDomain; Expires=$expiry; Max-Age=$TEN_YEARS_SECONDS; Path=/; SameSite=Lax$secureFlag"
                      cookieManager.setCookie(domain, persistentCookieDomain)
                    }
                  }
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
   * on app startup before any webpage is fetched.
   */
  fun restoreCookies(context: Context) {
    try {
      val cookieManager = CookieManager.getInstance()
      cookieManager.setAcceptCookie(true)
      val prefs = getPrefs(context)
      val allEntries = prefs.all
      val expiry = getFarFutureExpiryDate()

      for ((key, value) in allEntries) {
        if (key.startsWith(KEY_COOKIES_PREFIX) && value is String && value.isNotBlank()) {
          val domain = key.substring(KEY_COOKIES_PREFIX.length)
          val isHttps = domain.startsWith("https://")
          val secureFlag = if (isHttps) "; Secure" else ""
          val cookies = value.split(";")

          for (cookie in cookies) {
            val trimmed = cookie.trim()
            if (trimmed.isNotEmpty()) {
              val nameValue = trimmed.split("=", limit = 2)
              if (nameValue.isNotEmpty()) {
                val name = nameValue[0].trim()
                val cookieVal = if (nameValue.size > 1) nameValue[1].trim() else ""

                val persistentCookie = "$name=$cookieVal; Expires=$expiry; Max-Age=$TEN_YEARS_SECONDS; Path=/; SameSite=Lax$secureFlag"
                cookieManager.setCookie(domain, persistentCookie)

                val lowerDomain = domain.lowercase()
                val rootDomains = mutableListOf<String>()
                if (lowerDomain.contains("sti.edu.ph")) rootDomains.add(".sti.edu.ph")
                if (lowerDomain.contains("sti.edu")) rootDomains.add(".sti.edu")
                if (lowerDomain.contains("microsoftonline.com")) rootDomains.add(".microsoftonline.com")
                if (lowerDomain.contains("microsoft.com")) rootDomains.add(".microsoft.com")
                if (lowerDomain.contains("live.com")) rootDomains.add(".live.com")
                if (lowerDomain.contains("windows.net")) rootDomains.add(".windows.net")
                if (lowerDomain.contains("office.com")) rootDomains.add(".office.com")
                if (lowerDomain.contains("office365.com")) rootDomains.add(".office365.com")
                if (lowerDomain.contains("instructure.com")) rootDomains.add(".instructure.com")

                for (rootDomain in rootDomains.distinct()) {
                  val persistentCookieDomain = "$name=$cookieVal; Domain=$rootDomain; Expires=$expiry; Max-Age=$TEN_YEARS_SECONDS; Path=/; SameSite=Lax$secureFlag"
                  cookieManager.setCookie(domain, persistentCookieDomain)
                }
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

    // Don't save transient login redirects as the main portal home
    if (lower.contains("login.microsoftonline.com") || lower.contains("sts.sti.edu/adfs/ls")) {
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
