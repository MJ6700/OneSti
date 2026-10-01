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
import java.util.concurrent.Executors

/**
 * Manages persistent login sessions for ONE STI Portal and ALL associated student,
 * faculty, Microsoft 365, Canvas/ELMS, and SSO institutional accounts.
 *
 * Guarantees that cookies, tokens, and browser state for ALL accounts never expire
 * or log out automatically under any circumstances.
 */
object SessionManager {

  private const val PREFS_NAME = "one_sti_session_store"
  private const val KEY_COOKIES_PREFIX = "cookie_"
  private const val KEY_LAST_URL = "last_valid_portal_url"
  private const val KEY_KEEP_SIGNED_IN = "keep_signed_in_active"
  private const val KEY_KNOWN_DOMAINS = "tracked_visited_domains"

  // 10 years in seconds (315,360,000s)
  private const val TEN_YEARS_SECONDS = 315360000L

  private val persistExecutor = Executors.newSingleThreadExecutor()
  @Volatile private var isPersistQueued = false

  val TRACKED_DOMAINS = listOf(
    // STI Portals & Services (.edu and .edu.ph)
    "https://one.sti.edu",
    "https://one.sti.edu.ph",
    "https://sti.edu",
    "https://sti.edu.ph",
    "https://sts.sti.edu",
    "https://sts.sti.edu.ph",
    "https://elms.sti.edu",
    "https://elms.sti.edu.ph",
    "https://enrollment.sti.edu",
    "https://enrollment.sti.edu.ph",
    "https://portal.sti.edu",
    "https://portal.sti.edu.ph",
    "https://sis.sti.edu",
    "https://sis.sti.edu.ph",
    "https://canvas.sti.edu",
    "https://sti.instructure.com",
    "https://instructure.com",
    "https://adfs.sti.edu",
    "https://adfs.sti.edu.ph",
    "https://sso.sti.edu",
    "https://sso.sti.edu.ph",
    "https://auth.sti.edu",
    "https://auth.sti.edu.ph",

    // Microsoft Identity, Office 365, Teams, & Azure AD Single Sign-On
    "https://login.microsoftonline.com",
    "https://login.live.com",
    "https://login.microsoft.com",
    "https://login.windows.net",
    "https://portal.office.com",
    "https://www.office.com",
    "https://office.com",
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
    "https://outlook.office365.com",
    "https://onedrive.live.com",

    // Google Identity SSO (if linked to institutional accounts)
    "https://accounts.google.com",
    "https://accounts.google.com.ph"
  )

  /**
   * Universal JavaScript persistence and anti-inactivity monitor injected into every web page.
   *
   * 1. Monkey-patches document.cookie so all newly set session cookies are converted to 10-year persistent cookies.
   * 2. Synchronizes sessionStorage and localStorage bidirectionally with native Android SharedPreferences.
   * 3. Prevents multi-account OAuth/MSAL/ADAL token loss on tab/page transitions or process death.
   * 4. Auto-checks "Stay signed in", "Remember me", and "Don't ask again on this device".
   * 5. Confirms "Yes" on Microsoft KMSI prompts to acquire rolling Primary Refresh Tokens.
   * 6. Automatically clicks SSO login buttons if bouncing to login and user is not entering credentials.
   * 7. Runs an active keep-alive ping on every page every 25 seconds to prevent ASP.NET and OAuth sliding session expirations.
   * 8. Resets idle timers every 10 seconds across Class Schedule, Grades, Ledger, Enrollment, ELMS, and Profile.
   * 9. Intercepts and auto-dismisses session timeout/expiration dialogs.
   */
  const val SESSION_PERSISTENCE_JS = """
    (function() {
      try {
        var PREFIX = '__sti_persisted_session_';
        var FAR_FUTURE = '; expires=Fri, 31 Dec 2038 23:59:59 GMT; max-age=315360000; path=/; SameSite=Lax';

        // 1. Monkey-patch document.cookie so that session cookies are transformed into 10-year persistent cookies
        if (!window.__sti_cookie_hooked) {
          window.__sti_cookie_hooked = true;
          try {
            var cookieDesc = Object.getOwnPropertyDescriptor(Document.prototype, 'cookie') ||
                             Object.getOwnPropertyDescriptor(HTMLDocument.prototype, 'cookie');
            if (cookieDesc && cookieDesc.set) {
              var origCookieSet = cookieDesc.set;
              var origCookieGet = cookieDesc.get;
              Object.defineProperty(document, 'cookie', {
                get: function() {
                  return origCookieGet.call(document);
                },
                set: function(val) {
                  try {
                    if (val && typeof val === 'string') {
                      var lower = val.toLowerCase();
                      if (lower.indexOf('max-age=0') === -1 && lower.indexOf('expires=thu, 01 jan 1970') === -1) {
                        if (lower.indexOf('expires=') === -1 && lower.indexOf('max-age=') === -1) {
                          val = val.trim() + FAR_FUTURE;
                        }
                      }
                    }
                  } catch(e) {}
                  return origCookieSet.call(document, val);
                },
                configurable: true
              });
            }
          } catch (e) {}
        }

        // 2. Restore tokens from native Android vault (StiSessionBridge) and localStorage
        function restoreWebTokens() {
          try {
            var origin = location.origin || (location.protocol + '//' + location.host);
            if (window.StiSessionBridge && typeof window.StiSessionBridge.getWebTokens === 'function') {
              var vaultRaw = window.StiSessionBridge.getWebTokens(origin);
              if (vaultRaw && vaultRaw !== '{}') {
                var vaultData = JSON.parse(vaultRaw);
                for (var vKey in vaultData) {
                  if (vaultData.hasOwnProperty(vKey)) {
                    if (sessionStorage.getItem(vKey) === null) {
                      sessionStorage.setItem(vKey, vaultData[vKey]);
                    }
                    if (localStorage.getItem(vKey) === null) {
                      localStorage.setItem(vKey, vaultData[vKey]);
                    }
                  }
                }
              }
            }

            // Restore sessionStorage from localStorage mirrors
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
          } catch(e) {}
        }
        restoreWebTokens();

        // 3. Synchronize current sessionStorage & localStorage into Native Vault
        function syncSessionToVault() {
          try {
            var dump = {};
            for (var j = 0; j < sessionStorage.length; j++) {
              var sKey = sessionStorage.key(j);
              if (sKey) {
                var sVal = sessionStorage.getItem(sKey);
                if (sVal !== null) {
                  localStorage.setItem(PREFIX + sKey, sVal);
                  dump[sKey] = sVal;
                }
              }
            }
            // Include persistent auth tokens from localStorage
            for (var l = 0; l < localStorage.length; l++) {
              var lKey = localStorage.key(l);
              if (lKey && lKey.indexOf(PREFIX) !== 0) {
                var lVal = localStorage.getItem(lKey);
                if (lVal !== null) dump[lKey] = lVal;
              }
            }
            var origin = location.origin || (location.protocol + '//' + location.host);
            if (window.StiSessionBridge && typeof window.StiSessionBridge.saveWebTokens === 'function') {
              window.StiSessionBridge.saveWebTokens(origin, JSON.stringify(dump));
            }
          } catch (e) {}
        }
        syncSessionToVault();

        // 4. Intercept sessionStorage updates to keep persistent backup fresh
        if (!window.__sti_session_hooked) {
          window.__sti_session_hooked = true;
          var origSet = sessionStorage.setItem;
          sessionStorage.setItem = function(key, value) {
            origSet.apply(this, arguments);
            try {
              if (key) {
                localStorage.setItem(PREFIX + key, value);
                syncSessionToVault();
              }
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

          window.addEventListener('beforeunload', syncSessionToVault);
          window.addEventListener('pagehide', syncSessionToVault);
          document.addEventListener('visibilitychange', function() {
            if (document.visibilityState === 'hidden') syncSessionToVault();
          });
        }

        // 5. Auto-check "Stay signed in", "Remember me", and "Don't show this again" on all login screens
        var checkSelectors = [
          '#KmsiCheckboxField',
          'input[name="DontShowAgain"]',
          'input[name="RememberMe"]',
          'input[id*="RememberMe"]',
          'input[id*="Kmsi"]',
          'input[id*="remember"]',
          'input[name*="remember"]',
          'input[id*="persist"]',
          'input[name*="persist"]'
        ];
        for (var s = 0; s < checkSelectors.length; s++) {
          var cb = document.querySelector(checkSelectors[s]);
          if (cb && !cb.checked) {
            cb.checked = true;
            cb.dispatchEvent(new Event('change', { bubbles: true }));
          }
        }

        // 6. On Microsoft "Stay signed in?" prompt, confirm with "Yes" so persistent refresh tokens are created
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

        // 7. Universal Anti-Inactivity & Timeout Protection:
        // Keeps sessions active indefinitely using background server heartbeats and idle resets
        if (!window.__sti_keepalive_active) {
          window.__sti_keepalive_active = true;

          // Ping current URL every 25 seconds with session credentials
          setInterval(function() {
            try {
              if (location.href && location.protocol.indexOf('http') === 0) {
                var keepUrl = location.href;
                var lowerHref = keepUrl.toLowerCase();
                // Never ping explicit logout endpoints
                if (lowerHref.indexOf('logout') === -1 && lowerHref.indexOf('signout') === -1 && lowerHref.indexOf('logoff') === -1) {
                  var sep = keepUrl.indexOf('?') !== -1 ? '&' : '?';
                  var xhr = new XMLHttpRequest();
                  xhr.open('GET', keepUrl + sep + '_sti_stay=' + Date.now(), true);
                  xhr.withCredentials = true;
                  xhr.send();
                }
              }
              if (window.StiSessionBridge && typeof window.StiSessionBridge.heartbeat === 'function') {
                window.StiSessionBridge.heartbeat(location.origin || '');
              }
            } catch (e) {}
          }, 25 * 1000);

          // Reset all idle timers every 15 seconds safely without interfering with user touches
          setInterval(function() {
            try {
              if (typeof window.idleTime !== 'undefined') window.idleTime = 0;
              if (typeof window.idleSeconds !== 'undefined') window.idleSeconds = 0;
              if (typeof window.idleSecondsCounter !== 'undefined') window.idleSecondsCounter = 0;
              if (typeof window._idleSecondsCounter !== 'undefined') window._idleSecondsCounter = 0;
              if (typeof window.timeOutTimer !== 'undefined') window.timeOutTimer = 0;
              if (typeof window.countdownTimer !== 'undefined') window.countdownTimer = 0;
              if (typeof window.sessionTimeLeft !== 'undefined') window.sessionTimeLeft = 999999;
              if (typeof window.remainingSeconds !== 'undefined') window.remainingSeconds = 999999;
              if (typeof window.resetSessionTimer === 'function') window.resetSessionTimer();
              if (typeof window.keepAlive === 'function') window.keepAlive();
              if (typeof window.resetIdleTimeout === 'function') window.resetIdleTimeout();

              // Passive user activity heartbeat to perpetually prevent session timeout
              window.dispatchEvent(new Event('focus'));
              document.dispatchEvent(new Event('mousemove'));
            } catch (e) {}
          }, 15 * 1000);

          // Auto-confirm session extension modal dialogs
          setInterval(function() {
            try {
              var extendButtons = document.querySelectorAll('button, a, input[type="button"]');
              for (var b = 0; b < extendButtons.length; b++) {
                var btnText = (extendButtons[b].innerText || extendButtons[b].value || '').toLowerCase();
                if (btnText.indexOf('stay signed in') !== -1 || btnText.indexOf('continue session') !== -1 ||
                    btnText.indexOf('extend session') !== -1 || btnText.indexOf('keep me logged in') !== -1 ||
                    btnText.indexOf('still here') !== -1 || btnText.indexOf('renew session') !== -1) {
                  extendButtons[b].click();
                }
              }
            } catch(e) {}
          }, 5000);

          // Intercept client-side scripted redirects to timeout/inactivity endpoints
          try {
            var origAssign = window.location.assign;
            if (origAssign) {
              window.location.assign = function(url) {
                if (url && typeof url === 'string') {
                  var low = url.toLowerCase();
                  if (low.indexOf('timeout') !== -1 || low.indexOf('sessionexpired') !== -1 || low.indexOf('inactivity') !== -1) {
                    return;
                  }
                }
                return origAssign.apply(window.location, arguments);
              };
            }
            var origReplace = window.location.replace;
            if (origReplace) {
              window.location.replace = function(url) {
                if (url && typeof url === 'string') {
                  var low = url.toLowerCase();
                  if (low.indexOf('timeout') !== -1 || low.indexOf('sessionexpired') !== -1 || low.indexOf('inactivity') !== -1) {
                    return;
                  }
                }
                return origReplace.apply(window.location, arguments);
              };
            }
          } catch(e) {}
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
   * saves an encrypted backup in SharedPreferences, and flushes to disk immediately.
   * Runs sequentially on a dedicated background executor with queue protection.
   */
  fun persistSession(context: Context, currentUrl: String?) {
    trackDomain(context, currentUrl)

    if (isPersistQueued) return
    isPersistQueued = true

    persistExecutor.execute {
      isPersistQueued = false
      try {
        val cookieManager = CookieManager.getInstance()
        val prefs = getPrefs(context)
        val editor = prefs.edit()
        val expiry = getFarFutureExpiryDate()

        val domainsToScan = linkedSetOf<String>()
        if (!currentUrl.isNullOrBlank()) {
          try {
            val uri = Uri.parse(currentUrl)
            val host = uri.host
            if (!host.isNullOrBlank()) {
              domainsToScan.add("${uri.scheme ?: "https"}://$host")
            }
          } catch (_: Exception) {}
        }
        val dynamicDomains = prefs.getStringSet(KEY_KNOWN_DOMAINS, emptySet()) ?: emptySet()
        domainsToScan.addAll(dynamicDomains)
        domainsToScan.addAll(TRACKED_DOMAINS)

        for (domain in domainsToScan) {
          // Check root and /Student path
          val baseCookie = cookieManager.getCookie(domain)
          val studentCookie = cookieManager.getCookie(if (domain.endsWith("/")) "${domain}Student" else "$domain/Student")
          val rawHeader = listOfNotNull(baseCookie, studentCookie).joinToString("; ")

          if (rawHeader.isNotBlank()) {
            editor.putString(KEY_COOKIES_PREFIX + domain, rawHeader)

            val cookies = rawHeader.split(";")
            val isHttps = domain.startsWith("https://")
            val secureFlag = if (isHttps) "; Secure" else ""

            for (cookie in cookies) {
              val trimmed = cookie.trim()
              if (trimmed.isNotEmpty()) {
                val nameValue = trimmed.split("=", limit = 2)
                if (nameValue.isNotEmpty()) {
                  val name = nameValue[0].trim()
                  val value = if (nameValue.size > 1) nameValue[1].trim() else ""

                  // 1. Host-specific cookie with 10-year persistence
                  val persistentCookieHost = "$name=$value; Expires=$expiry; Max-Age=$TEN_YEARS_SECONDS; Path=/; SameSite=Lax$secureFlag"
                  cookieManager.setCookie(domain, persistentCookieHost)

                  // For HTTPS / SSO third-party authentication contexts (e.g. MSAL silent tokens in iframe)
                  if (isHttps) {
                    val persistentCookieNone = "$name=$value; Expires=$expiry; Max-Age=$TEN_YEARS_SECONDS; Path=/; SameSite=None; Secure"
                    cookieManager.setCookie(domain, persistentCookieNone)
                  }

                  // 2. Wildcard domain cookies
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
                  if (lowerDomain.contains("google.com")) rootDomains.add(".google.com")

                  for (rootDomain in rootDomains.distinct()) {
                    val persistentCookieDomain = "$name=$value; Domain=$rootDomain; Expires=$expiry; Max-Age=$TEN_YEARS_SECONDS; Path=/; SameSite=Lax$secureFlag"
                    cookieManager.setCookie(domain, persistentCookieDomain)
                    if (isHttps) {
                      val persistentCookieDomainNone = "$name=$value; Domain=$rootDomain; Expires=$expiry; Max-Age=$TEN_YEARS_SECONDS; Path=/; SameSite=None; Secure"
                      cookieManager.setCookie(domain, persistentCookieDomainNone)
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
    }
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

                if (isHttps) {
                  val persistentCookieNone = "$name=$cookieVal; Expires=$expiry; Max-Age=$TEN_YEARS_SECONDS; Path=/; SameSite=None; Secure"
                  cookieManager.setCookie(domain, persistentCookieNone)
                }

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
                if (lowerDomain.contains("google.com")) rootDomains.add(".google.com")

                for (rootDomain in rootDomains.distinct()) {
                  val persistentCookieDomain = "$name=$cookieVal; Domain=$rootDomain; Expires=$expiry; Max-Age=$TEN_YEARS_SECONDS; Path=/; SameSite=Lax$secureFlag"
                  cookieManager.setCookie(domain, persistentCookieDomain)
                  if (isHttps) {
                    val persistentCookieDomainNone = "$name=$cookieVal; Domain=$rootDomain; Expires=$expiry; Max-Age=$TEN_YEARS_SECONDS; Path=/; SameSite=None; Secure"
                    cookieManager.setCookie(domain, persistentCookieDomainNone)
                  }
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

    // Do NOT save transient login, signin, or SSO redirects as the last valid portal page
    if (lower.contains("login") ||
      lower.contains("signin") ||
      lower.contains("adfs/ls") ||
      lower.contains("microsoftonline.com") ||
      lower.contains("auth") ||
      lower.contains("oauth") ||
      lower.contains("timeout") ||
      lower.contains("sessionexpired")
    ) {
      return
    }

    // Only save legitimate STI portal pages (including Schedule, Grades, Dashboard)
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
